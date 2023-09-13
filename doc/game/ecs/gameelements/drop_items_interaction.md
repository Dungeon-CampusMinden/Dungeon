---
title: "Drop Items Interaction"
---

## Wofür

Eine Implementation des IInteraction Interfaces wo alle Gegenstände, die im Inventar der Entität sind,
auf dem Boden fallen gelassen werden.

## Benötigte Komponenten

- [`InventoryComponent`](../components/inventory_component.md):
  Das Inventar der Entität, welche die Items enthält, die fallengelassen werden sollen.
- [`PositionComponent`](../components/position_component.md):
  Die Position der Entität, welche die Items fallen lassen soll. Es wird dazu benötigt, um die Items an der richtigen Stelle fallenzulassen.

## Erzeugen

Ein Objekt dieser Klasse lässt sich mit dem Default-Konstruktor erzeugen. Daher: Es werden keine weiteren Parameter benötigt.
