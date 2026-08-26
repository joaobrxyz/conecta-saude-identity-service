ALTER TABLE doctors DROP COLUMN IF EXISTS especialidade;

ALTER TABLE doctors ADD COLUMN specialty_id BIGINT;

ALTER TABLE doctors ADD CONSTRAINT fk_doctor_specialty
    FOREIGN KEY (specialty_id) REFERENCES specialties(id);