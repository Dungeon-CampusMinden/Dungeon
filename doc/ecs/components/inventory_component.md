---
title: "Inventory Component"
---

## Wofür
- Wofür braucht es diese Component?

Diese Komponente ist dafür gedacht das der Entität ein Inventar hinzugefügt werden kann.
- Was macht es?

Es besitzt eine Liste welche mit `Item`s gefüllt werden kann. Sowie besitzt es eine Kapazität(`maxSize`) für das Inventar.


## Aufbau

- UML
- Aufbau Erklären

## Wie nutzt man es
- Welche Parameter gibt es, was machen diese?
Der Konstruktor nutzt zwei Parameter der erste ist wie bei jeden Component die Entity, an dem die Komponente angehangen werden soll.
Der zweite Parameter ist die größe des Inventars hier wird eine positive ganzzahl erwartet welche größer ist als 0.

addItem erlaubt es einen ein Item zum Inventar hinzuzufügen hier zu muss ein Item übergeben werden, welches gültig ist.
der Rückgabe wert hier ist immer true, wenn es erfolgreich war den Gegenstand hinzuzufügen. Und es wird ein false zurückgegeben, wenn der Gegenstand nicht hinzugefügt wurde, gründe dafür können sein ein Gegenstand, welcher bereits im Inventar vorhanden ist oder wenn der gegenstand nicht mer ins inventar passt.

removeItem erlaubt es einen Gegenstand aus dem Inventar zu entfernen.
der Rückgabewert hier ist immer true, wenn der Gegenstand entfernt wurde. Wenn der Gegenstand nicht entfernt werden kann, z.b. da er nicht existiert wird ein false zurückgegeben.

Die 3 Methoden filledSlots, emptySlots und getMaxSize existieren, um den aktuellen Status des Inventares zu kontrollieren.



## Implementierungen der Strategien
- FÜr jede Implementierung einen eigenen Header mit: Wofür, Aufbau, Nutzen

Hier wurden keine Strategien benötigt da es hier stand commit x keine Komplexität gibt.

## Testabdeckung
- Wie ist die Testabdeckung?
Stand commit x 100% abdeckung

- Welche Methode/Funktionalitäten können nicht getestet werden, warum?
In dieser Komponente gibt es stand commit x keine Funktionalität welche nicht getestet werden kann. Hier sind keine Visuellen Komponente welche schwerer zu testen sind.
