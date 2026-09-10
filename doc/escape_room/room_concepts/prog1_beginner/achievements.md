# Achievements im Programmierraum

Die Auslöser liegen in der serverseitigen Spiellogik. Die bestehenden Popups und das
Achievement-Menü verwenden `achievements/programming.json`; der lokale Fortschritt
wird getrennt von anderen Räumen in `programming-achievement-unlock.json` gespeichert.
Popups sind in diesem Raum stumm. Persönliche Experimente gehen an den handelnden
Spieler, gemeinsame Fortschritte an alle anwesenden Spieler.

| Achievement | Auslöser | Empfänger | Versteckt |
| --- | --- | --- | --- |
| Im Rhythmus | Sammelt eure erste Rhythmusrune. | Team | Nein |
| Das ganze Archiv | Sammelt alle 24 Rhythmusrunen in einem Durchlauf. | Team | Nein |
| Seelenweber | Vervollständigt Nox' Seelenbindung. | Team | Nein |
| Passt doch fast | Versuche, eine Essenz in ein Gefäß des falschen Typs zu füllen. | Handelnder Spieler | Ja |
| Neu befüllt | Ersetze einen gespeicherten Wert durch einen anderen, ohne das Gefäß zu leeren. | Handelnder Spieler | Ja |
| Noch fünf Minuten | Setze Nox' Eigenschaft Aktiviert auf false. | Handelnder Spieler | Ja |
| Licht aus | Lösche eine brennende Fackel. | Handelnder Spieler | Ja |
| Auf Sicht | Beobachte Nox durch den Sehstein im Keller. | Handelnder Spieler | Nein |
| Läuft | Schließt den ersten Räumauftrag im Keller ab. | Team | Nein |
| Fehler gefunden | Schließt einen Keller-Auftrag nach mindestens einem Fehlversuch an derselben Arbeitsposition ab. | Team | Ja |
| Dreht sich im Kreis | Lasst die endlose Drehrune bis zum Sicherheitsstopp laufen und Nox zurückkehren. | Team | Ja |
| Mit Nebenwirkungen | Schließt alle fünf Räumaufträge ab und öffnet den Weg aus der Werkstatt. | Team | Nein |
| Auf Anhieb | Schließt alle fünf Keller-Aufträge in einem Durchlauf ohne fehlgeschlagenes Steuerprogramm ab. | Team | Nein |
| Valerius wäre stolz | Schalte alle anderen Achievements dieses Raums frei, auch über mehrere Durchläufe. | Lokaler Gesamtfortschritt | Nein |

`Fehler gefunden` erfordert einen fehlgeschlagenen Versuch an derselben Arbeitsposition.
Der Zähler dafür wird erst nach deren erfolgreichem Räumauftrag zurückgesetzt.
`Auf Anhieb` schließt jeden fehlgeschlagenen Steuerlauf in diesem Kellerdurchlauf aus,
auch wenn Nox seinen Startpunkt noch nicht verlassen hat. Beide Achievements sind daher
nicht im selben Durchlauf erreichbar. Fehler bei der Seelenbindung zählen dafür nicht.

`Dreht sich im Kreis` wird erst nach dem Sicherheitsstopp und der abgeschlossenen
Rückkehr verliehen. Das bloße Einsetzen der Rune reicht nicht. Die normalen Schranken
für Entfernung, Besitz, Phase und laufende Ausführung bleiben vor allen Auslösern aktiv.

Die erste Rune und die Sammlung werden nur nach einer neuen, akzeptierten Aufnahme
geprüft. Die Sammlung muss vor Abschluss des letzten Keller-Auftrags vollständig sein.
Der Gesamtabschluss schaltet sich lokal frei, sobald alle 13 anderen Erfolge gespeichert
sind; dafür sind mehrere Durchläufe vorgesehen. Er behauptet keinen Abschluss von Akt 3.
