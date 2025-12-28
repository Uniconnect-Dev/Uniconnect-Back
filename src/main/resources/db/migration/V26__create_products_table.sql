CREATE TABLE products (
    product_id BIGSERIAL PRIMARY KEY,

    company_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price INTEGER NOT NULL,
    thumbnail_url TEXT,
    short_description VARCHAR(300),

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- FK: products.company_id → companies.company_id
ALTER TABLE products
ADD CONSTRAINT fk_products_company
FOREIGN KEY (company_id)
REFERENCES companies(company_id)
ON DELETE CASCADE;
