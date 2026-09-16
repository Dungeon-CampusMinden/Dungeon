package rooms.systemRecovery.modules.computer.content;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.Scene2dElementFactory;
import feature.hud.dialogs.DialogCallbackResolver;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerCallbacks;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerTab;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Editor shown while an empty sort-program stick is inserted into the computer. */
public final class SortProgramTab extends SystemRecoveryComputerTab {

  private static final String INITIAL_SOURCE = SystemRecoveryText.text("computer.sort-template");

  /** Creates the sort-program editor tab. */
  public SortProgramTab() {
    super("sort-program", SystemRecoveryText.text("computer.sort-tab"));
    createActors();
  }

  @Override
  protected void createActors() {
    Table layout = new Table(skin);
    layout.top().defaults().growX();
    layout.add(createLabel(SystemRecoveryText.text("computer.sort-heading"), 24)).left().row();

    TextField style = Scene2dElementFactory.createTextField(INITIAL_SOURCE);
    TextArea editor = new TextArea(INITIAL_SOURCE, new TextField.TextFieldStyle(style.getStyle()));
    editor.setPrefRows(14);
    layout.add(editor).grow().padTop(12).row();

    Label feedback = createLabel("", 18);
    layout.add(feedback).left().padTop(10).row();

    TextButton save = createButton(SystemRecoveryText.text("computer.save-sort"), "green", 24);
    save.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            DialogCallbackResolver.createButtonCallback(
                    context().dialogId(), SystemRecoveryComputerCallbacks.SORT_PROGRAM_SAVE)
                .accept(new DialogResponseMessage.StringValue(editor.getText()));
            feedback.setText(SystemRecoveryText.text("computer.saving"));
          }
        });
    layout.add(save).right().width(220).height(52).padTop(12);
    add(layout).grow();
  }
}
