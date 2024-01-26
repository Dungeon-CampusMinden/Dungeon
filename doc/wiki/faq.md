---
title: "FAQ"
---

## Troubleshooting/FAQs

### Problem: Gradlebuild schlägt fehl

Im PM-Dungeon verwendet wir das Buildtool Gradle in der Version 7.2 dieses benötigt für das bei unserem Repo verwendete Setup die GradleVM der verwendeten JDKversion 17. Diese Version des verwendeten JDKs sollte
auch in den Einstellungen von IntelliJ über das Menü wie folgt gesetzt sein:

* "File -> Settings.. -> Build, Execution, Deployment -> Build Tools -> Gradle" und dort die GradleVM auf die aktuell verwendete JVM 17.0.2. stellen (falls dies noch nicht der Fall sein sollte).

Des Weiteren sollte überprüft werden, ob in den "Umgebungsvariablen" unter Windows 8/10/11 die "Systemvariable" `JAVA_HOME` auf die bei Euch installierte JVM 17.0.2 zeigt. Dies sollte in etwa so in den "Systemvariablen" auf Eurem Rechner eingetragen sein.
| Variable    | Wert                                              |
|:------------|:--------------------------------------------------|
| `JAVA_HOME` | `C:\Users\EUER_BENUTZERNAME\.jdks\openjdk-17.0.2` |

### Problem: Gradle Konfiguration wird nicht erkannt

Bei dem öffnen des Projektes sollte der "code"-Folder als IntelliJ-Projekt geöffnet werden, da sonst die automatische
Konfiguration von Gradle nicht richtig abläuft. Sobald der "code"-Ordner als IntelliJ-Projekt geöffnet wurde, sorgt
Gradle automatisch dafür das alle Abhängigkeiten für das Projekt runtergeladen und konfiguriert werden.

### Problem: Das IntelliJ-google-java-format-Plugin formatiert den Code (Hotkey oder rekursiv) gar nicht oder nicht vollständig

Nutzen Sie zum formatieren aller java-Dateien die Konsole: `./gradlew spotlessApply`. Dieser Aufruf formatiert alle Dateien vollständig im "AOSP"-Style.

### Problem: Eclipse stellt sich beim Import des Projekts quer

Clonen Sie zuerst das [pm-dungeon](https://github.com/Dungeon-CampusMinden/pm-dungeon) Repo. Öffnen Sie Ihren Eclipse-Workspace und wählen Sie "File -> Import... -> Existing Gradle Project". Wählen Sie als "Projekt root directory" `<absolute path to repo>/dungeon-starter/code` aus(!) Überschreiben Sie nicht die Workspace settings und nutzen Sie den Gradle wrapper. Der Import-Preview sollte dann in etwa so sein:

![image](https://user-images.githubusercontent.com/85501570/162235250-a4c92ec3-e1b6-4eac-80e6-5c9cbf367ca1.png)

### Problem: Dungeon und M1 Mac

Das Dungeon funktioniert inzwischen auch auf M1 Macs.

### Problem: Dungeon lässt sich auf MacOS nicht in der IDE starten

Damit das Dungeon auf macOS gestartet werden kann, muss die JVM mit dem Parameter `-XstartOnFirstThread` ausgeführt werden.
Dies lässt sich in der jeweiligen IDE konfigurieren.
- [IntelliJ](https://www.jetbrains.com/help/idea/run-debug-configuration-java-application.html)


