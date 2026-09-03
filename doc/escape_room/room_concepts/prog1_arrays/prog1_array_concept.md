# Programmieren 2 DEER - Dungeon GPT

Digital Educational Escape Room für das Fach Programmieren 1 im ersten Semester Informatik. Der Escape Room soll ein Praktikum sein bei denen sich die Studenten mit den Konzepten von Arrays vertraut machen, die sie zuvor in der Vorlesung kennen gelernt haben.

## Inhalte

Im Verlauf des Escape Rooms sollen die Studierenden praktisch mit folgenden Inhalten arbeiten:

- Arrays erstellen
- Arrays füllen
- Elemente löschen bzw. als leer markieren
- Arrays iterieren
- Elemente mit `for-each` zählen
- Arrays sortieren
- Bubble Sort implementieren
- 2D-Arrays erstellen und verwenden
- über 2D-Arrays iterieren

## Vorgaben

Der Escape Room soll in einem 2D Spiel in Pixelartgrafik mit Perspektive von vorne schräg oben umgesetzt werden. Das Spiel ist auf einem Raster aus Kacheln aufgebaut. Texturen sind frei anpassbar.

Die Studenten sollen die Konzepte möglichst anfassen können, also mit Ihnen interagieren oder Konzepte und Veränderungen direkt in der Spielwelt dargestellt sehen.

Das Setting des Escape Rooms sollte so gewählt sein, dass sich die Studenten in die Welt und das Setting hineinfühlen können und eine gute Motivation haben. Programmieraufgaben müssen nicht in Java sein sondern können passend zum Setting abstrahiert werden. Das Setting sollte nah am Informatikkontext sein.

## Konzept

Spieler können die Arrays im Spiel sehen, als Elemente die im Level liegen, die sie bewegen, entnehmen und sortieren können. Sie müssen die entsprechenden Aufgaben mit den Arrays im Spiel lösen und als Code für die Tür müssen sie den entsprechenden Java Code der das Array lösen würde eingeben um in den nächsten Raum zu gelangen.
Arrays sollen verschiedene Arten von Datentypen enthalten um zu vermitteln das diese aus verschiedenen Typen erstellt werden können.

Idee: Die Spieler programmieren Elemente die daraufhin im Spiel auftauchen.
- Wir beginnen damit das die Spieler Zahlen benötigen die sie in ein Feld im Spiel legen können.
- Die Spieler fügen die Elemente hinzu.


# Digital Educational Escape Room – „System Recovery“

## 1. Grundidee

Die Studierenden betreten eine beschädigte Informatik-Forschungsstation. Das zentrale System ist ausgefallen und mehrere Subsysteme müssen wiederhergestellt werden.

Die Spieler sind im Computer und müssen diesen reparieren. Vor dem Rechner sitzt ein User und promted GPT. Die Spieler erhalten die Prompts und müssen die Antworten liefern.

UI sieht aus wie ein LLM Chat Fenster.

Die zentrale Spielmechanik lautet:

> **„Was du programmierst, wird in der Spielwelt sichtbar.“**

Arrays werden nicht nur im Code dargestellt, sondern als konkrete Objekte in der Spielwelt.

Beispiel:

```java
int[] energie = {40, 10, 80, 30, 60};
```

führt dazu, dass fünf Energiekristalle mit den Werten `40`, `10`, `80`, `30` und `60` im entsprechenden Array-Speicher erscheinen.

Die Studierenden wechseln daher ständig zwischen:

1. **Code schreiben**
2. **Auswirkung in der Spielwelt beobachten**
3. **Objekte manipulieren**
4. **Ergebnis interpretieren**
5. **Java-Code für das nächste Schloss formulieren**

---

# 2. Lernziele

Am Ende des Escape Rooms sollen die Studierenden praktisch mit folgenden Konzepten gearbeitet haben:

* Arrays erstellen
* Arrays mit Werten füllen
* Array-Indizes verwenden
* Array-Elemente verändern
* Elemente als `null` markieren
* Array-Länge bestimmen
* Arrays mit `for` durchlaufen
* Arrays mit `for-each` durchlaufen
* Elemente zählen
* Arrays sortieren
* Bubble Sort implementieren
* 2D-Arrays erstellen
* auf Elemente eines 2D-Arrays zugreifen
* 2D-Arrays mit verschachtelten Schleifen durchlaufen

---

# 3. Übergreifende Spielmechanik

## 3.1 Der Array-Compiler

Zu Beginn erhalten die Studierenden Zugriff auf ein Terminal.

Dort können sie Java-Code eingeben.

Beispiel:

```java
int[] energie = new int[5];
```

Nach der Ausführung erscheint in der Spielwelt ein Array-Speicher mit fünf Slots:

```text
┌────┬────┬────┬────┬────┐
│  0 │  1 │  2 │  3 │  4 │
├────┼────┼────┼────┼────┤
│    │    │    │    │    │
└────┴────┴────┴────┴────┘
```

Wird anschließend geschrieben:

```java
energie[0] = 40;
energie[1] = 10;
energie[2] = 80;
energie[3] = 30;
energie[4] = 60;
```

erscheinen dort die entsprechenden Energiekristalle.

---

# 4. Raum 1 – Die Materialisierungskammer

## Lernziel

* Arrays erstellen
* Array-Größe festlegen
* Arrays über Indizes befüllen

---

## Situation

Die Studierenden betreten einen Raum mit einer defekten Materialisierungsmaschine.

Ein Terminal zeigt:

```text
MATERIALISIERUNGSSYSTEM OFFLINE

Kein Energie-Array gefunden.

Erforderliche Kapazität:
5 Speicherplätze
```

Daneben befindet sich ein leerer Array-Speicher.

---

## Rätsel 1.1 – Array erstellen

Die Studierenden müssen ein Array mit fünf Speicherplätzen erstellen.

Gesuchte Lösung:

```java
int[] energie = new int[5];
```

Nach erfolgreicher Ausführung erscheinen fünf leere Speicherplätze.

```text
[ 0 ] [ 1 ] [ 2 ] [ 3 ] [ 4 ]
```

---

## Rätsel 1.2 – Array füllen

Das Terminal zeigt:

```text
Benötigte Energiewerte:

40
10
80
30
60
```

Die Studierenden müssen die Werte an den richtigen Positionen eintragen.

```java
energie[0] = 40;
energie[1] = 10;
energie[2] = 80;
energie[3] = 30;
energie[4] = 60;
```

In der Welt erscheinen:

```text
[40] [10] [80] [30] [60]
```

---

## Rätsel 1.3 – Index verstehen

Das System gibt anschließend einen Hinweis:

```text
Die Tür benötigt die Energie aus Speicherplatz 2.
```

Die Studierenden müssen erkennen, dass Speicherplatz 2 den Wert `80` enthält.

Das Türterminal akzeptiert beispielsweise:

```java
energie[2]
```

oder eine entsprechende Zuweisung, abhängig davon, wie das Schloss gestaltet wird.

---

## Ausgang

Die erste Tür öffnet sich.

---

# 5. Raum 2 – Der defekte Modulspeicher

## Lernziel

* Arrays mit anderen Datentypen verwenden
* Elemente verändern
* `null` verstehen
* Unterschied zwischen Array-Größe und belegten Elementen verstehen

---

## Situation

Im nächsten Raum befindet sich ein beschädigter Computerspeicher.

Das System benötigt fünf Module.

Die Module sind:

```text
CPU
RAM
GPU
SSD
NETWORK
```

---

## Rätsel 2.1 – String-Array erzeugen

Die Studierenden erzeugen:

```java
String[] module = new String[5];
```

Danach füllen sie das Array:

```java
module[0] = "CPU";
module[1] = "RAM";
module[2] = "GPU";
module[3] = "SSD";
module[4] = "NETWORK";
```

In der Spielwelt erscheinen fünf Modul-Chips.

---

## Rätsel 2.2 – Defektes Modul entfernen

Das System meldet:

```text
GPU beschädigt.

GPU darf nicht weiter verwendet werden.
```

Die Studierenden müssen das Element als leer markieren.

```java
module[2] = null;
```

Der GPU-Chip verschwindet.

Der Speicher sieht jetzt so aus:

```text
[CPU] [RAM] [   ] [SSD] [NETWORK]
```

Wichtig:

Das Array besitzt weiterhin fünf Speicherplätze.

Nur eines der Elemente ist `null`.

---

## Rätsel 2.3 – Array-Größe

Das System fragt:

> „Wie viele Speicherplätze besitzt der Modulspeicher?“

Die Studierenden müssen `module.length` verwenden.

```java
module.length
```

Ergebnis:

```text
5
```

---

# 6. Raum 3 – Der Inventarscanner

## Lernziel

* Arrays iterieren
* `for-each` verwenden
* Elemente zählen
* `null` berücksichtigen

---

## Situation

Ein Scanner soll feststellen, wie viele funktionierende Module noch vorhanden sind.

Aktueller Zustand:

```text
[CPU] [RAM] [   ] [SSD] [NETWORK]
```

---

## Rätsel 3.1 – Aktive Module zählen

Die Studierenden erhalten die Aufgabe:

> Zähle alle Module, die nicht `null` sind.

Es tauchen verschiedene Arrays auf, die die Spieler scannen müssen. Und zum Beispiel die Elemente einer Art zählen müssen oder kaputte Elemente löschen müssen

Zähle wie viele R in einem String sind. Beispiel: Strawberry

Counter visualisieren

Eine mögliche Lösung:

```java
int count = 0;

for (String m : module) {
    if (m != null) {
        count++;
    }
}
```

---

## Darstellung im Spiel

Der Scanner läuft nacheinander über die Elemente:

```text
CPU       → erkannt
RAM       → erkannt
null      → übersprungen
SSD       → erkannt
NETWORK   → erkannt
```

Am Ende:

```text
AKTIVE MODULE: 4
```

---

## Rätsel 3.2 – Das Scanner-Schloss

Die Tür verlangt:

> „Wie viele Module sind aktiv?“

Lösung:

```text
4
```

Die Tür öffnet sich.

---

# 7. Raum 4 – Das Transportlager

## Lernziel

* klassische `for`-Schleife
* Indexvariable
* Zugriff auf Array-Elemente
* `array.length`

---

## Situation

Ein Transportroboter muss fünf Datenpakete aufnehmen.

Array:

```java
int[] pakete = {15, 40, 20, 60, 30};
```

Die Pakete erscheinen auf fünf Förderbändern.

```text
[15] [40] [20] [60] [30]
```

---

## Rätsel

Der Roboter kann nur über einen Index gesteuert werden.

Die Studierenden müssen alle Pakete nacheinander übertragen.

Roboter löscht Elemente oder fügt welche hinzu. Länge des Arrays ändert sich und die Schleife muss auf die Längenänderung reagieren können. Deswegen for

Beispiel:

```java
for (int i = 0; i < pakete.length; i++) {
    roboter.aufnehmen(pakete[i]);
}
```

Im Spiel sieht man:

```text
i = 0 → Paket 15
i = 1 → Paket 40
i = 2 → Paket 20
i = 3 → Paket 60
i = 4 → Paket 30
```

---

## Lernmoment

Die Studierenden sehen unmittelbar:

```text
i
↓
Position im Array
```

und:

```text
pakete[i]
↓
Wert an dieser Position
```

---

# 8. Raum 5 – Der chaotische Datenspeicher

## Lernziel

* Arrays sortieren
* Ordnung von Daten verstehen
* Elemente vergleichen und verschieben

---

## Situation

Ein Datenspeicher enthält unsortierte Sicherheitsdaten:

```text
[73] [12] [45] [8] [31]
```

Das Sicherheitssystem funktioniert nur mit aufsteigend sortierten Daten.

---

## Rätsel 5.1 – Manuelles Sortieren

Die Studierenden dürfen die Datenkristalle in der Welt bewegen.

Sie müssen daraus machen:

```text
[8] [12] [31] [45] [73]
```

Dabei sehen sie die Veränderung des Arrays unmittelbar.

---

## Rätsel 5.2 – Sortiercode

Danach fordert das Terminal:

> „Welche Operation muss das Programm durchführen, damit die Daten automatisch sortiert werden?“

Die Studierenden sollen nun die manuelle Sortierung in einen Algorithmus übertragen.

---

# 9. Raum 6 – Die Bubble-Sort-Maschine

## Lernziel

* Bubble Sort verstehen
* verschachtelte Schleifen
* benachbarte Elemente vergleichen
* Elemente tauschen
* temporäre Variable verwenden

---

## Situation

Der Spieler soll ein Array mit Bubblesort sortieren. Er muss den Algorithmus finden, die richtigen Schritte ausgeben, und das Array im richtigen Ablauf sortieren.

Die Studierenden finden eine große mechanische Sortiermaschine.

Sie enthält:

```text
[42] [17] [8] [31] [23]
```

Die Maschine kann jeweils zwei benachbarte Elemente vergleichen.

---

## Rätsel 6.1 – Vergleich

Die Maschine zeigt:

```text
42     17
│      │
└──┬───┘
   ↓
42 > 17
```

Sie fordert:

> „Was muss passieren?“

Die Studierenden wählen:

```text
TAUSCHEN
```

Danach:

```text
[17] [42] [8] [31] [23]
```

---

## Rätsel 6.2 – Nicht tauschen

Die Maschine zeigt:

```text
8     42
```

Da:

```text
8 < 42
```

werden die Werte nicht vertauscht.

---

## Rätsel 6.3 – Algorithmus erkennen

Die Studierenden müssen erkennen:

1. Elemente vergleichen
2. Wenn links größer als rechts → tauschen
3. Mehrfach über das Array laufen
4. Wiederholen, bis das Array sortiert ist

---

## Rätsel 6.4 – Bubble Sort programmieren

Die Studierenden erhalten einen vorbereiteten Code:

```java
for (int i = 0; i < array.length - 1; i++) {

    for (int j = 0; j < array.length - 1 - i; j++) {

        if (__________________) {

            int temp = array[j];
            array[j] = array[j + 1];
            array[j + 1] = temp;
        }
    }
}
```

Gesucht:

```java
array[j] > array[j + 1]
```

---

## Darstellung

Während der Ausführung werden die gerade verglichenen Elemente hervorgehoben.

Beispiel:

```text
[17] [42] [8] [31] [23]
       ↑    ↑
     Vergleich
```

Nach einem Tausch:

```text
[17] [8] [42] [31] [23]
```

Damit wird Bubble Sort Schritt für Schritt sichtbar.

---

# 10. Raum 7 – Das Datenarchiv

## Lernziel

* verschiedene Datentypen
* Arrays für unterschiedliche Arten von Daten

---

## Situation

Im Archiv liegen verschiedene Datensätze.

Die Studierenden müssen die passenden Arrays erzeugen.

---

## Energie

```java
int[] energie = {20, 50, 80};
```

In der Welt:

```text
[20] [50] [80]
```

---

## Module

```java
String[] module = {"CPU", "GPU", "RAM"};
```

In der Welt erscheinen drei Modul-Chips.

---

## Status

```java
boolean[] aktiv = {true, false, true};
```

In der Welt:

```text
[GRÜN] [ROT] [GRÜN]
```

---

## Rätsel

Die Studierenden erhalten verschiedene Datensätze und müssen entscheiden:

```text
Zahlen      → int[]
Text        → String[]
Ja/Nein     → boolean[]
```

Anschließend müssen sie die passenden Arrays programmieren.

---

# 11. Raum 8 – Der zweidimensionale Speicher

## Lernziel

* 2D-Arrays erstellen
* Zeilen und Spalten verstehen
* auf Elemente mit zwei Indizes zugreifen

---

## Situation

Die Studierenden betreten einen Lagerraum.

Der Boden ist ein Raster:

```text
       0     1     2     3
    ┌─────┬─────┬─────┬─────┐
 0  │     │     │     │     │
    ├─────┼─────┼─────┼─────┤
 1  │     │     │     │     │
    ├─────┼─────┼─────┼─────┤
 2  │     │     │     │     │
    └─────┴─────┴─────┴─────┘
```

Das Spielfeld wird durch ein 2D-Array beschrieben.

Die Spieler müssen die größe des Arrays selber Zählen anhand der Raumgröße.

---

## Rätsel 8.1 – 2D-Array erstellen

Die Studierenden erstellen:

```java
int[][] lager = new int[3][4];
```

Im Spiel erscheint ein Raster mit:

```text
3 Zeilen
4 Spalten
```

---

## Rätsel 8.2 – Objekte platzieren

Das System gibt vor:

```text
Batterie → Zeile 0, Spalte 2
Schlüssel → Zeile 1, Spalte 3
Computer → Zeile 2, Spalte 1
```

Die Studierenden schreiben beispielsweise:

```java
lager[0][2] = 1;
lager[1][3] = 2;
lager[2][1] = 3;
```

Daraufhin erscheinen die Objekte genau an diesen Positionen.

---

## Aha-Moment

Die Studierenden sehen:

```java
lager[1][3]
```

und gleichzeitig in der Spielwelt:

```text
       0    1    2    3
    ┌────┬────┬────┬────┐
 0  │    │    │    │    │
    ├────┼────┼────┼────┤
 1  │    │    │    │ 🔑 │
    ├────┼────┼────┼────┤
 2  │    │ 💻 │    │    │
    └────┴────┴────┴────┘
```

Der Zusammenhang zwischen:

```text
Zeile + Spalte
```

und:

```text
lager[zeile][spalte]
```

wird unmittelbar sichtbar.

---

# 12. Raum 9 – Der Suchroboter

## Lernziel

* 2D-Arrays iterieren
* verschachtelte `for`-Schleifen
* Zeilen und Spalten durchlaufen
* Werte innerhalb eines Rasters suchen

---

## Situation

Ein Roboter muss alle Batterien im Lager finden.

Das Array:

```java
int[][] map = {
    {0, 0, 1, 0},
    {0, 2, 0, 0},
    {0, 0, 0, 3},
    {1, 0, 0, 0}
};
```

Dabei bedeutet:

```text
0 = leer
1 = Batterie
2 = Schlüssel
3 = Computer
```

---

## Rätsel 9.1 – Das gesamte Raster durchsuchen

Der Roboter soll jede einzelne Kachel überprüfen.

Die Studierenden müssen eine verschachtelte Schleife erstellen:

```java
for (int i = 0; i < map.length; i++) {
    for (int j = 0; j < map[i].length; j++) {

        // Kachel untersuchen

    }
}
```

---

## Darstellung im Spiel

Der Roboter untersucht:

```text
(0,0)
(0,1)
(0,2) ← Batterie
(0,3)

(1,0)
(1,1)
(1,2)
(1,3)

...
```

Die jeweils untersuchte Kachel wird hervorgehoben.

Findet der Roboter:

```java
map[i][j] == 1
```

wird die Batterie eingesammelt.

---

# 13. Raum 10 – Das zentrale Rechenzentrum

## Lernziel

Alle bisher behandelten Konzepte kombinieren.

---

## Situation

Das zentrale System der Forschungsstation ist weiterhin gesperrt.

Auf einem großen Bildschirm steht:

```text
SYSTEM RECOVERY

3 Subsysteme müssen repariert werden.
```

---

# 14. Finales Rätsel – Energieversorgung

Das System enthält:

```java
int[] energie = {73, 12, 45, 8, 31};
```

Die Energieversorgung funktioniert nur, wenn die Werte sortiert sind.

Die Studierenden müssen Bubble Sort implementieren.

Erwarteter Zustand:

```text
[8] [12] [31] [45] [73]
```

Nach erfolgreicher Sortierung wird das erste Subsystem aktiviert.

---

# 15. Finales Rätsel – Modulkontrolle

Das zweite Subsystem verwendet:

```java
String[] module = {
    "CPU",
    "RAM",
    null,
    "SSD",
    "NETWORK"
};
```

Die Studierenden müssen mit `for-each` die aktiven Module zählen.

```java
int count = 0;

for (String module : modules) {
    if (module != null) {
        count++;
    }
}
```

Ergebnis:

```text
4 aktive Module
```

---

# 16. Finales Rätsel – Rechenzentrumskarte

Das letzte Subsystem verwendet ein 2D-Array:

```java
int[][] map = {
    {0, 0, 0, 1},
    {0, 2, 0, 0},
    {0, 0, 3, 0},
    {1, 0, 0, 0}
};
```

Die Studierenden müssen mit verschachtelten Schleifen alle Batterien finden.

```java
for (int i = 0; i < map.length; i++) {
    for (int j = 0; j < map[i].length; j++) {

        if (map[i][j] == 1) {
            // Batterie gefunden
        }
    }
}
```

Der Roboter sammelt beide Batterien ein.

---

# 17. Das finale Türschloss

Nach erfolgreicher Reparatur erzeugt jedes Subsystem einen Teil des Zugangscodes.

Beispielsweise:

```text
┌─────────────────────────────┐
│       SYSTEM RECOVERY       │
├─────────────────────────────┤
│ Energie:       8 12 31 45 73│
│ Module:                 4   │
│ Batterien:              2   │
│ Schlüsselposition:      13  │
└─────────────────────────────┘
```

Das Türsystem fordert nun:

> **„Gib den Java-Code ein, der den finalen Systemzustand erzeugt.“**

Die Studierenden müssen die entscheidenden Codefragmente aus den vorherigen Rätseln kombinieren.

---

# 18. Didaktischer Ablauf des gesamten Escape Rooms

```text
                    START
                      │
                      ▼
             ┌─────────────────┐
             │ 1. Array        │
             │    erstellen    │
             └────────┬────────┘
                      │
                      ▼
             ┌─────────────────┐
             │ 2. Array        │
             │    füllen       │
             └────────┬────────┘
                      │
                      ▼
             ┌─────────────────┐
             │ 3. Elemente     │
             │    verändern    │
             └────────┬────────┘
                      │
                      ▼
             ┌─────────────────┐
             │ 4. null / leer  │
             └────────┬────────┘
                      │
                      ▼
             ┌─────────────────┐
             │ 5. Iterieren    │
             │    for          │
             └────────┬────────┘
                      │
                      ▼
             ┌─────────────────┐
             │ 6. for-each     │
             │    zählen       │
             └────────┬────────┘
                      │
                      ▼
             ┌─────────────────┐
             │ 7. Sortieren    │
             └────────┬────────┘
                      │
                      ▼
             ┌─────────────────┐
             │ 8. Bubble Sort  │
             └────────┬────────┘
                      │
                      ▼
             ┌─────────────────┐
             │ 9. 2D-Array     │
             └────────┬────────┘
                      │
                      ▼
             ┌─────────────────┐
             │10. 2D-Iteration │
             └────────┬────────┘
                      │
                      ▼
             ┌─────────────────┐
             │   FINAL SYSTEM  │
             │    RECOVERY     │
             └────────┬────────┘
                      │
                      ▼
                   ESCAPE
```

---

# 19. Wichtiges Designprinzip für jedes Rätsel

Jedes Rätsel sollte idealerweise aus **vier Phasen** bestehen:

### Phase A – Programmieren

Die Studierenden schreiben beispielsweise:

```java
int[] energie = new int[5];
```

### Phase B – Materialisieren

Das Array erscheint in der Spielwelt:

```text
[ ] [ ] [ ] [ ] [ ]
```

### Phase C – Manipulieren

Die Studierenden sehen oder verändern die Elemente:

```text
[40] [10] [80] [30] [60]
```

### Phase D – Programmier-Schloss

Das Spiel verlangt schließlich die entsprechende Java-Lösung:

```java
energie[2] = 80;
```

Damit entsteht eine wiederkehrende Lernschleife:

> **Code → Datenstruktur → sichtbare Welt → Problem → Code**

Genau diese Schleife würde ich zum zentralen Markenzeichen des gesamten Escape Rooms machen.
