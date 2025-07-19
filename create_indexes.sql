-- TODO
DROP INDEX IF EXISTS loginRole;

CREATE INDEX loginRole
ON Users
USING HASH
(login);