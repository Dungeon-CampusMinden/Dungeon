## Informations Dialog für GUI
Zur Darstellung der Inhalte für das Spiel wird mithilfe der Widgets aus der `libgdx` und des  `scene2d.ui`-Pakets ein Dialog erstellt. Der Dialog wurde dafür erstellt um Inhalte auf dem Spielbildschirm anzeigen zu können, sowie das Spiel und alle seine Systeme, ausgenommen von `DrawSystem` pausieren zu können. Mit dem Klick auf den Button „Ok“ kann das Spiel fortgesetzt werden indem alle Systeme wieder aktiviert werden.

![DialogWindow](/img/dialogWindow.png)

### Wichtige Klassen die verwendet werden
`TextDialog`
Enthällt Constructor, welcher unmittelbar den Dialog inklusive aller seiner Elemente erstellt .
```java
public final class TextDialog extends Dialog {
    protected void result(final Object object) {
        if (object.toString() == btnID) {
            enable = false;
            UITools.deleteDialogue();
        }
    }
}
```
Wenn der einzige Button des Dialoges (z.B „Ok“) gedrückt wird, wird der Dialog vom Stage genommen und alle Systeme neu gestartet. Das Dialog- Objekt wird entsorgt.

`ResponsiveDialogue`
Erstellt ein Dialog- Objekt, formatiert und gibt den Dialog an den `ScreenController` weiter, damit der Dialog auf dem Bildschirm angezeigt werden kann.
`UITools`
Formatierung der Hauptmeldung (Inhalt der im Label angezeigt wird) und steuert die Erstellung eines  Dialog- Objektes abhängig von einem Ereigniss.
Siehe Funktion:
 ``` java
 public static void showInfoText(String... arrayOfMessages) {
```
Der Funktion `showInfoText` kann ein beliebig langer Textinhalt sowie die Benennung für einen Button (optional) und ein Dialog (Dialogtitel) (optional)  übergeben werden, sonst erfolgt die Verwendung der Default- Werte.


### Was sind Widgets?
Das scene2d.ui-Paket von libgdx bietet allgemeine Benutzeroberflächen-Widgets und andere Klassen, um  Benutzeroberflächen erstellen zu können.
Widgets sind Komponenten, die in ein graphischen Fenstersystems eingebunden sind, welches diese zur Interaktion mit dem Anwender oder anderen Widgets des Fenstersystems nutzen. Dazu zählen zum Beispiel: Dialog, Label, Button, Slider, CheckBox etc.

### Was ist ein Dialog?
Dialog ist ein Fenster mit einer Inhaltstabelle und einer darunter liegenden Schaltflächentabelle. Dem Dialog kann alles hinzugefügt werden, aber es werden bequeme Methoden bereitgestellt, um der Inhaltstabelle eine Beschriftung und der Schaltflächentabelle Schaltflächen hinzuzufügen.
Die Größe und Position der untergeordneten UI-Widgets wird durch das übergeordnete Widget festgelegt.
Bei den meisten  scene2d.ui-Layouts wird eine Tabelle verwendet, welche dem Stage entspricht. Alle anderen Widgets werden in den Zeilen einer dieser Tabelle platziert und können so innerhalb des Dialoges an bestimmten Positionen dargestellt werden.

```java
public final class TextDialog extends Dialog {
    public TextDialog(Skin skin, String outputMsg, String buttonMsg, String title) {

        Label labelContent = new Label(outputMsg, skin);
        labelContent.setAlignment(Align.left);
        labelContent.setColor(Color.WHITE);

        Table scrollTable = new Table();
        scrollTable.add(labelContent);
        scrollTable.row();
    }
}
```

### Skin
libGDX besitzt die Klasse `Skins`, diese kann zur Speicherung  von Bildressourcen (Texturen, Schriftarten, etc.)  verwendet werden, um damit das Aussehen der UI-Elemente definieren zu können. Neben der Bereitstellung der Texturbereiche, welche von den Widgets benötigt werden, können auch UI-Widget-Stile gespeichert werden, die später das Aussehen der Widgets bestimmen können. Die Ressourcen (Farben, Bilder, Schriftart), welche die Widgets benötigen, bezeichnet man als „Stil“. Jedes Widget definiert eine eigene statische Mitgliedsklasse mit Konstruktoren zum setzen von Basis Styles, welche durch die setStyle Methode zur Stilveränderung verwendet werden können.
Zur Darstellung und Formatierung der einzelnen Widgets können auch Skin-Dateien von libGDX verwendet werden.
ACHTUNG: Die default- Skins von libGDX können ausschließlich nur zu Testzwecken verwendet werden und dürfen nicht in das Projekt eingefügt werden.
Die Skins können unter folgendem Link gefunden werden.
https://github.com/libgdx/libgdx/tree/master/extensions/gdx-tools/assets
Im Package `assets` gibt es ein  Package `skin` dort können folgende Skin Dateien eingefügt werden:
`default.fnt`
Formatierung des Schriftzuges. Durch das Programm `Hiero` können eigene Schriftzüge mit Größe und Art dargestellt werden. Es wird automatisch eine `fnt`-Datei generiert, welche der Struktur der `default.png`entspricht.
`default.png`
Abbildung des Schriftzuges.
`uiskin.png`
Es handelt sich um um eine Vielzahl von Widget Elementen sowie dem verwendeten Schriftzug. Der Vorteil der Kombination all dieser Elemente ist, dass weniger Dateien geladen werden müssen, was die Leistung des Spiels verbessert.
`uiskin.atlas`
Die Atlas-Datei ist ein Mapping aller unserer Bilder, die zusammen in unserem uiskin.png gepackt wurden.
`uiskin.json`
Die JSON-Datei wird verwendet , um eine schnelle und einfache Möglichkeit zu schaffen die Assets zu verwenden ohne, das diese  manuell mit den spezifischen Stilen verbunden werden müssen.

In der Klasse `Constants` wird der Pfad der json-Datei angegeben, damit die Widgets entsprechend Dargestellt werden können.
```java
public final class Constants {
    /** set Path to libgdx default Skins */
    public static final String SKIN_FOR_DIALOG = "skin/uiskin.json";
}
```
In der Klasse `UITools` erfolgt die Erstellung des Dialogs welche die Skins aus der  `uiskin.json` verwendet. Der Inhalt des Dialoges wird mit weißer Schriftfarbe dargestellt (Text, Button Beschriftungen und Label).

```java
public class UITools {
    private static void generateDialogue(String... arrayOfMessages) {
        if (dialog != null) return;

        dialog =
                new ResponsiveDialogue(
                        new Skin(Gdx.files.internal(Constants.SKIN_FOR_DIALOG)),
                        Color.WHITE,
                        arrayOfMessages);

        if (controller != null) controller.add(dialog);

        if (systems != null) {
            for (ECS_System system : systems) {
                system.notRunExceptSystems(exceptSystemName);
            }
        }
    }
}
```




### Welche Möglichkeiten gibt es ein Skin zu erstellen?
ToDo: herausfinden wie man eigene Skins ertellen kann






