package dslinterop.nativescenariobuilder;

import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.entities.EntityFactory;
import contrib.entities.WorldItemBuilder;
import contrib.hud.dialogs.OkDialog;
import contrib.hud.dialogs.TextDialog;
import contrib.hud.elements.GUICombination;
import contrib.hud.inventory.InventoryGUI;
import contrib.utils.components.draw.ChestAnimations;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.components.draw.Animation;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import task.Task;
import task.TaskContent;
import task.game.components.TaskComponent;
import task.game.components.TaskContentComponent;
import task.game.content.QuestItem;
import task.game.hud.QuizUI;
import task.game.hud.YesNoDialog;
import task.reporting.AnswerPickingFunctions;
import task.tasktype.AssignTask;
import task.tasktype.Element;
import task.tasktype.Quiz;

/** WTF? . */
public class NativeScenarioBuilder {
  /**
   * WTF? .
   *
   * @param quiz foo
   * @return foo
   */
  public static Set<Set<Entity>> quizOnHud(Quiz quiz) {
    Entity questowner = new Entity("Questgeber");
    questowner.add(new PositionComponent());
    try {
      questowner.add(new DrawComponent(new SimpleIPath("character/blue_knight")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    new TaskComponent(quiz, questowner);

    questowner.add(askOnInteractionQuiz(quiz));

    Set<Set<Entity>> returnSet = new HashSet<>();
    Set<Entity> roomSet = new HashSet<>();

    roomSet.add(questowner);

    returnSet.add(roomSet);
    returnSet.add(fillerSet());
    return returnSet;
  }

  /**
   * WTF? .
   *
   * @param task foo
   * @return foo
   */
  public static Set<Set<Entity>> assignTaskA(AssignTask task) {
    Set<Set<Entity>> returnSet = new HashSet<>();
    Set<Entity> roomSet = new HashSet<>();

    // set scenario text
    task.scenarioText(
        "Die Schriftrollen enthalten die Antworten. Ordne die Schriftrollen "
            + "den passenden Quest-Truhen zu!");
    // set answer picker function
    task.answerPickingFunction(AnswerPickingFunctions.multipleChestPicker());

    // setup quest owner
    Entity questowner = new Entity("Questgeber");
    questowner.add(new PositionComponent());
    try {
      questowner.add(new DrawComponent(new SimpleIPath("character/blue_knight")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    new TaskComponent(task, questowner);
    questowner.add(askOnInteractionYesNo(task));
    roomSet.add(questowner);

    // setup items and chests
    var entrySet = task.solution().entrySet();
    for (var entry : entrySet) {
      // setup quest items
      for (var element : entry.getValue()) {
        if (!element.equals(AssignTask.EMPTY_ELEMENT)) {
          Animation animation =
              Animation.fromSingleImage(new SimpleIPath("items/book/wisdom_scroll.png"));
          TaskContentComponent tcc = new TaskContentComponent(element);
          QuestItem questItem = new QuestItem(animation, tcc);
          Entity worldItem = WorldItemBuilder.buildWorldItem(questItem);
          roomSet.add(worldItem);
        }
      }

      // setup chests
      Element key = entry.getKey();
      if (!key.equals(AssignTask.EMPTY_ELEMENT)) {
        try {
          Entity chest = EntityFactory.newChest();

          // empty generated chest
          chest.remove(InventoryComponent.class);
          chest.add(new InventoryComponent());

          chest.remove(InteractionComponent.class);
          chest.add(new InteractionComponent(1.5f, true, QuestChestInventoryInteraction()));

          // mark as task container
          var tcc = new TaskContentComponent();
          tcc.content(key);
          chest.add(tcc);

          task.addContent(key);
          task.addContainer(key);
          roomSet.add(chest);
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      }
    }

    returnSet.add(roomSet);
    return returnSet;
  }

  private static InteractionComponent askOnInteractionQuiz(Quiz quiz) {
    return new InteractionComponent(
        1,
        true,
        (thisEntity, otherEntity) -> {
          if (quiz.state().equals(Task.TaskState.ACTIVE)
              || quiz.state().equals(Task.TaskState.PROCESSING_ACTIVE)) {
            QuizUI.askQuizOnHud(quiz);
          } else {
            OkDialog.showOkDialog("Du hast die Aufgabe schon bearbeitet.", "Info", () -> {});
          }
        });
  }

  private static BiConsumer<Entity, Entity> QuestChestInventoryInteraction() {
    return (chest, other) -> {
      InventoryComponent chestIc = chest.fetch(InventoryComponent.class).get();
      InventoryComponent otherIc = other.fetch(InventoryComponent.class).get();

      var optionalTcc = chest.fetch(TaskContentComponent.class);
      InventoryGUI inventory;
      if (optionalTcc.isEmpty()) {
        inventory = new InventoryGUI(chestIc);
      } else {
        TaskContent content = optionalTcc.get().content();
        Task task = content.task();
        inventory = new InventoryGUI(content + " (Quest: '" + task.taskName() + "')", chestIc);
      }

      UIComponent uiComponent =
          new UIComponent(new GUICombination(new InventoryGUI(otherIc), inventory), true);
      uiComponent.onClose(
          () ->
              chest
                  .fetch(DrawComponent.class)
                  .ifPresent(
                      interactedDC -> {
                        // remove all prior
                        // opened animations
                        interactedDC.deQueueByPriority(ChestAnimations.OPEN_FULL.priority());
                        if (chestIc.count() > 0) {
                          // aslong as
                          // there is an
                          // item inside
                          // the chest
                          // show a full
                          // chest
                          interactedDC.queueAnimation(ChestAnimations.OPEN_FULL);
                        } else {
                          // empty chest
                          // show the
                          // empty
                          // animation
                          interactedDC.queueAnimation(ChestAnimations.OPEN_EMPTY);
                        }
                      }));
      other.add(uiComponent);
      chest
          .fetch(DrawComponent.class)
          .ifPresent(
              interactedDC -> {
                // only add opening animation when it is not
                // finished
                if (interactedDC
                    .animation(ChestAnimations.OPENING)
                    .map(animation -> !animation.isFinished())
                    .orElse(true)) {
                  interactedDC.queueAnimation(ChestAnimations.OPENING);
                }
              });
    };
  }

  private static InteractionComponent askOnInteractionYesNo(Task task) {
    return new InteractionComponent(
        1,
        true,
        (thisEntity, otherEntity) -> {
          if (task.state().equals(Task.TaskState.ACTIVE)
              || task.state().equals(Task.TaskState.PROCESSING_ACTIVE)) {
            YesNoDialog.showYesNoDialog(task);
          } else {
            OkDialog.showOkDialog("Du hast die Aufgabe schon bearbeitet.", "Info", () -> {});
          }
        });
  }

  private static BiConsumer<Task, Set<TaskContent>> showAnswersOnHud() {
    return (task, taskContents) -> {
      float score = task.gradeTask(taskContents);
      task.managerEntity().get().remove(InteractionComponent.class);
      TextDialog.textDialog("Your score: " + score, "Ok", "Given answer");
    };
  }

  private static Set<Entity> fillerSet() {
    try {
      return Set.of(
          EntityFactory.newChest(),
          EntityFactory.randomMonster(),
          EntityFactory.randomMonster(),
          EntityFactory.randomMonster());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
