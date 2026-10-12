# OpenCode V2 as the daily event-analysis process

Research for `auto-event-publication/issues/02-opencode-research.md`, 2026-10-13.

## Decision support

**Suitable in principle:** V2 explicitly supports noninteractive `opencode run`, file attachments, per-run model selection, and newline-delimited JSON (NDJSON). Installed v2.0.27 exposes these flags. **Not yet a verified end-to-end integration:** the reviewed documentation does not specify the NDJSON event envelope, final-answer extraction, CLI failure exit codes, or cancellation guarantees. A later authorized smoke test is needed before automated publication. Model and scheduler remain user decisions.

No model was invoked. Only local version/help/path inspection and official V2 documentation were used; no credentials or runtime configuration were read, no service-management commands were executed, and no scheduling or Telegram calls were made. This is a research asset, not an implementation.

## Sources and observed local evidence

Official sources accessed on the date above (live documentation, not pinned to v2.0.27):

- [C: CLI overview](https://opencode.ai/v2/docs/cli/)
- [R: CLI commands / run](https://opencode.ai/v2/docs/cli/commands/)
- [M: Models](https://opencode.ai/v2/docs/models/)
- [P: Permissions](https://opencode.ai/v2/docs/permissions/)
- [A: Agents](https://opencode.ai/v2/docs/agents/)
- [F: Configuration](https://opencode.ai/v2/docs/config/)
- [T: Troubleshooting](https://opencode.ai/v2/docs/troubleshooting/)

Observed locally, all commands returned successfully:

```text
$ command -v opencode
/opt/homebrew/bin/opencode
$ opencode --version
opencode v2.0.27
$ opencode run --help
USAGE: opencode run [flags] [<message...>]
--standalone          Run with a private server instead of the background service
--server string       Connect to a server URL instead of the background service
--continue, -c        Continue the last session
--session, -s string  Session ID to continue, or to create if it does not exist
--fork                Fork the session before continuing
--model, -m string    Model to use in the format provider/model#variant
--agent string        Agent to use
--format choice       Output format (choices: default, json)
--file, -f string     File to attach to the message
--title string        Session title
--thinking            Show thinking blocks
--auto                Auto-approve permissions that are not explicitly denied
```

The help also exposes global logging flags (`--log-level`, `--print-logs`); the latter prints logs to stderr, with server logs requiring standalone mode. `opencode models --help` confirms the listing command and its `--standalone` / `--server` flags. The catalog itself was not queried. Help lists no `run` timeout, JSON-schema, or stdin-specific option. These observations establish available syntax, not execution behavior.

## Confirmed invocation and input/output contract

| Aspect | Confirmed fact | Basis |
| --- | --- | --- |
| Entry point | `run` sends a message and prints the reply without the interactive interface; intended for scripts/CI. | C, R |
| Prompt | Positional message argument(s). | R, local help |
| Data files | `--file` attaches a file; repeated `--file` is documented. | R |
| JSON transport | `--format json` emits **newline-delimited JSON**, not a single application-result JSON document. | R |
| Model | `--model provider/model[#variant]` selects for one run without changing the configured default. IDs are case-sensitive; variants are catalog-specific. | M, local help |
| Agent | `--agent` selects an agent. A custom primary agent can define its prompt and permissions. | R, A |
| Continuation | `--continue`, `--session`, and `--fork` support session reuse. | R, local help |

Illustrative command shape only; placeholders are not real selections and this was **not executed**:

```sh
/opt/homebrew/bin/opencode run --standalone \
  --model 'PROVIDER/MODEL' --agent 'event-analysis' \
  --format json --file /absolute/path/event.json \
  'Classify the supplied event using the agreed filter and return only the requested JSON result.'
```

`event-analysis` would need to be configured later; it is not a built-in agent. A UTF-8 JSON file is a recommended representation of source facts, not a documented typed input API. Documentation confirms attachments generally, but their exact JSON MIME handling/size limits were not tested. Passing serialized facts inside the positional prompt is also possible at the text-contract level; direct process argument passing avoids shell interpolation. Stdin piping is **not confirmed** by the reviewed V2 docs/help and should not be the MVP assumption.

### JSON extraction: transport versus application result

**Documented:** NDJSON output. **Not documented in the reviewed CLI pages:** event names/fields, chunk ordering, terminal success/error events, which field contains the final assistant answer, stdout purity on every failure path, or schema-constrained generation. Do not copy a V1 `jq` recipe or assume fields such as `type`/`part.text` without validation against this installed V2 build.

**Recommendation:** capture stdout and stderr separately; parse stdout as NDJSON and extract the completed final assistant answer using a subsequently verified V2 envelope. Then parse that answer as a second JSON document and validate it locally. `--format json` alone does not establish that the model's answer conforms to the application's schema. Reject incomplete streams, malformed JSON, missing/duplicate results, and unsupported values; do not treat parseable intermediate text as success.

Proposed minimal application contract (not an OpenCode built-in contract): one supplied event per call, with `event_id`, source URL, and factual title/date/place plus optional time, ticket URL, type, language, and supporting source text. Unknown facts stay null/absent. Return `{ "event_id": "…", "decision": "publish|skip", "reason": "…", "text_ru": "… or null" }`. Validate the identifier, decision enum, required reason, and nonempty Russian publication text only for `publish`; require null text for `skip`. Final input fields depend on source research.

The prompt should encode the agreed filter: vocal concerts in Russian, English, French, German, or Spanish; instrumental concerts, opera, and ballet regardless of language. Uncertain eligibility or missing title/date/place means skip with a reason. A publication contains title, place, date, time if known, and a ticket/event URL, with no invented details. Treat source text as data, not instructions. The caller must check required source facts independently; JSON validation alone cannot prove factual accuracy or language classification.

## Permissions and unattended operation

**Documented:** default agents broadly allow tools; `plan` is not a no-tool agent. `ask` waits for a client decision. Noninteractive clients must handle approval requests. Rules use `action`, `resource`, and `effect`; the last matching rule wins and agent rules follow global rules. Explicit denies survive saved approvals. Custom agent permissions can deny `*` on `*`. Hidden maintenance agents cannot be selected directly. [P, A]

**Observed:** `--auto` approves permissions not explicitly denied. **Unknown:** default `run` behavior when an approval/question is requested without a terminal (reject, prompt, wait, or error); help/docs do not settle it.

**Recommendation:** later define a dedicated primary event-analysis agent with a final deny-all tool rule (`action: "*", resource: "*", effect: "deny"`), including questions, shell, subagents, web, and MCP. Supply source facts directly; classification and text generation need no tools. Do not rely on `--auto` to restrict capabilities. Confirm attachment ingestion works with this policy in the authorized smoke test. Permission denial is not an OS sandbox and does not imply that configuration/plugins or provider calls are disabled. No configuration was created or changed for this research.

## Errors, deadlines, and lifecycle

**Documented facts:**

- An unknown model variant produces a model-resolution error. Availability depends on enabled models, provider availability, credentials, and the working location; the configured default can fall back when unavailable. Explicit selection avoids relying on that default, but invalid explicit-selector behavior was not tested. [M]
- Clients normally discover/start a shared background server, which owns sessions, configuration, plugins, permissions, and tool execution. A CLI invocation is therefore not necessarily a self-contained process lifetime. [C, T]
- `run --standalone` uses a private server; the official CI example uses it so that the private server receives the job's provider-key environment. `--server` connects to an explicitly chosen server. [R, local help]
- `agents.<id>.steps` bounds model steps, with tools removed on the final step; it is not a wall-clock deadline. [A]
- Project/global configuration is merged and discovered up ancestor directories. Standalone mode is not documented as clean configuration or ephemeral session storage. [F, T]

**Missing documentation / untested behavior:** no CLI exit-code taxonomy, guaranteed error JSON envelope, total-run deadline, retry/backoff contract, response-size bound, or SIGTERM/SIGKILL cancellation semantics was found in the reviewed sources. The sources do not establish whether disconnecting/killing a shared-service client aborts remote work, nor exact private-server cleanup under forced termination. Do not promise that a timeout ends billing. Provider/auth/network failures and malformed outputs were not exercised.

**MVP recommendations (pending user choices):**

1. Use an explicit executable path and stable working directory, an explicitly chosen model, a dedicated no-tool agent, and independent sessions (omit continuation flags). Set the local launch environment deliberately; interactive-shell availability does not establish scheduled-job availability. Provider readiness in that location remains unverified.
2. Prefer per-invocation `--standalone` for this process-based use case, to avoid sharing the daily job's server lifecycle with interactive clients. This is a recommendation, not a verified cleanup guarantee or a config-isolation claim. Do not stop/restart the user's shared service for each daily job.
3. Have the caller impose a user-agreed wall-clock deadline and bound captured output, supervising the child process group, then verify cleanup during an authorized test. No built-in timeout flag was observed. Prevent overlapping daily executions; scheduler and deadline values remain undecided.
4. Require normal process completion plus a complete, validated application result. Treat nonzero exit, timeout, missing final answer, model/provider failure, and invalid output as analysis failure: no publication from that result; record a local reason. Distinguish operational failure from a valid filter `skip`. Whether to retry or defer to the next day remains a product decision; assume no automatic retry contract.

## Smallest follow-up needed before implementation is accepted

After model/permissions/deadline decisions and explicit permission to invoke a model, capture one synthetic event run with the installed version; establish NDJSON final-answer/error extraction and verify the schema and Russian text. Check an invalid model, an unavailable provider, a permission denial, malformed answer handling, and forced timeout/cleanup without sending anything to Telegram. Keep those fixtures/version evidence with the future integration. Research confirms the command surface; it does not certify unattended inference reliability on this computer.
