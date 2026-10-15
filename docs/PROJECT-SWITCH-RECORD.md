# Project switch record — 2026-10-15

## Decision
PocketLedger was formally stopped as the active project at the user's explicit direction. AgentForge is the sole active project. PocketLedger local artifacts are to be preserved and are not to be overwritten or deleted.

## Repository safety
- Target repository: https://github.com/Karnchoudhary-99/agentforge
- Verified repository exists, public, default branch `main`, user has admin/push permissions.
- At initial inspection it was empty: no files, no branches, no commits, and no GitHub Actions runs.
- Existing repositories including DevGuard must not be modified as part of AgentForge work.

## Delivery rule
Only this repository is in scope. Every session requires code review, tests, CI inspection, failure remediation, and an honest completion report. No credentials or secrets in Git.

## Prior artifacts
The previous PocketLedger source ZIP, wheel, and status report remain in the working environment; do not use them as AgentForge source.
