# AGENTS.md

## Overview

Cloudflare Worker written in ly2k (Clojure dialect compiling to JS). Event recommendation form app.

## Principles

- **Minimal work**: Do the least amount of work required. Write less code.
- **One example**: When asked to write a test, write 1 test. When asked for an example, give 1 example.
- **No over-engineering**: Simple solutions first. Add complexity only when explicitly needed.

## Language & Toolchain

- **ly2k**: Clojure-like language targeting JS via `ly2k compile`
- Source files use `.clj` extension but are NOT standard Clojure
- Build generates JS to `.wrangler/bin/` (gitignored)
- Deploys to Cloudflare Workers via wrangler

## Commands

```bash
make build    # Compile ly2k -> JS
make test     # Build + run tests (node --test .wrangler/bin/test/main_test.js)
make run      # Build + wrangler dev on :8787
make deploy   # Test + wrangler deploy
```

All commands must run from repo root. The `.wrangler/` dir is the wrangler project root.

## Structure

```
src/           # ly2k source
test/          # Tests run via node --test
.wrangler/     # Wrangler config, compiled output
build.clj      # ly2k build config (deps, targets)
```

## ly2k Quirks

- JS interop: `request.url`, `js/URL.`, `Object/fromEntries`
- Hiccup for HTML: `[:tag {:attr "val"} content]` via `xml/to-string`
- `export-default` for Worker exports
- Tests execute as scripts (no test framework)
- use (def- ...) for private fields
