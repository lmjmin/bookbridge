# 📚 BookBridge

> ISBN 기반 자동 입력 + 다차원 상세 검색 + 개인화 추천을 결합한 전공서적 거래 플랫폼

---

## 📌 프로젝트 소개

BookBridge는 대학생들이 전공서적을 보다 정확하고 효율적으로 거래할 수 있도록 개발된 웹 플랫폼입니다.

기존 중고서적 거래 서비스는

- 단일 키워드 검색  
- 최신순 중심 노출  
- 데이터 입력 오류  

등의 문제로 인해 원하는 도서를 찾기 어렵다는 한계가 있었습니다.

이를 해결하기 위해 본 프로젝트는  

**ISBN 기반 자동 입력, 다차원 상세 검색, 하이브리드 개인화 추천 기능**을 통합하여  
데이터 품질과 사용자 경험을 동시에 개선하는 것을 목표로 합니다.

---

## 🎯 개발 목적

- 📖 도서 정보 자동 입력 → 입력 오류 및 누락 최소화  
- 🔍 다차원 검색 → 정확하고 일관된 검색 결과 제공  
- 🤖 개인화 추천 → 사용자 맞춤형 탐색 경험 제공  
- 🔗 등록 → 검색 → 추천까지 하나의 흐름으로 연결  

---

## 🚀 주요 기능

### 1️⃣ ISBN 기반 자동 입력

- 제목 키워드 입력 → Kakao Book API 호출  
- 도서 선택 시 제목 / 저자 / 출판사 / ISBN 자동 입력  
- API 실패 시 수동 입력 지원  
- 입력 시간 단축 및 데이터 정확도 향상  

<img width="807" height="530" alt="image" src="https://github.com/user-attachments/assets/0b083efb-3c00-4248-a53b-25f9964fef03" />

<img width="710" height="567" alt="image" src="https://github.com/user-attachments/assets/b00d57c2-f5f1-4ddf-a6f2-d79d1adfb85d" />

---

### 2️⃣ 다차원 상세 검색

- 키워드 / 학과 / 가격 / 상태 / 이미지 여부 / ISBN 검색  
- 여러 조건 동시 적용 가능  
- 모바일 ISBN 바코드 스캔 지원  
- 재현성 높은 검색 결과 제공  

<img width="1129" height="764" alt="image" src="https://github.com/user-attachments/assets/2f805603-51ea-49d4-a0de-3c6a26e34646" />

---

### 3️⃣ 하이브리드 개인화 추천

- 최근 조회 기반 추천  
- 저자 정보 + 사용자 행동 데이터 활용  
- 관심 도서 자동 노출  
- 검색 없이 탐색 가능  

<img width="915" height="723" alt="image" src="https://github.com/user-attachments/assets/71127359-da3d-446c-bec8-08e88d81c070" />

---

### 4️⃣ 회원 인증 및 보안

- 이메일 인증 코드 기반 회원가입  
- BCrypt 비밀번호 암호화  
- 로그인 인증 처리  

---

## 🛠 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Java 17, Spring Boot, Spring Security, Spring Data JPA, Lombok |
| Frontend | HTML5, CSS3, JavaScript |
| Database | MySQL |
| API | Kakao Book Search API |
| Server | Embedded Tomcat |

---

## 🗄 데이터베이스 구조 (ERD)

<img width="1037" height="798" alt="image" src="https://github.com/user-attachments/assets/aee4e5b2-f4f5-4aa2-b537-e35933d90120" />

### 핵심 테이블

- users : 사용자 정보  
- books : ISBN 기반 도서 메타데이터  
- book_listings : 판매글  
- wishlist : 찜  
- dm_threads : 채팅  
- dm_messages : 메시지  
- transactions : 거래  
- reviews : 평점  

---

## 🔄 시스템 구조

```
Controller → Service → Repository
```

- REST API 기반 구조  
- 계층 분리로 유지보수성 향상  
- 보안 및 데이터 처리 분리  

---

## 🌐 서비스 주소

👉 https://bookbridgemarket.com/

> 도메인 구매 및 배포 경험 포함

---

## ⚡ 실행 방법

### 1. 프로젝트 클론
```bash
git clone https://github.com/your-repo/bookbridge.git
```

### 2. DB 설정
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bookbridge
spring.datasource.username=root
spring.datasource.password=1234
```

### 3. 실행
```bash
./gradlew bootRun
```

### 4. 접속
```
http://localhost:8080
```

---

## 📈 기대 효과

- 데이터 정확도 향상 (ISBN 자동 입력)  
- 검색 효율 증가 (다차원 검색)  
- 사용자 경험 향상 (개인화 추천)  
- 탐색 시간 감소  

---

## 🧠 프로젝트 의의

BookBridge는 단순한 중고거래 플랫폼이 아니라  

👉 **데이터 기반 검색 + 추천 시스템이 결합된 지능형 거래 플랫폼**으로  
기존 서비스의 한계를 개선한 시스템입니다.

---

## 👨‍💻 개발자

- 이보람  
- 이민주  
- 김지훈  

---

## 📌 한줄 정리

> ISBN 기반 데이터 정확성과 다차원 검색, 사용자 행동 기반 추천을 결합한 전공서적 거래 플랫폼
