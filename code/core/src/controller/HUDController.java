package controller;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.HUDCamera;
import interfaces.IHUDElement;

import java.util.HashSet;
import java.util.Set;

/** Keeps a list of Hud elements and makes sure they are drawn */
public class HUDController {
    private GraphicController graphicController;
    private HUDCamera hudCamera;
    private SpriteBatch batch;
    private Set<IHUDElement> elements;

    /**
     * Keeps a list of Hud elements and makes sure they are drawn
     *
     * @param batch batch for the hud
     * @param graphicController
     */
    public HUDController(SpriteBatch batch, GraphicController graphicController, HUDCamera camera) {
        this.batch = batch;
        hudCamera = camera;
        hudCamera.getPosition().set(0, 0, 0);
        hudCamera.update();
        elements = new HashSet<>();
        this.graphicController = graphicController;
    }

    /**
     * Adds an element to the HUD
     *
     * @param element
     */
    public void addElement(IHUDElement element) {
        elements.add(element);
    }

    /**
     * Removes an element from the HUD
     *
     * @param element
     */
    public void removeElement(IHUDElement element) {
        elements.remove(element);
    }

    public void clearHUD() {
        this.elements.clear();
    }

    /** @return List with all the elements on the hud */
    public Set getElements() {
        return elements;
    }

    /** redraw hud and hud elements */
    public void update() {
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        drawElements();
    }

    private void drawElements() {
        elements.forEach(
                element ->
                        graphicController.draw(element.getTexture(), element.getPosition(), batch));
    }
}
