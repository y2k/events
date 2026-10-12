# Sava Centar source research — MVP decision

Observed: 2026-10-13. Research for `auto-event-publication/issues/01-source-research.md`.
Primary sources only: venue listing, venue event pages, and their returned HTML.
This is a snapshot, not a guarantee about subsequent site changes.

## Resolution

**Usable for a conservative MVP:** read the full Blue Hall listing and each linked
event's HTML; extract occurrence facts mechanically, then use AI to interpret
description evidence and write short Russian text. Unknown or contradictory
evidence must produce a skip reason. The source does **not** support a promise to
identify every qualifying concert's vocal language. Its public listing can be
enumerated today, but completeness against the venue's entire booking inventory
cannot be established from it.

The existing scope needs no expansion: consider all future qualifying occurrences
on the first run, without an arbitrary date horizon. Do not equate a performer or
production with one occurrence. Two Nutcracker performances on the same date are
two events. [S1, S3, S4]

## 1. Access, extraction and completeness — observed facts

- [S1] returned **50 distinct event links and 50 distinct `e-loop-item-N` IDs**
  in the fetched HTML. The Serbian listing [S2] returned the same 50 IDs in the
  same order. The displayed dates extend from October 2026 to October 2027, with
  one anomalous March 2026 entry. This is not a count of validated future events.
- Cards are in `.elementor-loop-container`; each
  `[data-elementor-type="loop-item"]` contains an event anchor, date text,
  category/location text, `h2` title, excerpt and image. For example, Petar Grašo
  has `e-loop-item-5335 post-5335`, `category-koncert category-plava-dvorana`.
  The loop template's `data-elementor-id="353"` is shared and is **not** an
  event ID. [S1 HTML]
- No event-list pagination controls, next-page link, load-more control or
  pagination setting appeared in this HTML. The loop widget is
  `data-id="76ceaa6"`, `data-widget_type="loop-grid.post"`.
  Read-only probes of `/page/2/`, `?e-page-76ceaa6=2`, and `?paged=2` each
  returned HTTP 200 and the **same** 50 IDs, with no additional IDs. The last
  redirected to `/en/dogadjaji-u-plavoj-dvorani/page/2/`. These URLs are probes,
  not discovered navigation or a usable pagination protocol. [S1 HTML and URL variants]
- **Limit of the evidence:** no public total-count or query limit was found.
  Exactly 50 entries could still reflect an undisclosed cap. Agreement with the
  Serbian page corroborates the visible list, not inventory completeness.
  The listing links to an archive, `/en/events-held-in-the-sava-center/`; an
  exhaustive archive/sitemap audit is outside this MVP investigation. [S1]
- Ordinary HTTP retrieval works: `curl -L -A 'Mozilla/5.0'` returned HTTP 200
  and 361,497 bytes for S1; the webfetch tool also returned complete event text.
  A Python `urllib.request.urlopen` using its default user agent received
  HTTP 403 on S1. Thus a particular minimal client was rejected, but browser
  execution is not required for the observed HTML. Do not assume all clients
  or future runs will succeed. The curl response had a LiteSpeed cache comment
  dated 2026-10-12; freshness to the instant of retrieval is not guaranteed.
- The HTML configures GTranslate with default language `sr`, languages
  `en/sr`, and subdirectory URLs. Visible English translations can distort names:
  S1 calls Nikolija “Nicholas” at `/en/events/no-one/`, while its excerpt says
  Nikolija. Preserve source evidence; do not manufacture an artist's name from
  a translated slug. [S1 HTML/text]

**MVP recommendation:** one full listing request, then one request per distinct
linked event needing assessment. Extract the HTML, not only visible anchor text:
in Markdown conversion the card URL can be an empty anchor separated from its
title. Follow actual URLs; do not construct translated slugs. Treat failed or
structurally unexpected retrieval as a source-read failure, not an empty calendar.
No REST API, browser automation, ticket-site crawler or undocumented pagination
is needed for the demonstrated reading path.

## 2. Field availability and date handling

| Field | Evidence and extraction consequence |
| --- | --- |
| Title | Listing `h2`, event `h1`; present on inspected pages. Preserve the event title with its source URL. [S1, S3–S13] |
| Date | Event heading immediately after title; `DD.MM.YYYY.`. Parse the full year and compare to the listing, not the page publication date. [S3–S13] |
| Time | Optional: Nutcracker and Molière show `15: 00` / `20: 00`; normalize whitespace around the colon. Tommy Emmanuel, Meladze and Yasmin Levy headings have date only. Omit unknown time; do not default to 20:00 or midnight. [S3–S8] |
| Venue | Cards explicitly say Blue Hall; event notice names the ceremonial entrance of the Blue Hall, and descriptions identify Sava Center. Footer supplies Milentija Popovića 9, Belgrade. The Vladimira Popovića entrance instruction is not a replacement postal address. [S1, S3–S13] |
| Type | Card category plus narrative. Categories are fallible: Legendary Rock Voices is labeled **Film** on S1, while its detail describes a concert with orchestra, choir and rock band. [S1, S10] |
| Vocal language | No dedicated vocal-language field observed. Sometimes explicitly discussed in prose, often absent. HTML `lang` and JSON-LD `inLanguage` describe the webpage, not the singing. [S6, S8, S11 HTML/text] |
| Tickets | Event-specific `Buy` link is directly in HTML; global Tickets navigation instead opens a popup with vendor homepages. Extract the event button, not the first tickets-related link. [S3, S4, S10] |

**Real conflict:** Parni Valjak's listing and event heading say `17.03.2026.`;
its event description explicitly says the concert was moved from 12 October
2026 to **17 March 2027**, with existing tickets valid. Do not silently fix the
year from ordering or AI knowledge. Under the accepted uncertainty rule, skip
with `conflicting_date` and retain both quotes. This matters even though editing
already-published posts is out of scope. [S1, S9]

The inspected JSON-LD on S3, S11, S12 and S13 is Yoast `WebPage`, image,
breadcrumb, website and organization metadata, not a structured `Event` feed.
`datePublished` / `dateModified` are article dates and `inLanguage: en` is the
document language. No machine-readable event `startDate` was found there.

**Recommendation, not a source fact:** interpret occurrence date/time in
`Europe/Belgrade`; the headings do not declare a timezone. Keep date and optional
time separately. Assess future status using local dates/times, and never infer
an exact start instant from a date-only entry. Retain raw date text for validation.

## 3. Occurrence identity and repeat performances

Observed IDs from S1 HTML:

| Production | Occurrence | WordPress ID | Event path under `https://savacentar.rs/en/events/` |
| --- | --- | --- | --- |
| Molière | 2026-10-30 20:00 | 5059 | `Moliere-2/` |
| Molière | 2026-10-31 15:00 | 5797 | `Moliere-3/` |
| Molière | 2026-10-31 20:00 | 5062 | `moliere/` |
| Nutcracker | 2026-12-20 15:00 | 5793 | `Krk-orascic-2/` |
| Nutcracker | 2026-12-20 20:00 | 5657 | `krcko-orascic/` |

The two Nutcracker detail pages independently confirm the times and have
different ticket IDs: `the_nutcracker_28581` and `the_nutcracker_27347`.
The Molière matinee's prose still says “two evenings”, despite the listing
having three performances; use occurrence headings rather than counting prose
mentions. Bajaga likewise has three distinct listing entries on October 22–24.
[S1, S3, S4, S5]

**Recommended identity:** `savacentar:<WordPress post ID>` for the observed
one-page-per-occurrence model, storing canonical/fetched URL and date/time as
attributes. IDs agree across the English and Serbian listings; S3 also exposes
`rel="shortlink" href="https://savacentar.rs/en/?p=5657"`. Prefer the numeric
ID over translated slugs, title, artist or ticket vendor ID. A canonical event
URL is a fallback if the ID is unavailable; avoid automatically merging an
unmatched fallback with another event.

**Uncertainty:** there is no documented permanence contract for IDs/slugs, nor
proof that every future page will represent exactly one occurrence. Do not add
the mutable date to the primary identity merely to handle rescheduling; that
would turn an edited date into a new publication. If a new page contains several
unresolvable performances, skip rather than guess their identities.

## 4. Genre and vocal-language evidence

AI should return a category, language evidence (or unknown), quoted supporting
text and source URL, then a filter decision/reason. It can interpret the supplied
description and detect contradictions; model familiarity with an artist is not
primary-source evidence of this performance's repertoire.

| Sample | Facts in primary source | Conservative consequence under accepted scope |
| --- | --- | --- |
| Nutcracker, both shows | Listing says Ballet; prose explicitly calls it Tchaikovsky's ballet. [S1, S3, S4] | Clear qualifying ballet; vocal language irrelevant. |
| Tommy Emmanuel | Guitar concert, fingerstyle melody/bass/rhythm, described as an instrumentalist. [S7] | Strong textual basis for AI classification as instrumental; no explicit guarantee that the entire set is vocal-free. Do not invent a singing language. |
| Valery Meladze | Concert, voice, new songs; partly Russian promotional text, but no explicit performance-language statement. [S6] | Russian copy is not proof of Russian vocals. Unknown language from this page alone → skip. |
| Yasmin Levy | Songs and a new interpretation of “Caruso”; no stated vocal language. [S8] | Do not infer Spanish, Ladino or another language from performer identity/song title. Unknown → skip. |
| Perpetuum Jazzile | Explicitly vocal; “sings in different languages: English, Portuguese, Spanish, Italian, German, Croatian, Macedonian, Slovenian and others.” [S11] | Actual language evidence exists, but it is mixed and general repertoire, not this show's set list. Do not label the entire concert EN/ES/DE. If qualification cannot be established under the existing filter, skip. |
| ABBA Symphonic Tribute Show | Tribute band, orchestra **and choir**, ABBA hits; no language statement. [S13] | “Symphonic” does not establish instrumental-only; artist/repertoire familiarity alone does not establish vocal language. |
| Legendary Rock Voices | Choir, rock band, orchestra; many named bands; card says Film. [S1, S10] | Detail corrects likely genre classification, but does not state singing languages; unknown language → skip. |
| Molière | “Original French musical”, categorized as musical. [S1, S5] | Musical is not automatically opera or an accepted concert. Do not expand the agreed scope because “French” appears. |
| From Opera to Hollywood | Gala concert mixing opera arias by Verdi/Puccini/Bizet and film music; no language statement. [S12] | Opera in a title does not establish a staged opera or an entirely operatic program. Preserve mixed evidence; skip if qualification is uncertain. |

No unambiguous standalone opera performance was established in this sample.
These are evidence examples, not an exhaustive eligibility audit of all 50 pages.
The practical limitation is lower recall for vocal concerts, not missing basic
title/date/venue data. Additional artist research is unnecessary for the MVP's
already-accepted “unknown evidence → skip” behavior.

## 5. Ticket links and minimal output contract

Examples of event-specific targets observed on the venue pages:

- S3: <https://tickets.rs/event/the_nutcracker_27347>
- S4: <https://tickets.rs/event/the_nutcracker_28581>
- S5: <https://tickets.rs/event/musical_moliere_28718>
- S6: <https://tickets.rs/event/valery_meladze_26902>
- S10: <https://efinity.rs/event-details/legendary_rock_voices_beograd_32301>

These are verified **link targets in source HTML**, not verification of ticket
inventory, prices, availability or checkout. The venue explicitly says it is not
the organizer and identifies Ticket Vision and Efinity as authorized providers.
If a usable event-specific link is absent, the accepted fallback is the event
page. Do not substitute a vendor homepage or advertise availability from a Buy
button alone. [S3–S10]

Recommended minimal assessment record: source ID, event URL, title, local date,
optional local time, venue, optional ticket URL, description evidence,
classification/language evidence, and accepted/skipped with reason. Keep dates,
identity and links grounded in extracted fields; AI supplies interpretation and
the short Russian description. A post needs only title, venue, date, known time
and ticket/event link. No inferred price, duration, language or availability.

## Primary sources

All accessed 2026-10-13; citations above refer to these live pages and HTML.

- **S1** <https://savacentar.rs/en/events-in-the-blue-hall/>
- **S2** <https://savacentar.rs/dogadjaji-u-plavoj-dvorani/>
- **S3** <https://savacentar.rs/en/events/krcko-orascic/>
- **S4** <https://savacentar.rs/en/events/Krk-orascic-2/>
- **S5** <https://savacentar.rs/en/events/Moliere-3/>
- **S6** <https://savacentar.rs/en/events/Valery-Meladze/>
- **S7** <https://savacentar.rs/en/events/tommy-emmanuel/>
- **S8** <https://savacentar.rs/en/events/yasmin-levy-2026/>
- **S9** <https://savacentar.rs/en/events/steamroller-2/>
- **S10** <https://savacentar.rs/en/events/legendary-rock-voices/>
- **S11** <https://savacentar.rs/en/events/perpetual-jazz/>
- **S12** <https://savacentar.rs/en/events/Tamara-Radjenovic/>
- **S13** <https://savacentar.rs/en/events/ABBA-Symphonic-Tribute-Show/>
