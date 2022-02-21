package controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import interfaces.IHUDElement;

public class HUDController extends AbstractController<IHUDElement> {
    private final Stage textStage;

    /**
     * Keeps a set of HUD elements and makes sure they are drawn.
     *
     * @param batch the batch for the HUD
     */
    public HUDController(SpriteBatch batch) {
        textStage = new Stage(new ScreenViewport(), batch);
    }

    /** Redraws the HUD and all HUD elements. */
    @Override
    public void update() {
        removeIf(IHUDElement::removable);
        forEach(IHUDElement::update);

        textStage.act();
        textStage.draw();
    }

    /**
     * Draws a given text on the screen.
     *
     * @param text text to draw
     * @param fontPath font to use
     * @param color color to use
     * @param size font size to use
     * @param width width of the text box
     * @param height height of the text box
     * @param x x-position in pixel
     * @param y y-position in pixel
     * @param borderWidth borderWidth for the text
     * @return Label (use this to alter text or remove the text later)
     */
    public Label drawText(
            String text,
            String fontPath,
            Color color,
            int size,
            int width,
            int height,
            int x,
            int y,
            int borderWidth) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(fontPath));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.borderWidth = borderWidth;
        parameter.color = color;
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = generator.generateFont(parameter);
        generator.dispose();
        Label label = new Label(text, labelStyle);
        label.setSize(width, height);
        label.setPosition(x, y);

        textStage.addActor(label);
        return label;
    }

    /**
     * Draws a given text on the screen.
     *
     * @param text text to draw
     * @param fontPath font to use
     * @param color color to use
     * @param size font size to use
     * @param width width of the text box
     * @param height height of the text box
     * @param x x-position in pixel
     * @param y y-position in pixel
     * @return Label (use this to alter text or remove the text later)
     */
    public Label drawText(
            String text,
            String fontPath,
            Color color,
            int size,
            int width,
            int height,
            int x,
            int y) {
        return drawText(text, fontPath, color, size, width, height, x, y, 1);
    }
}
