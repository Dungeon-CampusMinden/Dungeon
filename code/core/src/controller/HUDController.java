package controller;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import graphic.HUDCamera;
import interfaces.IHUDElement;

public class HUDController extends AbstractController<IHUDElement> {
    private final HUDCamera hudCamera;
    private final SpriteBatch batch;

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
    }

    /** Redraws the HUD and all HUD elements. */
    @Override
    public void update() {
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        forEach(element -> element.draw(batch));
    }
}
