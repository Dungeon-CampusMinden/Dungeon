package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogFactory;
import feature.prefabs.ActionContext;
import feature.prefabs.ArtifactSlot;
import feature.prefabs.ArtifactType;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabAction;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEvent;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Server-side dialog script shown whenever {@link #SHOW_DIALOG} runs, e.g. when a player entered a
 * {@link TriggerPrefab trigger} or interacted with a decoration.
 *
 * <p>The dialog is shown either to the player that caused the triggering event or to every player.
 * If the event carries no player, it is shown to every player.
 */
public final class DialogPrefab extends Prefab {

  /**
   * Raised on the server when a player finished or closed the dialog, with the player as payload.
   */
  public static final PrefabEvent<Entity> DIALOG_FINISHED =
      PrefabEvent.withPlayer("dialog-finished", "Dialog Finished");

  /** Shows the dialog to its audience. */
  public static final PrefabAction SHOW_DIALOG =
      PrefabAction.forPlayer("show-dialog", "Show Dialog", DialogPrefab::show);

  private static final String TYPE = "dialog";
  private static final String DISPLAY_NAME = "Dialog";
  // Dialogs still shown by a spawned instance, closed without an event when it despawns.
  private static final SpawnState<List<UIComponent>> OPEN = new SpawnState<>();

  public static final PrefabProperty<String> TEXT =
      PrefabProperty.string("text", "Dialog Script", "Hello.", value -> !value.isBlank());
  public static final PrefabProperty<Boolean> TRANSLATE =
      PrefabProperty.bool("translate", "Translate", false);

  /** Who sees the dialog. */
  public static final PrefabProperty<String> AUDIENCE =
      PrefabAudience.property("Show For", PrefabAudience.TRIGGERING_PLAYER);

  /** Values shown in the dialog script through {@code {alias}} placeholders. */
  public static final ArtifactSlot<String> VALUES =
      ArtifactSlot.multiple("values", "Placeholder Values", ArtifactType.TEXT)
          .withPlaceholdersIn(TEXT);

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(TEXT, TRANSLATE, AUDIENCE);

  /** Creates the dialog definition. */
  public DialogPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored dialog.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public DialogPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  /**
   * Registers a listener called on the server whenever a player finished or closed the dialog of
   * this instance.
   *
   * @param listener receives the player that read the dialog
   */
  public void onDialogFinished(Consumer<Entity> listener) {
    listen(DIALOG_FINISHED, listener);
  }

  @Override
  protected List<PrefabPort> declaredPorts() {
    return List.of(SHOW_DIALOG, DIALOG_FINISHED, VALUES);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    OPEN.start(context.level(), instance.name(), new ArrayList<>());
    return List.of();
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    OPEN.end(context.level(), instance.name()).ifPresent(PrefabDialogs::closeAll);
  }

  @Override
  public Optional<String> editorSummary(PrefabInstance instance) {
    return Optional.of(PrefabDialogs.preview(value(instance, TEXT)));
  }

  private static void show(ActionContext context) {
    PrefabCreationContext creation = context.creationContext();
    PrefabInstance instance = context.instance();
    List<UIComponent> open = OPEN.get(creation.level(), instance.name()).orElse(null);
    if (open == null) return;
    for (Entity player : PrefabAudience.players(context, AUDIENCE)) {
      // Finishing and closing both report, removing the dialog makes sure the event fires once.
      UIComponent[] shown = new UIComponent[1];
      shown[0] =
          DialogFactory.showDialogDialog(
              instance.get(TEXT),
              instance.get(TRANSLATE),
              PrefabDialogs.placeholders(input(creation, instance, VALUES)),
              () -> {
                if (open.remove(shown[0])) fire(creation, instance, DIALOG_FINISHED, player);
              },
              player.id());
      open.add(shown[0]);
    }
  }
}
