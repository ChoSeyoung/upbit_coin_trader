use upbit;

create table configs
(
    name varchar(255) not null
        primary key,
    val  varchar(255) null
);

create table trades
(
    id              bigint auto_increment
        primary key,
    price           double       null,
    quantity        double       null,
    timestamp       datetime(6)  null,
    type            varchar(255) null,
    simulation_mode bit          null,
    ticker_symbol   varchar(255) null,
    identifier      varchar(255) null,
    is_signed       bit          null,
    exchange_fee    double       null
);

create table users
(
    id        bigint auto_increment
        primary key,
    inventory json null
);

INSERT INTO users (id, inventory) VALUES (1, null);
INSERT INTO configs (name, val) VALUES ('whole_sell_when_profit', 'true');
