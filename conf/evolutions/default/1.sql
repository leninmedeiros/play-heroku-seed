# --- !Ups
----Table 'question' ups codes - INIT
--
--CREATE TABLE "question" (
--    "id" SERIAL NOT NULL PRIMARY KEY,
--    "title" TEXT,
--    "body" TEXT,
--    "creationDate" DATE,
--    "tags" TEXT
--);
--
----You'll need to change the path of QuestionsReducedParsed.csv
----To execute this, go to the PostgreSQL database (this is a psql command)
----Remember: you'll need to comment this line when deploying
----Finally, you'll need to set the file's permission if necessary
--\copy "question" from '/home/lenin/workspace/activator-1.2.12/play-heroku-seed/QuestionsReducedParsed.csv' WITH DELIMITER ',' CSV HEADER;
--
--ALTER TABLE question ADD COLUMN link TEXT DEFAULT NULL;
--
--UPDATE question SET link = 'http://pt.stackoverflow.com/questions/'||CAST(id AS TEXT)||'#post-editor';
--
----Table 'question' ups codes - END

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

CREATE TABLE "user_configuration" (
    "id" SERIAL NOT NULL PRIMARY KEY,
    "configuration_id" INT NOT NULL REFERENCES configuration (id),
    "ip" VARCHAR(255) NOT NULL
);

CREATE TABLE "shared_question" (
    "id" SERIAL NOT NULL PRIMARY KEY,
    "user_configuration_id" INT NOT NULL REFERENCES user_configuration (id),
    "question_id" INT NOT NULL REFERENCES question (id)
);

CREATE TABLE "displayed_question" (
    "id" SERIAL NOT NULL PRIMARY KEY,
    "user_configuration_id" INT NOT NULL REFERENCES user_configuration (id),
    "question_id" INT NOT NULL REFERENCES question (id)
);

# --- !Downs
DROP TABLE "displayed_question";
DROP TABLE "shared_question";
DROP TABLE "user_configuration";
DROP TABLE "last_configuration";
DROP TABLE "configuration";

--DROP TABLE "question";
