package core.systems;

import com.badlogic.gdx.Gdx;

import contrib.configuration.KeyboardConfig;
import contrib.utils.components.interaction.InteractionTool;

import core.Component;
import core.Entity;
import core.System;
import core.components.PlayerComponent;
import core.components.VelocityComponent;

import java.util.HashSet;
import java.util.Set;

/** Used to control the player */
public class PlayerSystem extends System {

    public PlayerSystem() {
        super(PlayerComponent.class, getSet());
    }

    private static Set<Class<? extends Component>> getSet() {
        Set<Class<? extends Component>> set = new HashSet<>();
        set.add(VelocityComponent.class);
        return set;
    }

    @Override
    public void execute() {
        getEntityStream().map(this::buildDataObject).forEach(this::checkKeystroke);
    }

    private void checkKeystroke(PSData ksd) {
        if (Gdx.input.isKeyPressed(KeyboardConfig.MOVEMENT_UP.get()))
            ksd.vc.setCurrentYVelocity(1 * ksd.vc.getYVelocity());
        else if (Gdx.input.isKeyPressed(KeyboardConfig.MOVEMENT_DOWN.get()))
            ksd.vc.setCurrentYVelocity(-1 * ksd.vc.getYVelocity());
        else if (Gdx.input.isKeyPressed(KeyboardConfig.MOVEMENT_RIGHT.get()))
            ksd.vc.setCurrentXVelocity(1 * ksd.vc.getXVelocity());
        else if (Gdx.input.isKeyPressed(KeyboardConfig.MOVEMENT_LEFT.get()))
            ksd.vc.setCurrentXVelocity(-1 * ksd.vc.getXVelocity());

        if (Gdx.input.isKeyPressed(KeyboardConfig.INTERACT_WORLD.get()))
            InteractionTool.interactWithClosestInteractable(ksd.e);

        // check skills
        else if (Gdx.input.isKeyPressed(KeyboardConfig.FIRST_SKILL.get()))
            ksd.pc.getSkillSlot1().ifPresent(skill -> skill.execute(ksd.e));
        else if (Gdx.input.isKeyPressed(KeyboardConfig.SECOND_SKILL.get()))
            ksd.pc.getSkillSlot2().ifPresent(skill -> skill.execute(ksd.e));
    }

    private PSData buildDataObject(Entity e) {
        PlayerComponent pc = (PlayerComponent) e.getComponent(PlayerComponent.class).get();
        VelocityComponent vc = (VelocityComponent) e.getComponent(VelocityComponent.class).get();

        return new PSData(e, pc, vc);
    }

    private record PSData(Entity e, PlayerComponent pc, VelocityComponent vc) {}
}
