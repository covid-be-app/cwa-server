CREATE TABLE authorization_code (
    signature varchar(512) PRIMARY KEY,
    mobile_test_id varchar(15) NOT NULL,
    date_patient_infectious date NOT NULL,
    date_test_communicated date NOT NULL
);
