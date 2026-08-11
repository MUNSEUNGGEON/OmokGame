# OmokGame

Java Swing, TCP 소켓, Oracle Database로 구현한 멀티플레이 오목·채팅 애플리케이션입니다.

## 주요 기능

- 회원 가입, 로그인, 아이디·비밀번호 찾기, 프로필 관리
- 대기실과 게임방, 준비/시작, 플레이어 및 관전자
- 오목 착수와 승패 처리, 전적·랭킹, 복기 데이터
- 공개·개인·그룹 채팅, 이모티콘과 채팅 기록
- 관리자 회원 관리, 날씨 및 우편번호 검색

## 기술 스택

- Java 17, Java Swing
- TCP Socket/DataInputStream/DataOutputStream
- Oracle Database/JDBC
- Maven
- FlatLaf, JCalendar, org.json, JavaMail

## 프로젝트 구조

```text
scr/
├─ config/       환경 변수 설정
├─ core/         서버, 클라이언트, 방, 메시지 및 채팅
├─ DB/           JDBC 연결, 스키마 초기화 및 SQL
├─ ui/           Swing 화면
├─ resources/    이모티콘 리소스
└─ sound/        오디오 리소스와 재생 코드
img/             UI 이미지
Weather_img/     날씨 아이콘
docs/            기술·보안 보고서
```

`scr`은 기존 Eclipse 프로젝트와의 호환을 위해 유지한 소스 디렉터리이며 Maven의 `sourceDirectory`로 지정되어 있습니다.

## 환경 설정

실제 값은 저장소에 커밋하지 않습니다. [.env.example](.env.example)에 다음 변수 이름이 정리되어 있습니다.

- `OMOK_DB_URL`, `OMOK_DB_USER`, `OMOK_DB_PASSWORD`
- `OMOK_ADMIN_ID`, `OMOK_ADMIN_PASSWORD`
- `OMOK_SMTP_FROM`, `OMOK_SMTP_APP_PASSWORD`
- `OMOK_OPENWEATHER_API_KEY`, `OMOK_POSTAL_API_KEY`
- 선택: `OMOK_POSTAL_API_URL`

IDE 실행 설정이나 운영체제 환경 변수로 값을 주입하세요. `.env` 파일은 Java가 자동으로 읽지 않으므로 사용할 경우 IDE 플러그인 또는 별도 로더가 필요합니다.

## 빌드

```bash
mvn clean test
```

서버 엔트리 포인트는 `core.Server`, 클라이언트 엔트리 포인트는 `core.Client`입니다. Oracle에 접근하려면 먼저 환경 변수와 스키마를 준비해야 합니다.

## 보안 주의사항

이전 로컬 소스에 DB/API/SMTP 자격 증명이 직접 기록되어 있었습니다. 해당 값은 코드에서 제거했지만 소유자는 반드시 기존 키와 앱 비밀번호를 폐기하고 재발급해야 합니다. 비밀번호 평문 저장과 네트워크 평문 전송 등 남은 개선사항은 [SECURITY_REVIEW.md](docs/SECURITY_REVIEW.md)를 확인하세요.

## 문서

- [기술 보고서](docs/TECHNICAL_REPORT.md)
- [보안 검토](docs/SECURITY_REVIEW.md)
- [개선 계획](docs/IMPROVEMENT_PLAN.md)

