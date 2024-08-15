# 프로젝트 설정

## (참고) 개발자 개발 환경
- MacBook Pro 16" Apple M3 Pro 36GB SONOMA
- IntelliJ
- CheckStyle-IDEA Plugin (Google Check)
  - IDEA 종속된 플러그인을 사용합니다만, 타 IDEA 에서 PR 생성 후 요청주시면 코드 검토 후 승인하겠습니다.
  - 해당 플러그인 관련된 설정은 config/checkstyle.xml 파일로 설정합니다.
  - Setting > Code Editor > Code Style > Java > 설정 아이콘 > import Scheme > 파일선택 과정으로 적용할 수 있습니다.

## 사전 요구 사항
- Java Development Kit 22 이상
- Gradle (IntelliJ 를 사용하고 있다면 설치할 필요 없습니다)
- chromedriver (brew 로 설치)
  - ubmi 지수를 받아오기 위해 설치합니다.
  - 해당 패키지를 설치하기 부담스러우신 분들은 fetchUpbitMarketIndexRatio 메소드를 적절히 수정하시기 바랍니다.
- 업비트 원화 계좌에 300만원 정도 필요.
  - 개인 경험을 바탕으로 필요 금액을 고지하였습니다.
  - 폭락장이 이어질 경우 추가 금액이 필요할 수 있습니다.
  - 평균적으로 30%~50% 의 예수금을 매매에 이용하게 됩니다.
  - 주의: 투자는 개인 판단의 몫 입니다. 시드머니의 100% 를 이용하지 마세요. 

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
---

# 기여 가이드

## 소개

이 가이드는 오픈 소스 프로젝트에 기여하기 위한 분기 전략을 설명합니다. 이 전략을 따르면 깨끗하고 관리 가능한 프로젝트 구조를 유지하여 모든 기여자 간의 효율적인 협력을 보장할 수 있습니다.

## 브랜치 전략

이 프로젝트는 특정 플랫폼에서 실행되는 것이 아니라 로컬 호스트 환경을 기반으로 개발되기 때문에 릴리스 및 핫픽스 지점은 이 프로젝트의 지점 전략에서 제외됩니다. 이러한 지점이 필요하시면 의견을 나눌 수 있는 이슈를 제출해 주시기 바랍니다.

## 기여방법 

어떤 내용이 수정될지 간략하게 단어로 구분하여 브랜치 생성 후 main 브랜치를 target 으로 Pull Request 를 생성해주세요.
예를 들어 new-strategy, fix-scheduler, new-calculator 등등..

---

# 매수/매도 전략

## 매수 전략
UBMI 및 RSI 를 추종하여 UBMI 가 0% 이하이면서 RSI 가 30 이하인 종목.

혹은, UBMI 가 0% 초과이면서 RSI 가 35이하인 종목.

매수 진행시 업비트 최저 주문금액을 기준으로 분할 매수 합니다.

## 매도 전략
현재 수익률이 0.2~0.3%(익절) 인 종목.

현재수익률이 -2% 이하이면서 RSI 가 70 이상인(손절) 종목.

익절 진행시 일괄 매도, 손절매 진행시 분할 매도 합니다.

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

## UBMI (Upbit Marekt Index)
업비트가 시장의 전반적인 성과를 측정하기 위해 만든 지수입니다. UBMI는 다양한 암호화폐의 시가총액을 바탕으로 계산되며, 이를 통해 암호화폐 시장의 전반적인 동향을 파악할 수 있습니다.
