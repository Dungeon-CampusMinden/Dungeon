# Wizard Research Source Notes

These notes summarize why each source matters for the Dungeon Wizard. Full
citation metadata lives in [`sources.bib`](sources.bib).

The file is deliberately organized by public research theme. It does not track
private/local source-list origin, local paths, or full PDFs.

## Educational Escape-Room Design and Learning Context

These sources support the argument that the Wizard should guide authors through
participants, objectives, theme, puzzle structure, constraints, briefing, and
later debriefing/evaluation without making all of that mandatory for V0.

| BibTeX key | Priority | Wizard relevance |
| --- | --- | --- |
| `biggs1996constructive` | background | Didactic basis for aligning objectives, activities, and assessment evidence. Useful for future learning-goal mapping. |
| `clarke2017escaped` | core | Direct framework for creating educational escape rooms and interactive games. Supports structured authoring steps. |
| `veldkamp2020escape` | core | Systematic review of educational escape rooms. Useful for puzzle structure, group size, playtime, technology role, and pedagogical alignment. |
| `veldkamp2021beyond` | core/secondary | Teacher-perspective source on adoption barriers, teamwork, and motivation. Supports the Wizard as teacher-facing support. |
| `makri2021digital` | core | Review focused on digital educational escape rooms. Helps separate digital affordances from analog practice. |
| `fotaris2022room2educ8` | core | Design-thinking framework for educational escape-room creation. Useful comparison for learner context, objectives, narrative, prototyping, and playtesting. |
| `guigon2018segam` | core | Model for learning escape games. Particularly relevant to riddle graphs with levels, riddles, clues, and objectives. |
| `botturi2020star` | core | Validated teacher-facing design model. Supports structured workflow, game flow, classroom integration, and debriefing. |
| `sanchez2019debriefing` | secondary | Treats debriefing as the bridge from gameplay to formal learning. Future-facing for reflection concepts. |
| `fanning2007debriefing` | background | Foundational debriefing source from simulation-based learning. Useful when learning is inferred from situated activity. |
| `cheng2014debriefing` | background | Review/meta-analysis on debriefing for technology-enhanced simulation. Later evaluation context. |
| `nicholson2015recipe` | background | Meaningful gamification framework. Helps argue against shallow points/badges and toward meaningful player choice and reflection. |
| `nicholson2018classroom` | background | Classroom escape-room source. Useful for practical classroom adaptation and avoiding shallow "content as locks" designs. |
| `reuter2020strategies` | secondary | Creation and design strategies for educational escape rooms. Useful as checklist coverage for authoring steps. |
| `elmetGuide` | practical/background | Applied design, implementation, facilitation, and evaluation guide. Useful non-peer-reviewed workflow comparison. |
| `lopezpernas2019programming` | secondary | Early higher-education programming escape-room source with concrete design choices. |
| `lathwesen2021stem` | secondary | STEM review with useful criticism of the evidence base. Good for a differentiated research-state section. |
| `kuo2022digitalphysical` | secondary | Digital/physical escape-room study. Useful as a comparison point, not a Wizard core source. |
| `gordillo2024rct` | core | Controlled study comparing educational escape rooms with traditional lectures. Stronger evidence source for learning-effect claims. |
| `kim2024meta` | core/secondary | Meta-analysis on knowledge gain and attitude change. Complements `lopezpernas2024meta`. |
| `grepperud2025framework` | core/future | Framework synthesis for primary and secondary education. Useful if the Wizard scope expands beyond higher education. |
| `rawlinson2024failure` | secondary | Theoretical framing for failure, retry, hints, and reflection as learning opportunities. |

## Authoring, Generation, and Platform Lifecycle

These sources support the Wizard as a config-first, teacher-facing authoring
tool that produces stable artifacts for generation and runtime preview.

| BibTeX key | Priority | Wizard relevance |
| --- | --- | --- |
| `laurent2022authoring` | core | Serious-game authoring principles. Direct support for the power/usability tradeoff and abstraction layers for non-programmers. |
| `ahmad2020instructional` | core | Review of instructional-design support in serious-game authoring tools. Future-facing without forcing learning outcomes into V0. |
| `roungas2016model` | core | Model-driven educational game design. Strong support for a schema/config-first artifact. |
| `shaker2016pcg` | core | Foundational procedural-content-generation reference. Supports seed/config-based generation, constraints, and reproducibility. |
| `liapis2016mixed` | core | Mixed-initiative creation. Frames the generator as collaborator while the author controls meaning. |
| `hunicke2004mda` | background | Mechanics-dynamics-aesthetics framing. Keeps the authoring model linked to runtime behavior and intended player experience. |
| `togelius2011search` | secondary | Search-based PCG taxonomy. Useful later for layout fitness, graph constraints, and generator evaluation. |
| `lai2020friendly` | secondary | Designer-friendly mixed-initiative PCG. Supports human control, explainable suggestions, and later fine-tuning tools. |
| `torres2022moirai` | comparison | No-code serious-game authoring platform. Useful comparison for non-technical authoring. |
| `torres2025moirai` | future evaluation | Usability follow-up for Moirai. Useful if the Wizard UI itself is evaluated. |
| `mehm2016authoring` | secondary | Serious-game authoring processes and tools. Supports the Wizard as a structured process, not just a UI. |
| `mehm2013education` | secondary | Education-focused serious-game authoring. Useful comparison for abstraction over concrete code. |
| `alonsofernandez2017lifecycle` | core/secondary | Lifecycle architecture connecting authoring, runtime, and analytics. Relevant to `deer.zip` and later traceability. |
| `sousa2022adaptability` | generator/future | Connects educational escape rooms with adaptability, accessibility, level design, narrative, and PCG. |
| `lopezpernas2021escapp` | platform comparison | Web platform for running educational escape rooms. Comparison point for preview/runtime, monitoring, hints, and multi-team sessions. |
| `queiros2023lms` | future integration | LMS integration source. Relevant after a stable `deer.zip` and runtime contract exist. |
| `queiros2024gerf` | future integration | Virtual escape-room framework with LTI/xAPI/analytics/adaptive learning paths. Relevant outside V0. |
| `bonnat2022digital` | monitoring/future | Digital companion for monitoring, Game Master support, and debriefing. Useful warning that dashboards must match teaching practice. |
| `lopezpernas2024dashboard` | monitoring/future | Sequence-analysis-inspired dashboard for educational escape-room progress. Context for later monitoring views. |
| `vigneau2023skills` | future skills | Escape-game template and 21st-century skills assessment. Useful if competence/template work becomes central. |
| `abdulrazic2026nocode` | background/future | Recent no-code 3D serious-game authoring comparison. Not escape-room-specific, but useful for related-work boundaries. |

## Evaluation, Analytics, and Research Methodology

These sources are not V0 requirements. They are kept because the Wizard can
later generate traceability, telemetry, debriefing, validation, or study
artifacts alongside playable rooms.

| BibTeX key | Priority | Wizard relevance |
| --- | --- | --- |
| `hevner2004design` | background | Foundational design-science framing. Supports the Wizard as an artifact evaluated through build, demonstration, and evaluation. |
| `peffers2007methodology` | background | Practical design-science methodology. Useful for paper structure: problem, objectives, design/development, demonstration, evaluation, communication. |
| `lopezpernas2024meta` | core | Meta-analysis of educational escape-room learning effectiveness. Strong evidence base for the domain. |
| `veldkamp2022escaped` | core/secondary | Empirical study of learning, immersion, collaboration, and debriefing during gameplay. Supports evaluation dimensions beyond completion. |
| `lopezpernas2023analytics` | analytics/future | Learning-analytics perspective on educational escape rooms. Runtime evaluation context outside V0. |
| `alonsofernandez2022gla` | analytics/future | Serious-game interaction standardization, visualization, and analysis. Useful for later telemetry and traceability. |
| `daoudi2022learning` | analytics/future | Learning analytics for serious-game usability. Helps separate Wizard usability, game quality, pedagogy, context, and data analysis. |
| `zhu2023review` | assessment/future | Broader digital game-based assessment review. Useful if the project later emphasizes assessment evidence. |
| `gomez2022gba` | assessment/background | Broader game-based assessment review. Kept as secondary background. |
| `calvomorata2025articoding` | analytics/background | Current example of learning analytics in a serious-game lifecycle. Useful for development-feedback framing. |

## Reusable Evaluation Pattern

The repo should not depend on private/local documents for its public research
notes. The reusable public pattern is:

```text
learning goal
-> riddle / story beat / mechanic
-> expected player behavior
-> telemetry event(s)
-> debrief prompt
-> optional survey or knowledge item
-> success/quality criterion
```

Important constraints for later versions:

- Telemetry should not be treated as direct proof of competence. It records
  behavior that needs interpretation.
- Useful event models need actor/player, timestamp, verb, object, result, and
  context; relational storage can still keep flexible JSON fields for event
  details.
- Evaluation should separate player/team indicators, survey data, usability,
  presence, perceived learning, and qualitative context.
- Debriefing should not be decorative. It is where players and educators turn
  observed play into interpreted learning.

Candidate future artifact categories:

- traceability overview
- telemetry profile
- debriefing guide
- optional pre/post survey material
- validation report

## Lower-Priority Candidates

These are useful to know about but should not drive the Wizard concept unless a
later paper needs a broader related-work section:

- Profession-specific escape-room case studies without strong evaluation or
  platform relevance.
- General game-based-learning sources with no clear bridge to authoring,
  traceability, evaluation, or escape rooms.
- Highly domain-specific acceptance studies that mainly support usability or
  professional-context discussion.
