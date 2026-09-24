# Akt I: Die Schmiede des Golems

Teil des Raums [Programmieren 1: Das Erbe der Seelenweber](prog1_beginner_concept.md).

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
