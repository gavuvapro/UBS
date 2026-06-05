CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE app_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_names VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(50),
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE app_user_roles (
    app_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (app_user_id, role_id)
);

CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_names VARCHAR(255) NOT NULL,
    national_id VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255),
    phone_number VARCHAR(50),
    address TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    user_id UUID UNIQUE REFERENCES app_users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE meters (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meter_number VARCHAR(100) NOT NULL UNIQUE,
    meter_type VARCHAR(20) NOT NULL,
    installation_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    customer_id UUID REFERENCES customers(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE meter_readings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meter_id UUID NOT NULL REFERENCES meters(id) ON DELETE CASCADE,
    previous_reading DECIMAL(18,2) NOT NULL DEFAULT 0,
    current_reading DECIMAL(18,2) NOT NULL,
    reading_date DATE NOT NULL,
    consumption DECIMAL(18,2) NOT NULL,
    reading_month INT NOT NULL,
    reading_year INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(meter_id, reading_month, reading_year)
);

CREATE TABLE tariffs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meter_type VARCHAR(20) NOT NULL,
    tariff_type VARCHAR(20) NOT NULL,
    unit_price DECIMAL(18,2),
    effective_date DATE NOT NULL,
    version INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tariff_tiers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tariff_id UUID NOT NULL REFERENCES tariffs(id) ON DELETE CASCADE,
    min_units DECIMAL(18,2) NOT NULL,
    max_units DECIMAL(18,2),
    price_per_unit DECIMAL(18,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE service_charges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    meter_type VARCHAR(20) NOT NULL,
    effective_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tax_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tax_name VARCHAR(100) NOT NULL,
    rate DECIMAL(5,4) NOT NULL,
    effective_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE penalty_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    penalty_type VARCHAR(20) NOT NULL,
    value DECIMAL(18,2) NOT NULL,
    grace_period_days INT NOT NULL DEFAULT 0,
    effective_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bills (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bill_reference VARCHAR(50) NOT NULL UNIQUE,
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    meter_id UUID NOT NULL REFERENCES meters(id) ON DELETE CASCADE,
    meter_reading_id UUID UNIQUE REFERENCES meter_readings(id) ON DELETE SET NULL,
    billing_month INT NOT NULL,
    billing_year INT NOT NULL,
    consumption DECIMAL(18,2) NOT NULL,
    consumption_charge DECIMAL(18,2) NOT NULL,
    service_charge DECIMAL(18,2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    penalty_amount DECIMAL(18,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(18,2) NOT NULL,
    amount_paid DECIMAL(18,2) NOT NULL DEFAULT 0,
    outstanding_balance DECIMAL(18,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    approved_by UUID REFERENCES app_users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bill_id UUID NOT NULL REFERENCES bills(id) ON DELETE CASCADE,
    amount_paid DECIMAL(18,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    payment_date DATE NOT NULL,
    reference VARCHAR(100) NOT NULL UNIQUE,
    recorded_by UUID REFERENCES app_users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notification_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_bills_customer ON bills(customer_id);
CREATE INDEX idx_bills_status ON bills(status);
CREATE INDEX idx_bills_reference ON bills(bill_reference);
CREATE INDEX idx_payments_bill ON payments(bill_id);
CREATE INDEX idx_notification_logs_customer ON notification_logs(customer_id);
CREATE INDEX idx_meter_readings_meter ON meter_readings(meter_id);
CREATE INDEX idx_meters_customer ON meters(customer_id);
CREATE INDEX idx_meters_number ON meters(meter_number);
CREATE INDEX idx_customers_national_id ON customers(national_id);
CREATE INDEX idx_customers_user ON customers(user_id);
