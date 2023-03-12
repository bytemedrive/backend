create table customer
(
    id     varchar(32) primary key not null,
    events jsonb                   not null
);