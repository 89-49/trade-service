# trade-service

거래 생명주기를 담당하는 Spring Boot 기반 마이크로서비스입니다. 예약 완료 이벤트를 수신해 거래를 생성하고, 거래 조회와 구매자/판매자 양측 완료 처리를 제공합니다.

## 주요 기능

- 예약 완료 Kafka 이벤트 수신 후 거래 생성
- 거래 목록/단건 조회 REST API 제공
- 구매자/판매자 거래 완료 처리
- 양측 완료 시 거래 완료 이벤트 Outbox 등록
- 거래 생성/완료/취소 이벤트 발행 포트 제공
- Promtail 호환 설정을 통한 로그 수집

## 기술 스택

- Java 21
- Spring Boot 3.5.14
- Spring Cloud 2025.0.2
- Spring Data JPA
- Spring Kafka
- Eureka Client
- PostgreSQL, H2 Test DB
- Gradle
- Docker, Docker Compose
- Grafana Alloy(Promtail config format)

## 프로젝트 구조

```text
src/main/java/org/pgsg/trade
├── application      # 유스케이스, 서비스, 포트, 커맨드/결과 DTO
├── domain           # 거래 도메인 모델과 도메인 예외
├── global           # 공통 설정과 예외 처리
├── infrastructure   # JPA, Kafka 어댑터
└── presentation     # REST 컨트롤러와 API DTO
```

## 사전 준비

- JDK 21
- Docker, Docker Compose
- GitHub Package Registry 접근 권한
- 실행 환경에 맞는 Config Server, Eureka, Kafka, DB 설정

`org.pgsg:common` 패키지를 GitHub Package Registry에서 내려받기 때문에 Gradle 인증 정보가 필요합니다.

```properties
# ~/.gradle/gradle.properties
gpr.user=GITHUB_USERNAME
gpr.token=GITHUB_TOKEN
```

또는 환경변수로 지정할 수 있습니다.

```bash
export GPR_USER=GITHUB_USERNAME
export GPR_TOKEN=GITHUB_TOKEN
```

## 설정

애플리케이션은 기본적으로 `.env`, Config Server, Eureka를 사용합니다.

```yaml
spring:
  application:
    name: trade-service
  profiles:
    active: kafka, topics, trade-error
  config:
    import:
      - "optional:file:.env[.properties]"
      - "optional:configserver:"
```

로컬 또는 배포 환경에서는 다음 값이 필요합니다.

| 변수 | 설명 |
| --- | --- |
| `DB_USERNAME` | 데이터베이스 사용자명 |
| `DB_PASSWORD` | 데이터베이스 비밀번호 |
| `DB_DATABASE` | 데이터베이스 이름 |
| `EUREKA_SERVER_URL` | Eureka 서버 URL 목록 |
| `TRUST_STORE_PASSWORD` | Kafka SSL truststore 비밀번호 |
| `SPRING_KAFKA_SSL_TRUST_STORE_LOCATION` | Kafka SSL truststore 경로 |
| `GPR_USER` | GitHub Package Registry 사용자명 |
| `GPR_TOKEN` | GitHub Package Registry 토큰 |

Kafka topic 설정은 Config Server 또는 활성 프로필 설정에서 제공되어야 합니다.

| 설정 키 | 설명 |
| --- | --- |
| `topics.reservation.completed` | 예약 완료 이벤트 수신 topic |
| `topics.trade.created` | 거래 생성 이벤트 발행 topic |
| `topics.trade.completed` | 거래 완료 이벤트 발행 topic |
| `topics.trade.cancelled` | 거래 취소 이벤트 발행 topic |

## 로컬 실행

```bash
./gradlew bootRun
```

테스트 프로필은 Config Server와 Eureka를 비활성화하고 H2 인메모리 DB를 사용합니다.

```bash
./gradlew test
```

## Docker 실행

로컬 Docker Compose는 `trade-service`와 로그 수집용 Alloy 컨테이너를 함께 실행합니다.

```bash
docker compose up --build
```

기본 포트 매핑은 다음과 같습니다.

| 대상 | 포트 |
| --- | --- |
| Host | `19020` |
| Container | `8083` |

Compose 실행 전 외부 네트워크가 필요합니다.

```bash
docker network create pgsg-network
```

Docker 빌드 시 `~/.gradle/gradle.properties`가 BuildKit secret으로 전달됩니다.

## API

### 거래 목록 조회

```http
GET /api/v1/trades?page=0&size=20
```

- `page`: 0 이상의 페이지 번호
- `size`: 1 이상 100 이하의 페이지 크기

### 거래 단건 조회

```http
GET /api/v1/trades/{tradeId}
```

### 거래 완료 처리

```http
PATCH /api/v1/trades/{tradeId}/complete
```

현재 인증 사용자 ID로 구매자 또는 판매자의 완료 상태를 저장합니다. 구매자와 판매자가 모두 완료 처리하면 거래 상태가 `COMPLETED`가 되고 거래 완료 이벤트 발행이 요청됩니다.

## 이벤트 흐름

### 수신 이벤트

`topics.reservation.completed` topic의 예약 완료 이벤트를 수신해 거래를 생성합니다.

```json
{
  "reservationId": "00000000-0000-0000-0000-000000000000",
  "productId": "00000000-0000-0000-0000-000000000000",
  "productName": "상품명",
  "productPrice": 10000,
  "sellerId": "00000000-0000-0000-0000-000000000000",
  "sellerNickName": "판매자",
  "buyerId": "00000000-0000-0000-0000-000000000000",
  "buyerNickName": "구매자"
}
```

### 발행 이벤트

서비스는 common 모듈의 Outbox 이벤트를 통해 다음 이벤트 등록을 요청합니다.

- 거래 생성: `topics.trade.created`
- 거래 완료: `topics.trade.completed`
- 거래 취소: `topics.trade.cancelled`

## 로그 수집

`promtail-config.yml`과 `deploy/promtail-config.yml`은 `/logs/*.log` 파일을 수집해 Loki 엔드포인트로 전송합니다. 로컬 Compose와 운영 Compose 모두 Grafana Alloy를 Promtail config format으로 실행합니다.

## 배포

운영 Compose 파일은 `deploy/docker-compose.prod.yaml`에 있습니다.

```bash
docker compose -f deploy/docker-compose.prod.yaml up -d
```

운영 환경에서는 다음 경로를 사용합니다.

| 경로 | 용도 |
| --- | --- |
| `/opt/trade-service/ssl` | Kafka SSL 인증서 |
| `/opt/trade-service/logs` | 애플리케이션 로그 |
| `/opt/trade-service/promtail-config.yml` | 로그 수집 설정 |

## 테스트

```bash
./gradlew test
```

테스트는 도메인 모델, 애플리케이션 서비스, 영속성 어댑터, Kafka Consumer, 컨트롤러, 아키텍처 규칙을 검증합니다.
