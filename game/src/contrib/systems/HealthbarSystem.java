package contrib.systems;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;

import contrib.components.HealthComponent;

import core.Entity;
import core.System;
import core.components.PositionComponent;
import core.components.UIComponent;
import core.hud.UITools;
import core.systems.CameraSystem;
import core.utils.Point;
import core.utils.logging.CustomLogLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class HealthbarSystem extends System {
    private static final Logger LOGGER = Logger.getLogger(HealthbarSystem.class.getSimpleName());

    private final Map<Integer, ProgressBar> healthbarMapping = new HashMap<>();

    public HealthbarSystem() {
        super(HealthComponent.class, PositionComponent.class);
        this.onEntityAdd =
                (x) -> {
                    LOGGER.log(CustomLogLevel.TRACE, "HealthbarSystem got send a new Entity");
                    ProgressBar newHealthbar =
                            createNewHealthbar(x.fetch(PositionComponent.class).orElseThrow());
                    LOGGER.log(CustomLogLevel.TRACE, "created a new Healthbar");
                    Entity e = new Entity();
                    LOGGER.log(CustomLogLevel.TRACE, "created a new Entity for the Healthbar");
                    new UIComponent(e, new Container<>(newHealthbar), false);
                    LOGGER.log(CustomLogLevel.TRACE, "created a new UIComponent for the Healthbar");
                    healthbarMapping.put(x.id(), newHealthbar);
                    LOGGER.log(CustomLogLevel.TRACE, "HealthbarSystem added to temporary mapping");
                };
        LOGGER.log(CustomLogLevel.TRACE, "HealthbarSystem onEntityAdd was changed");
        this.onEntityRemove = (x) -> healthbarMapping.remove(x.id());
        LOGGER.log(CustomLogLevel.TRACE, "HealthbarSystem onEntityRemove was changed");
        LOGGER.info("HealthbarSystem created");
    }

    @Override
    public void execute() {
        entityStream().map(this::buildDataObject).forEach(this::update);
    }

    private void update(EnemyData ed) {
        // entferne wenn tod
        if (ed.hc.currentHealthpoints() <= 0) ed.pb.remove();
        // set visible only if entity lost health
        // ed.pb.setVisible(ed.hc.currentHealthpoints() != ed.hc.maximalHealthpoints());
        updatePosition(ed.pb, ed.pc);

        // set value to health percent
        ed.pb.setValue((float) ed.hc.currentHealthpoints() / ed.hc.maximalHealthpoints() * 100);
    }

    private EnemyData buildDataObject(Entity entity) {
        return new EnemyData(
                entity.fetch(HealthComponent.class).orElseThrow(),
                entity.fetch(PositionComponent.class).orElseThrow(),
                healthbarMapping.get(entity.id()));
    }

    private ProgressBar createNewHealthbar(PositionComponent pc) {
        ProgressBar progressBar = new ProgressBar(0, 100, 10, false, UITools.DEFAULT_SKIN);
        progressBar.setAnimateDuration(0.1f);
        progressBar.setSize(35, 10);
        progressBar.setVisible(true);
        updatePosition(progressBar, pc);
        return progressBar;
    }

    private void updatePosition(ProgressBar pb, PositionComponent pc) {
        Point position = pc.position();
        Vector3 screenPosition =
                CameraSystem.camera().project(new Vector3(position.x, position.y, 0));
        pb.setPosition(screenPosition.x, screenPosition.y);
    }

    private record EnemyData(HealthComponent hc, PositionComponent pc, ProgressBar pb) {}
}
