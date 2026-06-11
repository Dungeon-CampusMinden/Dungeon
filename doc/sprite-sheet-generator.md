# Arbeiten mit dem `sprite-sheet-generator.py`

Mit dem neuen DrawSystem ermöglicht der Dungeon nun die einfache Nutzung von Spritesheets. Zum Generieren der Spritesheets aus den bestehenden Assets mit vielen verschiedenen Zuständen und Bildern für die Animationen wurde ein Python script im Root Verzeichnis des Projekts erstellt: `sprite-sheet-generator.py`.

---

# Bedienungsanleitung

Dieses Script dient zum Erstellen, Kombinieren und Zerlegen von **Sprite Sheets** für Animationen in Spielen.
Es arbeitet mit Bilddateien (`.png`, `.jpg`, `.jpeg`) und erzeugt `.png` Spritesheets sowie passende **AnimationConfigs** im `.json`-Format.

---

## Installation

1. Stelle sicher, dass **Python 3** installiert ist.

2. Installiere die benötigten Pakete:

```bash
pip install pillow
```

---

## Verwendung

### 1. Multi-Modus (Standard)

Im **Multi-Modus** wird ein Ordner mit mehreren Unterordnern verarbeitet.
Jeder Unterordner stellt eine **Animation** dar (z. B. `idle`, `run`).
Das Script erzeugt für jeden Unterordner zunächst ein einzelnes Sprite Sheet und fügt sie anschließend zu einem großen Sheet zusammen.

**Beispielstruktur:**

```
wizard/
  idle/
    img01.png
    img02.png
  run/
    img01.png
    img02.png
```

**Befehl:**

```bash
python sprite-sheet-generator.py wizard
```

**Ergebnis:**

* `wizard.png` → gesamtes Sprite Sheet aller Animationen
* `wizard.json` → passende Konfigurationsdatei
* Einzelbilder (`idle.png`, `run.png`) werden nach dem Kombinieren automatisch gelöscht

---

### 2. Single-Modus

Falls nur **ein Ordner mit Bildern** vorliegt, nutze `--single`.

**Beispielstruktur:**

```
idle/
  img01.png
  img02.png
  img03.png
```

**Befehl:**

```bash
python sprite-sheet-generator.py idle --single
```

**Ergebnis:**

* `idle.png` → Sprite Sheet mit allen Einzelbildern
* `idle.json` → passende Konfigurationsdatei

---

### 3. Stapel-Modus (`--stack`)

Mit `--stack` werden alle Bilder **untereinander** gestapelt (statt in einer Raster-Form).
Das ist besonders nützlich, wenn Bilder **unterschiedliche Dimensionen** haben.

#### Regeln für `--stack`:

* Ohne `--stack`:

    * Alle Bilder müssen **dieselbe Breite und Höhe** haben.
    * Das Script arrangiert die Bilder standardmäßig in **einer Reihe** nebeneinander, wenn es **≤ 8 Bilder** gibt.
    * Ab **9 Bildern** wird automatisch ein **Grid** erstellt (max. 8 Spalten, mehrere Zeilen).
* Mit `--stack`:

    * Bilder werden **immer vertikal** untereinander angeordnet.
    * Unterschiedliche Bildgrößen sind erlaubt.
    * Kein Grid-Aufbau, unabhängig von der Bildanzahl.

**Beispiel:**

```bash
python sprite-sheet-generator.py idle --single --stack
```

---

### 4. Unpack-Modus (`--unpack`)

Mit `--unpack` lässt sich ein Sprite Sheet wieder in Einzelbilder zerlegen.
Es wird davon ausgegangen, dass sowohl `.png` als auch `.json` vorhanden sind.

**Beispielstruktur:**

```
objects/
  stone/
    stone.png
    stone.json
```


**Beispiel:**

```bash
python sprite-sheet-generator.py objects/stone/stone --unpack
```

**Ergebnis:**

Aus `stone.png` und `stone.json` werden Ordner erstellt (`idle/`, `run/` ...), in denen die Einzelbilder (`img01.png`, `img02.png` ...) abgelegt werden.

---

## Übersicht der Optionen

| Option     | Beschreibung                                                               |
| ---------- | -------------------------------------------------------------------------- |
| *(ohne)*   | Multi-Modus (Standard): Verarbeitet alle Unterordner                       |
| `--single` | Verarbeitet nur einen Ordner mit Bildern                                   |
| `--stack`  | Stapelt Bilder vertikal, erlaubt unterschiedliche Bildgrößen               |
| `--unpack` | Zerlegt ein bestehendes Sprite Sheet anhand der JSON-Datei in Einzelbilder |

---

## Hinweise

* JSON-Dateien enthalten alle Parameter (auch Standardwerte), sodass Anpassungen leicht möglich sind.
* Im Multi-Modus werden temporär erzeugte Einzel-Sheets nach dem Kombinieren automatisch gelöscht.
* Das Script arbeitet nur **eine Ebene tief** (kein rekursives Durchsuchen weiterer Unterordner).

## Beispiel JSON Datei

Die Sprites können beliebige Größen haben. Die resultierende Animation geht davon aus, dass das Sprite in der kleinsten Dimension genau eine Einheit in Weltkoordinaten groß ist. Der Wizard aus dem Beispiel unten ist `16 x 28` Pixel groß, womit er in der Welt eine größe von `1 x 1.75` Einheiten hat. Wird die y-Skalierung auf `0` gesetzt, entspricht sie der gesetzten x-Skalierung.

```json
{
  "idle_down": {
    "config": {
      "spriteWidth": 16,
      "spriteHeight": 28,
      "x": 0,
      "y": 0,
      "rows": 1,
      "columns": 2
    },
    "framesPerSprite": 20,
    "scaleX": 1,
    "scaleY": 0,
    "isLooping": true,
    "centered": false
  },
  "idle_left": {
    "config": {
      "spriteWidth": 16,
      "spriteHeight": 28,
      "x": 0,
      "y": 56,
      "rows": 1,
      "columns": 4
    },
    "framesPerSprite": 30,
    "scaleX": 1,
    "scaleY": 0,
    "isLooping": true,
    "centered": false
  },
  "idle_right": {
    "config": {
      "spriteWidth": 16,
      "spriteHeight": 28,
      "x": 0,
      "y": 84,
      "rows": 1,
      "columns": 4
    },
    "framesPerSprite": 30,
    "scaleX": 1,
    "scaleY": 0,
    "isLooping": true,
    "centered": false
  },
  "idle_up": {
    "config": {
      "spriteWidth": 16,
      "spriteHeight": 28,
      "x": 0,
      "y": 112,
      "rows": 1,
      "columns": 3
    },
    "framesPerSprite": 30,
    "scaleX": 1,
    "scaleY": 0,
    "isLooping": true,
    "centered": false
  },
  "run_down": {
    "config": {
      "spriteWidth": 16,
      "spriteHeight": 28,
      "x": 0,
      "y": 28,
      "rows": 1,
      "columns": 4
    },
    "framesPerSprite": 10,
    "scaleX": 1,
    "scaleY": 0,
    "isLooping": true,
    "centered": false
  },
  "run_left": {
    "config": {
      "spriteWidth": 16,
      "spriteHeight": 28,
      "x": 0,
      "y": 56,
      "rows": 1,
      "columns": 4
    },
    "framesPerSprite": 10,
    "scaleX": 1,
    "scaleY": 0,
    "isLooping": true,
    "centered": false
  },
  "run_right": {
    "config": {
      "spriteWidth": 16,
      "spriteHeight": 28,
      "x": 0,
      "y": 84,
      "rows": 1,
      "columns": 4
    },
    "framesPerSprite": 10,
    "scaleX": 1,
    "scaleY": 0,
    "isLooping": true,
    "centered": false
  },
  "run_up": {
    "config": {
      "spriteWidth": 16,
      "spriteHeight": 28,
      "x": 0,
      "y": 112,
      "rows": 1,
      "columns": 3
    },
    "framesPerSprite": 10,
    "scaleX": 1,
    "scaleY": 0,
    "isLooping": true,
    "centered": false
  }
}
```
