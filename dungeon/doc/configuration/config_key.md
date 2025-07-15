---
title: Configuration
---

## Intro

Verknüpfung zwischen einem Schlüssel und einem [ConfigValue\<T>](./ConfigValue.md). Wird von der Klasse [Configuration](./Configuration.md) verwendet und verwaltet.

## Einen neuen Config Schlüssel erstellen

1. Erstellen einer neuen ConfigMap-Klasse, optional mit der Annotation `@ConfigMap` und der gewünschten Prefix.
2. Erstellen einer neuen `ConfigKey`-Instanz mit dem gewünschten Schlüssel und dem gewünschten [ConfigValue\<T>](./ConfigValue.md).
3. Die `ConfigKey`-Instanz muss in der ConfigMap-Klasse als `public static`-Feld deklariert werden.
4. Die ConfigMap-Klasse muss in der `Configuration.loadAndGetConfiguration()`-Methode übergeben werden, damit die Felder der ConfigMap-Klasse initialisiert werden.

## Konstruktor

1. `ConfigKey(String key, ConfigValue<T> value)`
    - `key`: Der Schlüssel des ConfigKeys als String. Dabei wird der Pfad an Punkten getrennt.
    - `value`: Das ConfigValue des ConfigKeys
2. `ConfigKey(String[] key, ConfigValue<T> value)`
    - `key`: Der Schlüssel des ConfigKeys als String-Array. Jeder Eintrag des Arrays ist ein Teil des Pfades.
    - `value`: Das ConfigValue des ConfigKeys

## Beispiele

```java
package example;

import com.badlogic.gdx.Input;
import configuration.ConfigMap;
import configuration.ConfigKey;
import configuration.values.ConfigIntValue;
import configuration.values.ConfigBooleanValue;

@ConfigMap(prefix= {"settings", "graphics"})
public class GraphicsConfigMap {

    public static ConfigKey<Boolean> FULLSCREEN = new ConfigKey<>(new String[] {"fullscreen"}, new ConfigBooleanValue(false));
    public static ConfigKey<Integer> RESOLUTION_X = new ConfigKey<>(new String[] {"resolution", "x"}, new ConfigIntValue(1920));
    public static ConfigKey<Integer> RESOLUTION_Y = new ConfigKey<>(new String[] {"resolution", "y"}, new ConfigIntValue(1080));

}
```
```java
package example;

import com.badlogic.gdx.Input;
import configuration.ConfigMap;
import configuration.ConfigKey;

@ConfigMap(prefix= {"settings", "keyboard"})
public class KeyboardConfigMap {

    public static ConfigKey<Integer> INTERACT_EXAMPLE = new ConfigKey<>("interact", new ConfigIntValueValue(Input.Keys.E));
    public static ConfigKey<Integer> INTERACT_EXAMPLE = new ConfigKey<>("inventory.open", new ConfigIntValueValue(Input.Keys.I));
    public static ConfigKey<Integer> INTERACT_EXAMPLE = new ConfigKey<>("menu.open", new ConfigIntValueValue(Input.Keys.ESC));

}
```

Wenn beide ConfigMaps via [`Configuration.loadAndGetConfiguration()`](./Configuration.md) geladen werden, werden die Felder der ConfigMaps mit den entsprechenden Werten aus der Konfigurationsdatei initialisiert.
Sollte die Konfigurationsdatei nicht existieren, werden die Felder mit dem Standardwert aus der ConfigValue initialisiert und gespeichert werden.
Die produzierte Konfigurationsdatei würde so, oder ähnlich aussehen:

```json
{
    "settings": {
        "graphics": {
            "fullscreen": "false",
            "resolution": {
                "x": "1920",
                "y": "1080"
            }
        },
        "keyboard": {
            "interact": "69",
            "inventory": {
                "open": "73"
            },
            "menu": {
                "open": "27"
            }
        }
    }
}
```

