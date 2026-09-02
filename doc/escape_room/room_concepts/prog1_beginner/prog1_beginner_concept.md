# Programmieren 1 – Escape Room

## Das Erbe der Seelenweber

---

# 1. Überblick

**Fach:** Programmieren 1
**Studiengang:** Informatik, 1. Semester
**Einsatz:** Nach der ersten Vorlesung als Selbststudium / Vertiefung
**Dauer:** maximal 60 Minuten
**Spieler:** 1–2 Personen
**Setting:** Fantasy / Magische Schmiede

Der Escape Room besteht aus vier aufeinander aufbauenden Akten:

1. **Die Schmiede des Golems** – Variablen und Datentypen
2. **Das Labyrinth der ewigen Wächter** – Schleifen
3. **Die Fertigkeits-Scrolls** – Methoden
4. **Das Labyrinth der Entscheidungen** – `if` / `else`

Die vier Rätsel bauen auf demselben Golem auf. Die Ergebnisse eines Rätsels werden teilweise in den nächsten Akt übernommen.

---

# 2. Rahmenhandlung

Die Spieler erwachen in der verlassenen Schmiede von **Aethelgard**.

Die Schmiede gehörte dem legendären Seelenweber **Meister Valerius**. Er erschuf einen mächtigen Wächter-Golem, der das Herzfeuer der Schmiede beschützen sollte.

Doch Valerius verschwand.

Seitdem breitet sich die **Entropie**, eine magische Kraft des Verfalls, durch die Schmiede aus. Die Maschinen stehen still, die Schutzmechanismen versagen und das Herzfeuer droht zu erlöschen.

Der Golem könnte die Schmiede retten.

Doch seine Seele ist leer.

Auf einer alten Steintafel steht:

> *„Ein Körper braucht Essenz.*
> *Eine Seele braucht Rhythmus.*
> *Ein Geist braucht Fertigkeiten.*
> *Und ein Wächter muss entscheiden können.“*

Die Spieler müssen die vier Säulen der Seelenbindung wiederherstellen.

---

# AKT I – Die Schmiede des Golems

## Thema

**Variablen und Datentypen**

---

## Story

In der Mitte der Schmiede steht ein unvollständiger Steingolem.

Sein Körper ist fertig, doch in seiner Brust befindet sich ein leerer Seelenkristall.

Auf einem Altar steht:

> *„Jeder Golem benötigt Eigenschaften.*
> *Doch jede Eigenschaft verlangt das richtige Gefäß.“*

Im Raum liegen drei Arten magischer Gegenstände:

* Eigenschaftsrunen
* Seelengefäße
* Magische Essenzen

Die Spieler müssen die drei Ebenen miteinander verbinden.

---

# Rätsel 1.1 – Die Eigenschaften des Golems

## Material für die Spieler

Spieler A erhält folgende Runen:

| Rune              | Beschreibung                                   |
| ----------------- | ---------------------------------------------- |
| **Name**          | Wie heißt der Golem?                           |
| **Lebensenergie** | Wie viel Schaden hält er aus?                  |
| **Mana**          | Wie viel magische Energie besitzt er?          |
| **Aktiviert**     | Ist der Golem eingeschaltet?                   |
| **Blickrichtung** | Welches einzelne Zeichen zeigt seine Richtung? |
| **Schritte**      | Wie viele Schritte ist er bereits gegangen?    |

Spieler B erhält folgende Gefäße:

| Gefäß               | Beschreibung                          |
| ------------------- | ------------------------------------- |
| **Eisenkiste**      | Kann nur ganze Mengen aufnehmen       |
| **Kristallflasche** | Kann auch Bruchteile speichern        |
| **Pergament**       | Kann beliebig viele Zeichen speichern |
| **Runenstein**      | Kann genau ein Zeichen aufnehmen      |
| **Lichtkugel**      | Kennt nur JA oder NEIN                |

---

## Aufgabe

Die Spieler müssen für jede Eigenschaft das passende Gefäß finden.

### Spielerblatt

| Eigenschaft   | Passendes Gefäß |
| ------------- | --------------- |
| Name          | __________      |
| Lebensenergie | __________      |
| Mana          | __________      |
| Aktiviert     | __________      |
| Blickrichtung | __________      |
| Schritte      | __________      |

---

## Lösung

| Eigenschaft   | Gefäß           | Begründung                                   |
| ------------- | --------------- | -------------------------------------------- |
| Name          | Pergament       | Ein Name besteht aus beliebig vielen Zeichen |
| Lebensenergie | Eisenkiste      | Lebensenergie wird als ganze Menge angegeben |
| Mana          | Kristallflasche | Mana kann Bruchteile enthalten               |
| Aktiviert     | Lichtkugel      | Es gibt nur JA oder NEIN                     |
| Blickrichtung | Runenstein      | Es wird genau ein Zeichen gespeichert        |
| Schritte      | Eisenkiste      | Die Anzahl der Schritte ist eine ganze Zahl  |

Damit haben die Spieler die verschiedenen Arten von Werten unterschieden, ohne zunächst mit Java-Datentypen arbeiten zu müssen.

---

# Rätsel 1.2 – Die magischen Essenzen

Nach der Zuordnung erscheinen folgende Essenzen:

```text
125
17
3.5
true
false
"Nox"
'O'
```

Die Spieler müssen die passenden Essenzen den Eigenschaften zuordnen.

---

## Aufgabe

Ordnet jeder Eigenschaft den passenden konkreten Wert zu.

### Spielerblatt

| Eigenschaft   | Gefäß           | Wert       |
| ------------- | --------------- | ---------- |
| Name          | Pergament       | __________ |
| Lebensenergie | Eisenkiste      | __________ |
| Mana          | Kristallflasche | __________ |
| Aktiviert     | Lichtkugel      | __________ |
| Blickrichtung | Runenstein      | __________ |
| Schritte      | Eisenkiste      | __________ |

---

## Lösung

| Eigenschaft   | Gefäß           | Wert    |
| ------------- | --------------- | ------- |
| Name          | Pergament       | `"Nox"` |
| Lebensenergie | Eisenkiste      | `125`   |
| Mana          | Kristallflasche | `3.5`   |
| Aktiviert     | Lichtkugel      | `true`  |
| Blickrichtung | Runenstein      | `'O'`   |
| Schritte      | Eisenkiste      | `17`    |

Die übrigen Essenzen `false` werden nicht benötigt.

---

# Rätsel 1.3 – Die Übersetzung der Gefäße

Wenn alle Werte korrekt eingesetzt wurden, aktiviert sich der Seelenkristall.

Auf einer Steintafel erscheint:

| Magisches Gefäß | Programmiersprache |
| --------------- | ------------------ |
| Eisenkiste      | `int`              |
| Kristallflasche | `double`           |
| Pergament       | `String`           |
| Runenstein      | `char`             |
| Lichtkugel      | `boolean`          |

---

## Finale Werte des Golems

```text
Name:
Nox

Lebensenergie:
125

Mana:
3.5

Aktiviert:
true

Blickrichtung:
'O'

Schritte:
17
```

Diese Werte werden im weiteren Verlauf des Escape Rooms verwendet.

---

## Lösungsschlüssel Akt I

```text
Name         → Pergament → "Nox" → String
Lebensenergie→ Eisenkiste → 125 → int
Mana         → Kristallflasche → 3.5 → double
Aktiviert    → Lichtkugel → true → boolean
Blickrichtung→ Runenstein → 'O' → char
Schritte     → Eisenkiste → 17 → int
```

---

# AKT II – Das Labyrinth der ewigen Wächter

## Thema

**Schleifen: `while`, `do-while`, `for`**

---

## Story

Der Golem ist erwacht.

Doch er kann seine Bewegungen noch nicht kontrollieren. Er muss lernen, Handlungen zu wiederholen.

Vor dem Labyrinth stehen drei Steintafeln:

### Der Wächterzauber

> *„Erst prüfen, dann handeln.“*

### Der Vorstoßzauber

> *„Erst handeln, dann prüfen.“*

### Der Zählerzauber

> *„Beginne mit einem Zähler. Wiederhole die Handlung, solange du noch nicht am Ziel bist.“*

Im Labyrinth befinden sich verschiedene Situationen.

Die Spieler müssen die passende Rune für jede Situation finden.

---

# Die drei Schleifenrunen

## Rune W – Der Wächter

> **Solange die Bedingung erfüllt ist:**
>
> Führe die Handlung aus.
>
> Prüfe anschließend erneut die Bedingung.
>
> Wenn die Bedingung nicht erfüllt ist, endet der Zauber.

**Typ:** `while`

---

## Rune D – Der Vorstoß

> Führe die Handlung zunächst einmal aus.
>
> Prüfe anschließend die Bedingung.
>
> Wenn sie erfüllt ist, wiederhole die Handlung.

**Typ:** `do-while`

---

## Rune F – Der Zähler

> Lege einen Startwert fest.
>
> Prüfe, ob der Zähler noch nicht am Ziel ist.
>
> Führe die Handlung aus.
>
> Verändere anschließend den Zähler.
>
> Wiederhole den Vorgang.

**Typ:** `for`

---

# Die 15 Labyrinthstationen

Die folgenden Stationen liegen in einem zusammenhängenden Labyrinth.

Die Nummerierung dient nur der Spielleitung.

Die Spieler erhalten die Situationen in einer gemischten Reihenfolge bzw. finden sie auf ihrem Weg.

---

## Station 1 – Der lange Gang

### Situation

Vor dem Golem befindet sich ein langer, gerader Gang. Am Ende befindet sich eine Wand.

```text
████████████████████
█ Golem → · · · · · █
████████████████████
```

### Aufgabe

Der Golem soll so lange vorwärts gehen, wie der Weg frei ist.

### Rune

> **Solange der Weg vor dir frei ist:**
>
> → Gehe einen Schritt vorwärts.
>
> → Prüfe danach erneut den Weg.
>
> Wenn der Weg nicht mehr frei ist: Ende.

### Lösung

**`while`**

---

## Station 2 – Die Wand zur Linken

### Situation

Links vom Golem befindet sich eine Wand. An einer bestimmten Stelle endet die Wand.

```text
████████████████
█ Golem → · · · █
██████████████ █
```

### Aufgabe

Der Golem soll sich bewegen, solange sich links von ihm eine Wand befindet.

### Rune

> **Solange sich links vom Golem eine Wand befindet:**
>
> → Gehe einen Schritt vorwärts.
>
> → Prüfe danach erneut die linke Seite.

### Lösung

**`while`**

---

## Station 3 – Der Druckschalter

### Situation

Vor dem Golem befindet sich ein magischer Druckschalter.

```text
██████████████
█ Golem → · ◆ · · █
██████████████
```

Der Schalter wird erst aktiviert, wenn der Golem ihn betritt.

### Aufgabe

Der Golem muss sich zunächst bewegen. Danach wird geprüft, ob der Schalter aktiviert wurde.

### Rune

> **Gehe zuerst einen Schritt vorwärts.**
>
> Prüfe danach:
>
> **Ist der Druckschalter aktiviert?**
>
> Solange der Schalter noch nicht aktiviert ist:
>
> → Gehe einen weiteren Schritt.
>
> → Prüfe den Schalter erneut.

### Lösung

**`do-while`**

---

## Station 4 – Die fünf Bewegungskristalle

### Situation

Der Golem besitzt fünf Bewegungskristalle.

```text
████████████████
█ Golem → · · · · · G █
████████████████
```

### Aufgabe

Der Golem darf höchstens fünf Schritte gehen.

### Rune

> **Beginne mit 0 verbrauchten Schritten.**
>
> Solange weniger als 5 Schritte verbraucht wurden:
>
> → Gehe einen Schritt vorwärts.
>
> → Erhöhe die Schrittzahl um 1.

### Lösung

**`for`**

---

## Station 5 – Die Wand zur Rechten

### Situation

Rechts vom Golem befindet sich eine Wand.

### Aufgabe

Der Golem soll sich bewegen, solange sich rechts von ihm eine Wand befindet.

### Rune

> **Prüfe zuerst die rechte Seite.**
>
> Solange sich dort eine Wand befindet:
>
> → Gehe einen Schritt vorwärts.
>
> → Prüfe erneut die rechte Seite.

### Lösung

**`while`**

---

## Station 6 – Die magische Brücke

### Situation

Vor dem Golem befindet sich ein magisches Feld. Wenn der Golem das Feld betritt, erscheint eine Brücke.

```text
██████████████
█ Golem → ◆ ─ ─ ─ G █
██████████████
```

### Aufgabe

Der Golem muss zunächst das Feld betreten.

### Rune

> **Betritt zuerst das nächste Feld.**
>
> Prüfe danach:
>
> **Ist die magische Brücke erschienen?**
>
> Solange die Brücke vorhanden ist:
>
> → Gehe einen Schritt weiter.
>
> → Prüfe erneut.

### Lösung

**`do-while`**

---

## Station 7 – Die brennenden Fackeln

### Situation

Entlang des Weges befinden sich Fackeln.

```text
██████████████████
█ Golem → 🔥 🔥 🔥 🔥 · G █
██████████████████
```

### Aufgabe

Der Golem soll den Fackeln folgen, solange die nächste Fackel brennt.

### Rune

> **Prüfe die nächste Fackel.**
>
> Solange sie brennt:
>
> → Gehe einen Schritt vorwärts.
>
> → Prüfe danach die nächste Fackel.

### Lösung

**`while`**

---

## Station 8 – Die drei Runensteine

### Situation

Auf dem Weg befinden sich drei Runensteine.

```text
████████████████
█ Golem → ◆ → ◆ → ◆ → G █
████████████████
```

### Aufgabe

Der Golem soll genau drei Runensteine aktivieren.

### Rune

> **Beginne mit dem ersten Runenstein.**
>
> Solange noch nicht 3 Runensteine aktiviert wurden:
>
> → Aktiviere den nächsten Runenstein.
>
> → Erhöhe die Anzahl um 1.

### Lösung

**`for`**

---

## Station 9 – Der Nebelgang

### Situation

Der Golem betritt einen magischen Nebel.

```text
██████████████████
█ Golem → 🌫 · · · · · G █
██████████████████
```

Der Ausgang ist zunächst nicht sichtbar.

### Aufgabe

Der Golem muss zunächst in den Nebel laufen.

### Rune

> **Gehe zuerst einen Schritt in den Nebel.**
>
> Prüfe danach:
>
> **Ist der Ausgang sichtbar?**
>
> Solange der Ausgang noch nicht sichtbar ist:
>
> → Gehe einen weiteren Schritt.
>
> → Prüfe erneut.

### Lösung

**`do-while`**

---

## Station 10 – Die sieben Kristalle

### Situation

Auf dem Weg liegen sieben Kristalle.

```text
████████████████████
█ Golem → ◆ ◆ ◆ ◆ ◆ ◆ ◆ → G █
████████████████████
```

### Aufgabe

Der Golem soll alle sieben Kristalle einsammeln.

### Rune

> **Beginne mit 0 gesammelten Kristallen.**
>
> Solange weniger als 7 Kristalle gesammelt wurden:
>
> → Sammle einen Kristall.
>
> → Erhöhe die Anzahl um 1.

### Lösung

**`for`**

---

## Station 11 – Das verschlossene Tor

### Situation

Der Weg führt zu einem magischen Tor.

```text
████████████████
█ Golem → · · 🔒 · · G █
████████████████
```

Das Tor öffnet sich, sobald der Golem den richtigen Punkt erreicht.

### Aufgabe

Der Golem soll sich bewegen, solange das Tor verschlossen ist.

### Rune

> **Solange das Tor verschlossen ist:**
>
> → Gehe einen Schritt vorwärts.
>
> → Prüfe danach erneut das Tor.

### Lösung

**`while`**

---

## Station 12 – Die magische Rune

### Situation

Vor dem Golem befindet sich eine magische Rune.

```text
████████████████
█ Golem → · ◆ · · · G █
████████████████
```

Beim Betreten verändert sich der Zustand des Labyrinths.

### Aufgabe

Der Golem muss die Rune zunächst betreten.

### Rune

> **Betritt zuerst das nächste Runenfeld.**
>
> Prüfe danach:
>
> **Ist die Rune noch aktiv?**
>
> Solange die Rune aktiv ist:
>
> → Gehe zum nächsten Feld.
>
> → Prüfe erneut.

### Lösung

**`do-while`**

---

## Station 13 – Die vier Wächter

### Situation

Auf dem Weg befinden sich vier Wächter.

```text
████████████████████
█ Golem → 👤 · 👤 · 👤 · 👤 → G █
████████████████████
```

### Aufgabe

Der Golem soll genau vier Wächter passieren.

### Rune

> **Beginne bei Wächter Nummer 1.**
>
> Solange noch nicht 4 Wächter passiert wurden:
>
> → Gehe am nächsten Wächter vorbei.
>
> → Erhöhe die Wächterzahl um 1.

### Lösung

**`for`**

---

## Station 14 – Der Ausgang im Nebel

### Situation

Der Ausgang ist zunächst nicht sichtbar.

### Aufgabe

Der Golem soll sich bewegen, bis er den Ausgang sehen kann.

### Rune

> **Prüfe zuerst: Ist der Ausgang sichtbar?**
>
> Wenn nein:
>
> → Gehe einen Schritt vorwärts.
>
> → Prüfe erneut.
>
> Wiederhole dies, solange der Ausgang nicht sichtbar ist.

### Lösung

**`while`**

---

## Station 15 – Die letzte Passage

### Situation

Der Golem besitzt maximal fünf Bewegungskristalle. Gleichzeitig befindet sich ein Hindernis auf dem Weg.

```text
████████████████████
█ Golem → · · ◆ · · · G █
████████████████████
```

### Aufgabe

Der Golem darf höchstens fünf Schritte gehen und darf nicht gegen das Hindernis laufen.

### Rune

> **Beginne mit 0 verbrauchten Schritten.**
>
> Solange weniger als 5 Schritte verbraucht wurden:
>
> → Prüfe, ob der Weg frei ist.
>
> → Wenn der Weg frei ist, gehe einen Schritt.
>
> → Erhöhe danach die Schrittzahl um 1.
>
> Wenn 5 Schritte erreicht sind oder der Weg nicht mehr frei ist: Ende.

### Lösung

**`for`**

---

# Gemischte Runen – Material für das Rätsel

Die 15 Runen werden in gemischter Reihenfolge ausgegeben.

| Rune | Gehört zu               |
| ---- | ----------------------- |
| A    | Langer Gang             |
| B    | Wand zur Linken         |
| C    | Druckschalter           |
| D    | Fünf Bewegungskristalle |
| E    | Wand zur Rechten        |
| F    | Magische Brücke         |
| G    | Brennende Fackeln       |
| H    | Drei Runensteine        |
| I    | Nebelgang               |
| J    | Sieben Kristalle        |
| K    | Verschlossenes Tor      |
| L    | Magische Rune           |
| M    | Vier Wächter            |
| N    | Ausgang im Nebel        |
| O    | Letzte Passage          |

### Ausgabe-Reihenfolge

Die tatsächliche Reihenfolge der ausgeteilten Runen:

1. H
2. C
3. N
4. F
5. D
6. K
7. I
8. M
9. B
10. O
11. G
12. L
13. A
14. J
15. E

---

# Lösungsschlüssel Akt II

| Abschnitt | Rune | Schleifentyp |
| --------: | ---- | ------------ |
|         1 | A    | `while`      |
|         2 | B    | `while`      |
|         3 | C    | `do-while`   |
|         4 | D    | `for`        |
|         5 | E    | `while`      |
|         6 | F    | `do-while`   |
|         7 | G    | `while`      |
|         8 | H    | `for`        |
|         9 | I    | `do-while`   |
|        10 | J    | `for`        |
|        11 | K    | `while`      |
|        12 | L    | `do-while`   |
|        13 | M    | `for`        |
|        14 | N    | `while`      |
|        15 | O    | `for`        |

---

# AKT III – Die Fertigkeits-Scrolls

## Thema

**Methoden**

---

## Story

Der Golem erreicht die Werkstatt von Meister Valerius.

Auf einer großen Steintafel steht ein langes Ritual.

Das Ritual funktioniert, ist aber unnötig lang.

Immer wieder werden dieselben Abläufe vollständig ausgeschrieben.

Der Seelenkern beginnt zu überhitzen.

Eine Inschrift erscheint:

> *„Mein Golem kennt viele Fertigkeiten.*
> *Doch ich habe jede Handlung immer wieder einzeln niedergeschrieben.*
> *Findet die Abläufe, die zusammengehören.*
> *Gebt ihnen einen Namen.*
> *Was einmal gelernt wurde, kann immer wieder verwendet werden.“*

---

# Rollen

### Spieler A – Seelenweber

Besitzt das vollständige Ritual.

### Spieler B – Meistermechaniker

Besitzt leere Fertigkeits-Scrolls.

---

# Rätsel 3.1 – Das große Ritual

## Material für Spieler A

```text
BEGINNE SEELENRITUAL

ENERGIE = 120
KRAFT = 80
KRISTALLE = 0
HERZENERGIE = 0
TOR_STATUS = GESCHLOSSEN

AUSGABE "Der Golem erwacht."

--------------------------------------------------
NORDTOR
--------------------------------------------------

GEHE ZUM NORDTOR

ENERGIE = ENERGIE - 10
RUNE_AKTIV = WAHR
SIEGELSTÄRKE = 30
ENERGIE = ENERGIE - SIEGELSTÄRKE
TOR_STATUS = OFFEN

KRISTALL_GEFUNDEN = 1
KRISTALLE = KRISTALLE + KRISTALL_GEFUNDEN
ENERGIE = ENERGIE + 15

--------------------------------------------------
OSTTOR
--------------------------------------------------

GEHE ZUM OSTTOR

ENERGIE = ENERGIE - 10
RUNE_AKTIV = WAHR
SIEGELSTÄRKE = 30
ENERGIE = ENERGIE - SIEGELSTÄRKE
TOR_STATUS = OFFEN

KRAFTVERLUST = 15
KRAFT = KRAFT - KRAFTVERLUST

KRISTALL_GEFUNDEN = 1
KRISTALLE = KRISTALLE + KRISTALL_GEFUNDEN
ENERGIE = ENERGIE + 15

--------------------------------------------------
SÜDTOR
--------------------------------------------------

GEHE ZUM SÜDTOR

ENERGIE = ENERGIE - 10
RUNE_AKTIV = WAHR
SIEGELSTÄRKE = 30
ENERGIE = ENERGIE - SIEGELSTÄRKE
TOR_STATUS = OFFEN

KRISTALL_GEFUNDEN = 1
KRISTALLE = KRISTALLE + KRISTALL_GEFUNDEN
ENERGIE = ENERGIE + 15

KRAFTVERLUST = 20
KRAFT = KRAFT - KRAFTVERLUST

--------------------------------------------------
HERZFEUER
--------------------------------------------------

HERZENERGIE = ENERGIE + KRISTALLE * 10

AUSGABE "Herzenergie:"
AUSGABE HERZENERGIE

ENDE SEELENRITUAL
```

---

# Rätsel 3.2 – Wiederkehrende Abläufe finden

Die Spieler sollen alle Abläufe finden, die mehrfach vorkommen und gemeinsam eine sinnvolle Aufgabe erfüllen.

---

## Ablauf A

```text
ENERGIE = ENERGIE - 10
RUNE_AKTIV = WAHR
SIEGELSTÄRKE = 30
ENERGIE = ENERGIE - SIEGELSTÄRKE
TOR_STATUS = OFFEN
```

### Frage

Welche Aufgabe erfüllt dieser Ablauf?

### Lösung

Der Ablauf **öffnet ein Tor**.

Möglicher Methodenname:

```text
OEFFNE_TOR
```

---

## Ablauf B

```text
KRISTALL_GEFUNDEN = 1
KRISTALLE = KRISTALLE + KRISTALL_GEFUNDEN
ENERGIE = ENERGIE + 15
```

### Frage

Welche Aufgabe erfüllt dieser Ablauf?

### Lösung

Der Ablauf **sammelt einen Kristall und erhöht die Energie**.

Möglicher Methodenname:

```text
SAMMLE_KRISTALL
```

---

# Rätsel 3.3 – Fertigkeits-Scrolls erstellen

Die Spieler erstellen zwei Scrolls.

## Scroll A

```text
NAME:
____________________________

AUFGABE:
____________________________

ENTHÄLT:

ENERGIE = ENERGIE - 10
RUNE_AKTIV = WAHR
SIEGELSTÄRKE = 30
ENERGIE = ENERGIE - SIEGELSTÄRKE
TOR_STATUS = OFFEN
```

### Lösung

```text
NAME:
OEFFNE_TOR

AUFGABE:
Öffnet ein Tor und verbraucht Energie.
```

---

## Scroll B

```text
NAME:
____________________________

AUFGABE:
____________________________

ENTHÄLT:

KRISTALL_GEFUNDEN = 1
KRISTALLE = KRISTALLE + KRISTALL_GEFUNDEN
ENERGIE = ENERGIE + 15
```

### Lösung

```text
NAME:
SAMMLE_KRISTALL

AUFGABE:
Sammelt einen Kristall und erhöht die Energie.
```

---

# Rätsel 3.4 – Das Ritual verkürzen

Die Spieler ersetzen die wiederkehrenden Abläufe durch ihre neuen Fertigkeiten.

## Nordtor

Vorher:

```text
GEHE ZUM NORDTOR

ENERGIE = ENERGIE - 10
RUNE_AKTIV = WAHR
SIEGELSTÄRKE = 30
ENERGIE = ENERGIE - SIEGELSTÄRKE
TOR_STATUS = OFFEN

KRISTALL_GEFUNDEN = 1
KRISTALLE = KRISTALLE + KRISTALL_GEFUNDEN
ENERGIE = ENERGIE + 15
```

Nachher:

```text
GEHE ZUM NORDTOR

____________________________

____________________________
```

### Lösung

```text
GEHE ZUM NORDTOR

OEFFNE_TOR

SAMMLE_KRISTALL
```

---

## Osttor

```text
GEHE ZUM OSTTOR

____________________________

KRAFTVERLUST = 15
KRAFT = KRAFT - KRAFTVERLUST

____________________________
```

### Lösung

```text
GEHE ZUM OSTTOR

OEFFNE_TOR

KRAFTVERLUST = 15
KRAFT = KRAFT - KRAFTVERLUST

SAMMLE_KRISTALL
```

---

## Südtor

```text
GEHE ZUM SÜDTOR

____________________________

____________________________

KRAFTVERLUST = 20
KRAFT = KRAFT - KRAFTVERLUST
```

### Lösung

```text
GEHE ZUM SÜDTOR

OEFFNE_TOR

SAMMLE_KRISTALL

KRAFTVERLUST = 20
KRAFT = KRAFT - KRAFTVERLUST
```

---

# Rätsel 3.5 – Eine Fertigkeit benötigt Werte

Auf einer weiteren Schriftrolle steht:

```text
FERTIGKEIT: BERECHNE_ENERGIE

BENÖTIGT:
ENERGIE
SIEGELSTÄRKE

BERECHNUNG:
ERGEBNIS = ENERGIE - SIEGELSTÄRKE

GIBT ZURÜCK:
ERGEBNIS
```

---

## Fragen

### 1. Welche Werte benötigt die Fertigkeit?

**Lösung:**

```text
ENERGIE
SIEGELSTÄRKE
```

### 2. Was berechnet sie?

**Lösung:**

```text
ENERGIE - SIEGELSTÄRKE
```

### 3. Was gibt sie zurück?

**Lösung:**

```text
ERGEBNIS
```

### 4. Was ergibt sich bei diesen Werten?

```text
ENERGIE = 100
SIEGELSTÄRKE = 30
```

**Lösung:**

```text
100 - 30 = 70
```

---

# Rätsel 3.6 – Rückgabewert verwenden

Vervollständigt:

```text
ENERGIE = 100
SIEGELSTÄRKE = 30

ENERGIE = __________________________

AUSGABE ENERGIE
```

### Lösung

```text
ENERGIE = BERECHNE_ENERGIE
```

Ausgabe:

```text
70
```

---

# Rätsel 3.7 – Abschlussrechnung

Startwerte:

```text
ENERGIE = 120
KRISTALLE = 0
```

Die Tor-Fertigkeit wird dreimal ausgeführt.

Jedes Tor verursacht:

```text
-10
-30
```

Die Kristall-Fertigkeit wird ebenfalls dreimal ausgeführt.

Jeder Kristall verursacht:

```text
KRISTALLE + 1
ENERGIE + 15
```

---

## Aufgabe

Berechnet:

```text
120
- 10
- 30
+ 15
- 10
- 30
+ 15
- 10
- 30
+ 15

= __________
```

### Lösung

```text
45
```

Also:

```text
ENERGIE = 45
```

---

## Kristalle

```text
KRISTALLE = 3
```

---

## Herzfeuer

```text
HERZENERGIE = ENERGIE + KRISTALLE * 10
```

Einsetzen:

```text
HERZENERGIE = 45 + 3 * 10
```

Ergebnis:

```text
HERZENERGIE = 75
```

### Code für Akt IV

```text
75
```

---

# AKT IV – Das Labyrinth der Entscheidungen

## Thema

**`if` / `else` und verschachtelte Bedingungen**

---

## Story

Der Golem erreicht das Herzfeuer.

Doch vor ihm liegt ein letztes Labyrinth.

An jeder Kreuzung befindet sich eine Entscheidungsrune.

Die Rune kennt nur zwei Antworten:

```text
WAHR
FALSCH
```

Die Antwort bestimmt den Weg:

```text
WAHR   → RECHTS
FALSCH → LINKS
```

---

# Rollen

## Oracle

Bleibt am Eingang.

Kennt die aktuellen Werte des Golems.

## Guardian

Betritt das Labyrinth.

Sieht die Entscheidungsrunen, kennt aber die Werte nicht.

---

# Startwerte

Der Oracle sieht:

```text
KRAFT       = 45
ENERGIE     = 70
TEMPERATUR  = 22
```

Der Guardian darf diese Werte nicht sehen.

---

# Entscheidungsregel

> **WENN** eine Aussage wahr ist, wird der rechte Weg gewählt.
>
> **SONST** wird der linke Weg gewählt.

---

# Station 1 – Das Tor der Energie

## Rune

> **WENN die Energie des Golems größer als 50 ist,**
>
> → RECHTS
>
> **SONST**
>
> → LINKS

## Wert

```text
ENERGIE = 70
```

## Lösung

```text
70 > 50
```

→ WAHR

→ **RECHTS**

---

# Station 2 – Die verschachtelte Kraftprüfung

## Rune

> **WENN die Temperatur größer als 20 ist:**
>
>   **WENN die Kraft mindestens 50 beträgt:**
>
>     → RECHTS
>
>   **SONST:**
>
>     → LINKS
>
> **SONST:**
>
>   → LINKS

## Werte

```text
TEMPERATUR = 22
KRAFT = 45
```

## Lösung

Äußere Bedingung:

```text
22 > 20
```

→ WAHR

Innere Bedingung:

```text
45 >= 50
```

→ FALSCH

→ **LINKS**

---

# Station 3 – Das Tor der Hitze

## Rune

> **WENN die Temperatur größer als 20 ist:**
>
>   **WENN die Temperatur kleiner als 25 ist:**
>
>     → LINKS
>
>   **SONST:**
>
>     → RECHTS
>
> **SONST:**
>
>   → RECHTS

## Wert

```text
TEMPERATUR = 22
```

## Lösung

```text
22 > 20
```

→ WAHR

Danach:

```text
22 < 25
```

→ WAHR

→ **LINKS**

---

# Station 4 – Das Tor der Erfahrung

## Rune

> **WENN die Kraft mindestens 50 beträgt:**
>
>   **WENN die Kraft mindestens 80 beträgt:**
>
>     → LINKS
>
>   **SONST:**
>
>     → RECHTS
>
> **SONST:**
>
>   → LINKS

## Wert

```text
KRAFT = 45
```

## Lösung

```text
45 >= 50
```

→ FALSCH

Der äußere `SONST`-Zweig wird ausgeführt.

→ **LINKS**

Die innere Bedingung wird nicht geprüft.

---

# Station 5 – Das Tor der Zeit

## Rune

> **WENN die Energie mindestens 50 beträgt:**
>
>   **WENN die Energie höchstens 80 beträgt:**
>
>     → RECHTS
>
>   **SONST:**
>
>     → LINKS
>
> **SONST:**
>
>   → RECHTS

## Wert

```text
ENERGIE = 70
```

## Lösung

```text
70 >= 50
```

→ WAHR

Dann:

```text
70 <= 80
```

→ WAHR

→ **RECHTS**

---

# Erster Lösungspfad

Die fünf Stationen ergeben:

| Station | Ergebnis | Richtung |
| ------: | -------- | -------- |
|       1 | WAHR     | Rechts   |
|       2 | FALSCH   | Links    |
|       3 | WAHR     | Links    |
|       4 | FALSCH   | Links    |
|       5 | WAHR     | Rechts   |

Der korrekte Pfad lautet:

```text
RECHTS
→ LINKS
→ LINKS
→ LINKS
→ RECHTS
```

---

# Falscher Weg

Eine falsche Entscheidung führt nicht sofort zu einer Sackgasse.

Der Spieler gelangt zu einer versiegelten Tür.

Auf ihr steht:

> *„Die Rune verweigert den Weg.“*
>
> *„Kehrt zum Anfang zurück.“*

Der Spieler muss zum Eingang zurückkehren und die Entscheidung erneut treffen.

---

# Dynamische Erweiterung

Optional können sich Werte während des Labyrinths verändern.

Beispiele:

### Feuerrune

```text
TEMPERATUR + 5
```

### Kristallquelle

```text
ENERGIE + 10
```

### Kraftquelle

```text
KRAFT + 15
```

### Eisrune

```text
TEMPERATUR - 5
```

Dadurch können sich spätere Entscheidungen verändern.

---

# Zweiter Durchlauf

Wenn der erste Spieler das Ziel erreicht, findet er dort neue Werte.

```text
KRAFT       = 68
ENERGIE     = 42
TEMPERATUR  = 31
```

Die Spieler wechseln die Rollen.

Der zweite Spieler betritt nun das Labyrinth.

Der erste Spieler kennt die neuen Werte und muss den zweiten Spieler durch das Labyrinth lotsen.

---

# Station 6 – Die letzte Entscheidung

Die letzte Rune ist stärker verschachtelt.

## Rune

> **WENN die Energie mindestens 40 beträgt:**
>
>   **WENN die Kraft größer als 60 ist:**
>
>     **WENN die Temperatur größer als 30 ist:**
>
>       → RECHTS
>
>     **SONST:**
>
>       → LINKS
>
>   **SONST:**
>
>     → LINKS
>
> **SONST:**
>
>   → RECHTS

## Werte

```text
ENERGIE = 42
KRAFT = 68
TEMPERATUR = 31
```

## Lösung

Erste Bedingung:

```text
42 >= 40
```

→ WAHR

Zweite Bedingung:

```text
68 > 60
```

→ WAHR

Dritte Bedingung:

```text
31 > 30
```

→ WAHR

Damit:

**RECHTS**

---

# Abschluss von Akt IV

Hinter dem letzten Tor befindet sich das Herzfeuer.

Eine letzte Inschrift erscheint:

> *„Ihr habt gelernt, dass ein Golem nicht nur handeln muss.*
> *Er muss entscheiden können.*
> *Eine Bedingung führt zu einer Entscheidung.*
> *Eine Entscheidung führt zu einem Weg.“*

Erst jetzt wird die Sprache der Runen übersetzt:

| Runensprache | Java    |
| ------------ | ------- |
| WENN         | `if`    |
| SONST        | `else`  |
| WAHR         | `true`  |
| FALSCH       | `false` |

---

# FINALE – Die vollständige Seele

Die vier Prüfungen haben jeweils einen Teil der Seele des Golems wiederhergestellt.

## 1. Essenz

Der Golem besitzt Werte.

**Variablen und Datentypen**

```text
Name
Energie
Mana
Aktiviert
Blickrichtung
Schritte
```

## 2. Rhythmus

Der Golem kann Handlungen wiederholen.

**Schleifen**

```text
while
do-while
for
```

## 3. Fertigkeiten

Der Golem kann Aufgaben bündeln und wiederverwenden.

**Methoden**

```text
OEFFNE_TOR
SAMMLE_KRISTALL
BERECHNE_ENERGIE
```

## 4. Entscheidungen

Der Golem kann abhängig von seinem Zustand unterschiedliche Handlungen ausführen.

**If / Else**

```text
WENN ...
SONST ...
```

---

# Finale Aufgabe

Der Golem muss das Herzfeuer aktivieren.

Die Spieler erhalten eine letzte Anweisung:

> *„Nutze alles, was du gelernt hast.*
>
> *Trage deine Werte in den Seelenkern ein.*
>
> *Lass den Golem den Weg durch das Herzfeuer finden.*
>
> *Wiederhole seine Bewegungen, solange es nötig ist.*
>
> *Nutze seine Fertigkeiten.*
>
> *Und entscheide an jeder Barriere anhand seines aktuellen Zustands.“*

Die Spieler müssen dabei die vier bisher erlernten Konzepte miteinander verbinden.

---

# Abschluss

Das Herzfeuer erwacht.

Die Zahnräder der Schmiede beginnen sich wieder zu drehen.

Magische Energie fließt durch die Kupferrohre.

Der Golem öffnet seine Augen.

Auf der letzten Steintafel erscheint:

> **„Die Seele ist gebunden.“**
>
> **„Der Wächter ist erwacht.“**
>
> **„Aethelgard ist gerettet.“**

Die Spieler erhalten den Titel:

## Meister-Seelenweber

---

# Lösungsschlüssel – Gesamtübersicht

| Akt | Rätsel              | Lösung                                     |
| --- | ------------------- | ------------------------------------------ |
| I   | Eigenschaft → Gefäß | Name → Pergament                           |
| I   | Eigenschaft → Gefäß | Lebensenergie → Eisenkiste                 |
| I   | Eigenschaft → Gefäß | Mana → Kristallflasche                     |
| I   | Eigenschaft → Gefäß | Aktiviert → Lichtkugel                     |
| I   | Eigenschaft → Gefäß | Blickrichtung → Runenstein                 |
| I   | Eigenschaft → Gefäß | Schritte → Eisenkiste                      |
| I   | Essenzen            | `"Nox"`, `125`, `3.5`, `true`, `'O'`, `17` |
| II  | Langer Gang         | `while`                                    |
| II  | Wand links          | `while`                                    |
| II  | Druckschalter       | `do-while`                                 |
| II  | Fünf Schritte       | `for`                                      |
| II  | Wand rechts         | `while`                                    |
| II  | Magische Brücke     | `do-while`                                 |
| II  | Fackeln             | `while`                                    |
| II  | Drei Runensteine    | `for`                                      |
| II  | Nebelgang           | `do-while`                                 |
| II  | Sieben Kristalle    | `for`                                      |
| II  | Verschlossenes Tor  | `while`                                    |
| II  | Magische Rune       | `do-while`                                 |
| II  | Vier Wächter        | `for`                                      |
| II  | Ausgang im Nebel    | `while`                                    |
| II  | Letzte Passage      | `for`                                      |
| III | Wiederholung A      | `OEFFNE_TOR`                               |
| III | Wiederholung B      | `SAMMLE_KRISTALL`                          |
| III | Ritual verkürzen    | Methoden einsetzen                         |
| III | Berechnung          | `100 - 30 = 70`                            |
| III | Rückgabewert        | `ENERGIE = BERECHNE_ENERGIE`               |
| III | Abschlussenergie    | `45`                                       |
| III | Kristalle           | `3`                                        |
| III | Herzenergie         | `75`                                       |
| IV  | Energie             | Rechts                                     |
| IV  | Kraftprüfung        | Links                                      |
| IV  | Hitze               | Links                                      |
| IV  | Erfahrung           | Links                                      |
| IV  | Zeit                | Rechts                                     |
| IV  | Letzte Entscheidung | Rechts                                     |

---

# 9. Materialübersicht

Für die praktische Umsetzung werden mindestens benötigt:

## Akt I

* 6 Eigenschaftsrunen
* 5 Seelengefäße
* 7 magische Essenzen
* Zuordnungstafel
* Übersetzungstafel mit Java-Datentypen
* Golem-/Seelenkern-Asset

## Akt II

* Labyrinthkarte
* Golem-Figur
* 15 Situationskarten bzw. Labyrinthstationen
* 15 Schleifenrunen
* Wand-, Kristall-, Fackel-, Tor- und Wächter-Assets
* Lösungskarte für die Spielleitung

## Akt III

* Großes Ritual als Spieler-A-Dokument
* 2 leere Fertigkeits-Scrolls
* Material zum Markieren der wiederkehrenden Abschnitte
* Schriftrolle `BERECHNE_ENERGIE`
* Abschlussrechnung
* Lösungsschlüssel

## Akt IV

* Labyrinthkarte
* Entscheidungsrunen
* Wertekarte für das Oracle
* zweite Wertekarte
* mehrere Abzweigungen
* falsche Wege / Rückkehr zum Start
* optionale Ereigniskarten zur Veränderung der Werte
* finale verschachtelte Entscheidungsrune

## Finale

* Herzfeuer-Asset
* finale Aktivierung
* Abschlussinschrift
* optional Zertifikat / Titel „Meister-Seelenweber“

---

# 10. Zeitplanung

| Abschnitt          |      Richtwert |
| ------------------ | -------------: |
| Einführung         |      5 Minuten |
| Akt I – Variablen  |     10 Minuten |
| Akt II – Schleifen |     15 Minuten |
| Akt III – Methoden |     15 Minuten |
| Akt IV – If / Else |     10 Minuten |
| Finale             |      5 Minuten |
| **Gesamt**         | **60 Minuten** |

Die Rätsel sind so konzipiert, dass einzelne Unteraufgaben bei Zeitmangel übersprungen werden können. Besonders Akt II enthält mit 15 Stationen mehr Material als zwingend notwendig, sodass die Spielleitung je nach verfügbarer Zeit eine Auswahl treffen kann.
