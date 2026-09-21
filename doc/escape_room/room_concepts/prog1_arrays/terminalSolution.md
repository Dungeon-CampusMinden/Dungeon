git p# Musterlösungen - System Recovery

Dieses Dokument beschreibt die Lösungen in der Reihenfolge, in der sie aktuell in
`TerminalInterpreterSetup` registriert sind. Code aus vorherigen Schritten darf im Editor stehen
bleiben; der Interpreter prüft immer die aktuelle Anforderung plus bereits gelöste Schritte.

## Grundregeln

- Leerzeilen und Zeilenkommentare mit `//` werden ignoriert.
- Mehrere Statements dürfen in einer Editorzeile stehen, wenn sie mit `;` getrennt sind.
- Bei ungeordneten Steps ist die Reihenfolge der erwarteten Codezeilen egal.
- Bei geordneten Steps muss die inhaltliche Reihenfolge stimmen.
- Alternative Array-Schreibweisen wie `int[] a` und `int a[]` werden akzeptiert.
- Schleifen dürfen `i++`, `++i`, `i += 1` oder `i = i + 1` verwenden.
- Für vollständige Array-Iterationen sind `< array.length` und `<= array.length - 1` erlaubt.
- Für Bubble-Sort-Grenzen sind `< array.length - 1` und `<= array.length - 2` erlaubt.
- Flexible Variablennamen sind nur dort erlaubt, wo das Rätsel keine festen Namen verlangt.
  Danach muss derselbe Name konsequent weiterverwendet werden.

## Rätsel 1 - Die Materialisierungskammer

### Step 1: Energie-Array erstellen

Minimal:

```java
int[] energie = new int[5];
```

Alternative:

```java
int energie[] = new int[5];
```

Erklärung: Es muss ein `int`-Array mit exakt 5 Plätzen unter dem Namen `energie` erstellt werden.

### Step 2: Energie-Werte setzen

Minimal:

```java
energie[0] = 40;
energie[1] = 10;
energie[2] = 80;
energie[3] = 30;
energie[4] = 60;
```

Alternative mit anderer Reihenfolge:

```java
energie[4] = 60;
energie[2] = 80;
energie[0] = 40;
energie[3] = 30;
energie[1] = 10;
```

Erklärung: Alle fünf Indizes müssen mit den passenden Werten belegt sein. Die Reihenfolge ist
egal, weil jede Zuweisung für sich eindeutig ist.

## Rätsel 2 - Der defekte Modulspeicher

### Step 1: Modul-Array erstellen

Minimal:

```java
String[] module = new String[5];
```

Alternative:

```java
String m[] = new String[5];
```

Erklärung: Der Arrayname ist flexibel. Wenn hier z.B. `m` verwendet wird, müssen alle folgenden
Modul-Steps ebenfalls `m` verwenden.

### Step 2: Module einsetzen

Minimal:

```java
dw
```

Alternative mit flexiblem Namen und gemischter Formatierung:

```java
m[3] = "SSD";
m[4] = "NETWORK";
m[0] = "CPU"; m[1] = "RAM";
m[2] = "GPU";
```

Erklärung: Die Reihenfolge ist egal. Wichtig ist, dass alle Werte am richtigen Index landen und
der in Step 1 gewählte Arrayname weiterverwendet wird.

### Step 3: GPU entfernen

```java
module[2] = null;
```

Bei flexiblem Namen:

```java
m[2] = null;
```

Erklärung: Der Eintrag an Index 2 wird als leer markiert.

### Step 4: Array-Länge auslesen

```java
module.length;
```

Bei flexiblem Namen:

```java
m.length;
```

Erklärung: Es wird die feste Länge des Arrays ausgelesen, nicht die Anzahl der belegten Plätze.

## Rätsel 3 - Inventarscanner

```java
int count = 0;

for (String entry : module) {
    if (entry != null) {
        count++;
    }
}
```

Bei flexiblem Arraynamen aus Rätsel 2:

```java
int count = 0;

for (String item : m) {
    if (item != null) {
        count += 1;
    }
}
```

Erklärung: Der Schleifenvariablenname ist flexibel. Er muss aber in der `if`-Bedingung konsistent
verwendet werden. `count++`, `++count`, `count += 1` und `count = count + 1` sind erlaubt.

## Rätsel 4 - Das Transportlager

### Step 1: Pakete erstellen

Minimal:

```java
int[] pakete = {15, 40, 20, 60, 30};
```

Alternative:

```java
int pakete[] = {15, 40, 20, 60, 30};
```

### Step 2: Pakete einsammeln

Minimal:

```java
a
```

Alternativen:

```java
for (int index = 0; index <= pakete.length - 1; index += 1) {
    roboter.collect(pakete[index]);
}
```

```java
for (int packageIndex = 0; packageIndex < pakete.length; ++packageIndex) {
    roboter.collect(pakete[packageIndex]);
}
```

Erklärung: Der Indexname ist flexibel, muss aber im Arrayzugriff identisch sein. Der Roboter muss
`collect` mit dem aktuellen Paketwert verwenden. Die fünf Paketwerte müssen in der vorgegebenen
Reihenfolge im Array stehen.

## Rätsel 5 - Der chaotische Datenspeicher

Dieses Rätsel verwendet keine Terminal-Eingabe. Die Studierenden entscheiden an der
Vergleichsanzeige über zwei benachbarte Werte:

- `TAUSCHEN`, wenn der linke Wert größer als der rechte ist.
- `BEHALTEN`, wenn der linke Wert kleiner oder gleich dem rechten ist.

Bei einer falschen Entscheidung wird die Sortierung auf den Anfangszustand zurückgesetzt.

Nach erfolgreichem Abschluss erscheint der leere Sortierchip.

## Rätsel 6 - Die Bubble-Sort-Maschine

Das ist keine normale Terminal-Stage. Der Spieler nimmt den leeren Sortierchip zu einem Computer
und bearbeitet dort den vorbereiteten Freitext-Code.

Vorlage:

```java
for (int i = 0; i < array.length - 1; i++) {
    for (int j = 0; j < array.length - 1 - i; j++) {
        if (____________________) {
            int temp = array[j];
            array[j] = array[j + 1];
            array[j + 1] = temp;
        }
    }
}
```

Gesucht ist:

```java
array[j] > array[j + 1]
```

Der vollständige Code wird auf den Sortierchip geschrieben. Danach wird der programmierte Chip
in die Bubble-Sort-Maschine eingesetzt.

## Rätsel 7 - Das Datenarchiv

```java
int[] energie = {20, 50, 80};
String[] module = {"CPU", "GPU", "RAM"};
boolean[] aktiv = {true, false, true};
```

Alternativ:

```java
int energie[] = {20, 50, 80};
String module[] = {"CPU", "GPU", "RAM"};
boolean aktiv[] = {true, false, true};
```

Erklärung: Alle drei Arrays müssen erstellt werden. Die Reihenfolge der drei Deklarationen ist
egal. Auch die Werte innerhalb jedes Array-Literals dürfen in beliebiger Reihenfolge stehen, weil
dieser Step nur die neue Array-Literal-Schreibweise und die enthaltenen Werte prüft.

## Rätsel 8 - Zweidimensionales Lager

### Step 1: 2D-Array erstellen

```java
int[][] lager = new int[3][4];
```

Alternative:

```java
int storage[][] = new int[3][4];
```

Erklärung: Der Arrayname ist flexibel, muss danach aber konsistent weiterverwendet werden.
Die Matrix hat exakt drei Zeilen und vier Spalten.

### Step 2: Lager befüllen

```java
lager[0][2] = 1;
lager[1][3] = 2;
lager[2][1] = 3;
```

Alternative Reihenfolge:

```java
storage[2][1] = 3;
storage[0][2] = 1;
storage[1][3] = 2;
```

### Weltreaktion nach Step 2

Nach der Array-Deklaration werden die zwölf Bodenfelder der Matrix sichtbar aktiviert und die
drei Zielfelder markiert. Nach den drei Zuweisungen werden die belegten Felder farblich
unterschieden und mit sichtbaren Datenobjekten gefüllt. Anschließend wird der Ortungschip aus der
Matrix zum Punkt `chip` gebracht und kann vom Spieler aufgenommen werden. Ein künstlicher
Lesezugriff wie `lager[1][3];` ist dafür nicht mehr nötig.

## Rätsel 9 - Suchroboter

Der Ortungschip enthält bereits die äußere Schleife und eine einfache Prüfung der ersten Spalte:

```java
for (int i = 0; i < map.length; i++) {
    int j = 0;
    if (map[i][j] == 1) {
        roboter.collect();
    }
}
```

Die Studierenden ersetzen `int j = 0;` durch die fehlende innere Schleife. Die vollständige Lösung
lautet:

```java
for (int i = 0; i < map.length; i++) {
    for (int j = 0; j < map[i].length; j++) {
        if (map[i][j] == 1) {
            roboter.collect();
        }
    }
}
```

Alternativen:

```java
for (int row = 0; row <= map.length - 1; row += 1) {
    for (int column = 0; column <= map[row].length - 1; column += 1) {
        if (map[row][column] == 1) {
            roboter.collect();
        }
    }
}
```

Erklärung: Die Matrix wird aus den beiden Custom Points `roboter_start` und `roboter_end` gebildet.
Ihre Größe ist deshalb durch das Level festgelegt und nicht im Code hardcodiert. Die Namen der
Zeilen- und Spaltenvariablen sind flexibel. Beide Namen müssen in der inneren Schleife, der
Bedingung und dem Arrayzugriff konsistent bleiben. Die vorbereitete Zeile `int j = 0;` darf nicht
zusätzlich neben der inneren Schleife stehen, weil `j` dort erneut deklariert würde.

## Rätsel 10 - Das zentrale Rechenzentrum

### Step 1: Bubble Sort

```java
for (int i = 0; i < array.length - 1; i++) {
    for (int j = 0; j < array.length - 1 - i; j++) {
        if (array[j] > array[j + 1]) {
            int temp = array[j];
            array[j] = array[j + 1];
            array[j + 1] = temp;
        }
    }
}
```

Alternative:

```java
for (int outer = 0; outer <= values.length - 2; outer += 1) {
    for (int inner = 0; inner <= values.length - 2 - outer; ++inner) {
        if (values[inner] > values[inner + 1]) {
            int tmp = values[inner];
            values[inner] = values[inner + 1];
            values[inner + 1] = tmp;
        }
    }
}
```

Erklärung: Arrayname, äußerer Index, innerer Index und Temp-Variable sind flexibel. Die Namen
müssen aber über den ganzen Bubble-Sort-Block konsistent verwendet werden.

### Step 2: Module zaehlen

```java
w
```

Erklärung: Hier ist der Arrayname fest `modules`. Die Schleifenvariable ist flexibel.

### Step 3: Batterien finden

```java
for (int i = 0; i < map.length; i++) {
    for (int j = 0; j < map[i].length; j++) {
        if (map[i][j] == 1) {
            roboter.collect();
        }
    }
}
```

Alternative:

```java
w2343
```

Erklärung: Wie bei Rätsel 9 wird die Map durchlaufen und bei Wert `1` wird gesammelt.

### Meta-Schritt: Ergebnisse zusammenführen

Nachdem alle drei Bereiche des Rechenzentrums abgeschlossen sind, zeigt das zentrale Display die
Ergebnisse der Teilaufgaben:

- sortierte Energie: `8 17 23 31 42`
- belegte Module: `3`
- Batteriesignale: `3`

Diese drei Ergebnisse werden in der Eingabemaske des zentralen Terminals eingetragen: fünf
Felder für die sortierte Energiefolge sowie je ein Feld für die belegten Module und die
Batteriesignale. Die Werte der drei Teilaufgaben werden serverseitig gemeinsam geprüft.

Erklärung: Der Interpreter prüft hier keine neue Codezeile und führt die drei Algorithmen nicht
erneut aus. Die Eingabemaske überträgt nur die Ergebnisse der vorherigen Aufgaben. Erst nach
dieser Bestätigung wird der Systemkern vollständig wiederhergestellt, der Aufzug geöffnet und
der Alarm abgeschaltet.
