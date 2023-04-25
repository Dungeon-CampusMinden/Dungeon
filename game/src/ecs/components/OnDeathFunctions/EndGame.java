package ecs.components.OnDeathFunctions;

import ecs.components.IOnDeathFunction;
import ecs.entities.Entity;
import ecs.tools.Flags.Flag;
import starter.Game;

public class EndGame implements IOnDeathFunction {

    @Override
    public void onDeath(Entity entity) {
        // turn into deathscreen
        Game.gameOver();
    }

}
