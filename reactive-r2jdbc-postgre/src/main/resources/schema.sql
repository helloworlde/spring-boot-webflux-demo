DROP DATABASE IF EXISTS post;
CREATE DATABASE post;
CREATE SCHEMA post;
CREATE TABLE post.post
(
  id          SERIAL,
  title       VARCHAR(100),
  content     VARCHAR(1000),
  create_date TIMESTAMP DEFAULT now(),
  CONSTRAINT pk_post_id PRIMARY KEY (id)
);