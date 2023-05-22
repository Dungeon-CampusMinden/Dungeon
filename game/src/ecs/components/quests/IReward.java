package ecs.components.quests;

import java.io.Serializable;

import ecs.entities.Entity;

public interface IReward extends Serializable {

    public void reward(Entity entity);

}
