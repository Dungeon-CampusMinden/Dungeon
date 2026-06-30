# 04 - Local Prior Work: Dungeon Evaluation Thesis

Scope note: This file documents reusable prior work for future evaluation
phases. The current Wizard V0 does not generate evaluation artifacts.

## Schumann Thesis - Automated Dungeon Evaluation

- Local source: Schumann master thesis PDF, available in the author's private
  thesis archive.
- Priority: local prior work.
- Use: Prior automated evaluation system for Dungeon, originally focused on
  Future Skills.
- Wizard relevance: The evaluation logic transfers well to educational escape
  rooms if the original `Skill-Mechanic-Mapping` is generalized into a
  `LearningGoal-Riddle-Mechanic-Evidence-Mapping`.

## Extracted Evaluation Logic

The thesis does not treat telemetry as direct proof of competence. It first
defines mappings from skill targets to mechanics, expected behavior, and
measurable parameters, then interprets telemetry together with surveys and
qualitative/contextual data.

Relevant sections from the thesis explorer pass:

- pp. 59-62: skill-mechanic mapping; telemetry measures behavior, not
  competence.
- pp. 63-69: two-track evaluation concept with telemetry plus pre/post survey.
- pp. 64-66 and 94-97: xAPI-like event model with actor/player, timestamp, verb,
  object, result, and context; event data can be stored relationally with JSON
  fields and joined to survey data by session and pseudonym.
- pp. 67-68: pre-survey and post-survey structure, including background,
  self-assessment, perceived skill use, usability, presence/IPQ, and free text.
- pp. 114-115: derived telemetry indicators on player and team level, including
  hints, errors, solutions, time, balance, and frustration-like metrics.
- pp. 133-135 and 142-143: limitations and future work; no causality claim,
  small samples, indirect proxies, need for richer event semantics,
  debriefing/communication analysis, and clearer puzzle contexts.

## Transfer to the Wizard

The wizard should generate a traceability and evaluation package for every room:

```text
learning goal
-> riddle / story beat / mechanic
-> expected player behavior
-> telemetry event(s)
-> debrief prompt
-> optional survey or knowledge item
-> success/quality criterion
```

Candidate package artifacts:

- `evaluation/traceability.json`
- `evaluation/traceability.md`
- `evaluation/telemetry-profile.json`
- `evaluation/debriefing-guide.md`
- `evaluation/survey-pre.json`
- `evaluation/survey-post.json`
- `evaluation/validation-report.md`

Debriefing should not be optional decoration. Telemetry can say what happened;
debriefing helps educators and learners explain what it meant.
