# AGENTS.md

## Overview

Cloudflare Worker written in ly2k (Clojure dialect compiling to JS). Event recommendation form app.

## Language & Toolchain

- **ly2k**: Clojure-like language targeting JS via `ly2k compile`
- Source files use `.clj` extension but are NOT standard Clojure
- Build generates JS to `.wrangler/bin/` (gitignored)
- Deploys to Cloudflare Workers via wrangler

## Commands

```bash
make build    # Compile ly2k -> JS
make test     # Build + run tests (node .wrangler/bin/test/main_test.js)
make run      # Build + wrangler dev on :8787
make deploy   # Test + wrangler deploy
```

All commands must run from repo root. The `.wrangler/` dir is the wrangler project root.

## Structure

```
src/main.clj   # Worker entrypoint (export-default {:fetch ...})
src/views.clj  # Hiccup-style HTML templates
src/db.clj     # D1 database helpers (not yet wired)
test/          # Tests run directly via node
.wrangler/     # Wrangler config, compiled output
build.clj      # ly2k build config (deps, targets)
```

## ly2k Quirks

- JS interop: `request.url`, `js/URL.`, `Object/fromEntries`
- Hiccup for HTML: `[:tag {:attr "val"} content]` via `xml/to-string`
- `export-default` for Worker exports
- Tests execute as scripts (no test framework)
- use (def- ...) for private fields
