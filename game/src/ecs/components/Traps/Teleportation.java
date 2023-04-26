package game.src.ecs.components.Traps;

import ecs.components.PositionComponent;
import level.elements.ILevel;
import level.elements.tile.Tile;
import starter.Game;
import ecs.entities.Entity;

public class Teleportation implements ITrigger{
    
    public void trigger(Entity entity) {
        Tile tile = Game.currentLevel.getRandomFloorTile();
        ((PositionComponent) entity.getComponent(PositionComponent.class).get()).setPosition(tile.getCoordinateAsPoint());
    }

}
