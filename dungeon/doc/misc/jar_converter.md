---
title: "Jar Converter: How to"
---

## Was ist `jarConvert.sh`

`jarConverter.sh` ist ein Bash-Skript, das dazu dient, Dateien und Verzeichnisse zu kopieren und sie anschließend in einer JAR-Datei zu exportieren.
Das Skript ermöglicht es dir, Dateien und Verzeichnisse mit relativen Pfaden als Befehlszeilenargumente anzugeben.
Diese Dateien und Verzeichnisse werden dann in einen neu erstellten Ordner mit dem Namen "scripts" kopiert.
Nachdem die Dateien und Verzeichnisse kopiert wurden, erstellt das Skript eine JAR-Datei mit dem Namen "scripts.jar", die den Inhalt des "scripts"-Ordners enthält.

`jarConverter.sh` kann genutzt werden, um verschiedene DSL-Skripte in einer JAR-Datei zu sammeln, um sie dann im Spiel einzulesen zu lassen.

*Anmerkung:* Unter Windows kann `javConverter.bat` verwendet werden. Die Funktionalität ist identisch zum `jarConverter.sh`.
## Anwenden

1.  Öffnen Sie Ihr Terminal.
2.  Navigieren Sie zum Verzeichnis, in dem sich das Skript befindet (falls Sie sich dort nicht bereits befinden).
3.  Um das Skript zu verwenden, geben Sie die relativen Pfade der Dateien und Verzeichnisse an, die Sie in die JAR-Datei aufnehmen möchten, als Argumente. Zum Beispiel:

    ```bash
    ./jarConverter.sh datei1.dng verzeichnis1 datei2.dng
    ```

    oder unter Windows

    -   in der cmd:

        ```batch
        jarConverter.bat datei1.dng verzeichnis1 datei2.dng
        ```

    -   in der PowerShell:

        ```batch
        ./jarConverter.bat datei1.dng verzeichnis1 datei2.dng
        ```

    Ersetzen Sie `datei1.dng`, `verzeichnis1` und `datei2.dng` durch die tatsächlichen relativen Pfade der Dateien und Verzeichnisse, die Sie aufnehmen möchten.

4.  Das Skript kopiert die angegebenen Dateien und Verzeichnisse in einen neuen Ordner namens "scripts" und erstellt eine JAR-Datei mit dem Namen "scripts.jar", die den Inhalt des Ordners "scripts" enthält.
