package contrib.systems;

import static contrib.hud.UITools.DEFAULT_SKIN;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;

import contrib.components.HealthComponent;
import contrib.components.UIComponent;

import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.systems.CameraSystem;
import core.utils.Point;
import core.utils.logging.CustomLogLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The HealthbarSystem creates a Progressbar which follows the associated Entity and shows the
 * current healthPercentage.
 */
public final class HealthbarSystem extends System {
    // well percentage =)
    private static final float MIN = 0;
    private static final float MAX = 1;
    // bar percentage precision
    private static final float STEP_SIZE = 0.01f;
    private static final Logger LOGGER = Logger.getLogger(HealthbarSystem.class.getSimpleName());
    // how long the change to the Healthbar should take in seconds
    private static final float HEALTHBAR_UPDATE_DURATION = 0.1f;
    // the height of the healthbar which can´t be smaller than the nineslicedrawable
    private static final int HEALTHBAR_HEIGHT = 10;
    // the width of the healthbar which can´t be smaller than the nineslicedrawable
    private static final int HEALTHBAR_WIDTH = 50;

    /** Mapping from actual Entity and Healthbar of this Entity */
    private final Map<Integer, ProgressBar> healthbarMapping = new HashMap<>();

    public HealthbarSystem() {
        super(HealthComponent.class, PositionComponent.class);
        this.onEntityAdd =
                (x) -> {
                    LOGGER.log(CustomLogLevel.TRACE, "HealthbarSystem got send a new Entity");
                    ProgressBar newHealthbar =
                            createNewHealthbar(x.fetch(PositionComponent.class).orElseThrow());
                    LOGGER.log(CustomLogLevel.TRACE, "created a new Healthbar");
                    Entity e = new Entity("Healthbar");
                    LOGGER.log(CustomLogLevel.TRACE, "created a new Entity for the Healthbar");
                    Container<ProgressBar> group = new Container<>(newHealthbar);
                    // disabling layout enforcing from parent
                    group.setLayoutEnabled(false);
                    e.add(new UIComponent(group, false, false));
                    Game.add(e);
                    LOGGER.log(CustomLogLevel.TRACE, "created a new UIComponent for the Healthbar");
                    healthbarMapping.put(x.id(), newHealthbar);
                    LOGGER.log(CustomLogLevel.TRACE, "HealthbarSystem added to temporary mapping");
                };
        LOGGER.log(CustomLogLevel.TRACE, "HealthbarSystem onEntityAdd was changed");
        this.onEntityRemove = (x) -> healthbarMapping.remove(x.id()).remove();
        LOGGER.log(CustomLogLevel.TRACE, "HealthbarSystem onEntityRemove was changed");
        LOGGER.info("HealthbarSystem created");
    }

    @Override
    public void execute() {
        entityStream().map(this::buildDataObject).forEach(this::update);
    }

    private void update(EnemyData ed) {
        if (ed.hc.currentHealthpoints() <= 0) ed.pb.remove();
        // set visible only if entity lost health
        ed.pb.setVisible(ed.hc.currentHealthpoints() != ed.hc.maximalHealthpoints());
        updatePosition(ed.pb, ed.pc);

        // set value to health percent
        ed.pb.setValue((float) ed.hc.currentHealthpoints() / ed.hc.maximalHealthpoints());
    }

    private EnemyData buildDataObject(Entity entity) {
        return new EnemyData(
                entity.fetch(HealthComponent.class).orElseThrow(),
                entity.fetch(PositionComponent.class).orElseThrow(),
                healthbarMapping.get(entity.id()));
    }

    private ProgressBar createNewHealthbar(PositionComponent pc) {
        ProgressBar progressBar =
                new ProgressBar(MIN, MAX, STEP_SIZE, false, DEFAULT_SKIN, "healthbar");
        progressBar.setAnimateDuration(HEALTHBAR_UPDATE_DURATION);
        progressBar.setSize(HEALTHBAR_WIDTH, HEALTHBAR_HEIGHT);
        progressBar.setVisible(true);
        updatePosition(progressBar, pc);
        return progressBar;
    }

    /** moves the Progressbar to follow the Entity */
    private void updatePosition(ProgressBar pb, PositionComponent pc) {
        Point position = pc.position();
        Vector3 conveered = new Vector3(position.x, position.y, 0);
        // map Entity coordinates to window coords
        Vector3 screenPosition = CameraSystem.camera().project(conveered);
        // get the stage of the Game
        Stage stage = Game.stage().orElseThrow(() -> new RuntimeException("No Stage available"));
        // remap window coords again stage coords
        screenPosition.x =
                screenPosition.x / stage.getViewport().getScreenWidth() * stage.getWidth();
        screenPosition.y =
                screenPosition.y / stage.getViewport().getScreenHeight() * stage.getHeight();
        pb.setPosition(screenPosition.x, screenPosition.y);
    }

    private record EnemyData(HealthComponent hc, PositionComponent pc, ProgressBar pb) {}
}
