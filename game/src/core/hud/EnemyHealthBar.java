package core.hud;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;

import contrib.components.HealthComponent;

import core.Entity;
import core.components.PositionComponent;
import core.systems.CameraSystem;
import core.utils.Constants;
import core.utils.Point;

public class EnemyHealthBar extends ProgressBar {

    private final Point POSITION_OFFSET = new Point(-29, 12);
    private final Entity entity;

    private record EnemyData(HealthComponent hc, PositionComponent pc) {}

    /**
     * Creates a new HealthBar for the given entity
     *
     * @param entity the entity to create the HealthBar for
     */
    public EnemyHealthBar(Entity entity) {
        super(0, 100, 1, false, Constants.enemyHealthBarSkin);
        this.entity = entity;
        this.setAnimateDuration(0.1f);
        EnemyData ed = buildDataObject();
        Point position = ed.pc.getPosition();
        Vector3 screenPosition =
                CameraSystem.camera().project(new Vector3(position.x, position.y, 0));
        this.setBounds(
                screenPosition.x + POSITION_OFFSET.x, screenPosition.y + POSITION_OFFSET.y, 35, 10);
        HeroUI.getHeroUI().add(this);
    }

    /**
     * Updates an entity's HealthBar, sets the position and value every frame. Removes the HealthBar
     * if entity has no health left.
     */
    public void update() {
        EnemyData ed = buildDataObject();
        if (ed.hc.getCurrentHealthpoints() <= 0) HeroUI.getHeroUI().remove(this);
        // set visible only if entity lost health
        this.setVisible(ed.hc.getCurrentHealthpoints() != ed.hc.getMaximalHealthpoints());
        Point position = ed.pc.getPosition();
        Vector3 screenPosition =
                CameraSystem.camera().project(new Vector3(position.x, position.y, 0));
        this.setPosition(
                screenPosition.x + POSITION_OFFSET.x, screenPosition.y + POSITION_OFFSET.y);

        // set value to health percent
        this.setValue(
                (float) ed.hc.getCurrentHealthpoints() / ed.hc.getMaximalHealthpoints() * 100);
    }

    private EnemyData buildDataObject() {
        return new EnemyData(
                (HealthComponent) entity.getComponent(HealthComponent.class).orElseThrow(),
                (PositionComponent) entity.getComponent(PositionComponent.class).orElseThrow());
    }
}
