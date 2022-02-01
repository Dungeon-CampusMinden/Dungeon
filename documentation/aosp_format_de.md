Ihre Commits müssen im "AOSP"-Style (google) formatiert sein.

Um alle `.java`-Dateien zu formatieren, gibt es unter anderem zwei Möglichkeiten:

- Gradle-Task: `gradlew spotlessApply`
- Installieren Sie das `google-java-format`-Plugin für IntelliJ und stellen Sie dieses auf den "AOSP"-Style ein.<sup>1</sup>

Um die Formatierung zu prüfen, können Sie den Gradle-Task `gradlew spotlessJavaCheck` aufrufen.

<sup>1</sup>: Sie können folgende Konfigurationsdatei in `.idea/google-java-format.xml` hinzufügen:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project version="4">
    <component name="GoogleJavaFormatSettings">
        <option name="enabled" value="true"/>
        <option name="style" value="AOSP"/>
    </component>
</project>
```
