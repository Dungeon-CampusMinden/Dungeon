# System Recovery: Musterlösungen

**Spoiler:** Dieses Dokument ist für Entwicklung und Tests gedacht, nicht als Spielanleitung.
Maßgeblich sind die Prüfungen in `TerminalInterpreterSetup` und die jeweiligen Rätselklassen.
Die Beispiele zeigen je eine gültige Eingabe; Leerzeichen und viele Bezeichner können variieren.
Im Terminal dürfen angenommene frühere Zeilen stehen bleiben. Stick-Editor und finale Eingabemaske
sind eigene Eingabeflächen, keine normalen Terminalschritte.

## 1. Energieversorgung

Zuerst nur die Struktur anlegen:

```java
int[] energie = new int[5];
```

Die Werte sind ein **eigener** Schritt. Eine Initialisierung mit Werten statt `new int[5]`
erfüllt den ersten Schritt nicht.

```java
energie[0] = 40;
energie[1] = 10;
energie[2] = 80;
energie[3] = 30;
energie[4] = 60;
```

Die fünf Zuweisungen dürfen in anderer Reihenfolge stehen. Danach wird die Batterie
materialisiert und eingesetzt.

## 2. Modulspeicher

```java
String[] module = new String[5];
```

```java
module[0] = "CPU";
module[1] = "RAM";
module[2] = "GPU";
module[3] = "SSD";
module[4] = "NETWORK";
```

Nach der Untersuchung des GPU-Sockels:

```java
module[2] = null;
```

Anschließend:

```java
module.length;
```

Der Arrayname darf frei gewählt werden, muss dann aber in allen Modul- und Scanner-Schritten
gleich bleiben. Der Code für den Zugang zum Inventarscanner ist aktuell **0502**:
Kapazität 5, ausgefallene GPU an Index 2.

## 3. Inventarscanner

```java
int count = 0;
for (String entry : module) {
    if (entry != null) {
        count++;
    }
}
```

Die for-each-Variable ist frei wählbar; der Arrayname stammt aus Rätsel 2. Nach dem Scan sind
vier Einträge belegt. Der Code zum Transportlager ist aktuell **0504**.

## 4. Transportlager

Das Paketarray muss beim Anlegen bereits die fünf Gewichte enthalten:

```java
int[] pakete = {15, 40, 20, 60, 30};
```

`int[] pakete = new int[]{15, 40, 20, 60, 30};` ist ebenfalls gültig. Der Name darf
anders lauten; die folgende Schleife muss denselben Namen verwenden.

```java
for (int i = 0; i < pakete.length; i++) {
    roboter.collect();
}
```

Es ist eine indexbasierte `for`-Schleife gefordert. `collect()` hat **keinen Parameter**.
Nach dem Transportlauf muss ECHOs Anruf beantwortet werden, bevor der Datenspeicher aufgeht.

## 5. Manuelle Sortierung

An der Vergleichsanzeige für die Startfolge `90, 30, 10, 70, 50` jeweils **Tauschen**
wählen, wenn der linke Wert größer als der rechte ist; andernfalls **Behalten**.
Eine falsche Entscheidung setzt die Folge zurück. Die sortierte Folge lautet
`10, 30, 50, 70, 90`. Dieses Rätsel hat keinen Terminal-Code.

## 6. Bubble-Sort-Maschine

Im vorbereiteten Programm auf dem Sortierstick die Bedingung ergänzen:

```java
array[j] > array[j + 1]
```

Den programmierten Stick in die Maschine einsetzen und ihren Lauf abschließen.
Anschließend wird der Archivschlüssel ausgegeben. Dies ist ein Stick-Editor-Schritt,
kein normaler Terminalschritt.

## 7. Datenarchiv

```java
int[] energie = {20, 50, 80};
String[] module = {"CPU", "GPU", "RAM"};
boolean[] aktiv = {true, false, true};
```

Die drei Deklarationen und die Werte innerhalb jedes Literals dürfen in beliebiger
Reihenfolge stehen. Auch die explizite Java-Form `new int[]{...}` beziehungsweise
`new String[]{...}` und `new boolean[]{...}` wird akzeptiert.

## 8. Zweidimensionaler Speicher

```java
int[][] lager = new int[3][4];
```

```java
lager[0][2] = 1;
lager[1][3] = 2;
lager[2][1] = 3;
```

Der Arrayname darf frei gewählt werden, danach muss er gleich bleiben. Die Zuweisungen
dürfen in anderer Reihenfolge stehen. Nach dem Befüllen erscheinen Datenobjekte und der
Ortungschip; ein zusätzlicher Lesezugriff ist nicht nötig.

## 9. Suchroboter

Der Chip enthält bereits die äußere Schleife und `int j = 0;`. Diese feste Spalte muss
durch eine innere Schleife ersetzt werden:

```java
for (int i = 0; i < map.length; i++) {
    for (int j = 0; j < map[i].length; j++) {
        if (map[i][j] == 1) {
            roboter.collect();
        }
    }
}
```

Das Programm wird auf den Ortungschip geschrieben. Nach dem Einsetzen fährt der Roboter
seine Matrix ab und liefert das Systemkern-Zugriffsmodul. Die Matrixgröße wird aus
`roboter_start` und `roboter_end` ermittelt.

## 10. Systemkern

Mit dem Zugriffsmodul das Freigabeskript ausführen; erst dann öffnet der Systemkern
und der Alarm beginnt. Die drei Prüfbereiche benutzen folgende Arraynamen:

1. `array`: Bubble Sort für `42, 17, 8, 31, 23`.
2. `modules`: belegte Einträge zählen.
3. `map`: den Suchroboter jedes Feld der 3x5-Matrix besuchen lassen.

**Sortieren:**

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

**Module zählen:**

```java
int count = 0;
for (String module : modules) {
    if (module != null) {
        count++;
    }
}
```

**Alle Rasterfelder erfassen:**

```java
for (int row = 0; row < map.length; row++) {
    for (int column = 0; column < map[row].length; column++) {
        roboter.collect();
    }
}
```

Im letzten Bereich wird **jedes** Feld erfasst; die Farbe und der Zellwert sind egal.
Die Eingabe startet den zweiten Roboter. Erst wenn er fertig ist, steht die
Abschlussmaske bereit.

**Ergebnisse für die Abschlussmaske:** `8, 17, 23, 31, 42`, **3** belegte Module,
**15** erfasste Rastermodule. Die Maske prüft diese Ergebnisse serverseitig; sie erwartet
keinen weiteren Java-Code. Danach muss ECHOs letzter Anruf beantwortet werden, um den
Aufzug zu öffnen.
