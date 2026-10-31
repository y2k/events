# Источники новостей

- https://afishamira.com/?_city=belgrade
- https://belgrad-consult.com/afisha-belgrada/

## Автопубликация Sava Centar (MVP)

Локальный скрипт на ly2k → Node.js 26 читает афишу, отбирает события через
OpenCode (`openai/gpt-6.1-sol`) и отправляет русские текстовые посты в Telegram.
Нужны ly2k с `LY2K_PACKAGES_DIR`, Node.js 26 и авторизованный OpenCode в PATH.
Для существующих Worker-тестов: `npm install --no-save --package-lock=false wrangler`.

```sh
make test
make build
```

Сохраните секреты локально в `~/.config/events/telegram.env` (права `0600`):

```dotenv
TELEGRAM_BOT_TOKEN=ваш_токен
TELEGRAM_CHAT_ID=ваш_канал
```

Бот должен иметь право публиковать в канале. Ручной запуск из корня проекта:

```sh
mkdir -p "$HOME/.local/state/events"
node --env-file="$HOME/.config/events/telegram.env" .wrangler/bin/src/publish.js >> "$HOME/.local/state/events/publish.log" 2>&1
```

Если переменные уже экспортированы, можно использовать `make publish`.
Первый запуск публикует **все** подходящие предстоящие события, ближайшие первыми,
с паузой 3 секунды. AI работает без инструментов, максимум 120 секунд на событие.
После таймаута прекращается локальная группа процессов; прекращение удалённого
вычисления провайдера не гарантируется.

### Состояние и логи

- `~/.local/state/events/events/5657.json` — одна попытка для ID, исходная ссылка,
  статус и причина; для публикации также текст и ID сообщения.
- Файл создаётся и синхронизируется **до** обработки. Любой существующий файл,
  даже пустой после аварии, запрещает повтор. Итог заменяется атомарно.
- Статусы: `started`, `published`, `skipped`, `error`, `unknown`.
  `started` после аварии и `unknown` после потери ответа Telegram требуют
  ручной проверки канала; автоматического повтора нет.
- Не удаляйте файлы состояния при обычном повторном запуске.
- **Запускайте только один экземпляр.** Блокировки параллельных запусков вне MVP.
- `publish.log` содержит JSON-строки: время, `event_id`, действие, причина.
  stdout/stderr сохраняются перенаправлением выше или launchd; секреты не пишутся.
  Ротации нет.

```sh
grep '"event_id":"5657"' ~/.local/state/events/publish.log
```

### Ежедневный запуск на macOS

Создайте `~/Library/LaunchAgents/local.events.publish.plist` с правами `0600`.
В примере пути соответствуют `/Users/igor/project/events` и Homebrew на Apple Silicon;
при другом расположении замените их **абсолютными** путями (`~` и `$HOME` внутри plist
не раскрываются). PATH должен содержать `opencode` и Node.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0"><dict>
  <key>Label</key><string>local.events.publish</string>
  <key>ProgramArguments</key><array>
    <string>/opt/homebrew/bin/node</string>
    <string>--env-file=/Users/igor/.config/events/telegram.env</string>
    <string>/Users/igor/project/events/.wrangler/bin/src/publish.js</string>
  </array>
  <key>WorkingDirectory</key><string>/Users/igor/project/events</string>
  <key>EnvironmentVariables</key><dict>
    <key>PATH</key><string>/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin</string>
  </dict>
  <key>StartCalendarInterval</key><dict>
    <key>Hour</key><integer>10</integer>
    <key>Minute</key><integer>0</integer>
  </dict>
  <key>StandardOutPath</key><string>/Users/igor/.local/state/events/publish.log</string>
  <key>StandardErrorPath</key><string>/Users/igor/.local/state/events/publish.log</string>
</dict></plist>
```

После сборки и создания папки логов:

```sh
plutil -lint "$HOME/Library/LaunchAgents/local.events.publish.plist"
launchctl bootstrap "gui/$(id -u)" "$HOME/Library/LaunchAgents/local.events.publish.plist"
```

Запуск ежедневно в **10:00 по времени компьютера**; пропущенные во сне срабатывания
объединяются после пробуждения. После полного выключения — следующее срабатывание
или ручной запуск. Даты самих событий оцениваются в `Europe/Belgrade`.
После изменений исходников повторите `make build`.
