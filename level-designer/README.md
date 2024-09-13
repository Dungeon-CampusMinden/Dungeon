# Level-Designer

Der Level-Designer hilft bei der Erstellung von Karten für ein Level. Für die
Konfiguration des Levels wird der folgende Tilemap-Editor verwendet:
https://www.spritefusion.com/editor. In den folgenden Schritten wird erklärt wie
der Level-Designer zu verwenden ist.

## Schritt 1: Tilemap erstellen

In dem Online-Editor https://www.spritefusion.com/editor kann ein neues Projekt
gestartet werden oder eines der Beispielprojekte aus dem Ordner "example_projects"
geladen werden. Wenn ein neues Projekt erstellt wird kann ein tileset importiert
werden. Dabei kann als tile_set die Datei "tile_set_spritesheet.png" verwendet
werden. Es besteht auch die Möglichkeit sich ein eigenes tileset zu bauen über die
folgende URL: https://www.codeandweb.com/tp-online. Mit welchen tiles gearbeitet
wird, ist für die Erstellung einer ".level" Datei egal.

### Layer erstellen

Wenn das Projekt so weit erstellt wurde müssen die Layer erstellt werden. Diese
sind später entscheidend, um die Tilemap aus dem Editor in das Format des Dungeons
zu übertragen. Es können die folgenden Layer angelegt werden:
- hero
- custom
- hole
- pit
- exit
- floor
- wall
- door
- skip

## Schritt 2: Tilemap exportieren

Über den Button "Export" kann die erstellte Tilemap exportiert werden. Für die
Übertragung in den Dungeon muss die Tilemap als JSON exportiert werden. Diese JSON
muss dann in den Ordner ``src_maps`` kopiert werden.

## Schritt 3: Level Datei generieren

Um letztendlich die Level Datei zu generieren, welche vom Dungeon geladen werden
kann, muss das Python-Skript ``build_level_file``.py ausgeführt werden. Die
generierten Dateien sind in dem Ordner ``dist_maps`` zu finden. In dieser Datei
kann in der ersten Zeile das Theme des Levels manuell nachgetragen werden. Wenn
die erste Zeile leer ist, wird ein zufälliges Theme ausgewählt. Die verfügbaren
Themes sind mit ihren Wahrscheinlichkeiten bei einer zufälligen Auswahl des
Themes im folgenden aufgelistet:

- DEFAULT (50% Chance)
- FIRE (0% Chance, keine geschlossenen Türen vorhanden)
- FOREST (9% Chance)
- ICE (10% Chance)
- TEMPLE (30% Chance)
- DARK (0% Chance, keine geschlossenen Türen vorhanden)
- RAINBOW( (1% Chance)
