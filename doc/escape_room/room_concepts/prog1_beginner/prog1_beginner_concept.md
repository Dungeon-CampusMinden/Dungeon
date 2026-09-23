# Programmieren 1: Das Erbe der Seelenweber

Dieses Dokument beschreibt den umgesetzten Escape Room für Programmieren 1 als Ganzes.
Der ursprüngliche Entwurf steht im Pitch-Dokument
`Pitch-Dokument_Programmieren-Einsteiger-Room.pdf`. Technische Einzelheiten zu Akt III und
Akt IV stehen in [Akt III: Methodenwerkstatt](../../../../game/doc/act3-method-workshop.md)
und [Akt IV: Labyrinth der Entscheidungen](../../../../game/doc/act4-decision-labyrinth.md).

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
| Akt I | Schmiede | Variablen / Datentypen | Was speichert Nox und in welchem Gefäß? |
| Akt II | Archiv, ferngesteuerter Keller | Schleifen | Wie oft und wie lange soll Nox etwas tun? |
| Akt III | Valerius' Werkstatt | Methoden | Welche Arbeit lässt sich benennen und wiederverwenden? |
| Akt IV | Labyrinth der Entscheidungen | Bedingungen | Welcher Weg folgt aus Nox' aktuellen Werten? |
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

# 4. Akt I: Die Schmiede des Golems

**Konzept:** Variablen und Datentypen

## Story

In der Schmiede steht Nox, ein fünf Tiles hoher Steingolem mit leerem Seelenkern. Zwei
Werkstattkisten enthalten die Eigenschaftsrunen und die Seelengefäße mit den Essenzen.
Neben dem Golem liegt Valerius' Bindungsplan: ein Buch mit einer Seite pro Eigenschaft,
einem Bild, dem gewünschten Wert und einer kurzen Notiz. Zum Beispiel: "O wie Osten.
Dorthin soll er sich nach dem Erwachen wenden."

## Ablauf

Die Gruppe öffnet beide Kisten und dann die Seelenwerkbank am Golem. Dort bindet sie in
zwei Stufen.

**Gefäße.** Jede der sechs Eigenschaften bekommt ein Gefäß. Die Gefäße nennen nur, was
sie fassen können, noch keine Java-Namen:

| Gefäß | Prägung | Aufgedeckt als |
| --- | --- | --- |
| Eisenkiste | Ganze Zahlen | `int` |
| Kristallflasche | Zahlen, auch Bruchteile | `double` |
| Pergament | Wörter und Texte | `String` |
| Runenstein | Ein einzelnes Zeichen | `char` |
| Lichtkugel | An oder aus | `boolean` |

Ein falsches Gefäß wird abgelehnt und erklärt. Ein Sonderfall bekommt eine eigene
Antwort: Die Kristallflasche fasst auch ganze Zahlen, Valerius verwendet dafür aber die
Eisenkiste.

**Essenzen.** Danach kommen die Werte `125`, `17`, `3.5`, `true`, `false`, `"Nox"` und
`'O'` in die Gefäße. Ein Wert vom falschen Typ passt nicht hinein. Ein Wert mit passendem
Typ, aber falschem Inhalt wird gespeichert, doch die Bindung reagiert nicht. `false` ist
der bewusste Ablenker. Ein neuer Wert überschreibt den alten.

Sobald alle Werte stimmen, deckt die Werkbank die Java-Typen auf und zeigt jede Fassung
als Deklaration, etwa `int schritte = 17;`. Mit "Aktivieren" erwacht Nox, bricht durch
die Wand und läuft zur Schleuse in den Keller.

## Lösung

| Eigenschaft | Gefäß | Wert | Java |
| --- | --- | --- | --- |
| Name | Pergament | `"Nox"` | `String name = "Nox";` |
| Lebensenergie | Eisenkiste | `125` | `int lebensenergie = 125;` |
| Mana | Kristallflasche | `3.5` | `double mana = 3.5;` |
| Aktiviert | Lichtkugel | `true` | `boolean aktiviert = true;` |
| Blickrichtung | Runenstein | `'O'` | `char blickrichtung = 'O';` |
| Schritte | Eisenkiste | `17` | `int schritte = 17;` |

## Lernziel

- Eine Variable besteht aus Typ, Name und Wert.
- Der Typ bestimmt, welche Werte hineinpassen.
- Ein passender Typ allein reicht nicht. Der Wert muss auch zur Bedeutung passen.
- Eine Zuweisung überschreibt den bisherigen Wert.

# 5. Akt II: Der Maschinenkeller

**Konzept:** Schleifen mit `while`, `do-while` und `for`

## Story

Im Archiv liegt Valerius' Arbeitsauftrag für den Keller. Nox soll die Förderbahn, den
Pumpenzugang und den Kettenzug freiräumen, den beschädigten Kühlkanal überqueren und
zuletzt den Schutt an der Torwinde entfernen. Messingmarken zeigen seine
Arbeitspositionen, Pfeile die geforderte Ausrichtung.

## Material

- **24 Rhythmusrunen** liegen auf den Lesetischen im Archiv und an zwei frühen Fundorten
  in der Schmiede. Für jeden der fünf Abschnitte gibt es je eine `while`-, `do-while`-
  und `for`-Variante (blau, violett, orange). Dazu kommen neun Ablenker aus alten
  Probeläufen, darunter eine endlose Drehrune. Eine gesammelte Rune gehört der ganzen
  Gruppe.
- **Die Kellersteuerung** am Schleusentor ist eine Arbeitsfläche. Die gesammelten Runen
  liegen dort als Karten mit ihrem Code. Genau eine Rune passt in den Executor.
- **Die Karte** zeigt das Raster des Kellers, Nox' Kopf mit Blickrichtung und Gefahren
  als `!`.
- **Der Sehstein** zeigt Nox' Umgebung live. Dort erkennt man, dass ein `!` ein
  Eindringling oder eine Grube ist. Während der Beobachtung ist die eigene Figur gesperrt.

Die Runen verwenden echten Java-Code mit deutschen Befehlen, zum Beispiel:

```java
do {
    schritt();
    angreifen();
} while (bodenVoraus());
rechtsDrehen();
```

Befehle: `schritt()`, `linksDrehen()`, `rechtsDrehen()`, `angreifen()`, `springen()`.
Bedingungen: `bodenVoraus()`, `amWegzeichen()`, `!amWegzeichen()`.

## Ablauf

Nox muss fünf Arbeitspositionen nacheinander erreichen und dort richtig ausgerichtet
stehen bleiben. Die bloße Berührung einer Marke genügt nicht.

| Abschnitt | Ort | Besonderheit |
| --- | --- | --- |
| 1 | Förderbahn | Das Ziel liegt vor dem Ende des Gangs |
| 2 | Pumpenzugang | Das Ziel liegt am Ende eines längeren Gangs |
| 3 | Kettenzug | Ein Eindringling steht im Weg. Angriff vor dem Schritt |
| 4 | Kühlkanal | Eine Grube. Sprung vor dem Schritt |
| 5 | Torwinde | Der Weg knickt wiederholt. Ein Schleifenrumpf mit Schritten und Drehungen |

Das Programm läuft tatsächlich ab. `while` prüft vor jedem Durchlauf, `do-while` erst
danach und `for` zählt. Die Auswertung nutzt Nox' Position, Blickrichtung und die
Hindernisse. Nach 24 Wiederholungen bricht ein Sicherheitsstopp ab. Ein falscher Lauf
zeigt das Scheitern, und Nox kehrt zur letzten Arbeitsposition zurück. Die Rune verlässt
den Executor, bleibt aber gesammelt. Während Nox läuft, nimmt die Steuerung kein neues
Programm an.

Nach dem fünften Abschnitt räumt Nox die Torwinde frei. Die verrostete Halterung bricht,
das Gegengewicht fällt, und der zweite Ausgang im Archiv öffnet sich. Eine
Wartungsnotiz in der Schmiede hat den Rost schon angekündigt. Der Schaden folgt auch aus
dem richtigen Programm. Nox kehrt über die Schleuse zurück und geht zur Werkstatt.

## Lösung

Jeder Abschnitt hat mindestens eine passende Rune aus seiner Familie. Die Hilfe verweist
auf diese:

| Abschnitt | Passende Rune |
| --- | --- |
| 1 Förderbahn | `for` (orange) |
| 2 Pumpenzugang | `while` (blau) |
| 3 Kettenzug | `while` (blau) |
| 4 Kühlkanal | `do-while` (violett) |
| 5 Torwinde | `for` (orange) |

Auch andere Runen zählen, wenn sie Nox richtig ausgerichtet ans Ziel bringen.

## Lernziel

- `while` prüft zuerst und kann null Durchläufe haben.
- `do-while` läuft mindestens einmal.
- `for` wiederholt eine feste Anzahl.
- Die Reihenfolge im Rumpf zählt, etwa Angriff vor Schritt oder Sprung vor Schritt.
- Ein Programm liest man, sagt seine Wirkung vorher und prüft sie dann am Verhalten.

# 6. Akt III: Valerius' Werkstatt

**Konzept:** Methoden, Parameter und Rückgabewerte.
Details: [act3-method-workshop.md](../../../../game/doc/act3-method-workshop.md)

## Story

Die Winde ist zerstört. Übrig bleibt der Nebenausgang der Werkstatt. Zwei Kristallaltäre
versorgen ihn, der erste mit drei, der zweite mit fünf Kristallen. Zwei Runensteine geben
die Kristallfelder frei.

Valerius' langes Programm im Steuerbuch auf der Werkbank erledigt alles schon. Seine
Steuerung fasst aber nur acht Befehle im Hauptprogramm. Methoden haben ihren eigenen
Speicher. Die Gruppe muss das Programm also umbauen, ohne dass es aufhört zu
funktionieren.

## Aufgabe

Das Ausgangsprogramm hat 28 Zeilen und durchläuft acht Arbeitsstellen: zwei Tore, einen
Runenstein rechts, einen Runenstein links, zwei Kristallfelder und zwei Altäre. Befehle
sind etwa `ÖFFNE();`, `GEHE(4);`, `DREHE(RECHTS);`, `AKTIVIERE();`,
`gesammelt = SAMMLE_ALLE();` und `LEGE_AB(3);`.

Die Prüfleiste verlangt sechs Bedingungen:

1. Alle Arbeitsstellen sind erledigt, beide Altäre sind gefüllt.
2. Nox trägt am Ende keine Kristalle mehr.
3. Die Variable `kristalle` ist `0`.
4. Das Hauptprogramm hat höchstens acht Blöcke.
5. Dieselbe Methode mit Parameter wird mehrfach aufgerufen.
6. Ein Rückgabewert wird im Aufrufer verwendet.

## Werkstatt

Die Werkbank ist eine freie Arbeitsfläche mit Hauptprogramm, Methodenentwurf, gebauten
Methodenrunen und losen Blöcken. Zeilen lassen sich einzeln oder als Auswahl zwischen
Hauptprogramm, Entwurf und Hintergrund verschieben. "Methode bauen" erzeugt aus dem
Entwurf eine benannte Rune mit höchstens sechs Zeilen. Diese Rune kommt als Aufruf ins
Hauptprogramm. Ausdrücke wie `kristalle = 1 + sammeln()` sind erlaubt. `GIB_ZURÜCK`
beendet eine Methode, und Blöcke danach werden als unerreichbar markiert. Die Ansicht
"Original" zeigt jederzeit das unveränderte Ausgangsprogramm.

"Ausführen" setzt Nox und alle Arbeitsstellen zurück und startet dann einen ganzen Lauf.
Code und gebaute Methoden bleiben dabei erhalten. Fehler erscheinen rot am auslösenden
Block im Hauptprogramm. Eine Befehls- und Aufruftiefengrenze stoppt endlose Rekursion.
Es gibt keine vorgeschriebene Klickfolge. Namen und Methodengrenzen sind frei wählbar.

## Beispiellösung

Diese Lösung lädt auch die Hilfe:

```text
hilfeTor():            ÖFFNE(); GEHE(4);
hilfeRune(richtung):   GEHE(1); DREHE(richtung); GEHE(3); AKTIVIERE();
hilfeSammeln():        GEHE(1); gesammelt = SAMMLE_ALLE(); GEHE(1); GIB_ZURÜCK gesammelt;
hilfeAltar(menge):     GEHE(1); LEGE_AB(menge); GEHE(1); GIB_ZURÜCK menge;

Hauptprogramm:
hilfeTor();
hilfeTor();
hilfeRune(RECHTS);
hilfeRune(LINKS);
kristalle += hilfeSammeln();
kristalle += hilfeSammeln();
kristalle -= hilfeAltar(3);
kristalle -= hilfeAltar(5);
```

Nach dem erfolgreichen Lauf öffnet sich der Nebenausgang. Ohne Fehlversuch gibt es den
Erfolg "Aus einem Guss", sonst "Übung macht den Meister".

## Lernziel

- Wiederholte Abläufe als benannte Methode herauslösen.
- Unterschiede zwischen ähnlichen Stellen als Parameter übergeben.
- Ein lokales Ergebnis mit `GIB_ZURÜCK` an den Aufrufer geben und dort weiterrechnen.
- Lokale Methodenvariablen von Variablen des Hauptprogramms unterscheiden.
- Refactoring ändert die Form des Programms, nicht seine Wirkung.

# 7. Akt IV: Das Labyrinth der Entscheidungen

**Konzept:** verschachtelte Bedingungen mit `if` / `else`, `&&` und `||`.
Details: [act4-decision-labyrinth.md](../../../../game/doc/act4-decision-labyrinth.md)

## Story

Hinter der Werkstatt liegt das Labyrinth vor dem Herzfeuer. Sechs Kreuzungen tragen je
eine Entscheidungsrune. Nox trägt drei Werte mit sich: Kraft, Energie und Temperatur.

## Ablauf

Ein Spieler steigt mit E auf Nox und entscheidet an jeder Kreuzung zwischen LINKS und
RECHTS. Weitere Spieler bewegen sich frei. Trennt sich der Reiter, wird der Sitz frei,
und ein anderer Spieler kann weitermachen.

Unten in der Mitte steht die Rune als eingerückter Pseudocode mit WENN, SONST, UND und
ODER. Die aktuellen Werte bleiben sichtbar. Die Wahl öffnet die gewählte Tür:

- **Richtig:** Nox läuft weiter zur nächsten Kreuzung. Eine Rune am Weg verändert seine
  Werte.
- **Falsch:** Nox nimmt den äußeren Rückgang bis zum START. Dort erhält er die festen
  Startwerte des nächsten Versuchs.

Jeder Versuch hat eigene Startwerte, bei denen keine Rune im äußersten SONST endet.
Nach einem Fehler kann dieselbe Kreuzung deshalb einen anderen Zweig verlangen.
Auswendiglernen hilft nicht, nur Nachrechnen.

## Runen und Werte

Start: Kraft 45, Energie 70, Temperatur 22.

| Kreuzung | Rune | Werte davor (K / E / T) | Richtig | Ereignis danach |
| --- | --- | --- | --- | --- |
| 1 | Tor der Energie | 45 / 70 / 22 | RECHTS | Energie -30 |
| 2 | Tor der Temperatur | 45 / 40 / 22 | RECHTS | Energie +30 |
| 3 | Tor der drei Runen | 45 / 70 / 22 | LINKS | Kraft +10, Temperatur +5 |
| 4 | Tor der zwei Pfade | 55 / 70 / 27 | LINKS | Kraft +13, Energie -15 |
| 5 | Tor der verbundenen Kräfte | 68 / 55 / 27 | RECHTS | Energie -13, Temperatur +4 |
| 6 | Herzfeuer-Siegel | 68 / 42 / 31 | LINKS | Ankunft am Herzfeuer |

Das gilt für einen fehlerfreien Durchlauf. Die letzte Rune hat vier Ebenen und
verbindet Bedingungen mit UND und ODER.

## Lernziel

- Eine Bedingung ergibt wahr oder falsch und wählt damit genau einen Zweig.
- Verschachtelte Bedingungen werden von außen nach innen geprüft. Ein falscher äußerer
  Zweig überspringt die inneren Prüfungen.
- `&&` und `||` verbinden Bedingungen.
- Dasselbe Programm liefert bei anderen Werten einen anderen Weg.

# 8. Abschluss am Herzfeuer

Nach der sechsten Rune steigt der Reiter ab. Vor dem Herzfeuer liegt eine Schriftrolle:

> Lege die Kristalle ins Herzfeuer.
> Damit schließt ihr den Raum ab.
> Nach dem Abspann endet das Spiel für alle.

"Opfergabe darbringen" nimmt nur der Server an, einmal und erst nach dem Labyrinth.
Schließen bricht ab. Die Opfergabe verbraucht die beiden Kristalle am Sockel, entzündet
das Herzfeuer und beendet das Tracking als erfolgreich. Danach sehen alle verbundenen
Spieler den Abspann:

> Die Kristalle verglühen im Herzfeuer. Für einen Moment leuchten Nox' Runen im selben
> Takt wie die Flammen.
>
> Du hast es geschafft! Du hast Nox zum Leben erweckt und bis zum Herzfeuer geführt.
> Variablen, Schleifen, Methoden und Bedingungen waren deine Werkzeuge.
> Programmieren 1 ist abgeschlossen.

Wer den Abspann bestätigt, wartet auf die anderen. Getrennte Spieler halten das Ende
nicht auf. Sobald alle verbundenen Spieler bestätigt haben, endet das Spiel regulär.

Eine eigene Abschlussaufgabe, die alle vier Konzepte kombiniert, gibt es bewusst nicht.
Akt IV ist schon die Prüfung, die frühere Konzepte wieder aufgreift: Nox trägt Werte,
läuft Wege und reagiert auf Bedingungen. Ein weiteres Rätsel würde den Raum über die
Zielzeit hinaus verlängern.

# 9. Didaktischer Aufbau

| Akt | Erst die Erfahrung | Dann der Begriff |
| --- | --- | --- |
| I | Werte passen nur in bestimmte Gefäße | `int`, `double`, `String`, `char`, `boolean` als Deklaration |
| II | Nox wiederholt Bewegungen, bis etwas eintritt oder gezählt ist | `while`, `do-while`, `for` im Runencode |
| III | Das lange Programm passt nicht in die Steuerung | Methode, Parameter, Rückgabe |
| IV | Werte bestimmen den Weg | WENN / SONST, UND, ODER als Runen-Pseudocode |

Die Abstraktion steigt von Akt zu Akt. In Akt I ordnen die Spieler zu, in Akt II wählen
sie fertige Programme, in Akt III bauen sie Programme um, und in Akt IV werten sie
Programme im Kopf aus.

# 10. Zeitplanung

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
