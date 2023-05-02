---
title: "DropLoot"
---

## Wofür

Eine Implementation des IOnDeathFunction Interfaces wo alle Gegenstände, die im Inventar der Entität sind, auf dem Boden fallen gelassen werden.

## Aufbau

Benötigt folgende Komponenten um zu funktionieren.
 - PositionComponent
 - InventoryComponent

## Wie nutzt man es

Einfach mit dem Default Konstruktor erstellen und dann dem HealthComponent mitgeben.

## Testabdeckung

Die public Methode wurden mit den Theoretisch problematischen Fällen kontrolliert. Nicht testbar sind private Methoden, welche nur im Gesamtpaket kontrolliert werden können.
