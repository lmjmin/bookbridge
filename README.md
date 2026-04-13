📚 BookBridge

ISBN 기반 자동 입력 + 다차원 상세 검색 + 개인화 추천을 결합한 전공서적 거래 플랫폼

📌 프로젝트 소개

BookBridge는 대학생들이 전공서적을 보다 정확하고 효율적으로 거래할 수 있도록 개발된 웹 플랫폼입니다.

기존 중고서적 거래 서비스는

단일 키워드 검색
최신순 중심 노출
데이터 입력 오류

등의 문제로 인해 원하는 도서를 찾기 어렵다는 한계가 있었습니다.

이를 해결하기 위해 본 프로젝트는
ISBN 기반 자동 입력, 다차원 상세 검색, 하이브리드 개인화 추천 기능을 통합하여
데이터 품질과 사용자 경험을 동시에 개선하는 것을 목표로 합니다.

🎯 개발 목적
📖 도서 정보 자동 입력 → 입력 오류 및 누락 최소화
🔍 다차원 검색 → 정확하고 일관된 검색 결과 제공
🤖 개인화 추천 → 사용자 맞춤형 탐색 경험 제공
🧩 전체 흐름 연결 → 등록 → 검색 → 추천까지 하나의 UX로 통합
🚀 주요 기능
1️⃣ ISBN 기반 자동 입력
제목 키워드 입력 → Kakao Book API 호출
도서 선택 시
👉 제목 / 저자 / 출판사 / ISBN 자동 입력
API 실패 시 수동 입력 지원
입력 시간 단축 + 데이터 정확도 향상
2️⃣ 다차원 상세 검색
키워드 / 학과 / 가격 / 상태 / 이미지 여부 / ISBN 검색
여러 조건 동시 적용 가능
📱 모바일 ISBN 바코드 스캔 지원
재현성 높은 검색 결과 제공
3️⃣ 하이브리드 개인화 추천
최근 조회 기반 추천
저자 정보 + 사용자 행동 데이터 활용
관심 도서 자동 노출
검색 없이도 자연스러운 탐색 가능
4️⃣ 회원 인증 및 보안
이메일 인증 코드 기반 회원가입
BCrypt 비밀번호 암호화
로그인 시 안전한 인증 처리
🛠 기술 스택
Backend
Java 17
Spring Boot
Spring Security
Spring Data JPA
Lombok
Frontend
HTML5
CSS3
JavaScript
Database
MySQL
External API
Kakao Book Search API
Server
Embedded Tomcat
🗄 데이터베이스 구조 (ERD)
📌 핵심 테이블
users : 사용자 정보
books : ISBN 기반 도서 메타데이터
book_listings : 판매글
wishlist : 찜 기능
dm_threads : 채팅 스레드
dm_messages : 메시지
transactions : 거래 정보
reviews : 평점
v_user_rating_summary : 사용자 평점 집계
📌 설계 특징
ISBN 기반 메타데이터 분리 (books)
사용자–판매글–거래 흐름 연결 구조
찜 / 채팅 / 거래 / 평점까지 전체 흐름 관리
검색 + 추천을 위한 데이터 통합 구조
🔄 시스템 구조
Controller → Service → Repository
REST API 기반 통신
계층 분리로 유지보수성 향상
보안 및 데이터 흐름 분리
📷 주요 기능 흐름
도서 등록
→ ISBN 자동 입력
→ 판매글 생성
→ 다차원 검색
→ 개인화 추천
→ 거래
→ 리뷰
⚡ 실행 방법
1️⃣ 프로젝트 클론
git clone https://github.com/your-repo/bookbridge.git
2️⃣ DB 설정
MySQL 설치
DB 생성 후 application.yml 설정
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bookbridge
    username: root
    password: 1234
3️⃣ 실행
./gradlew bootRun

또는 IDE에서 실행

4️⃣ 접속
http://localhost:8080
📈 기대 효과
📊 데이터 정확도 향상 (ISBN 기반 자동 입력)
🔍 검색 효율 증가 (다차원 필터)
🤖 사용자 경험 향상 (개인화 추천)
⏱ 탐색 시간 감소
🧠 프로젝트 의의

BookBridge는 단순한 중고거래 플랫폼이 아니라,

👉 데이터 기반 검색 + 추천 시스템이 결합된 플랫폼

으로
기존 서비스의 한계를 개선한 지능형 거래 시스템입니다.

👨‍💻 개발자
이보람
이민주
김지훈
📌 한줄 정리

“정확한 데이터 입력 → 정확한 검색 → 개인화 추천까지 이어지는 전공서적 거래 플랫폼”
