# Idee Link Checker

# Problem

Der LSP-Server für die Dungeon DSL bietet Templates für die verschiedenen Aufgabentypen an.
Wird der Vorschlag in der Autovervollständigung angezeigt, wird ein Link zur Dokumentation dargestellt.
Dieser Link verweist auf die Markdown Dokumentation im Dungeon Repository.
Sollte jemand im Dungeon Repository die Dokumentation verschieben, würde der Link nicht mehr funktionieren.

# Lösungsidee

Im Github Workflow/Action eines Pull Requests im Dungeon Repository könnte ein Skript/Programm/Linter ausgeführt werden.
Wenn das Tool erkennt, dass ein verlinktes Dokument durch den Pull Request unerreichbar wird, blockiert es den Merge des PullRequests und meldet, dass die Referenz auf das unerreichbar gewordene Dokument angepasst werden muss.
Dazu müsste das Tool eine Liste der extern referenzierten Links haben.
Gut wäre, wenn in dsl-ide-support-with-lsp eine Datei mit den referenzierten Links erstellt wird (aktuell sind die Links innerhalb von `lsp-server/src/main/java/autocompletion/CompletionItemQuery.java` im Feld `templateCompletionItems` hartkodiert).
Diese Datei könnte dann als Single-Source-Of-Truth dienen und das Workflow-Tool und der LSP-Server entnehmen daraus die Links.
Für den LSP-Server könnten die Links auch per Gradle vor dem Compile injected werden, um das Auslesen zur Laufzeit zu vermeiden.