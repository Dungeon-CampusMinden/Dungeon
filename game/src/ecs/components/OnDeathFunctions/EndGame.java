package ecs.components.OnDeathFunctions;

import ecs.components.IOnDeathFunction;
import ecs.entities.Entity;
import ecs.tools.Flags.Flag;
import starter.Game;

import java.io.Serializable;

/** Global on death Function that will end the Game */
public class EndGame implements IOnDeathFunction, Serializable {

    @Override
    public void onDeath(Entity entity) {
        Game.gameOver();
    }

}
