# Teacher Workflow V0.2

Status: Persona-Reise und Akzeptanzszenarien
Stand: 09.07.2026

## Rollen

- **Lehrende Person:** arbeitet ausschließlich im Wizard und im
  Projektordner-Dialog.
- **Technische Betreuung:** führt Generator, Integration und Gradle-Build aus.
- **Spielende:** erhalten das gebaute Spiel, nicht den Authoring-Entwurf.

Der manuelle Java-Schritt ist eine Foundation-Einschränkung. Solange er
notwendig ist, darf V0.2 nicht als vollständig lehrendenfertiger
Ein-Klick-Workflow bezeichnet werden.

## Beispielreise

Eine Lehrkraft möchte einen 30-minütigen Raum zu sicherem Umgang mit
Zahlencodes erstellen:

1. Sie legt einen neuen Entwurf an und formuliert das Lernziel.
2. Sie erfasst Zielgruppe, Vorwissen, Spielerzahl, Zeitlimit und Geschichte.
3. Sie fügt im ersten Abschnitt einen **Fund** hinzu: Im Schreibtisch liegt ein
   Text mit dem Code-Hinweis und optional ein ergänzendes Bild.
4. Sie fügt im zweiten Abschnitt einen **Zahlencode** hinzu und benennt das
   Gerät „Tür-Keypad“.
5. Sie ordnet beide Rätsel dem Lernziel zu und ergänzt abgestufte Hilfen.
6. Sie liest Story, Ablauf, Inhalte und Hilfen in der Vorschau.
7. Die Prüfung meldet ein Bild ohne Alternativbeschreibung. Sie springt direkt
   zum Bild, ergänzt die Beschreibung und prüft erneut.
8. Sie finalisiert `deer.json` und das Bild in den Projektordner.
9. Eine technische Betreuung erzeugt daraus das Modul-ZIP, integriert und
   baut es.
10. Nach dem Playtest öffnet die Lehrkraft denselben Entwurf, verbessert eine
    Hilfe und finalisiert erneut.

## Akzeptanzszenarien

### Entwurf unterbrechen und fortsetzen

- Ein unvollständiger Entwurf wird automatisch lokal gespeichert.
- Beim nächsten Start wird er mit seinen stabilen internen Kennungen und
  Bildern angeboten.
- Die UI behauptet nicht, dieser Arbeitsstand sei bereits eine gültige
  `deer.json`.

### Ablauf ohne Graphwissen erstellen

- Rätsel werden in Abschnitte eingefügt und per Buttons verschoben.
- Parallele Rätsel stehen im selben Abschnitt.
- Die UI erzeugt Graphknoten und Kanten ohne technische Eingabe.
- Screenreader- und Tastaturnutzende erhalten dieselben Möglichkeiten wie
  Drag-and-drop-Nutzende.

### Fehler verstehen und beheben

- `Entwurf prüfen` bleibt auch bei unvollständigen Daten verfügbar.
- Ein Problem erklärt Auswirkung und Korrektur in Fachsprache.
- „Zum Rätsel“ fokussiert das relevante Element.
- Bereits eingegebene Inhalte bleiben erhalten.

### Finalisieren

- `Entwurf finalisieren` ist nur ohne blockierende Fehler aktiv.
- Die Ausgabe enthält ausschließlich eine validierte `deer.json` und
  referenzierte Bilder/Dateien; die UI erzeugt kein ZIP.
- Neue Dateien werden inhaltsadressiert geschrieben; `deer.json` wird zuletzt
  sicher ersetzt. Unbekannte Dateien werden nicht gelöscht.
- Der Entwurf bleibt weiter bearbeitbar.

### Iterieren

```text
Entwurf
-> finalisieren
-> generieren und bauen
-> spielen und beobachten
-> Entwurf überarbeiten
-> erneut finalisieren
```

Ein statisch gültiger Entwurf ist nicht automatisch verständlich,
unterrichtlich passend oder zeitlich realistisch. Der Workflow fordert deshalb
vor einer Nutzung mit Lernenden einen Playtest. Datum, Testgruppe, Ergebnis und
Notizen können im lokalen UI-Draft protokolliert werden; sie sind kein Teil von
`deer.json`.

## Was V0.2 nicht verspricht

- kein Generatorstart durch die Lehrenden-UI;
- keine spielbare 3D-Vorschau;
- keine automatische Bewertung von Lernerfolg;
- keine Garantie, dass Code und Hinweis inhaltlich zusammenpassen;
- keine freie Verzweigung oder optionalen Rätsel;
- keine Zusammenarbeit mehrerer Autorinnen und Autoren;
- keine Versionierung oder Cloud-Synchronisation.
