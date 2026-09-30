package rooms.systemRecovery.modules.computer.content;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerTab;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Reference tab for the package scanner in transport-storage riddle four. */
public final class TransportInstructionsTab extends SystemRecoveryComputerTab {

  /** Stable tab key used by the computer dialog. */
  public static final String KEY = "transport-instructions";

  /** Creates the transport scanner reference tab. */
  public TransportInstructionsTab() {
    super(KEY, SystemRecoveryText.text("computer.transport-instructions-tab"));
    createActors();
  }

  @Override
  protected void createActors() {
    Table layout = new Table(skin);
    layout.top().defaults().growX();
    layout
        .add(createLabel(SystemRecoveryText.text("computer.transport-instructions-heading"), 24))
        .left()
        .row();

    Label instructions =
        createLabel(SystemRecoveryText.text("computer.transport-instructions"), 22);
    instructions.setWrap(true);
    layout.add(instructions).grow().top().left().padTop(18);
    add(layout).grow();
  }
}
