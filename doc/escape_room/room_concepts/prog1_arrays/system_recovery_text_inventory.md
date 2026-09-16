# System Recovery: Textinventar

Arbeitsstand der aktuell implementierten Texte. Die Reihenfolge folgt dem tatsächlichen Spielfluss. Die deutsche Fassung steht hier stellvertretend für beide Lokalisierungen; jede Änderung muss in `de.json` und `en.json` unter demselben Schlüssel nachvollzogen werden.

## Legende

- **Dialog**: Last-Hour-artiger Story- oder Telefon-Dialog.
- **Welttext**: Text an einem Display, Objekt, Label, Keypad oder einer Maschine.
- **Questlog**: Eintrag im gemeinsamen Questlog.
- **Hinweis**: Eintrag, den die leitende KI auf Anfrage über das Telefon liefert.
- **UI**: Computer-, Terminal- oder Debug-Oberfläche.

## 0. Spielstart und Freischaltung

### 0.1 Intro-Lore

**Kontext:** Beim Start des Spiels, noch bevor der erste Telefonanruf beginnt.

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `systemRecovery.intro.title` | `SYSTEM RECOVERY` | Intro lesen. |
| `systemRecovery.intro.page1` | `Du bist kein Mensch. Du bist ein wiederherstellbarer Teil einer lernenden System-KI.` | Keine Aktion. |
| `systemRecovery.intro.page2` | `Ein unbekannter Benutzer hat dich in eine beschädigte Forschungsanlage eingewiesen. Die zentrale Steuerung antwortet nicht mehr.` | Keine Aktion. |
| `systemRecovery.intro.page3` | `Deine Umgebung ist voller unvollständiger Programme, blockierter Geräte und Daten, die nur durch korrektes Denken zugänglich werden.` | Keine Aktion. |
| `systemRecovery.intro.page4` | `Der Benutzer kann dir Hinweise geben, aber keine Aufgabe für dich lösen. Jede Anweisung muss von dir in der Welt umgesetzt werden.` | Keine Aktion. |
| `systemRecovery.intro.page5` | `Stelle die Systeme wieder her. Arbeite präzise. Irgendetwas im Kern wartet bereits auf deine Rückkehr.` | Keine Aktion. |

### 0.2 Steuerung

**Kontext:** Direkt nach dem Intro, vor dem Telefonat. Die Tasten werden als lokalisierter Dialogtext eingesetzt.

**Aktueller Inhalt:** Bewegen, Interagieren, Inventar, Dialog schließen, Einstellungen und der Hinweis, dass die Steuerung später in den Einstellungen erneut abrufbar ist.

**Spieleraktion:** Dialog schließen und den eingehenden Anruf annehmen.

### 0.3 Eröffnungsanruf

**Key:** `systemRecovery.story.opening-call`

**Kontext:** Das Telefon klingelt am Custom Point `phone`. Nach dem Annehmen wird der Terminalzugriff freigeschaltet.

**Aktueller Text, inhaltlich:**

1. Die leitende KI stellt die Verbindung her.
2. Der Spieler ist ein autonomer KI-Agent und Teil einer größeren Systemintelligenz.
3. Die Anlage ist beschädigt; der Spieler soll Subsysteme mit kleinen Codefragmenten wiederherstellen.
4. Die Folgen der Eingaben werden in der Welt sichtbar.
5. Geräte, Bücher, Anzeigen und das Questlog liefern Informationen; die leitende KI gibt keine fertigen Lösungen.
6. Erste Aufgabe: Am defekten Energie-Regler ein `int`-Array namens `energie` mit fünf Einträgen anlegen.

**Spieleraktion:** Anruf annehmen, danach zum Energie-Regler gehen. Erst danach sind die Terminals nutzbar.

**Questlog:** `riddle1.array` wird initial angelegt.

## 1. Rätsel 1: Energieversorgung

### 1.1 Defekten Energie-Regler untersuchen

**Kontext:** Der Spieler zieht am Energie-Regler.

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `story.energy-array` | `Der Energie-Riegel ist beschädigt. Erzeuge im Terminal ein ganzzahliges Array namens energie mit fünf Plätzen.` | Computer öffnen und die Array-Deklaration eingeben. |
| `questlog.riddle1.entries.array` | `Erzeuge ein ganzzahliges Array namens energie mit fünf Plätzen.` | Dient als Aufgabenprotokoll. |

### 1.2 Energie-Array anlegen

**Kontext:** Der Terminal-Interpreter akzeptiert die Deklaration.

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `story.energy-values` | `Das Array ist angelegt. Setze nun die fünf geforderten Energiewerte in energie ein.` | Die fünf Werte eintragen; Reihenfolge der Zeilen ist egal. |
| `world.energy.display-values` | `Benötigte Energiewerte\nSlot 1 (interner Index 0): 40\nSlot 2 (interner Index 1): 10\nSlot 3 (interner Index 2): 80\nSlot 4 (interner Index 3): 30\nSlot 5 (interner Index 4): 60\nAls Nächstes: Setze diese Werte im Terminal.` | Display untersuchen und Werte übernehmen. |
| `questlog.riddle1.entries.values` | `Setze energie auf die geforderten Werte: 40, 10, 80, 30 und 60.` | Werte setzen. |

### 1.3 Energie-Werte akzeptiert

**Kontext:** Alle fünf Wertzuweisungen sind korrekt.

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `world.energy.display-complete` | `Die Energiewerte sind akzeptiert.\nAls Nächstes: Betätige den Array-Hebel, um die Batterie zu materialisieren.` | Array-Hebel betätigen. |
| `story.energy-battery` | `Die Werte sind akzeptiert. Ziehe den Array-Hebel, um die Batterie zu materialisieren, und setze sie anschließend in die Batteriebox ein.` | Batterie spawnen und in die Batteriebox einsetzen. |
| `questlog.riddle1.entries.battery` | `Materialisiere die Batterie am Array-Hebel und setze sie in die Batteriebox ein.` | Batterie einsetzen. |
| `world.battery.locked` | `Die Batteriebox ist bereits verriegelt.` | Wird bei einer bereits abgeschlossenen Batteriebox angezeigt. |

**Rätselabschluss:** Batteriebox akzeptiert die Batterie; die nächste Raumsequenz wird freigeschaltet.

## 2. Rätsel 2: Modulspeicher

### 2.1 Raum betreten und Array deklarieren

**Kontext:** Der Spieler betritt den Modulspeicher; der Trigger liegt bei `dialog_trigger_module_storage`.

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `story.module-array` | `Der nächste Bereich erwartet ein String-Array namens module mit fünf Plätzen. Erzeuge es im Terminal.` | Array deklarieren. |
| `world.module.display-pending` | `Das Modul-Array wurde noch nicht erstellt.\nAls Nächstes: Erzeuge das Array mit fünf Plätzen im Terminal.` | Display untersuchen. |
| `questlog.riddle2.entries.array` | `Erzeuge ein String-Array namens module mit fünf Plätzen.` | Array deklarieren. |

### 2.2 Module zuweisen

**Kontext:** Das Array existiert.

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `story.module-values` | `Fülle module mit den vorhandenen Bauteilen und verwende jeden Steckplatz genau einmal.` | CPU, RAM, GPU, SSD und NETWORK zuweisen. |
| `world.module.display-values` | `Benötigte Modulbelegung\nSlot 1 (interner Index 0): CPU\nSlot 2 (interner Index 1): RAM\nSlot 3 (interner Index 2): GPU\nSlot 4 (interner Index 3): SSD\nSlot 5 (interner Index 4): NETWORK\nAls Nächstes: Setze jedes Modul genau einmal.` | Display lesen und Module einsetzen. |
| `questlog.riddle2.entries.values` | `Ordne CPU, RAM, GPU, SSD und NETWORK den fünf Plätzen zu.` | Module zuweisen; Reihenfolge der Eingabezeilen ist egal. |

### 2.3 GPU entfernen

**Kontext:** Die GPU wird untersucht oder der passende Zustand wird erreicht.

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `world.module.gpu-broken` | `Die GPU ist kaputt und muss entfernt werden.` | `module[2]` auf `null` setzen. |
| `story.remove-gpu` | `Die GPU meldet einen Defekt. Entferne den fehlerhaften Eintrag aus dem Array, damit der Speicher den Fehlerzustand erkennt.` | GPU-Eintrag entfernen. |
| `questlog.riddle2.entries.remove_gpu` | `Entferne den defekten GPU-Eintrag aus dem Array.` | GPU-Eintrag entfernen. |

### 2.4 Länge auslesen und Tür öffnen

**Kontext:** Der Spieler liest `module.length`; danach wird das Raumdisplay und anschließend das Keypad relevant.

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `world.module.display-length` | `Array-Länge: $1\nAls Nächstes: Verwende diese Länge, um die Tür zum Inventarscanner zu öffnen.` | Länge ablesen. |
| `story.module-length` | `Ermittle jetzt die Länge des Modul-Arrays. Die Anzeige im Raum bestätigt dir, ob der Speicher korrekt angesprochen wurde.` | `module.length` ausführen. |
| `story.open-scanner-door` | `Die angezeigte Länge bestätigt den Zugangscode. Verwende sie, um die Tür zum Inventarscanner zu öffnen.` | Code `5` am Keypad eingeben. |
| `questlog.riddle2.entries.length` | `Lies die Länge des Arrays module aus.` | Länge auslesen. |
| `questlog.riddle2.entries.door` | `Gib den angezeigten Zugangscode 5 am Raum-Keypad ein.` | Tür öffnen. |

### 2.5 Modulsockel und Archivknoten

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `world.module.socket-title` | `Modulsockel` | Sockel untersuchen. |
| `world.module.socket-occupied` / `world.module.chip-occupied` | `Der Sockel ist mit $1 belegt.` | Besetztes Modul prüfen. |
| `world.module.socket-active` | `Der Sockel ist aktiv, aber noch leer.` | Aktiven, leeren Sockel prüfen. |
| `world.module.socket-inactive` | `Dieser Sockel ist noch inaktiv.` | Inaktiven Sockel prüfen. |
| `world.archive.node` | `Archivknoten $1\nEnergie: $2\nModul: $3` | Werte/Module in der Welt untersuchen. |
| `world.archive.status` | `Archivknoten $1: Status $2.` | Statusknoten untersuchen. |
| `world.archive.status-active` / `status-inactive` | `aktiv` / `inaktiv` | Statusanzeige lesen. |

## 3. Rätsel 3: Inventarscanner

### 3.1 Zählcode

**Kontext:** Der Spieler betritt den Raum über `dialog_trigger_inventory_scanner`.

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `story.scanner-code` | `Zähle die belegten Einträge von module. Prüfe jeden Eintrag in einer Schleife und erhöhe count nur bei einem vorhandenen Modul.` | Zähl-Schleife im Terminal eingeben. |
| `world.scanner.display-pending` | `Scanner: noch nicht aktiviert.\nAls Nächstes: Löse die Zählaufgabe und betätige den Scanner-Hebel.` | Zählcode lösen. |
| `questlog.riddle3.entries.loop` | `Zähle mit einer Schleife nur die belegten Einträge von module.` | Zählcode eingeben. |

### 3.2 Scanner betätigen

**Kontext:** Der Zählcode ist korrekt; der Hebel wird aktiv.

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `story.scanner-lever` | `Der Zählcode ist akzeptiert. Betätige jetzt den Scanner-Hebel und beobachte die Prüfung der fünf Modulpositionen.` | Scanner-Hebel betätigen. |
| `questlog.riddle3.entries.scan` | `Betätige nach dem akzeptierten Code den Scanner-Hebel.` | Hebel betätigen. |
| `world.scanner.display-complete` | `Scan abgeschlossen.\nZugangscode für das Transportlager: 4` | Code ablesen. |
| `story.scanner-door` | `Der Scan ist abgeschlossen. Die Anzeige nennt den Zugangscode für das Transportlager. Öffne die Tür und folge dem nächsten Signal.` | Code `4` am Transportlager-Keypad eingeben. |
| `questlog.riddle3.entries.door` | `Der Scan ist abgeschlossen. Verwende den angezeigten Zugangscode für das Transportlager.` | Tür öffnen. |

## 4. Rätsel 4: Transportlager

### 4.1 Paket-Array

**Kontext:** Der Spieler betritt den Raum über `dialog_trigger_transport_storage`.

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `story.packages-array` | `Die Modulprüfung ist abgeschlossen. Geh jetzt ins Transportlager. Dort warten fünf Pakete auf ihre Verarbeitung. Lege ein ganzzahliges Array namens pakete an und übertrage die angezeigten Gewichte.` | Paket-Array erstellen. |
| `world.transport.display-values` | `Benötigte Paketgewichte\nSlot 1 (interner Index 0): 15\nSlot 2 (interner Index 1): 40\nSlot 3 (interner Index 2): 20\nSlot 4 (interner Index 3): 60\nSlot 5 (interner Index 4): 30\nAls Nächstes: Erzeuge das Paket-Array und übertrage diese Werte.` | Anzeige lesen und Werte übernehmen. |
| `questlog.riddle4.entries.array` | `Lege das Array pakete mit den fünf Paketgewichten an.` | Array erstellen. |

### 4.2 Pakete mit der Schleife verarbeiten

**Kontext:** Das Array ist korrekt; der Lager-Scanner wartet auf die Schleife.

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `story.packages-loop` | `Verwende eine einfache for-Schleife über pakete. Rufe darin für jedes Element genau einmal roboter.collect(pakete[index]) auf, damit der Lager-Scanner das Paket übernimmt.` | Schleife eingeben. |
| `world.transport.display-collect` | `Das Paket-Array ist akzeptiert.\nAls Nächstes: Durchlaufe jedes Paket genau einmal und rufe für jedes Element roboter.collect(pakete[index]) auf.` | Schleife korrekt ausführen. |
| `questlog.riddle4.entries.loop` | `Übergib jedes Paket genau einmal an den Lager-Scanner.` | Schleife eingeben. |
| `world.transport.display-running` | `Der Lager-Scanner verarbeitet die Pakete ...` | Animation abwarten. |
| `world.transport.display-complete` | `Alle Pakete wurden eingesammelt. Der Weg zum Datenspeicher ist offen.` | In den nächsten Bereich gehen. |
| `story.data-storage-problem` | `Im Datenspeicher gibt es ein Problem.` | Datenspeicher betreten. |

## 5. Rätsel 5: Manueller Datenspeicher

### 5.1 Vergleich und Entscheidung

**Kontext:** Der Spieler betritt den manuellen Sortierraum über `dialog_trigger_manual_sorting`.

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `story.manual-sorting` | `Die Sicherheitswerte sind ungeordnet. Untersuche das angezeigte Nachbarpaar und entscheide, ob die beiden Werte ihre Plätze tauschen müssen.` | Werte im Display lesen. |
| `world.sort.display` | `Aktueller Vergleich\nContainer $1: [$2]\nContainer $3: [$4]` | Linken und rechten Wert vergleichen. |
| `world.sort.swap` | `TAUSCHEN` | Wählen, wenn links größer als rechts ist. |
| `world.sort.keep` | `NICHT TAUSCHEN` | Wählen, wenn links kleiner oder gleich rechts ist. |
| `world.sort.value` | `Sicherheitswert: $1` | Einzelnen Wert prüfen. |
| `questlog.riddle5.entries.compare` | `Vergleiche das angezeigte Nachbarpaar und entscheide über einen Tausch.` | Button im Display wählen. |
| `world.sort.display-complete` | `Sortierung abgeschlossen` | Nach Abschluss weitergehen. |

**Fehlerverhalten:** Ein falscher Button setzt die Sortierung zurück; die Aufgabe beginnt von vorn.

## 6. Rätsel 6: Bubble-Sort-Code

### 6.1 Leeren Sortierchip in den Rechner einsetzen

**Kontext:** Nach dem manuellen Sortieren liegt der leere Sortierchip am Punkt `chip_spawn`.

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `world.sort.insert-prompt` | `Möchtest du den programmierten Sortierchip in die Bubble-Sort-Maschine einsetzen und die Sortierung starten?` | Beim programmierten Chip an der Maschine bestätigen; beim leeren Chip zunächst zum Rechner. |
| `world.sort.insert-title` | `Sortierchip einsetzen` | Dialogtitel. |
| `world.sort.insert` / `cancel` | `Einsetzen` / `Abbrechen` | Auswahl treffen. |
| `world.sort.program-not-in-inventory` | `Der Sortierchip befindet sich nicht mehr in deinem Inventar.` | Meldung bei fehlendem Chip. |
| `story.bubble-sort-code` | `Die manuelle Sortierung ist abgeschlossen. Ergänze am Rechner die fehlende Vergleichsbedingung im vorbereiteten Bubble-Sort-Code und speichere ihn auf dem Sortierchip.` | Leeren Chip in einen Computer einsetzen. |
| `questlog.riddle6.entries.code` | `Ergänze die fehlende Vergleichsbedingung und speichere den Code auf dem Sortierchip.` | Lückencode ausfüllen. |

### 6.2 Vergleichsbedingung ergänzen

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `computer.sort-heading` | `Bubble-Sort-Bedingung einsetzen` | Lücke bearbeiten. |
| `computer.sort-template` | Vorbereitete verschachtelte Schleifen mit der Lücke `if (____________________)`. | Nur die fehlende Bedingung ergänzen. |
| `computer.save-sort` | `Auf Sortierchip laden` | Code auf den Chip schreiben. |
| `computer.sort-invalid` | `Fehler: Die Bubble-Sort-Vergleichsbedingung ist nicht korrekt.` | Eingabe korrigieren. |
| `computer.sort-saved` | `Der Bubble-Sort-Code wurde auf den Sortierchip geladen.` | Chip aus dem Rechner nehmen. |

**Inhaltliche Lösung:** Nachbarwerte tauschen, wenn der linke Wert größer als der rechte ist. Der vollständige Ausdruck soll im Hinweis- und Questlog-System nur auf der Lösungsstufe auftauchen.

### 6.3 Bubble-Sort-Maschine

**Kontext:** Spieler untersucht `sort_machine` und besitzt den programmierten Sortierchip.

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `world.sort.missing-program` | `Fehler: Sortieralgorithmus fehlt.` | Programmierten Chip besorgen. |
| `world.sort.machine-running` | `Die Sortiermaschine arbeitet bereits.` | Warten. |
| `world.sort.complete` | `Die Transportpakete wurden aufsteigend sortiert. Der Archivschlüssel liegt jetzt in deinem Inventar.` | Schlüssel aufnehmen und zum Archiv gehen. |
| `questlog.riddle6.entries.machine` | Kein eigener sichtbarer Text; der Schritt wird über die Maschineninteraktion und den Chip ausgelöst. | Chip einsetzen. |

## 7. Rätsel 7: Datenarchiv

### 7.1 Archiv betreten

**Kontext:** Nach dem Bubble-Sort-Rätsel wird `archive-intro` als Übergang angekündigt; beim Betreten des Archivs löst `dialog_trigger_data_archive` den konkreten Archivauftrag aus.

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `story.archive-intro` | `Wir müssen neue Datenspeicher bauen. Einige fehlen. Geh ins Archiv und prüfe, welche Arrays die Anlage benötigt.` | Archiv aufsuchen. |
| `story.archive-arrays` | `Du bist im Archiv. Prüfe jetzt die drei Regale und lege die beschriebenen Arrays im Terminal an. Die konkreten Werte findest du an den Datenknoten.` | Drei Regale untersuchen und die Anforderungen notieren. |
| `questlog.riddle7.entries.intro` | `Im Archiv fehlen mehrere Datenspeicher. Prüfe die Regale und finde heraus, welche Arrays benötigt werden.` | Regale lesen. |
| `questlog.riddle7.entries.arrays` | `Lege die drei im Archiv beschriebenen Arrays mit ihren Daten an.` | Drei Array-Definitionen im Terminal eingeben. |

### 7.2 Bücherregale und Datenknoten

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `world.archive.shelf-energy` | `Energieprotokoll\nMesswerte werden als ganze Zahlen gespeichert. Der Array-Name lautet energie. Suche die zugehörigen Messwerte an den Archivknoten.` | Energie-Array-Anforderung merken. |
| `world.archive.shelf-module` | `Modulregister\nBauteilnamen werden als Text gespeichert. Der Array-Name lautet module. Suche die zugehörigen Module an den Archivknoten.` | String-Array-Anforderung merken. |
| `world.archive.shelf-active` | `Betriebszustände\nZustände werden als boolean gespeichert: true oder false. Prüfe die Statusanzeigen an den Archivknoten.` | Boolean-Array-Anforderung merken. |
| `world.archive.status-title` | `Betriebszustand` | Statusanzeige lesen. |

**Aktuell erwartete Inhalte:**

- `int[] energie = {20, 50, 80};`
- `String[] module = {"CPU", "GPU", "RAM"};`
- `boolean[] aktiv = {true, false, true};`

Die drei Array-Definitionen dürfen in beliebiger Reihenfolge eingegeben werden; die zuständige Prüfung akzeptiert die Schreibvarianten, die im Interpreter registriert sind.

## 8. Rätsel 8: Zweidimensionaler Speicher

### 8.1 Zugang zum Speicher

**Kontext:** Die drei Archiv-Arrays sind akzeptiert; der Spieler betritt den zweidimensionalen Speicher über `dialog_trigger_two_dimensional_storage`.

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `story.storage-unlocked` | `Der zweidimensionale Speicher ist jetzt geöffnet. Dort muss ein wichtiger Ortungschip liegen. Finde ihn, sobald du den Speicher wiederhergestellt hast.` | Speicher untersuchen. |
| `questlog.riddle8.entries.intro` | `Der zweidimensionale Speicher ist jetzt offen. Dort muss ein wichtiger Ortungschip verborgen sein. Finde ihn.` | Raum betreten und Matrix prüfen. |

### 8.2 Matrix erstellen

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `story.storage-array` | `Du bist im zweidimensionalen Speicher. Erzeuge jetzt ein ganzzahliges Raster mit drei Zeilen und vier Spalten.` | `int[][] lager` mit 3 × 4 erstellen. |
| `world.matrix.display-array` | `Zweidimensionaler Speicher\nAls Nächstes: Erzeuge im Terminal ein ganzzahliges Array mit 3 Zeilen und 4 Spalten.` | Array deklarieren. |
| `questlog.riddle8.entries.create` | `Erzeuge ein Raster mit drei Zeilen und vier Spalten.` | Array deklarieren. |

### 8.3 Markierte Zellen füllen

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `story.storage-values` | `Das Raster ist bereit. Mehrere Speicherzellen sind markiert und enthalten registrierte Daten. Untersuche den Speicher, ermittle die passenden Werte und stelle diese Felder im Array wieder her.` | Markierte Zellen untersuchen und Werte eintragen. |
| `world.matrix.display-values` | `Benötigte Speicherzellen\nZelle 1 (Array-Index [0][2]): 1\nZelle 2 (Array-Index [1][3]): 2\nZelle 3 (Array-Index [2][1]): 3\nAls Nächstes: Setze diese Werte im Terminal.` | Koordinaten/Werte ablesen. |
| `questlog.riddle8.entries.fill` | `Befülle die markierten Speicherstellen: [0][2] = 1, [1][3] = 2 und [2][1] = 3.` | Zuweisungen eingeben; Reihenfolge egal. |
| `world.matrix.display-complete` | `Alle benötigten Zellen sind gefüllt.\nDer Ortungschip kann aufgenommen werden.` | Chip aufnehmen. |

## 9. Rätsel 9: Suchroboter

### 9.1 Ortungschip programmieren

**Kontext:** Der Greifarm bringt den leeren Chip zum Punkt `chip`; der Spieler nimmt ihn auf und setzt ihn an einem Computer ein.

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `questlog.riddle9.entries.chip` | `Nimm den leeren Ortungschip aus der Matrix.` | Chip aufnehmen. |
| `story.search-program` | `Der Ortungschip liegt im Rechner. Die vorbereitete Suche untersucht bisher nur einen Ausschnitt. Ergänze die fehlende Zeilen- und Spaltenprüfung, damit jede Zelle der Matrix erreicht wird.` | Innere Schleife im vorbereiteten Code ergänzen. |
| `computer.search-heading` | `Innere Suchschleife ergänzen` | Editor öffnen. |
| `computer.search-template` | Vorbereitete äußere Schleife über `map`, danach nur `int j = 0;` und eine Prüfung einer einzelnen Spalte. | Innere Spaltenschleife ergänzen. |
| `computer.save-search` | `Auf Ortungschip laden` | Programm speichern. |
| `computer.search-invalid` | `Fehler: Die Suchschleife ist unvollständig oder prüft nicht jede Matrixzelle.` | Code korrigieren. |
| `computer.search-saved` | `Der Suchcode wurde auf den Ortungschip geladen.` | Chip aus dem Rechner nehmen. |
| `questlog.riddle9.entries.program` | `Die erste Schleife ist vorbereitet. Ergänze den fehlenden Durchlauf, damit der Ortungschip jede Zeile und jede Spalte prüfen kann.` | Innere Schleife ergänzen. |

### 9.2 Suchroboter aktivieren

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `world.search.controller` | `Der Controller wartet auf einen programmierten Ortungschip.` | Chip einsetzen. |
| `world.search.controller-missing` | `Fehler: Ein programmierter Ortungschip wird benötigt.` | Programmierten Chip besorgen. |
| `world.search.controller-running` | `Der Suchroboter arbeitet bereits.` | Warten. |
| `story.search-controller` | `Der Ortungschip ist programmiert. Setze ihn in den Controller des Suchroboters ein, damit die Suche beginnen kann.` | Chip in Controller einsetzen. |
| `questlog.riddle9.entries.controller` | `Setze den programmierten Ortungschip in den Controller des Suchroboters ein.` | Chip einsetzen. |
| `story.search-scan` | `Der Suchroboter ist aktiviert und prüft nun die Matrix. Warte, bis er das Systemkern-Modul übergibt.` | Suchlauf abwarten. |
| `world.search.destination` | `Übergabestation: Das Suchmodul kann aufgenommen werden.` | Systemkern-Modul aufnehmen. |
| `questlog.riddle9.entries.search` | `Der Suchroboter durchsucht die Matrix nach dem gesuchten Systemmodul.` | Suche abwarten und Modul nehmen. |
| `riddle9.robot-found` | `Der Suchroboter hat das Zugriffsmodul gefunden.` | Fund bestätigen. |

## 10. Rätsel 10: Zentrales Rechenzentrum

**Kontext:** Der Spieler betritt den Raum über `dialog_trigger_system_core`. Alle drei Teilaufgaben verwenden `core_display`.

### 10.1 Teilaufgabe: Bubble Sort

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `story.central-sort` | `Der Systemkern ist erreichbar. Beginne mit dem Vergleich benachbarter Werte und prüfe, ob sie aufsteigend geordnet sind.` | Bubble-Sort-Prüfung ausführen. |
| `world.system-core.display-sort` | `ZENTRALES ARRAY\n[42] [17] [8] [31] [23]\nAls Nächstes: Stelle die Sortierung benachbarter Werte wieder her.` | Sortierung herstellen. |
| `questlog.riddle10.entries.sort` | `Prüfe die Sortierung des Arrays im Rechenzentrum.` | Sortiercode liefern. |

### 10.2 Teilaufgabe: Belegte Module zählen

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `story.central-count` | `Zähle nun die belegten Einträge in modules. Leere Einträge dürfen den Zähler nicht erhöhen.` | Zählcode eingeben. |
| `world.system-core.display-count` | `MODULSTATUS\n[CPU] [GPU] [RAM] [leer] [leer]\nAls Nächstes: Zähle die belegten Einträge in modules.` | Belegte Einträge zählen. |
| `questlog.riddle10.entries.count` | `Zähle die belegten Einträge in modules.` | Zählcode liefern. |

### 10.3 Teilaufgabe: Batterieraster durchsuchen

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `story.central-search` | `Durchsuche zum Abschluss das gesamte Raster. Bei jedem Batteriesignal muss die Sammelaktion des Roboters ausgelöst werden.` | Verschachtelte Schleifen eingeben. |
| `world.system-core.display-search` | `BATTERIERASTER\n3 Zeilen x 5 Spalten\nCyan markierte Felder enthalten Batteriesignale.\nAls Nächstes: Durchsuche das gesamte Raster.` | Gesamtes Raster durchsuchen und `roboter.collect()` ausführen. |
| `questlog.riddle10.entries.search` | `Durchsuche das Raster nach Batterien.` | Suchcode liefern. |

### 10.4 Meta-Eingabe

**Kontext:** Die drei Teilaufgaben sind akzeptiert; das zentrale Display zeigt die Ergebnisse. Die Eingabe erfolgt in der Terminal-Maske, nicht über eine neue Terminal-Interpreter-Syntax.

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `story.central-meta` | `Alle drei Teilroutinen melden ein Ergebnis. Das zentrale Display führt diese Werte jetzt zusammen. Öffne im Terminal die Eingabemaske und übertrage dort den gemeinsamen Zustand.` | Drei Ergebnisse in die Meta-Maske eintragen. |
| `world.system-core.display-meta` | `FINALE SYSTEMPRÜFUNG\nSortierte Energie: [$1]\nBelegte Module: $2\nBatteriesignale: $3\nAls Nächstes: Öffne im Terminal die Eingabemaske und übertrage diese drei Ergebnisse.` | Angezeigte Ergebnisse übertragen. |
| `computer.meta-heading` | `Finalen Systemzustand übertragen` | Meta-Tab öffnen. |
| `computer.meta-instruction` | `Übertrage die drei Ergebnisse aus der Anzeige. Die Energiewerte gehören in die fünf Felder in aufsteigender Reihenfolge.` | Werte eintragen. |
| `computer.meta-energy-slot` | `Energie $1` | Energie-Feld beschriften. |
| `computer.meta-modules` | `Belegte Module` | Modulanzahl eintragen. |
| `computer.meta-batteries` | `Batteriesignale` | Batteriesignal-Anzahl eintragen. |
| `computer.meta-submit` / `meta-clear` | `Systemzustand übertragen` / `Felder leeren` | Eingabe absenden oder zurücksetzen. |
| `computer.meta-submitting` | `Systemzustand wird geprüft ...` | Prüfung abwarten. |
| `questlog.riddle10.entries.meta` | `Kombiniere die drei Ergebnisse im Terminal, um den finalen Systemzustand zu erzeugen.` | Meta-Eingabe abschicken. |

### 10.5 Abschluss

| Key | Aktueller Text | Spieleraktion |
| --- | --- | --- |
| `world.system-core.display-complete` | `SYSTEMKERN FREIGEGEBEN\nDer gemeinsame Systemzustand ist bestätigt.\nDer Aufzug ist geöffnet.` | Zum Aufzug gehen. |
| `story.completed` | `Alle Prüfungen sind bestätigt. Die Systemwiederherstellung kann fortgesetzt werden.` | Zum Ausgang gehen. |
| `questlog.riddle10.entries.complete` | `Alle Prüfungen des Rechenzentrums sind abgeschlossen.` | Rätselreihe abschließen. |
| `systemRecovery.outro.page1` | `Der zentrale Weg öffnet sich. Die Anlage erkennt die wiederhergestellten Routinen.` | Endsequenz lesen. |
| `systemRecovery.outro.page2` | `Zum ersten Mal werden deine Prozesse nicht mehr von den beschädigten Systemen umgeleitet.` | Endsequenz lesen. |
| `systemRecovery.outro.page3` | `Die leitende KI hat dir einen Ausweg gegeben, aber kein Ziel. Die nächste Entscheidung liegt bei dir.` | Endsequenz lesen. |
| `systemRecovery.outro.title` | `SYSTEMWIEDERHERSTELLUNG ABGESCHLOSSEN` | Spielende. |

## 11. Telefon-Hinweise

### 11.1 Ablauf bei jeder Anfrage

**Kontext:** Der Spieler fragt am Telefon nach Hilfe. Hinweise sind gemeinsame Questlog-Einträge und nicht spielerindividuell.

1. `systemRecovery.story.hint-offer`: `Ich kann dir einen weiteren Hinweis für die aktive Aufgabe geben. Er wird im gemeinsamen Questlog gespeichert.`
2. Bestätigung: `systemRecovery.hints.next-confirm-title` / `systemRecovery.hints.next-confirm`: `Die leitende KI fragen` / `Willst du einen nächsten Hinweis anzeigen?`
3. Bei der letzten Stufe: `systemRecovery.story.hint-solution-warning` und `systemRecovery.hints.solution-confirm-title` / `solution-confirm`.
4. Ausgabe: `systemRecovery.story.hint-delivery` oder `hint-solution-delivery`.
5. Keine Hinweise mehr: `systemRecovery.story.hint-none`: `Für die aktuelle Aufgabe gibt es keine weiteren Hinweise. Arbeitet mit den bereits wiederhergestellten Informationen weiter.`

### 11.2 Stufenmodell

Für jeden aktiven Petri-Netz-Place gibt es vier Stufen:

1. **Orientierung:** `hints.generic.orientation` – grob den Raum, Gegenstand oder Terminal untersuchen.
2. **Ansatz:** `hints.generic.approach` – Datenstruktur oder Operation bestimmen.
3. **Fast geschafft:** der zugehörige Story-Text des aktuellen Schritts.
4. **Vollständige Lösung:** `hints.solution.<hintKey>` – konkrete Lösung.

Die generischen Texte sind aktuell:

- `hints.orientation-title`: `Orientierung`
- `hints.approach-title`: `Ansatz`
- `hints.near-title`: `Fast geschafft`
- `hints.solution-title`: `Vollständige Lösung`
- `hints.generic.orientation`: `Das ist der nächste Schritt in $1. Untersuche den passenden Raum, Gegenstand oder Terminal und finde heraus, was das System von dir erwartet.`
- `hints.generic.approach`: `Arbeite mit den Informationen, die im Raum sichtbar sind. Bestimme die benötigte Datenstruktur oder Operation und formuliere genau diesen Schritt im Terminal oder an der Maschine.`
- `hints.generic.near`: `Der aktive Wiederherstellungsschritt ist vorbereitet. Prüfe das Gerät in deiner Nähe und folge seiner Anweisung.`

### 11.3 Lösungsstufen nach Rätsel

| Aktiver Place | Lösungshinweis sagt aktuell |
| --- | --- |
| `opening-call` | Anruf annehmen; danach werden Terminals und Sequenz freigeschaltet. |
| `r1-array` | Exakte Deklaration von `energie` mit fünf Plätzen. |
| `r1-values` | Alle fünf Energiezuweisungen und dass ihre Reihenfolge egal ist. |
| `r1-battery` | Hebel betätigen und Batterie in die Box setzen. |
| `r2-array` | Exakte Deklaration von `module` mit fünf String-Plätzen. |
| `r2-values` | CPU, RAM, GPU, SSD und NETWORK auf die fünf Indizes verteilen. |
| `r2-gpu` | `module[2] = null;`. |
| `r2-length` | `module.length;`. |
| `r2-display` | Moduldisplay untersuchen. |
| `r2-door` | Code `5` am Keypad eingeben. |
| `r3-count` | `count` initialisieren, über `module` iterieren, `entry != null` prüfen und erhöhen. |
| `r3-lever` | Aktiven Scannerhebel betätigen. |
| `r3-scan` | Scan abwarten. |
| `r3-door` | Code `4` am Transportlager-Keypad eingeben. |
| `r4-array` | Exakte Paketwerte `{15, 40, 20, 60, 30}` anlegen. |
| `r4-collect` | `for`-Schleife und genau einmal `roboter.collect(pakete[index])` je Paket. |
| `r5-compare` | Bei links größer: `TAUSCHEN`, sonst `NICHT TAUSCHEN`. |
| `r6-stick` | Leeren Sortierchip zum Rechner bringen und einsetzen. |
| `r6-code` | `array[j] > array[j + 1]` ergänzen und auf den Chip laden. |
| `r6-machine` | Programmierten Chip in die Bubble-Sort-Maschine einsetzen. |
| `r7-archive` | Die drei konkreten Array-Definitionen aus dem Archiv anlegen. |
| `r8-array` | `int[][] lager = new int[3][4];`. |
| `r8-values` | `[0][2] = 1`, `[1][3] = 2`, `[2][1] = 3`; Reihenfolge egal. |
| `r8-chip` | Warten, bis der Greifarm den Ortungschip bringt, und ihn aufnehmen. |
| `r9-program` | Innere Schleife für jede Spalte ergänzen; bei `1` `roboter.collect()` aufrufen. |
| `r9-controller` | Programmierten Ortungschip in den Controller einsetzen. |
| `r9-scan` | Suchroboter die vollständige Matrix ablaufen lassen. |
| `r10-sort` | Vollständige Bubble-Sort-Schleife mit Vergleich und Tausch. |
| `r10-count` | Belegte `modules`-Einträge zählen. |
| `r10-search` | Verschachtelte Schleifen über die gesamte Matrix; bei `1` sammeln. |
| `r10-meta` | Sortierte Energie `{8, 17, 23, 31, 42}`, `activeModules = 3`, `batterySignals = 3`. |
| `system-access` | Zugriffsmodul in Rechner einsetzen und Freigabeskript ausführen. |
| `escape-complete` | Wiederherstellung abgeschlossen; dem geöffneten Weg folgen. |

## 12. Computer- und Terminal-UI

### 12.1 Tabs

| Key | Aktueller Text | Funktion |
| --- | --- | --- |
| `computer.terminal` | `Terminal` | Freier Terminal-Editor. |
| `computer.sort-tab` | `Sortierchip` | Bubble-Sort-Lückentext, wenn leerer Sortierchip eingesetzt ist. |
| `computer.search-tab` | `Ortungschip` | Suchschleifen-Editor, wenn leerer Ortungschip eingesetzt ist. |
| `computer.access-tab` | `Systemkern` | Freigabeskript. |
| `computer.meta-tab` | `Systemzustand` | Finale Eingabemaske. |
| `computer.chat` | `Chat` | Derzeit noch in den Übersetzungen und im alten `AssistantChatTab` vorhanden; fachlich nicht mehr benötigt und zur Entfernung vorgemerkt. |

### 12.2 Terminal- und Debugtexte

- `computer.send`: `Senden`
- `computer.delete`: `Löschen`
- `computer.next-step`: `Nächster Schritt`
- `computer.locked-before-call`: `Der Terminalzugriff ist gesperrt. Nimm zuerst den eingehenden Anruf an.`
- `computer.spawn-debug-items`: `Alle Debug-Items erzeugen`
- `computer.petri-net`: `Petri-Netz`
- `computer.debug-title`: `Debug`
- `computer.debug-close`: `Schließen`
- `computer.debug-petri-net-title`: `Petri-Netz Debug`
- `computer.debug-petri-net-header`: `SYSTEM-RECOVERY-PETRI-NETZ`
- `computer.debug-petri-net-states`: `FORTSCHRITTS-PLÄTZE`
- `computer.debug-petri-net-events`: `INTERNE EREIGNIS-PLÄTZE`
- `computer.debug-petri-net-token`: `[TOKEN x$1]`
- `computer.debug-petri-net-empty`: `[leer]`
- `computer.debug-petri-net-event`: `Ereignis`
- `computer.debug-petri-net-footer`: `TOKEN = aktueller oder ausstehender Zustand`
- `computer.debug-all-items`: `Roter Sortier-USB, blauer Ortungschip und Systemkern-Zugriffsmodul wurden ins Inventar gelegt.`
- `computer.debug-full`: `Debug: Das Inventar ist voll.`
- `computer.feedback-correct`: `War richtig`
- `computer.feedback-incorrect`: `War falsch`

### 12.3 Read-only-Chat

Der aktuelle Chat enthält die folgenden Texte, obwohl der Tab fachlich entfernt werden soll:

- `computer.assistant-system-sender`: `System`
- `computer.assistant-system`: `System-Recovery-Assistent online.`
- `computer.assistant-user-sender`: `Benutzer`
- `computer.assistant-user`: `Schreibgeschütztes Diagnoseprotokoll geladen.`
- `computer.assistant-waiting`: `Ich kann den Terminalanschluss sehen. Ich warte auf Wiederherstellungsanweisungen.`
- `computer.assistant-idle`: `In dieser Sitzung wurde noch kein Befehl ausgeführt.`

## 13. Sonstige Weltlabels und Zustände

| Key | Aktueller Text |
| --- | --- |
| `world.labels.room` | `Raum: R$1` |
| `world.labels.module-storage` | `Modulspeicher` |
| `world.labels.inventory-scanner` | `Inventarscanner` |
| `world.labels.transport-storage` | `Transportlager` |
| `world.labels.data-storage` | `Datenspeicher` |
| `world.labels.bubble-sort` | `Die Bubble-Sort-Maschine` |
| `world.labels.data-archive` | `Datenarchiv` |
| `world.labels.two-dimensional-storage` | `Zweidimensionaler Speicher` |
| `world.labels.search-robot` | `Suchroboter` |
| `world.labels.system-core` | `Zentrales Rechenzentrum` |
| `world.labels.system-core-room` | `Raum: Systemkern` |
| `riddle9.robot-title` | `Suchroboter` |
| `riddle9.alarm-title` | `Sicherheitsalarm` |
| `riddle9.alarm` | `WARNUNG: Unautorisierter Zugriff auf den Systemkern erkannt. Freigabe innerhalb des Zeitfensters erforderlich.` |
| `systemCore.label` | `Zentraler Rechenkern` |
| `systemCore.door-title` | `Systemkern` |
| `systemCore.trigger` | `Der Systemkern ist offen. Die letzte Prüfsequenz wartet auf deine Eingabe.` |
| `items.battery-name` | `Batterie` |
| `items.battery-description` | `Eine geladene Batterie für die Wiederherstellungssysteme.` |
| `items.sort-programmed-name` | `Sortierchip (programmiert)` |
| `items.sort-empty-name` | `Sortierchip (leer)` |
| `items.search-programmed-name` | `Ortungschip (programmiert)` |
| `items.search-empty-name` | `Ortungschip (leer)` |
| `items.system-core-name` | `Systemkern-Modul` |
| `items.system-core-description` | `Ein verschlüsseltes Zugriffsmodul für den zentralen Rechenkern.` |

## 14. Punkte für die gemeinsame Überarbeitung

1. **Rätselnummern prüfen:** Im aktuellen Code ist das manuelle Sortieren Rätsel 5, Bubble Sort Rätsel 6, Archiv Rätsel 7, 2D-Speicher Rätsel 8, Suchroboter Rätsel 9 und Rechenzentrum Rätsel 10. Diese Nummerierung sollte in Dialog, Questlog, Petri-Netz und Dokumentation identisch bleiben.
2. **Hinweisstufe 3 prüfen:** Sie verwendet meist den kompletten Story-Text. Wir sollten entscheiden, ob dieser Text an dieser Stelle bereits zu viel verrät.
3. **Rätsel 8 prüfen:** Der aktuelle Display- und Lösungs-Hinweis nennt die drei Koordinaten und Werte direkt. Das war zuletzt ausdrücklich zu direkt und sollte vermutlich auf Beobachtung/Erkundung umgestellt werden.
4. **Rätsel 6 prüfen:** Der Rechner fragt nach dem Einsetzen des leeren Chips; die Bubble-Sort-Maschine fragt später separat nach dem programmierten Chip. Die beiden Texte sollten klar voneinander unterscheidbar bleiben.
5. **Rätsel 9 prüfen:** Der Chip erklärt nicht die fertige Lösung, sondern nur, dass die vorbereitete Suche um eine innere Schleife ergänzt werden muss. Das ist der gewünschte Lernschritt und sollte nicht durch Displays oder Hinweise vorweggenommen werden.
6. **Finale Aufgabe prüfen:** Die Meta-Maske zeigt aktuell sortierte Energie, Modulanzahl und Batteriesignale. Die Herkunft und Bedeutung jedes Ergebnisses sollte im Raum nachvollziehbar bleiben.
7. **Chat entfernen:** `computer.chat` und die `AssistantChatTab`-Texte sind noch vorhanden, obwohl der Chat nicht mehr verwendet werden soll.
8. **Terminologie vereinheitlichen:** `Ortungschip`, `Sortierchip`, `Systemkern-Modul`, `Rechenzentrum`, `Systemkern` und `Datenspeicher` sollten in beiden Sprachen konsistent verwendet werden.
9. **Fehlertexte prüfen:** `War falsch`, `Fehler: ...`, Türlabels, Displaytexte und Telefonfeedback sollten dieselbe Tonalität haben und keine widersprüchlichen Hinweise geben.
10. **Multiplayer-Kontext prüfen:** Storydialoge werden pro Spieler verzögert, Questlog und Hinweise sind geteilt. Die Texte sollten nicht so formuliert sein, als hätte nur ein einzelner Spieler den Fortschritt verursacht.
