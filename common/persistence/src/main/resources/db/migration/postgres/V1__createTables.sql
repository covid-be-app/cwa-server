CREATE TABLE diagnosis_key (
    id bigserial PRIMARY KEY,
    key_data bytea,
    rolling_period bigint NOT NULL,
    rolling_start_number bigint NOT NULL,
    submission_timestamp bigint NOT NULL,
    transmission_risk_level integer NOT NULL
);

CREATE TABLE export_configuration (
    id bigserial PRIMARY KEY,
    bucket_name text NOT NULL,
    filename_root text NOT NULL,
    period int NOT NULL,
    region text NOT NULL,
    from_timestamp timestamp NOT NULL,
    thru_timestamp timestamp NOT NULL,
    signing_key text NOT NULL,
    signing_key_id text NOT NULL,
    signing_key_version text NOT NULL,
    app_pkg_id text,
    bundle_id text
);

CREATE TABLE export_batch (
    id bigserial PRIMARY KEY,
    from_timestamp timestamp NOT NULL,
    to_timestamp timestamp NOT NULL,
    status int NOT NULL,
    configuration_id bigserial REFERENCES export_configuration (id)
);