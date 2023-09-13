#!/bin/bash

# Erstelle den Ordner "scripts", falls er nicht existiert
mkdir -p scripts

# Iteriere über alle übergebenen Argumente
for arg in "$@"; do
    # Prüfe, ob das Argument eine Datei oder ein Verzeichnis ist
    if [ -e "$arg" ]; then
        # Extrahiere den Dateinamen aus dem Pfad
        filename=$(basename "$arg")
        # Kopiere die Datei oder das Verzeichnis in den "scripts"-Ordner
        cp -r "$arg" "scripts/$filename"
    else
        echo "Das Argument '$arg' ist keine gültige Datei oder Verzeichnis."
    fi
done

# Erstelle die JAR-Datei
jar -cvf scripts.jar scripts

# Ausgeben der kopierten Dateien
echo "Die folgenden Dateien wurden kopiert:"
find scripts -type f

echo "Die Dateien und Verzeichnisse wurden in den 'scripts'-Ordner kopiert und als 'scripts.jar' exportiert."
