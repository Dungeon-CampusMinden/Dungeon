package ecs.components.quests;

import java.io.Serializable;

public interface ITask extends Serializable {

    public void advance();

    public boolean isCompleted();

    public String completion();

}
