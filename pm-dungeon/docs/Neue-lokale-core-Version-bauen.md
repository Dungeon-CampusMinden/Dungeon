Wir erstellen normalerweise bei jeder Änderung ein neues Release von `version4` auf GitHub und auf MavenCentral.

Wenn eine Version vom `core` jedoch lokal gebaut und eingebunden werden soll, gehen Sie wie folgt vor:

Im `core`:

1. `git checkout <zielbranch>`
2. `cd code`
3. `gradlew jar`

Es wird dann eine neue `jar`-Datei gebaut: ...`code/build/libs/code.jar`

Im `dungeon-starter` können Sie die neue `jar` in die `build.gradle` einbinden:

1. Ersetzen Sie `implementation "io.github.pm-dungeon:core:4.+"` durch `implementation files("../../core/code/build/libs/code.jar")`.

Danach können Sie `gradlew run` aufrufen, und es wird die neue `jar` verwendet.
