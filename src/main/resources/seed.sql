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