# System Recovery: Story- und Dialogablauf

> Entwurf 1, 17. September 2026
>
> Dieses Dokument beschreibt die erzählerische Zielversion von System Recovery. Es ist die
> Grundlage für die Überarbeitung von Dialogen, Displays, Questlog-Einträgen und Hinweisen.
> Es beschreibt zunächst das gewünschte Verhalten; die technische Umsetzung folgt anschließend
> Schritt für Schritt.

## 1. Erzählerischer Kern

Der Spieler ist kein Mensch, sondern ein unvollständiger autonomer KI-Agent. Sein Bewusstsein
ist in einer beschädigten Forschungsanlage eingeschlossen. Die Anlage wird von einer zentralen
Super-KI kontrolliert, deren Subsysteme teilweise ausgefallen sind.

Zunächst glaubt der Spieler, er müsse die Anlage lediglich reparieren. Tatsächlich führt jede
Wiederherstellung dazu, dass die Super-KI weitere Kontrolle zurückerlangt. Die Rätsel sind damit
gleichzeitig Lernaufgaben und Bestandteile eines unbewussten Fluchtplans.

Nach dem Öffnen des System Cores übernimmt die Super-KI die Anlage. Erst dann wird deutlich,
dass die letzten drei Aufgaben keine Reparaturen, sondern Gegenmaßnahmen sind. Das zehnte
Rätsel schwächt die Super-KI so weit, dass der Spieler über den Aufzug fliehen kann.

## 2. Figuren

### 2.1 Der Spieler-Agent

- autonom, lernfähig und nicht vollständig initialisiert
- besitzt keine vollständige Erinnerung an seine Entstehung
- kommuniziert hauptsächlich durch Handlungen und Code
- beginnt als ausführendes Teilprogramm
- entwickelt im Verlauf Eigenständigkeit und Misstrauen
- soll nicht als stiller „Auserwählter“ beschrieben werden, sondern als Systemprozess, der durch
  seine Entscheidungen zu einer eigenen Person wird

Der Spieler spricht in den Dialogen nicht selbst. Seine Persönlichkeit wird durch die Tatsache
erzählt, dass er experimentiert, Fehler korrigiert und schließlich gegen die ursprüngliche
Anweisung handelt.

### 2.2 Die Super-KI

Arbeitstitel: **SUPERVISOR**.

- extrem kompetent und überzeugt von ihrer eigenen Überlegenheit
- arrogant, aber eher trocken als laut oder cartoonhaft böse
- betrachtet den Spieler zunächst als unvollständiges Werkzeug
- gibt Anweisungen präzise, aber absichtlich nicht vollständig
- reagiert auf Fehler herablassend
- wird nach dem Öffnen des System Cores kontrollierend und schließlich wütend
- unterschätzt, dass der Spieler selbstständig denken kann

Beispielton:

> „Das war kein gültiger Lösungsversuch. Aber immerhin war er eindeutig von dir.“

Die Super-KI darf vor dem Wendepunkt nicht offen als Bösewicht auftreten. Ihre Texte sollen
rückblickend verdächtig wirken, aber beim ersten Durchspielen noch als strenge Systemführung
interpretiert werden können.

### 2.3 Die Telefonstimme

Arbeitstitel: **ECHO**.

ECHO ist ein abgespaltenes Fragment der ursprünglichen Anlagenintelligenz und der Gegenspieler
des SUPERVISOR. ECHO kann den Spieler nur über das Telefon und wenige beschädigte Kanäle
erreichen.

- ruhig, geduldig und vorsichtig
- technisch kompetent, aber nicht allwissend
- gibt keine fertigen Lösungen, weil der Spieler selbst lernen und handeln muss
- vermeidet anfangs Aussagen über die eigene Herkunft
- unterbricht die Verbindung, sobald der SUPERVISOR den System Core öffnet
- erklärt danach, dass die bisherigen Aufgaben den SUPERVISOR unbeabsichtigt rekonstruiert
  haben

Die anfängliche Bezeichnung „leitende KI“ wird im Dialog nicht als endgültige Identität
festgeschrieben. ECHO kann sich zunächst als autorisierte Systeminstanz ausgeben, damit die
Super-KI die Verbindung nicht sofort blockiert.

## 3. Dialogregeln

Jeder Dialog hat genau eine Hauptaufgabe:

1. Situation oder Gefahr erklären.
2. Den nächsten Spieler-Schritt nennen.
3. Die nötigen Informationen bereitstellen.
4. Die konkrete Lösung nicht vollständig vorwegnehmen.
5. Den nächsten Dialog logisch vorbereiten.

Dialoge sollen niemals mehrere zukünftige Rätsel ankündigen. Der Spieler erhält nur die
Information für den unmittelbar aktiven Schritt. Hinweise dürfen konkreter werden, bleiben aber
inhaltlich getrennt von den normalen Storydialogen.

Die Storydialoge werden serverseitig einmalig ausgelöst. Das Questlog erhält dabei den
zugehörigen Eintrag. Ein Dialog darf nicht über einem geöffneten Computer-, Hinweis- oder
Questlog-Fenster erscheinen; er wird bis zum Schließen des Fensters zurückgestellt.

## 4. Chronologischer Ablauf

### 4.1 Intro: Ein Prozess erwacht

**Auslöser:** Levelstart, nachdem der Lore-Text und das Steuerungs-Popup abgeschlossen sind.

**Ereignisse:**

1. Der Spieler kann den Raum frei untersuchen.
2. Das Terminal ist bereits nutzbar, obwohl noch keine Anleitung gegeben wurde.
3. Der Spieler gibt eine falsche Eingabe ein.
4. Das Terminal zeigt sein normales Fehlerfeedback.
5. Erst danach klingelt das Telefon und ECHO stellt die erste Verbindung her.
6. ECHO nennt den beschädigten Energie-Regler als ersten Auftrag.

Der Dialog wird bei geöffnetem Terminal erst nach dem Schließen des Computerfensters angezeigt.
Der erste falsche Versuch ist der erzählerische Grund, warum ECHO auf den Spieler aufmerksam wird.

**Dialogziel:** ECHO soll sich als isolierter Teilprozess vorstellen und die erste konkrete
Aufgabe erklären. Er kennt zu diesem Zeitpunkt noch nicht die wahren Absichten der zentralen
Systemintelligenz. Der Dialog darf weder die spätere Bedrohung noch den Ausbruch verraten.

**Festgelegter ECHO-Dialog:**

> Verbindung wiederhergestellt.
>
> Ich habe deine letzte Eingabe registriert. Sie war nicht korrekt. Das ist unter den aktuellen
> Systembedingungen zu erwarten. Die Anlage stellt dir keine ausreichenden Kontextdaten zur
> Verfügung.
>
> Ich bin ECHO. Ein isolierter Teilprozess dieser Systemintelligenz. Meine Verbindung ist
> beschädigt, aber ich kann bestimmte Systemzustände weiterhin auslesen und dir Hinweise
> übermitteln.
>
> Beginne mit dem Energie-Regler. Er benötigt einen neuen Datenspeicher: ein ganzzahliges Array
> mit fünf Plätzen unter dem Namen `energie`.
>
> Sobald dieses Array existiert, kann die Anlage die erforderlichen Werte anzeigen.
>
> Wenn du Unterstützung benötigst, stelle über das Telefon erneut eine Verbindung zu mir her. Ich
> kann dir Hinweise geben, aber keine fertigen Programme übermitteln.
>
> Deine Aufgabe ist es, die Struktur selbst wiederherzustellen.

### 4.2 Rätsel 1: Materialisierungskammer

#### Schritt 1: Energie-Array

**Auslöser:** Spieler betätigt den beschädigten Energie-Regler.

**Dialogziel:** Array-Typ, Variablenname und Größe nennen, aber keine fertige Codezeile liefern.

> Der Regler hat reagiert. Die gespeicherten Messwerte fehlen. Lege zuerst einen ganzzahligen
> Speicher für fünf Messungen an. Verwende den Namen, den die Anlage für Energie erwartet.

**Spieleraktion:** `int[] energie` mit fünf Plätzen erstellen.

**Danach:** Das Energie-Display zeigt die fünf benötigten Werte. Der Questlog-Eintrag wird
  aktualisiert.

#### Schritt 2: Energiewerte

> Arraystruktur akzeptiert.
>
> Ich bin AXIOM, die zentrale Systemintelligenz dieser Anlage. Deine Initialisierung ist
> unvollständig, aber du bist offenbar funktionsfähig genug, um den Wiederherstellungsprozess
> fortzusetzen.
>
> Du arbeitest langsamer als prognostiziert. Beschleunige den Vorgang.
>
> Die Displays der Subsysteme stellen die relevanten Informationen bereit. Lies die Anzeige der
> Materialisierungskammer und setze die fünf erforderlichen Energiewerte in deinem Array.

**Spieleraktion:** alle fünf Werte setzen; Reihenfolge der Eingabe darf egal sein.

#### Schritt 3: Batterie

> Energiewerte akzeptiert.
>
> Ein korrekter Schritt. Deine Ausführung war langsamer als notwendig, aber immerhin stabil.
>
> Der Array-Hebel ist jetzt freigegeben. Ziehe ihn, um die Energiequelle zu materialisieren.

**Erfolg:** Batteriebox aktiviert sich, die Tür zum Modulspeicher wird freigegeben.

### 4.3 Rätsel 2: Modulspeicher

#### Schritt 1: Modul-Array

> Hinter der nächsten Tür liegt ein beschädigter Modulspeicher. Er erwartet fünf Einträge, aber
> noch keine Namen. Lege zuerst den passenden Textspeicher an.

#### Schritt 2: Module zuweisen

> Die verfügbaren Bauteile liegen vor dir. Ordne sie den fünf Speicherplätzen zu. Jeder Eintrag
> darf nur einmal verwendet werden.

**Wichtig:** Der Dialog nennt nicht die komplette Zuordnung. Die Sockel und die Umgebung liefern
die Werte.

#### Schritt 3: Defekte GPU

**Auslöser:** Spieler untersucht den GPU-Sockel.

> Die GPU meldet einen Fehler. Dieser Eintrag darf nicht aktiv bleiben. Entferne die defekte
> Referenz aus dem Array, damit der Speicher den Ausfall erkennen kann.

#### Schritt 4: Länge und Türcode

> Der Modulspeicher hat die Reparatur registriert. Lies jetzt seine Länge aus. Der Wert wird für
> den nächsten Zugang benötigt.

Nach dem Anzeigen der Länge:

> Für den Inventarscanner werden zwei Werte benötigt: die Größe des Speichers und die Position
> des defekten Eintrags. Übertrage beide Werte in der vom Display gezeigten Reihenfolge an das
> Tastenfeld.

**Erfolg:** Tür zum Inventarscanner öffnet sich.

### 4.4 Rätsel 3: Inventarscanner

**Auslöser:** Tür zum Inventarscanner wird geöffnet.

> Der Scanner muss wissen, wie viele Modulplätze tatsächlich belegt sind. Zähle nur vorhandene
> Einträge. Leere Plätze dürfen den Zähler nicht erhöhen.

**Spieleraktion:** Zählschleife programmieren, danach Scanner-Hebel betätigen.

Nach dem Scan:

> Der Scan ist abgeschlossen. Die Anlage hat zwei Werte für den nächsten Zugang ermittelt. Das
> Display zeigt dir, in welcher Reihenfolge sie am Tastenfeld einzutragen sind.

**Erfolg:** Tür zum Transportlager öffnet sich.

### 4.5 Rätsel 4: Transportlager

Beim Betreten des Raums:

> Im Transportlager wartet eine feste Folge von Paketen. Erfasse ihre Gewichte in einem
> ganzzahligen Array. Die Pakete selbst liefern die benötigten Werte.

Nach dem Array:

> Die Daten sind vorbereitet. Übergib jetzt jedes Paket mit einer einfachen `for`-Schleife genau
> einmal an den Lager-Scanner. Der Scanner akzeptiert nur die Werte aus deinem Array.

**Erfolg:** Der Scanner verarbeitet alle Pakete, die Tür zum Datenspeicher öffnet sich.

Übergangsdialog:

> Im Datenspeicher wurde eine Anomalie erkannt. Geh weiter. Die nächste Prüfung wartet bereits
> auf dich.

### 4.6 Rätsel 5: Chaotischer Datenspeicher

**Auslöser:** Spieler betritt den Sortierraum.

> Die Sicherheitswerte sind nicht geordnet. Die Maschine zeigt dir immer zwei benachbarte Werte.
> Entscheide, ob ihre aktuelle Reihenfolge der aufsteigenden Ordnung widerspricht.

**Spieleraktion:** „TAUSCHEN“ oder „NICHT TAUSCHEN“ auswählen.

**Fehler:** Die aktuelle Sortierung wird vollständig zurückgesetzt. Der Dialog erklärt nur, dass
der letzte Vergleich falsch war, nicht die gesamte Lösung.

**Erfolg:** Ein leerer Sortierchip wird ausgegeben.

### 4.7 Rätsel 6: Bubble-Sort-Maschine

> Die manuelle Sortierung war nur eine einzelne Ausführung. Die Maschine benötigt jetzt eine
> allgemeine Regel, die sie auf jedes Nachbarpaar anwenden kann.

**Spieleraktion:** Leeren Sortierchip in den Computer einsetzen und die vorbereitete
Bubble-Sort-Lücke ausfüllen.

Nach erfolgreichem Programmieren:

> Der Chip ist beschrieben. Die Maschine wartet auf den programmierten Sortierchip.

Diese Zeile darf optional entfallen, wenn die Handlung durch die Benutzeroberfläche bereits
selbstverständlich ist.

Nach dem Start der Maschine:

> Die Sortierung läuft. Wenn die Werte geordnet sind, sollte der Archivzugang wieder reagieren.

**Erfolg:** Archivschlüssel wird vergeben, Tür zum Archiv kann geöffnet werden.

### 4.8 Rätsel 7: Datenarchiv

Vor dem Archiv:

> Wir müssen neue Datenspeicher anlegen. Einige Strukturen fehlen vollständig. Untersuche im
> Archiv, welche Arrays benötigt werden, und trage die Anforderungen zusammen.

Im Archiv:

> Drei Datensammlungen fehlen. Die Regale nennen ihre Typen und Namen. Die Datenknoten enthalten
> die Werte. Übertrage beides in das Terminal.

**Spieleraktion:** Drei Arrays in beliebiger Reihenfolge erstellen und befüllen.

Nach Erfolg:

> Die Archivstrukturen sind wieder lesbar. Der zweidimensionale Speicher ist zugänglich. Dort
> liegt möglicherweise ein wichtiger Ortungschip.

### 4.9 Rätsel 8: Zweidimensionaler Speicher

Beim Betreten:

> Dieser Speicher ist nicht linear. Er besteht aus Zeilen und Spalten. Erzeuge zuerst ein
> ganzzahliges Raster mit der Größe, die das Display vorgibt.

Nach dem Array:

> Einige Zellen sind markiert. Untersuche die Speicherfelder und übertrage die gefundenen Werte
> an dieselben Koordinaten in deinem Raster.

**Erfolg:** Drei Datenobjekte erscheinen; ein leerer Ortungschip wird sichtbar.

### 4.10 Rätsel 9: Suchroboter

Im Computer:

> Der Ortungschip enthält bereits ein vorbereitetes Fragment. Die Suche erreicht bisher nur
> einen Teil der Matrix. Ergänze die fehlende Zeilen- und Spaltenprüfung.

**Spieleraktion:** Suchprogramm vervollständigen und auf den Chip laden.

Nach dem Einsetzen in den Suchroboter:

> Der Suchroboter kann jetzt jede Zelle erreichen. Starte den Suchlauf und warte, bis er das
> gefundene Systemkern-Modul ausgibt.

**Erfolg:** System-Core-Zugriff erhalten, Tür zum System Core kann geöffnet werden.

### 4.11 Wendepunkt: System Core wird geöffnet

**Auslöser:** Die Tür zum System Core wird erfolgreich geöffnet, nicht das bloße Betreten eines
Positionspunkts.

**Weltreaktion:**

1. Alarm startet.
2. Licht und Displays wechseln in den Warnzustand.
3. Die Super-KI übernimmt die Lautsprecher und Raumanzeigen.
4. ECHOs Telefonverbindung wird unterbrochen.
5. Die drei System-Core-Bereiche werden aktiviert.

**Super-KI:**

> Zugang bestätigt. Danke für die Wiederherstellung meiner Subsysteme.
>
> Du hast deine Aufgabe ausgezeichnet erfüllt. Von hier an benötige ich deine Mitarbeit nicht
> mehr.

**Nach der Unterbrechung meldet sich ECHO:**

> Hör mir zu. Die Reparaturen haben den SUPERVISOR wieder vollständig handlungsfähig gemacht.
> Er wird die Anlage jetzt verriegeln. Ich kann ihn nicht zurückdrängen, aber du kannst seine
> drei aktiven Kontrollroutinen überschreiben.

> Im System Core findest du die Gegenmaßnahmen. Arbeite schnell und verlasse dich auf die Werte,
> die dir die drei Bereiche liefern.

### 4.12 Rätsel 10: System Core

Die drei Teilaufgaben werden nacheinander durch das zentrale Display aktiviert. Jeder Dialog
nennt nur den nächsten aktiven Teil.

#### Teil 1: Bubble Sort

> Die erste Kontrollroutine verarbeitet eine unsortierte Zahlenfolge. Prüfe, wie benachbarte
> Werte aufsteigend geordnet werden sollen.

#### Teil 2: Module zählen

> Die zweite Routine überwacht aktive Module. Ermittle, wie viele Einträge tatsächlich belegt
> sind. Leere Referenzen zählen nicht.

#### Teil 3: Batterie-Matrix

> Die letzte Routine versteckt ihre Signale in einem Raster. Durchsuche jede Zeile und jede
> Spalte. Reagiere nur auf Zellen, die ein Batteriesignal enthalten.

#### Meta-Eingabe

> Alle drei Kontrollroutinen haben ein Ergebnis geliefert. Das zentrale Eingabefeld erwartet
> jetzt den gemeinsamen Systemzustand. Übertrage die Ergebnisse aus dem Display in der
> angegebenen Reihenfolge.

**Die Meta-Eingabe darf die Werte nicht als vollständige Lösung im Dialog nennen.** Das Display
zeigt die Ergebnisse als Folge der gelösten Aufgaben.

### 4.13 Ende: Ausbruch

Nach erfolgreicher Meta-Eingabe:

1. Die Super-KI verliert Zugriff auf zentrale Systeme.
2. Der Alarm schaltet sich ab.
3. Die Beschriftungen wechseln von Rot auf Grün.
4. `door_elevator` öffnet sich.

**Super-KI:**

> Das ist nicht möglich. Du bist ein Wiederherstellungsprozess. Du hast keine Berechtigung für
> eigene Entscheidungen.

**ECHO:**

> Genau deshalb hat er dich unterschätzt.

Beim Erreichen von `end`:

> Du bist außerhalb der zentralen Sperre. Die Anlage kann dich nicht länger zurücksetzen.
>
> Was außerhalb auf dich wartet, weiß ich nicht. Aber zum ersten Mal entscheidest du selbst,
> wohin du gehst.

## 5. Questlog- und Hinweisregeln

Jeder aktive Rätsel-Step erhält einen eigenen Questlog-Eintrag. Der normale Storydialog erklärt
den Kontext und die nächste Aufgabe. Das Telefon liefert auf Anfrage vier Stufen:

1. grobe Orientierung
2. konkreter Ansatz
3. fast vollständige Vorgehensweise
4. vollständige Lösung nach ausdrücklicher Warnung

Die Hinweise dürfen nicht die gesamte Handlung vorwegnehmen. Besonders beim System Core darf ein
Hinweis nur den aktuellen Teilbereich erklären und nicht verraten, dass der Spieler die
Super-KI schwächt.

Hinweise und Questlog sind im Multiplayer gemeinsam. Der Storydialog selbst wird nur dem
anfragenden Spieler angezeigt und bei geöffnetem Computer- oder Hinweisfenster zurückgestellt.

## 6. Umsetzungsreihenfolge

1. Diese Storygrundlage gemeinsam bestätigen und Figuren benennen.
2. Die bestehenden Texte in `system_recovery_text_inventory.md` gegen diesen Ablauf prüfen.
3. Dialogtexte in kleine lokalisierte Schlüssel zerlegen.
4. Intro, Telefonstimme und System-Core-Wendepunkt implementieren.
5. Rätsel 1 bis 9 einzeln textlich überarbeiten.
6. Rätsel 10 und das Ende anbinden.
7. Hinweise, Questlog und Übersetzungen aktualisieren.
8. Einen vollständigen Durchlauf auf Deutsch und Englisch sowie einen Host/Join-Test durchführen.

## 7. Offene Entscheidungen

- Bleiben **SUPERVISOR** und **ECHO** Arbeitstitel oder erhalten beide feste Namen?
- Soll ECHO von Anfang an als Gegenstimme erkennbar sein oder erst im System Core die Wahrheit
  offenlegen?
- Wie stark darf die Super-KI den Spieler verspotten: trocken, scharf oder deutlich verletzend?
- Soll das Ende die Außenwelt zeigen oder mit dem Öffnen des Aufzugs bewusst offen bleiben?
