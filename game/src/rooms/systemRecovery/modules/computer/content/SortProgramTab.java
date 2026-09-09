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

/** Editor shown while an empty sort-program stick is inserted into the computer. */
public final class SortProgramTab extends SystemRecoveryComputerTab {

  private static final String INITIAL_SOURCE =
      "for (int i = 0; i < array.length - 1; i++) {\n"
          + "    for (int j = 0; j < array.length - 1 - i; j++) {\n"
          + "        if (____________________) {\n"
          + "            int temp = array[j];\n"
          + "            array[j] = array[j + 1];\n"
          + "            array[j + 1] = temp;\n"
          + "        }\n"
          + "    }\n"
          + "}";

  /** Creates the sort-program editor tab. */
  public SortProgramTab() {
    super("sort-program", "Sortierchip");
    createActors();
  }

  @Override
  protected void createActors() {
    Table layout = new Table(skin);
    layout.top().defaults().growX();
    layout.add(createLabel("Bubble-Sort-Bedingung einsetzen", 24)).left().row();

    TextField style = Scene2dElementFactory.createTextField(INITIAL_SOURCE);
    TextArea editor = new TextArea(INITIAL_SOURCE, new TextField.TextFieldStyle(style.getStyle()));
    editor.setPrefRows(14);
    layout.add(editor).grow().padTop(12).row();

    Label feedback = createLabel("", 18);
    layout.add(feedback).left().padTop(10).row();

    TextButton save = createButton("Auf Stick laden", "green", 24);
    save.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            DialogCallbackResolver.createButtonCallback(
                    context().dialogId(), SystemRecoveryComputerCallbacks.SORT_PROGRAM_SAVE)
                .accept(new DialogResponseMessage.StringValue(editor.getText()));
            feedback.setText("Programm wird auf den Stick geschrieben ...");
          }
        });
    layout.add(save).right().width(220).height(52).padTop(12);
    add(layout).grow();
  }
}
