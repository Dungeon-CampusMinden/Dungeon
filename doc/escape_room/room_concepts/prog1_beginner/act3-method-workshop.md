# Akt III: Valerius' Werkstatt

Teil des Raums [Programmieren 1: Das Erbe der Seelenweber](prog1_beginner_concept.md).

## Auftrag und Geschichte

Nox hat im Keller den Räumauftrag erledigt und dabei die Torwinde beschädigt.
Der Weg führt durch den geöffneten Durchbruch zurück in Valerius' Werkstatt.
Ihr eigener Nebenausgang wird von zwei Kristallaltären versorgt. Der erste benötigt
drei, der zweite fünf Kristalle. Zwei Runensteine geben die entsprechenden Vorräte frei.

Valerius hat den Ablauf als langes Programm hinterlassen. Die Werkbank kann diesen
Entwurf testen; die Steuer-Rune für den Ausgang hat jedoch nur acht Hauptspeicherplätze.
Methoden speichern wiederkehrende Arbeit getrennt vom Hauptprogramm. Die Lernenden
müssen den Ablauf verstehen und so umformen, dass er weiter funktioniert.

Der Auftrag hat zwei sichtbare Teile:

- Beide Altäre versorgen; keine Kristalle übrig lassen.
- Ein Hauptprogramm mit höchstens acht Anweisungen herstellen, in dem eine Methode
  mit Eingaben mehrfach verwendet und ein Rückgabewert im Aufrufer übernommen wird.

Die Ziele stehen in der Hilfe. Namen, genaue Methodengrenzen und
die Reihenfolge unabhängig ausführbarer Aktionen sind keine versteckten Lösungsschlüssel.

## Arbeitsplatz

Ein begrenzter, verschiebbarer und zoombarer Canvas von 5400 × 3600 Einheiten enthält
das Hauptprogramm, einen Methodenentwurf und die gebauten Methodenrunen. Unbenutzte
Blöcke liegen frei auf dem Hintergrund, einzeln oder als lose Codegruppen. Die Fenster
starten in der Mitte der Arbeitsfläche. Beim Öffnen
richtet sich der sichtbare Ausschnitt nach den gespeicherten Fensterpositionen.
Die Kopfzeile enthält Ausführung, Raumansicht und Hilfe. Es gibt keine Aufgabenfolge mit
„Weiter“, keinen Vergleichsbildschirm und keinen manuellen Reset-Schritt.

Anweisungen werden an der ganzen Zeile gezogen. Die obere beziehungsweise untere
Hälfte einer Zielzeile fügt sie davor beziehungsweise dahinter ein. Strg-Klick wählt
einzelne Zeilen aus oder ab, Umschalt-Klick einen zusammenhängenden Bereich.
Eine ausgewählte Zeile zieht alle ausgewählten Anweisungen desselben Fensters in
ihrer ursprünglichen Reihenfolge mit. Die Schaltfläche „...“ öffnet den Zeileneditor.
Dies funktioniert innerhalb eines Programms und zwischen Hauptprogramm,
Methodenentwurf und freiem Hintergrund. Lose Gruppen lassen sich an ihren Codezeilen
verschieben und wieder in ein Programm ziehen. Lange lose Gruppen scrollen innerhalb
einer begrenzten Höhe. Ihre Position und Gruppierung bleiben
beim Schließen und Öffnen erhalten. Die Reihenfolge ist ausführbare Logik, keine Dekoration.
Eine kleine Befehlsauswahl liefert zusätzliche Anweisungen zum Experimentieren.
Die Fenster bleiben innerhalb der Arbeitsfläche. Ziehen mit gedrückter rechter
Maustaste ändert ihre Größe: Hauptprogramm und Bausteinauswahl nur in der Höhe,
der Methodenentwurf auch in der Breite. Fenster werden am Titel verschoben.
Das Mausrad scrollt das Fenster unter dem Zeiger. Auch an den Scrollgrenzen bleibt der
Zoom unverändert. Über dem freien Hintergrund zoomt es die Arbeitsfläche.
Rechts neben dem Hauptprogrammzähler schaltet „Original“ auf den unveränderten Startcode
zum Vergleichen. Diese Ansicht ist schreibgeschützt: keine Bearbeitung, Auswahl oder
Drag-and-drop von Codeblöcken hinein oder heraus. „Mein Code“ wechselt zurück zum eigenen
Programm; beide Ansichten behalten ihre Scrollposition. Der Zähler und „Ausführen“ beziehen
sich weiterhin auf das eigene Programm. Die Ansicht ist eine lokale Einstellung des Spielers.

Der Methodenentwurf hat einen frei wählbaren Namen, Eingaben und einen Körper.
„Methode bauen“ erzeugt ausdrücklich eine benannte Rune. Eine Rune lässt sich als
Aufruf ins Hauptprogramm ziehen. Argumente, Zuweisungsziel und Rückgabewerte bleiben
bearbeitbar. Gebaute Methoden können erneut bearbeitet und bewusst ersetzt werden.
Unfertige Entwürfe verändern eine bereits gebaute Rune nicht stillschweigend.
Eine Methode darf höchstens sechs Anweisungen enthalten, einschließlich Aufrufen und
Rückgaben. Das Methodenfenster zeigt die Zeilenzahl als `4 / 6` und nummeriert die Blöcke.
Längere Entwürfe bleiben bearbeitbar; Überschrift und Hinweis werden rot, und
„Methode bauen“ ist gesperrt. Auch der Server lehnt das Bauen mit mehr als sechs Zeilen ab.
Eine zuvor gebaute Version bleibt dabei erhalten. Nach dem Kürzen lässt sich der Entwurf
wieder bauen.
`GIB_ZURÜCK` beendet die Methode sofort. Alle Blöcke danach werden im Entwurf rot
als nicht erreichbar markiert, mit Verweis auf die Zeile der ersten Rückgabe.
Das Methodenfenster zeigt die Warnung auch oberhalb des scrollbaren Codes und scrollt
zur ersten betroffenen Zeile. UI und Server verhindern das Bauen, bis diese Blöcke
gelöscht oder vor die Rückgabe verschoben wurden. Eine Rückgabe als letzte Anweisung
ist erlaubt; eine Methode ohne Rückgabe bleibt ebenfalls erlaubt.

In „Variable setzen“ sind auch Aufrufe gebauter Methoden erlaubt, etwa
`kristalle = 1 + meineMethode(15)`. Ausdrücke unterstützen Zahlen, Variablen, `+`, `-`,
Klammern und verschachtelte Methodenaufrufe, auch in Argumenten und Rückgaben.
Aufrufe werden von links nach rechts ausgeführt, einschließlich ihrer Aktionen im Raum.
Danach wird mit dem Rückgabewert weitergerechnet. Fehlt die Rückgabe, stoppt der Lauf
mit einer Fehlermeldung. Das Speicherziel direkt am Aufruf bleibt eine gleichwertige
Option; beide Schreibweisen zählen als Verwendung eines Rückgabewerts für die Kontrollrune.

Hilfe enthält Auftrag, Bedienung, Begriffe, gestufte Denkanstöße und die letzte
Rückmeldung. Ausführungsfehler stehen zusätzlich direkt am betroffenen Block im
Hauptprogramm, rot markiert und mit ausgeschriebener Meldung. Bei Fehlern innerhalb
einer Methode wird der auslösende Hauptprogrammblock markiert. Das Codefenster scrollt
zur Fehlerzeile. Die Meldung bleibt beim Wechsel aus der Raumansicht und beim erneuten
Öffnen erhalten, bis das Hauptprogramm geändert, eine Methode gebaut oder ein neuer
Lauf gestartet wird. Manuelles Stoppen markiert keinen Block als fehlerhaft.
Fehler beim Methodenbau stehen direkt im Methodenentwurf. Die Hilfe ist jederzeit
schließbar. Der Canvas bleibt beim Umschalten und bei neuen Serverzuständen erhalten.
Solange die Methodenwerkstatt geöffnet ist, sind Bewegungen und Interaktionen im Level
für diesen Spieler gesperrt, auch in der Raumansicht und Hilfe. Nox und die Simulation
laufen weiter. Andere Spieler bleiben steuerbar; Schließen gibt die Eingaben wieder frei.

Über dem Canvas bleibt eine Prüfleiste unabhängig von Zoom und Verschiebung sichtbar.
Sie unterscheidet sechs Bedingungen: Arbeitsstellen erledigt, Nox trägt keine Kristalle,
Variable `kristalle = 0`, höchstens acht Hauptblöcke, dieselbe parametrisierte Methode
mehrfach aufgerufen und einen Rückgabewert verwendet. Jede Bedingung zeigt ausdrücklich
„Erfüllt“, „Offen“ oder „Ungeprüft“. Die Blockanzahl ist sofort prüfbar; die übrigen
Bedingungen werden erst nach einem vollständig ausgeführten Programm bewertet.
Die Abschlussmeldung nennt nur die fehlenden Bedingungen mit konkreten Ist- und Sollwerten.
Nox' Kristallvorrat und die Variable `kristalle` werden getrennt benannt.
Nach Änderungen am ausführbaren Programm werden die Ergebnisse als ungeprüft angezeigt;
Änderungen an einem noch nicht gebauten Methodenentwurf verändern das Ergebnis nicht.
Bei einem Laufzeitfehler nennen Prüfleiste und Raumansicht die nummerierte Hauptprogrammzeile.

## Ausführung und Fehlversuche

„Ausführen“ startet immer einen ganzen Versuch. Vor dem Start stellt die Werkbank
Nox, Tore, Runensteine, Kristalle und Altäre auf den Versuchsbeginn zurück. Der Code
und die gebauten Methoden bleiben erhalten. Diese Rückstellung gehört ausdrücklich
zum Testbetrieb; während der Ausführung gibt es keine unsichtbaren Wege oder Aktionen.

Nox führt ausschließlich die angeschlossenen Anweisungen aus. Der Blick auf den Raum
und die aktuelle Anweisung machen die Wirkung beobachtbar. „Stopp“ beendet auch eine
laufende Bewegung. Nach einem Fehler bleibt das Ergebnis bis zum nächsten Start sichtbar.

Beispiele für echte Fehler:

- Ein fehlendes Öffnen lässt Nox am Tor stehen.
- Eine falsche Richtung führt an einem Runenstein vorbei oder gegen eine Wand.
- Sammeln vor dem Freigeben liefert keine aktivierte Quelle.
- Ein zu großer Ablagewert passt nicht in den Altar oder überschreitet Nox' Vorrat.
- Ein lokaler Sammelwert ohne Rückgabe steht im Hauptprogramm nicht zur Verfügung.
- Eine vergessene Zuweisung verändert die Vorratsrechnung des Hauptprogramms nicht.
- Eine unbekannte Variable oder ein fehlendes Argument stoppt an der betreffenden Zeile.

Eine begrenzte Befehlszahl und Aufruftiefe beenden rekursive oder überlange Programme.
Fehler und Experimente können keinen dauerhaften Softlock erzeugen. Bearbeitung und
Ausführung bleiben serverautoritativ; weitere Spieler sehen denselben Versuch.

## Lernweg ohne vorgeschriebene Klickfolge

1. Den langen Entwurf ausprobieren und seine Wirkung im Raum beobachten.
2. Wiederholte Arbeit erkennen, beispielsweise Öffnen und Weitergehen an beiden Toren.
3. Einzelne Zeilen in eine eigene Methode ziehen, benennen und bauen.
4. Wiederholungen durch Aufrufe ersetzen und erneut testen.
5. Unterschiede zwischen ähnlichen Stellen durch Eingaben ausdrücken: etwa die
   Drehrichtung oder die benötigte Kristallzahl.
6. Einen lokalen Sammelwert zurückgeben und im Hauptprogramm übernehmen.
7. Das gesamte, kürzere Programm testen und den versorgten Ausgang öffnen.

Diese Reihenfolge ist eine mögliche Herangehensweise, keine Sperre im Editor.
Eine Methode darf anders heißen, größere Abschnitte enthalten oder andere Methoden
aufrufen. Zusätzliche, unverbundene Blöcke auf dem Hintergrund werden nicht ausgeführt.
Zufällige Ablenkungsblöcke werden zunächst nicht vorgegeben: Die eigenen Experimente
liefern bereits sinnvolle Fehler, ohne die Aufgabenstellung zu verschleiern.

## Raum und Übergang

Die Werkstatt bleibt ein zusammenhängender kleiner Raum mit zwei echten Schwellen,
den beiden Runensteinen, zwei Kristallfeldern und zwei Altären vor dem Nebenausgang.
Tore schließen ihre gesamte Passage. Die Laufwege berücksichtigen Nox' fünf Felder
breite Kollisionsfläche; Möbel stehen außerhalb dieser Wege. Aktionen beziehen sich
auf tatsächlich erreichbare Objekte, nicht auf eine unsichtbare Aufgabennummer.
Leere Kristallfassungen an den Altären verwenden den Slot-Shader. Beim Ablegen wird
der Amethyst sichtbar; ein neuer Versuch stellt die leeren Fassungen wieder her.

Der Keller und das Archiv behalten ihren bestehenden Ablauf. Nach dem letzten
Kellerauftrag kehrt Nox über den normalen kollisionsgeprüften Weg zur Werkbank zurück.
Der Übergang ist getrennt von den frei programmierten Testläufen: Seine Ankunft ist
der feste Ausgangspunkt jedes Versuchs.

## Abnahme

- Durchspielen des langen Entwurfs und eines kompakten Programms mit Methoden.
- Falsche Bewegung, falsche Mengen, fehlende Rückgabe und anschließender erfolgreicher Versuch.
- Verschieben, Einfügen, Bearbeiten und Bauen durch die sichtbare Oberfläche.
- Unverbundene Blöcke, veränderte Methoden und lokale Variablen verhalten sich nachvollziehbar.
- Stopp während Bewegung, erneuter Start und Schließen/Öffnen des Editors.
- Darstellung bei kleiner und maximierter Fenstergröße sowie Nativeingaben mit `cua-driver`.
- Letzter Kellerauftrag und tatsächlicher Lauf in Akt III; keine Teleport-Abkürzung für diesen Check.

Die Zielzeit bleibt vorläufig 20–25 Minuten. Verständlichkeit, Spaß und tatsächliche
Dauer müssen mit Lernenden getestet werden; technische Abnahme ersetzt diesen Playtest nicht.

## Beispiellösung

Diese Lösung lädt auch die Hilfe:

```text
hilfeTor():            ÖFFNE(); GEHE(4);
hilfeRune(richtung):   GEHE(1); DREHE(richtung); GEHE(3); AKTIVIERE();
hilfeSammeln():        GEHE(1); gesammelt = SAMMLE_ALLE(); GEHE(1); GIB_ZURÜCK gesammelt;
hilfeAltar(menge):     GEHE(1); LEGE_AB(menge); GEHE(1); GIB_ZURÜCK menge;

Hauptprogramm:
hilfeTor();
hilfeTor();
hilfeRune(RECHTS);
hilfeRune(LINKS);
kristalle += hilfeSammeln();
kristalle += hilfeSammeln();
kristalle -= hilfeAltar(3);
kristalle -= hilfeAltar(5);
```

Nach dem erfolgreichen Lauf öffnet sich der Nebenausgang. Ohne Fehlversuch gibt es den
Erfolg "Aus einem Guss", sonst "Übung macht den Meister".

## Lernziel

- Wiederholte Abläufe als benannte Methode herauslösen.
- Unterschiede zwischen ähnlichen Stellen als Parameter übergeben.
- Ein lokales Ergebnis mit `GIB_ZURÜCK` an den Aufrufer geben und dort weiterrechnen.
- Lokale Methodenvariablen von Variablen des Hauptprogramms unterscheiden.
- Refactoring ändert die Form des Programms, nicht seine Wirkung.
