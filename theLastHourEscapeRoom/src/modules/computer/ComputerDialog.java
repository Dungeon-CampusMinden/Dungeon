package modules.computer;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import contrib.hud.UIUtils;
import core.Game;
import core.utils.FontHelper;
import core.utils.components.draw.TextureGenerator;
import core.utils.logging.DungeonLogger;
import modules.computer.content.ComputerTab;
import modules.computer.content.LoginMask;
import util.Scene2dElementFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ComputerDialog extends Group {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ComputerDialog.class);
  private static ComputerDialog INSTANCE = null;

  private ComputerStateComponent sharedState;
  private final Skin skin;

  private Table tabArea;
  private Table contentArea;

  private String activeTab = null;
  private final Map<String, ComputerTab> tabContentMap = new LinkedHashMap<>();

  public ComputerDialog(ComputerStateComponent state) {
    INSTANCE = this;

    this.sharedState = state;
    this.skin = UIUtils.defaultSkin();
    this.setSize(Game.windowWidth(), Game.windowHeight());

    // Tab restore comes first
    activeTab = ComputerStateLocal.Instance.tab();

    // Then build tabs + content
    createActors();
    if(!tabContentMap.containsKey(activeTab)){
      activeTab = tabContentMap.keySet().stream().findFirst().orElse(null);
    }
    showContent(activeTab);
  }

  public static Optional<ComputerDialog> getInstance() {
    if(INSTANCE.getStage() == null) return Optional.empty();
    return Optional.of(INSTANCE);
  }

  public void updateState(ComputerStateComponent newState) {
    this.sharedState = newState;
    // Additional logic to update the dialog based on the new state
  }

  private void createActors(){
    Table container = new Table();
    container.setTouchable(Touchable.enabled);
    container.addListener(new ClickListener() {
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        if(!(event.getTarget() instanceof TextField)){
          container.getStage().setKeyboardFocus(null); //Unfocus text fields when clicking outside
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
    browserArea.setBackground("computer-window-alt");
    browserArea.pad(5, 5, 5, 5);
    Button exit = Scene2dElementFactory.createExitButton();
    exit.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if(ComputerFactory.computerDialogInstance != null){
          UIUtils.closeDialog(ComputerFactory.computerDialogInstance);
        }
      }
    });
    browserArea.add(exit).height(40).width(40).expandX().right().row();

    Image divider = new Image(skin, "divider");
    browserArea.add(divider).growX().padTop(1).row();

    contentArea = new Table(skin);
    contentArea.padTop(15);
    contentArea.padBottom(15);
    contentArea.padLeft(15);
    contentArea.padRight(15);
    browserArea.add(contentArea).grow();

    container.add(tabArea).growX().left().row();
    container.add(browserArea).grow();

    // Add a tab as example
    addTab(new LoginMask());
//    addTab("Emails - Very Long", new Image(cyan), false);
//    addTab("image.png", new Image(green), true);
  }

  private void addUnfocusListener(Table container) {
    this.setTouchable(Touchable.enabled);
    this.addCaptureListener(new InputListener() {
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

  public void addTab(ComputerTab tab){
    tabContentMap.put(tab.key(), tab);
    buildTabs();
  }

  private void buildTabs(){
    tabArea.clearChildren();
    for (String tabKey : tabContentMap.keySet()) {
      buildTab(tabKey);
    }
  }

  private void buildTab(String tabKey){
    ComputerTab computerTab = tabContentMap.get(tabKey);

    Table tab = new Table(skin);
    boolean isActive = tabKey.equals(activeTab);
    tab.setBackground(isActive ? "tab-active-alt" : "tab-inactive-alt");
    tab.pad(0, 15, 0, 15);

    Label.LabelStyle labelStyle = new Label.LabelStyle();
    labelStyle.font = FontHelper.getFont(FontHelper.DEFAULT_FONT_PATH, 32, isActive ? Color.WHITE : Color.GRAY, 2, isActive ? Color.BLACK : Color.WHITE);
    Label label = new Label(computerTab.title(), labelStyle);
    tab.add(label).grow();
    tab.setTouchable(Touchable.enabled);
    tab.addListener(new ClickListener(Input.Buttons.LEFT) {
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        clickedTab(tabKey);
        return super.touchDown(event, x, y, pointer, button);
      }
    });

    if(computerTab.closeable()){
      Button exit = Scene2dElementFactory.createExitButton();
      exit.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          // Close the tab
          tabContentMap.remove(tabKey);
          if(tabKey.equals(activeTab)){
            activeTab = tabContentMap.keySet().stream().findFirst().orElse(null);
            showContent(activeTab);
          }
          buildTabs();
          event.handle();
          event.stop();
        }
      });
      tab.add(exit).height(40).width(40);
    }

    tabArea.add(tab).left().height(51).padBottom(-4);
  }

  private void clickedTab(String tabKey){
    if(tabKey.equals(activeTab)) return;
    activeTab = tabKey;
    buildTabs();
    showContent(tabKey);
  }

  private void showContent(String tabKey){
    if(tabKey == null) {
      LOGGER.error("Invalid tab name: null");
      return;
    }
    contentArea.clearChildren();
    contentArea.add(tabContentMap.get(tabKey)).grow();
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    // Adjust size in case of window resize
    this.setSize(Game.windowWidth(), Game.windowHeight());

    super.draw(batch, parentAlpha);
  }
}
