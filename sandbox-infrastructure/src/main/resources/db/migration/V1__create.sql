CREATE TABLE IF NOT EXISTS user
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    name         TEXT NOT NULL,
    position     TEXT NOT NULL,
    mail_address TEXT NOT NULL
);
