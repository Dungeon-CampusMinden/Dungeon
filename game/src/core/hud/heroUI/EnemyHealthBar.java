package core.hud.heroUI;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import contrib.components.HealthComponent;

import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.systems.CameraSystem;
import core.utils.Point;

/** This class represents the HealthBar of the enemy */
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
        super(0, 100, 1, false, new Skin());
        this.entity = entity;
        this.setAnimateDuration(0.1f);
        EnemyData ed = buildDataObject();
        Point position = ed.pc.position();
        Vector3 screenPosition =
                CameraSystem.camera().project(new Vector3(position.x, position.y, 0));
        this.setBounds(
                screenPosition.x + POSITION_OFFSET.x, screenPosition.y + POSITION_OFFSET.y, 35, 10);
        Game.stage().get().addActor(this);
    }

    /**
     * Updates an entity's HealthBar, sets the position and value every frame. Removes the HealthBar
     * if entity has no health left.
     */
    public void update() {
        EnemyData ed = buildDataObject();
        if (ed.hc.currentHealthpoints() <= 0) this.remove();
        // set visible only if entity lost health
        this.setVisible(ed.hc.currentHealthpoints() != ed.hc.maximalHealthpoints());
        Point position = ed.pc.position();
        Vector3 screenPosition =
                CameraSystem.camera().project(new Vector3(position.x, position.y, 0));
        this.setPosition(
                screenPosition.x + POSITION_OFFSET.x, screenPosition.y + POSITION_OFFSET.y);

        // set value to health percent
        this.setValue((float) ed.hc.currentHealthpoints() / ed.hc.maximalHealthpoints() * 100);
    }

    private EnemyData buildDataObject() {
        return new EnemyData(
                entity.fetch(HealthComponent.class).orElseThrow(),
                entity.fetch(PositionComponent.class).orElseThrow());
    }
}
