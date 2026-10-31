# Src Hierarchy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans or superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Сделать принадлежность исходников веб-форме, публикации и общей интеграции Telegram видимой в дереве каталогов.

**Architecture:** Сохранить точки входа в корне, переместить представления в `web/`, сценарий публикации и его зависимости в `publication/`, Telegram в `vendor/`. Меняются только пути, namespace и импорты.

**Tech Stack:** ly2k → JavaScript ES modules, make 0.6.0, Node test runner, Cloudflare Worker.

**Spec:** `docs/superpowers/specs/2026-10-13-src-hierarchy-design.md`

## Global Constraints

- Работать в основном worktree на main. Не создавать коммиты без просьбы пользователя.
- Пути `.wrangler/bin/src/main.js` и `.wrangler/bin/src/publish.js` сохраняются.
- Имена функций, параметры, поведение, журнал, ошибки и параметры AI сохраняются.
- Новые зависимости и переадресующие модули не вводятся.
- Worker не получает Node-зависимости автопубликации.
- Существующая раскладка тестов сохраняется; после изменений выполнить `make test`.

## Review Focus

1. Остаточные JS-файлы могут маскировать неверные импорты: проверить чистой сборкой.
2. Runtime должен разрешаться из вложенных каталогов: проверить сгенерированные импорты и тесты.
3. Worker должен собираться с перемещёнными views и Telegram: проверить существующим Wrangler-тестом.
4. Дочерние процессы тестов должны находить модули AI: сохранить пути тестовых точек входа и выполнить существующие проверки таймаута.
5. Локальная точка входа должна находить новые модули, не отправляя реальные сообщения: проверить её сгенерированные импорты без запуска публикации.

---

### Task 1: Переместить модули и проверить обе точки входа

**Files:**

| From | To | Namespace |
| --- | --- | --- |
| `src/views.clj` | `src/web/views.clj` | `web.views` |
| `src/telegram.clj` | `src/vendor/telegram.clj` | `vendor.telegram` |
| `src/auto_publish.clj` | `src/publication/workflow.clj` | `publication.workflow` |
| `src/sava_centar.clj` | `src/publication/sava_centar.clj` | `publication.sava-centar` |
| `src/event_time.clj` | `src/publication/event_time.clj` | `publication.event-time` |
| `src/event_analysis.clj` | `src/publication/analysis.clj` | `publication.analysis` |
| `src/ai_process.clj` | `src/publication/ai_process.clj` | `publication.ai-process` |

Modify: `src/main.clj`, `src/publish.clj`, `test/auto_publish_test.clj`, namespace/import declarations in moved files.

Test: existing `test/main_test.clj`, `test/auto_publish_test.clj`.

**Interfaces:**
- Consumes and preserves `handle-fetch [request env ctx]`, `run-publication [directory config io]`, `send-message [config chat_id text]`, `analyze [event now]` and the existing public functions used by tests.
- Produces the same interfaces under the namespaces in the table; existing aliases remain unchanged.

- [x] **Step 1: Establish baseline.** Read the spec and current `git status --short`, then run `make test`. Stop on a baseline failure and investigate before moving files.

- [x] **Step 2: Move the seven files.** Use patch move operations and replace each `ns` with the namespace in the table. Preserve function bodies.

- [x] **Step 3: Update symbolic requires.** Apply these exact replacements throughout `src/` and `test/`:

```clojure
; old                              ; new
[views :as views]                   [web.views :as views]
[telegram :as tg]                   [vendor.telegram :as tg]
[auto-publish :as pub]              [publication.workflow :as pub]
[sava-centar :as sava]              [publication.sava-centar :as sava]
[event-time :as time]               [publication.event-time :as time]
[event-analysis :as analysis]       [publication.analysis :as analysis]
[ai-process :as ai]                 [publication.ai-process :as ai]
```

The paths `test/auto_publish_test.js` used by subprocess checks remain valid because test files are not moved. `test/entrypoint.clj` continues importing `main`.

- [x] **Step 4: Verify clean compilation and behavior.** Run `make clean`, then `make test` from the repository root. Expect exit code 0 and all existing tests passing, including Wrangler and AI subprocess checks. The inspected make 0.6.0 generator already searches recursively, preserves source directories, and creates output directories; no build configuration change is expected.

- [x] **Step 5: Inspect generated imports.** Read `.wrangler/bin/src/main.js`, `.wrangler/bin/src/publish.js`, and imports in generated `web/`, `vendor/`, `publication/` files. Confirm that relative paths resolve to existing files and runtime imports point to `.wrangler/bin/src/language_runtime.js`. Confirm the analogous runtime placement under `.wrangler/bin/test/`. Do not execute the production publication entrypoint.

- [x] **Step 6: Review the final diff.** Search `src/` and `test/` for old symbolic requires from Step 3; expect none. Check the Worker dependency graph (`main`, `web.views`, `vendor.telegram`, `effect`, `xml`) for accidental Node imports. Run `git diff --check` and review changed and untracked files with rename-aware diff and direct reads. Only paths, namespaces and requires should change; function bodies and fixtures must be preserved.

- [x] **Step 7: Report results.** Summarize the resulting structure and actual test output. Leave changes uncommitted for the user.

## Результат выполнения

Семь файлов перенесены, namespace и импорты обновлены. Конфигурация сборки
не потребовала изменений. Исходный `make test`: 3/3. После `make clean`
сборка и три проверки прошли, но процесс Wrangler не завершился до таймаута
120 секунд. Повторный `make test` без правок: exit 0, 3/3, около 11,5 секунды;
зависание не воспроизвелось, его первопричина не установлена.
Независимый review подтвердил неизменность тел функций, корректность импортов
в обоих деревьях сборки и отсутствие Node-зависимостей Worker; замечаний нет.
`git diff --check` прошёл. Изменения оставлены без коммита.
