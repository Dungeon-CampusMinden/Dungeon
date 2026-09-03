package rooms.systemRecovery.modules.computer;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import engine.Game;
import engine.sound.CoreSounds;
import engine.sound.Sounds;
import engine.utils.Cursors;
import engine.utils.FontHelper;
import engine.utils.Scene2dElementFactory;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.LinkedHashMap;
import java.util.Map;
import rooms.systemRecovery.modules.computer.content.AssistantChatTab;
import rooms.systemRecovery.modules.computer.content.TerminalTab;

/** Two-tab computer dialog for the System Recovery escape room. */
public class SystemRecoveryComputerDialog extends Group {

  private final Skin skin;
  private final DialogContext context;
  private final Map<String, SystemRecoveryComputerTab> tabs = new LinkedHashMap<>();

  private Table tabArea;
  private Table contentArea;
  private String activeTab;

  /**
   * Creates a System Recovery computer dialog.
   *
   * @param context dialog context used for close callbacks
   */
  public SystemRecoveryComputerDialog(DialogContext context) {
    this.context = context;
    this.skin = UIUtils.defaultSkin();
    setSize(Game.windowWidth(), Game.windowHeight());
    createActors();
    addTab(new AssistantChatTab());
    addTab(new TerminalTab());
    activeTab = AssistantChatTab.KEY;
    showContent(activeTab);
  }

  /**
   * Builds a renderable dialog or a headless placeholder.
   *
   * @param context dialog context
   * @return dialog group
   */
  public static Group build(DialogContext context) {
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup("System Recovery", "Computer");
    }
    return new SystemRecoveryComputerDialog(context);
  }

  private void createActors() {
    Table container = new Table();
    container.setTouchable(Touchable.enabled);
    container.setFillParent(true);
    container.pad(100);
    this.addActor(container);
    addUnfocusListener(container);

    tabArea = new Table(skin);
    tabArea.left().padLeft(20);
    tabArea.defaults().spaceRight(10).left();

    Table computerArea = new Table(skin);
    computerArea.setBackground("generic-area-depth");
    computerArea.pad(5);

    Button exit = Scene2dElementFactory.createExitButton();
    exit.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            DialogCallbackResolver.createButtonCallback(
                    context.dialogId(), DialogContextKeys.ON_CLOSE)
                .accept(null);
          }
        });
    computerArea.add(exit).height(40).width(40).expandX().right().row();

    Image divider = new Image(skin, "divider");
    computerArea.add(divider).growX().height(5).padTop(1).row();

    contentArea = new Table(skin);
    contentArea.pad(15);
    computerArea.add(contentArea).grow();

    container.add(tabArea).growX().left().row();
    container.add(computerArea).grow();
  }

  private void addUnfocusListener(Table container) {
    this.setTouchable(Touchable.enabled);
    this.addCaptureListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            Actor target = event.getTarget();
            while (target != null && !(target instanceof Widget)) {
              target = target.getParent();
            }

            if (target == null && container.getStage() != null) {
              container.getStage().setKeyboardFocus(null);
              return true;
            }
            return super.touchDown(event, x, y, pointer, button);
          }
        });
  }

  private void addTab(SystemRecoveryComputerTab tab) {
    tab.context(context);
    tabs.put(tab.key(), tab);
    buildTabs();
  }

  private void buildTabs() {
    if (tabArea == null) {
      return;
    }

    tabArea.clearChildren();
    for (String tabKey : tabs.keySet()) {
      buildTab(tabKey);
    }
  }

  private void buildTab(String tabKey) {
    SystemRecoveryComputerTab tabContent = tabs.get(tabKey);
    boolean isActive = tabKey.equals(activeTab);

    Table tab = new Table(skin);
    tab.setBackground(isActive ? "blue_square_flat" : "generic-area");
    tab.getBackground().setLeftWidth(15);
    tab.getBackground().setRightWidth(15);
    tab.setTouchable(Touchable.enabled);
    tab.setUserObject(Cursors.INTERACT);
    tab.addListener(
        new ClickListener(Input.Buttons.LEFT) {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            activateTab(tabKey);
            return super.touchDown(event, x, y, pointer, button);
          }
        });

    Label.LabelStyle labelStyle = new Label.LabelStyle();
    labelStyle.font =
        FontHelper.getFont(
            Scene2dElementFactory.FONT_PATH, 24, isActive ? Color.WHITE : Color.BLACK, 0);
    Label label = new Label(tabContent.title(), labelStyle);
    tab.add(label).pad(0, 15, 0, 15).grow();

    tabArea.add(tab).left().height(51).padBottom(-5);
  }

  private void activateTab(String tabKey) {
    if (tabKey.equals(activeTab)) {
      return;
    }
    activeTab = tabKey;
    Sounds.play(CoreSounds.INTERFACE_BUTTON_CLICKED);
    showContent(tabKey);
    buildTabs();
  }

  private void showContent(String tabKey) {
    if (contentArea == null || !tabs.containsKey(tabKey)) {
      return;
    }
    contentArea.clearChildren();
    contentArea.add(tabs.get(tabKey)).grow();
  }
}
