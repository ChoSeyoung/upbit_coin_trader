use upbit;

CREATE TABLE closed_orders
(
    uuid             VARCHAR(255) PRIMARY KEY,
    side             VARCHAR(10) NOT NULL,
    ord_type         VARCHAR(20) NOT NULL,
    price            DOUBLE      NOT NULL,
    state            VARCHAR(20) NOT NULL,
    market           VARCHAR(20) NOT NULL,
    created_at       DATETIME    NOT NULL,
    volume           DOUBLE      NOT NULL,
    remaining_volume DOUBLE      NOT NULL,
    reserved_fee     DOUBLE      NOT NULL,
    remaining_fee    DOUBLE      NOT NULL,
    paid_fee         DOUBLE      NOT NULL,
    locked           DOUBLE      NOT NULL,
    executed_volume  DOUBLE      NOT NULL,
    executed_funds   DOUBLE      NOT NULL,
    trades_count     INT         NOT NULL,
    time_in_force    VARCHAR(20) NOT NULL
);

create table configs
(
    name varchar(255) not null
        primary key,
    val  varchar(255) null
);

INSERT INTO configs (name, val)
VALUES ('whole_sell_when_profit', 'true');
INSERT INTO configs (name, val)
VALUES ('scheduled_market', 'KRW-BTC, KRW-XRP, KRW-ETH');
INSERT INTO configs (name, val)
VALUES ('exchange_fee_ratio', '1.0005');
INSERT INTO configs (name, val)
VALUES ('take_profit_percentage', '1.0');
INSERT INTO configs (name, val)
VALUES ('min_order_amount', '5100');