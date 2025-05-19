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
- **Spring Cloud Config**
- **MySQL 8.0**
- **Flyway (DB Migration)**
- **Lombok**
- **RestTemplate (외부 API 연동)**

## 📁 프로젝트 구조

```
src/main/java/com/ohgiraffers/refrigegobackend/
├── RefrigeGoBackendApplication.java    # 메인 애플리케이션 클래스
├── common/                             # 공통 컴포넌트
│   ├── dto/                           # 공통 DTO
│   ├── constant/                      # 상수 클래스
│   └── util/                          # 유틸리티 클래스
├── config/                            # 애플리케이션 설정
│   ├── AppConfig.java                 # RestTemplate 설정
│   └── CorsConfig.java                # CORS 설정
├── global/                            # 전역 설정
│   ├── config/SecurityConfig.java     # Spring Security 설정
│   ├── exception/GlobalExceptionHandler.java  # 전역 예외 처리
│   ├── filter/                        # 필터 (예정)
│   └── interceptor/                   # 인터셉터 (예정)
├── ingredient/                        # 재료 관리 도메인 ✅
│   ├── controller/
│   │   ├── IngredientController.java  # 기준 재료 API
│   │   └── UserIngredientController.java  # 사용자 재료 API
│   ├── service/
│   │   ├── IngredientService.java     # 기준 재료 서비스
│   │   └── UserIngredientService.java # 사용자 재료 서비스
│   ├── domain/
│   │   ├── Ingredient.java            # 기준 재료 엔티티
│   │   └── UserIngredient.java        # 사용자 재료 엔티티
│   ├── dto/
│   │   ├── IngredientResponseDto.java
│   │   ├── UserIngredientRequestDto.java
│   │   └── UserIngredientResponseDto.java
│   └── infrastructure/repository/
│       ├── IngredientRepository.java  # 기준 재료 Repository
│       └── UserIngredientRepository.java  # 사용자 재료 Repository
├── recipe/                            # 레시피 관리 도메인 ✅
│   ├── controller/RecipeApiController.java  # 레시피 API
│   ├── service/RecipeApiService.java       # 레시피 API 서비스
│   ├── domain/Recipe.java                  # 레시피 엔티티
│   ├── dto/RecipeApiResponseDto.java       # 외부 API 응답 DTO
│   └── infrastructure/repository/RecipeRepository.java
├── recommendation/                    # 레시피 추천 도메인 ✅ ⭐
│   ├── controller/RecipeRecommendationController.java
│   ├── service/RecipeRecommendationService.java
│   ├── dto/
│   │   ├── RecipeRecommendationRequestDto.java
│   │   ├── RecipeRecommendationResponseDto.java
│   │   └── RecommendedRecipeDto.java
│   └── infrastructure/repository/
├── notification/                      # 알림 도메인 (예정)
└── user/                             # 사용자 관리 도메인 (예정)
```

## 🔥 구현된 주요 기능

### 1. 재료 관리 (Ingredient) ✅
- **기준 재료 관리**: 시스템 전체에서 사용하는 공통 재료 마스터 데이터
- **사용자 재료 관리**: 개별 사용자가 냉장고에 보관 중인 재료 정보
- **재료 카테고리**: 채소, 육류, 유제품, 곡류, 양념 등으로 분류
- **초기 데이터**: 25개 기본 재료 프리셋 제공

### 2. 레시피 관리 (Recipe) ✅
- **외부 API 연동**: 식품안전처 공공데이터 API 연동
- **레시피 수집**: 외부 API에서 레시피 데이터 자동 수집
- **배치 처리**: 대량 레시피 데이터 효율적 저장 (100개씩 처리)
- **레시피 저장**: JSON → 엔티티 변환 후 데이터베이스 저장

### 3. 레시피 추천 (Recommendation) ✅ ⭐
- **재료 기반 추천**: 사용자가 선택한 재료를 기반으로 레시피 추천
- **스마트 매칭**: 대소문자 무시, 공백 제거한 정확한 재료 매칭
- **매칭 점수 시스템**: (매칭된 재료 수 / 선택한 총 재료 수) 기반 점수 계산
- **우선순위 정렬**: 매칭 점수 → 매칭된 재료 개수 순 정렬
- **레시피 상세 조회**: 레시피 상세 정보 제공
  
**참고**: 찜하기 기능은 별도 모듈(recipe_bookmarks)에서 구현됩니다.

### 4. 전역 설정 및 보안 ✅
- **Spring Security**: MVP 테스트용 모든 요청 허용 설정
- **CORS 설정**: 프론트엔드(localhost:3000) 연동 준비
- **전역 예외 처리**: 일관된 에러 응답을 위한 GlobalExceptionHandler
- **데이터베이스 마이그레이션**: Flyway를 통한 스키마 버전 관리

## 📍 API 엔드포인트

### 재료 관리 API ✅
```http
GET    /ingredients              # 전체 기준 재료 조회
```

### 레시피 관리 API ✅
```http
GET    /api/recipes/fetch        # 외부 API에서 레시피 조회 (JSON)
GET    /api/recipes/save?start=1&end=10    # 레시피 DB 저장 (범위 지정)
GET    /api/recipes/saveAll      # 전체 레시피 배치 저장 (100개씩)
```

### 레시피 추천 API ✅ ⭐
```http
POST   /api/recommendations/recipes              # 재료 기반 레시피 추천
GET    /api/recommendations/recipes/{recipeId}   # 레시피 상세 조회
```

## 🎯 레시피 추천 시스템 상세

### 추천 알고리즘
1. **재료 입력 검증**: 최소 2개 이상의 재료 선택 필수
2. **재료 매칭**: 선택한 재료와 레시피 재료 문자열 비교
3. **매칭 점수 계산**: 매칭된 재료 수 / 선택한 총 재료 수
4. **결과 정렬**: 
   - 1차: 매칭 점수 순 (내림차순)
   - 2차: 매칭된 재료 개수 순 (내림차순)
5. **결과 제한**: 최대 N개 레시피 반환 (기본값: 10개)

### 사용 시나리오
```
1. 사용자가 냉장고에서 재료 선택 (2~3개 이상)
   ↓
2. 선택한 재료 기반으로 레시피 검색 및 매칭 점수 계산
   ↓
3. 매칭 점수에 따라 레시피 목록 정렬 후 반환
   ↓
4. 추천된 레시피 중 하나 선택하여 상세 정보 확인
   ↓
5. 레시피 북마크 기능은 별도 모듈에서 제공 📖
```

## 🗄️ 데이터베이스 설계

### 주요 테이블
```sql
-- 기준 재료 마스터 테이블
ingredients (id, name, category)

-- 사용자 냉장고 재료 테이블  
user_ingredients (id, user_id, ingredient_id, custom_name, purchase_date, expiry_date, is_frozen)

-- 레시피 정보 테이블
recipes (rcp_seq, rcp_nm, rcp_parts_dtls, manual01, manual02)
```

### Flyway 마이그레이션 스크립트
- `V2__modify_user_ingredients_null.sql`: 사용자 재료 테이블 컬럼 타입 수정

## 🚦 현재 개발 상태

✅ **완료된 기능**
- ✅ 기준 재료 관리 시스템
- ✅ 사용자 재료 도메인 설계
- ✅ 외부 API 레시피 수집 및 저장
- ✅ 재료 기반 레시피 추천 시스템
- ✅ 전역 예외 처리 및 보안 설정

⏳ **개발 예정**
- 사용자 관리 시스템 (회원가입, 로그인)
- 사용자 재료 CRUD API 구현
- 알림 기능 (소비기한 임박 등)
- 자동 레시피 추천 (사용자 보유 재료 기반)
- 레시피 북마크 시스템 (별도 모듈)

## 🛠️ 로컬 개발 환경 설정

### 필수 요구사항
- Java 17 이상
- MySQL 8.0 이상
- Gradle 7.0 이상

### 환경 설정 (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/refrige_go?serverTimezone=Asia/Seoul
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

api:
  foodsafety:
    key: "your_api_key_here"  # 식품안전처 API 키

server:
  port: 8080
```

### 실행 방법
```bash
# 1. 데이터베이스 생성
mysql -u root -p
CREATE DATABASE refrige_go CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 2. 초기 데이터 로드 (선택사항)
mysql -u username -p refrige_go < src/main/resources/data/ingredients.sql

# 3. 애플리케이션 실행
./gradlew bootRun

# 4. 레시피 데이터 수집 (선택사항)
curl "http://localhost:8080/api/recipes/saveAll"
```

## 📚 API 사용 예시

### 1. 기준 재료 조회
```bash
curl -X GET http://localhost:8080/ingredients
```

### 2. 레시피 추천
```bash
curl -X POST http://localhost:8080/api/recommendations/recipes \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "selectedIngredients": ["브로콜리", "양파", "계란"],
    "limit": 5
  }'
```

### 3. 레시피 상세 조회
```bash
curl -X GET http://localhost:8080/api/recommendations/recipes/RCP_001
```

## 📝 개발 참고사항

### 코드 품질
- **Clean Architecture**: 도메인별 모듈화 및 계층 분리
- **DDD 패턴**: 도메인 중심 설계
- **Lombok**: 보일러플레이트 코드 최소화
- **JPA/Hibernate**: 객체-관계 매핑 및 자동 DDL

### 성능 최적화
- **배치 처리**: 대량 데이터 처리를 위한 배치 저장
- **인덱싱**: 검색 성능 향상을 위한 데이터베이스 인덱스
- **페이징**: 대용량 결과 처리를 위한 제한 기능

### 보안 고려사항
- **입력 검증**: DTO를 통한 데이터 유효성 검증
- **SQL 인젝션 방지**: JPA 사용으로 자동 방지
- **CORS 설정**: 프론트엔드와의 안전한 통신

### 향후 개선 계획
- **캐싱**: Redis를 활용한 추천 결과 캐싱
- **검색 엔진**: Elasticsearch 도입으로 고급 검색 기능
- **마이크로서비스**: 도메인별 서비스 분리
- **모니터링**: 애플리케이션 성능 모니터링 도구 도입

---

**⚡ refrige-go-backend v0.1.0 - 냉장GO와 함께 스마트한 냉장고 관리를 시작하세요!**
