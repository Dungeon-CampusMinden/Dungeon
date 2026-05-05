package modules.computer;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import core.Game;
import core.sound.Sounds;
import core.utils.Cursors;
import core.utils.FontHelper;
import core.utils.Scene2dElementFactory;
import core.utils.logging.DungeonLogger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import modules.computer.content.*;
import util.LastHourSounds;

/** Main dialog for computer interaction, containing tabs for different content. */
public class ComputerDialog extends Group {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ComputerDialog.class);
  private static final float ATTENTION_BLINK_STEP_SECONDS = 0.4f;
  private static ComputerDialog INSTANCE = null;

  private ComputerStateComponent sharedState;
  private final Skin skin;

  private Table tabArea;
  private Table contentArea;

  private String activeTab;
  private String previousTab = null;
  private final Map<String, ComputerTab> tabContentMap = new LinkedHashMap<>();
  private final BlogCommentAttentionTracker blogCommentAttentionTracker;

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
    this.blogCommentAttentionTracker =
        new BlogCommentAttentionTracker(
            BlogTab::countVisibleComments,
            () -> ComputerStateLocal.getInstance().acknowledgedBlogCommentCount(),
            count -> ComputerStateLocal.getInstance().acknowledgedBlogCommentCount(count));
    this.setSize(Game.windowWidth(), Game.windowHeight());
    ctx.requireEntity(DialogContextKeys.OWNER_ENTITY);

    // Tab restore comes first
    activeTab = ComputerStateLocal.getInstance().tab();

    // Then build tabs + content
    createActors();

    // Restore open files
    for (String s : ComputerStateLocal.getInstance().openFiles()) {
      addTab(new FileTab(sharedState, s));
    }
    restoreControlPanelTabIfNeeded();

    if (!tabContentMap.containsKey(activeTab)) {
      activeTab = tabContentMap.keySet().stream().findFirst().orElse(null);
    }

    checkVirus();

    BlogCommentAttentionTracker.AttentionChange change = blogCommentAttentionTracker.initialize(BlogTab.KEY.equals(activeTab), tabContentMap.containsKey(BlogTab.KEY));
    applyBlogAttentionChange(change);

    showContent(activeTab);
  }

  private void applyBlogAttentionChange(BlogCommentAttentionTracker.AttentionChange change) {
    if (change == BlogCommentAttentionTracker.AttentionChange.NONE
        || !tabContentMap.containsKey(BlogTab.KEY)) {
      return;
    }

    ComputerTab blogTab = tabContentMap.get(BlogTab.KEY);
    if (change == BlogCommentAttentionTracker.AttentionChange.REQUEST) {
      blogTab.requestAttention();
    } else if (change == BlogCommentAttentionTracker.AttentionChange.DISMISS) {
      blogTab.dismissAttention();
    }
    buildTabs();
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
   * status has changed, tabs will be added or removed accordingly. If the computer progress has
   * regressed (e.g. forced shutdown back to pre-login), all tabs that require a higher progress are
   * removed and the local UI is reset to the login tab so it matches the new shared state.
   *
   * @param newState the new shared state to update to
   */
  public void updateState(ComputerStateComponent newState) {
    if (sharedState.equals(newState)) return;
    ComputerProgress oldProgress = sharedState.state();
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
    if (newState.state().progress() < oldProgress.progress()) {
      rebuildTabsForRegressedProgress();
    }

    // Add the USB drive tab once the correct stick has been plugged in
    if (newState.usbInserted()
        && newState.state().hasReached(ComputerProgress.LOGGED_IN)
        && !tabContentMap.containsKey(UsbDriveTab.KEY)) {
      addTab(new UsbDriveTab(newState));
    }
    for (ComputerTab tab : tabContentMap.values()) {
      tab.setSharedState(newState);
    }
  }

  /**
   * Removes every tab that requires a higher progress than the current {@link #sharedState} and
   * rebuilds the available tabs from scratch. Switches the active tab back to the login tab so the
   * UI reflects the regressed state. The virus tab (if present) is preserved.
   */
  private void rebuildTabsForRegressedProgress() {
    List<String> keysToClose = new ArrayList<>(tabContentMap.keySet());
    for (String key : keysToClose) {
      if (key.equals(VirusTab.KEY)) continue;
      ComputerTab tab = tabContentMap.remove(key);
      if (tab != null) tab.onRemove();
    }
    ComputerStateLocal.getInstance().openFiles().clear();
    ComputerStateLocal.getInstance().controlPanelOpen(false);
    addTabsForState(ComputerProgress.ON);
    if (sharedState.state().hasReached(ComputerProgress.LOGGED_IN)) {
      addTabsForState(ComputerProgress.LOGGED_IN);
    }
    String desired = tabContentMap.containsKey(VirusTab.KEY) ? VirusTab.KEY : LoginTab.KEY;
    setActiveTab(desired);
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
      // About tab is always visible and sits to the LEFT of the Login tab,
      // so it must be added first.
      addTab(new AboutTab(sharedState));
      addTab(new LoginTab(sharedState));
    } else if (state == ComputerProgress.LOGGED_IN) {
      ComputerTab emailsTab = new EmailsTab(sharedState);
      emailsTab.dismissAttention();
      addTab(emailsTab);

      ComputerTab browserTab = new BrowserTab(sharedState);
      browserTab.dismissAttention();
      addTab(browserTab);

      addTab(new BlogTab(sharedState));
      applyBlogAttentionChange(
          blogCommentAttentionTracker.initialize(BlogTab.KEY.equals(activeTab), true));
      if (sharedState.usbInserted() && !tabContentMap.containsKey(UsbDriveTab.KEY)) {
        addTab(new UsbDriveTab(sharedState));
      }
      restoreControlPanelTabIfNeeded();
    }
  }

  private void restoreControlPanelTabIfNeeded() {
    if (!ComputerStateLocal.getInstance().controlPanelOpen()) return;
    if (!sharedState.usbInserted()) return;
    if (!sharedState.state().hasReached(ComputerProgress.LOGGED_IN)) return;
    if (tabContentMap.containsKey(ControlPanelTab.KEY)) return;
    addTab(new ControlPanelTab(sharedState));
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

    // Faint red flashing for inactive tabs that still need the user's attention.
    if (!isActive && computerTab.needsAttention()) {
      Color tint = new Color(1f, 0.55f, 0.55f, 1f);
      tab.addAction(
          Actions.forever(
              Actions.sequence(
                  Actions.delay(ATTENTION_BLINK_STEP_SECONDS),
                  Actions.color(tint, 0f),
                  Actions.delay(ATTENTION_BLINK_STEP_SECONDS),
                  Actions.color(Color.WHITE, 0f))));
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
    ComputerTab tab = tabContentMap.get(tabKey);
    tab.dismissAttention();
    if (BlogTab.KEY.equals(tabKey)) {
      blogCommentAttentionTracker.onBlogTabViewed();
    }
    contentArea.add(tab).grow();
    tab.onShow();
  }

  private void setActiveTab(String tabKey) {
    previousTab = activeTab;
    activeTab = tabKey;
    ComputerStateLocal.getInstance().tab(tabKey);
    ComputerTab newTab = tabContentMap.get(tabKey);
    if (newTab != null) newTab.dismissAttention();
    showContent(activeTab);
    buildTabs();
  }

  /**
   * Whether a tab with the given key is currently registered.
   *
   * @param tabKey the tab key to look up
   * @return {@code true} if the tab exists
   */
  public boolean containsTab(String tabKey) {
    return tabContentMap.containsKey(tabKey);
  }

  /**
   * Activates (focuses) the tab with the given key, if it exists.
   *
   * @param tabKey the tab key to activate
   */
  public void activateTab(String tabKey) {
    if (!tabContentMap.containsKey(tabKey)) return;
    setActiveTab(tabKey);
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    // Adjust size in case of window resize
    this.setSize(Game.windowWidth(), Game.windowHeight());
    float nowSeconds = System.currentTimeMillis() / 1000f;
    applyBlogAttentionChange(
        blogCommentAttentionTracker.tick(
            nowSeconds, BlogTab.KEY.equals(activeTab), tabContentMap.containsKey(BlogTab.KEY)));

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
