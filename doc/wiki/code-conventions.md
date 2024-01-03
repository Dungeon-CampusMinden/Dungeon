---
title: "Code-Codvention"
---

## Code-Style

Als Code-Style verwenden wir den Google AOSP-Style. Spotless überprüft dabei die Einhaltung der Formatierung für uns. Siehe [hier](https://github.com/diffplug/spotless/tree/main/plugin-gradle#google-java-format).

Alle Java-Dateien eines Pull-Requests müssen im AOSP-Style formatiert sein. Nicht-formatierte Pull-Request können nicht gemerged werden.

Sie können alle Java-Dateien mit dem Aufruf `./gradlew :spotlessApply` formatieren. Alternativ können Sie das `google-java-format`-Plugin in IntelliJ installieren.

## Das `google-java-format`-Plugin in IntelliJ installieren

1.  Installieren Sie das Plugin `google-java-format` in IntelliJ
2.  Aktivieren Sie das Plugin:
    - Stellen Sie folgende Einstellungen in den `google-java-format`-Settings manuell ein:
        - Enable
        - Code Style: AOSP
    - Alternativ fügen Sie die folgende Konfigurationsdatei in `.idea/google-java-format.xml` hinzu:

        ```xml
        <?xml version="1.0" encoding="UTF-8"?>
        <project version="4">
            <component name="GoogleJavaFormatSettings">
                <option name="enabled" value="true"/>
                <option name="style" value="AOSP"/>
            </component>
        </project>
        ```
