INSERT INTO "timeDeposits" ("planType", "balance", "days") VALUES
    ('basic', 1200.00, 31),
    ('student', 1200.00, 365),
    ('premium', 1200.00, 46),
    ('basic', 1200.00, 30),
    ('student', 1200.00, 366),
    ('custom-plan', 1200.00, 90);

INSERT INTO "withdrawals" ("timeDepositId", "amount", "date") VALUES
    (1, 12.34, '2026-10-01'),
    (3, 99.99, '2026-10-02');
