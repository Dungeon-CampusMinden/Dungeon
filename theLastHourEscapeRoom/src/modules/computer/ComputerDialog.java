package modules.computer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import core.Game;
import core.systems.CameraSystem;
import core.utils.FontHelper;
import core.utils.components.draw.TextureGenerator;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ComputerDialog extends Group {

  private ComputerStateComponent state;
  private Skin skin;

  private Table tabArea;
  private Table contentArea;

  private String activeTab = null;
  private Map<String, Actor> tabContentMap = new LinkedHashMap<>();

  public ComputerDialog(ComputerStateComponent state) {
    this.state = state;
    this.skin = UIUtils.defaultSkin();
    this.setSize(Game.windowWidth(), Game.windowHeight());
    createActors();
  }

  public void updateState(ComputerStateComponent newState) {
    this.state = newState;
    // Additional logic to update the dialog based on the new state
  }

  private void createActors(){
    Drawable bg = new TextureRegionDrawable(TextureGenerator.generateColorTexture(100, 100, new Color(1, 0, 0, 0.2f)));
    Drawable cyan = new TextureRegionDrawable(TextureGenerator.generateColorTexture(100, 100, new Color(0, 1, 1, 1.0f)));
    Drawable green = new TextureRegionDrawable(TextureGenerator.generateColorTexture(100, 100, new Color(0, 1, 0, 1.0f)));

    Table container = new Table();
    container.setFillParent(true);
    container.setBackground(bg);
    this.addActor(container);

//    container.defaults().expand().uniform().space(50);
    container.pad(100);
//    container.defaults().grow();

    tabArea = new Table(skin);
    tabArea.left().padLeft(20);
    tabArea.defaults().spaceRight(10).left();

    contentArea = new Table(skin);
    contentArea.setBackground("computer-window");
    contentArea.defaults().grow();
    contentArea.padTop(10 + contentArea.getPadTop());
    contentArea.padBottom(15);
    contentArea.padLeft(15);
    contentArea.padRight(15);
    contentArea.setTouchable(Touchable.enabled);
    contentArea.addListener(new ClickListener() {
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
//        if(clickedClose(contentArea, x, y) && ComputerFactory.computerDialogInstance != null){
//          UIUtils.closeDialog(ComputerFactory.computerDialogInstance);
//        }
        Optional<UIComponent> comp = Game.player().orElseThrow().fetch(UIComponent.class);
        comp.ifPresent(uiComponent -> {
          if(clickedClose(contentArea, x, y)){
            UIUtils.closeDialog(uiComponent);
          }
        });
        return super.touchDown(event, x, y, pointer, button);
      }
    });

    container.add(tabArea).growX().left().row();
    container.add(contentArea).grow();

    // Add a tab as example
    addTab("Login", new Image(bg));
    addTab("Emails - Very Long", new Image(cyan));
    addTab("image.png", new Image(green));
  }

  public void addTab(String name, Actor content){
    tabContentMap.put(name, content);
    if(tabContentMap.size() == 1) {
      activeTab = name;
    }
    buildTabs();
  }

  private void buildTabs(){
    tabArea.clearChildren();
    for (String tabName : tabContentMap.keySet()) {
      buildTab(tabName);
    }
  }

  private void buildTab(String name){
    Table tab = new Table(skin);
    boolean isActive = name.equals(activeTab);
    tab.setBackground(isActive ? "tab-active" : "tab-inactive");

    Label.LabelStyle labelStyle = new Label.LabelStyle();
    labelStyle.font = FontHelper.getFont(FontHelper.DEFAULT_FONT_PATH, 32, isActive ? Color.WHITE : Color.GRAY, 2, isActive ? Color.BLACK : Color.WHITE);
    Label label = new Label(name, labelStyle);
    tab.add(label).grow();
    tab.setTouchable(Touchable.enabled);
    tab.addListener(new ClickListener(Input.Buttons.LEFT) {
      @Override
      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        if (clickedClose(tab, x, y)) {
          // Close the tab
          tabContentMap.remove(name);
          if(name.equals(activeTab)){
            activeTab = tabContentMap.keySet().stream().findFirst().orElse(null);
            showContent(tabContentMap.get(activeTab));
          }
          buildTabs();
          return true;
        }

        clickedTab(name);
        return super.touchDown(event, x, y, pointer, button);
      }
    });

    tabArea.add(tab).left();
  }

  private boolean clickedClose(Actor actor, float x, float y){
    return x >= actor.getWidth() - 43 && x <= actor.getWidth() - 9 && y >= 4 && y <= 40;
  }

  private void clickedTab(String name){
    System.out.println("Clicked tab: " + name);
    activeTab = name;
    buildTabs();
    showContent(tabContentMap.get(name));
  }

  private void showContent(Actor actor){
    contentArea.clearChildren();
    contentArea.add(actor).grow();
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    // Adjust size in case of window resize
    this.setSize(Game.windowWidth(), Game.windowHeight());

    super.draw(batch, parentAlpha);
  }
}
