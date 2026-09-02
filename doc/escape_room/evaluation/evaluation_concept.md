# Evaluierungskonzept für Digital Educational Escape Rooms

## Methoden

### Quantitative Wissenstests (Pre-Post-Tests):
- Erfassung des kognitiven Wissenszuwachses vor und unmittelbar nach dem Spiel.
- Differenzierte Designs unterscheiden zwischen reiner Faktenreproduktion (Retention) und kognitiver Anwendung auf neue Problemstellungen (Transfer).
- Ergänzend werden vereinzelt Delayed-Post-Tests zur Langzeitbehaltensleistung eingesetzt.

### Standardisierte psychometrische Fragebögen:
- Intrinsische Motivation: Adaptionen des Intrinsic Motivation Inventory (IMI).
- Engagement: ISA Engagement Scale.
- Kognitive Belastung: NASA-TLX zur Erfassung von mentaler Anforderung, Anstrengung und Frustration.
- Usability & Technikakzeptanz: System Usability Scale (SUS) und User Engagement Questionnaire (UEQ).

### In-Game-Performanzmessung & Checklisten:
Erfassung objektiv messbarer Spielparameter wie:
- benötigten Zeit (Escape Time)
- Anzahl genutzter Hinweise (Hints)
- Fehlversuchen bei Rätseln
- Absolvierung strukturierter Beobachtungs-Checklisten.

### Learning Analytics & Telemetrie (Logfiles):
Serverseitige, automatisierte Aufzeichnung von Klickpfaden, Aktionssequenzen und Zeitverläufen zur verhaltensbasierten Analyse

### Geführte Debriefings & Qualitative Reflexion:
- Nachbereitsgespräche zur Aufarbeitung des Gelernten und zur Erfassung mentaler Modelle
- ergänzt durch qualitative Inhaltsanalysen von Gesprächstranskripten oder Freitextfeldern

### Multivariate Modellierung & Inferenzstatistik:
- Statistische Absicherung durch zweifaktorielle Varianzanalysen (ANOVA)
- Regressionsmodelle und Strukturgleichungsmodelle (SEM) zur Abbildung komplexer Wirkungsnetze zwischen Spielmechanik, Motivation und Lernerfolg.


## geeignete Evaluierungsmethoden:
### Mixed-Methods-Designs mit Fokus auf Post-Game-Debriefing:
Während des Spiels stehen Teilnehmende unter Zeitdruck. Erst das strukturierte Debriefing überführt spielimmanente Erfahrungen in de-kontextualisiertes Fachwissen, deckt tiefsitzene Fehlkonzepte auf und weist höhere kognitive Taxonomiestufen nach Bloom (z. B. Analysieren, Evaluieren) nach.


### Non-invasive Learning Analytics & Telemetrie-Daten:
Befragungen während des Spiels stören die Immersion und den Flow-Zustand. Automatisch aufgezeichnete Logfiles (z. B. Hint-Nutzung, Verweildauern) liefern unterbrechungsfreie, objektive Daten und erklären verhaltensbasierte Ursachen für Unterschiede im Lernerfolg.


### Pre-Post-Tests:
Basiert auf der Cognitive Load Theory und dem Productive Failure-Paradigma. Durch die klare Trennung von Retentions- und Transferaufgaben lässt sich präzise messen, wie das didaktische Timing des DEERs (z. B. vor oder nach einer Lehrveranstaltung) den echten Kompetenzzuwachs beeinflusst.


### Usability (SUS), Kognitive Belastung (NASA-TLX) und Wissenszuwachs:
Erlaubt die saubere Isolation von Fehlerquellen: Ein geringer Wissenszuwachs kann somit eindeutig auf didaktische Mängel (hohe intrinsische Belastung) oder auf technische Usability-Hürden (hohe extrinsische Belastung) zurückgeführt werden.


## Weniger geeignete und problematische Evaluierungsmethoden
### Isolierte Erfolgsquoten (Completion Rates) und reine Spielzeit (Escape Time):
Das schnelle Entkommen oder das Erreichen des Ziels korreliert nicht zwingend mit kognitivem Wissenserwerb. Rätsel können durch unsystematisches Ausprobieren gelöst werden, während zeitintensive, tiefgründige Fachdiskussionen im Team oft zum Überschreiten des Zeitlimits führen.


### Unkontrollierte Ein-Gruppen-Pre-Post-Designs:
Weisen eine geringe interne Validität auf. Gemessene Wissenszuwächse sind ohne Ad-hoc-Kontrollgruppe anfällig für den Testing Effect (Lerneffekt durch das bloße Bearbeiten des Pre-Tests) sowie kurzfristiges Priming.


### Synchrone Dozenten-Interventionen und Fremdbeobachtungen während des Spiels:
Externe Eingriffe während der Spielphase unterbrechen die narrative Immersion und zerstören das Gefühl der Autonomie sowie den Flow-Zustand, was die intrinsische Motivation negativ beeinflusst.


### Unstandardisierte Zufriedenheitsbefragungen ("Fun Factor"):
Unterliegen stark dem Novelty Effect (Neuheitseffekt). Studierende bewerten die spielerische Abwechslung fast immer positiv, was jedoch kaum Aussagekraft über das tatsächliche Erreichen fachlicher Lernziele besitzt.


## Evaluierungskonzepte

| Evaluationsmethode | Evidenz | Stärken | Limitationen | Quellen |
|--------------------|:------:|----------|--------------|------------------------------------|
| **Pre-Post-Wissenstest (differenziert)** | ★★★★★ | Objektive Quantifizierung von Faktenwissen, Retention und Transfer; etablierte Methode zur Messung von Lernerfolg | Anfällig für Testing Effects; misst nicht den Lösungsprozess | Campbell & Stanley (1963); Shadish, Cook & Campbell (2002); Roediger & Karpicke (2006); Veldkamp et al. (2020) |
| **Learning Analytics & Telemetrie** | ★★★★★ | Vollkommen objektiv, echtzeitbasiert, non-invasiv; ermöglicht Analyse des gesamten Lernprozesses | Hoher technischer Aufwand; komplexe Datenbereinigung und Feature Engineering erforderlich | Shute et al. (2009); Loh, Sheng & Ifenthaler (2015); Ifenthaler & Yau (2020) |
| **Geführtes Debriefing & qualitative Analyse** | ★★★★★ | Tiefes Verständnis von Fehlkonzepten; verbindet Lehre und Evaluation; unterstützt Wissenstransfer | Höherer Auswertungsaufwand; Ergebnisse abhängig von Moderation und qualitativer Analyse | Sanchez & Plumettaz-Sieber (2019); Veldkamp et al. (2020); Krueger & Casey (2015) |
| **Psychometrische Fragebögen (SUS, NASA-TLX, IMI)** | ★★★★★ | Valide Messung von Usability, kognitiver Belastung und Motivation; standardisierte Instrumente | Retrospektiver Selbstauskunft-Bias | Brooke (1996); Hart & Staveland (1988); Deci & Ryan (2000) |
| **Strukturgleichungsmodellierung (SEM)** | ★★★★★ | Validierung komplexer ursächlicher Wirkungsmodelle; Analyse latenter Variablen | Benötigt große Stichproben (N ≥ 100-200) und statistische Expertise | Kline (2023); Hair et al. (2022) |
| **Isolierte Spielzeit / Erfolgsquote** | ★★☆☆☆ | Einfach zu erheben; misst Performanz unter Zeitdruck | Keine kausale Korrelation mit echtem Wissenszuwachs; keine Aussage über Lernprozesse | Veldkamp et al. (2020); Shute et al. (2009) |
| **Ein-Gruppen-Design ohne Kontrollgruppe** | ★★☆☆☆ | Geringer organisatorischer Aufwand | Sehr geringe interne Validität; anfällig für Testing-, History- und Reifungseffekte | Campbell & Stanley (1963); Shadish et al. (2002) |
| **Unstandardisierte Zufriedenheitsbefragung** | ★☆☆☆☆ | Erfasst kurzfristige Akzeptanz und Spielspaß | Stark verzerrt durch Novelty Effect; keine Evidenz für Lernerfolg; geringe Vergleichbarkeit | Davis (1989); Venkatesh et al. (2003); Veldkamp et al. (2020) |


## Empfehlung

| Evaluierungsphase | Empfohlene Methoden | Erfasste Konstrukte / Parameter | Operationalisierung |
|-------------------|---------------------|---------------------------------|---------------------|
| **1. Prä-Game-Phase** | Themenbezogener Wissenstest, Demografie | Vorwissen, Retention, Transfer, Vorerfahrung | Online-Fragebogen vor Spielbeginn |
| **2. In-Game-Phase** | Server-Telemetrie, Learning Analytics | Klickpfade, Hint-Nutzung, Verweildauer, Fehlversuche | Hintergrund-Logfile-Aufzeichnung ohne Unterbrechung des Spiels |
| **3. Unmittelbare Post-Phase** | Wissens-Post-Test, NASA-TLX, SUS, IMI, geführtes Debriefing | Wissenszuwachs, kognitive Belastung, Usability, intrinsische Motivation, Reflexion | Kombinierter Fragebogen unmittelbar nach Spielende sowie moderiertes Debriefing |
| **4. Post-Prozess-Phase** | Qualitative Inhaltsanalyse, Delayed-Post-Test | Qualität der Reflexion, Langzeitbehaltensleistung (Retention), Wissenstransfer | Transkription und qualitative Auswertung des Debriefings sowie Wissenstest nach 4-8 Wochen |


## Fazit

- Der wichtigste Punkt: Für gute Evidenz der Ergebnisse werden immer Kontrollgruppen benötigt
- Vor- und Nachher Wissenstests funktionieren nur mit Kontrollgruppen
- Learning Analytics, Beobachtung und Debriefing liefern wichtige Informationen zur Wirksamkeit und Anfälligkeiten des Escape Rooms
- Standatisierte Tests können Rückmeldung über Probleme im Design geben
- Selbsteinschätzungen und Zufriedensheitsbefragung haben geringe Aussagekraft
- erneute spätere Post-Befragung kann gute Aussagen über Langfristigen Wissenserhalt bieten
- Fragebögen mit Wissenstest müssen Themenspezifisch zu jedem Escape Room neu erstellt werden

## Probleme

- Kontrollgruppen sind im Lehrkontext schwierig, da keine Chancengleichheit herrscht
- Kontrollgruppen benötigen eine gewisse Stichprobengröße, damit Effekte wie der


## Sonstiges

Gespräch mit Kollegen Dr. Melanie Frieling, Dr. Christian Beer:
- verwenden auch keine Kontrollgruppen
- wollen uns ihre Fragebögen schicken, Fragen für Demographie könnten hilfreich sein
- Stehen ansonsten vor dem gleichen Problem wie wir


## Quellen

Brooke, J. (1996). *SUS: A "Quick and Dirty" Usability Scale*. In P. W. Jordan et al. (Eds.), *Usability Evaluation in Industry*. Taylor & Francis.

Campbell, D. T., & Stanley, J. C. (1963). *Experimental and Quasi-Experimental Designs for Research*. Houghton Mifflin.

Deci, E. L., & Ryan, R. M. (2000). The "What" and "Why" of Goal Pursuits: Human Needs and the Self-Determination of Behavior. *Psychological Inquiry, 11*(4), 227-268.

Davis, F. D. (1989). Perceived Usefulness, Perceived Ease of Use, and User Acceptance of Information Technology. *MIS Quarterly, 13*(3), 319-340.

Hair, J. F., Black, W. C., Babin, B. J., & Anderson, R. E. (2022). *Multivariate Data Analysis* (9th ed.). Cengage.

Hart, S. G., & Staveland, L. E. (1988). Development of NASA-TLX. In P. A. Hancock & N. Meshkati (Eds.), *Human Mental Workload* (pp. 139-183). Elsevier.

Ifenthaler, D., & Yau, J. Y.-K. (2020). *Utilising Learning Analytics for Study Success*. Springer.

Kline, R. B. (2023). *Principles and Practice of Structural Equation Modeling* (5th ed.). Guilford Press.

Krueger, R. A., & Casey, M. A. (2015). *Focus Groups: A Practical Guide for Applied Research* (5th ed.). Sage.

Loh, C. S., Sheng, Y., & Ifenthaler, D. (Eds.). (2015). *Serious Games Analytics*. Springer.

Roediger, H. L., & Karpicke, J. D. (2006). Test-Enhanced Learning: Taking Memory Tests Improves Long-Term Retention. *Psychological Science, 17*(3), 249-255.

Sanchez, E., & Plumettaz-Sieber, M. (2019). Teaching and Learning with Escape Games: From Debriefing to Institutionalization of Knowledge.

Shadish, W. R., Cook, T. D., & Campbell, D. T. (2002). *Experimental and Quasi-Experimental Designs for Generalized Causal Inference*. Houghton Mifflin.

Shute, V. J., Ventura, M., Bauer, M., & Zapata-Rivera, D. (2009). Melding the Power of Serious Games and Embedded Assessment. *Educational Researcher, 38*(6), 404-412.

Veldkamp, A., van de Grint, L., Knippels, M. C. P. J., & van Joolingen, W. R. (2020). Escape Education: A Systematic Review on Escape Rooms in Education. *Educational Research Review, 31*, 100364.

Venkatesh, V., Morris, M. G., Davis, G. B., & Davis, F. D. (2003). User Acceptance of Information Technology: Toward a Unified View. *MIS Quarterly, 27*(3), 425-478.
