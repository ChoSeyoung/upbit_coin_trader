# 프로젝트 설정 / Project Setup

## (참고) 개발자 개발 환경
- MacBook Pro 16" Apple M3 Pro 36GB SONOMA
- IntelliJ, Docker Desktop, DataGrip
- CheckStyle-IDEA Plugin (Google Check)
  - IDEA 종속된 플러그인을 사용합니다만, 타 IDEA 에서 PR 생성 후 요청주시면 코드 검토 후 승인하겠습니다.
  - 해당 플러그인 관련된 설정은 config/checkstyle.xml 파일로 설정합니다.
  - Setting > Code Editor > Code Style > Java > 설정 아이콘 > import Scheme > 파일선택 과정으로 적용할 수 있습니다.

## 사전 요구 사항 / Prerequisites
- Java Development Kit 22 이상
- Gradle (IntelliJ 를 사용하고 있다면 설치할 필요 없습니다)
- Docker Desktop

## 설정 지침 / Setup Instructions

### 1. 리포지토리 클론 / Clone the repository
```sh
git clone https://github.com/ChoSeyoung/upbit_coin_trader.git

cd upbit_coin_trader
```

### 2. application.properties.example을 application.properties로 복사 / Copy application.properties.example to application.properties

```sh
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

### 3. API 키 구성 및 필수값 설정 / Configure your API keys and environment keys
```sh
# src/main/resources/application.properties

# 업비트 API KEY 를 확인하세요
upbit.api.key=${your_api_key_here}
# 업비트 SECRET KEY 를 확인하세요
upbit.secret.key=${your_secret_key_here}
# 업비트 매수/매도 수수료율을 항상 확인하세요
upbit.ratio.exchange=${platform_exchange_fee}
# 업비트에서 본인이 거래하고싶은 티커심볼을 확인하고 설정합니다.
upbit.ticker.symbol=${ticker_symbol}
```

### 4. Docker 를 이용한 local mysql 실행 / Run localhost MySQL by Docker
```sh
cd docker/mysql
# 필요시 docker-compose.yml 파일을 변경할 수 있습니다. (포트 및 기타설정 등등)
# 해당 컨테이너의 데이터 디렉토리는 ${ProjectRootDirectory}/docker/mysql/data 에 쌓이므로 mysql 을 기본적으로 사용하고 있는 PC 에서도 데이터 이슈 없이 사용할 수 있습니다.
docker-compose up
```

---

# 기여 가이드 / Contribution Guide

## 소개 / Introduction

This guide outlines the branch strategy for contributing to our open-source project. Following this strategy helps maintain a clean and manageable project structure, ensuring efficient collaboration among all contributors.

## 브랜치 전략 / Branch Strategy

Since this project is developed based on a localhost environment rather than running on a specific platform, the release and hotfix branches are excluded from this project's branch strategy. If you need these branches, please submit an issue to share your opinion.

### 1. 메인 브랜치 / Main Branches

#### 1.1 `main`

The `main` branch contains the stable, released code. All new features and fixes must be merged into `main` through pull requests after they have been thoroughly tested.

#### 1.2 `development`

The `development` branch serves as the integration branch for features. This is where all developmentment work is merged before being released. It contains the latest delivered developmentment changes for the next release.

### 2. 지원 브랜치 / Supporting Branches

#### 2.1 기능 브랜치 / Feature Branches
Feature branches are used to development new features for the upcoming or a distant future release.

- **Naming convention:** `feature/feature-name`
- **Merging into:** `development`

##### 단계 / Steps:
1. Create a new feature branch from `development`:
    ```sh
    git checkout development
    git pull origin development
    git checkout -b feature/feature-name
    ```
2. development the feature.
3. Commit your changes.
4. Push the feature branch to the repository:
    ```sh
    git push origin feature/feature-name
    ```
5. Create a pull request to merge your changes into `development`.

#### 2.2 버그 수정 브랜치 / Bugfix Branches
Bugfix branches are used to quickly patch production releases.

- **Naming convention:** `bugfix/bugfix-name`
- **Merging into:** `development`

##### 단계 / Steps:
1. Create a new bugfix branch from `development`:
    ```sh
    git checkout development
    git pull origin development
    git checkout -b bugfix/bugfix-name
    ```
2. Fix the bug.
3. Commit your changes.
4. Push the bugfix branch to the repository:
    ```sh
    git push origin bugfix/bugfix-name
    ```
5. Create a pull request to merge your changes into `development`.

---

# 암호화폐 거래시장 용어설명 및 가이드

## 소개 / Introduction

This guide provides an overview of key financial terms that are essential for understanding the cryptocurrency market and investing. Whether you're
a beginner or an experienced investor, these terms will help you navigate the world of digital assets more effectively.

## Key Financial Terms / 주요 금융 용어

### 1. Cryptocurrency Market / 암호화폐 시장

**암호화폐 시장**은 암호화폐가 거래되는 디지털 또는 가상 시장입니다. 전통적인 주식 시장과 달리 암호화폐 시장은 24시간 운영되며, 정부나 금융 기관과 같은 단일 기관에 의해 통제되지 않습니다. 인기 있는 암호화폐로는 비트코인(BTC), 이더리움(ETH) 등이 있습니다.

The **cryptocurrency market** is a digital or virtual marketplace where cryptocurrencies are traded. Unlike traditional stock markets, the cryptocurrency market operates 24/7 and is decentralized, meaning it is not controlled by any single entity like a government or financial institution. Popular cryptocurrencies include Bitcoin (BTC), Ethereum (ETH), and many others.

### 2. Risks of the Cryptocurrency Market / 암호화폐 시장의 위험성

암호화폐 시장에 투자하는 것은 높은 변동성, 규제의 부재, 사기 가능성으로 인해 상당한 위험을 수반합니다. 암호화폐 가격은 짧은 기간 내에 급격히 변동할 수 있으며, 이는 상당한 재정적 손실로 이어질 수 있습니다. 또한, 규제 감독의 부재는 사기 계획 및 보안 침해의 위험을 증가시킵니다. 투자자는 암호화폐에 투자하기 전에 철저히 조사하고 자신의 위험 허용 범위를 고려해야 합니다.

Investing in the cryptocurrency market carries significant risks due to its high volatility, lack of regulation, and potential for fraud. Prices of cryptocurrencies can fluctuate wildly in a short period, leading to substantial financial losses. Additionally, the absence of regulatory oversight increases the risk of fraudulent schemes and security breaches. Investors should thoroughly research and consider their risk tolerance before investing in cryptocurrencies.

### 3. Altcoin / 알트코인

**알트코인**은 비트코인을 제외한 모든 암호화폐를 의미합니다. 이 용어는 "대안"과 "코인"의 결합으로, 다양한 기능과 목적을 가진 수천 개의 암호화폐를 포함합니다. 예로는 이더리움(ETH), 리플(XRP), 라이트코인(LTC) 등이 있습니다.

An **altcoin** is any cryptocurrency other than Bitcoin. The term is a combination of "alternative" and "coin" and includes thousands of cryptocurrencies with varying functions and purposes. Examples include Ethereum (ETH), Ripple (XRP), and Litecoin (LTC).

### 4. Stablecoin / 스테이블코인

**스테이블코인**은 미국 달러와 같은 법정 화폐 또는 금과 같은 상품에 연동된 암호화폐의 일종입니다. 이는 연동되지 않은 암호화폐에 비해 변동성을 줄이기 위한 것입니다. 예로는 테더(USDT)와 USD 코인(USDC)이 있습니다.

A **stablecoin** is a type of cryptocurrency that is pegged to a stable asset, such as a fiat currency like the US dollar or a commodity like gold. This is intended to reduce volatility compared to unpegged cryptocurrencies. Examples include Tether (USDT) and USD Coin (USDC).

### 5. ICO (Initial Coin Offering) / ICO (초기 코인 공개)

**초기 코인 공개(ICO)**는 새로운 프로젝트가 자본을 모으기 위해 기본 암호화폐 토큰을 판매하는 자금 조달 메커니즘입니다. 이는 주식 시장의 초기 공모(IPO)와 유사하지만, 암호화폐 공간에서 발생합니다.

An **Initial Coin Offering (ICO)** is a fundraising mechanism in which new projects sell their underlying crypto tokens in exchange for capital. It is similar to an Initial Public Offering (IPO) in the stock market but occurs in the cryptocurrency space.

### 6. Exchange / 거래소

**암호화폐 거래소**는 사용자가 암호화폐를 사고팔고 거래할 수 있는 플랫폼입니다. 거래소는 회사가 거래를 통제하고 자산을 보유하는 중앙 집중식일 수도 있고, 중앙 권한 없이 사용자 간 직접 거래가 이루어지는 분산형일 수도 있습니다.

A **cryptocurrency exchange** is a platform where users can buy, sell, and trade cryptocurrencies. Exchanges can be centralized, where a company controls the transactions and holds the assets, or decentralized, where transactions occur directly between users without a central authority.

### 7. Wallet / 지갑

**암호화폐 지갑**은 사용자가 암호화폐를 저장, 송금, 수신할 수 있는 디지털 도구입니다. 지갑은 하드웨어 기반(물리적 장치)일 수도 있고, 소프트웨어 기반(컴퓨터나 스마트폰의 애플리케이션)일 수도 있습니다.

A **cryptocurrency wallet** is a digital tool that allows users to store, send, and receive cryptocurrencies. Wallets can be hardware-based (physical devices) or software-based (applications on a computer or smartphone).

### 8. Private Key / 개인 키

**개인 키**는 사용자만 알고 있는 보안 디지털 코드로, 암호화폐 거래를 승인하는 데 사용됩니다. 개인 키는 비밀로 유지해야 하며, 접근 권한이 있는 사람은 관련 암호화폐를 제어할 수 있습니다.

A **private key** is a secure digital code known only to the user and used to authorize cryptocurrency transactions. It must be kept secret as anyone with access to it can control the associated cryptocurrencies.

### 9. Public Key / 공용 키

**공용 키**는 다른 사람이 사용자의 지갑으로 암호화폐를 보낼 수 있게 하는 디지털 코드입니다. 개인 키에서 파생되었지만 거래를 승인하는 데 사용할 수는 없습니다.

A **public key** is a digital code that allows others to send cryptocurrency to a user's wallet. It is derived from the private key but cannot be used to authorize transactions.

### 10. Blockchain / 블록체인

**블록체인**은 암호화폐의 기반이 되는 분산 원장 기술입니다. 각 블록에는 거래 데이터가 포함되어 있으며, 이 블록들은 컴퓨터(노드) 네트워크에 의해 안전하게 연결되고 검증됩니다.

**Blockchain** is a distributed ledger technology that underlies cryptocurrencies. It consists of a chain of blocks, each containing transaction data, which is securely linked and verified by a network of computers (nodes).

## Conclusion / 결론

이 주요 금융 용어를 이해하는 것은 암호화폐 시장에 투자하거나 참여하려는 모든 사람에게 필수적입니다. 이 가이드는 정보에 입각한 결정을 내리는 데 도움이 되는 기초적인 이해를 제공하는 것을 목표로 합니다.

Understanding these key financial terms is essential for anyone looking to invest or participate in the cryptocurrency market. This guide aims to provide a foundational understanding to help you make informed decisions.

