---
title: "Voriger Ansatz und Schritte zur Erstellung eines physischen Escape-Rooms - Git"
---

# Initial geplantes Vorgehen zur Umsetzung des analogen Escape-Rooms

Als Grundlage zur Erstellung des Escape-Rooms wurde das [Vorgehen](../research/design.md#approach) aus den verschiedenen Anleitungen aus der Recherche abgeleitet. Dabei werden die Lernziele und der Plot zunächst in groben Zügen skizziert und dann in Form von Rätseln und Geschichte konkretisiert.  Ein [Referenzscript von EscapeIF](https://www.becauseplaymatters.com/escapeif) hätte zum Abgleich gedient.

Das Vorgehen zur Erstellung des analogen Escape-Rooms ist gleichermaßen auch für digitale Escape-Rooms anwendbar.

Die beiden wissenschaftlichen Escape-Room-Plattformen aus der Lehre bringen dazu auch Schritte zur Gestaltung der Auswertung von Durchläufen mit.
Im Projektantrag wird "die Steigerung der Motivation der Studierenden und eine verbesserte Begleitung und Strukturierung von Lernwegen in der Selbstlernphase [..]" als Projektziel vorgestellt. Als erwartete Wirkungen werden "Zufriedenheit, Lernerfolg und Kompetenzerwerb" und "Erhöhung der Motivation [durch] Spielspaß [..] und kooperative/kompetitive Anreize" aufgezählt. Auf diese Kriterien ließe sich die Evaluierung ausrichten.

Sowohl A/B-Tests verschiedener Ausgestaltungen des Escape-Rooms als auch eine iterative Veränderung des Escape-Rooms sind damit möglich. Das Vorgehen sieht eine Verbesserung des Escape-Rooms mit iterativem Playtesting vor. Dies wäre mit den im Projektplan vorgesehenen mehrfachen Evaluierungen vereinbar gewesen.

# 1. Analoger Escape-Room als Kartenspiel für Lerninhalt "Git"

Aufbauend auf den Erfahrungen mit dem getesteten Exit-Spiel wurde zunächst ein Kartenspiel angestrebt, das primär in einer Gruppe am Tisch gespielt wird. Die vorderste Motivation dafür war die einfache Fertigung. Das Budget für Bürowaren hätte hoffentlich für den Druck von beidseitig bedruckten Karten benutzt werden können.

Die erste Kernidee für die Geschichte war eine Spurensuche mit den diversen Mitteln, die Git zum Nachvollziehen von Änderungen bereitstellt (`git blame -M`, `git bisect`). Anstatt eine böswilligen Änderung wäre die Ursache als fehlerhafte Konfliktauflösung erkannt worden. Mit `git format-patch` und einem USB-Stick wäre die Korrektur des Fehlers aus einem für die Suche abgeriegelten System extrahiert worden. Der Zweck sollte die Ergänzung des Vorlesungsmaterial von PM2 mit optionalen und fortgeschrittenen Inhalten sein.

Als Mechanik zur Eingabe von Befehlen und zur Überprüfung von Eingaben sollten nummerierte Eingabeschnipsel dienen, die in der richtigen Reihenfolge in eine Berechnung einfließen sollten. Die Mechanik dafür wäre in einem Einführungsrätsel anhand vom Bubble-Sort-Algorithmus vorgestellt worden, wobei auch der Fehler gefunden würde.

Die Progression durch den Bisect wäre mit einer Folge von Karten mit einer Visualisierung der aktuellen Position und des Fortschritts umgesetzt worden. Für die Bugsuche war eine größere Zahl Karten mit Codeschnipseln vorgesehen, die in der Gruppe parallel bearbeitet werden.

## Zeitprobleme bei der Vorbereitung der Präsentation

Die Zeit zwischen der ersten brauchbaren Version des Vorgehens in KW16 und der Vorstellung der geplanten Lösung in KW17 hat nicht ausgereicht, um Geschichte und Rätsel in einen Zustand zu bringen, aus dem heraus die Erreichung der angedachten Ziele sichtbar sein würde. Vor allem die mangelnde Erfahrung im Ausschreiben der Geschichte und Interaktionen haben diesen Prozess spürbar beeinträchtigt.

Als Maßnahme zur Verbesserung hat die Projektleitung die Aufgabe gestellt, [Techniken zur Erzählung](../research/narrative.md) zu recherchieren. Auch sollte die Struktur der Rätsel als Graph skizziert werden. Darüber hinaus sollten Strukturen von Rätseln in betrachteten Spielen recherchiert werden.

Ebenfalls wurden neue Anforderungen an die Lerninhalte gestellt (die Einbeziehung von regulären Git-Workflows). In dem top-down-Ansatz hätte das korrekterweise einen völligen Neuanfang bedingt.
Abseits davon war die Erstellung des Rätselgraphs nur durch Vorziehen der Ausarbeitung der Rätsel möglich.
Parallel dazu lief ein langwieriger Versuch der Klärung einer weiteren Anfrage und die Einarbeitung einer SHK.

Aus diesen Gründen war die Bearbeitung dieser Aufgaben war bis zum nächsten Meeting in KW19 nur eingeschränkt (keine Dokumentation der gefundenen Rätselstrukturen) und nur mit Abweichungen vom Vorgehen möglich.

## Mängel in der Betrachtung des Spielablaufs ausschließlich durch den Rätselgraphen

Rätsel und Geschichte von Escape-Rooms sind ineinander verzahnte Strukturen, die den Escape-Room im Kern ausmachen und deren Graphen ähnlich aussehen können. Allerdings sind sie nicht identisch.
Der Rätselgraph erklärt z.B. nicht unbedingt, warum Ereignisse stattfinden. Gleichermaßen trifft die "Story-Map" keine Aussage über nötige Reihenfolgen oder die Erfüllbarkeit von Bedingungen.

Für die Vorstellung wurde ein Rätselgraph angefragt. Da die Geschichte bei der Betrachtung des Graphs im Meeting nicht ersichtlich war, gab es Schwierigkeiten beim Verständnis der Geschichte und es wurde vorgeschlagen, den Rätselgraphen zur Erklärung der Abläufe mit Knoten aus Ereignissen in der Geschichte zu ergänzen. Weiterhin gab es Mängel bei Vorstellung des Vorgehens und dem Austausch darüber. Vor dem Hintergrund des Ausbleibens einer Einigung auf eine Vision zur Umsetzung des analogen Escape-Rooms wurde dieser Versuch zugunsten der Adaption eines fertigen Escape-Rooms abgebrochen.

# Verworfene technische Arbeit

Im Rahmen der Arbeit an dem analogen Escape-Room mit dem Thema "Git" wurde ein Git-Repo konstruiert, in das den Fehler aus der Geschichte eingebaut wurde. Der Fehler ist ein Off-By-One auf numerischen Ergebnissen. Zwei parallele Entwicklungen fügen eine Möglichkeit zur Eingabe mehrerer Antworten einem Quiz-System hinzu. Eine für die Bearbeitung von Fragen im Team, eine um mehrere Antworten zu akzeptieren. Als Ablenkung bei der Fehlersuche wurde ein Anti-Pattern eingebaut, bei dem die Indizes für die Eingaben um 1 verschoben werden um Platz für eine andere Resource am Anfang der Liste zu machen. Diese Änderung soll den Entwickler beim Mergen der zweiten Änderung irritiert haben, sodass dabei ein Fehler gemacht wurde.

Zur Hilfe bei der Veranschaulichung (und nachher zur Produktion der Karten) wurde ein Werkzeug gebaut, dass eine Liste von Karten-Definition einliest, diese numeriert und die Umleitungen anhand von Referenzen in Umleitungen anhand von Indizies umbaut. Die Vorder- und Rückseiten der Karten werden als Markdown ausgegeben, das programmatisch gerendert und leicht zum doppelseitigen Bedrucken von Kartonbögen angeordnet werden kann.

Die Git-Patches, das Programm und die Definition der Karten bis zum Einführungsrätsel zur Überprüfung der Machbarkeit liegen in `artefacts/`.
