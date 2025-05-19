# RefrigeGO Backend

Spring Boot 기반 냉장GO 백엔드 프로젝트

## 🚀 프로젝트 개요

냉장고 관리 애플리케이션의 백엔드 API 서버입니다.
사용자가 냉장고에 있는 재료를 관리하고, 이를 기반으로 레시피를 추천받을 수 있는 기능을 제공합니다.

## 🛠️ 기술 스택

- **Java 17**
- **Spring Boot 3.4.5**
- **Spring Data JPA**
- **Spring Data MongoDB**
- **Spring Security**
- **MySQL**
- **Flyway (DB Migration)**
- **Lombok**

## 📁 프로젝트 구조

```
src/main/java/com/ohgiraffers/refrigegobackend/
├── common/                 # 공통 컴포넌트
├── config/                 # 설정 클래스
├── global/                 # 전역 설정
│   ├── config/            # 보안 설정
│   └── exception/         # 전역 예외 처리
├── ingredient/            # 재료 관리 도메인
│   ├── controller/
│   ├── service/
│   ├── domain/
│   ├── dto/
│   └── infrastructure/
├── recipe/                # 레시피 관리 도메인
│   ├── controller/
│   ├── service/
│   ├── domain/
│   ├── dto/
│   └── infrastructure/
├── recommendation/        # 레시피 추천 도메인 ⭐
│   ├── controller/
│   ├── service/
│   ├── domain/
│   ├── dto/
│   └── infrastructure/
├── notification/          # 알림 도메인 (예정)
└── user/                  # 사용자 관리 도메인 (예정)
```

## 🔥 주요 기능

### 1. 재료 관리 (Ingredient)
- ✅ 기준 재료 목록 조회
- ✅ 사용자 냉장고 재료 관리

### 2. 레시피 관리 (Recipe)
- ✅ 외부 API(식품안전처)에서 레시피 데이터 수집
- ✅ 레시피 데이터베이스 저장

### 3. 레시피 추천 (Recommendation) ⭐
- ✅ 직접 선택한 재료 기반 레시피 추천
- ✅ 매칭 점수에 따른 우선순위 정렬
- ✅ 레시피 찜하기/찜하기 취소
- ✅ 찜한 레시피 목록 조회
- ✅ 레시피 상세 정보 조회

## 📍 API 엔드포인트

### 재료 관리
```
GET    /ingredients              # 전체 기준 재료 조회
```

### 레시피 관리
```
GET    /api/recipes/fetch        # 외부 API에서 레시피 조회
GET    /api/recipes/save         # 레시피 DB 저장
GET    /api/recipes/saveAll      # 전체 레시피 배치 저장
```

### 레시피 추천 ⭐
```
POST   /api/recommendations/recipes              # 재료 기반 레시피 추천
POST   /api/recommendations/favorites/toggle     # 레시피 찜하기/취소
GET    /api/recommendations/favorites/{userId}   # 찜한 레시피 조회
GET    /api/recommendations/recipes/{recipeId}   # 레시피 상세 조회
```

## 🎯 레시피 추천 시스템

### 추천 알고리즘
1. **재료 매칭**: 사용자가 선택한 재료와 레시피 재료 비교
2. **매칭 점수 계산**: (매칭된 재료 수 / 선택한 총 재료 수)
3. **우선순위 정렬**: 
   - 1차: 매칭 점수 순 (내림차순)
   - 2차: 매칭된 재료 개수 순 (내림차순)

### 사용 시나리오
```
1. 사용자가 냉장고에서 재료 선택 (2~3개 이상)
   ↓
2. 선택한 재료 기반으로 레시피 검색
   ↓
3. 매칭 점수에 따라 레시피 목록 정렬
   ↓
4. 추천된 레시피 중 하나 선택하여 상세 확인
   ↓
5. 마음에 드는 레시피 찜하기
```

## 🗄️ 데이터베이스 테이블

### 주요 테이블
- `ingredients`: 기준 재료 마스터 데이터
- `user_ingredients`: 사용자 냉장고 재료
- `recipes`: 레시피 정보
- `recipe_favorites`: 사용자 레시피 찜하기 ⭐

## 🚦 현재 개발 상태

✅ **완료된 기능**
- 재료 관리 (기준 재료 조회)
- 레시피 수집 및 저장
- 레시피 추천 시스템
- 레시피 찜하기 기능

⏳ **개발 예정**
- 사용자 관리 시스템
- 사용자 재료 CRUD API
- 알림 기능
- 자동 레시피 추천 (사용자 보유 재료 기반)

## 🛠️ 로컬 개발 환경 설정

### 필수 요구사항
- Java 17 이상
- MySQL 8.0 이상
- Gradle

### 설정 파일 (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/refrige_go?serverTimezone=Asia/Seoul
    username: your_username
    password: your_password
    
api:
  foodsafety:
    key: your_api_key
```

### 실행 방법
```bash
# 1. 데이터베이스 생성
mysql -u root -p
CREATE DATABASE refrige_go;

# 2. 애플리케이션 실행
./gradlew bootRun
```

## 📝 개발 노트

### 레시피 추천 구현 특징
- **유연한 재료 매칭**: 공백 제거, 대소문자 무시하여 매칭 정확도 향상
- **점수 기반 정렬**: 단순 개수가 아닌 비율을 고려한 스마트 추천
- **찜하기 토글**: 중복 방지를 위한 UNIQUE 제약 조건 활용
- **전역 예외 처리**: 일관된 에러 응답을 위한 GlobalExceptionHandler 구현

### 향후 개선 계획
- 레시피 카테고리별 가중치 적용
- 사용자 선호도 학습 기능
- 영양 정보 기반 추천
- 계절별/상황별 맞춤 추천

