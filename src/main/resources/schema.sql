CREATE TABLE product
(
    id    UUID PRIMARY KEY,
    name  VARCHAR(255)   NOT NULL,
    price NUMERIC(19, 2) NOT NULL CHECK (price >= 0)
);

CREATE TABLE customer
(
    id         UUID PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL
);

CREATE TABLE customer_order
(
    id           UUID PRIMARY KEY,
    customer_id  UUID           NOT NULL REFERENCES customer (id),
    status       VARCHAR(30)    NOT NULL DEFAULT 'DRAFT',
    total_amount NUMERIC(19, 2) NOT NULL DEFAULT 0
        CHECK (total_amount >= 0),
    created_at   TIMESTAMP WITH TIME ZONE
                                NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_order_status
        CHECK (status IN ('DRAFT', 'CONFIRMED', 'CANCELLED'))
);

CREATE TABLE order_item
(
    id         UUID PRIMARY KEY,
    order_id   UUID           NOT NULL REFERENCES customer_order (id),
    product_id UUID           NOT NULL REFERENCES product (id),
    quantity   INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(19, 2) NOT NULL CHECK (unit_price >= 0)
);

CREATE TABLE stock
(
    product_id         UUID PRIMARY KEY REFERENCES product (id),
    available_quantity INTEGER NOT NULL DEFAULT 0
        CHECK (available_quantity >= 0),
    reserved_quantity  INTEGER NOT NULL DEFAULT 0
        CHECK (reserved_quantity >= 0)
);

CREATE TABLE stock_reservation
(
    id         UUID PRIMARY KEY,
    order_id   UUID        NOT NULL REFERENCES customer_order (id),
    product_id UUID        NOT NULL REFERENCES product (id),
    quantity   INTEGER     NOT NULL CHECK (quantity > 0),
    status     VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT chk_reservation_status
        CHECK (status IN ('ACTIVE', 'RELEASED', 'CONSUMED')),

    CONSTRAINT uq_order_product_reservation
        UNIQUE (order_id, product_id)
);

CREATE TABLE payment
(
    id       UUID PRIMARY KEY,
    order_id UUID           NOT NULL REFERENCES customer_order (id),
    amount   NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    status   VARCHAR(30)    NOT NULL DEFAULT 'PENDING',

    CONSTRAINT chk_payment_status
        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED'))
);