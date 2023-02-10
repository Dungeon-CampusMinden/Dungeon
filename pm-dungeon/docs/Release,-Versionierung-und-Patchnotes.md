Ein Release muss manuell im GitHub-Repository durchgeführt werden. Der Upload auf Mavencentral erfolgt nach einem GitHub-Release automatisch. 

Um einen Release über GitHub zu erstellen muss auf der [Releases-Page](https://github.com/PM-Dungeon/core/releases) die Schaltfläche "Draft a new release" angeklickt werden. 

## Versionierung
Für jeden Release muss ein neuer Tag erstellt werden. Tags geben die Versionsnummer an und sind nachfolgendem Schema aufgebaut. 

`MAJOR.MINOR.PATCH`  
`MAJOR` bei nicht abwärtskompatiblen API-Änderungen  
`MINOR` bei neuen (zusätzlichen) Features  
`PATCH` bei Fehlerbehebungen ohne Features/API-Änderungen.  

## Patchnotes
Für jeden neuen Release müssen Patchnotes geschrieben werden, welche alle Änderungen zum Vorherigen Release auflisten.
Um die Patchnotes übersichtlich zu halten, sollten für `MAJOR` und `MINOR` Änderungen nur Kurzbeschreibungen mit Verlinkung zu einer ausführlicheren Dokumentation erstellt werden. Für Änderungen der Kategorie `PATCH` reicht ein kurzer Satz, der beschreibt welches Fehlverhalten behoben wurde.   
*Anmerkung: ggf. Kann es Sinnvoll sein, einzelne Issues bzw. PR in den Patchnotes zu verlinken*   

Zum Erstellen der Patch-Notes kann Git verwendet werden: 
```
git shortlog <lastrev>..HEAD --format="[%h] %s"
``` 
(dabei `<lastref>` mit dem Commit/Tag des letzten Releases ersetzen).
***

Eine ähnliche Patch-Notes-Übersicht kann auch mithilfe des Buttons "Auto-generate release notes" auf GitHub erstellt werden:

![Auto-generate release notes](https://user-images.githubusercontent.com/85501570/151417191-a2e244dd-be88-49a8-9f6c-a33d52eae26f.png)
