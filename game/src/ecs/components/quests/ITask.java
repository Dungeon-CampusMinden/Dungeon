package ecs.components.quests;

import java.io.Serializable;

import ecs.entities.Entity;

public interface ITask extends Serializable {

    public void advance();

    public boolean isCompleted();

    public String completion();

    public void load(Entity entity);

}
