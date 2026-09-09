# Akt 1: Seelenwerkbank

Die beiden Werkstattkisten liefern Eigenschaftsrunen und einen wiederverwendbaren
Gefäßvorrat. Am Golem öffnet sich das Canvas: sechs benannte Fassungen um seinen
Seelenkern. Die eingeritzten Sollwerte sind sichtbar, die Java-Typnamen noch nicht.

Zuerst zieht der Spieler Gefäße auf die Eigenschaften. Ein Gefäßtyp kann mehrfach
verwendet werden: Lebensenergie und Schritte brauchen jeweils eine eigene Eisenkiste.
Nach der vollständigen Zuordnung öffnet sich das Essenzfach mit sieben Werten.

## Was die Handlung vermittelt

- **Name:** Jede Fassung bezeichnet eine bestimmte Eigenschaft.
- **Typ:** Das Gefäß begrenzt, welche Werte hineinpassen. Text passt nicht in eine
  Eisenkiste; der vorherige Inhalt bleibt bei einem solchen Versuch erhalten.
- **Wert und Zuweisung:** `17` passt in die Lebensenergie-Kiste, erfüllt aber nicht
  den Sollwert `125`. Der neue Wert ersetzt den alten, nicht das Gefäß oder den Namen.
  Auch `false` ist ein zulässiger Inhalt der Lichtkugel, aktiviert Nox jedoch nicht.
- **Mehrere Variablen desselben Typs:** Der gemeinsame Vorrat erzeugt unabhängige
  Fassungen. Ein Wertwechsel bei Lebensenergie verändert Schritte nicht.

Die Werkstatt verwendet für ganze Zahlen Eisenkisten und für Bruchteile Kristall.
Das ist Valerius' Bauplan, keine Behauptung, dass `double` keine ganzen Zahlen speichern
kann. Die Kristallflasche nimmt im Essenzschritt auch `17` und `125` an. Das Modell
beschränkt sich auf die angebotenen Wertkategorien; Java-Konvertierungen werden hier
nicht vollständig simuliert.

Bei sechs passenden Werten werden die Typnamen und die zusammengesetzten Deklarationen
sichtbar, etwa `int schritte = 17;`. Erst ein bewusster Klick auf **Aktivieren** startet
den bestehenden Wanddurchbruch und Nox' Weg in den Maschinenkeller.

Fassungen lassen sich vor Abschluss leeren und Werte beliebig oft ersetzen. Es wird
nichts verbraucht. Schließen und Wiederöffnen verlieren keine Zuordnung. Im Multiplayer
sind Vorräte, Füllungen und Fortschritt gemeinsam; der Server prüft Nähe, Phase und
Eingaben. Die Raumgeometrie und die Laufstrecke zu Akt 2 bleiben unverändert.

## Noch provisorische Bilder

Die Werkbank nutzt vorhandene Assets. Für die Eisenkiste steht derzeit die gewöhnliche
Kistengrafik; für den Runenstein ein Amethyst. Eigene Metallgefäß- und gravierte
Runenstein-Icons fehlen. Die übrigen Gefäße verwenden Flasche, Pergament und Lichtkugel.
Die Essenzen teilen sich ein Kristallbild; ihr gespeicherter Wert unterscheidet sie.
