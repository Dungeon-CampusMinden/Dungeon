## Veröffentlichung auf MavenCentral

Um ein Artefakt auf MavenCentral/Sonatype zu veröffentlichen, müssen verschiedene Vorgaben von MavenCentral eingehalten werden:

- Das Buildfile darf keine Abhängigkeiten enthalten, die gefährlich sind
- Das Buildfile sollte die sources und die javadocs generieren
- Das Buildfile muss für alles Signaturen erstellen, dafür müssen alle Files mit GPG signiert werden
- Das Buildfile muss eine `groupId`, `artifactId` und Versionsnummer enthalten
- Das Buildfile muss verschiedene Zusatzinformationen bereitstellen, dazu zählen:
  - der Projektname,
  - die Projektbeschreibung,
  - die Projekt-URL,
  - die Lizenzinformationen,
  - die SCM-URL,
  - und Informationen über die beteiligten Entwickler:
    - ID,
    - Name,
    - E-Mail.

Weitere Informationen: https://central.sonatype.org/publish/publish-guide/

## GPG-Verschlüsselung

Alle Artefakte bzw. JARs müssen signiert sein. Neue Releases werden automatisch mit einem GPG-Schlüssel signiert, sobald die GitHub-Action getriggert wird, also sobald eine neues Release angelegt wurde.

Weitere Informationen: https://central.sonatype.org/publish/requirements/gpg/

## Publish- bzw. Staging-Prozess

Der aktuelle "Staging"-Prozess kann hier eingesehen werden: https://s01.oss.sonatype.org/#welcome

Weitere Informationen:
- https://s01.oss.sonatype.org/#Documentation
- https://help.sonatype.com/repomanager3/nexus-repository-administration/staging
