package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.components.ai.AITools;
import starter.Game;

public class Tombstone extends Entity{
    private final String path = "dungeon/Tombstone/skull.png";
    private Npc npcGhost;

    public Tombstone(Npc npc){
        super();
        npcGhost = npc;
        setupPositionComponent();
        setupAnimationComponent();
        setupHitBoxComponent();
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
            (you, other, direction) -> System.out.println(""));
    }

    public void reward(Entity entity){
        if(entity instanceof Hero){
            if(this.npcGhost != null && AITools.playerInRange(this.npcGhost, 2)){
                int random = (int) (Math.random() * 2);
                switch (random) {
                    case 0 -> randomMonsterKindDelete();
                    case 1 -> randomMonsterSpawn();
                }
                Game.removeEntity(this.npcGhost);
                this.npcGhost = null;
            }
        }
    }

    private void randomMonsterKindDelete() {
        int random = (int) (Math.random() * 3);
        switch (random) {
            case 0 -> Game.getEntities().stream()
                // Consider only monsters
                .filter(e -> Imp.class.isAssignableFrom(e.getClass()))
                // Remove the monsters
                .forEach(Game::removeEntity);
            case 1 -> Game.getEntities().stream()
                // Consider only monsters
                .filter(e -> Chort.class.isAssignableFrom(e.getClass()))
                // Remove the monsters
                .forEach(Game::removeEntity);
            case 2 -> Game.getEntities().stream()
                // Consider only monsters
                .filter(e -> DarkKnight.class.isAssignableFrom(e.getClass()))
                // Remove the monsters
                .forEach(Game::removeEntity);
        }
    }

    private void randomMonsterSpawn(){
        int random = (int) (Math.random() * 3);
        switch (random) {
            case 0 -> Game.addEntity(new Imp(Game.getLevel()));
            case 1 -> Game.addEntity(new Chort(Game.getLevel()));
            case 2 -> Game.addEntity(new DarkKnight(Game.getLevel()));
        }
    }
}
