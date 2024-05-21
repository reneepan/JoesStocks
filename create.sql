CREATE DATABASE joesstocks;

USE joesstocks;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50),
    password VARCHAR(256),
    email VARCHAR(100),
    balance DECIMAL(10, 2) DEFAULT 50000.00 
);

CREATE TABLE portfolio (
    trade_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT ,
    ticker VARCHAR(10),
    numStock INT ,
    price DECIMAL(10, 2)
);