package controller;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.HUDCamera;
import interfaces.IHUDElement;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class HUDController implements IController<IHUDElement> {
    private final HUDCamera hudCamera;
    private final SpriteBatch batch;
    private final Set<IHUDElement> elements;

    /**
     * Keeps a set of HUD elements and makes sure they are drawn.
     *
     * @param batch the batch for the HUD
     */
    public HUDController(SpriteBatch batch, HUDCamera camera) {
        this.batch = batch;
        hudCamera = camera;
        hudCamera.getPosition().set(0, 0, 0);
        hudCamera.update();
        elements = new LinkedHashSet<>();
    }

    /** Registers an element to the HUD. */
    @Override
    public void add(IHUDElement element) {
        elements.add(element);
    }

    /** Returns <code>true</code> if the element is registered. */
    @Override
    public boolean contains(IHUDElement element) {
        return elements.contains(element);
    }

    /** Removes an element from the HUD. */
    @Override
    public void remove(IHUDElement element) {
        elements.remove(element);
    }

    /** Clears all HUD elements. */
    @Override
    public void removeAll() {
        elements.clear();
    }

    /** Returns a copy set with all elements on the HUD. */
    @Override
    public Set<IHUDElement> getSet() {
        return new LinkedHashSet<>(elements);
    }

    /** Returns a copy list with all elements on the HUD. */
    @Override
    public List<IHUDElement> getList() {
        return new ArrayList<>(elements);
    }

    /** Redraws the HUD and all HUD elements. */
    @Override
    public void update() {
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        drawElements();
    }

    private void drawElements() {
        elements.forEach(element -> element.draw(batch));
    }
}
