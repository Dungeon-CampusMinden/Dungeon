## Troubleshooting/FAQs

### Erste Schritte (version4)

- Clonen Sie das `dungeon-starter`-Repo (Public archive): `git clone https://github.com/Programmiermethoden/dungeon-starter.git`
- Wechseln Sie in das `code`-Verzeichnis: `cd dungeon-starter/code/`

Nun können Sie das Spiel direkt starten mit `./gradlew run` (Linux) bzw. `.\gradlew run` (Windows) - oder in einer IDE bearbeiten, indem Sie das Unterverzeichnis `code` direkt als Projekt in der IDE öffnen.

Einen guten Einstieg bietet hierfür der Quickstart-Guide: https://github.com/Programmiermethoden/PM-Dungeon/blob/version4/documentation/quickstart_de.md

Um später den `core` (version4) zu bearbeiten:

- Clonen Sie das `PM-Dungeon`-Repo und den `version4`-Branch: `git clone -b version4 https://github.com/Programmiermethoden/PM-Dungeon.git`
- Wechseln Sie in das `code`-Verzeichnis: `cd PM-Dungeon/code/`
- Bearbeiten Sie den `core` mit einer IDE
- Mit `./gradlew jar` können Sie ein `jar`-File vom `core` erstellen und dieses in der `build.gradle` des `dungeon-starter` hinzufügen

### Problem: Gradlebuild schlägt fehl

Im PM-Dungeon verwendet wir das Buildtool Gradle in der Version 7.2 dieses benötigt für das bei unserem Repo verwendete Setup die GradleVM der verwendeten JDKversion 17. Diese Version des verwendeten JDKs sollte
auch in den Einstellungen von IntelliJ über das Menü wie folgt gesetzt sein:

* "File -> Settings.. -> Build, Execution, Deployment -> Build Tools -> Gradle" und dort die GradleVM auf die aktuell verwendete JVM 17.0.2. stellen (falls dies noch nicht der Fall sein sollte).

Des Weiteren sollte überprüft werden, ob in den "Umgebungsvariablen" unter Windows 8/10/11 die "Systemvariable" `JAVA_HOME` auf die bei Euch installierte JVM 17.0.2 zeigt. Dies sollte in etwa so in den "Systemvariablen" auf Eurem Rechner eingetragen sein.
| Variable    | Wert |
| :---        | :---- |
| `JAVA_HOME`      | `C:\Users\EUER_BENUTZERNAME\.jdks\openjdk-17.0.2` |

### Problem: Gradle Konfiguration wird nicht erkannt

Bei dem öffnen des Projektes sollte der "code"-Folder als IntelliJ-Projekt geöffnet werden, da sonst die automatische
Konfiguration von Gradle nicht richtig abläuft. Sobald der "code"-Ordner als IntelliJ-Projekt geöffnet wurde, sorgt
Gradle automatisch dafür das alle Abhängigkeiten für das Projekt runtergeladen und konfiguriert werden.

### Problem: Das IntelliJ-google-java-format-Plugin formatiert den Code (Hotkey oder rekursiv) gar nicht oder nicht vollständig

Nutzen Sie zum formatieren aller java-Dateien die Konsole: `./gradlew spotlessApply`. Dieser Aufruf formatiert alle Dateien vollständig im "AOSP"-Style.

### Problem: Eclipse stellt sich beim Import des Projekts quer

Clonen Sie zuerst das [pm-dungeon](https://github.com/Programmiermethoden/pm-dungeon) Repo. Öffnen Sie Ihren Eclipse-Workspace und wählen Sie "File -> Import... -> Existing Gradle Project". Wählen Sie als "Projekt root directory" `<absolute path to repo>/dungeon-starter/code` aus(!) Überschreiben Sie nicht die Workspace settings und nutzen Sie den Gradle wrapper. Der Import-Preview sollte dann in etwa so sein:

![image](https://user-images.githubusercontent.com/85501570/162235250-a4c92ec3-e1b6-4eac-80e6-5c9cbf367ca1.png)

### Problem: Dungeon und M1 Mac

Das Dungeon funktioniert inzwischen auch auf M1 Macs.
