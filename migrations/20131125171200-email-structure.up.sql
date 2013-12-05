CREATE TABLE Base_Mail_Templates (
    base_key VARCHAR(120) NOT NULL,
    domain VARCHAR(100) NOT NULL
        REFERENCES Sites (domain)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    body TEXT NOT NULL,
    description VARCHAR(2048) NOT NULL,
    language VARCHAR(20) NOT NULL
        REFERENCES Languages (language)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    date_created TIMESTAMP(2)
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL,
    last_updated TIMESTAMP(2)
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL,
    PRIMARY KEY(base_key, domain, language)
);

CREATE TRIGGER Update_Base_Mail_Timestamp BEFORE UPDATE ON Base_Mail_Templates
    FOR EACH ROW EXECUTE PROCEDURE
    Update_Last_Updated_Column();

CREATE TABLE Mail_Templates (
    template_key VARCHAR(120) NOT NULL,
    base_key VARCHAR(120) NOT NULL,
    domain VARCHAR(100) NOT NULL
        REFERENCES Sites (domain)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    language VARCHAR(20) NOT NULL
        REFERENCES Languages (language)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    description VARCHAR(2048) NOT NULL,
    parts JSON NOT NULL,
    date_created TIMESTAMP(2)
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL,
    last_updated TIMESTAMP(2)
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL,
    FOREIGN KEY (base_key, domain, language) REFERENCES Base_Mail_Templates (base_key, domain, language)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    PRIMARY KEY(template_key, domain, language)
);

CREATE TRIGGER Update_Mail_Timestamp BEFORE UPDATE ON Mail_Templates
    FOR EACH ROW EXECUTE PROCEDURE
    Update_Last_Updated_Column();

