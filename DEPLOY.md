# 2gether Backend 배포 가이드

## 1. 배포 환경

- 클라우드: AWS Lightsail
- 운영체제: Ubuntu 24.04
- Java: 17
- 데이터베이스: PostgreSQL 16
- 데이터베이스 실행 방식: Docker
- Spring Boot 실행 방식: systemd
- 현재 배포 기준 브랜치: `dev`

---

## 2. 외부 접속 주소

- API Base URL: `http://<SERVER_IP>:8080`
- Swagger: `http://<SERVER_IP>:8080/swagger-ui/index.html`

> 실제 서버 IP는 공개 문서에 기록하지 않고 팀 내부 채널에서 공유합니다.

---

## 3. Spring Boot 서버 정보

- systemd 서비스 이름: `twogether-backend`
- 프로젝트 경로: `/home/ubuntu/app/2gether_BE`
- 실행 파일:

```text
/home/ubuntu/app/2gether_BE/build/libs/backend-0.0.1-SNAPSHOT.jar
```

- 환경변수 파일:

```text
/home/ubuntu/postgres.env
```

> 환경변수 파일에는 DB 비밀번호 등 민감정보가 포함되므로 Git에 업로드하지 않습니다.

---

## 4. PostgreSQL Docker 정보

- 컨테이너 이름: `twogether-postgres`
- 이미지: `postgres:16`
- Docker 볼륨 이름: `twogether-postgres-data`
- 서버 연결 포트: `5433`
- PostgreSQL 컨테이너 내부 포트: `5432`

연결 구조:

```text
Spring Boot
→ localhost:5433
→ Docker 포트 연결
→ PostgreSQL 컨테이너:5432
```

---

## 5. 최신 코드 재배포

서버에 SSH로 접속한 뒤 실행합니다.

```bash
cd /home/ubuntu/app/2gether_BE

git checkout dev
git pull origin dev

./gradlew clean build

sudo systemctl restart twogether-backend
```

서버 상태 확인:

```bash
sudo systemctl status twogether-backend
```

상태 화면 종료:

```text
q
```

---

## 6. 서버 로그 확인

실시간 로그:

```bash
sudo journalctl -u twogether-backend -f
```

최근 로그 확인:

```bash
sudo journalctl -u twogether-backend -n 100
```

실시간 로그 종료:

```text
Ctrl + C
```

---

## 7. Docker 상태 확인

실행 중인 컨테이너 확인:

```bash
sudo docker ps
```

모든 컨테이너 확인:

```bash
sudo docker ps -a
```

PostgreSQL 로그 확인:

```bash
sudo docker logs twogether-postgres
```

Docker 볼륨 확인:

```bash
sudo docker volume ls
```

---

## 8. 주의사항

다음 정보는 절대 GitHub에 업로드하지 않습니다.

- DB 실제 비밀번호
- JWT Secret
- AWS Access Key
- API Key
- GitHub Token
- OAuth Client Secret
- 실제 `.env` 파일
- 서버의 개인 인증키

또한 PostgreSQL 데이터가 필요한 상태에서는 아래 명령을 함부로 실행하지 않습니다.

```bash
sudo docker rm -f twogether-postgres
sudo docker volume rm twogether-postgres-data
```

특히 Docker 볼륨을 삭제하면 저장된 DB 데이터가 사라질 수 있습니다.