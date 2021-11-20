package controller;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class LevelController {
    private SpriteBatch batch;
    private GraphicController graphicController;

    public LevelController(SpriteBatch batch, GraphicController graphicController) {
        this.batch = batch;
        this.graphicController = graphicController;
    }

    public void update() {}
}
