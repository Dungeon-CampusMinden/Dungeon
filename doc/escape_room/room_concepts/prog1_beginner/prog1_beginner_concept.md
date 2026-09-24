# Programmieren 1: Das Erbe der Seelenweber

Dieses Dokument gibt einen Überblick über den umgesetzten Escape Room für Programmieren 1.
Die Akte sind in eigenen Dokumenten beschrieben:

- [Akt I: Die Schmiede des Golems](act1-golem-forge.md)
- [Akt II: Der Maschinenkeller](act2-machine-cellar.md)
- [Akt III: Valerius' Werkstatt](act3-method-workshop.md)
- [Akt IV: Das Labyrinth der Entscheidungen](act4-decision-labyrinth.md)

Der ursprüngliche Entwurf steht im Pitch-Dokument
`Pitch-Dokument_Programmieren-Einsteiger-Room.pdf`.

# 1. Überblick

| | |
| --- | --- |
| Fach | Programmieren 1, Informatik, 1. Semester |
| Einsatz | Nach der ersten Vorlesung, als Selbststudium oder Vertiefung |
| Dauer | Zielwert 60 bis 65 Minuten, im Playtest zu prüfen |
| Spieler | 1 bis 2, allein oder im Multiplayer |
| Setting | Fantasy, verlassene magische Schmiede |
| Start | `rooms.programming.Programming`, Tracking-Raum `programming-1` |

Der Raum behandelt vier Grundlagen in fester Reihenfolge:

1. Variablen und Datentypen
2. Schleifen (`while`, `do-while`, `for`)
3. Methoden mit Parametern und Rückgabewerten
4. Verschachtelte Bedingungen (`if` / `else`, `&&`, `||`)

Alle vier Akte arbeiten mit demselben Golem Nox. Die Spieler steuern ihn nie direkt. Sie
geben ihm Werte, Programme, Methoden und Entscheidungen und sehen dann, was er damit tut.
Jeder Akt beginnt mit einem Problem in der Spielwelt. Die Java-Schreibweise folgt erst,
wenn die Idee dahinter schon funktioniert hat.

# 2. Rahmenhandlung

Ein Zwischentitel eröffnet das Spiel: "Aethelgard. Die Schmiede des vermissten Meisters
Valerius."

Am Eingang liegt ein Brief von Valerius. Er ist zum Herzfeuer gegangen und nicht
zurückgekehrt. Der zweite Ausgang der Schmiede klemmt, weil Schutt die Torwinde im Keller
blockiert und aus den Leitungen heißer Dampf austritt. Menschen sollen nicht hinunter.
Für solche Arbeiten hat Valerius den Golem Nox gebaut, doch dessen Seelenbindung ist
erloschen.

Daraus ergibt sich der Auftrag des Raums:

1. Nox' Seelenbindung wiederherstellen (Akt I).
2. Nox fernsteuern, bis er den Keller geräumt hat (Akt II).
3. Die Winde bricht dabei. Der Nebenausgang der Werkstatt ist der letzte Weg. Seine
   Steuerung verlangt ein kurzes Programm mit Methoden (Akt III).
4. Hinter der Werkstatt liegt das Labyrinth der Entscheidungen. An seinem Ende brennt das
   Herzfeuer (Akt IV).
5. Am Herzfeuer bringen die Spieler eine Opfergabe dar und beenden das Spiel (Abschluss).

Nox spricht kurz und trocken. Nach der gebrochenen Winde sagt er: "Der Schutt ist
beseitigt. Die Winde leider auch."

# 3. Raum und Ablauf

| Abschnitt | Ort | Konzept | Zentrale Frage |
| --- | --- | --- | --- |
| [Akt I](act1-golem-forge.md) | Schmiede | Variablen / Datentypen | Was speichert Nox und in welchem Gefäß? |
| [Akt II](act2-machine-cellar.md) | Archiv, ferngesteuerter Keller | Schleifen | Wie oft und wie lange soll Nox etwas tun? |
| [Akt III](act3-method-workshop.md) | Valerius' Werkstatt | Methoden | Welche Arbeit lässt sich benennen und wiederverwenden? |
| [Akt IV](act4-decision-labyrinth.md) | Labyrinth der Entscheidungen | Bedingungen | Welcher Weg folgt aus Nox' aktuellen Werten? |
| Abschluss | Herzfeuer | Alle | Die Reise endet |

Die Akte laufen nacheinander. Tore und Durchbrüche trennen die Bereiche und öffnen sich
erst, wenn der vorherige Akt gelöst ist. Den Keller aus Akt II betreten die Spieler nie.
Sie beobachten ihn über eine Karte und einen Sehstein.

## Gemeinsame Regeln

- Falsche Eingaben kosten nichts außer Zeit. Jede Aufgabe lässt sich beliebig oft
  wiederholen. Ein Fehlversuch setzt nur den aktuellen Abschnitt zurück, nie den
  bisherigen Fortschritt.
- Programme laufen wirklich ab. Ein falsches Programm lässt Nox sichtbar scheitern. Das
  Spiel meldet nicht bloß "falsch".
- Der Server besitzt Fortschritt, Werte und Ausführung. Clients schicken nur Absichten.
- Alle Spieler starten an derselben Stelle als Char03. Es gibt keine festen Rollen. Im
  Multiplayer sehen beide denselben Zustand und können sich die Arbeit aufteilen.

## Hilfe, Questlog und Erfolge

**Hilfe.** In jedem Akt kann die Gruppe für die aktuelle Aufgabe gestufte Hinweise
anfordern. Die Stufen werden nacheinander freigegeben. Stufe 1 erklärt die Aufgabe,
Stufe 2 nennt die entscheidende Idee und Stufe 3 markiert die Lösung im UI. Danach bietet
die Hilfe eine automatische Lösung an, die erst nach einer Bestätigung ausgeführt wird.
In Akt IV setzt Stufe 3 Nox' aktuelle Werte in die Rune ein, und die automatische Lösung
wählt nur an der aktuellen Kreuzung die richtige Tür. Freigegebene Hinweise bleiben im
Questlog lesbar.

**Questlog.** Das Questlog zeigt die aktuelle Aufgabe und gefundene Texte. Jeder Akt hat
einen eigenen Tab mit seinem Verlauf: kompakte Blöcke mit den Ergebnissen der Versuche,
dazu die angeforderten Tipps und die gelösten Aufgaben. Akt I führt jede Zuordnung,
Akt II jeden Kellerlauf, Akt III jeden Programmlauf und Akt IV jede Kreuzungsentscheidung
mit den Werten, bei denen sie getroffen wurde. Hilfe über die automatische Lösung ist
markiert.

**Erfolge.** Der Raum hat 18 Erfolge. Einige gibt es für Fortschritt, zum Beispiel
"Seelenweber" für die fertige Bindung oder "Mit Nebenwirkungen" für den geräumten Keller.
Andere belohnen Neugier: ein Wert im falschen Gefäß, `aktiviert = false`, die endlose
Drehrune bis zum Sicherheitsstopp, alle Fackeln gleichzeitig aus oder das gelöschte
Herzfeuer. Im Keller und in der Werkstatt unterscheiden je zwei Erfolge einen
fehlerfreien Durchlauf von einem Abschluss nach Fehlversuchen. "Valerius wäre stolz"
(Platin) verlangt alle übrigen Erfolge, auch über mehrere Durchläufe. Nach einer
automatischen Lösung im Keller entfällt "Auf Anhieb". In der Werkstatt entfallen dann
beide Abschlusserfolge.

## Tracking

Das Tracking erfasst pro Rätsel Start, Lösung, Versuche mit Fehlergründen, genutzte
Hinweise und wichtige Interaktionen. Die Rätsel heißen `vessels`, `essences`,
`cellar-0` bis `cellar-4`, `methods` und `decisions`. Die Opfergabe am Herzfeuer beendet
die Sitzung als erfolgreich abgeschlossen. Die Lesezeit des Abspanns zählt damit nicht
mehr zur Spielzeit.

# 4. Abschluss am Herzfeuer

Nach der sechsten Rune bringen die Spieler am Herzfeuer eine Opfergabe dar. Sie beendet
das Tracking als erfolgreich, danach sehen alle verbundenen Spieler den Abspann. Details
und Texte stehen in [Akt IV](act4-decision-labyrinth.md#abschluss-am-herzfeuer).

Eine eigene Abschlussaufgabe, die alle vier Konzepte kombiniert, gibt es bewusst nicht.
Akt IV ist schon die Prüfung, die frühere Konzepte wieder aufgreift: Nox trägt Werte,
läuft Wege und reagiert auf Bedingungen. Ein weiteres Rätsel würde den Raum über die
Zielzeit hinaus verlängern.

# 5. Didaktischer Aufbau

| Akt | Erst die Erfahrung | Dann der Begriff |
| --- | --- | --- |
| I | Werte passen nur in bestimmte Gefäße | `int`, `double`, `String`, `char`, `boolean` als Deklaration |
| II | Nox wiederholt Bewegungen, bis etwas eintritt oder gezählt ist | `while`, `do-while`, `for` im Runencode |
| III | Das lange Programm passt nicht in die Steuerung | Methode, Parameter, Rückgabe |
| IV | Werte bestimmen den Weg | WENN / SONST, UND, ODER als Runen-Pseudocode |

Die Abstraktion steigt von Akt zu Akt. In Akt I ordnen die Spieler zu, in Akt II wählen
sie fertige Programme, in Akt III bauen sie Programme um, und in Akt IV werten sie
Programme im Kopf aus.

# 6. Zeitplanung

| Abschnitt | Richtwert |
| --- | ---: |
| Einführung und Brief | 3 Min. |
| Akt I: Variablen | 10 Min. |
| Akt II: Schleifen | 15 Min. |
| Akt III: Methoden | 20 bis 25 Min. |
| Akt IV: Bedingungen | 10 Min. |
| Abschluss | 2 Min. |
| **Gesamt** | **60 bis 65 Min.** |

Die Werte sind Schätzungen. Die tatsächliche Dauer, die Verständlichkeit und die
Hinweisstufen müssen mit Lernenden getestet werden.
