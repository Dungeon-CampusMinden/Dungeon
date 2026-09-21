package rooms.systemRecovery.modules.computer.content;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.network.messages.s2c.DialogFeedbackMessage;
import engine.utils.Scene2dElementFactory;
import feature.hud.dialogs.DialogCallbackResolver;
import rooms.systemRecovery.SystemRecovery;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerCallbacks;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerTab;
import rooms.systemRecovery.util.SystemRecoveryText;
import rooms.systemRecovery.util.interpreter.TerminalInterpreterSetup;

/** Editor shown while an empty locator chip is inserted into the computer. */
public final class SearchProgramTab extends SystemRecoveryComputerTab {

  /** Stable tab key used by server feedback routing. */
  public static final String KEY = "search-program";

  private static final String INITIAL_SOURCE = SystemRecoveryText.text("computer.search-template");
  private ProgramWriteStatus writeStatus;

  /** Creates the search-program editor tab. */
  public SearchProgramTab() {
    super(KEY, SystemRecoveryText.text("computer.search-tab"));
    createActors();
  }

  @Override
  protected void createActors() {
    Table layout = new Table(skin);
    layout.top().defaults().growX();
    layout.add(createLabel(SystemRecoveryText.text("computer.search-heading"), 24)).left().row();

    TextField style = Scene2dElementFactory.createTextField(INITIAL_SOURCE);
    TextArea editor = new TextArea(INITIAL_SOURCE, new TextField.TextFieldStyle(style.getStyle()));
    editor.setPrefRows(14);
    layout.add(editor).grow().padTop(12).row();

    writeStatus = new ProgramWriteStatus();
    layout.add(writeStatus).growX().left().padTop(10).row();

    TextButton save = createButton(SystemRecoveryText.text("computer.save-search"), "green", 24);
    save.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            String source = editor.getText();
            beginWrite(source);
            DialogCallbackResolver.createButtonCallback(
                    context().dialogId(), SystemRecoveryComputerCallbacks.SEARCH_PROGRAM_SAVE)
                .accept(new DialogResponseMessage.StringValue(source));
          }
        });
    Table actions = new Table(skin);
    actions.right();
    if (SystemRecovery.debugMode()) {
      TextButton solve =
          createButton(SystemRecoveryText.text("computer.solve"), "blue-outline", 24);
      solve.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              String source = TerminalInterpreterSetup.searchRobotDebugSource();
              beginWrite(source);
              DialogCallbackResolver.createButtonCallback(
                      context().dialogId(), SystemRecoveryComputerCallbacks.SEARCH_PROGRAM_SAVE)
                  .accept(new DialogResponseMessage.StringValue(source));
            }
          });
      actions.add(solve).width(180).height(52).padRight(12);
    }
    actions.add(save).width(240).height(52);
    layout.add(actions).right().padTop(12);
    add(layout).grow();
  }

  private void beginWrite(String source) {
    writeStatus.begin(source);
  }

  /**
   * Applies the authoritative write result without opening a modal popup.
   *
   * @param serverFeedback authoritative write result
   * @param onSuccess action to run after a successful write
   */
  public void applyServerFeedback(DialogFeedbackMessage serverFeedback, Runnable onSuccess) {
    writeStatus.apply(serverFeedback, onSuccess);
  }
}
