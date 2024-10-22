package contrib.systems;


import core.System;
import newdsl.events.EventHandler;
import newdsl.events.GradeEvent;
import newdsl.events.GradeReceivedEvent;
import newdsl.interpreter.DSLInterpreter;
import newdsl.tasks.Task;

public class GradingSystem extends System {

    private final DSLInterpreter interpreter;

    public GradingSystem(DSLInterpreter interpreter) {
        super();
        this.interpreter = interpreter;
        EventHandler.subscribeToAllTopics(GradeEvent.class, this::onGradeEvent);
    }

    @Override
    public void execute() {

    }

    private void onGradeEvent(GradeEvent event) {
        float score = interpreter.gradeTask(event.getTaskId(), event.getAnswers());
        Task t = (Task) interpreter.env.get(event.getTaskId());
        EventHandler.publish(new GradeReceivedEvent(score, t), event.getTaskId());
    }
}
