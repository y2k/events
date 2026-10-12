# AGENTS.md

## Overview

Cloudflare Worker written in ly2k (Clojure dialect compiling to JS). Event recommendation form app.

## Principles

- **Minimal work**: Do the least amount of work required. Write less code.
- **One example**: When asked to write a test, write 1 test. When asked for an example, give 1 example.
- **No over-engineering**: Simple solutions first. Add complexity only when explicitly needed.
- **Run tests after changes**: After code changes, run `make test` from the repo root unless the user explicitly asks not to.

## Language & Toolchain

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
