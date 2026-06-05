CREATE OR REPLACE FUNCTION generate_bill_notification(p_bill_id UUID)
RETURNS VOID AS $$
DECLARE
    v_customer_id UUID;
    v_full_names VARCHAR(255);
    v_billing_month INT;
    v_billing_year INT;
    v_total_amount DECIMAL(18,2);
    v_month_year TEXT;
    v_message TEXT;
BEGIN
    SELECT b.customer_id, c.full_names, b.billing_month, b.billing_year, b.total_amount
    INTO v_customer_id, v_full_names, v_billing_month, v_billing_year, v_total_amount
    FROM bills b
    JOIN customers c ON c.id = b.customer_id
    WHERE b.id = p_bill_id;

    IF v_customer_id IS NULL THEN
        RETURN;
    END IF;

    v_month_year := v_billing_month || '/' || v_billing_year;
    v_message := 'Dear ' || v_full_names || ', Your ' || v_month_year || ' utility bill of ' || v_total_amount || ' FRW has been successfully processed.';

    INSERT INTO notification_logs (customer_id, message, notification_type, sent)
    VALUES (v_customer_id, v_message, 'BILL_GENERATED', TRUE);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION on_full_payment(p_bill_id UUID)
RETURNS VOID AS $$
DECLARE
    v_customer_id UUID;
    v_full_names VARCHAR(255);
    v_bill_reference VARCHAR(50);
    v_message TEXT;
BEGIN
    SELECT b.customer_id, c.full_names, b.bill_reference
    INTO v_customer_id, v_full_names, v_bill_reference
    FROM bills b
    JOIN customers c ON c.id = b.customer_id
    WHERE b.id = p_bill_id;

    IF v_customer_id IS NULL THEN
        RETURN;
    END IF;

    UPDATE bills SET status = 'PAID' WHERE id = p_bill_id;

    v_message := 'Dear ' || v_full_names || ', Your payment for bill ' || v_bill_reference || ' has been received. Outstanding balance: 0 FRW.';

    INSERT INTO notification_logs (customer_id, message, notification_type, sent)
    VALUES (v_customer_id, v_message, 'PAYMENT_CONFIRMED', TRUE);
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION trg_after_bill_insert_func()
RETURNS TRIGGER AS $$
BEGIN
    PERFORM generate_bill_notification(NEW.id);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_after_bill_insert
AFTER INSERT ON bills
FOR EACH ROW
EXECUTE FUNCTION trg_after_bill_insert_func();

CREATE OR REPLACE FUNCTION trg_after_bill_update_payment_func()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.outstanding_balance = 0 AND OLD.outstanding_balance > 0 THEN
        PERFORM on_full_payment(NEW.id);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_after_payment_update
AFTER UPDATE ON bills
FOR EACH ROW
EXECUTE FUNCTION trg_after_bill_update_payment_func();
