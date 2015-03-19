# --- !Ups

CREATE TABLE "USERS" (
    "NAME" VARCHAR(254) NOT NULL,
    "ID" SERIAL NOT NULL PRIMARY KEY
);

CREATE TABLE "configuration" (
    "id" SERIAL NOT NULL PRIMARY KEY,
    "message" VARCHAR(100)
);

INSERT INTO configuration (id, message)
    VALUES (0, '');
INSERT INTO configuration (id, message)
    VALUES (1, 'Compartilhar');
INSERT INTO configuration (id, message)
    VALUES (2, 'Algumas pessoas costumam compartilhar para ajudar. Compartilhe!');
INSERT INTO configuration (id, message)
    VALUES (3, 'Algum amigo seu pode saber a resposta. Compartilhe!');

CREATE TABLE "last_configuration" (
    "id" SERIAL NOT NULL PRIMARY KEY REFERENCES configuration (id)
);

INSERT INTO last_configuration (id) VALUES(0);

# --- !Downs

DROP TABLE "USERS";
DROP TABLE "last_configuration";
DROP TABLE "configuration";

