# Wizard UI flow V0.4

Status: schlanker lokaler Authoring-Flow für DEER `0.4`

## Ziel

Die UI führt nicht-technische Lehrende durch diesen Ablauf:

```text
Starten oder fortsetzen
-> Eckdaten
-> Geschichte
-> Spieleinstellungen
-> Eigene Bilder und Dateien
-> Rätsel
-> Spielablauf
-> Spiel-Ende
-> Spiel erstellen
```

Feldsemantik und Runnerregeln stehen in den verlinkten Contract-Dateien. Die
Frontend-/Host-Grenze steht im
[`Frontend-Handoff`](frontend-handoff-overview-v0.md).

## Grundsätze

- Lehrende bearbeiten keine JSON-Datei und sehen keine technischen IDs.
- Der unvollständige private Entwurf ist nicht `deer.json`.
- Vorwärtsnavigation prüft den aktuellen Schritt. Blockierende lokale Fehler
  halten die Lehrkraft dort; Warnungen und Rückwärtsnavigation werden dadurch
  nicht blockiert. Nur eine laufende Dateiübertragung sperrt die
  Schrittnavigation kurz; während Prüfung und Spielerstellung bleibt sie offen.
- Die Navigation zeigt Abschlussgrad, Probleme, Warnungen und den lokalen
  Speicherstatus.
- Die Fehlerübersicht zeigt sofort die Meldungen des aktuellen und aller bereits
  bearbeiteten Schritte. Noch nicht besuchte spätere Schritte bleiben bis zur
  vollständigen Bearbeitung ausgeblendet. Danach zeigt sie alle Meldungen.
- Blockiert die Vorwärtsnavigation, nennt die Meldung den ersten konkreten
  Fehler und verweist auf die anklickbare Fehlerübersicht. Dieselben Meldungen
  stehen am betroffenen Feld oder bei zusammengesetzten Inhalten am engsten
  passenden Abschnitt. Die fachlichen Regeln stammen ausschließlich aus dem
  `ErrorChecker`.
- Der Spielablauf ist ein mandatory AND-DAG mit genau einem geschützten Start-
  und Endknoten und einem Knoten je Rätsel.
- Beim Betreten von `Spiel erstellen` startet die Produktionsprüfung
  automatisch, sobald lokale Prüfung und eigene Dateien bereit sind.
- `Spiel erstellen und herunterladen` setzt einen gültigen aktuellen
  Produktionsreport voraus.
- Warnungen blockieren nicht.
- Eine Prüfung garantiert Struktur und Runnerfähigkeit, nicht menschliche
  Lösbarkeit oder Lernerfolg.

## Entwurfslebenszyklus

Die UI speichert `WizardDraft` v1 und alle Uploadbytes ausschließlich in einer
neuen IndexedDB und bittet den Browser beim Start um dauerhafte Speicherung.
Änderungen werden automatisch gespeichert. Alte Browser- oder AppData-Entwürfe
werden nicht migriert. Genau ein Tab darf gleichzeitig bearbeiten; öffnet ein
weiterer Tab einen Entwurf, erhält er eine klare Warnung. Es gibt keine
tabübergreifende Konfliktauflösung.

Ein Draft darf unvollständig und im Rätselgraphen unverbunden sein. Stabile
Authoring-IDs entstehen beim Anlegen und bleiben bei Umbenennungen erhalten.
Neue Drafts enthalten bereits je einen leeren, nicht löschbaren Eintrag für
Lernziel, Intro-Text und erfolgreichen Abschluss. Beim harten Zeitlimit gilt
dasselbe für den Misserfolgstext.
Ein Draft kann nach ausdrücklicher Bestätigung mit seinen privaten Uploads
gelöscht werden. Bereits heruntergeladene Spieler-JARs bleiben davon
unberührt.

Ein neuer unvollständiger Draft hat keinen echten Seed. Beim ersten Betreten
von `Spiel erstellen` erzeugt die UI einmal einen sicheren
53-Bit-Seed im Bereich `0..9007199254740991`. Sie speichert ihn vor dem
Hostaufruf in IndexedDB. Danach bleibt er für diesen Draft stabil. Der
Java-Host erzeugt keinen Seed.

Der Java-Host läuft fest auf `127.0.0.1:27777` und speichert weder Drafts noch
Uploads. Ein belegter Port führt zu einem klaren Startfehler. Die
Zielgruppen-EXE und ein Installer mit gebündelter Runtime bleiben ein späterer
Meilenstein.

## Starten oder fortsetzen

Die Startansicht bietet:

- neuen Entwurf anlegen;
- letzten lokalen Entwurf fortsetzen;
- anderen lokalen Entwurf öffnen;
- pro Entwurf anzeigen, ob er lokal bereit ist oder wie viele blockierende
  Probleme noch bestehen;
- Entwurf samt privaten Uploads nach Bestätigung löschen.

Beliebiger `deer.json`-Import, Projektordner und Browser-ZIP-Export sind nicht
Teil des sichtbaren V0-Produktflusses. Das fertige Artefakt wird direkt als
`WizardRoom.jar` heruntergeladen.

## Eckdaten und Spieleinstellungen

| Sichtbares Feld | DEER-Ziel |
|---|---|
| Raumtitel | `metadata.title` |
| Inhaltssprache | `metadata.locale` |
| Zielgruppe | `session.targetAudience` |
| Vorwissen | `session.priorKnowledge` |
| Spielerzahl von/bis | `session.playerCount` |
| Zeitlimit und Modus | `session.time` |
| Lernziele | `learningDesign.objectives[]` |

`metadata.id` wird intern einmal erzeugt und bei Umbenennungen beibehalten. Die
UI zeigt sie nicht. Optional sind Beschreibung, Autor, weitere Lernziele und
Fragen für die Nachbesprechung. V0.4 unterstützt `de-DE` als einzige
Inhaltssprache.

Blockierend sind leere Pflichtfelder, kein Lernziel, Spielerzahlen außerhalb
`1..4`, `min > max` und Zeitlimits außerhalb `1..240` Minuten.

## Geschichte

Pflicht sind Mission, Intro-Seiten, Erfolgsseiten und bei hartem Zeitlimit
Fehlschlagseiten. V0.4 nutzt das feste Theme `default`. Jede Storyseite ist ein
eigener weiterklickbarer Black-Fade-Abschnitt; die Mission erscheint als letzte
Intro-Seite.

## Spielablauf

Die UI zeigt genau einen geschützten Startknoten, einen geschützten Endknoten
und einen Knoten je Rätsel. Eine gerichtete Kante bedeutet, dass der folgende
Knoten nach seinem Vorgänger verfügbar wird. Mehrere eingehende Kanten
bedeuten immer, dass alle Vorgänger gelöst sein müssen.

Die UI verhindert doppelte Kanten, Selbstkanten und Zyklen. Sie bietet keine
OR-Verzweigungen, optionalen Pflichträtsel oder freien Kantenbedingungen. Der
DEER-Kandidat übernimmt Knoten und Kanten exakt in ihrer gespeicherten
Reihenfolge. Er ergänzt oder repariert den Graphen nicht.

## Rätsel, Inhalte und Hinweise

Jedes Rätsel hat einen internen stabilen Bezeichner, einen sichtbaren Titel,
mindestens ein Lernziel, eine Zeitschätzung, optional eine Schwierigkeit,
Informationsquellen, mindestens eine Eingabe und optionale geordnete Hinweise.
Alle Eingaben eines Rätsels sind mit AND verknüpft.

Eine Informationsquelle benötigt einen sichtbaren Fundort und mindestens
einen Inhalt. Ein eigenes PNG- oder JPEG-Bild wird mit Bytes und Metadaten im
Browserdraft gespeichert. Ein Bild aus der Spielbibliothek speichert nur seinen
internen Pfad und die Metadaten. Technische Pfade, Hashes und Asset-IDs bleiben
in der UI verborgen.

V0.4 bietet Zahlencode und verpflichtendes Entdecken einer Informationsquelle.
Ein Zahlencode hat einen sichtbaren Gerätenamen und einen Code mit einer bis
acht Ziffern. Falsche Eingaben können im Spiel wiederholt werden.

Hinweise sind geordnet. Die Lehrkraft wählt für jeden Hinweis eine der
sichtbaren Stufen Orientierung, Lösungsweg oder Lösung. Die Runtime verlangt
vor jeder Freigabe eine Bestätigung. Zeit- und Fehlversuchsbedingungen gehören
nicht zu V0.4.

## Spiel-Ende

Die Lehrkraft benennt genau einen Ausgang. Die UI verwaltet World- und
Door-Surface intern. Sobald alle direkten Vorgänger des Endknotens gelöst
sind, öffnet der Host die Tür serverautoritativ.

## Spiel erstellen

Die lokale Prüfung läuft während der Bearbeitung und bildet das Seitengate.
Beim Betreten der letzten Seite projiziert die UI den exakt gespeicherten
Browserdraft auf einen vollständigen DEER-Kandidaten und sendet ihn mit allen
benötigten Uploadbytes automatisch an die Produktionsvalidierung. Die UI
ignoriert veraltete Antworten. Lokale und produktive Probleme erscheinen in
derselben Fehlerübersicht und werden möglichst dem betroffenen Schritt
zugeordnet. Ein Klick auf eine Meldung öffnet diesen Schritt. Technische Codes,
JSON-Pointer und IDs bleiben verborgen.

`Spiel erstellen und herunterladen`:

1. liest den aktuellen gespeicherten Kandidaten und seine Uploadbytes;
2. materialisiert sie nur temporär im Java-Host;
3. validiert den Kandidaten erneut und paketiert nur ein gültiges Projekt als
   `WizardRoom.jar`;
4. liefert die JAR direkt als Browserdownload aus.

Nur ein erfolgreicher Download in der aktuellen UI-Sitzung zeigt `Das Spiel
ist bereit`. Nach einem Reload ist dieser Zustand weg. Ein Packaging-Fehler
aktualisiert die gemeinsame Fehlerübersicht. Ein neuer Versuch erzeugt die JAR
erneut aus dem aktuellen gespeicherten Draft.

Die UI startet die Spieler-JAR nicht. Sie erklärt, dass dieselbe vollständige
JAR an Host und alle weiteren Spielenden verteilt wird. Aktuell benötigt sie
Java 25 und öffnet dann das Host-/Join-Menü. Eine spätere `jpackage`-Ausgabe
kann die Runtime mitliefern, ohne den Authoring-Vertrag zu ändern.

## Blockierende Prüfungen

- fehlende Pflichtangabe;
- doppelte ID oder unbekannte Referenz;
- ungültiges Graphprofil, doppelte Kante oder Zyklus;
- nicht erreichbares Rätsel oder Erfolgsziel;
- nicht unterstützter Rätsel-, Inhalts- oder Assettyp;
- fehlende oder unsichere Datei;
- Informationsquelle ohne Inhalt;
- Rätsel ohne Eingabe;
- unbekannte Formatversion.

## Redaktionelle Selbstprüfung

Die UI stellt diese Fragen als Checkliste, nicht als automatisch erkannte
Probleme:

- Lässt sich die Lösung aus Aufgabe und Material ableiten?
- Ist die Aufgabe für die Zielgruppe verständlich?
- Passen Schwierigkeit und Zeit zum Unterricht?
- Führt jeder Hinweis gezielt weiter?
- Sind Lernziel und Nachbesprechung verbunden?

DEER bleibt bei Version `0.4`. Mandatory AND-DAG, Runner-Identität und
Multiplayervertrag bleiben unverändert.
