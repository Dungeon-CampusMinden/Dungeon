---
title: "Anforderungen an das Tooling"
---

Dieses Dokument soll im weiteren Verlauf des Projektes mit konkreten Anforderungen an das Tooling für Lehrende gefüllt werden.

# Welche Erwartungen werden an den Prozess gestellt?

Lehrende interagieren voraussichtlich zur Erfüllung der folgenden Zwecke mit dem System:

- Erstellung eines Escape-Rooms anhand des Lerninhalts
- Bereitstellung des Escape-Rooms für die Lernenden
- evtl. Einsicht in Metriken

## Erstellung eines Escape-Rooms

Lehrende starten vermutlich in erster Linie mit einem Lerninhalt. Die Findung der Geschichte und geeigneter Rätsel ist ein [komplexes Problem](https://de.wikipedia.org/wiki/Komplexes_Problem), das sich nicht allein mit Software sinnvoll lösen lassen wird. Eine einfache, aber beschränkte Lösung ist die Vorauswahl von vorgefertigten Schablonen. Alternativ oder zusätzlich sollte es einen Prozess geben, der die Erstellung von effektiven Escape-Rooms begünstigt. Ziele und Vorgehen sollten dem [Vorgehen](./research_design.md#approach) entsprechen und Erkenntnisse über bspw. Belohnungssysteme oder Lernmechanismen berücksichtigen.

# Dependency Graph

We want to be able to make sure that players are able to reach the goal from every state. At the same time, there should be some level of redundancy in the ways of obtaining the goal. Dependency charts are a good way to achieve this. The dungeon's DSL also offers options to achieve something very similar ([dsl example](../../dungeon/doc/dsl/examplescripts/quickstart_task_dependency.dng)), though there seems to be no abstract type for actions.

See [research on tooling](research_solutions_other.md#tooling) for existing solutions.
