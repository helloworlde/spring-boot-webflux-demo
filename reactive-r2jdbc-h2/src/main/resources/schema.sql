CREATE TABLE post (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  title       VARCHAR(100),
  content     VARCHAR(1000),
  create_date DATETIME DEFAULT now()
);