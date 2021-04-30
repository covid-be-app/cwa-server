CREATE TABLE covicode (
    code varchar(12) PRIMARY KEY,
    startInterval datetime NOT NULL,
    endInterval datetime NOT NULL,
    status varchar(20) NOT NULL DEFAULT 'CREATED'
);
