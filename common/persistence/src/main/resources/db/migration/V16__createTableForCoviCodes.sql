CREATE TABLE covi_code (
    code varchar(12),
    start_interval timestamp NOT NULL,
    end_interval timestamp NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'CREATED',
    PRIMARY KEY(code, start_interval, end_interval)
);
