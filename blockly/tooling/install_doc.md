# Raspberry Pi Installationsskript – Dokumentation

## 1. Übersicht  
Dieses Bash-Skript automatisiert die Einrichtung der Raspberry PIs und des Workshop-Projekts. Es führt System-Updates durch, installiert Java (Temurin OpenJDK 21), Visual Studio Code und richtet ein Projekt das Workshop-Projekt ein. Das Skript ist für Debian-basierte Distributionen (z. B. Raspberry Pi OS) konzipiert und erfordert `sudo`-Rechte.

---

## 2. Hauptfunktionen  
- System-Update (`apt update && apt upgrade`)  
- Installation Temurin OpenJDK 21 (arm64-Download, Extraktion nach `/opt/…`)  
- Installation VS Code (arm-/amd64-Pakete)  
- Download und des Workshop-Projekts:  
  - TAR-Datei herunterladen  
  - Entpacken in temporäres Verzeichnis  
  - Validierung der Verzeichnisstruktur (`Workshop/Blockly`, `Workshop/Source`)  
  - Verschieben auf Desktop (`~/Desktop/Workshop`)  
  - Build-Test via Gradle  
  - Installation der VS-Code Extension (VSIX)  

---

## 3. Voraussetzungen  
- Debian-basierte Distribution (z. B. Raspberry Pi OS)  
- `sudo`-Rechte (script bricht ab, wenn keine vorhanden)  
- Internetzugriff (Ping 8.8.8.8)  
- Vorhandene Tools: `wget`, `curl`, `tar`, `gpg`, `file`, `gradle` (wird per Script installiert)  
- Externer Server/CDN, der die Workshop-TAR-Datei bereitstellt (`PROJECT_TAR_URL` muss gesetzt sein)

### Externe Ressourcen  
- Web-Server oder CDN, der folgende Dateien bereitstellt:  
  1. **Workshop-TAR** (`PROJECT_TAR_URL` muss auf eine `.tar.gz` oder `.tar.bz2` zeigen)  

---

## 4. Aufbau der Workshop-TAR  
Die TAR-Datei muss – nach dem Entpacken – folgende Struktur liefern:

```
Workshop/  
├── Blockly/  
│   └── blockly.bin  
├── Source/  
│   ├── gradlew  
│   └── … Quellcode …  
└── blockly-code-runner-1.0.0.vsix   (optional)
```

- `Workshop/Blockly/blockly.bin`  
  – wird ausführbar gemacht  
- `Workshop/Source/gradlew`  
  – wird gebaut via `./gradlew build`  
- `.vsix`-Datei im Root der entpackten Workshop-Struktur  
  – wird mit `code --install-extension` in VS-Code installiert.  

---

## 5. Nutzung & Konfiguration  
1. **URL anpassen**  
   - `PROJECT_TAR_URL="https://…/Workshop.tar.gz"`  
2. **Skript ausführbar machen**  
   ```bash
   chmod +x install.sh
   ```
3. **Mit sudo starten**  
   ```bash
   sudo ./install.sh
   ```
4. **Interaktive Abfragen**  
   - Bestätigung für Gesamtinstallation  
   - Überschreiben alten Desktop-Ordners  
   - Neustart optional  

---

## 6. Aktuelle Probleme & Limitierungen  
- **TAR-Struktur**: Skript erwartet exakt `Workshop`-Verzeichnis – Abweichungen führen zu Abbruch.  
- **Cleanup**: Temporäre Dateien werden per `trap` gelöscht – evtl. Fehlermeldungen vor Cleanup nicht geloggt.  
- **Java-Version**: Feste Version 21 – zukünftige Updates erfordern manuelles Anpassen `JAVA_VERSION` und URL.  

---

*Version: Stand Juli 2025*
