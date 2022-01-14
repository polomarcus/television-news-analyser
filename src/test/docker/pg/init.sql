CREATE TABLE news (
  id SERIAL,
  title VARCHAR(255),
  description TEXT,
  "date" timestamptz,
  "order" int,
  presenter VARCHAR(255),
  authors TEXT [],
  editor VARCHAR(255),
  editorDeputy TEXT [],
  url VARCHAR(255),
  urlTvNews VARCHAR(255),
  year VARCHAR(50),
  containsWordGlobalWarming BOOLEAN
);