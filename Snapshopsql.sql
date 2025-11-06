-- ----------------------------------------------------
-- DATABASE SETUP
-- ----------------------------------------------------

-- Create the database if it doesn't already exist
CREATE DATABASE IF NOT EXISTS snapshop_db;

-- Use the database for subsequent table and data commands
USE snapshop_db;

-- ----------------------------------------------------
-- 1. Table: products (Catalog)
-- Stores the fixed items loaded by the Java app's initializeProducts() method.
-- ----------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL UNIQUE, -- MUG, TSHIRT, FRAME
    price DECIMAL(10, 2) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------
-- 2. Table: orders (Master Transaction Record)
-- Stores the main order total and unique code upon checkout.
-- ----------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    order_code VARCHAR(10) UNIQUE NOT NULL, -- The Java-generated SS###### ID
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL,
    status ENUM('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED') NOT NULL DEFAULT 'PENDING'
);

-- ----------------------------------------------------
-- 3. Table: order_details (Customized Items/Cart Contents)
-- Stores the specific customized items linked to an order.
-- ----------------------------------------------------
CREATE TABLE IF NOT EXISTS order_details (
    detail_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    size VARCHAR(10) NOT NULL,
    color_rgb VARCHAR(20) NOT NULL, -- Stores the R,G,B value (e.g., "255,255,255")
    price DECIMAL(10, 2) NOT NULL,
    
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
        ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- ----------------------------------------------------
-- 4. Initial Data Insertion (Populate Catalog)
-- Provides the starting data for the Java application to display.
-- ----------------------------------------------------
INSERT INTO products (name, type, price, description) VALUES
('Printed Mug', 'MUG', 250.00, 'Perfect for your morning coffee'),
('Custom T-Shirt', 'TSHIRT', 499.00, 'Wear your memories'),
('Photo Frame', 'FRAME', 350.00, 'Frame your special moments');


-- ----------------------------------------------------
-- HELPER QUERIES (To check the data after running the Java app)
-- ----------------------------------------------------

-- To view all saved orders and their itemized details:
SELECT 
    o.order_code AS OrderID,
    o.order_date AS Date,
    od.product_name AS ItemName,
    od.size AS ItemSize,
    od.color_rgb AS ColorCode,
    od.price AS ItemPrice,
    o.total_amount AS Total
FROM
    orders o
JOIN
    order_details od ON o.order_id = od.order_id
ORDER BY
    o.order_date DESC;