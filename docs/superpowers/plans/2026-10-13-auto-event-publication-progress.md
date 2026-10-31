# SDD ledger — plan: docs/superpowers/plans/2026-10-13-auto-event-publication.md

## Approved scope overrides

- User approved one JSON file per source ID instead of SQLite.
- User explicitly removed concurrent-run locking and recovery of locks from MVP. Run only one instance at a time.
- stdout/stderr go to the project's `~/.local/state/events/publish.log`, with timestamp, event ID and action/error. No rotation or separate logger.
- User chose the current directory with a new branch: `auto-event-publication`.

Ruling: These later user decisions supersede SQLite/locking steps in the original plan and reliability ticket. No database, TCP lock, PID lock, or lock tests will be implemented.
Ruling: Track execution here with native file tools instead of the skill's shell bookkeeping scripts; no commits requested.

## Pre-flight

- Task 1 → 2: extracted event fields supply the AI input and validation; shared `event_id`, title, date, time, venue, URL.
- Task 1 + 2 → 3: pipeline consumes parsed cards/events and a validated decision. Storage failures must propagate outside per-event analysis recovery.
- Baseline: initial `make test` failed because wrangler was absent. User approved restoring the environment. Installed existing wrangler dependency locally without package/lock files. `make test`: 2/2 pass.

## Tasks

- [x] Task 1: source extraction and date rules. RED missing module → GREEN extraction/date assertions, suite 3/3.
- [x] Task 2: isolated AI invocation and response contract. RED missing read-answer → GREEN strict NDJSON, malformed/error/nonzero/timeout cases.
- [x] Task 3: file journal, publication, launchd instructions. `make test`: 3/3 pass; no-config CLI exits 1 with `missing_telegram_config`; README plist passes `plutil -lint`.
- [x] Final tests and fresh review. Both Important findings fixed RED→GREEN; final `make test`: 3/3 pass, 0 failures (~11.7s), `git diff --check` clean.

Task 3: pipeline RED missing run-publication → GREEN real file journal, two runs, Telegram request body, failure matrix, date order, 3000 ms pause, failed claim preventing external work. `make test`: 3/3 pass.

Ruling: Document native Node `--env-file` for local credentials instead of storing bot credentials in launchd XML. Existing environment names unchanged; launchd must point to the local credential file.

Environment note: one full-suite run hit an intermittent Wrangler reload loop in the existing POST test and was terminated at 120 seconds. Standalone existing tests and subsequent full suites passed; no unrelated Worker changes made.

Read-only live-source check: fetched current list and event5657 via HTTP, then ran compiled ly2k reader against saved HTML. Found 50 cards; event5657 parsed as 2026-12-20 20:00 with its event-specific tickets.rs URL. No AI invocation or Telegram request. Removed the temporary probe.

Remaining deployment: user supplies local Telegram credentials and enables launchd using README. No live channel publication or schedule installation performed during implementation.

## Final review

Fresh read-only reviewer: ses_ecaab12e2ffe2LgGZ0CTQCZ0KJ. No Critical findings; two Important findings verified with regressions.

- Final: fixed local descendant surviving timeout — separate timeout-controller process plus SIGTERM-ignoring descendant test RED→GREEN; suite 3/3. Escalation is referenced and awaited before rejecting.
- Final: fixed conflicting duplicate card evidence — repeated ID/URL with different dates reproduced missing rejection (RED); reject the inconsistent listing before any claims or sends (GREEN), suite 3/3.
- Final: minor (deferred): crash-after-claim test uses an empty existing file rather than killing the publication process itself.
- Final: minor (deferred): claim-write failure is tested; failure during final record replacement is covered by propagation structure, not a separate executable case.
- Final: minor (deferred): parent of newly created state directories is not fsynced; stronger whole-machine power-loss guarantees are outside process-interruption MVP.

Final: Ruling: conflicting duplicate dates fail the whole listing before processing — avoids silently dropping evidence with the smallest change — cost: unaffected new events wait for a corrected list.
Final: Ruling: concurrency remains excluded by explicit user decision — cost: overlapping runs are unsupported.
Final: Ruling: live AI quality, effective runtime permissions, Telegram delivery and installed launchd execution are not newly verified; use previously recorded AI probe plus offline boundary tests and plist lint — cost: first configured live run may expose integration differences.
Final: Ruling: remote provider cancellation and complete/future HTML source coverage are not guaranteed — matches the agreed limits — cost: a timed-out remote computation may continue and source changes may stop a run.
