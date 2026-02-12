-- Allow new BookingStatus.EXPIRED (and keep PaymentStatus constrained too).
-- Your current DB has an auto-generated CHECK constraint (e.g. SYS_C....) that blocks new enum values.

DECLARE
BEGIN
    FOR c IN (
        SELECT uc.constraint_name
        FROM user_constraints uc
        JOIN user_cons_columns ucc
            ON ucc.constraint_name = uc.constraint_name
        WHERE uc.constraint_type = 'C'
          AND uc.table_name = 'BOOKING'
          AND ucc.column_name = 'STATUS'
    ) LOOP
        EXECUTE IMMEDIATE 'ALTER TABLE booking DROP CONSTRAINT ' || c.constraint_name;
    END LOOP;
END;
/

DECLARE
BEGIN
    FOR c IN (
        SELECT uc.constraint_name
        FROM user_constraints uc
        JOIN user_cons_columns ucc
            ON ucc.constraint_name = uc.constraint_name
        WHERE uc.constraint_type = 'C'
          AND uc.table_name = 'BOOKING'
          AND ucc.column_name = 'PAYMENT_STATUS'
    ) LOOP
        EXECUTE IMMEDIATE 'ALTER TABLE booking DROP CONSTRAINT ' || c.constraint_name;
    END LOOP;
END;
/

ALTER TABLE booking ADD CONSTRAINT chk_booking_status
CHECK (status IN ('PENDING', 'IN_PROGRESS', 'CONFIRMED', 'EXPIRED', 'CANCELLED'));

ALTER TABLE booking ADD CONSTRAINT chk_booking_payment_status
CHECK (payment_status IN ('NOT_STARTED', 'PENDING', 'PAID', 'FAILED'));

