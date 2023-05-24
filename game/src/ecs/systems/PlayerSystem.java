package ecs.systems;

import com.badlogic.gdx.Gdx;
import configuration.KeyboardConfig;
import ecs.components.InventoryComponent;
import ecs.components.MissingComponentException;
import ecs.components.PlayableComponent;
import ecs.components.VelocityComponent;
import ecs.entities.Entity;
import ecs.tools.interaction.InteractionTool;
import starter.Game;

/** Used to control the player */
public class PlayerSystem extends ECS_System {

    private record KSData(Entity e, PlayableComponent pc, VelocityComponent vc, InventoryComponent in) {}

    @Override
    public void update() {
        Game.getEntities().stream()
                .flatMap(e -> e.getComponent(PlayableComponent.class).stream())
                .map(pc -> buildDataObject((PlayableComponent) pc))
                .forEach(this::checkKeystroke);
    }

    private void checkKeystroke(KSData ksd) {
        if (Gdx.input.isKeyPressed(KeyboardConfig.MOVEMENT_UP.get()))
            ksd.vc.setCurrentYVelocity(1 * ksd.vc.getYVelocity());
        else if (Gdx.input.isKeyPressed(KeyboardConfig.MOVEMENT_DOWN.get()))
            ksd.vc.setCurrentYVelocity(-1 * ksd.vc.getYVelocity());
        else if (Gdx.input.isKeyPressed(KeyboardConfig.MOVEMENT_RIGHT.get()))
            ksd.vc.setCurrentXVelocity(1 * ksd.vc.getXVelocity());
        else if (Gdx.input.isKeyPressed(KeyboardConfig.MOVEMENT_LEFT.get()))
            ksd.vc.setCurrentXVelocity(-1 * ksd.vc.getXVelocity());

        if (Gdx.input.isKeyPressed(KeyboardConfig.INVENTORY_FIRST.get())){
            ksd.in.useItem(0);
        }
        else if(Gdx.input.isKeyPressed(KeyboardConfig.INVENTORY_SECOND.get())){
            ksd.in.useItem(1);
        }
        else if(Gdx.input.isKeyPressed(KeyboardConfig.INVENTORY_THIRD.get())){
            ksd.in.useItem(2);
        }
        else if(Gdx.input.isKeyPressed(KeyboardConfig.INVENTORY_FIFTH.get())){
            ksd.in.useItem(3);
        }
        else if(Gdx.input.isKeyPressed(KeyboardConfig.INVENTORY_FIFTH.get())){
            ksd.in.useItem(4);
        }
        else if(Gdx.input.isKeyPressed(KeyboardConfig.INVENTORY_SIXTH.get())){
            ksd.in.useItem(5);
        }
        else if(Gdx.input.isKeyPressed(KeyboardConfig.INVENTORY_SEVENTH.get())){
            ksd.in.useItem(6);
        }
        else if(Gdx.input.isKeyPressed(KeyboardConfig.INVENTORY_EIGHTH.get())){
            ksd.in.useItem(7);
        }
        else if(Gdx.input.isKeyPressed(KeyboardConfig.INVENTORY_NINTH.get())){
            ksd.in.useItem(8);
        }
        else if(Gdx.input.isKeyPressed(KeyboardConfig.INVENTORY_REMOVE.get())){
            ksd.in.removeFirstItem();
        }

        if (Gdx.input.isKeyPressed(KeyboardConfig.INTERACT_WORLD.get()))
            InteractionTool.interactWithClosestInteractable(ksd.e);
        else if (Gdx.input.isKeyPressed(KeyboardConfig.INTERACT_WORLD_X.get()))
            InteractionTool.interactWithClosestInteractable(ksd.e);

        // check skills
        else if (Gdx.input.isKeyPressed(KeyboardConfig.FIRST_SKILL.get()))
            ksd.pc.getSkillSlot1().ifPresent(skill -> skill.execute(ksd.e));
        else if (Gdx.input.isKeyPressed(KeyboardConfig.SECOND_SKILL.get()))
            ksd.pc.getSkillSlot2().ifPresent(skill -> skill.execute(ksd.e));
    }

    private KSData buildDataObject(PlayableComponent pc) {
        Entity e = pc.getEntity();

        VelocityComponent vc =
                (VelocityComponent)
                        e.getComponent(VelocityComponent.class)
                                .orElseThrow(PlayerSystem::missingVC);

        InventoryComponent in =
                (InventoryComponent)
                        e.getComponent(InventoryComponent.class)
                                .orElseThrow(PlayerSystem::missingIN);

        return new KSData(e, pc, vc, in);
    }

    private static MissingComponentException missingVC() {
        return new MissingComponentException("VelocityComponent");
    }

    private static MissingComponentException missingIN() {
        return new MissingComponentException("InventoryComponent");
    }
}
