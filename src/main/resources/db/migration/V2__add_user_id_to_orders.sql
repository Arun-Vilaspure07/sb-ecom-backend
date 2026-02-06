ALTER TABLE orders ADD COLUMN user_id BIGINT;

UPDATE orders o
SET user_id = u.user_id
FROM users u
WHERE o.email = u.email;

ALTER TABLE orders
ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE orders
ADD CONSTRAINT fk_orders_user
FOREIGN KEY (user_id)
REFERENCES users(user_id);
