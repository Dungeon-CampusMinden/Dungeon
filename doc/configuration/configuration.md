---
title: Configuration
---

## Intro

Verwaltende Klasse für die ConfigKeys. Verantwortlich für das Laden und Speichern der Konfiguration.

## How to use

```java
import configuration.Configuration;

public class ExampleClass {
    public void exampleMethod() {
        Configuration config = Configuration.loadAndGetConfiguration("config.json", ConfigMapA.class, ConfigMapB.class);
        config.saveConfiguration();
    }
}
```

- `Configuration.loadAndGetConfiguration()`
  lädt die angegebene Konfigurationsdatei und läd ihre Inhalte in die in ConfigMapA und ConfigMapB definierten [ConfigKeys\<T>](./ConfigKey.md).

- `Configuration.saveConfiguration()` speichert die Konfiguration in die beim Laden angegebene Datei.
