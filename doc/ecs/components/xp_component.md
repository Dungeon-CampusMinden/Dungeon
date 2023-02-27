---
title: "XP Component"
---

## Wofür
- Einer Entität die Möglichkeit geben XP zu sammeln und Level aufzusteigen.
- Zähl gesammelte XP Punkte und das aktuelle Level.

## Aufbau

![UML](../img/xpComponent.png)

- XPSystem prüft, ob ein LevelUp stattfinden muss (`getXPToNextLevel()` gibt `<= 0` zurück) und führt dieses durch
- HealthSystem fügt XP hinzu, wenn ein gegner getötet/zerstört wird

## Wie nutzt man es
- Hinzufügen der Komponente `XPComponent` zu einer Entität durch Erzeugen eines neuen
XP-Component-Objekts, bei dem als Konstruktorparameter die Entität übergeben wird.
- `FORMULA_A` & `FORMULA_B` sind zwei Parameter zum Anpassen der XP->Level Formel.
Die Formel gibt an wie viele XP pro Level benötigt werden.

## Testabdeckung
- Es werden alle Methoden der Funktion getestet.
