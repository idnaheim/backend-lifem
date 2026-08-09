# Lifem — Product Overview

Lifem is a personal life management backend. It helps individuals track their financial activity and personal events in one place.

## Core Domains

- **Accounts** — Bank/cash/wallet accounts with balances and categories
- **Income** — Income sources with frequency tracking (weekly, bi-weekly, monthly, quarterly, yearly, one-time)
- **Expenses** — Recurring and one-time expenses linked to accounts
- **Transactions** — Immutable ledger entries created when income is received or expenses are paid; each gets a generated 10-char reference number
- **Events / Calendar Events** — Personal events and calendar scheduling
- **Passwords** — Encrypted credential storage using Spring Security's `TextEncryptor`

## Key Business Rules

- Income and expenses are linked to an `AccountEntity`; paying or receiving updates the account balance directly
- Transactions are the audit trail — they should never be created manually outside of a service action
- All entities carry `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate` audit fields via `AuditingEntity`
- The app is timezone-aware (Asia/Manila, GMT+08:00)
- There is currently no authentication layer; the auditor defaults to `"SYSTEM"`
