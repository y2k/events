# Event Publishing

This context describes the discovery and publication of public events for a Russian-speaking audience in Belgrade.

## Language

**Event**:
A single scheduled opportunity to attend a publicly announced happening in Belgrade on a specific date or date range and at a specific place. Repeated dates, times, or places are separate Events, but a multi-day happening is always one Event; one Event may be described by multiple Sources.
_Avoid_: News item, recommendation

**Source**:
A human-approved public website from which event information may be discovered.
_Avoid_: Feed, provider

**Source evidence**:
Human-visible content from an approved Source's canonical detail page that explicitly supports a fact about one Event Candidate. It must be current and unambiguously attributable to that Event Candidate; listings, metadata, and images alone are insufficient.
_Avoid_: Confidence, model output

**Event Candidate**:
An Event discovered from a Source that has not yet passed the editorial checks required for publication. One Source page may yield multiple Event Candidates when it lists separately scheduled Events.
_Avoid_: Event, draft

**Recommendation**:
A public link submitted by a person to notify the Operator about a possible Event. It does not enter the Autonomous Event Editor's discovery and publication flow.
_Avoid_: Published Event, manual post

**Published Event**:
An event represented by a Russian-language Telegram post containing a short description, date, place, price when known, and a source link.
_Avoid_: Message, recommendation

**Autonomous Event Editor**:
The system that discovers Event Candidates, checks them, and publishes eligible events without per-event human approval. A human controls its rules and can stop publication.
_Avoid_: It, scraper, bot

**Operator**:
The person who controls the Autonomous Event Editor, manages approved Sources, and responds to its alerts.
_Avoid_: Moderator, administrator
