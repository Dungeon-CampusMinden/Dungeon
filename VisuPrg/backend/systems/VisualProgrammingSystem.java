package systems;

import components.ValueComponent;
import core.Entity;
import core.System;
import core.components.PlayerComponent;
import core.level.elements.ILevel;
import tasks.IVisuTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class VisualProgrammingSystem extends System {

    // level stack
    private final BlockingQueue<IVisuTask> tasks = new LinkedBlockingQueue<>();
    private IVisuTask currentTask;

    private Map<String, Entity> variableChests = new HashMap<>();
    private Map<ILevel, Map<String, Entity>> levelVariable = new HashMap<>();

    public VisualProgrammingSystem(){
        super(ValueComponent.class, PlayerComponent.class);
    }
    @Override
    public void execute() {
        if (!tasks.isEmpty() && currentTask == null){
            currentTask = tasks.element();
        } else if (currentTask != null){
            currentTask.execute();
        }
        // Just for debuggin / delete please
        currentTask = currentTask;
    }

    public void setTaskDone(){
        // Todo - what if exception is thrown?
        if (tasks.remove(currentTask)){
            currentTask = null;
        };
    }

    public void addVariable(Entity variableChest){
        variableChests.put(variableChest.name(), variableChest);
    }

    public void addTask(ArrayList<IVisuTask> taskList){
        for (IVisuTask task : taskList) {
            tasks.add(task);
        }
    }

    public Entity getVariableChest(String variableName) {
        return variableChests.get(variableName);
    }
}
