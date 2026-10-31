# AGENTS.md

## Overview

Cloudflare Worker written in ly2k (Clojure dialect compiling to JS). Event recommendation form app.

## Principles

- **Default workspace**: Work on `main` in the primary worktree unless explicitly instructed otherwise.
- **MVP first**: Start with the smallest working version that solves the current problem. Extend it only when explicitly needed.
- **Minimal work**: Do the least amount of work required. Write less code.
- **File size**: Prefer keeping `.clj` files at 100 lines or fewer.
- **One example**: When asked to write a test, write 1 test. When asked for an example, give 1 example.
- **No over-engineering**: Simple solutions first. Add complexity only when explicitly needed.
- **Run tests after changes**: After code changes, run `make test` from the repo root unless the user explicitly asks not to.

## Language & Toolchain

- **Existing stack first**: Write new code, including local scripts, automation, and tests, in ly2k. Evaluate simplicity for the whole project, not just one file.
- Before choosing an implementation, check existing code, libraries, and current stack capabilities. Unfamiliarity with the language is not evidence of a limitation.
- Another language requires the user's explicit approval of that specific exception. This rule applies during planning as well as implementation.

- **ly2k**: Clojure-like language targeting JS via `ly2k compile`
- Source files use `.clj` extension but are NOT standard Clojure
- Build generates JS to `.wrangler/bin/` (gitignored)
- Deploys to Cloudflare Workers via wrangler

## Structure

```
src/           # ly2k source
test/          # Tests run via node --test
.wrangler/     # Wrangler config, compiled output
build.clj      # ly2k build config (deps, targets)
```

## Agent skills

### Issue tracker

Issues live in `.scratch/<feature>/`. Read `docs/agents/issue-tracker.md` before issue operations.

### Y2K Language issues

Create ly2k language, compiler, and prelude issues in
https://github.com/y2k/language/issues. Follow the `y2k-language` skill.

Describe the problem, its impact, and expected behavior; include a minimal
reproduction when applicable. Leave solution design to the maintainers:
omit proposed implementations, acceptance criteria, and task checklists.

### Triage labels

Use the five default triage roles. Read `docs/agents/triage-labels.md` when triaging.

### Domain docs

Single-context layout. Read `docs/agents/domain.md` before exploring the codebase.
