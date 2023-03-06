---
title: "XPSystem"
---

## Funktion
- Durchführen eines LevelUps, wenn eine Entität genügend XP gesammelt ha, um das nächste Level zu erreichen.

## Key-Components
- XPComponent

## Benötigte Components
- XPComponent

## Optionale Components
- Keine

## Designentscheidungen
- Wenn eine Entität in einem Tick/Frame mehr als ein Level aufsteigen kann, wird onLevelUp() für jedes Level aufgerufen.
Dies dient dazu, dass möglicherweise bei einem LevelUp, mit einem bestimmten Level, eine bestimmte Aktion ausgeführt werden soll,
welche möglicherweise übersprungen wird, wenn nicht für jedes Level onLevelUp() aufgerufen wird.
