INSERT INTO "timeDeposits" ("planType", "balance", "days") VALUES
    ('basic', 1234.56, 90),
    ('custom-plan', 0.00, 0),
    ('premium', 9876.54, 365);

INSERT INTO "withdrawals" ("timeDepositId", "amount", "date") VALUES
    ((SELECT "id" FROM "timeDeposits" WHERE "planType" = 'basic'), 12.34, '2026-10-01'),
    ((SELECT "id" FROM "timeDeposits" WHERE "planType" = 'premium'), 99.99, '2026-10-02'),
    ((SELECT "id" FROM "timeDeposits" WHERE "planType" = 'basic'), 5.67, '2026-09-30');
