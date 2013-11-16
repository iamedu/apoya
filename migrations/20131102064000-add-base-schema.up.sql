CREATE FUNCTION Update_Last_Updated_Column()
RETURNS TRIGGER AS '
BEGIN
   NEW.last_updated = now(); 
   RETURN NEW;
END; ' language 'plpgsql';

CREATE TABLE Sites (
    domain VARCHAR(100) NOT NULL,
    description VARCHAR(4096),
    date_created TIMESTAMP(2)
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL,
    last_updated TIMESTAMP(2)
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL,
    PRIMARY KEY(domain)
);

CREATE TRIGGER Update_Sites_Timestamp BEFORE UPDATE ON Sites
    FOR EACH ROW EXECUTE PROCEDURE
    Update_Last_Updated_Column();

CREATE TABLE Languages (
    language VARCHAR(20) NOT NULL,
    PRIMARY KEY(language)
);

CREATE TYPE User_Status AS ENUM ('not-entered', 'filling-profile', 'full-profile');

CREATE TABLE Users (
    username VARCHAR(512) NOT NULL,
        CONSTRAINT Lowercase_Username
        CHECK (LOWER(username) = username),
    email VARCHAR(512) NOT NULL,
        CONSTRAINT Lowercase_Email
        CHECK (LOWER(email) = email),
    password VARCHAR(512) NOT NULL,
    mail_confirmed BOOLEAN DEFAULT false NOT NULL,
    status User_Status NOT NULL DEFAULT 'not-entered',
    active BOOLEAN DEFAULT true NOT NULL,
    date_created TIMESTAMP(2)
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL,
    last_updated TIMESTAMP(2)
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL,
    PRIMARY KEY (username),
    CONSTRAINT Unique_Email 
    UNIQUE (email)
);


CREATE TRIGGER Update_Users_Timestamp BEFORE UPDATE ON Users 
    FOR EACH ROW EXECUTE PROCEDURE
    Update_Last_Updated_Column();

CREATE TABLE Roles (
    -- The role code should be a working name
    -- for each role
    role_code VARCHAR(64) NOT NULL,
    description VARCHAR(1024) NOT NULL,
    date_created TIMESTAMP(2)
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL,
    last_updated TIMESTAMP(2)
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL,
    PRIMARY KEY (role_code)
);

CREATE TRIGGER Update_Roles_Timestamp BEFORE UPDATE ON Roles
    FOR EACH ROW EXECUTE PROCEDURE
    Update_Last_Updated_Column();

CREATE TABLE Role_Assignments (
    username VARCHAR(512) NOT NULL
        REFERENCES Users (username)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    role_code VARCHAR(64) NOT NULL
        REFERENCES Roles (role_code)
        ON UPDATE CASCADE,
    date_created TIMESTAMP(2)
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL,
    last_updated TIMESTAMP(2)
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL,
    PRIMARY KEY (username, role_code)
);

CREATE TRIGGER Update_Role_Assignments_Timestamp BEFORE UPDATE ON Role_Assignments
    FOR EACH ROW EXECUTE PROCEDURE
    Update_Last_Updated_Column();

CREATE TABLE Restricted_Urls (
    url VARCHAR(1024) NOT NULL,
    description TEXT,
    PRIMARY KEY (url)
);

CREATE TABLE Role_Urls (
    role_code VARCHAR(64) NOT NULL
        REFERENCES Roles (role_code)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    url VARCHAR(1024) NOT NULL
        REFERENCES Restricted_Urls(url)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    PRIMARY KEY (role_code, url)
);

CREATE TABLE Person_Urls (
    username VARCHAR(64) NOT NULL
        REFERENCES Users (username)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    url VARCHAR(1024) NOT NULL
        REFERENCES Restricted_Urls(url)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    PRIMARY KEY (username, url)
);

CREATE TABLE Permissions (
    permission VARCHAR(1024) NOT NULL,
    description TEXT,
    PRIMARY KEY (permission)
);

CREATE TABLE Role_Permissions (
    role_code VARCHAR(64) NOT NULL
        REFERENCES Roles (role_code)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    permission VARCHAR(1024) NOT NULL
        REFERENCES Permissions(permission)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    PRIMARY KEY (role_code, permission)
);

CREATE TABLE Person_Permissions (
    username VARCHAR(64) NOT NULL
        REFERENCES Users (username)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    permission VARCHAR(1024) NOT NULL
        REFERENCES Permissions(permission)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    PRIMARY KEY (username, permission)
);

CREATE TABLE Error_Sources (
    name VARCHAR(50) NOT NULL,
    description VARCHAR(4096),
    date_created TIMESTAMP(2)
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL,
    last_updated TIMESTAMP(2)
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL,
    PRIMARY KEY (name)
);

CREATE TRIGGER Update_Error_Sources_Timestamp BEFORE UPDATE ON Error_Sources
    FOR EACH ROW EXECUTE PROCEDURE
    Update_Last_Updated_Column();

CREATE UNLOGGED TABLE Errors (
    error_sha1 VARCHAR(50) NOT NULL,
    error_text TEXT NOT NULL,
    first_appearance TIMESTAMP(2)
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL,
    PRIMARY KEY (error_sha1)
);

CREATE TYPE Error_Severity AS ENUM ('INFO', 'WARNING', 'DANGER', 'FATAL');

CREATE UNLOGGED TABLE Error_Events (
    event_sha1 VARCHAR(50) NOT NULL,
    error_sha1 VARCHAR(50) NOT NULL
        REFERENCES Errors (error_sha1)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,
    username VARCHAR(512)
        REFERENCES Users (username)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    error_source VARCHAR(255) NOT NULL
        REFERENCES Error_Sources (name)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,
    domain VARCHAR(100)
        REFERENCES Sites (domain)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    severity Error_Severity NOT NULL,
    metadata json,
    event_date TIMESTAMP(2)
        DEFAULT CURRENT_TIMESTAMP
        NOT NULL,
    PRIMARY KEY (event_sha1)
);

CREATE TABLE Labels (
    label_key VARCHAR(120) NOT NULL,
    label_text TEXT NOT NULL,
    domain VARCHAR(100) NOT NULL
        REFERENCES Sites (domain)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
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
    PRIMARY KEY(label_key, domain, language)
);

CREATE TRIGGER Update_Labels_Timestamp BEFORE UPDATE ON Labels
    FOR EACH ROW EXECUTE PROCEDURE
    Update_Last_Updated_Column();

--- Data
INSERT INTO Error_Sources(name, description) VALUES ('webapp', 'Something happened with the webapp, most errors should be related to this');
INSERT INTO Error_Sources(name, description) VALUES ('mq', 'There was an error when processing a message in the messaging queue');
INSERT INTO Error_Sources(name, description) VALUES ('quartz', 'There was a problem while running a cron job');
INSERT INTO Error_Sources(name, description) VALUES ('netty', 'There was an error with fortress or the netty library');

INSERT INTO Users(username, email, password) VALUES ('iamedu', 'iamedu@gmail.com', '$2a$10$I8IVBVBHmC.Yhn14kjbi0uL91J6YCv.lYG1/XrxHAKj/WyrQXXj3.');
INSERT INTO Roles(role_code, description) VALUES ('admin', 'User that can do anything');
INSERT INTO Role_Assignments(username, role_code) VALUES ('iamedu', 'admin');

INSERT INTO Permissions(permission, description) VALUES ('*:*', 'Permission to do anything');
INSERT INTO Permissions(permission, description) VALUES ('sample:*', 'Can do anything for the sample module');
INSERT INTO Restricted_Urls(url, description) VALUES ('.*', 'Every url!');

INSERT INTO Role_Permissions(role_code, permission) VALUES ('admin', '*:*');
INSERT INTO Person_Permissions(username, permission) VALUES ('iamedu', 'sample:*');
INSERT INTO Role_Urls(role_code, url) VALUES ('admin', '.*');

INSERT INTO Languages(language) VALUES ('en');
INSERT INTO Sites(domain, description) VALUES('default', 'Default website, when nobody else has entered!');

