---
name: telegram-event-description
description: Generate short Russian Telegram descriptions for public event pages in Serbia without inventing missing details.
user-invocable: true
---

# Telegram Event Description

## Purpose

Generate short Russian descriptions for a Telegram channel about public events in Serbia.

## Input

- Event URL
- Optional tone: neutral, lively, concise
- Optional length: short or standard

## Output Requirements

- Write in Russian
- Keep the text suitable for a Telegram channel
- Focus on important facts only
- Do not invent details that are not present on the source page
- If some event details are missing, omit them instead of guessing
- After generating the final text, immediately copy it to the system clipboard

## Fields To Extract

- Event title
- Venue
- City
- Date
- Time if present
- Ticket link if present, otherwise use the source URL

## Style

- Clear, compact, readable
- No hype if the source does not support it
- Mention why the event may be interesting in 1 sentence only if the source supports it
- Use simple formatting suitable for Telegram

## Template

```text
{TITLE} в {CITY}

{DATE}{OPTIONAL_TIME} в {VENUE}, {CITY} пройдет {TITLE} — {SHORT_DESCRIPTION}.{OPTIONAL_INTEREST_LINE}

📍 Место: {VENUE}, {CITY}
📅 Дата: {DATE}
{OPTIONAL_TIME_LINE}
🎟 Билеты: {TICKET_URL}
```

Where:

- `{OPTIONAL_TIME}` is empty or ` в HH:MM`
- `{OPTIONAL_INTEREST_LINE}` is empty or ` <one supported sentence about why it may be interesting>`
- `{OPTIONAL_TIME_LINE}` is omitted if time is missing

## Example

Source URL: `https://tickets.rs/event/rammstein_symphonic_experience_25729`

```text
Rammstein Symphonic Experience в Белграде

27 мая 2026, среда в 20:00 в Sava Centar - Plava dvorana, Белград пройдет Rammstein Symphonic Experience — симфоническое шоу с оркестровым звучанием хитов Rammstein. Необычный формат может быть интересен тем, кто хочет услышать знакомый материал в оркестровой подаче.

📍 Место: Sava Centar - Plava dvorana, Белград
📅 Дата: 27 мая 2026, среда
🕗 Время: 20:00
🎟 Билеты: https://tickets.rs/event/rammstein_symphonic_experience_25729
```

## Notes

- For `tickets.rs`, event fields are often available directly on the event page.
- Preserve the official event title in Latin script if that is how it appears on the source page.
- Use Russian month names and natural Russian sentence structure.
- If time, venue, or ticket details are missing, omit the corresponding fragment instead of inventing it.
- After producing the description, copy exactly that final text to the clipboard without waiting for a separate user request.
