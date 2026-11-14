package feature.interaction.sorting;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.SoundSpec;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.systems.EventScheduler;
import feature.tasks.TaskComponent;
import java.util.Map;
import java.util.Set;

/**
 * Factory for sorting boards, and the server side of their dialog.
 *
 * <p>Interacting with a sorting board shows the dialog {@link DialogType.DefaultTypes#SORTING} with
 * its {@link Sorting}. The dialog counts the wrong terms right away and sends the placement as
 * {@link DialogResponseMessage.IntList} to the server, which checks it again with the board's
 * {@link TaskComponent}; register reactions with {@link TaskComponent#onCorrect} and {@link
 * TaskComponent#onWrong}. Once the task is solved, the dialogs of all players close, and from then
 * on the dialog shows the solution. A room that saves its progress restores this with {@link
 * TaskComponent#setSolved}.
 */
public final class SortingFactory {

  private static final float INTERACTION_RADIUS = 1.5f;
  // The player who solved the task sees the success for a moment before the dialogs close.
  private static final long CLOSE_DELAY_MS = 1200;

  private SortingFactory() {}

  /**
   * Configures a supplied entity, e.g. a decoration, as a sorting board.
   *
   * @param entity entity to configure
   * @param sorting the task
   * @return the supplied entity, with a {@link TaskComponent} that checks the placements
   */
  public static Entity createSorting(Entity entity, Sorting sorting) {
    TaskComponent<Map<String, String>> task = new TaskComponent<>(sorting.task());
    entity.add(task);
    entity.add(
        new InteractionComponent(
            new Interaction(
                (board, player) -> show(board, sorting, task, player), INTERACTION_RADIUS)));
    return entity;
  }

  private static void show(
      Entity board, Sorting sorting, TaskComponent<Map<String, String>> task, Entity player) {
    DialogContext.Builder dialog =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.SORTING)
            .put(DialogContextKeys.ENTITY, board.id())
            .put(Sorting.SOLVED_KEY, task.isSolved());
    sorting.put(dialog);
    UIComponent ui = DialogFactory.show(dialog.build(), player.id());
    ui.registerCallback(
        DialogContextKeys.ON_CONFIRM,
        payload -> {
          if (payload instanceof DialogResponseMessage.IntList(int[] placement)) {
            submit(board, sorting, task, placement, player);
          }
        });
  }

  /**
   * Checks a placement on the server. A right placement solves the task and closes the dialogs of
   * the board shortly after.
   *
   * @param board the sorting board
   * @param sorting its task
   * @param task the component that checks the placement and runs the callbacks
   * @param placement bin of every term or {@link Sorting#NONE}
   * @param player the player who sent the placement
   */
  static void submit(
      Entity board,
      Sorting sorting,
      TaskComponent<Map<String, String>> task,
      int[] placement,
      Entity player) {
    // Another player may have solved the task a moment earlier.
    if (task.isSolved()) return;
    if (!task.submit(sorting.answer(placement), player)) {
      Game.audio().playGlobal(SoundSpec.builder("retro_event_wrong").targets(player.id()));
      return;
    }
    Game.audio().playGlobal(SoundSpec.builder("retro_event_correct"));
    EventScheduler.scheduleAction(() -> closeDialogs(board), CLOSE_DELAY_MS);
  }

  // Closes the dialog of every player who has this board open.
  private static void closeDialogs(Entity board) {
    Game.entities(Set.of(UIComponent.class))
        .flatMap(entity -> entity.fetch(UIComponent.class).stream())
        .filter(ui -> ui.dialogContext().dialogType() == DialogType.DefaultTypes.SORTING)
        .filter(
            ui ->
                ui.dialogContext()
                    .find(DialogContextKeys.ENTITY, Integer.class)
                    .filter(id -> id == board.id())
                    .isPresent())
        .toList()
        .forEach(ui -> UIUtils.closeDialog(ui, true));
  }
}
