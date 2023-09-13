---
title: "Strategy Pattern im ECS"
---

Das [Strategie-Pattern](https://en.wikipedia.org/wiki/Strategy_pattern) hat sich als besonders hilfreich in Komponenten erwiesen, die eigene Logiken implementieren müssen, obwohl es nicht streng im Sinne des ECS-Paradigmas ist.

Zukünftig sollen die in der DSL geschriebenen Funktionen als mögliche Strategien repräsentiert werden.

## Umsetzung

Eine Strategie wird über ein funktionales Interface umgesetzt. Das `Component` speichert eine Referenz auf eine konkrete Implementierung in Form eines Lambda-Ausdrucks, einer Methodenreferenz oder einer Instanz einer implementierenden Klasse. Über diese Referenz kann die konkrete Strategie ausgeführt werden.

Durch die `void execute()`-Methode einer Komponente wird die Referenz auf die konkrete Strategie aufgerufen. Die `#execute`-Methode kann dann vom zuständigen System aufgerufen werden.