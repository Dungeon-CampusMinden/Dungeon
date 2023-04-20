---
title: "HUD"
---

WIP, Issue #529

Der HUD ist Teil einer visuellen Benutzeroberfläche eines Spiels, welcher dazu verwendet werden kann den Spieler während
des Spiels mit wichtigen Informationen zu versorgen. Die Darstellung erfolgt als Overlay, welcher die Informationen als
2D -Text oder als Symbole (z.B Lebensanzeige in Herzform) auf dem Bildschirm über der Spieleszene anzeigt. Bevor man mit
der HUD arbeiten kann, ist es erforderlich einen `ScreenController` anzulegen, welcher zur Darstellung und Verwaltung
von UI-Elementen verwendet wird. Für dessen Erstellung wird eine `Batch` benötigt, welche bereits im Spiel integriert
ist und wird verwendet, um Objekte auf dem Bildschirm darstellen zu können. Zur HUD Darstellung und der Event
bearbeitung ist es zudem erforderlich den neu erstellten `ScreenController` den anderen Controllern hinzuzufügen. Die
Konstanten `Constants.WINDOW_WIDTH` und `Constants.WINDOW_HEIGHT` entsprechen einer festen Displaygröße, bei veränderung
des Displays werden alle Elemente durch den `ScreenController` entsprechend ausgerichtet.

``` java
package controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import tools.Constants;

public class ScreenController<T extends Actor> extends AbstractController<T> {

public ScreenController(SpriteBatch batch) {
    super();
    stage = new Stage(
        new ScalingViewport(Scaling.stretch, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT), batch);

        Gdx.input.setInputProcessor(stage);
    }
...
}
```

## UI Elemente

### ScreenImage

Kann verwendet werden, um ein nicht bewegliches Bild an einer bestimmten Position auf dem Bildschirm auszugeben und zu
konfigurieren. Bei der Zeichenfläche kann es sich entweder eine Textur, Texturregion, Ninepatch etc. handeln und kann
innerhalb der Grenzen des Bild-Widgets auf verschiedene Weise skaliert und ausgerichtet werden.

``` java
public class ScreenImage extends Image {

    /**
     * Creates an Image for the UI
     *
     * @param texturePath the Path to the Texture
     * @param position the Position where the Image should be drawn
     */
    public ScreenImage(String texturePath, Point position) {
        super(new Texture(texturePath));
        this.setPosition(position.x, position.y);
        this.setScale(1 / Constants.DEFAULT_ZOOM_FACTOR);
    }
}
```

### ScreenText

Wird verwendet um Texte an einer bestimmten Stelle durch `setPosition()` des Bildschirms ausgegeben und ermöglicht zudem
eine dynamische Konfiguration des Standardstils für den generierten Text.

``` java
package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import tools.Point;

public class ScreenText extends Label {
    /** Allows the dynamic configuration of the default style for the generated ScreenTexts */
    public static final LabelStyle DEFAULT_LABEL_STYLE;

    static {
        DEFAULT_LABEL_STYLE =
                new LabelStyleBuilder(FontBuilder.DEFAULT_FONT).setFontcolor(Color.BLUE).build();
    }

    /**
     * Creates a Text with the default label style at the given position.
     * @param text the text which should be written
     * @param position the Point where the ScreenText should be written 0,0 bottom left
     * @param scaleXY the scale for the ScreenText
     * @param style the style
     */
    public ScreenText(String text, Point position, float scaleXY, LabelStyle style) {
        super(text, style);
        this.setPosition(position.x, position.y);
        this.setScale(scaleXY);
    }

    /**
     * Creates the ScreenText with the default style.
     * @param text the text which should be written
     * @param position the position for the ScreenText 0,0 bottom left
     * @param scaleXY the scale for the ScreenText
     */
    public ScreenText(String text, Point position, float scaleXY) {
        this(text, position, scaleXY, DEFAULT_LABEL_STYLE);
    }
}
```

### ScreenButton

Beim Button handelt es sich um eine leere, erweiterbare Schaltfläche, welche einen checked"-Status besitzt und in
Abhängigkeit, ob der Button angeklickt oder nicht angeklickt wurde, den unteren- oder den oberen Hintergrund anzeigt.
Der Button kann außerdem durch eine Beschriftung in einer Bitmap-Schriftart und beliebiger Textfarbe dargestellt werden,
wobei sich die Textfarbe des Buttons in Abhängigkeit der Zustände verändern kann. Auch das Erweitern des Buttons durch
ein Bild-Widget ist ebenfalls möglich, welches sich in Abhängigkeit der Zustände gecklickt oder nicht geklickt verändern
kann. Größe und Ausrichtung des Elementes können durch folgende Funktionen `setScale()` sowie `setPosition()` verändert
werden.

``` java
package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import tools.Constants;
import tools.Point;

public class ScreenButton extends TextButton {
    private static final TextButtonStyle DEFAULT_BUTTON_STYLE;

    static {
        DEFAULT_BUTTON_STYLE =
                new TextButtonStyleBuilder(FontBuilder.DEFAULT_FONT)
                        .setFontColor(Color.BLUE)
                        .setDownFontColor(Color.YELLOW)
                        .build();
    }

    /**
     * Creates a ScreenButton which can be used with the ScreenController.
     * @param text the text for the ScreenButton
     * @param position the Position where the ScreenButton should be placed 0,0 is bottom left
     * @param listener the TextButtonListener which handles the button press
     * @param style the TextButtonStyle to use
     */
    public ScreenButton(
            String text, Point position, TextButtonListener listener, TextButtonStyle style) {
        super(text, style);
        this.setPosition(position.x, position.y);
        this.addListener(listener);
        this.setScale(1 / Constants.DEFAULT_ZOOM_FACTOR);
    }

    /**
     * Creates a ScreenButton which can be used with the ScreenController.
     * <p>Uses the DEFAULT_BUTTON_STYLE
     * @param text the text for the ScreenButton
     * @param position the Position where the ScreenButton should be placed 0,0 is bottom left
     * @param listener the TextButtonListener which handles the button press
     */
    public ScreenButton(String text, Point position, TextButtonListener listener) {
        this(text, position, listener, DEFAULT_BUTTON_STYLE);
    }
}
```
