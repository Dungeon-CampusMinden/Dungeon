package controller;

import basiselements.DungeonElement;
import graphic.Painter;

/**
 * A class to manage <code>DungeonElement</code>s.
 *
 * <p>On each <code>DungeonElement</code> the update and draw method be are called.
 */
public class EntityController extends AbstractController<DungeonElement> {
    Painter painter;

    public EntityController(Painter painter) {
        super();
        this.painter = painter;
    }

    /**
     * Updates all elements that are registered at this controller, removes deletable elements and
     * calls the update and draw method for every registered element.
     */
    @Override
    public void process(DungeonElement e) {
        e.update();
        e.draw(painter);
    }
}
