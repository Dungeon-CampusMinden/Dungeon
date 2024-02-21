package tasks;

import contrib.entities.EntityFactory;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import systems.VisualProgrammingSystem;

public class CreateVariable extends VisuTask {

    final String variableName;
    final VisualProgrammingSystem visualProgrammingSystem;
    public CreateVariable(String message, VisualProgrammingSystem visualProgrammingSystem, String variableName) {
        super(message);
        this.variableName = variableName;
        this.visualProgrammingSystem = visualProgrammingSystem;
    }


    @Override
    public void execute() {
        // todo - set value if given (is this necessary?)
        Entity chest = null;
        try{
            // todo - dont make position random
            chest = EntityFactory.newChestDummy( PositionComponent.ILLEGAL_POSITION, variableName, 0);
            Game.add(chest);
        } catch(Exception e){

        }
        visualProgrammingSystem.addVariable(chest);
        visualProgrammingSystem.setTaskDone();
    }
}
