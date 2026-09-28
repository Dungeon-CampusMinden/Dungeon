package rooms.systemRecovery.modules.computer.content;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerTab;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Reference tab for sending a computed value to an in-room display. */
public final class DisplayInstructionsTab extends SystemRecoveryComputerTab {

  /** Stable tab key used by the computer dialog. */
  public static final String KEY = "display-instructions";

  /** Creates the display reference tab. */
  public DisplayInstructionsTab() {
    super(KEY, SystemRecoveryText.text("computer.display-instructions-tab"));
    createActors();
  }

  @Override
  protected void createActors() {
    Table layout = new Table(skin);
    layout.top().defaults().growX();
    layout
        .add(createLabel(SystemRecoveryText.text("computer.display-instructions-heading"), 24))
        .left()
        .row();

    Label instructions = createLabel(SystemRecoveryText.text("computer.display-instructions"), 22);
    instructions.setWrap(true);
    layout.add(instructions).grow().top().left().padTop(18);
    add(layout).grow();
  }
}
