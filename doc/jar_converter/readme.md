---
title: "Jar Converter: How to"
---

## Was ist `jarConvert.sh`

`jarConverter.sh` ist ein Bash-Skript, das dazu dient, Dateien und Verzeichnisse zu kopieren und sie anschließend in einer JAR-Datei zu exportieren.
Das Skript ermöglicht es dir, Dateien und Verzeichnisse mit relativen Pfaden als Befehlszeilenargumente anzugeben.
Diese Dateien und Verzeichnisse werden dann in einen neu erstellten Ordner mit dem Namen "scripts" kopiert.
Nachdem die Dateien und Verzeichnisse kopiert wurden, erstellt das Skript eine JAR-Datei mit dem Namen "scripts.jar", die den Inhalt des "scripts"-Ordners enthält.

`jarConverter.sh` kann genutzt werden, um verschiedene DSL-Skripte in einer JAR-Datei zu sammeln, um sie dann im Spiel einzulesen zu lassen.

## How to use

1. Open your terminal.
2. Navigate to the directory where the script is located (if you're not already there).
3. To use the script, provide the relative paths of the files and directories you want to include in the JAR file as arguments. For example:

   ```bash
   ./jarConverter.sh file1.dng directory1 file2.dng
   ```

   Replace `file1.dng`, `directory1`, and `file2.dng` with the actual relative paths of the files and directories you want to include.

4. The script will copy the specified files and directories to a new folder named "scripts" and create a JAR file named "scripts.jar" containing the contents of the "scripts" folder.
