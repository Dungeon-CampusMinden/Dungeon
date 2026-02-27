package modules.computer;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import core.Entity;
import core.Game;
import core.sound.Sounds;
import core.utils.Cursors;
import core.utils.FontHelper;
import core.utils.Scene2dElementFactory;
import core.utils.logging.DungeonLogger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import modules.computer.content.*;
import util.LastHourSounds;

/** Main dialog for computer interaction, containing tabs for different content. */
public class ComputerDialog extends Group {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ComputerDialog.class);
  private static ComputerDialog INSTANCE = null;

  private ComputerStateComponent sharedState;
  private final Skin skin;
  private final Entity owner;

  private Table tabArea;
  private Table contentArea;

  private String activeTab = null;
  private String previousTab = null;
  private final Map<String, ComputerTab> tabContentMap = new LinkedHashMap<>();

  private final DialogContext ctx;

  /**
   * Creates a new ComputerDialog with the given shared state and dialog context.
   *
   * @param state the shared state of the computer, which will be passed to all tabs and updated
   *     when the state changes
   * @param ctx the dialog context containing configuration for this dialog, such as the owner
   *     entity
   */
  public ComputerDialog(ComputerStateComponent state, DialogContext ctx) {
    INSTANCE = this;

    this.sharedState = state;
    this.ctx = ctx;
    this.skin = UIUtils.defaultSkin();
    this.setSize(Game.windowWidth(), Game.windowHeight());
    this.owner = ctx.requireEntity(DialogContextKeys.OWNER_ENTITY);

    // Tab restore comes first
    activeTab = ComputerStateLocal.getInstance().tab();

    // Then build tabs + content
    createActors();

    // Restore open files
    for (String s : ComputerStateLocal.getInstance().openFiles()) {
      addTab(new FileTab(sharedState, s));
    }

    if (!tabContentMap.containsKey(activeTab)) {
      activeTab = tabContentMap.keySet().stream().findFirst().orElse(null);
    }

    checkVirus();

    showContent(activeTab);
  }

  private void checkVirus() {
    if (sharedState.isInfected()) {
      addVirusTab();
    }
  }

  private void addVirusTab() {
    if (tabContentMap.containsKey(VirusTab.KEY)) return;
    ComputerTab virusTab = new VirusTab(sharedState);
    addTab(virusTab);
    setActiveTab(virusTab.key());
  }

  /**
   * Gets the singleton instance of ComputerDialog.
   *
   * @return Optional containing the ComputerDialog instance if it exists and is active, or empty if
   *     it does not exist or has no stage
   */
  public static Optional<ComputerDialog> getInstance() {
    if (INSTANCE == null || INSTANCE.getStage() == null) return Optional.empty();
    return Optional.of(INSTANCE);
  }

  /**
   * Updates the shared state of the computer and propagates changes to tabs. If the infection
   * status has changed, tabs will be added or removed accordingly.
   *
   * @param newState the new shared state to update to
   */
  public void updateState(ComputerStateComponent newState) {
    if (sharedState.equals(newState)) return;
    this.sharedState = newState;
    if (newState.isInfected()) {
      if (!tabContentMap.containsKey(VirusTab.KEY)) {
        addVirusTab();
      }
    } else {
      if (tabContentMap.containsKey(VirusTab.KEY)) {
        closeTab(VirusTab.KEY);
      }
    }
    for (ComputerTab tab : tabContentMap.values()) {
      tab.setSharedState(newState);
    }
  }

  private void createActors() {
    Table container = new Table();
    container.setTouchable(Touchable.enabled);
    container.addListener(
        new ClickListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (!(event.getTarget() instanceof TextField)) {
              container
                  .getStage()
                  .setKeyboardFocus(null); // Unfocus text fields when clicking outside
            }
            return super.touchDown(event, x, y, pointer, button);
          }
        });
    container.setFillParent(true);
    container.pad(100);
    this.addActor(container);
    addUnfocusListener(container);

    tabArea = new Table(skin);
    tabArea.left().padLeft(20);
    tabArea.defaults().spaceRight(10).left();

    Table browserArea = new Table(skin);
    browserArea.setBackground("generic-area-depth");
    browserArea.pad(5, 5, 5, 5);
    Button exit = Scene2dElementFactory.createExitButton();
    exit.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            DialogCallbackResolver.createButtonCallback(ctx.dialogId(), DialogContextKeys.ON_CLOSE)
                .accept(null);
          }
        });
    browserArea.add(exit).height(40).width(40).expandX().right().row();

    Image divider = new Image(skin, "divider");
    browserArea.add(divider).growX().height(5).padTop(1).row();

    contentArea = new Table(skin);
    contentArea.padTop(15);
    contentArea.padBottom(15);
    contentArea.padLeft(15);
    contentArea.padRight(15);
    browserArea.add(contentArea).grow();

    container.add(tabArea).growX().left().row();
    container.add(browserArea).grow();

    // only default tab is login
    addTabsForState(ComputerProgress.ON);
    if (sharedState.state() == ComputerProgress.LOGGED_IN) {
      addTabsForState(ComputerProgress.LOGGED_IN);
    }
  }

  /**
   * Adds tabs to the dialog based on the given computer state.
   *
   * @param state the computer state to add tabs for
   */
  public void addTabsForState(ComputerProgress state) {
    if (state == ComputerProgress.ON) {
      addTab(new LoginTab(sharedState));
    } else if (state == ComputerProgress.LOGGED_IN) {
      addTab(new EmailsTab(sharedState));
      addTab(new BrowserTab(sharedState));
      addTab(new BlogTab(sharedState));
    }
  }

  private void addUnfocusListener(Table container) {
    this.setTouchable(Touchable.enabled);
    this.addCaptureListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            Actor t = event.getTarget();
            while (t != null && !(t instanceof Widget)) {
              t = t.getParent();
            }

            if (t == null) {
              container.getStage().setKeyboardFocus(null);
              return true;
            }
            return super.touchDown(event, x, y, pointer, button);
          }
        });
  }

  /**
   * Adds a new tab to the dialog.
   *
   * @param tab the ComputerTab to add
   */
  public void addTab(ComputerTab tab) {
    tab.context(ctx);
    tabContentMap.put(tab.key(), tab);
    buildTabs();
  }

  /**
   * Closes the tab with the given key.
   *
   * @param tabKey the key of the tab to close
   */
  public void closeTab(String tabKey) {
    if (!tabContentMap.containsKey(tabKey)) return;
    ComputerTab tab = tabContentMap.get(tabKey);
    tab.onRemove();
    tabContentMap.remove(tabKey);
    if (tabKey.equals(activeTab)) {
      if (previousTab != null) {
        setActiveTab(previousTab);
      } else {
        String firstTab = tabContentMap.keySet().stream().findFirst().orElse(null);
        setActiveTab(firstTab);
      }
    }
    buildTabs();
  }

  /** Rebuilds the tab buttons area. */
  public void buildTabs() {
    if (tabArea == null) return;
    tabArea.clearChildren();
    for (String tabKey : tabContentMap.keySet()) {
      buildTab(tabKey);
    }
  }

  private void buildTab(String tabKey) {
    ComputerTab computerTab = tabContentMap.get(tabKey);

    Table tab = new Table(skin);
    boolean isActive = tabKey.equals(activeTab);
    tab.setBackground(isActive ? "blue_square_flat" : "generic-area");
    tab.getBackground().setLeftWidth(15);
    tab.getBackground().setRightWidth(15);

    Label.LabelStyle labelStyle = new Label.LabelStyle();
    labelStyle.font =
        FontHelper.getFont(
            Scene2dElementFactory.FONT_PATH, 24, isActive ? Color.WHITE : Color.BLACK, 0);
    Label label = new Label(computerTab.title(), labelStyle);
    tab.add(label).pad(0, 15, 0, 15).grow();
    tab.setTouchable(Touchable.enabled);
    tab.setUserObject(Cursors.INTERACT);
    if (sharedState.isInfected()) tab.setUserObject(Cursors.DISABLED);
    tab.addListener(
        new ClickListener(Input.Buttons.LEFT) {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (!(event.getTarget() instanceof Button)) {
              clickedTab(tabKey);
            }
            return super.touchDown(event, x, y, pointer, button);
          }
        });

    if (computerTab.closeable()) {
      Button exit = Scene2dElementFactory.createExitButton();
      exit.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              // Close the tab
              closeTab(tabKey);
              event.handle();
              event.stop();
            }
          });
      tab.add(exit).height(40).width(40);
    }

    tabArea.add(tab).left().height(51).padBottom(-5);
  }

  /**
   * Handles clicking on a tab.
   *
   * @param tabKey the key of the clicked tab
   */
  public void clickedTab(String tabKey) {
    if (tabKey.equals(activeTab)) return;
    if (sharedState.isInfected()) return; // Cannot switch off of the virus tab
    setActiveTab(tabKey);
    Sounds.play(LastHourSounds.COMPUTER_TAB_CLICKED, 1.2f, 0.4f);
  }

  private void showContent(String tabKey) {
    if (tabKey == null) {
      LOGGER.error("Invalid tab name: null");
      return;
    }
    contentArea.clearChildren();
    contentArea.add(tabContentMap.get(tabKey)).grow();
  }

  private void setActiveTab(String tabKey) {
    previousTab = activeTab;
    activeTab = tabKey;
    ComputerStateLocal.getInstance().tab(tabKey);
    showContent(activeTab);
    buildTabs();
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    // Adjust size in case of window resize
    this.setSize(Game.windowWidth(), Game.windowHeight());

    super.draw(batch, parentAlpha);
  }

  /**
   * Gets the shared state of the computer.
   *
   * @return the shared state of the computer
   */
  public ComputerStateComponent sharedState() {
    return sharedState;
  }
}
