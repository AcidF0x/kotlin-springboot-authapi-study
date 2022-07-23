CREATE TABLE auth_code
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    phone_number   VARCHAR(11)  NOT NULL,
    auth_code_type VARCHAR(255) NOT NULL,
    code           VARCHAR(6)   NOT NULL,
    send_count     INT          NOT NULL,
    requested_at   datetime     NOT NULL,
    verified_at    datetime NULL,
    created_at     datetime     NOT NULL,
    updated_at     datetime     NOT NULL,
    CONSTRAINT pk_authcode PRIMARY KEY (id),
    CONSTRAINT unique_phone_number_with_code_Type UNIQUE (phone_number, auth_code_type)
);
