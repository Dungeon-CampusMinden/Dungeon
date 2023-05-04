---
title: "Playable Component"
---

## Wofür
Dieses Component ist für den Spielercharakter vorgesehen. Es markiert eine Entität als die vom Spieler steuerbare Entität.
Dieses Component sollte nur von genau einer Entität im Spiel implementiert werden, da die Codebasis aktuell nicht auf mehrere spielbare Charaktere ausgelegt ist.
Es speichert alle Daten, die nur für den Spieler der Entität sind.
Speichert die Skills, die im PlayerSystem über Skilltasten ausgelöst werden.

## Wie nutzt man es
Das Component beinhaltet einfache Setter und Getter. Die genaue Verwendung entnimmt man der Javadoc.

## Testabdeckung
Alle Methoden sind zum Stand am 02.04.2023 getestet.
