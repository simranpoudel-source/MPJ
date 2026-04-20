CREATE TABLE IF NOT EXISTS warehouse (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS location (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL REFERENCES warehouse(id),
    zone VARCHAR(50) NOT NULL,
    rack VARCHAR(50) NOT NULL,
    shelf VARCHAR(50) NOT NULL,
    bin VARCHAR(50) NOT NULL,
    location_type VARCHAR(30) NOT NULL,
    capacity INTEGER NOT NULL,
    occupied INTEGER NOT NULL DEFAULT 0,
    UNIQUE (warehouse_id, zone, rack, shelf, bin)
);

CREATE TABLE IF NOT EXISTS product (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(80) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    barcode VARCHAR(120) UNIQUE NOT NULL,
    reorder_level INTEGER NOT NULL DEFAULT 0,
    velocity_class VARCHAR(20),
    weight_class VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS lot_batch (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(id),
    lot_no VARCHAR(80) NOT NULL,
    expiry_date DATE,
    received_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS inventory (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(id),
    warehouse_id BIGINT NOT NULL REFERENCES warehouse(id),
    location_id BIGINT REFERENCES location(id),
    lot_batch_id BIGINT REFERENCES lot_batch(id),
    quantity INTEGER NOT NULL,
    reserved_qty INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS movement_history (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES product(id),
    from_location_id BIGINT REFERENCES location(id),
    to_location_id BIGINT REFERENCES location(id),
    quantity INTEGER NOT NULL,
    movement_type VARCHAR(50) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    reference_no VARCHAR(120),
    performed_by VARCHAR(80)
);

CREATE TABLE IF NOT EXISTS sales_order (
    id BIGSERIAL PRIMARY KEY,
    order_no VARCHAR(80) UNIQUE NOT NULL,
    warehouse_id BIGINT NOT NULL REFERENCES warehouse(id),
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS sales_order_line (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES sales_order(id),
    product_id BIGINT NOT NULL REFERENCES product(id),
    requested_qty INTEGER NOT NULL,
    allocated_qty INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS picking_task (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES sales_order(id),
    worker_username VARCHAR(80),
    wave_no VARCHAR(80),
    strategy VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(80) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS app_role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(40) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES app_user(id),
    role_id BIGINT NOT NULL REFERENCES app_role(id),
    PRIMARY KEY(user_id, role_id)
);

CREATE TABLE IF NOT EXISTS alert (
    id BIGSERIAL PRIMARY KEY,
    alert_type VARCHAR(40) NOT NULL,
    message VARCHAR(255) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(80),
    method VARCHAR(20) NOT NULL,
    path VARCHAR(255) NOT NULL,
    status_code INTEGER NOT NULL,
    event_time TIMESTAMP NOT NULL
);

ALTER TABLE picking_task
    ADD COLUMN IF NOT EXISTS progress_pct INTEGER NOT NULL DEFAULT 0;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uq_inventory_product_warehouse_lot'
    ) THEN
        ALTER TABLE inventory
            ADD CONSTRAINT uq_inventory_product_warehouse_lot
                UNIQUE (product_id, warehouse_id, lot_batch_id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_movement_event_time ON movement_history(event_time);
CREATE INDEX IF NOT EXISTS idx_audit_username ON audit_log(username);
CREATE INDEX IF NOT EXISTS idx_audit_event_time ON audit_log(event_time);
