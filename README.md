# 프로젝트 설정

## (참고) 개발자 개발 환경
- MacBook Pro 16" Apple M3 Pro 36GB SONOMA
- IntelliJ, Docker Desktop, DataGrip
- CheckStyle-IDEA Plugin (Google Check)
  - IDEA 종속된 플러그인을 사용합니다만, 타 IDEA 에서 PR 생성 후 요청주시면 코드 검토 후 승인하겠습니다.
  - 해당 플러그인 관련된 설정은 config/checkstyle.xml 파일로 설정합니다.
  - Setting > Code Editor > Code Style > Java > 설정 아이콘 > import Scheme > 파일선택 과정으로 적용할 수 있습니다.

## 사전 요구 사항
- Java Development Kit 22 이상
- Gradle (IntelliJ 를 사용하고 있다면 설치할 필요 없습니다)
- Docker Desktop

## 설정 지침

### 1. 리포지토리 클론
```sh
git clone https://github.com/ChoSeyoung/upbit_coin_trader.git

cd upbit_coin_trader
```

### 2. application.properties.example을 application.properties로 복사

```sh
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

### 3. API 키 구성 및 필수값 설정
```sh
# src/main/resources/application.properties

# 업비트 API KEY 를 확인하세요
upbit.api.key=${your_api_key_here}
# 업비트 SECRET KEY 를 확인하세요
upbit.secret.key=${your_secret_key_here}
```

### 4. Docker 를 이용한 local mysql 실행
```sh
cd docker/mysql
# 필요시 docker-compose.yml 파일을 변경할 수 있습니다. (포트 및 기타설정 등등)
# 해당 컨테이너의 데이터 디렉토리는 ${ProjectRootDirectory}/docker/mysql/data 에 쌓이므로 mysql 을 기본적으로 사용하고 있는 PC 에서도 데이터 이슈 없이 사용할 수 있습니다.
docker-compose up
```

### 5. seed.sql 를 이용한 데이터 시딩
```sql
use upbit;

...
```
---

# 기여 가이드

## 소개

이 가이드는 오픈 소스 프로젝트에 기여하기 위한 분기 전략을 설명합니다. 이 전략을 따르면 깨끗하고 관리 가능한 프로젝트 구조를 유지하여 모든 기여자 간의 효율적인 협력을 보장할 수 있습니다.

## 브랜치 전략

이 프로젝트는 특정 플랫폼에서 실행되는 것이 아니라 로컬 호스트 환경을 기반으로 개발되기 때문에 릴리스 및 핫픽스 지점은 이 프로젝트의 지점 전략에서 제외됩니다. 이러한 지점이 필요하시면 의견을 나눌 수 있는 이슈를 제출해 주시기 바랍니다.

### 1. 메인 브랜치

#### 1.1 `main`

`main` 브랜치에는 안정적으로 출시된 코드가 포함되어 있습니다. 모든 새로운 기능과 수정은 철저한 테스트를 거친 후 PR을 통해 `main` 으로 병합해야 합니다.

#### 1.2 `development`

`development` 브랜치는 특징에 대한 통합 지점으로서 역할을 합니다. 출시되기 전에 모든 개발 작업이 병합됩니다. 다음 릴리스에 대한 최신 개발 변경 사항이 포함되어 있습니다.

### 2. 지원 브랜치

#### 2.1 기능 브랜치
`feature` 브랜치는 향후 릴리스 또는 먼 미래 릴리스에 대한 새로운 기능을 개발하기 위해 사용됩니다.

EX: `feature/feature-name`

#### 2.2 버그 수정 브랜치
`bugfix` 브랜치는 패치 제품 릴리스로 빠르게 패치 제품 릴리스에 사용됩니다.

EX: `bugfix/bugfix-name`

---

# 프로젝트 이해를 위한 용어 가이드

## RSI (Relative Strength Index)

RSI(상대강도지수)는 기술적 분석 지표로, 주식이나 다른 자산의 가격 변동 속도를 측정합니다. 0에서 100까지의 값으로 표현되며, 일반적으로 70 이상일 때는 과매수 상태를, 30 이하일 때는 과매도 상태를 나타냅니다. RSI는 주로 단기적인 가격 움직임을 분석하는 데 사용되며, 과매수나 과매도 상태를 식별하여 매매 시점을 결정하는 데 도움을 줍니다.

### RSI 계산 절차 및 관련 용어 정리
U (Up): 가격이 전일 가격보다 상승한 날의 상승분을 의미합니다.
D (Down): 가격이 전일 가격보다 하락한 날의 하락분을 의미합니다.
AU (Average Ups): 일정 기간 동안의 U 값의 평균을 의미합니다.
AD (Average Downs): 일정 기간 동안의 D 값의 평균을 의미합니다.
RS (Relative Strength): AU를 AD 값으로 나눈 것을 의미합니다. RS 값이 크다는 것은 일정 기간 동안 하락한 폭보다 상승한 폭이 크다는 것을 의미합니다.
RSI (Relative Strength Index): 다음 공식을 사용하여 RSI 값을 구합니다.

## EMA (Exponential Moving Average)

EMA(지수이동평균)는 특정 기간 동안의 자산 가격의 평균을 계산하되, 최신 데이터에 더 많은 가중치를 부여하는 기술적 분석 지표입니다. EMA는 이동평균의 한 형태로, 주가의 변동성을 더 민감하게 반영합니다. 이는 트렌드의 방향을 파악하고, 지지 및 저항 수준을 찾는 데 도움을 줍니다. EMA는 단기, 중기, 장기적으로 적용될 수 있으며, 일반적으로 12일, 26일, 50일, 200일 EMA가 많이 사용됩니다.

