ALTER TABLE diagnosis_key ADD COLUMN country varchar(3) NOT NULL;
ALTER TABLE diagnosis_key ADD COLUMN mobile_test_id varchar(15) NOT NULL;
ALTER TABLE diagnosis_key ADD COLUMN date_patient_infectious DATE NOT NULL;
ALTER TABLE diagnosis_key ADD COLUMN date_test_communicated DATE NOT NULL;
ALTER TABLE diagnosis_key ADD COLUMN result_channel int NOT NULL;
