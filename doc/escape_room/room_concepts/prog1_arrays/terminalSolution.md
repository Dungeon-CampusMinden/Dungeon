# Terminal Solutions - System Recovery

Dieses Dokument beschreibt die Terminal-Loesungen in der Reihenfolge, in der sie aktuell in
`TerminalInterpreterSetup` registriert sind. Code aus vorherigen Schritten darf im Editor stehen
bleiben; der Interpreter prueft immer die aktuelle Anforderung plus bereits geloeste Schritte.

## Grundregeln

- Leerzeilen werden ignoriert.
- Zeilenkommentare mit `//` werden ignoriert.
- Mehrere Statements duerfen in einer Editorzeile stehen, wenn sie mit `;` getrennt sind.
- Bei ungeordneten Steps ist die Reihenfolge der erwarteten Codezeilen egal.
- Bei geordneten Steps muss die inhaltliche Reihenfolge stimmen.
- Alternative Array-Schreibweisen wie `int[] a` und `int a[]` werden akzeptiert.
- Schleifen duerfen `i++`, `++i`, `i += 1` oder `i = i + 1` verwenden.
- Fuer vollstaendige Array-Iteration sind `< array.length` und `<= array.length - 1` erlaubt.
- Fuer Bubble-Sort-Bounds sind `< array.length - 1` und `<= array.length - 2` erlaubt.
- Flexible Variablennamen sind nur dort erlaubt, wo das Raetsel keine festen Namen verlangt.
  Danach muss derselbe Name konsequent weiterverwendet werden.

## Raetsel 1 - Die Materialisierungskammer

### Step 1: Energie-Array erstellen

Minimal:

```java
int[] energie = new int[5];
```

Alternative:

```java
int energie[] = new int[5];
```

Erklaerung: Es muss ein `int`-Array mit exakt 5 Plaetzen unter dem Namen `energie` erstellt werden.

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

Erklaerung: Alle fuenf Indizes muessen mit den passenden Werten belegt sein. Die Reihenfolge ist
egal, weil jede Zuweisung fuer sich eindeutig ist.

## Raetsel 2 - Der defekte Modulspeicher

### Step 1: Modul-Array erstellen

Minimal:

```java
String[] module = new String[5];
```

Alternative:

```java
String m[] = new String[5];
```

Erklaerung: Der Arrayname ist flexibel. Wenn hier z.B. `m` verwendet wird, muessen alle folgenden
Modul-Steps ebenfalls `m` verwenden.

### Step 2: Module einsetzen

Minimal:

```java
module[0] = "CPU";
module[1] = "RAM";
module[2] = "GPU";
module[3] = "SSD";
module[4] = "NETWORK";
```

Alternative mit flexiblem Namen und gemischter Formatierung:

```java
m[3] = "SSD";
m[4] = "NETWORK";
m[0] = "CPU"; m[1] = "RAM";
m[2] = "GPU";
```

Erklaerung: Die Reihenfolge ist egal. Wichtig ist, dass alle Werte am richtigen Index landen und
der in Step 1 gewaehlte Arrayname weiterverwendet wird.

### Step 3: GPU entfernen

```java
module[2] = null;
```

Bei flexiblem Namen:

```java
m[2] = null;
```

Erklaerung: Der Eintrag an Index 2 wird als leer markiert.

### Step 4: Array-Laenge auslesen

```java
module.length;
```

Bei flexiblem Namen:

```java
m.length;
```

Erklaerung: Es wird die feste Laenge des Arrays ausgelesen, nicht die Anzahl der belegten Plaetze.

## Raetsel 3 - Inventarscanner

```java
int count = 0;

for (String entry : module) {
    if (entry != null) {
        count++;
    }
}
```

Bei flexiblem Arraynamen aus Raetsel 2:

```java
int count = 0;

for (String item : m) {
    if (item != null) {
        count += 1;
    }
}
```

Erklaerung: Der Schleifenvariablenname ist flexibel. Er muss aber in der `if`-Bedingung konsistent
verwendet werden. `count++`, `++count`, `count += 1` und `count = count + 1` sind erlaubt.

## Raetsel 4 - Das Transportlager

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
for (int i = 0; i < pakete.length; i++) {
    roboter.collect(pakete[i]);
}
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

Erklaerung: Der Indexname ist flexibel, muss aber im Arrayzugriff identisch sein. Der Roboter muss
`collect` verwenden.

## Raetsel 5 - Der chaotische Datenspeicher

Aktuell gibt es fuer dieses Raetsel keine Terminal-Eingabe.

## Raetsel 6 - Die Bubble-Sort-Maschine

Aktuell gibt es fuer dieses Raetsel keine Terminal-Eingabe.

## Raetsel 7 - Das Datenarchiv

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

Erklaerung: Alle drei Arrays muessen erstellt werden. Die Reihenfolge der drei Deklarationen ist
egal.

## Raetsel 8 - Zweidimensionales Lager

### Step 1: 2D-Array erstellen

```java
int[][] lager = new int[3][4];
```

Alternative:

```java
int storage[][] = new int[3][4];
```

Erklaerung: Der Arrayname ist flexibel, muss danach aber konsistent weiterverwendet werden.

### Step 2: Lager befuellen

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

### Step 3: Feld auslesen

```java
lager[1][3];
```

Bei flexiblem Namen:

```java
storage[1][3];
```

## Raetsel 9 - Suchroboter

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

Erklaerung: Die Namen der Zeilen- und Spaltenvariablen sind flexibel. Beide Namen muessen in der
inneren Schleife, der Bedingung und dem Arrayzugriff konsistent bleiben.

## Raetsel 10 - Das zentrale Rechenzentrum

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

Erklaerung: Arrayname, aeusserer Index, innerer Index und Temp-Variable sind flexibel. Die Namen
muessen aber ueber den ganzen Bubble-Sort-Block konsistent verwendet werden.

### Step 2: Module zaehlen

```java
int count = 0;

for (String module : modules) {
    if (module != null) {
        count++;
    }
}
```

Erklaerung: Hier ist der Arrayname fest `modules`. Die Schleifenvariable ist flexibel.

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
for (int row = 0; row <= map.length - 1; row = row + 1) {
    for (int column = 0; column <= map[row].length - 1; column++) {
        if (map[row][column] == 1) {
            roboter.collect();
        }
    }
}
```

Erklaerung: Wie bei Raetsel 9 wird die Map durchlaufen und bei Wert `1` wird gesammelt.
