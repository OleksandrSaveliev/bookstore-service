CREATE TABLE users (
                       id       BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email    VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role     VARCHAR(20)  NOT NULL
) ENGINE=InnoDB;

CREATE TABLE client_profiles (
                                 id      BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 user_id BIGINT         NOT NULL UNIQUE,
                                 name    VARCHAR(255)   NOT NULL,
                                 balance DECIMAL(19, 2) NOT NULL,
                                 CONSTRAINT fk_client_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE employee_profiles (
                                   id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   user_id    BIGINT       NOT NULL UNIQUE,
                                   name       VARCHAR(255),
                                   phone      VARCHAR(50),
                                   birth_date DATE,
                                   CONSTRAINT fk_employee_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE books (
                       id               BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name             VARCHAR(255)   NOT NULL UNIQUE,
                       genre            VARCHAR(100),
                       age_group        VARCHAR(20),
                       price            DECIMAL(19, 2),
                       publication_year DATE,
                       author           VARCHAR(255),
                       number_of_pages  INT,
                       stock            INT,
                       characteristics  VARCHAR(500),
                       description      TEXT,
                       language         VARCHAR(20)
) ENGINE=InnoDB;