---
status: accepted
---

# Independent interest accrual batches

Process the initially selected deposit IDs sequentially in batches of at most 500,
with a separate transaction for each batch. This replaces the whole-run atomicity
decision in [ADR 0001](0001-explicit-interest-accrual-runs.md): we accept partial
success to bound transaction duration, loaded entities, and lock retention.

The coordinator selects all IDs once in ascending order and keeps them in memory;
later inserts remain outside the run. A separate Spring-managed batch service
uses `REQUIRES_NEW`, so each batch commits independently of any caller transaction.
It locks only the specified deposits in ascending ID order before calculation,
retaining locks until that batch commits or rolls back. Concurrent requests may
interleave between batches, while each calculation uses the latest committed
balance after acquiring its lock.

The synchronous POST returns 204 only after every batch succeeds, including for
an empty run. If a batch fails, that batch rolls back, earlier batches remain
committed, later batches are skipped, and the failure propagates as HTTP 500.
Retries still accrue again without deduplication, so retrying a partially failed
run applies additional interest to deposits already committed. Plan rules,
rounding, elapsed days, withdrawals, and existing access remain unchanged.
