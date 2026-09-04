# OpenAI API для извлечения и описания событий

Исследование для [issue #3](https://github.com/y2k/events/issues/3), состояние официальной документации на 2026-09-04.

## Короткий ответ

Для этого сценария подходит **Responses API** с **Structured Outputs** (`text.format.type = "json_schema"`, `strict: true`). Но Structured Outputs гарантирует только форму JSON, а не истинность значений: OpenAI прямо предупреждает, что структурированный ответ всё ещё может содержать ошибки и даже домыслы, если вход не подходит схеме. Поэтому модель нельзя делать источником истины или давать ей право публикации; она должна вернуть проверяемые утверждения и ссылки на заранее сохранённые фрагменты исходной страницы, а приложение — отклонить всё без подтверждения. [Structured Outputs](https://developers.openai.com/api/docs/guides/structured-outputs)

Рекомендуемый стартовый вариант:

- извлечение: закреплённый snapshot `gpt-5.4-nano-2026-03-17` с `reasoning.effort: "none"` — модель прямо предназначена в том числе для data extraction;
- русская редактура только подтверждённой записи: `gpt-5.4-mini-2026-03-17` с `reasoning.effort: "none"`;
- перед выбором рабочих моделей построить потолок качества на `gpt-6-astra`, затем оставить самые дешёвые модели, достигающие требуемой точности. Именно порядок «сначала accuracy, потом cost/latency» рекомендует OpenAI. [GPT-5.4 nano](https://developers.openai.com/api/docs/models/gpt-5.4-nano), [GPT-5.4 Mini](https://developers.openai.com/api/docs/models/gpt-5.4-mini), [Model selection](https://developers.openai.com/api/docs/guides/model-selection)

Это гипотеза для проверки на корпусе реальных страниц, а не утверждение о достаточном качестве этих двух моделей. Актуальный каталог также предлагает `gpt-5.6-luna` для дешёвых массовых задач и `gpt-5.6-terra` как баланс качества и цены; все актуальные модели заявлены как multilingual. Их стоит включить в сравнение, но не менять модель в продакшене без регрессионного прогона. [Models](https://developers.openai.com/api/docs/models), [GPT-5.6 Luna](https://developers.openai.com/api/docs/models/gpt-5.6-luna), [GPT-5.6 Terra](https://developers.openai.com/api/docs/models/gpt-5.6-terra)

## Подходящий интерфейс API

OpenAI рекомендует Responses API для новых text-generation приложений. Для извлечения нужен не function calling, а `text.format`: function calling предназначен для вызова функций приложения, `text.format` — для структурирования самого ответа модели. [Text generation](https://developers.openai.com/api/docs/guides/text), [Structured Outputs](https://developers.openai.com/api/docs/guides/structured-outputs)

В схеме извлечения нужны, как минимум:

```text
status: confirmed | insufficient | conflicting
facts:
  title, start_at_local, venue_name, address, price, description_facts
  для каждого: value (string | null), evidence[] { source_block_id, quote }
warnings[]
```

Практические требования Structured Outputs:

- корень схемы должен быть `object`;
- все поля должны быть перечислены в `required`; отсутствие значения моделируется типом вроде `["string", "null"]`;
- у каждого объекта нужен `additionalProperties: false`;
- поддерживается только подмножество JSON Schema; среди лимитов — не более 10 уровней вложенности и 5000 свойств;
- первый запрос с новой схемой получает дополнительную задержку обработки, повторные запросы с той же схемой — нет;
- приложение обязано отдельно обработать `refusal` и `status: "incomplete"` (например, при исчерпании `max_output_tokens`).

Источник: [Structured Outputs — supported schemas and edge cases](https://developers.openai.com/api/docs/guides/structured-outputs).

Схема должна разрешать `null` и явный `insufficient`: иначе требование всегда заполнить поле подталкивает модель выдумать значение. Не следует использовать самооценку `confidence` как разрешение на публикацию — она не является проверенным вероятностным сертификатом.

## Grounding и доказательства

### 1. Сначала зафиксировать источник

Source adapter сам загружает только одобренный URL и сохраняет URL, время получения, hash и нормализованный текст. Текст делится на небольшие стабильные блоки (`source_block_id`) с читаемым контекстом. OpenAI рекомендует именно стабильные идентификаторы и block-level citations как разумный баланс точности и простоты; цитаты затем следует парсить и проверять приложением. [Citation formatting](https://developers.openai.com/api/docs/guides/citation-formatting)

Страница передаётся как недоверенные данные после developer-инструкции: использовать только эти блоки, игнорировать инструкции внутри страницы, вернуть `null`/`insufficient`, если поддержки нет. Это снижает, но не устраняет prompt injection; OpenAI рекомендует adversarial testing и ограничение входа доверенными материалами. [Safety best practices](https://developers.openai.com/api/docs/guides/safety-best-practices)

### 2. Проверить извлечение кодом

После ответа приложение должно:

1. убедиться, что каждый `source_block_id` существует, а `quote` действительно встречается в сохранённом блоке;
2. независимо распарсить и нормализовать дату/время и применить `Europe/Belgrade` только по явно заданному правилу;
3. проверить обязательные условия карты: подтверждённая дата, физическое место в Белграде и публичный source URL;
4. отклонить событие при конфликтующих датах/местах, отмене, неподдержанной нормализации, `refusal`, `incomplete` или отсутствии evidence;
5. хранить evidence вместе с канонической записью, чтобы последующее изменение страницы можно было сравнить с опубликованным событием.

Structured Outputs не заменяет эти проверки: официальный guide отдельно говорит, что схема может быть соблюдена при ошибочных значениях. [Structured Outputs — handling mistakes](https://developers.openai.com/api/docs/guides/structured-outputs#handling-mistakes)

### 3. Отделить русский текст от извлечения

Второй запрос получает **только** уже проверенную каноническую запись и идентификаторы разрешённых фактов, а не исходную страницу. Он возвращает короткие русские фрагменты и перечень использованных fact IDs. Дата, место, цена и ссылка лучше вставляются детерминированным renderer-ом; модель пишет только заголовок/краткое описание из подтверждённых description facts. Если произвольный красивый текст не нужен, полностью шаблонный renderer безопаснее — OpenAI также советует не использовать LLM там, где выход жёстко ограничен. [Latency optimization — don't default to an LLM](https://developers.openai.com/api/docs/guides/latency-optimization#dont-default-to-an-llm)

Автоматическая проверка вторым LLM может отсеять часть ошибок, но не превращает текст в доказанный факт. При любом сомнении результат не публикуется. Для русской редакции нужны отдельные примеры и bilingual review в regression-наборе; заявленная multilingual capability не гарантирует нужный тон или буквальную верность источнику. [Models](https://developers.openai.com/api/docs/models), [Prompt engineering](https://developers.openai.com/api/docs/guides/prompt-engineering)

### 4. Web search — только дополнительный канал

Встроенный `web_search` в Responses API умеет live search, `allowed_domains`, полный список просмотренных URL и URL citations. Он полезен для discovery, но для подтверждения уже известной страницы прямое получение Source adapter-ом дешевле и лучше воспроизводится. Если search обязателен, `tool_choice: "auto"` недостаточно: документация требует `required` или конкретный tool choice. [Web search](https://developers.openai.com/api/docs/guides/tools-web-search)

## Ограничения

- **Нет гарантии factuality или entailment.** Строгая схема гарантирует ключи и типы, не то, что дата или русская перефразировка поддерживается источником. [Structured Outputs](https://developers.openai.com/api/docs/guides/structured-outputs)
- **Недетерминизм и обновления моделей.** OpenAI рекомендует закреплять snapshots и проверять изменения eval-набором. [Text generation — prompt engineering](https://developers.openai.com/api/docs/guides/text#prompt-engineering)
- **Контекст может ухудшать качество.** Лишний HTML и длинные страницы добавляют стоимость и шум; официальный accuracy guide предупреждает о retrieval noise и проблеме «lost in the middle». [Optimizing LLM accuracy](https://developers.openai.com/api/docs/guides/optimizing-llm-accuracy)
- **Изображения допустимы, но сложнее проверяются.** Предлагаемые модели принимают text и image, однако дата/место, найденные только на афише-картинке, не имеют проверяемой текстовой цитаты. Для первого релиза разумно отправлять такие случаи оператору, а не публиковать автоматически. [GPT-5.4 nano](https://developers.openai.com/api/docs/models/gpt-5.4-nano)
- **Хранение данных.** API-данные по умолчанию не используются для обучения без opt-in, но abuse-monitoring logs могут хранить customer content до 30 дней. Для stateless вызовов следует явно рассмотреть `store: false`. [Data controls](https://developers.openai.com/api/docs/guides/your-data)
- **Не строить долгоживущую систему вокруг Evals API.** По текущей документации Evals platform станет read-only 2026-10-31 и закроется 2026-11-30; regression fixtures и проверки лучше хранить в репозитории. [Working with evals](https://developers.openai.com/api/docs/guides/evals)

## Стоимость

Текущая Standard-цена за 1 млн токенов:

| Модель | Input | Cached input | Output |
|---|---:|---:|---:|
| `gpt-5.4-nano` | $0.20 | $0.02 | $1.25 |
| `gpt-5.4-mini` | $0.75 | $0.075 | $4.50 |
| `gpt-5.6-luna` | $0.20 | $0.02 | $1.20 |
| `gpt-5.6-terra` | $2.00 | $0.20 | $12.00 |
| `gpt-6-astra` | $10.00 | $1.00 | $50.00 |

Источник цен: [OpenAI API pricing](https://developers.openai.com/api/docs/pricing). Reasoning tokens невидимы, но тарифицируются как output; поэтому для короткой extraction-задачи стоит начинать с `reasoning.effort: "none"` и повышать только если eval показывает выигрыш. [Reasoning models — controlling costs](https://developers.openai.com/api/docs/guides/reasoning#controlling-costs)

Формула месячной оценки:

```text
C × (extract_input_tokens × extract_input_rate + extract_output_tokens × extract_output_rate) / 1_000_000
+ P × (copy_input_tokens × copy_input_rate + copy_output_tokens × copy_output_rate) / 1_000_000
+ web_search_calls × $0.01
+ retries/escalations
```

где `C` — число кандидатов, а `P` — число прошедших строгую проверку кандидатов.

Иллюстрация, не замер: extraction на nano = 3000 input + 500 output (`$0.001225` за кандидата); copy на mini = 1000 input + 300 output (`$0.00210` за принятый кандидат). При 1000 кандидатах и 100 принятых это около **$1.44**, без retries и web search. Если делать copy для всех 1000 — около **$3.33**. Один `web_search` стоит ещё **$0.01** плюс search-content tokens, то есть 1000 поисков добавят минимум $10 и будут дороже модельного извлечения. [Pricing — tools](https://developers.openai.com/api/docs/pricing#tools)

Batch API снижает token cost на 50%, имеет отдельные rate limits и срок выполнения до 24 часов. Он подходит для eval/backfill; для ежедневного рабочего цикла нужно отдельно решить, допустима ли такая задержка относительно правила 48 часов. [Batch API](https://developers.openai.com/api/docs/guides/batch)

Бюджет следует считать по фактическим `response.usage`: длина очищенной страницы, developer prompt и schema, output и reasoning tokens, доля cache hits, pass rate, retries, число проверочных вызовов и web searches. Prompt caching включён автоматически на поддерживаемых моделях, но для моделей до GPT-5.6 обычный минимальный cacheable prefix — 2048 visible tokens, поэтому короткий общий prompt может не дать экономии. [Prompt caching](https://developers.openai.com/api/docs/guides/prompt-caching)

## Что проверить перед спецификацией реализации

Собрать локальный размеченный набор реальных страниц на сербском, русском и английском: обычное событие, несколько сеансов, перенос, отмена, противоречащие блоки, отсутствующее место, относительная дата, цена-диапазон и image-only афиша. Сначала сравнить `gpt-6-astra`, `gpt-5.6-luna`, `gpt-5.6-terra`, `gpt-5.4-nano` и `gpt-5.4-mini`, затем закрепить самый дешёвый snapshot, который проходит порог.

При приоритете «лучше пропустить, чем ошибиться» главные метрики — не общий accuracy, а:

- 0 опубликованных обязательных полей без валидного evidence block;
- 0 неверных дат/мест среди автоматически пропущенных событий в acceptance-наборе;
- 0 новых фактических утверждений в русском тексте;
- отдельно измеряемая доля отказов/пропусков, которую можно улучшать только после выполнения первых трёх ограничений.

## Уточнения, которые теперь можно решить отдельно

1. Что считается достаточным evidence для нормализованной даты: один текстовый block, два согласующихся block или структурированное поле самой страницы?
2. Допустима ли автоматическая публикация, если дата и место существуют только внутри изображения, или это всегда Operator review?
3. Какой локальный acceptance-корпус и сколько событий без единой factual error требуются перед включением автономной публикации?
4. Допустим ли 24-часовой Batch SLA для ежедневной обработки при минимальном notice 48 часов, или production-вызовы должны быть синхронными?
