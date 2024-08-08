- [Idee](#idee)
- [Begründung, dass Qodana nicht weiter verfolgt wurde](#begründung-dass-qodana-nicht-weiter-verfolgt-wurde)
- [Versuch](#versuch)
- [Probleme mit der Projektstruktur](#probleme-mit-der-projektstruktur)
- [Qodana lokal im Docker laufen lassen](#qodana-lokal-im-docker-laufen-lassen)
- [Mögliche Ansatzpunkte](#mögliche-ansatzpunkte)

# Idee

[Qodana](https://www.jetbrains.com/qodana/) ist ein Tool zu statischen Syntaxanalyse von JetBrains. Es kann automatisiert in einer CI-Pipeline bestehenden Code analysieren und sogar Quick-fixes für problematische Syntax ausführen. Das Besondere an Qodana ist, dass es die gleiche Syntaxanalyse wie die zur jeweiligen Sprache gehörende JetBrains IDE beinhaltet. Dies ist ein großer Vorteil, da so die gewohnte Syntaxanalyse der IDE genutzt werden kann und ohne doppelte Konfiguration oder IDE-fremde Analysetools eine weitere Prüfung auf übersehene Fehler in der CI-Pipeline stattfindet.

# Begründung, dass Qodana nicht weiter verfolgt wurde

Während der Einrichtung von Qodana ist aufgetaucht, dass unsere Projektstruktur nicht mit der Projektstruktur von Qodana kompatibel ist, da das Gradle-Projekt in einem Unterordner liegt. Verschiedene Einstellungen zu versuchen (siehe unten) hat leider nicht zu einem Ergebnis geführt und aufgrund des hohen Zeitaufwands und des doch verhältnismäßig geringen Nutzen für das Projekt wurde der Versuch dokumentiert und nicht weiter verfolgt.

# Versuch

Festgehalten in folgendem Pull-Request https://github.com/Dungeon-CampusMinden/dsl-ide-support-with-lsp/pull/1

Weiteres unter https://www.jetbrains.com/help/qodana/quick-start.html
1. Auf https://qodana.cloud eine Organisation und ein Projekt anlegen, um ein Project Token zu erhalten.
2. Token im Github Projekt in Settings unter 'Secrets and variables' und 'Actions' als Repository Secret mit dem Namen `QODANA_TOKEN` eintragen. https://github.com/Dungeon-CampusMinden/dsl-ide-support-with-lsp/settings/secrets/actions
3. Github Workflow erstellen, welcher das `QODANA_TOKEN` nutzt: https://www.jetbrains.com/help/qodana/quick-start.html#quickstart-run-in-github
4. Man kann eine `qodana.yaml` anlegen, um JDK, linter und Profil festzulegen. Das Profil, welches von der IntelliJ IDEA genutzt wird liegt unter `.idea/inspectionProfiles/Project_Default.xml`. Die `qodana.yaml` muss im Root-Ordner des Github Repositories liegen, damit die CI Pipeline es findet.
5.  Im IntelliJ Problems ToolWindow kan man im Tab Server-Side-Analysis 'run locally' auswählen und die Analyse ausprobieren. Dabei muss die `qodana.yaml` jedoch im Root-Ordner des Java Projekts liegen. 

# Probleme mit der Projektstruktur

Leider erwartet Qodana, dass das zu analysierende Gradle-Projekt im Root-Ordner des Git Repositories liegt. Sonst wird der Java Code nicht gefunden. Da die Projektstruktur mit der VS-Code Extension jedoch wünschenswert ist, wurden viele Einstellungen probiert, um Qodana trotzdem auszuführen. Zuletzt wurde eine [Discussion im Github von Qodana](https://github.com/JetBrains/Qodana/discussions/250) angelegt, welche jedoch im Sand verlaufen ist. Folgendes wurde zum Beispiel ausprobiert:

- qodana.yaml wie lokal aus IntelliJ Projektordner nutzen (es wird immer eine Default qodana.yaml genommen), versuchtes:
  - qodana action parameter:
      - `args: --project-dir, lsp-server`
      - `args: --project-dir,lsp-server`
      - `args: --project-dir,lsp-server,--profile-path,lsp-server/qodana.yaml`
      - `args: --project-dir,lsp-server,--profile-path,lsp-server/qodana.yaml`
      - `args: --project-dir,lsp-server,--profile-path,lsp-server/qodana.yaml`
      - `args: --project-dir,lsp-server,--profile-path,qodana.yaml`
      - `args: --project-dir,lsp-server,--yaml-name,lsp-server/qodana.yaml`
  -  qodana.yml parameter
     - different jdks
       - `projectJDK: 19`
       - `projectJDK: corretto-19`
     - verschiedene linter
       - `linter: jetbrains/qodana-jvm-community:2024.1`
       - `linter: jetbrains/qodana-jvm-community:2023.3`
       - `linter: jetbrains/qodana-jvm-community:2023.1`
  
 - ohne qodana.yml `args: --source-directory,lsp-server,--profile-path,lsp-server/.idea/inspectionProfiles/Project_Default.xml,--linter,jetbrains/qodana-jvm-community:2023.3`

- qodana.yml im root (funktioniert, aber analysiert keine java dateien)
   - different jdks
     - `projectJDK: 21`
     - `projectJDK: 19`
  - action parameter
    - `args: --source-directory,lsp-server,--clear-cache`
    - `args: --source-directory,lsp-server/**/*`
    - `args: --source-directory,lsp-server/src`
    - damit nicht nur Dateien des neusten Commits analysiert werden:
      - `args: --source-directory,lsp-server,--full-history`
      - `pr-mode: false`
  - anderer linter
    - `linter: jetbrains/qodana-jvm:2023.3`


# Qodana lokal im Docker laufen lassen

Es wurde versucht Qodana CLI zum Debuggen in einem lokalem Docker laufen zu lassen. Dabei konnte man mehr Fehlermeldungen sehen. Installation per `winget install -e --id JetBrains.QodanaCLI` oder: https://github.com/JetBrains/qodana-cli/releases/tag/v2024.1.6
Ausprobiert:

- `qodana scan -project-dir lsp-server`
- `qodana scan -project-dir lsp-server --config qodana.yaml                                                                       `
- `qodana scan -project-dir lsp-server --yaml-name lsp-server/qodana.yaml`
- `qodana scan -h`
- `qodana --skip-pull --source-directory lsp-server --clear-cache --commit CIf7551efd2e5c71df12d3bd1f80ef9602cca7e7aa`
- `qodana --source-directory lsp-server --clear-cache --commit CIf7551efd2e5c71df12d3bd1f80ef9602cca7e7aa`
- `qodana --source-directory lsp-server/**/* --clear-cache --commit CIf7551efd2e5c71df12d3bd1f80ef9602cca7e7aa`
- `qodana --skip-pull --source-directory lsp-server --clear-cache --commit CIf7551efd2e5c71df12d3bd1f80ef9602cca7e7aa`
- `qodana --skip-pull --source-directory lsp-server --clear-cache --commit CIf7551efd2e5c71df12d3bd1f80ef9602cca7e7aa`

# Mögliche Ansatzpunkte

Wenn das Gradle-Projekt direkt der Root-Ordner des Git Projektes ist, sollte Qodana einfacher funktionieren.
Alternativ kann auf der [Discussion im Github von Qodana](https://github.com/JetBrains/Qodana/discussions/250) aufgebaut werden und mit weiteren Parametern ausprobiert werden, ob Änderungen an `gradle.xml` oder Parameter in der Qodana GitHub-Action (vielleicht `--project-dir` mit Pfaden auf lsp-server oder lsp-server/.idea) Qodana das Gradle-Projekt finden lassen.
Vielleicht kann auch die später entdeckte Möglichkeit das working-directory eines github steps zu setzen genutzt werden, [siehe Stackoverflow](https://stackoverflow.com/questions/58139175/running-actions-in-another-directory) und Beispiel:
```
  steps:
    - name: Spotless
      working-directory: ./lsp-server
      run: ./gradlew spotlessJavaCheck
```