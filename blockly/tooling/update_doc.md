# Update-Skript „update.sh“ – Dokumentation

## 1. Übersicht  
Dieses Bash-Skript aktualisiert das vorhandene Workshop-Verzeichnis (`~/Desktop/Workshop`) und seine Inhalte. Es führt folgende Hauptaufgaben durch:  
1. Git-Pull im Source-Verzeichnis  
2. Baunen der Blockly-JAR via Gradle  
3. Verschieben der JAR nach `Blockly/content`  
4. Kopieren von `kill_server.sh` ins Basisverzeichnis  
5. Download und Installation aktueller Blockly-Artefakte (`blockly.bin`, `index.html`)  
6. Installation/Aktualisierung von VS-Code-Extensions  
7. Löschen alter User-Skripte (optional)

---

## 2. Schritte im Detail  
1. **Abhängigkeiten prüfen**  
   - `git`, `wget`, `java`  
2. **Navigiere nach** `~/Desktop/Workshop/Source`  
3. **Git-Operationen**  
   - `git reset --hard`, `git clean -fd`, `git pull`  
4. **Gradle-Build**  
   - Aufruf von `./gradlew buildBlocklyJar`  
5. **JAR verschieben**  
   - Quelle: `./blockly/build/libs/Blockly.jar`  
   - Ziel: `~/Desktop/Workshop/Blockly/content/blockly.jar`  
6. **`kill_server.sh` kopieren**  
   - Quelle: `Source/blockly/toolings/kill_server.sh`  
   - Ziel: `~/Desktop/Workshop/kill_server.sh`  
7. **Blockly-Dateien herunterladen**  
   - `blockly.bin` → `~/Desktop/Workshop/Blockly/blockly.bin`  
   - `index.html` → `~/Desktop/Workshop/Blockly/content/index.html`  
8. **VS-Code-Extension installieren**  
   - `blockly-code-runner.vsix` (von `BASE_DOWNLOAD_URL`)  
   - Java-Extensions (`redhat.java`, `vscjava.vscode-gradle`)  
9. **User-Skripte optional löschen**  
10. **Zusammenfassung** aller Erfolge und Fehler  

---

## 3. Voraussetzungen  
- Debian-basierte Distribution (z. B. Raspberry Pi OS)  
- Workshop Ordner `~/Desktop/Workshop` muss existieren
- Installiert und im PATH:  
  - Git (`git`)  
  - Wget (`wget`)  
  - Java (`java`)  
  - VS Code CLI (`code`)  
- Externer Server/CDN, der die Workshop-Artefakte bereitstellt (`BASE_DOWNLOAD_URL` muss gesetzt sein)

---

## 4. Verzeichnis-Struktur & externe Dateien  
```
~/Desktop/Workshop/
├── Source/
├── Blockly/
│   ├── blockly.bin               # wird per Download aktualisiert
│   └── content/
│       ├── blockly.jar           # liegt nach Build hier
│       └── index.html            # wird per Download aktualisiert
├── kill_server.sh                # wird hierher kopiert
└── user/                         # optionale User-Skripte
```

Externe Dateien kommen vom `BASE_DOWNLOAD_URL`:  
- `blockly.bin`  
- `blockly_index` (wird nach Download `index.html` umbenannt)  
- `blockly-code-runner.vsix`  

---

## 5. Konfiguration  
- `BASE_DOWNLOAD_URL` ganz zu Beginn des Skripts anpassen  

---

*Stand: Juli 2025*
