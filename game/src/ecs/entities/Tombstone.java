package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.components.ai.AITools;
import starter.Game;

import java.util.logging.Logger;

/**
 * This Tombstone is used to despawn the Npc and to get the reward.
 * <p>
 * The reward will be executed, if the Player collides with the Tombstone and the Npc is in a range of 2.
 */
public class Tombstone extends Entity{
    private transient final Logger tombstoneLogger = Logger.getLogger(this.getClass().getName());
    private final String path = "dungeon/Tombstone/skull.png";
    private Npc npcGhost;

    /**
     * Tombstone constructor is used to set up the Entity.
     *
     * @param npc is the Npc that belongs to this Tombstone.
     */
    public Tombstone(Npc npc){
        super();
        npcGhost = npc;
        setupPositionComponent();
        setupAnimationComponent();
        setupHitBoxComponent();
        tombstoneLogger.info("Tombstone was created");
    }

    private void setupPositionComponent(){
        new PositionComponent(this);
    }

    private void setupAnimationComponent() {
        new AnimationComponent(this, AnimationBuilder.buildAnimation(path));
    }

    private void setupHitBoxComponent(){
        new HitboxComponent(
            this,
            (you, other, direction) -> reward(other),
            (you, other, direction) -> {
            });
    }

    private void reward(Entity entity){
        if(entity instanceof Hero){
            if(this.npcGhost != null && AITools.playerInRange(this.npcGhost, 2)){
                int random = (int) (Math.random() * 2);
                switch (random) {
                    case 0 -> randomKindOfMonsterDelete();
                    case 1 -> randomMonsterSpawn();
                }
                Game.removeEntity(this.npcGhost);
                this.npcGhost = null;
            }
        }
    }

    private void randomKindOfMonsterDelete() {
        int random = (int) (Math.random() * 3);
        switch (random) {
            case 0 -> {
                Game.getEntities().stream()
                    // Consider only Imp monsters
                    .filter(e -> Imp.class.isAssignableFrom(e.getClass()))
                    // Remove the monsters
                    .forEach(Game::removeEntity);
                tombstoneLogger.info("All " + Imp.class.getName() + "s have been deleted");
            }
            case 1 -> {
                Game.getEntities().stream()
                    // Consider only Chort monsters
                    .filter(e -> Chort.class.isAssignableFrom(e.getClass()))
                    // Remove the monsters
                    .forEach(Game::removeEntity);
                tombstoneLogger.info("All " + Chort.class.getName() + "s have been deleted");
            }
            case 2 -> {
                Game.getEntities().stream()
                    // Consider only DarkKnight monsters
                    .filter(e -> DarkKnight.class.isAssignableFrom(e.getClass()))
                    // Remove the monsters
                    .forEach(Game::removeEntity);
                tombstoneLogger.info("All " + DarkKnight.class.getName() + "s have been deleted");
            }
        }
    }

    private void randomMonsterSpawn(){
        int random = (int) (Math.random() * 3);
        switch (random) {
            case 0 -> {
                Game.addEntity(new Imp(Game.getLevel()));
                tombstoneLogger.info(Imp.class.getName() + "was added to the Game");
            }
            case 1 -> {
                Game.addEntity(new Chort(Game.getLevel()));
                tombstoneLogger.info(Chort.class.getName() + "was added to the Game");
            }
            case 2 -> {
                Game.addEntity(new DarkKnight(Game.getLevel()));
                tombstoneLogger.info(DarkKnight.class.getName() + "was added to the Game");
            }
        }
    }
}
