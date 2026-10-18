---
status: accepted
---

# Explicit interest accrual runs

The whole-run atomicity and serialization decisions below are superseded by
[independent accrual batches](0002-independent-accrual-batches.md). The remaining
decisions continue to apply.

Each request performs one monthly interest accrual using the established calculator
rules, rather than representing a particular calendar month. Repeated requests
therefore compound interest again, including retries after a lost response; we
accept this risk for the kata instead of introducing period tracking or
deduplication. Elapsed days and withdrawals remain unchanged.

The agreed contract is `POST /time-deposits/interest-accruals` with no request body
and `204 No Content` after commit, including for an empty database. Processing is
synchronous for a small dataset, with one transaction that either commits all
balance changes or rolls them all back. Existing access rules remain in place.

The run selects deposit IDs once at its start. Deposits inserted after that
selection are left for a subsequent run. Selected deposits are locked in ascending
ID order before their balances are read for calculation, retaining pessimistic
write locks until transaction completion. Concurrent successful runs each accrue
interest once on the balances produced by preceding committed runs.

Implementation will explain the repeat-accrual and selection assumptions in
concise code comments. Contract-first HTTP tests will cover populated and empty
databases, repeated requests, and persisted effects; database integration tests
will verify rollback and concurrent transaction locking. Existing calculation
boundaries and rounding remain unchanged.
