-- Triggers below facilitate the replication & deletion of diagnosis keys to the dedicated Federation Upload key table

CREATE OR REPLACE FUNCTION mirror_uploadable_keys()
RETURNS TRIGGER AS $$
BEGIN
    IF ( NEW.CONSENT_TO_FEDERATION = TRUE ) THEN
        INSERT INTO federation_upload_key VALUES (NEW.key_data,NEW.rolling_period,NEW.rolling_start_interval_number,NEW.submission_timestamp,NEW.transmission_risk_level,NEW.consent_to_federation,NEW.origin_country,NEW.visited_countries,NEW.report_type,NEW.days_since_onset_of_symptoms);
    END IF;
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;





