---
title: "Ziele und Zustand der aktuellen DungeonDSL Implementierung"
author: @malt-r
---

Dieses Dokument hält den aktuellen Zustand der DungeonDSL-Implementierung, also alle geplanten Features und deren aktueller Realisierungsstand.

Mögliche Zustände:
- geplant, kein klares Konzept vorhanden: 💭
- geplant, Konzept vorhanden: 💡
- implementiert, nicht getestet ☑
- implementiert, getestet: ✅

| Feature | Was und warum? | Zustand |
|-|-|-|
| **High Level Konzepte**| | |
| Taskdefinition | Die zentrale Definition für Aufgaben im Dungeon und wird vermutlich für die meisten Lehrpersonen das sein, was sie am häufigsten erstellen und verwenden |💡 |
| Level-Konfig | Die primäre Verbindung zwischen der DungeonDSL-Datei und dem Dungeon; konfiguriert, welche Aufgaben in einem Level dargestellt werden | 💡|
| Task-Organisation | Bietet die Möglichkeit, mehrere Aufgaben in Beziehung zu setzen, um sequentielle und parallele Aufgaben und Aufgabenverschachtelung zu realisieren; wird höchstwahrscheinlich auf Petri-Netzen basieren| 💡|
| Level-Organisation | Ein ähnliches Konzept wie für Task-Organisation, allerdings auf Level-Ebene; hiermit soll konfiguriert werden, welche Aufgaben / Level geladen werden, wenn ein Level abgeschlossen wurde| 💡|
| Entitätstyp-Definition | Definition von Entitätstypen (benannte Zusammenstellung aus mehreren Komponenten) | ✅ (als `game_object` Definition) |
| Entitätstyp-Konfiguration | Anpassung der Werte der Komponenten einer Entitätstyp-Definition, um aus einem Entitätstypen konkrete Ausprägungen zu erstellen. Es ist bereits möglich, aus `game_object` Definitionen konkrete Instanzen im Dungeon zu erzeugen, die für alle nicht vorgegebenen Werte in den Komponenten die Default-Werte verwenden | 💡|
| Event-Handler Funktion | Bietet die Möglichkeit, auf bestimmte Events aus dem Dungeon-Kontext oder dem Kontext einer einzelnen Entität zu reagieren. Wichtig, um (aufgabenbezogenes) Verhalten zu definieren, was von dem Default-Verhalten abweicht. | 💡|
| Task-Builder Methode | Methoden, die vom Dungeon aufgerufen werden, um eine Taskdefinition in ein konkretes Szenario zu übersetzen. Erzeugen eine Menge Entitäten, definieren und verknüpfen deren Event-Handler Methoden und geben sie an den Dungeon zurück| 💡|
| Bewertungskonfiguration | Die Bewertung einer Aufgabe soll über die DSL konfigurierbar sein, um bspw. festzulegen, wann und welche Daten als Antwort für eine Aufgabe geloggt werden, wie Fehlversuche in die Bewertung eingehen, etc. | 💭|
| **Sprachkonzepte**| | |
| Property-Bag für Entitätsdefinitionen|  | 💡|
| Funktionsdefinitionen || ☑|
| Funktionsaufrufe || ✅|
| Handling von Funktions-Rückgabewert || 💡|
| Function-as-value || 💭|
| Variablendefinitionen || 💡|
| Objektdefinitionen || ✅|
| Inline Objektdefinitionen || ☑|
| Single-Argument Type Adapter || 💡|
| Aggregate Type Adapter || ☑|
| Graphdefinition per dot || ✅|
| Attributierung von dot Knoten und Kanten | | 💭|
| Petri-Netz per dot || 💭|
| Objekt-Kappselung (Object encapsulation) || ☑|
| Include-Mechanismus für Entitätsdefinitionen || 💭|
| Include-Mechanismus für Funktionsdefinitionen || 💭|
| Enum-Variant Binding || 💭|
| Arrays || 💭|
| Kontrollflussmechanismen || 💭|
| **DungeonDSL "Ökosystem"**|| |
| Typechecking || 💡|
| Error-Handling/-Recovery || 💭|
| Error-Messages || 💭|
| Funktionsschnittstelle zum Dungeon || 💡|
