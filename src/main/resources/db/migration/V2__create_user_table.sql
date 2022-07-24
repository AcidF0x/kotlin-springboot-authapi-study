CREATE TABLE user
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    email        VARCHAR(255) NOT NULL,
    nickname     VARCHAR(255) NOT NULL,
    password     VARCHAR(255) NOT NULL,
    name         VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    created_at   datetime     NOT NULL,
    updated_at   datetime     NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT unique_user_email UNIQUE (email),
    CONSTRAINT unique_user_phonenumber UNIQUE (phone_number),
);
