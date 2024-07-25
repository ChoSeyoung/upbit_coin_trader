use upbit;

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
VALUES ('exchange_fee_percentage', '0.05');
INSERT INTO configs (name, val)
VALUES ('take_profit_percentage', '1');