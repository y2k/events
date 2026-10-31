# Issue tracker: Local Markdown

Issues and specs live in `.scratch/`.

## Conventions

- One directory per feature: `.scratch/<feature-slug>/`.
- Spec: `spec.md` within that directory.
- Tickets: `issues/<NN>-<slug>.md`, numbered from `01`, one file per ticket.
- Triage state: a `Status:` line near the top, using roles from
  `docs/agents/triage-labels.md`.
- Conversation: append under `## Comments`.

When asked to publish, create the appropriate spec or ticket file.
When asked to fetch a ticket, read its referenced file.
Resolve bare ticket numbers within the relevant feature directory.

## Wayfinding operations

For `/wayfinder`:

- Map: `.scratch/<effort>/map.md`, containing Notes, Decisions-so-far, and Fog.
- Child: `issues/<NN>-<slug>.md`, with the question in the body and a
  `Type:` line: research, prototype, grilling, or task.
- Blocking: `Blocked by: NN, NN`. Unblocked when all listed tickets are resolved.
- Frontier: first ticket by number that is open, unblocked, and unclaimed.
- Claim: save `Status: claimed` before starting work.
- Resolve: append `## Answer`, set `Status: resolved`, and add a summary
  and link to the map's Decisions-so-far.
