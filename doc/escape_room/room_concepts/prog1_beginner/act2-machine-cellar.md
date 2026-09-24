# Akt II: Der Maschinenkeller

Teil des Raums [Programmieren 1: Das Erbe der Seelenweber](prog1_beginner_concept.md).

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
