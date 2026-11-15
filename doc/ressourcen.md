# Ressourcen: globaler Zustand mit sichtbaren Abhängigkeiten

> **Kurzfassung:** Ein System holt sich globalen Zustand, z. B. das aktuelle Level, **im Konstruktor**
> als Handle `Res<T>` und speichert ihn in einem Feld. In `execute()` wird nur dieser Handle benutzt.
> Neuer Code ruft nie `Game.currentLevel()` oder `Game.tileAt(...)` auf.

## Warum?

Bisher greifen Systems mitten in ihrer Logik über die `Game`-Fassade auf das Level zu. Von außen sieht
man nicht, dass ein System das Level braucht, und jeder Test muss globalen Zustand setzen und wieder
aufräumen.

Mit Ressourcen steht die Abhängigkeit im Konstruktor. Tests geben einfach ihren eigenen Handle hinein
und brauchen keinen globalen Zustand mehr.

## Begriffe

| Begriff | Klasse | Bedeutung |
|---|---|---|
| Ressource | – | Zustand, den es im Spiel genau **einmal** gibt, z. B. das aktuelle Level |
| Registry | [`Resources`](../game/src/engine/resources/Resources.java) | speichert alle Ressourcen. Der Schlüssel ist der Typ, z. B. `ILevel.class` |
| Handle | [`Res<T>`](../game/src/engine/resources/Res.java) | Verweis auf eine Ressource, **nur zum Lesen**. Liest bei jedem `get()` den aktuellen Wert, wird also nach einem Levelwechsel nicht veraltet |
| Tracker | [`Changes`](../game/src/engine/resources/Changes.java) | merkt sich für **ein** System, ob sich eine Ressource seit dem letzten Prüfen geändert hat |
| Ressourcen-Sicht | z. B. [`LevelCollisionView`](../game/src/engine/collision/LevelCollisionView.java) | kleines Objekt, das einen Handle hält und eine Art von Frage zu der Ressource beantwortet |
| Besitzer | z. B. `LevelSystem` | der Einzige, der eine Ressource setzt oder entfernt |

Daten, die es **pro Entity** gibt, bleiben Components. Ressourcen sind nur für Dinge, die es einmal gibt.

## Namen

| Was | Klassenname | Feldname | Beispiel |
|---|---|---|---|
| Handle | `Res<T>` | nach der Ressource | `Res<ILevel> level` |
| Tracker | `Changes` | `<ressource>Changes` | `Changes levelChanges` |
| Ressourcen-Sicht | `<Ressource><Thema>View` | wie die Klasse, in lowerCamelCase | `LevelCollisionView levelCollisionView` |

- **Ressourcen-Sichten enden immer auf `View`**, so wie Systems immer auf `System` enden. Vorne steht
  die Ressource, dahinter das Thema der Fragen: `LevelCollisionView` beantwortet Kollisionsfragen zum
  Level. Spätere Sichten heißen entsprechend, z. B. `LevelPathView` für die Wegfindung oder
  `LevelVisibilityView` für Sicht und Nebel.
- **Ein Feld heißt wie sein Typ in lowerCamelCase**, es sei denn, ein anderer Name sagt deutlich mehr.
  Keine Abkürzungen und keine Namen, die nach etwas anderem klingen: `tiles` würde z. B. nach einer
  Liste von Tiles klingen, nicht nach einer Kollisionsprüfung.
- **Nicht verwenden:** `…Service` (klingt nach eigenem Zustand und Lebenszyklus), `…Query` (wird mit
  Entity-Abfragen im ECS verwechselt) und `…Utils` für etwas, das einen Handle hält.

## Die Regeln

1. **Handle im Konstruktor holen und in einem Feld speichern.** `Resources.…` wird nie in `execute()`
   oder in Hilfsmethoden aufgerufen.
2. **Zwei Konstruktoren:** ein `public`-Konstruktor ohne Parameter für das Spiel und einer **ohne**
   `public` mit einem `Res`-Parameter für Tests.
3. **Helfer bekommen Werte, keine Handles.** Das System holt das Level einmal und reicht es als
   Parameter weiter. Hilfsmethoden rufen weder `Game` noch `Resources` auf. Einzige Ausnahme ist
   eine [Ressourcen-Sicht](#ressourcen-sicht-levelcollisionview) wie `LevelCollisionView`: Sie hält
   selbst einen Handle und wird deshalb wie ein Handle im Konstruktor erzeugt.
4. **Mit einer fehlenden Ressource rechnen.** `get()` liefert ein `Optional`. Ohne Level tut das System
   einfach nichts.
5. **Nur der Besitzer schreibt.** Das Level setzt ausschließlich das `LevelSystem`.
   `Resources.insert(...)` gehört nicht in andere Systems.
6. **`Changes` nur, wenn ein System auf einen Wechsel reagieren muss**, und nur, um seinen **eigenen**
   Zustand anzupassen. Jedes System bekommt seinen eigenen Tracker. Tracker werden nie geteilt.
7. **Keine neuen Aufrufe von `Game.currentLevel()`.** Die Methode ist deprecated und wird nach und
   nach ersetzt.

## Beispiel: `LevelTickSystem`

[`LevelTickSystem`](../game/src/feature/systems/LevelTickSystem.java) ist das Vorbild. Es liest das
Level über einen Handle und erkennt mit `Changes`, ob ein Level neu ist.

**Vorher:** versteckter globaler Zugriff, eigene Erkennung des Levelwechsels

```java
public LevelTickSystem() {
  super(AuthoritativeSide.BOTH);
}

private ILevel currentLevel = null;

public void execute() {
  if (Game.currentLevel().orElse(null) instanceof ITickable tickable) {
    tickable.onTick(currentLevel != Game.currentLevel().orElse(null));
  }
  if (currentLevel != Game.currentLevel().orElse(null)) {
    this.currentLevel = Game.currentLevel().orElse(null);
  }
}
```

**Nachher:** die Abhängigkeit steht im Konstruktor

```java
private final Res<ILevel> level;
private final Changes levelChanges;

public LevelTickSystem() {               // für das Spiel
  this(Resources.level());
}

LevelTickSystem(final Res<ILevel> level) { // für Tests, ohne public
  super(AuthoritativeSide.BOTH);
  this.level = level;
  this.levelChanges = Changes.of(level);
}

public void execute() {
  if (level.get().orElse(null) instanceof ITickable tickable) {
    tickable.onTick(levelChanges.changedSinceLastCheck());
  }
}
```

## Schritt für Schritt: ein System umstellen

1. **Feld anlegen:** `private final Res<ILevel> level;`
2. **Konstruktoren aufteilen:**
   - Den bisherigen Inhalt des Konstruktors in einen neuen Konstruktor **ohne** `public` mit dem
     Parameter `final Res<ILevel> level` verschieben und dort `this.level = level;` setzen.
   - Der `public`-Konstruktor ruft nur noch `this(Resources.level());` auf.
3. **Aufrufe ersetzen:** in der ganzen Klasse, auch in privaten Methoden:

   | vorher | nachher |
   |---|---|
   | `Game.currentLevel()` | `level.get()` |
   | `Game.tileAt(point)` | `level.get().flatMap(l -> l.tileAt(point))` |
   | `Game.tileAtEntity(entity)` | `level.get().flatMap(l -> l.tileAtEntity(entity))` |
   | `Game.startTile()` | `level.get().flatMap(ILevel::startTile)` |
   | `Game.randomTile()` | `level.get().flatMap(ILevel::randomTile)` |

   Wird das Level in einer Methode mehrmals gebraucht, einmal am Anfang holen:
   `ILevel current = level.get().orElse(null); if (current == null) return;`
4. **Helfer mit Level-Zugriff:** Ruft das System einen Helfer auf, der selbst `Game.currentLevel()`
   oder `Game.tileAt(...)` benutzt (z. B. in `LevelUtils`), bekommt der Helfer eine **neue Variante
   mit `ILevel`-Parameter**. Die alte Variante bleibt vorerst und ruft die neue auf. Das System
   benutzt die neue Variante und übergibt sein Level. Für die Frage „Ist diese Position blockiert?“
   gibt es schon fertig [`LevelCollisionView`](#ressourcen-sicht-levelcollisionview).
5. **Test anpassen:** statt `Game.currentLevel(mockedLevel)` das System mit eigenem Handle erzeugen,
   siehe unten. Der Test muss im **gleichen Paket** liegen wie das System (z. B.
   `game/test/engine/systems/` für `engine.systems`), sonst ist der Testkonstruktor nicht sichtbar.
6. **Fertig, wenn:**
   - in der Klasse kein `Game.currentLevel()`, `Game.tileAt(...)` o. Ä. mehr steht,
   - der Test kein `Game.currentLevel(...)` mehr benutzt,
   - `./gradlew :game:test` grün ist.

## Tests

Handles für Tests gibt es fertig:

```java
new MySystem(Res.of(mockedLevel));   // festes Level
new MySystem(Res.empty());           // kein Level geladen
```

Um einen **Levelwechsel** zu simulieren, gibt es den Test-Helfer
[`MutableRes`](../game/test/engine/resources/MutableRes.java) (nur unter `test/`):

```java
MutableRes<ILevel> level = new MutableRes<>(firstLevel);
MySystem system = new MySystem(level);
system.execute();

level.set(secondLevel);   // das System bemerkt den Wechsel im nächsten execute()
system.execute();
```

Ein vollständiges Beispiel steht in
[`LevelTickSystemTest`](../game/test/feature/systems/LevelTickSystemTest.java).

## Ressourcen-Sicht: `LevelCollisionView`

Manche Fragen an das Level stellt ein System sehr oft und aus vielen privaten Methoden heraus, z. B.
„Ist diese Position blockiert?“ im `MoveSystem`. Das Level dafür durch jede Methode zu reichen, wäre
umständlich. Dafür gibt es **Ressourcen-Sichten**: kleine Objekte, die einen Handle halten und genau
eine Art von Frage beantworten.

[`LevelCollisionView`](../game/src/engine/collision/LevelCollisionView.java) beantwortet, ob eine
Position oder ein Collider an einer Position im aktuellen Level blockiert ist:

```java
private final LevelCollisionView levelCollisionView;

MoveSystem(final Res<ILevel> level) {
  super(VelocityComponent.class, PositionComponent.class);
  this.levelCollisionView = new LevelCollisionView(level); // wie ein Handle im Konstruktor erzeugen
}

// irgendwo in der Logik:
if (levelCollisionView.isBlocked(collider, newPos, vc.canEnterWalls())) { … }
```

- Eine Position ist **blockiert**, wenn es kein Level gibt, kein Tile an der Position oder das Tile
  nicht betreten werden darf. Wände dürfen Entities mit `canEnterWalls()` trotzdem betreten.
- Vorbilder sind [`MoveSystem`](../game/src/engine/systems/MoveSystem.java) und `CollisionSystem`.
  Der Test [`MoveSystemTest`](../game/test/engine/systems/MoveSystemTest.java) setzt sein Level mit
  `MutableRes`, ganz ohne `Game.currentLevel(...)`.
- Eine neue Ressourcen-Sicht lohnt sich erst, wenn mehrere Systems dieselbe Frage stellen. Für eine
  einzelne Abfrage reicht `level.get()`.

## Auf einen Levelwechsel reagieren: `Changes`

Die meisten Systems lesen nur das aktuelle Level und brauchen `Changes` **nicht**. Nur wer wissen muss,
**ob** das Level neu ist, legt einen Tracker an:

```java
this.levelChanges = Changes.of(level);       // im Konstruktor

if (levelChanges.changedSinceLastCheck()) {  // in execute()
  // eigenen Zustand an das neue Level anpassen, z. B. einen Cache leeren
}
```

Das muss man dazu wissen:

- **Der erste Check liefert immer `true`.** So reagiert auch ein System, das erst nach dem Laden des
  Levels dazukommt.
- **Höchstens einmal pro `execute()` prüfen.** Der Aufruf markiert die Änderung als gesehen. Wird das
  Ergebnis mehrmals gebraucht, in einer lokalen Variable speichern.
- **Jeder Tracker zählt für sich.** Zwei Systems mit eigenem Tracker bemerken denselben Wechsel beide.
  Ein geteilter Tracker würde ihn nur einem von beiden melden.
- **Nur für den eigenen Zustand.** `Changes` meldet den Wechsel erst im nächsten Lauf des Systems.
  Zustand, in den auch andere schreiben (z. B. Shader, die ein Raum für das neue Level setzt), darf
  damit **nicht** aufgeräumt werden. Er würde sonst nach dem Aufbau des neuen Levels gelöscht.
- **Nur das Ersetzen wird erkannt.** Ändert sich etwas **innerhalb** des Levels, z. B. mit
  `changeTileElementType(...)`, meldet `Changes` nichts.

## Fortgeschritten: eigene Ressourcen in Features

Eine Ressource, die zu einem Feature oder Raum gehört, bekommt **keinen** Getter in `Resources`, denn
`engine` darf nicht von `feature` abhängen. Stattdessen bringt der Typ seinen Zugang selbst mit:

```java
public final class SolidIndex {
  public static Res<SolidIndex> res() {
    return Resources.res(SolidIndex.class);
  }
}

Resources.insert(SolidIndex.class, new SolidIndex());   // nur im Besitzer
this.solids = SolidIndex.res();                         // in lesenden Systems
```

Leser müssen damit rechnen, dass es die Ressource nicht gibt, z. B. weil das besitzende System in
diesem Raum nicht registriert ist.

## Git

- **Branch immer von unserem `master` anlegen**, mit der Ticketnummer im Namen:

  ```bash
  git switch master
  git pull
  git switch -c <ticketnr>-<beschreibung>
  ```

- **GitHub Desktop:** Unter *Repository → Repository Settings → Fork Behavior* „For my own purposes“
  wählen. Sonst legt Desktop neue Branches auf `upstream/master` (dem Original-Repo) an, und es
  entstehen viele Merge-Konflikte.
- **Commits und PRs** beginnen mit der Ticketnummer, siehe [CONTRIBUTING](../CONTRIBUTING.md), z. B.
  `31: use level resource in FrictionSystem`.
