---
title: "Virtuelle Escape-Rooms in der Lehre - Glossar"
---

# Akteure

## Designer

Designer erstellen mit dem Dungeon Escape-Rooms zur Erreichung von Zielen aus der Lehre.

## Spieler

## (Spieler-) Charakter

Charaktere sind von Spielern gesteuerte Figuren.

## NPCs

NPCs sind Figuren, die von der Simulation gesteuert werden.

# Geschichte

Die Geschichte ist das, was die Spieler bei dem Durchlauf eines Escape-Rooms erleben.

## Geschehnis

Geschehnisse sind Zustandsänderungen der Geschichte, deren Zeitpunkt und Ablauf vom Designer vorgegeben ist.

> Bsp.: Ein Dschinn erscheint 30 Sekunden nach Spielstart und verlangt von den Charakteren die Preisgabe ihrer Namen.

## Ereignis

Ereignisse werden von Spielern initiiert bringen die Handlung der Geschichte/die Beendigung des Escape-Rooms voran.

TODO "geschehnis" und "ereignis" in anlehnung an die erzähltheorie von lahn und meister. das ereignis ist wichtigere. geschehnisse sind nicht wesentlich.

### (Lern-) Rätsel

Rätsel sind Ereignisse, die auf die Erreichung von Lernzielen zusteuern. Lernziele sind z.B. Inhalte von Lehrveranstaltungen oder Soft-Skills, wie logisches Denken, Problemlösungsstrategien, Kommunikation in der Gruppe und Zusammenarbeit.

TODO wollen wir Rätsel für Soft-Skills namentlich trennen?
TODO rian hat auch den begriff "aufgabe" eingeworfen, die die charaktere erhalten. sollte der umstand der aufforderung von spieler/charakter hier hervorgehoben werden?

### Aktivität

Aktivitäten sind Ereignisse, die keine Rätsel sind (und nicht direkt ein Lernziel verfolgen). Ein Designer kann Aktivitäten hinzufügen, um z.B. Interaktionen mit dem Escape-Room vorzustellen, die Erzählung der Geschichte zu verbessern oder auf bestimmte Lerntypen ausgerichtete Optionen anzubieten.

> Bsp.:
> - Die Charaktere geben dem Dschinn ihre Namen.
> - Die Charaktere tauschen ihre Meinungen von dem Dschinn in einem Gespräch am Lagerfeuer aus.

## Mechanik

Ereignisse und Geschehnisse werden durch eine Mechanik implementiert. Die Mechanik bestimmt die Art und Weise mit der die Spieler mit der Geschichte, mit dem Escape-Room oder miteinander interagieren.

> Bsp.:
> Kombinationsrätsel
> Suche
> Logikaufgabe

### Interaktion

Mechaniken werden durch eine nicht-leere Menge von Interaktionen definiert, deren Abhängigkeiten ein Designer festlegen kann.
Manche Interaktionen werden von den Spielern ausgelöst, andere von der Simulation.

TODO Diese Interaktionen müssen vom Dungeon implementiert und unterstützt werden.

> Bsp.:
> - Ein Charakter zieht einen Hebel
> - Der Dschinn belehrt die Spieler über die Folgen ihrer Handlungen.
