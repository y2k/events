# Проба процесса OpenCode: GPT-6.1 Sol

## Условия

- OpenCode v2.0.27, `/opt/homebrew/bin/opencode`.
- Один разрешённый вызов модели `openai/gpt-6.1-sol`, без явного варианта.
- `run --standalone --model openai/gpt-6.1-sol --agent event-probe --format json --title "Synthetic event process contract probe" --file <event.json> <prompt>`.
- Агент задан временным `opencode.json`: primary, system prompt для классификации, последняя permission rule `{ "action": "*", "resource": "*", "effect": "deny" }`.
- Рабочая папка: `/private/var/folders/vy/q595y6l936gggt042s884ww00000gn/T/opencode/event-process-probe.FoRVXA`. Там сохранены конфигурация, вход, stdout, stderr и метаданные. Глобальная конфигурация не менялась.
- stdin закрыт; stdout и stderr направлены в разные файлы. Вызывающий Python-процесс ждал до 120 секунд; при таймауте предусмотрены SIGTERM группе процесса, затем SIGKILL через 5 секунд. Этот путь не сработал и не проверен.
- В prompt переданы согласованные правила фильтра и требование вернуть один JSON без Markdown; источники трактуются как данные. Никакой отправки в Telegram.

## Синтетический вход

`synthetic:instrumental-001`: Synthetic Piano Evening, полностью инструментальный фортепианный концерт без вокала. Sava Centar, Blue Hall, Belgrade; 20 ноября 2026, 20:00. Фиксированное время сравнения — 13 октября 2026, 10:00, Europe/Belgrade. Ссылка `https://example.invalid/events/synthetic-piano-evening`, билеты не заданы. Prompt требует маркировать текст как синтетический.

## Наблюдаемый результат

- Код завершения: 0; время: 4,37 секунды; таймаут: нет.
- stdout: 1113 байт; stderr: 0 байт.
- [Сохранённый stdout](opencode-probe.ndjson): две NDJSON-записи, `step_start` и `text`.
- Вторая запись содержит `part.text` — строку с JSON результата, а также `part.time.start/end`. Отдельного завершающего события в этой пробе нет.
- JSON результата: `event_id`, `decision`, `reason`, `text_ru`. Решение `publish` ожидаемо для инструментального концерта; текст на русском содержит исходные название, место, дату, время и ссылку.

## Границы доказательства

Подтверждён один успешный вызов с файловым входом, запрещёнными инструментами и этой моделью. Поток не содержит событий вызова инструментов. Это пример формата установленной версии, не гарантия схемы всех будущих ответов.

Не проверены сетевые/провайдерские ошибки, отказы разрешений, malformed JSON, принудительный таймаут, очистка приватного сервера после отмены и прекращение удалённого вычисления. Дополнительных вызовов модели не делалось. Эти ограничения нужно учесть в решении о надёжности и проверках реализации; таймаут нельзя описывать как гарантию прекращения биллинга.

Минимальный кандидат контракта для успешного пути: нормальный выход процесса, один завершённый текстовый результат, затем отдельный разбор и проверка JSON приложения. Любое неоднозначное/ошибочное состояние не даёт права на публикацию. Формальный контракт ошибок остаётся предметом решения о надёжности.

## Повторяемая проверка сохранённого ответа

Из корня репозитория, без вызова модели:

```sh
python3 - <<'PY'
import json
from pathlib import Path
p = Path('.scratch/auto-event-publication/assets/opencode-probe.ndjson')
events = [json.loads(line) for line in p.read_text().splitlines()]
assert [e['type'] for e in events] == ['step_start', 'text']
part = events[1]['part']
assert part['time']['end'] >= part['time']['start']
result = json.loads(part['text'])
assert set(result) == {'event_id', 'decision', 'reason', 'text_ru'}
assert result['event_id'] == 'synthetic:instrumental-001'
assert result['decision'] == 'publish' and result['reason']
for value in ['Synthetic Piano Evening', 'Белград', 'Sava Centar',
              '20 ноября 2026', '20:00',
              'https://example.invalid/events/synthetic-piano-evening']:
    assert value in result['text_ru']
print('PASS: saved successful response matches the synthetic event')
PY
```
