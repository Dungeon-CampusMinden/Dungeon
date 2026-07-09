# Generator Input Format V0.2

Status: V0.2-Contract
Scope: finalisierte Übergabe von der Wizard-UI an den Java-Generator

## Grenze

Die UI schreibt einen Projektordner. Der Java-Generator liest ihn und erzeugt
das Modul-ZIP. Die UI erzeugt selbst kein ZIP.

```text
wizard-project/
  deer.json
  assets/
    custom/
      3b50ea522803-foundation-note.png
```

`deer.json` liegt im Wurzelverzeichnis. Alle Assetpfade verwenden
Forward-Slashes und beginnen mit `assets/custom/`.

## Draft und Finalisierung

- Ein UI-Entwurf darf unvollständig sein und bleibt außerhalb dieses Formats.
- Die Finalisierung projiziert den Draft auf Formatversion `0.2-draft`.
- Die UI prüft Schema, Fachregeln und Assetpfade vor dem Schreiben.
- Der native Storage-Adapter muss `deer.json` über eine temporäre Datei
  sicher ersetzen können.
- Ein fehlgeschlagener Schreibvorgang lässt die letzte gültige Ausgabe
  unverändert.
- Finalisierung sperrt den UI-Entwurf nicht.

V0.2 nutzt einen Standalone-Host mit nativem Storage-Adapter. Ein
browser-only Export ist nicht Teil des Foundation-Slices.

## Assetregeln

- Zulässig sind im Foundation-Slice PNG und JPEG.
- Assets liegen in V0.2 flach unter `assets/custom/`.
- Dateinamen werden von der UI portabel normalisiert und beginnen mit den
  ersten zwölf lowercase Hex-Zeichen des SHA-256-Inhaltshashes, gefolgt von
  `-`, z. B. `3b50ea522803-foundation-note.png`.
- Referenzierte Dateien müssen existieren; ein „optionales fehlendes Asset“
  gibt es nicht.
- MIME-Deklaration, Dateiendung und Dateiinhalt müssen zusammenpassen.
- `.`-, `..`-, leere Pfadsegmente, Backslashes, absolute Pfade, Drive-Pfade,
  UNC-Pfade und URLs sind unzulässig.
- Symlinks werden nicht verfolgt.
- Pfade werden in Java gegen den realen Projektroot aufgelöst und müssen nach
  Normalisierung weiterhin darunter liegen.
- Nicht referenzierte Dateien werden ignoriert und als Warnung gemeldet.
- Der Generator berechnet den Datei-Hash erneut und lehnt einen falschen
  Präfix ab.

Die UI schreibt neue, inhaltsadressierte Assets zuerst und ersetzt
`deer.json` über eine temporäre Datei zuletzt. Inhaltsadressierte Dateien
werden nie mit anderem Inhalt überschrieben. Ein Fehler vor diesem letzten
Schritt lässt die vorherige gültige Konfiguration nutzbar; zusätzliche
unreferenzierte Dateien sind nur Warnungen.

Berechtigungs-, Speicherplatz- und Schreibfehler werden mit „Erneut versuchen“
und „Anderen Ordner wählen“ beantwortet. Fremde und alte unreferenzierte
Dateien werden weder überschrieben noch automatisch gelöscht.

## Generatorverhalten

Der Generator:

- öffnet den Projektordner read-only;
- lehnt unbekannte `formatVersion`-Werte ab;
- validiert mit demselben Schema und denselben Rule-Codes wie die UI;
- berechnet Hashes aus kanonischer `deer.json` und allen referenzierten Assets;
- verändert oder löscht keine Eingabedatei;
- schreibt Ausgaben ausschließlich in ein getrenntes Ziel.

Das Ausgabeformat steht in
[`generator-output-format.md`](generator-output-format.md).
