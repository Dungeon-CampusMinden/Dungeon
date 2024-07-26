- [Idee](#idee)
- [Versuch](#versuch)
  - [VS Code Extension](#vs-code-extension)
    - [manuelle Analyse](#manuelle-analyse)
    - [license-report](#license-report)
    - [license-report-recursive](#license-report-recursive)
    - [license-report-generator](#license-report-generator)
  - [LSP Server](#lsp-server)
    - [Gradle-License-Report ](#gradle-license-report-)
      - [Apache-2.0](#apache-20)
    - [gradle-licenses-plugin](#gradle-licenses-plugin)
- [Fazit](#fazit)
- [Recherche zur möglichen Lizenz für das Gesamtprojekt](#recherche-zur-möglichen-lizenz-für-das-gesamtprojekt)


# Idee

Der Language Server Protocol (LSP) Server soll mit einer Visual Studio Code (VSCode) Extension ausgeliefert werden.
Rechtlich sauber und urheberrechtlich fair muss diese Auslieferung die Lizenzen der mit auszuliefernden Drittanbieter-Software auflisten, enthalten und in der Lizenzierung der Gesamtsoftware berücksichtigen.
Die Liste der eingebundenen Softwarepakete soll dabei automatisiert ermittelt werden, sinnvoll dargestellt werden und zusammen mit den entsprechenden Lizenzen in die ausgelieferte Software gepackt werden.

# Versuch

## VS Code Extension

Die VS Code Extension wird mit npm erstellt. Gesucht ist ein npm Paket, welches automatisch alle Abhängigkeiten finden und mit Lizenzen auflisten. Die Lizenzen der Abhängigkeiten legt npm bereits automatisch neben die Software in das Endergebnis (.vsix).
Eine manuelle Analyse soll zuerst zeigen, welche Abhängigkeiten in der .vsix zu finden sind.


### manuelle Analyse

Die auszuliefernde .vsix Datei lässt sich mit 7zip öffnen. Die npm Abhängigkeiten sind rekursiv im Ordner node_modules zu finden und jeweils in einer package.json unter dem key dependencies definiert.
Folgender Abhängigkeitsbaum ergibt sich aus der rekursiven manuellen Analyse der package.json Dateien und der im .vsix enthaltenen Dateien:

- eigene .vsix package.json
  - vscode-languageclient MIT
    - minimatch ISC
      - brace-expansion MIT
        - balanced-match MIT
    - semver ISC
      - lru-cache ISC
        - yallist ISC
    - vscode-languageserver-protocol MIT
      - vscode-jsonrpc MIT
      - vscode-languageserver-types MIT


Folgende relevante npm Pakete wurden ausprobiert, mit angegebenem Ergebnis:

### [license-report](https://www.npmjs.com/package/license-report)

Das Paket findet nur die direkte Abhängigkeit.
Installiert wird das Paket mit `npm i -g license-report`. Genutzt wurde folgende Konfigurationsdatei im Kommando mit dem nachfolgenden Ergebnis.

Konfigurationsdatei:
```json
{
    "only": "prod",
    "output": "markdown",
    "fields": [
        "name",
        "licenseType",
        "description",
        "link",
        "installedVersion",
        "author"
    ],
    "description": {
      "value": "n/a",
      "label": "Description"
    }
}
```
Kommando ausgeführt im `vscode-extension` Ordner: `license-report --config license-report-config.json`

Ergebnis:
| Name                  | License type | Description                           | Link                                                            | Installed version | Author                |
| :-------------------- | :----------- | :------------------------------------ | :-------------------------------------------------------------- | :---------------- | :-------------------- |
| vscode-languageclient | MIT          | VSCode Language client implementation | git+https://github.com/Microsoft/vscode-languageserver-node.git | 9.0.1             | Microsoft Corporation |

### [license-report-recursive](https://www.npmjs.com/package/license-report-recursive)

Weniger verbreitet und gepflegter Fork vom bereits getesteten [license-report](#license-report), welcher jedoch rekursive Abhängigkeiten auflöst, aber die mit .7zip in der .vsix gefundene Abhängigkeit yallist nicht findet. Das könnte daran liegen, dass andere package versionen die Abhängigkeit nicht enthalten und license-report-recursive nicht die eingetragene Version verfolgt.

Installation: `npm i -g license-report-recursive`

Kommando ausgeführt im `vscode-extension` Ordner:  `license-report-recursive --config license-report-config.json`

Ergebnis:
| Name                           | License type | Description                                                | Link                                                            | Installed version | Author                                                      |
| :----------------------------- | :----------- | :--------------------------------------------------------- | :-------------------------------------------------------------- | :---------------- | :---------------------------------------------------------- |
| balanced-match                 | MIT          | Match balanced character pairs, like "{" and "}"           | git://github.com/juliangruber/balanced-match.git                | 1.0.2             | Julian Gruber mail@juliangruber.com http://juliangruber.com |
| brace-expansion                | MIT          | Brace expansion as known from sh/bash                      | git://github.com/juliangruber/brace-expansion.git               | 2.0.1             | Julian Gruber mail@juliangruber.com http://juliangruber.com |
| lru-cache                      | ISC          | A cache object that deletes the least-recently-used items. | git://github.com/isaacs/node-lru-cache.git                      | 6.0.0             | Isaac Z. Schlueter <i@izs.me>                               |
| minimatch                      | ISC          | a glob matcher in javascript                               | git://github.com/isaacs/minimatch.git                           | 5.1.6             | Isaac Z. Schlueter <i@izs.me> (http://blog.izs.me)          |
| semver                         | ISC          | The semantic version parser used by npm.                   | git+https://github.com/npm/node-semver.git                      | 7.6.0             | GitHub Inc.                                                 |
| vscode-jsonrpc                 | MIT          | A json rpc implementation over streams                     | git+https://github.com/Microsoft/vscode-languageserver-node.git | 8.2.0             | Microsoft Corporation                                       |
| vscode-languageclient          | MIT          | VSCode Language client implementation                      | git+https://github.com/Microsoft/vscode-languageserver-node.git | 9.0.1             | Microsoft Corporation                                       |
| vscode-languageserver-protocol | MIT          | VSCode Language Server Protocol implementation             | git+https://github.com/Microsoft/vscode-languageserver-node.git | 3.17.5            | Microsoft Corporation                                       |
| vscode-languageserver-types    | MIT          | Types used by the Language server for node                 | git+https://github.com/Microsoft/vscode-languageserver-node.git | 3.17.5            | Microsoft Corporation                                       |


### [license-report-generator](https://www.npmjs.com/package/@wbmnky/license-report-generator)

Weniger verbreitet als [license-report](#license-report), findet license-report-generator Pakete, welche nicht in den ausgelieferten Dateien zu finden sind.

Installation: `npm i -g @wbmnky/license-report-generator`

Kommando ausgeführt im `vscode-extension` Ordner:  `license-report-generator --depth 65 --table`

Ergebnis:
| Package Name                   | Version | URL                                             | Description                                                                       | License |
| ------------------------------ | ------- | ----------------------------------------------- | --------------------------------------------------------------------------------- | ------- |
| ansi-regex                     | 6.0.1   | -                                               | Regular expression for matching ANSI escape codes                                 | MIT     |
| ansi-styles                    | 6.2.1   | -                                               | ANSI escape codes for styling strings in the terminal                             | MIT     |
| balanced-match                 | 1.0.2   | https://github.com/juliangruber/balanced-match  | Match balanced character pairs, like &quot;{&quot; and &quot;}&quot;              | MIT     |
| brace-expansion                | 2.0.1   | https://github.com/juliangruber/brace-expansion | Brace expansion as known from sh/bash                                             | MIT     |
| eastasianwidth                 | 0.2.0   | -                                               | Get East Asian Width from a character.                                            | MIT     |
| emoji-regex                    | 9.2.2   | https://mths.be/emoji-regex                     | A regular expression to match all Emoji-only symbols as per the Unicode Standard. | MIT     |
| is-fullwidth-code-point        | 3.0.0   | -                                               | Check if the character represented by a given Unicode code point is fullwidth     | MIT     |
| lru-cache                      | 6.0.0   | -                                               | A cache object that deletes the least-recently-used items.                        | ISC     |
| minimatch                      | 5.1.6   | -                                               | a glob matcher in javascript                                                      | ISC     |
| semver                         | 7.6.0   | -                                               | The semantic version parser used by npm.                                          | ISC     |
| string-width                   | 4.2.3   | -                                               | Get the visual width of a string - the number of columns required to display it   | MIT     |
| strip-ansi                     | 7.1.0   | -                                               | Strip ANSI escape codes from a string                                             | MIT     |
| vscode-jsonrpc                 | 8.2.0   | -                                               | A json rpc implementation over streams                                            | MIT     |
| vscode-languageclient          | 9.0.1   | -                                               | VSCode Language client implementation                                             | MIT     |
| vscode-languageserver-protocol | 3.17.5  | -                                               | VSCode Language Server Protocol implementation                                    | MIT     |
| vscode-languageserver-types    | 3.17.5  | -                                               | Types used by the Language server for node                                        | MIT     |
| wrap-ansi                      | 7.0.0   | -                                               | Wordwrap a string with ANSI escape codes                                          | MIT     |
| yallist                        | 4.0.0   | -                                               | Yet Another Linked List                                                           | ISC     |


## LSP Server

Die LSP Server wird mit gradle erstellt. Gesucht ist ein gradle plugin, welches automatisch alle Abhängigkeiten finden und mit Lizenzen auflisten. Die Lizenzen der Abhängigkeiten müssten zusätzlich automatisch neben die Software in das Endergebnis (.jar) gelegt werden, was gradle nicht macht.


### [Gradle-License-Report ](https://github.com/jk1/Gradle-License-Report)

Gradle-License-Report kann unter anderem Markdown generieren und wird regelmäßig gepflegt.

Installation in build.gradle.kts
```kts
plugins {
    id("com.github.jk1.dependency-license-report") version "2.6"
}
licenseReport {
    renderers = arrayOf(InventoryMarkdownReportRenderer())
    outputDir = layout.projectDirectory.dir("..").dir("licenses-of-deployed-dependencies").dir("lsp-server-via-gradle").toString()
}
```
Ausführen mit gradle task reporting/generateLicenseReport

Ergebnis unter build/reports/dependency-license:

#### Apache-2.0
___

**1** **Group:** `com.google.code.gson` **Name:** `gson` **Version:** `2.10.1` 
> - **Manifest Project URL**: [https://github.com/google/gson/gson](https://github.com/google/gson/gson)
> - **Manifest License**: "Apache-2.0";link="https://www.apache.org/licenses/LICENSE-2.0.txt" (Not Packaged)
> - **POM License**: Apache-2.0 - [https://www.apache.org/licenses/LICENSE-2.0.txt](https://www.apache.org/licenses/LICENSE-2.0.txt)

Eclipse Public License, Version 2.0
___

**2** **Group:** `org.eclipse.lsp4j` **Name:** `org.eclipse.lsp4j` **Version:** `0.22.0` 
> - **POM Project URL**: [https://github.com/eclipse-lsp4j/lsp4j](https://github.com/eclipse-lsp4j/lsp4j)
> - **POM License**: Eclipse Public License, Version 2.0 - [http://www.eclipse.org/legal/epl-2.0](http://www.eclipse.org/legal/epl-2.0)
> - **Embedded license files**: [org.eclipse.lsp4j-0.22.0.jar/about.html](org.eclipse.lsp4j-0.22.0.jar/about.html)

**3** **Group:** `org.eclipse.lsp4j` **Name:** `org.eclipse.lsp4j.jsonrpc` **Version:** `0.22.0` 
> - **POM Project URL**: [https://github.com/eclipse-lsp4j/lsp4j](https://github.com/eclipse-lsp4j/lsp4j)
> - **POM License**: Eclipse Public License, Version 2.0 - [http://www.eclipse.org/legal/epl-2.0](http://www.eclipse.org/legal/epl-2.0)
> - **Embedded license files**: [org.eclipse.lsp4j.jsonrpc-0.22.0.jar/about.html](org.eclipse.lsp4j.jsonrpc-0.22.0.jar/about.html)




### [gradle-licenses-plugin](https://github.com/chrimaeon/gradle-licenses-plugin)

Weniger gepflegtes gradle plugin, welches ebenso keine rekursiven Abhängigkeiten auflöst.

Installation in build.gradle.kts
```kts
plugins {
    id("com.cmgapps.licenses") version "4.7.0"
}
licenses {
    reports {
        html.enabled = false
        markdown.enabled = true
    }
}
```
Ausführen mit gradle task reporting/licenseReport

Ergebnis unter build/reports/licenses.md:
* LSP4J
```
Eclipse Public License, Version 2.0
http://www.eclipse.org/legal/epl-2.0
```

# Fazit

Ohne größeren Aufwand ist es nicht möglich eine saubere Auflistung der npm Abhängigkeiten automatisch zu erstellen, da die relevanten Pakete nicht die richtigen Abhängigkeiten finden. Unter gradle ist dies möglich, aber man muss die Lizenzen manuell oder durch weitere plugins neben die eingebundene Software ablegen. Da die korrekte Lizenzierung unklar ist und den Rahmen zu sprengen scheint, wird vorerst auf eine automatische Auslieferung verzichtet. Stattdessen wird in der README beschrieben, wie Nutzer lokal ein .vsix bauen können und später könnte dafür ein Docker image bereitgestellt werden.

# Recherche zur möglichen Lizenz für das Gesamtprojekt

Das Ziel ist das Projekt unter MIT-Lizenz ausliefern zu können.
Wenn eingebundene Software unter sogenannten copyleft licenses steht, kann es jedoch sein, dass die gesamte Software unter diese Lizenz gestellt werden muss.
Sogenannte Permissive licenses haben keine Anforderungen an die weitere Lizenzierung.
[source and good overview article](https://www.mend.io/blog/top-open-source-licenses-explained/#Types_of_software_license_copyleft_and_permissive)

Die einzige copyleft license bisher ist durch lsp4j eingebunden. lsp4j wird jedoch unter zwei Lizenzen veröffentlicht (Eclipse Public License v. 2.0 und Eclipse Distribution License v. 1.0, a BSD-3-clause license). Bisher ist uns nicht klar, wie man die permissive license BSD 3 wählt. Die standardmäßig genutzte Eclipse Public License (EPL) 2.0 ist eine [weak copyleft open source license and 'merely interfacing or interoperating' does not require licensing under EPL](https://www.mend.io/blog/top-10-eclipse-public-license-questions-answered/#2_Is_it_considered_a_copyleft_license)


 