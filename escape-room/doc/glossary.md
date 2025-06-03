---
title: "Virtuelle Escape-Rooms in der Lehre - Glossar"
---

# Escape-Room

Die grundlegende Spielidee (Ausbruch aus einem begrenzten Raum) für Escape-Rooms geht auf ein Textbuch-Adventure (Interactive Fiction) aus 1988 zurück. Bis in die frühen 2000er werden diverse auf dem Konzept aufbauende "Point-And-Click" Videospiele und digitale Text-Adventures veröffentlicht, die den Begriff "Escape-Room" / "Escape Game" zunächst prägen. In dieser Zeit werden die ersten direkt von diesen Spielen inspirierten, physischen Räume konstruiert, die auch für mehrere Spieler gedacht sind. Ab 2015 wächst das Interesse der breiteren Öffentlichkeit in das Genre "Escape Room", das sich vornehmlich auf die physischen Räume bezieht, nachdem kurze Zeit vorher in China das artverwandte "Jubensha" an Popularität gewonnen hat (Murder-Mystery-Rollenspiel im Team).

Im Kontext von Videospielen sind 'Escape-Rooms' ein Subgenre von Point-And-Click-Adventures oder Text-Adventures, die prinzipiell alleine und ohne Zeitdruck gespielt werden. Die Ästhetik dort ist nicht selten eine "zwanghafte Neugier", die von Befremdlichkeit oder auch Horror genährt wird. In der Öffentlichkeit sind "Escape Rooms" gemeinschaftliche Aktivitäten in gemieteten Räume
n mit einem eher durchgeplanten Spielverlauf und einem (zunächst der Vermarktung geschuldeten) Zeitlimit.

Die Anwendung des Konzeptes im Rahmen dieses Projektes wird im [Konzept-Dokument](concept.md) ausgeführt.

# Geschichte

Die Geschichte ist das, was die Spieler bei dem Durchlauf eines Escape-Rooms erleben.

## Geschehnis

Geschehnisse sind Zustandsänderungen der Geschichte, deren Zeitpunkt und Ablauf von den Lehrenden vorgegeben ist.

> Bsp.: Ein Dschinn erscheint 30 Sekunden nach Spielstart und verlangt von den Charakteren die Preisgabe ihrer Namen.

## Ereignis

Ereignisse werden von Spielern initiiert bringen die Handlung der Geschichte/die Beendigung des Escape-Rooms voran.

TODO "geschehnis" und "ereignis" in anlehnung an die erzähltheorie von lahn und meister. das ereignis ist wichtigere. geschehnisse sind nicht wesentlich.

### (Lern-) Rätsel

Rätsel sind Ereignisse, die auf die Erreichung von Lernzielen zusteuern. Lernziele sind z.B. Inhalte von Lehrveranstaltungen oder Soft-Skills, wie logisches Denken, Problemlösungsstrategien, Kommunikation in der Gruppe und Zusammenarbeit.

TODO wollen wir Rätsel für Soft-Skills namentlich trennen?
TODO rian hat auch den begriff "aufgabe" eingeworfen, die die charaktere erhalten. sollte der umstand der aufforderung von spieler/charakter hier hervorgehoben werden?

### Aktivität

Aktivitäten sind Ereignisse, die keine Rätsel sind (und nicht direkt ein Lernziel verfolgen). Eine lehrende Person kann Aktivitäten hinzufügen, um z.B. Interaktionen mit dem Escape-Room vorzustellen, die Erzählung der Geschichte zu verbessern oder auf bestimmte Lerntypen ausgerichtete Optionen anzubieten.

> Bsp.:
> - Die Spielercharaktere geben dem Dschinn ihre Namen.
> - Die Charaktere tauschen ihre Meinungen von dem Dschinn in einem Gespräch am Lagerfeuer aus.

## Mechanik

Ereignisse und Geschehnisse werden durch eine Mechanik implementiert. Die Mechanik bestimmt die Art und Weise mit der die Spieler mit der Geschichte, mit dem Escape-Room oder miteinander interagieren.

> Bsp.:
> Kombinationsrätsel
> Suche
> Logikaufgabe

### Interaktion

Mechaniken werden durch eine nicht-leere Menge von Interaktionen definiert, deren Abhängigkeiten lehrende Personen festlegen können.
Manche Interaktionen werden von den Spielern ausgelöst, andere von der Simulation.


TODO Diese Interaktionen müssen vom Dungeon implementiert und unterstützt werden.

> Bsp.:
> - Ein Charakter zieht einen Hebel
> - Der Dschinn belehrt die Spieler über die Folgen ihrer Handlungen.
