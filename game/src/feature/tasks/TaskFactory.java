package feature.tasks;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.Point;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.components.UIComponent;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.DialogFactory;
import feature.hud.elements.RichLabel;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Builds dungeon situations from {@link Task}s. */
public final class TaskFactory {

  private static final String TERMINAL_SPRITE = "objects/tech/Computer_1.png";

  private TaskFactory() {}

  /**
   * Shows a choice task as a dialog.
   *
   * @param task the question
   * @param title the dialog title
   * @param player the player who answers
   * @param onAnswer receives the player and the confirmed option values
   * @return the dialog component
   */
  public static UIComponent showChoice(
      final ChoiceTask task,
      final String title,
      final Entity player,
      final BiConsumer<Entity, Set<String>> onAnswer) {
    List<ChoiceOption> options =
        task.options().stream()
            .map(option -> ChoiceOption.of(RichLabel.toRichText(option.text()), option.value()))
            .toList();
    return DialogFactory.showMultipleChoiceDialog(
        RichLabel.toRichText(task.taskDescription()),
        title,
        options,
        task.multipleSelection(),
        true,
        payload -> onAnswer.accept(player, selection(payload)),
        () -> {},
        player.id());
  }

  /**
   * Creates a terminal that asks a choice task until it is answered correctly.
   *
   * @param position the terminal position
   * @param title the terminal and dialog title
   * @param task the question
   * @param onCorrect receives the player who answered correctly
   * @param onWrong receives the player who answered wrongly
   * @return the terminal entity with the {@link TaskComponent} of the task
   */
  public static Entity choiceTerminal(
      final Point position,
      final String title,
      final ChoiceTask task,
      final Consumer<Entity> onCorrect,
      final Consumer<Entity> onWrong) {
    TaskComponent<Set<String>> taskComponent = new TaskComponent<>(task);
    taskComponent.onCorrect(onCorrect);
    taskComponent.onWrong(onWrong);
    Entity terminal = new Entity(title);
    terminal.add(new PositionComponent(position));
    terminal.add(new DrawComponent(new SimpleIPath(TERMINAL_SPRITE)));
    terminal.add(new CollideComponent());
    terminal.add(taskComponent);
    terminal.add(
        new InteractionComponent(
            new Interaction(
                (ignored, player) -> {
                  if (!taskComponent.isSolved()) {
                    showChoice(
                        task,
                        title,
                        player,
                        (who, selection) -> {
                          if (!taskComponent.isSolved()) {
                            taskComponent.submit(selection, who);
                          }
                        });
                  }
                })));
    return terminal;
  }

  /**
   * Registers an assignment of items to containers that is checked on every placement change.
   *
   * <p>The caller creates and places the containers and the {@link AnswerItem}s. Wrong placements
   * are ignored; once solved, the assignment is no longer checked.
   *
   * @param task the assignment
   * @param containers the task containers by container identifier; each needs an inventory
   * @param onCorrect receives the player who completed the assignment, or the returned entity if no
   *     player changed a container through its inventory dialog
   * @return the task entity, to be added with {@link Game#add(Entity)}
   */
  public static Entity buildItemAssignment(
      final AssignmentTask task,
      final Map<String, Entity> containers,
      final Consumer<Entity> onCorrect) {
    return buildItemAssignment(task, containers, onCorrect, AssignmentChecker.CONTENTS);
  }

  /**
   * Registers an assignment of items to containers with a custom check.
   *
   * @param task the assignment
   * @param containers the task containers by container identifier; each needs an inventory
   * @param onCorrect receives the player who completed the assignment, or the returned entity if no
   *     player changed a container through its inventory dialog
   * @param checker decides after each placement change whether the assignment is solved
   * @return the task entity, to be added with {@link Game#add(Entity)}
   * @see #buildItemAssignment(AssignmentTask, Map, Consumer)
   */
  public static Entity buildItemAssignment(
      final AssignmentTask task,
      final Map<String, Entity> containers,
      final Consumer<Entity> onCorrect,
      final AssignmentChecker checker) {
    if (!Game.systems().containsKey(AssignmentSystem.class)) {
      Game.add(new AssignmentSystem());
    }
    TaskComponent<Map<String, String>> taskComponent = new TaskComponent<>(task);
    taskComponent.onCorrect(onCorrect);
    taskComponent.onWrong(ignored -> {});
    Entity entity = new Entity("assignment");
    entity.add(taskComponent);
    entity.add(new AssignmentComponent(taskComponent, task, containers, checker));
    return entity;
  }

  private static Set<String> selection(final DialogResponseMessage.Payload payload) {
    return switch (payload) {
      case DialogResponseMessage.StringValue(String value) -> Set.of(value);
      case DialogResponseMessage.StringList(String[] values) -> Set.copyOf(List.of(values));
      default -> Set.of();
    };
  }
}
