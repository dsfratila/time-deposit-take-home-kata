INSERT INTO "timeDeposits" ("planType", "balance", "days")
SELECT 'student', 1200.00, 365
FROM generate_series(1, 1001) AS deposits(position)
ORDER BY position;

INSERT INTO "withdrawals" ("timeDepositId", "amount", "date") VALUES
    (1, 12.34, '2026-10-01'),
    (501, 56.78, '2026-10-02'),
    (1001, 99.99, '2026-10-03');
