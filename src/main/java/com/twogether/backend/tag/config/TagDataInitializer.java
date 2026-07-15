package com.twogether.backend.tag.config;

import com.twogether.backend.tag.domain.Tag;
import com.twogether.backend.tag.domain.TagType;
import com.twogether.backend.tag.repository.TagRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class TagDataInitializer implements CommandLineRunner {

    private final TagRepository tagRepository;

    public TagDataInitializer(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        initializeHobbyTags();
        initializeSkillTags();
    }

    private void initializeHobbyTags() {
        if (tagRepository.existsByType(TagType.HOBBY)) {
            return;
        }

        tagRepository.saveAll(
                List.of(
                        new Tag("스터디", TagType.HOBBY),
                        new Tag("공부", TagType.HOBBY),
                        new Tag("어학/외국어", TagType.HOBBY),
                        new Tag("프로그래밍/IT", TagType.HOBBY),
                        new Tag("재테크/주식", TagType.HOBBY),
                        new Tag("사이드 프로젝트", TagType.HOBBY),
                        new Tag("독서", TagType.HOBBY),
                        new Tag("글쓰기/필사", TagType.HOBBY),
                        new Tag("운동/헬스", TagType.HOBBY),
                        new Tag("러닝/산책", TagType.HOBBY),
                        new Tag("등산/트레킹", TagType.HOBBY),
                        new Tag("클라이밍", TagType.HOBBY),
                        new Tag("요가/필라테스", TagType.HOBBY),
                        new Tag("수영", TagType.HOBBY),
                        new Tag("자전거", TagType.HOBBY),
                        new Tag("배드민턴", TagType.HOBBY),
                        new Tag("테니스/스쿼시", TagType.HOBBY),
                        new Tag("축구/풋살", TagType.HOBBY),
                        new Tag("농구", TagType.HOBBY),
                        new Tag("볼링", TagType.HOBBY),
                        new Tag("포켓볼/당구", TagType.HOBBY),
                        new Tag("골프", TagType.HOBBY),
                        new Tag("서핑", TagType.HOBBY),
                        new Tag("보드/스키", TagType.HOBBY),
                        new Tag("맛집 탐방", TagType.HOBBY),
                        new Tag("카페 투어", TagType.HOBBY),
                        new Tag("요리/베이킹", TagType.HOBBY),
                        new Tag("와인/위스키", TagType.HOBBY),
                        new Tag("전통주/맥주", TagType.HOBBY),
                        new Tag("비건/식단", TagType.HOBBY),
                        new Tag("파인다이닝", TagType.HOBBY),
                        new Tag("커피/차(Tea)", TagType.HOBBY),
                        new Tag("영화/OTT", TagType.HOBBY),
                        new Tag("전시회/미술관", TagType.HOBBY),
                        new Tag("뮤지컬/연극", TagType.HOBBY),
                        new Tag("콘서트/페스티벌", TagType.HOBBY),
                        new Tag("음악 감상", TagType.HOBBY),
                        new Tag("악기 연주", TagType.HOBBY),
                        new Tag("그림/드로잉", TagType.HOBBY),
                        new Tag("사진 촬영", TagType.HOBBY),
                        new Tag("캘리그라피", TagType.HOBBY),
                        new Tag("공예/DIY", TagType.HOBBY),
                        new Tag("꽃꽂이/플라워 클래스", TagType.HOBBY),
                        new Tag("보드게임", TagType.HOBBY),
                        new Tag("콘솔/PC 게임", TagType.HOBBY),
                        new Tag("모바일 게임", TagType.HOBBY),
                        new Tag("방탈출", TagType.HOBBY),
                        new Tag("코인노래방", TagType.HOBBY),
                        new Tag("만화/애니메이션", TagType.HOBBY),
                        new Tag("국내 여행", TagType.HOBBY),
                        new Tag("해외 여행", TagType.HOBBY),
                        new Tag("드라이브", TagType.HOBBY),
                        new Tag("캠핑/글램핑", TagType.HOBBY),
                        new Tag("반려동물", TagType.HOBBY),
                        new Tag("식물 집사/가드닝", TagType.HOBBY),
                        new Tag("다이어리 꾸미기", TagType.HOBBY),
                        new Tag("명상", TagType.HOBBY),
                        new Tag("야식", TagType.HOBBY),
                        new Tag("쇼핑/패션", TagType.HOBBY)
                )
        );
    }

    private void initializeSkillTags() {
        if (tagRepository.existsByType(TagType.SKILL)) {
            return;
        }

        tagRepository.saveAll(
                List.of(
                        new Tag("Spring Boot", TagType.SKILL),
                        new Tag("Java", TagType.SKILL),
                        new Tag("Node.js", TagType.SKILL),
                        new Tag("Python", TagType.SKILL),
                        new Tag("Go", TagType.SKILL),
                        new Tag("NestJS", TagType.SKILL),
                        new Tag("Express", TagType.SKILL),
                        new Tag("Django", TagType.SKILL),
                        new Tag("FastAPI", TagType.SKILL),
                        new Tag("MySQL", TagType.SKILL),
                        new Tag("PostgreSQL", TagType.SKILL),
                        new Tag("MongoDB", TagType.SKILL),
                        new Tag("Redis", TagType.SKILL),
                        new Tag("Docker", TagType.SKILL),
                        new Tag("Kubernetes", TagType.SKILL),
                        new Tag("AWS", TagType.SKILL),
                        new Tag("GCP", TagType.SKILL),
                        new Tag("Linux", TagType.SKILL),
                        new Tag("GraphQL", TagType.SKILL),
                        new Tag("RESTful API", TagType.SKILL),

                        new Tag("React", TagType.SKILL),
                        new Tag("Next.js", TagType.SKILL),
                        new Tag("Vue.js", TagType.SKILL),
                        new Tag("TypeScript", TagType.SKILL),
                        new Tag("JavaScript", TagType.SKILL),
                        new Tag("HTML5/CSS3", TagType.SKILL),
                        new Tag("Tailwind CSS", TagType.SKILL),
                        new Tag("Flutter", TagType.SKILL),
                        new Tag("React Native", TagType.SKILL),
                        new Tag("Swift", TagType.SKILL),
                        new Tag("Kotlin", TagType.SKILL),
                        new Tag("Redux", TagType.SKILL),
                        new Tag("Zustand", TagType.SKILL),

                        new Tag("Figma", TagType.SKILL),
                        new Tag("Adobe Photoshop", TagType.SKILL),
                        new Tag("Adobe Illustrator", TagType.SKILL),
                        new Tag("UI/UX 디자인", TagType.SKILL),
                        new Tag("웹 디자인", TagType.SKILL),
                        new Tag("브랜딩", TagType.SKILL),
                        new Tag("프로토타이핑", TagType.SKILL),

                        new Tag("영상 편집", TagType.SKILL),
                        new Tag("Premiere Pro", TagType.SKILL),
                        new Tag("After Effects", TagType.SKILL),
                        new Tag("Final Cut Pro", TagType.SKILL),
                        new Tag("유튜브 기획", TagType.SKILL),
                        new Tag("촬영/조명", TagType.SKILL),
                        new Tag("모션 그래픽", TagType.SKILL),
                        new Tag("3D 모델링/Blender", TagType.SKILL),

                        new Tag("데이터 분석", TagType.SKILL),
                        new Tag("SQL", TagType.SKILL),
                        new Tag("R", TagType.SKILL),
                        new Tag("머신러닝", TagType.SKILL),
                        new Tag("딥러닝", TagType.SKILL),
                        new Tag("AI 프롬프트 엔지니어링", TagType.SKILL),
                        new Tag("PyTorch", TagType.SKILL),
                        new Tag("TensorFlow", TagType.SKILL),
                        new Tag("Pandas", TagType.SKILL),

                        new Tag("서비스 기획", TagType.SKILL),
                        new Tag("프로젝트 관리(PM)", TagType.SKILL),
                        new Tag("Agile/Scrum", TagType.SKILL),
                        new Tag("QA/테스트", TagType.SKILL),
                        new Tag("데이터 기반 마케팅", TagType.SKILL),
                        new Tag("GA4", TagType.SKILL),
                        new Tag("SEO", TagType.SKILL),

                        new Tag("비즈니스 영어", TagType.SKILL),
                        new Tag("커뮤니케이션", TagType.SKILL),
                        new Tag("문제 해결 능력", TagType.SKILL),
                        new Tag("팀워크", TagType.SKILL),
                        new Tag("발표/프레젠테이션", TagType.SKILL),
                        new Tag("기술 블로그 작성", TagType.SKILL),

                        new Tag("퍼스널 트레이닝(PT)", TagType.SKILL),
                        new Tag("스포츠 코칭", TagType.SKILL),
                        new Tag("필라테스 지도", TagType.SKILL),
                        new Tag("요가 티칭", TagType.SKILL),
                        new Tag("응급처치/CPR", TagType.SKILL),
                        new Tag("라이프가드", TagType.SKILL),
                        new Tag("아웃도어 가이드", TagType.SKILL),

                        new Tag("보컬 트레이닝", TagType.SKILL),
                        new Tag("피아노 연주", TagType.SKILL),
                        new Tag("기타 연주", TagType.SKILL),
                        new Tag("작곡/편곡", TagType.SKILL),
                        new Tag("MIDI/작곡 프로그램", TagType.SKILL),
                        new Tag("댄스/안무 창작", TagType.SKILL),
                        new Tag("스피치/성우", TagType.SKILL),

                        new Tag("캘리그라피 지도", TagType.SKILL),
                        new Tag("일러스트레이션", TagType.SKILL),
                        new Tag("도자기 공예", TagType.SKILL),
                        new Tag("가죽 공예", TagType.SKILL),
                        new Tag("플라워 디렉팅/꽃꽂이", TagType.SKILL),
                        new Tag("다이어리 꾸미기(다꾸)", TagType.SKILL),
                        new Tag("재봉/자수", TagType.SKILL),

                        new Tag("한식 요리", TagType.SKILL),
                        new Tag("양식 요리", TagType.SKILL),
                        new Tag("베이킹/제과제빵", TagType.SKILL),
                        new Tag("바리스타/커피 추출", TagType.SKILL),
                        new Tag("조주(조주기능사/칵테일)", TagType.SKILL),
                        new Tag("와인 테이스팅/소믈리에", TagType.SKILL),

                        new Tag("영어 회화", TagType.SKILL),
                        new Tag("일본어 회화", TagType.SKILL),
                        new Tag("중국어 회화", TagType.SKILL),
                        new Tag("동시통역/번역", TagType.SKILL),
                        new Tag("비즈니스 작문", TagType.SKILL),
                        new Tag("토론/퍼실리테이션", TagType.SKILL),

                        new Tag("퍼스널 컬러 진단", TagType.SKILL),
                        new Tag("메이크업", TagType.SKILL),
                        new Tag("헤어 스타일링", TagType.SKILL),
                        new Tag("패션 스타일링", TagType.SKILL),
                        new Tag("의류 리폼", TagType.SKILL),

                        new Tag("카메라 조작/촬영", TagType.SKILL),
                        new Tag("드론 조종/촬영", TagType.SKILL),
                        new Tag("인물 프로필 촬영", TagType.SKILL),
                        new Tag("제품 사진 촬영", TagType.SKILL),

                        new Tag("반려동물 행동 교정", TagType.SKILL),
                        new Tag("반려동물 미용", TagType.SKILL),
                        new Tag("정리수납 전문가", TagType.SKILL),
                        new Tag("원예/가드닝", TagType.SKILL)
                )
        );
    }
}