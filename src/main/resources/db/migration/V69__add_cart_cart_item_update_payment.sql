
-- Create carts table for multi-product shopping cart
CREATE TABLE IF NOT EXISTS carts (
                                     cart_id BIGSERIAL PRIMARY KEY,
                                     student_org_id BIGINT NOT NULL UNIQUE,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
                                     FOREIGN KEY (student_org_id) REFERENCES student_orgs(student_org_id) ON DELETE CASCADE
    );

-- Create index for student_org_id
CREATE INDEX IF NOT EXISTS idx_carts_student_org_id ON carts(student_org_id);

-- Create cart_items table
CREATE TABLE IF NOT EXISTS cart_items (
                                          cart_item_id BIGSERIAL PRIMARY KEY,
                                          cart_id BIGINT NOT NULL,
                                          product_id BIGINT NOT NULL,
                                          quantity INT NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (cart_id, product_id),
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
    );

-- Create indexes for cart_items
CREATE INDEX IF NOT EXISTS idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_items_product_id ON cart_items(product_id);

-- Add cart_id column to payments table if not exists
-- This allows payments to be linked to either a single product or a cart of multiple products
ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS cart_id BIGINT;
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'fk_payments_cart_id'
        ) THEN
            ALTER TABLE payments
                ADD CONSTRAINT fk_payments_cart_id
                    FOREIGN KEY (cart_id)
                        REFERENCES carts(cart_id)
                        ON DELETE SET NULL;
        END IF;
    END $$;

-- Create index for cart_id in payments
CREATE INDEX IF NOT EXISTS idx_payments_cart_id ON payments(cart_id);

-- Add comment to explain the usage
COMMENT ON COLUMN payments.cart_id IS 'Cart ID for cart-based multi-product payments. NULL for single product payments.';