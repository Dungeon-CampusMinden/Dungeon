package contrib.systems;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;

import contrib.components.HealthComponent;

import core.Entity;
import core.System;
import core.components.PositionComponent;
import core.hud.UITools;
import core.systems.CameraSystem;
import core.utils.Point;

import java.util.HashMap;
import java.util.Map;

public final class HealthbarSystem extends System {

    private Map<Integer, ProgressBar> healthbarMapping = new HashMap<>();

    public HealthbarSystem() {
        super(HealthComponent.class, PositionComponent.class);
        this.onEntityAdd =
                (x) ->
                        healthbarMapping.put(
                                x.id(),
                                createNewHealthbar(x.fetch(PositionComponent.class).orElseThrow()));
        this.onEntityRemove = (x) -> healthbarMapping.remove(x.id());
    }

    @Override
    public void execute() {
        entityStream().map(this::buildDataObject).forEach(this::update);
    }

    private void update(EnemyData ed) {
        // entferne wenn tod
        if (ed.hc.currentHealthpoints() <= 0) ed.pb.remove();
        // set visible only if entity lost health
        ed.pb.setVisible(ed.hc.currentHealthpoints() != ed.hc.maximalHealthpoints());
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
