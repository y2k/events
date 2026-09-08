# Issue tracker: GitHub

Issues and specs for this repo live as GitHub issues. Use the `gh` CLI for all operations.

## Conventions

- Create, read, edit, comment on, label, assign, and close issues with `gh issue`.
- Infer `y2k/events` from the repository remote.
- Pull requests are not a triage request surface.

## Publishing

When a skill says to publish to the issue tracker, create a GitHub issue. When it asks for a ticket, load the issue and its comments.

## Wayfinding operations

- A map is an issue labelled `wayfinder:map`.
- Tickets are GitHub sub-issues of the map, with a `wayfinder:research`, `wayfinder:prototype`, `wayfinder:grilling`, or `wayfinder:task` label.
- Use native GitHub issue dependencies for blocking. If unavailable, use a `Blocked by: #<number>` line.
- The frontier contains open, unblocked, unassigned child issues.
- Claim a ticket by assigning it to the current GitHub user before doing any work.
- Resolve a ticket by posting the answer as a comment, closing it, and adding a linked gist to the map's Decisions-so-far section.
