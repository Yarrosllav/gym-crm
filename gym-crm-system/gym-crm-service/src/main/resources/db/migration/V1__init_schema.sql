CREATE TABLE users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    username   VARCHAR(150) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    is_active  BOOLEAN      NOT NULL
);

CREATE TABLE training_types
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    training_type_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE trainers
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT NOT NULL UNIQUE,
    specialization_id BIGINT NOT NULL,
    CONSTRAINT fk_trainers_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_trainers_specialization FOREIGN KEY (specialization_id) REFERENCES training_types (id)
);

CREATE TABLE trainees
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_of_birth DATE NULL,
    address       VARCHAR(255) NULL,
    user_id       BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_trainees_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE trainings
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    trainee_id        BIGINT       NOT NULL,
    trainer_id        BIGINT       NOT NULL,
    training_name     VARCHAR(150) NOT NULL,
    training_type_id  BIGINT       NOT NULL,
    training_date     DATE         NOT NULL,
    training_duration INT          NOT NULL,
    CONSTRAINT fk_trainings_trainee FOREIGN KEY (trainee_id) REFERENCES trainees (id),
    CONSTRAINT fk_trainings_trainer FOREIGN KEY (trainer_id) REFERENCES trainers (id),
    CONSTRAINT fk_trainings_type FOREIGN KEY (training_type_id) REFERENCES training_types (id)
);

CREATE TABLE trainee_trainer
(
    trainee_id BIGINT NOT NULL,
    trainer_id BIGINT NOT NULL,
    PRIMARY KEY (trainee_id, trainer_id),
    CONSTRAINT fk_tt_trainee FOREIGN KEY (trainee_id) REFERENCES trainees (id),
    CONSTRAINT fk_tt_trainer FOREIGN KEY (trainer_id) REFERENCES trainers (id)
);
