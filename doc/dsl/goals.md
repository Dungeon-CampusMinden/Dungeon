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
| Taskdefinition |  |💡 |
| Level-Konfig | | 💡|
| Task-Organisation | | 💡|
| Entitätstyp-Definition | | ✅|
| Entitätstyp-Konfiguration | | 💡|
| Event-Handler Funktion | | 💡|
| Task-Builder Methode | | 💡|
| Bewertungskonfiguration | | 💭|
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
| **DungeonDSL "Ökosystem"**|| |
| Typechecking || 💡|
| Error-Handling/-Recovery || 💭|
| Error-Messages || 💭|
| Funktionsschnittstelle zum Dungeon || 💡|
