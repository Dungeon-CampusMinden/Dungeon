package feature.credits;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import engine.Game;
import engine.utils.BaseContainerUI;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogDesign;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.Objects;

/** Presents optional room credits through the existing ECS dialog lifecycle. */
public final class CreditsFeature {

  private static final String ROOM_ID_KEY = "creditsRoomId";
  private static final String CLOSE_BUTTON = "credits-close";

  private CreditsFeature() {}

  /** Registers the reusable credits renderer with the shared dialog factory. */
  public static void register() {
    DialogFactory.register(CreditsDialogType.CREDITS, CreditsFeature::buildDialog);
  }

  /**
   * Shows room credits when their JSON file exists, then invokes the supplied completion action.
   *
   * <p>The dialog is represented by an ECS {@link UIComponent}; the regular {@code HudSystem}
   * handles its lifetime and synchronizes it to the selected players.
   *
   * @param roomId room identifier used in {@code credits/<roomId>.json}
   * @param onComplete action to run after credits close, or immediately when the file is absent
   * @param targetEntityIds optional player IDs that should see the credits
   * @return true if a credits file was found and a dialog was queued
   */
  public static boolean showAfterGame(String roomId, Runnable onComplete, int... targetEntityIds) {
    Objects.requireNonNull(onComplete, "onComplete");
    if (CreditsRepository.load(roomId).isEmpty()) {
      onComplete.run();
      return false;
    }

    DialogContext context =
        DialogContext.builder().type(CreditsDialogType.CREDITS).put(ROOM_ID_KEY, roomId).build();
    UIComponent ui = DialogFactory.show(context, true, false, targetEntityIds);
    ui.registerCallback(
        DialogContextKeys.ON_RESUME,
        data -> {
          UIUtils.closeDialog(ui, true);
          onComplete.run();
        });
    return true;
  }

  private static Group buildDialog(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    String roomId = context.require(ROOM_ID_KEY, String.class);
    CreditsDefinition definition =
        CreditsRepository.load(roomId)
            .orElseThrow(
                () ->
                    new IllegalStateException("Credits definition disappeared for room " + roomId));
    Skin skin = UIUtils.defaultSkin();
    Dialog dialog =
        new feature.hud.dialogs.HandledDialog(
            "",
            skin,
            (ignored, button) -> {
              if (!CLOSE_BUTTON.equals(button)) return true;
              DialogCallbackResolver.createButtonCallback(
                      context.dialogId(), DialogContextKeys.ON_RESUME)
                  .accept(null);
              return true;
            });
    DialogDesign.setDialogDefaults(dialog, "");

    Table content = dialog.getContentTable();
    content
        .add(CreditsView.contentPane(definition, skin))
        .width(Math.min(760f, Game.windowWidth() - 80f))
        .height(CreditsView.dialogContentHeight(Game.windowHeight()))
        .padBottom(12)
        .row();
    dialog.button(
        new TextButton(
            engine.language.Localization.getInstance().text("main_menu.confirm"), skin, "green"),
        CLOSE_BUTTON);
    dialog.pack();
    return new BaseContainerUI(dialog);
  }
}
