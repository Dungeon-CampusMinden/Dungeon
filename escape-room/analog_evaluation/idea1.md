---
title: "Idee für Evaluierung eines physischen Escape-Rooms - Git und die Hochschule"
---

# Zusammenfassung

Eine Gruppe von Studenten stellt beim Testen eines E-Assessments fest, dass nur um 1 verschobene Lösungen akzeptiert werden. Unter der Prämisse, dass es eine Manipulation gibt, suchen Sie den Täter anhand des Git-Repos aus dem das E-Assessment.

Als Spielmaterial dient ein Stapel nummerierter und sortierter Karten, die beidseitig bedruckt sind.
Die Karten enthalten sowohl Weiterleitungen, Geschichte, Aufgaben als auch die Gegenstände der Aufgaben.

Das Spiel beginnt mit dem Zug der ersten Karte.

# Story - Manipulatierte Prüfungsplattform

Die Gruppe von Studenten hat eine Abgabe für ein Tutorium verpasst, weil sie bei einer kurzfristigen Verlegung des Tutoriums irrtümlicherweise in Bielefeld vor eine verschlossenen Tür gewartet haben, anstatt in Minden. Als Ersatz wird ihnen vom Dozenten (Frau Dr. Kordula Igel) angeboten, ein E-Assessment zu durchlaufen.

## Abschnitt 1 - Das E-Assessment

> Das Einstiegsrätsel zum kennenlernen.

Als Aufgabe sollen sie den Bubblesort-Algorithmus auf eine Zahlenfolge anwenden und die Tauschoperationen zählen. Die sortierten Zahlen werden abwechselnd addiert und subtrahiert. Am Ende wird der Anzahl der Schritte addiert.
Der Computer akzeptiert das richtige Ergebnis aber nicht (sagen wir: 2) und behauptet, dass die richtige Lösung eine andere wäre (3).

> Die Zahlen werden anhand der Rückseite sortiert vom Stapel genommen, beginnend mit der obersten Karte (Rückseite x), auf den Tisch gelegt und dabei aufgedeckt.
> Die nächste Karte wird ebenfalls gezogen. Um das Ergebnis "einzugeben", folgen die Studenten den Hinweisen darauf zu einer Auflösungskarte.
> Damit es keinen Hänger in der Kalkulation gibt, kann das Muster auf einer Karte stehen: `z_1 + z_2 - z_3 + .. + c`

> Mögliche Ergebnisse könnten "Auflösungsnummern" zugeordnet werden, die so konstruiert sind, dass der Modulo 3 der Quersumme der Auflösungsnummer auf eine von drei Karten verweist ohne direkt die falschen Ergebnisse zu offenbaren.

Die Studenten haben abhängig vom eingegebenen Ergebnis folgende Möglichkeiten:

1. richtiges Ergebnis
  - "der Computer sagt, das das Ergebnis falsch ist, aber ihr seid euch sicher, dass ihr keinen Fehler gemacht habt"
  - die Studenten erhalten eine zusätzliche Option für die Meldung beim Dozenten (`I2_1`)
  - Optionen:
    - andere Eingabe probieren
    - abbrechen und beim Dozenten melden
2. "off-by-one"
  - "der Computer ist zufrieden, aber irgendwie sieht das nicht richtig aus"
  - Optionen:
    - Erfolg melden
    - andere Eingabe probieren
3. "anderes Ergebnis"
  - "das sieht nicht richtig aus"
  - nochmal probieren

## Interaktion 2 - Meldung beim Dozenten

> Karte `I2_0`: "*Legt alle Karten für Interaktion 1 ab*"
> Je nach Ausgang der vorigen Interaktion
> Karte `I2_2` (Abbruch): "*Nehmt `I2_4` und `I2_5`*"
> Karte `I2_3` (Beendigung): "*Habt ihr Karte `I2_1` gefunden? Wenn ja, nehmt `I2_4` und `I2_7`", ansonsten nehmt `I2_4` und `I2_6`*".

Dr. K. Igel empfängt die Studenten. "Mir ist vorhin eingefallen, dass die Frist zur Einreichung meines Forschungsantrags heute um Mitternacht abläuft, also habe ich nicht viel Zeit. Lasst mich nur schnell euer Ergebnis angucken..." (`I2_4`)

1. Abbruch (nach Eingabe des richtigen Ergebnisses)
  - "Hmm. Stimmt. Das ist nicht gut! Ich überlege mir etwas wegen der Anerkennung der Leistung, aber jetzt haben wir ein ganz anderes Problem! *Nehmt `I2_8`*" (`I2_5`)
2. Beendigung ohne Eingabe des richtigen Ergebnisses
  - "Das ist nicht die richtig. Dafür sollte ich euch eigentlich keine Punkte geben. Andererseits ist die Software kaputt und damit kriegen noch viel größere Probleme! *Nehmt `I2_8`*" (`I2_6`)
3. Beendigung mit voriger Eingabe des richtigen Ergebnisses
  - "Praktischerweise kann ich das als Leistung anerkennen. Nur muss der Fehler dringend behoben werden! *Nehmt `I2_8`*" (`I2_7`)

> Karte `I2_8`: "In einer Stunde kommt ein Auditor vom BSI um die Assessments zu zertifizieren. Dieser Fehler muss behoben werden! Ohne die Zertifizierung werden alle damit abgenommen Prüfungen rückwirkend für ungültig erklärt - eine Katastrophe! Bald wird der nächste Studiengangsleiter gewählt und ich vermute, dass einer der Kandidaten dieses Debakel verwenden wird, um seine Wahl zu begünstigen. Deshalb bitte ich euch, den Fehler unauffällig zu beheben und der Ursache für den Bug diskret auf den Grund zu gehen. *Nehmt I2_9*"
> Karte `I2_9`: "Hier habt ihr den Schlüssel zum Serverraum. Die Container-Images für das ILIAS werden dort an einem Computer generiert, der nur vor Ort bedient wird. *Nehmt I3_0* Eine Sache noch: Kürzlich wurde ein Feature für das Lernen in Gruppen eingebaut. Dabei hat jede Person eine eigene Eingabemaske. Das brauchen wir auch für den Audit, also könnt ihr nicht einfach alles zurückrollen. *Nehmt Hinweiskarte 1*"

TODO git am, git gc, reflog, bisect
