package rooms.systemRecovery.modules.computer.content;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.hud.dialogs.DialogCallbackResolver;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerCallbacks;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerTab;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Terminal tab used to run the final system-core access script. */
public final class SystemCoreAccessTab extends SystemRecoveryComputerTab {

  /** Creates the system-core access tab. */
  public SystemCoreAccessTab() {
    super("system-core", SystemRecoveryText.text("computer.access-tab"));
    createActors();
  }

  @Override
  protected void createActors() {
    Table layout = new Table(skin);
    layout.top().defaults().growX();
    layout.add(createLabel(SystemRecoveryText.text("computer.access-ready"), 24)).left().row();
    layout
        .add(createLabel(SystemRecoveryText.text("computer.access-script"), 22))
        .left()
        .padTop(22)
        .row();

    TextButton execute =
        createButton(SystemRecoveryText.text("computer.execute-access"), "green", 24);
    execute.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            DialogCallbackResolver.createButtonCallback(
                    context().dialogId(), SystemRecoveryComputerCallbacks.SYSTEM_CORE_SCRIPT_RUN)
                .accept(new DialogResponseMessage.StringValue("access.grant();"));
          }
        });
    layout.add(execute).left().width(280).height(52).padTop(28);
    add(layout).grow();
  }
}
