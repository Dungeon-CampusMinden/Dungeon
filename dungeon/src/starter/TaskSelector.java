package starter;

import contrib.components.InteractionComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.TileLevel;
import core.level.elements.ILevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.path.SimpleIPath;
import entrypoint.DSLEntryPoint;
import java.io.IOException;
import java.util.Set;
import java.util.function.BiConsumer;
import task.Task;
import task.TaskContent;
import task.game.hud.UIAnswerCallback;
import task.tasktype.Quiz;
import task.tasktype.quizquestion.SingleChoice;

/**
 * This class contains static methods to create the TaskSelector-Level to select the tasks at the
 * start of the game.
 *
 * <p>This class is part of the {@link Starter} and should not be used otherwise. The code is
 * extracted into this class for better code readability.
 */
public class TaskSelector {
  protected static DSLEntryPoint selectedDSLEntryPoint = null;

  protected static ILevel taskSelectorLevel() {
    // default layout is:
    //
    // W W W W W
    // W F F F W
    // W F F F W
    // W F F F W
    // W W W W W
    ILevel level =
        new TileLevel(
            new LevelElement[][] {
              new LevelElement[] {
                LevelElement.WALL,
                LevelElement.WALL,
                LevelElement.WALL,
                LevelElement.WALL,
                LevelElement.WALL,
              },
              new LevelElement[] {
                LevelElement.WALL,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.WALL,
              },
              new LevelElement[] {
                LevelElement.WALL,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.WALL,
              },
              new LevelElement[] {
                LevelElement.WALL,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.FLOOR,
                LevelElement.WALL,
              },
              new LevelElement[] {
                LevelElement.WALL,
                LevelElement.WALL,
                LevelElement.WALL,
                LevelElement.WALL,
                LevelElement.WALL,
              }
            },
            DesignLabel.randomDesign());
    level.changeTileElementType(level.endTile(), LevelElement.FLOOR);
    return level;
  }

  protected static SingleChoice selectTaskQuestion(Set<DSLEntryPoint> entryPoints) {
    SingleChoice question = new SingleChoice("Wähle deine Mission:");
    entryPoints.forEach(ep -> question.addAnswer(new PayloadTaskContent(ep)));
    question.state(Task.TaskState.PROCESSING_ACTIVE);
    question.taskName(" ");
    return question;
  }

  protected static Entity npc(SingleChoice selectionQuestion) throws IOException {
    Entity npc = new Entity("Selection NPC");
    npc.add(new DrawComponent(new SimpleIPath("character/blue_knight")));
    npc.add(new PositionComponent());
    npc.add(
        new InteractionComponent(
            1,
            true,
            UIAnswerCallback.askOnInteraction(selectionQuestion, setSelectedEntryPoint())));

    return npc;
  }

  private static BiConsumer<Task, Set<TaskContent>> setSelectedEntryPoint() {
    return (task, taskContents) -> {
      if (taskContents.isEmpty()) return;
      selectedDSLEntryPoint =
          ((PayloadTaskContent)
                  taskContents.stream()
                      .findFirst()
                      .orElseThrow(
                          () ->
                              new RuntimeException(
                                  "Something went wrong at selecting the DSLEntryPoint")))
              .payload();
      task.state(Task.TaskState.FINISHED_CORRECT);
    };
  }

  private static class PayloadTaskContent extends Quiz.Content {
    private final DSLEntryPoint payload;

    public PayloadTaskContent(DSLEntryPoint payload) {
      super(payload.displayName());
      this.payload = payload;
    }

    public DSLEntryPoint payload() {
      return payload;
    }
  }
}
