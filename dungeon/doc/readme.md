---
title: "Framework Dokumentation"
---

In diesem Verzeichnis ist die Dokumentation für das Dungeon-Framework (Projekt `game`) zu finden.

## Was ist das Dungeon Framework?

Das Dungeon-Framework ist ein auf [libGDX](https://libgdx.com/) basierendes Java-Framework zur Entwicklung eines 2D-Rogue-Like Rollenspiels. Es richtet sich vor allem an Programmieranfänger und soll einen einfachen Einstieg in die Java- und Spielentwicklung bieten. Das Framework stellt bereits die wichtigsten Strukturen und Funktionalitäten für die Umsetzung eines einfachen Spiels zur Verfügung. Dies umfasst das Bereitstellen einer [Entity-Component-System-Architektur](./ecs_basics.md) mit Basis-Systemen zum Zeichnen von Objekten, dem Generieren von Leveln sowie dem Steuern und Bewegen von Spielfiguren.

Um schnell mit dem Programmieren anzufangen, kann der [QuickStart](./quickstart.md) zur Hilfe genommen werden.

## Projekt Packagestruktur

Das Projekt ist in zwei Hauptpackages aufgeteilt. In `core` liegt die wesentliche Implementierung des Frameworks. In diesem Package befindet sich die Game-Loop, die Logiken zum Generieren einfacher Level, die Logik zum Anzeigen der Spielelemente sowie die wesentlichen Systeme zum Erstellen eines spielbaren Helden.

Im Package `contrib` sind weitere Spielinhalte und Logiken zu finden, die über ein Basisspiel hinausgehen. Das beinhaltet zum Beispiel ein Lebenspunkte-System, einen Feuerball-Skill, Crafting und Items oder verschiedene HUD-Elemente.

Die beiden Packages sind intern identisch aufgebaut. In den unterpackages `Components` befinden sich alle Components, in den unterpackages `systems` befinden sich die jeweiligen Systeme. Das unterpackage `utils` beinhaltet eine Vielzahl an kleineren und größeren Hilfsklassen.

Für größere Bausteine, wie die Spiellevel oder das Crafting, gibt es eigene Packages.

Zusätzlich befinden sich im oberen Package `starter` vorkonfigurierte Main-Methoden zum Starten des Spiels.


## Entfernte Features

Einige Features haben es nicht über die Ziellinie geschafft, um Inspiration zu holen können sie aber noch gut sein.
Suche im GitHub-Repository nach dem Tag `before-remove-unused-features`.
