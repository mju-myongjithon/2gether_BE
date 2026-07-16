# 유대감(2gether) ERD 검토 & 백엔드 설계

검토 대상: `dbdiagram.txt`(설계), `2gether_BE` 실제 코드베이스, 화면 기획서.
환경: Spring Boot 3.5 · JPA(Hibernate 6) · PostgreSQL · Java 17.

> **가장 중요한 전제**: DBML과 실제 코드가 이미 여러 곳에서 어긋나 있습니다. 지금 설계 단계에서 이 둘을 **하나의 단일 소스로 통일**하는 게 최우선입니다. 아래 검토는 그 정합화를 기준으로 합니다.

---

## 0. 현재 구현 상태 스냅샷 (코드 기준)

| 도메인 | Entity | Repository | Service | Controller | 상태 |
|---|---|---|---|---|---|
| user / department / tag / availability / email_verification | ✅ | ✅ | ✅ | ✅ | 실제 구현됨 |
| **gathering** | ❌ | ❌ | ❌ | ⚠️ 더미 | **컨트롤러가 전부 하드코딩 응답** |
| gathering_member / application | ❌ | ❌ | ❌ | ⚠️ 더미 | enum·DTO만 존재 |
| chat / message / verification | ❌ | ❌ | ❌ | ⚠️ 더미 | enum·DTO만 존재 |
| **user_tag(취미/기술 선택 저장)** | ❌ | ❌ | — | — | **링크 엔티티 자체가 없음** |

즉 "새로 추가하려는 모임 기능"은 사실상 **gathering 도메인 전체를 처음 구현**하는 일입니다. 아래 설계 파트가 그 골격입니다.

---

# Part A. ERD 검토 (요청 1~9)

## 1. 요구사항 충족 검토

큰 뼈대(학교/사용자/모임/신청/멤버/채팅/인증 도메인 분리)는 잘 잡혀 있습니다. 다만 **핵심 기능 3개**에 데이터 공백이 있습니다.

1. **캠퍼스 비율(인문×자연 융합)** — 서비스의 정체성 기능인데, 실제 `User` 엔티티에 **`campus` 컬럼이 없습니다**(DBML엔 있으나 코드엔 누락). 비율을 내려면 매번 `users → department → college.campus` 조인이 필요해집니다.
2. **모임 태그/검색** — 기획서는 "키워드로 모임 탐색, 각 모임 태그 표시"를 요구하지만 `gathering`에 태그 관계가 **전혀 없습니다**.
3. **교류 시각화(잔디/캔버스)** — `verification.canvas_pixel_x/y`는 있으나, 조회용 인덱스가 없어 캔버스 전체 렌더링 쿼리가 풀스캔이 됩니다.

## 2. 부족한 테이블 · 컬럼 · 관계

| 구분 | 항목 | 이유 |
|---|---|---|
| 신규 테이블 | `gathering_tag(gathering_id, tag_id)` | 모임 태그 표시·검색 (기획 필수) |
| 신규 테이블 | `user_tag(user_id, tag_id)` | 취미/기술 선택 **저장 위치가 코드에 없음**. `tags.type`으로 구분되므로 링크는 1개로 통합 |
| 신규 테이블(선택) | `idea_recommended_department` | `idea.recommended_departments` text(CSV) 정규화 |
| 컬럼 복원 | `users.campus` | 캠퍼스 비율 집계 기준. 코드에 누락됨 |
| 컬럼 추가 | `gathering.current_members` | 목록의 "현재 인원" 매 조회 집계 부담 완화(캐시) |
| 컬럼 추가 | `chat_room_member.last_read_message_id` | 안 읽은 메시지 수 계산 |
| 관계 정리 | `gathering.category` → enum | 아래 3·정규화 참고 |

## 3. 정규화 관점

- **가장 큰 문제: DBML↔코드 이원화.** `tags`(코드는 단일 테이블 + `type`) vs `hobby_tag`/`skill_tag`(DBML 분리). **코드 방식(통합)이 옳습니다** — 태그 마스터를 두 벌 관리하지 않아도 되고 확장(예: `INTEREST`)이 쉽습니다. DBML을 코드에 맞춰 갱신하세요.
- `users` 컬럼 불일치: DBML `university_email`/`email_verified_at`(ts)/`bio` ↔ 코드 `school_email`/`email_verified`(boolean)/`introduction`. **코드 기준으로 DBML을 통일**. 인증 시각 이력이 필요하면 `email_verification` 테이블이 이미 그 역할을 하므로 users에는 boolean만 두면 됩니다(중복 저장 회피).
- `idea.recommended_departments`가 `text`(CSV 추정) — **1NF 위반**. 학과가 복수면 `idea_recommended_department` 링크로. 단순 감사 로그면 유지 가능(용도 확인).
- `users.campus`는 `department.college.campus`에서 유도 가능한 **파생값**입니다. 그래도 비율 집계 성능 때문에 **의도적 비정규화로 보존**을 권장하되, "학과 변경 시 campus 재계산"을 서비스 로직에 명문화해 정합성을 지키세요.
- `verification`의 사유 3필드(`review_text`/`ai_reason`/`rejection_reason`)는 역할이 달라 정규화 위반은 아니지만, 채워지는 주체·시점을 주석으로 못박아야 혼선이 없습니다.

## 4. JPA 구현상 어려운 구조

- **PostgreSQL native enum ↔ Hibernate**: DBML의 `Enum ...`을 실제 PG enum 타입으로 만들면 Hibernate `@Enumerated(STRING)`와 타입 캐스팅 충돌이 잦습니다. **`varchar` + `CHECK` 제약 + `@Enumerated(EnumType.STRING)`** 조합을 권장(코드도 이미 이 방식). native enum을 꼭 쓰려면 커스텀 `UserType`이 필요해 비용이 큽니다.
- **파생 상태(모집중/모집완료/상시모집)**: 컬럼이 아니라 `recruit_start_at`·`recruit_end_at`·`now()`로 계산되는 값. 엔티티에 넣지 말고 **응답 DTO 계산** 또는 `@Transient` 메서드로. 저장 `status`(RECRUITING/CONFIRMED/…)와 화면 표시 상태는 다른 개념임을 API에 명시.
- **캠퍼스 비율·현재 인원 집계**: 엔티티 컬렉션 매핑(`@OneToMany`)으로 세려 하면 N+1과 전체 로딩이 터집니다. **DTO Projection 쿼리**(`group by campus`, `count`)로 분리하세요.
- **FK 매핑 전략이 코드 안에서 불일치**: `Availability`는 `@ManyToOne User`, `User.departmentId`는 그냥 `Long`. 팀 규칙을 하나로 정하세요. 권장: **연관 탐색이 잦은 곳만 `@ManyToOne(LAZY)`, 단순 참조는 `Long` FK**. 모임 도메인은 host/member 탐색이 많으니 `@ManyToOne(LAZY)` 채택.
- `gathering_application` 승인 → `gathering_member` 생성 + `gathering.current_members` 증가 + (확정 시)`chat_room_member` 추가가 **한 트랜잭션**이어야 함. 서비스 계층에서 원자적으로.

## 5. 성능 리스크

- **모임 목록 검색**: `keyword`를 `LIKE '%..%'`로 하면 인덱스를 못 탑니다. 데이터가 커지면 `pg_trgm` GIN 인덱스 또는 PostgreSQL full-text(`tsvector`) 도입.
- **N+1**: 목록 카드마다 host 정보 + 태그 + 현재인원. host는 `fetch join`, 태그는 `in` 배치 조회, 현재인원은 캐시 컬럼(`current_members`)로 해결.
- **캠퍼스 비율 매 요청 집계**: 상세 진입마다 `group by`. 트래픽이 크면 확정 시점에 스냅샷을 남기는 것도 방법(초기엔 실시간 집계로 충분).
- **채팅 메시지 조회**: `chat_room_id` + 최신순 무한 스크롤은 OFFSET 페이징이 뒤로 갈수록 느려집니다. `(chat_room_id, id)` 기준 **커서(keyset) 페이징** 권장.
- **AI 추천 배치**: `gathering_recommendation`가 회차마다 누적 → `(user_id, round_no)` 인덱스 없으면 조회가 풀스캔.

## 6. 추가하면 좋은 인덱스

```
gathering            (status, category)      -- 목록 필터
gathering            (recruit_end_at)        -- 마감 임박/파생상태
gathering            (host_id)               -- 내가 만든 모임
gathering            (created_at)            -- 최신순 정렬
gathering_member     (user_id)               -- 내가 참여한 모임
gathering_application(user_id, status)       -- 내 신청 목록
gathering_application(gathering_id, status)  -- 방장 신청함
gathering_tag        (tag_id, gathering_id)  -- 태그로 모임 검색
message              (chat_room_id, id)      -- 방별 커서 페이징
chat_room_member     (user_id)               -- 내 채팅방 목록
verification         (ai_status)             -- 심사 대기 목록
verification         (canvas_pixel_x, y)     -- 캔버스 렌더링
users                (department_id), (campus)
gathering_recommendation (user_id, round_no)
-- 검색어(keyword)가 커지면: gathering.title/content 에 pg_trgm GIN
```

## 7. 삭제(CASCADE) · NULL · UNIQUE

**삭제 정책**

- **모임/사용자는 물리 삭제 안 함**(soft): 모임은 `status = CANCELED`, 사용자도 탈퇴 플래그 방식 권장. FK 기본은 `ON DELETE RESTRICT`.
- 모임에 **종속된** `gathering_image`, `gathering_tag`는 모임이 실제 삭제되는 경우에만 함께 제거 → JPA `@OneToMany(orphanRemoval = true, cascade = ALL)` 또는 DB `ON DELETE CASCADE`.
- `message.sender_id`는 사용자 삭제 시 `ON DELETE SET NULL`(SYSTEM 메시지처럼 처리).
- `gathering_recommendation`, `verification` 같은 로그성은 사용자 soft-delete와 무관하게 보존.

**NULL 허용(의도 명시 필요)**

- `gathering.recruit_start_at`/`recruit_end_at` → **NULL 허용**(즉시 모집/상시 모집 표현에 필수).
- `chat_room.gathering_id` → **NULL 허용**(QUICK_CONNECT).
- `message.sender_id` → NULL(SYSTEM).
- `users.department_id`, `campus`, `school_email` → 온보딩 이전 NULL.

**UNIQUE (정책 결정 포인트)**

- `gathering_application(gathering_id, user_id)` UNIQUE는 **REJECTED 후 재신청을 막습니다.** 재신청을 허용하려면 부분 유니크로 전환:
  `CREATE UNIQUE INDEX uq_app_active ON gathering_application(gathering_id, user_id) WHERE status IN ('PENDING','ACCEPTED');`
- `quick_connect_code.code` 전체 UNIQUE는 만료 코드 재발급과 충돌 → **활성 코드만 유일**:
  `CREATE UNIQUE INDEX uq_active_code ON quick_connect_code(code) WHERE status = 'ACTIVE';`
- 유지해야 할 것: `users.nickname`, `users.school_email`, `chat_room.gathering_id`, `gathering_member(gathering_id, user_id)`, `gathering_image(gathering_id, sort_order)`.

**CHECK**

- `gathering.max_members >= 1`, `current_members <= max_members`, `availabilities.start_time < end_time`.

## 8. 개선된 ERD

→ 별도 파일 **`docs/ERD_개선안.dbml`** 로 제공했습니다. dbdiagram.io에 그대로 붙여넣으면 됩니다. 변경 지점마다 `[개선]` 주석을 달아뒀습니다.

## 9. 수정 이유 요약

| 수정 | 이유 |
|---|---|
| `hobby_tag`/`skill_tag` → `tags` + `type` | 코드가 이미 통합. 마스터 이중관리 제거, 태그 종류 확장 용이 |
| `user_hobby_tag`/`user_skill_tag` → `user_tag` | 링크도 하나면 충분(type로 구분). **현재 미구현이라 신규 필요** |
| `gathering_tag` 신규 | 모임 태그/검색 요구사항(기획) 미충족 해소 |
| `users.campus` 복원 | 캠퍼스 비율(핵심 기능) 집계를 조인 없이 |
| `gathering.category` enum화 | 표기 흔들림 방지, 필터 신뢰성 |
| `gathering.current_members` | 목록 집계 부담 완화 |
| `chat_room_member.last_read_message_id` | 안 읽음 카운트 |
| users 컬럼 코드 기준 통일 | 단일 소스화(school_email/boolean/introduction) |
| 부분 유니크(application/quick_code) | 재신청·코드 재발급 현실 반영 |
| 인덱스 추가 | 목록/검색/채팅/캔버스 조회 성능 |

---

# Part B. 백엔드 설계 (gathering 도메인 기준)

기존 코드 컨벤션(`ApiResponse<T>` 래퍼, `ErrorCode`+`BusinessException`+`GlobalExceptionHandler`, record DTO, `PageResponse<T>`, 생성자 주입, `@Transactional(readOnly=true)` 기본)을 **그대로 따릅니다.**

## B-1. 패키지 구조 (기존 방식 유지)

```
gathering/
 ├─ domain/        Gathering, GatheringStatus, GatheringCategory
 ├─ dto/request/   GatheringCreateRequest, GatheringUpdateRequest
 ├─ dto/response/  GatheringCreateResponse, GatheringSummaryResponse, GatheringDetailResponse ...
 ├─ repository/    GatheringRepository (+ QueryDSL/Projection용 Custom)
 ├─ service/       GatheringService
 └─ controller/    GatheringController  (지금 더미 → 실제 연결)
```

## B-2. Entity

핵심 원칙: **연관 탐색이 잦은 host/gathering은 `@ManyToOne(LAZY)`**, enum은 `@Enumerated(STRING)`, 생성자로 불변식 강제, 상태 전이는 도메인 메서드로.

```java
@Entity
@Table(name = "gathering")
public class Gathering {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(columnDefinition = "text")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GatheringCategory category;

    private String location;

    @Column(name = "max_members", nullable = false)
    private short maxMembers = 6;

    @Column(name = "current_members", nullable = false)
    private short currentMembers = 1;

    @Column(name = "fusion_enabled", nullable = false)
    private boolean fusionEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GatheringStatus status = GatheringStatus.RECRUITING;

    private OffsetDateTime recruitStartAt;
    private OffsetDateTime recruitEndAt;
    private OffsetDateTime meetAt;
    private OffsetDateTime confirmedAt;
    private OffsetDateTime canceledAt;

    @Column(nullable = false) private OffsetDateTime createdAt;
    @Column(nullable = false) private OffsetDateTime updatedAt;

    // --- 상태 전이(방장만/모집중만 등 규칙은 서비스에서 선검증) ---
    public void confirm() {
        this.status = GatheringStatus.CONFIRMED;
        this.confirmedAt = OffsetDateTime.now();
        touch();
    }
    public void cancel() {
        this.status = GatheringStatus.CANCELED;
        this.canceledAt = OffsetDateTime.now();
        touch();
    }
    public void increaseMember() { this.currentMembers++; touch(); }
    public boolean isRecruiting() { return status == GatheringStatus.RECRUITING; }
    public boolean isHost(Long userId) { return host.getId().equals(userId); }
    private void touch() { this.updatedAt = OffsetDateTime.now(); }

    // 파생 표시 상태 — 저장하지 않음
    @Transient
    public String displayStatus(OffsetDateTime now) {
        if (status != GatheringStatus.RECRUITING) return status.name();
        if (recruitEndAt == null) return "ALWAYS";           // 상시모집
        if (recruitStartAt != null && now.isBefore(recruitStartAt)) return "UPCOMING";
        if (!now.isAfter(recruitEndAt)) return "RECRUITING";
        return "CLOSED";                                      // 모집완료
    }
}
```

> `created_at/updated_at`은 `@PrePersist/@PreUpdate` 또는 `@EntityListeners(AuditingEntityListener.class)` + `@CreationTimestamp`로 자동화 권장. `gathering_member`/`gathering_tag`는 각각 별도 엔티티로 두고, 목록 성능을 위해 컬렉션 매핑은 최소화합니다.

## B-3. Repository

```java
public interface GatheringRepository extends JpaRepository<Gathering, Long> {

    // 상세: host 를 함께 로딩해 N+1 회피
    @Query("select g from Gathering g join fetch g.host where g.id = :id")
    Optional<Gathering> findDetailById(@Param("id") Long id);

    // 목록: 동적 조건은 Specification 또는 QueryDSL 권장(아래는 예시)
    Page<Gathering> findByStatusAndCategory(
            GatheringStatus status, GatheringCategory category, Pageable pageable);
}

// 캠퍼스 비율 — Projection 전용 쿼리 (엔티티 컬렉션 매핑 대신)
public interface GatheringMemberRepository extends JpaRepository<GatheringMember, Long> {

    boolean existsByGatheringIdAndUserId(Long gatheringId, Long userId);

    @Query("""
        select u.campus as campus, count(m) as cnt
        from GatheringMember m join m.user u
        where m.gathering.id = :gid
        group by u.campus
    """)
    List<CampusCount> countByCampus(@Param("gid") Long gid);
}
```

동적 검색(category/status/keyword 조합)은 **QueryDSL** 또는 **JPA Specification**이 실무 표준입니다. keyword가 커지면 `pg_trgm` 네이티브 쿼리로 분리.

## B-4. Service

```java
@Service
@Transactional(readOnly = true)
public class GatheringService {

    private final GatheringRepository gatheringRepository;
    private final GatheringMemberRepository memberRepository;
    private final UserRepository userRepository;

    // 생성자 주입 (기존 컨벤션)

    @Transactional
    public GatheringCreateResponse create(String authUserId, GatheringCreateRequest req) {
        User host = userRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Gathering g = Gathering.create(host, req); // 정적 팩토리 + 검증
        gatheringRepository.save(g);

        // 방장을 HOST 멤버로 등록 (같은 트랜잭션)
        memberRepository.save(GatheringMember.host(g, host));
        return GatheringCreateResponse.from(g);
    }

    @Transactional
    public void cancel(String authUserId, Long gatheringId) {
        Gathering g = getOrThrow(gatheringId);
        Long me = currentUserId(authUserId);
        if (!g.isHost(me)) throw new BusinessException(ErrorCode.FORBIDDEN);
        if (!g.isRecruiting()) throw new BusinessException(ErrorCode.GATHERING_NOT_MODIFIABLE);
        g.cancel();
    }

    public GatheringDetailResponse getDetail(Long id, String authUserId) {
        Gathering g = gatheringRepository.findDetailById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.GATHERING_NOT_FOUND));
        List<CampusCount> ratio = memberRepository.countByCampus(id);
        // 내 신청/참여 상태 조회 후 조립
        return GatheringDetailResponse.of(g, ratio, ...);
    }
}
```

핵심: **읽기는 `readOnly=true` 기본, 쓰기 메서드만 `@Transactional`. 권한/상태 검증 → 도메인 메서드 호출 순서.** 신청 수락 같은 복합 처리(멤버 생성 + current_members 증가 + 채팅방 추가)는 한 트랜잭션 안에서.

## B-5. Controller (더미 → 실제)

기존 시그니처(`ResponseEntity<ApiResponse<T>>`)를 유지하고 인증 주체를 주입합니다. Supabase JWT를 쓰므로 `@AuthenticationPrincipal Jwt jwt`에서 `sub`(=auth_user_id)를 꺼내는 방식이 자연스럽습니다.

```java
@PostMapping
public ResponseEntity<ApiResponse<GatheringCreateResponse>> create(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody GatheringCreateRequest request) {

    var res = gatheringService.create(jwt.getSubject(), request);
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("모임이 생성되었습니다.", res));
}
```

## B-6. DTO

- 요청/응답 모두 **record + Bean Validation**. 예: `@NotBlank String title`, `@Size(max=120)`, `@Min(1) short maxMembers`, `@Valid List<...> images`.
- 검증 실패는 `MethodArgumentNotValidException` → `GlobalExceptionHandler`에 **핸들러 하나 추가**해서 `INVALID_REQUEST`로 변환(현재 미처리):

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
    String msg = e.getBindingResult().getFieldErrors().stream()
            .findFirst().map(FieldError::getDefaultMessage)
            .orElse(ErrorCode.INVALID_REQUEST.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(ErrorCode.INVALID_REQUEST.getCode(), msg));
}
```

- 응답 DTO는 엔티티를 직접 노출하지 말고 `static from/of` 팩토리로 변환(지금도 이 패턴).

## B-7. 전체 데이터 흐름 (모임 생성 예)

```
Client (JWT: Bearer)
  → GatheringController.create(jwt, request)
      · @Valid 로 형식 검증 → 실패 시 400 INVALID_REQUEST
  → GatheringService.create(authUserId, req)   [@Transactional]
      · authUserId 로 User 조회 (없으면 404 USER_NOT_FOUND)
      · Gathering.create(...) 도메인 검증(max_members>=1 등)
      · gatheringRepository.save(g)             INSERT gathering
      · memberRepository.save(HOST)             INSERT gathering_member
  ← GatheringCreateResponse.from(g)
  ← ApiResponse.success("모임이 생성되었습니다.", data)   201 Created
```

신청 수락 흐름: `accept()` → application.status=ACCEPTED + `gathering_member` INSERT + `gathering.current_members++` → (확정 상태면) 채팅방 멤버 추가, 모두 한 트랜잭션.

## B-8. API 설계 (Method · URI · Request · Response · 예외)

공통: 성공 `ApiResponse{success,message,data}`, 실패 `ErrorResponse{success,code,message}`, 목록 `PageResponse`, 날짜 ISO-8601, 인증 필요 API는 `Authorization: Bearer {jwt}`.

| Method | URI | Request | 성공 | 주요 예외 |
|---|---|---|---|---|
| POST | `/api/gatherings` | `{title, category, content, location, maxMembers, meetAt, recruitStartAt, recruitEndAt, fusionEnabled, tagIds[], imageUrls[]}` | 201 `{gatheringId, hostId, title, status, createdAt}` | 400 INVALID_REQUEST, 401 UNAUTHORIZED, 404 USER_NOT_FOUND |
| GET | `/api/gatherings` | query: `category, status, keyword, page=0, size=20` | 200 `PageResponse<GatheringSummary>` | 400 INVALID_REQUEST |
| GET | `/api/gatherings/{id}` | — | 200 `GatheringDetail{... members[], campusRatio, myApplicationStatus, isHost, isMember}` | 404 GATHERING_NOT_FOUND |
| PATCH | `/api/gatherings/{id}` | 수정 필드(부분) | 200 `{gatheringId, updatedAt}` | 403 FORBIDDEN, 409 GATHERING_NOT_MODIFIABLE(모집중 아님) |
| POST | `/api/gatherings/{id}/cancel` | — | 200 `{gatheringId, status, canceledAt}` | 403 FORBIDDEN, 409 GATHERING_NOT_MODIFIABLE |
| POST | `/api/gatherings/{id}/confirm` | — | 200 `{gatheringId, status, confirmedAt, chatRoomId}` | 403 FORBIDDEN, 409 GATHERING_NOT_MODIFIABLE |
| POST | `/api/gatherings/{id}/applications` | `{message}` | 201 `{applicationId, status}` | 409 DUPLICATE_APPLICATION, 409 ALREADY_MEMBER, 409 GATHERING_NOT_RECRUITING |
| GET | `/api/gatherings/{id}/applications` | (방장) page/size | 200 `PageResponse<Applicant>` | 403 FORBIDDEN |
| POST | `/api/gatherings/{id}/applications/{appId}/accept` | — | 200 `{applicationId, status, memberId}` | 403 FORBIDDEN, 409 CAPACITY_EXCEEDED |
| POST | `/api/gatherings/{id}/applications/{appId}/reject` | `{rejectReason}` | 200 `{applicationId, status}` | 403 FORBIDDEN |

**추가할 ErrorCode** (기존 enum에 이어서):

```
FORBIDDEN(403, "FORBIDDEN", "권한이 없습니다.")
GATHERING_NOT_FOUND(404, "GATHERING_NOT_FOUND", "모임을 찾을 수 없습니다.")
GATHERING_NOT_MODIFIABLE(409, "GATHERING_NOT_MODIFIABLE", "모집 중인 모임만 수정/취소할 수 있습니다.")
GATHERING_NOT_RECRUITING(409, "GATHERING_NOT_RECRUITING", "모집이 마감된 모임입니다.")
DUPLICATE_APPLICATION(409, "DUPLICATE_APPLICATION", "이미 신청한 모임입니다.")
ALREADY_MEMBER(409, "ALREADY_MEMBER", "이미 참여 중인 모임입니다.")
CAPACITY_EXCEEDED(409, "CAPACITY_EXCEEDED", "모집 정원을 초과했습니다.")
```

## B-9. 실무 권장 사항 (마무리)

- **`ddl-auto`를 운영에서 `update`로 두지 마세요.** 지금 `update`인데, 스키마 드리프트/데이터 손상 위험이 큽니다. **Flyway**(또는 Liquibase)로 마이그레이션을 버전 관리하고, 운영은 `validate`로. DBML → SQL DDL을 초기 마이그레이션으로.
- 인증 주체 접근을 `@AuthenticationPrincipal` 한 곳으로 통일(현재 더미 컨트롤러엔 인증 주체 주입이 없음).
- 목록/검색은 QueryDSL 도입을 권해요(동적 조건 3개 조합 + 정렬 고정).
- 동시성: 정원 초과 방지를 위해 수락 시 `SELECT ... FOR UPDATE`(비관적 락) 또는 `current_members` 조건부 UPDATE로 경쟁 방지.
```
