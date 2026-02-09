package modules.computer.content;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import core.utils.Scene2dElementFactory;
import java.util.*;
import modules.computer.ComputerStateComponent;

public class BrowserTab extends ComputerTab {

  private static BrowserTab Instance;

  private static final String PLACEHOLDER = "Enter URL...";
  private String url = "";
  private TextField urlField;
  private Table contentTable;
  private Map<String, Actor> websites = null;

  public BrowserTab(ComputerStateComponent sharedState) {
    super(sharedState, "browser", "Browser", false);
    Instance = this;
    navigate(url);
  }

  public static Optional<BrowserTab> getInstance() {
    return Optional.ofNullable(Instance);
  }

  protected void createActors() {
    this.url = localState().browserUrl();
    this.clearChildren();
    this.add(createAdressBar()).growX().row();
    this.add(Scene2dElementFactory.createHorizontalDivider())
        .height(4)
        .pad(10, 0, 10, 0)
        .fillX()
        .row();
    contentTable = new Table();
    this.add(contentTable).grow();
  }

  private Table createAdressBar() {
    Table table = new Table();

    urlField = Scene2dElementFactory.createTextField(url);
    urlField.setMessageText(PLACEHOLDER);
    urlField.setAlignment(Align.left);
    urlField.addListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (urlField.getText().equals(PLACEHOLDER)) {
              urlField.setText("");
            }
            return super.touchDown(event, x, y, pointer, button);
          }

          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            if (keycode == Input.Keys.ENTER) {
              url = urlField.getText().trim();
              navigate(url);
              return true;
            }
            return super.keyDown(event, keycode);
          }
        });
    table.add(urlField).growX().height(50);
    Button goButton = Scene2dElementFactory.createButton("Go", "clean-green");
    goButton.addListener(
        new ClickListener(Input.Buttons.LEFT) {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            url = urlField.getText().trim();
            navigate(url);
            return super.touchDown(event, x, y, pointer, button);
          }
        });
    table.add(goButton).width(100).height(50).padLeft(10);
    return table;
  }

  public void navigate(String url) {
    this.url = url;
    localState().browserUrl(url);
    urlField.setText(url);
    contentTable.clear();

    Actor target;
    if (getWebsites().containsKey(url)) {
      target = getWebsites().get(url);
    } else {
      target = create404Page();
    }

    if(url.equals("https://cloud.gogle.com/s?id=cf4PngLVZo6bbzm")){
      ComputerStateComponent.setInfection(true);
    }

    contentTable.add(target).grow();
  }

  private Actor create404Page() {
    Table table = new Table();
    Label label =
        Scene2dElementFactory.createLabel("404 - Page Not Found", 48, new Color(0.8f, 0, 0, 1));
    table.add(label);
    return table;
  }

  private Actor createNoContentPage() {
    Table table = new Table();
    Label label =
        Scene2dElementFactory.createLabel("Enter a URL to visit a website!", 48, Color.GRAY);
    table.add(label);
    return table;
  }

  private Actor createVirusPage() {
    Table table = new Table();
    Label label = Scene2dElementFactory.createLabel("Warning: Virus Detected!", 96, Color.RED);
    label.setAlignment(Align.center);
    table.add(label).growX().row();
    Label desc =
        Scene2dElementFactory.createLabel(
            "Your computer has been infected with a virus!\nPlease restart your computer to remove the threat.",
            48,
            Color.RED);
    desc.setWrap(true);
    desc.setAlignment(Align.center);
    table.add(desc).growX().padTop(20);
    return table;
  }

  private Map<String, Actor> getWebsites() {
    if (websites == null) registerWebsites();
    return websites;
  }

  private void registerWebsites() {
    websites = new HashMap<>();
    websites.put("", createNoContentPage());
    websites.put(
        "https://www.example.com",
        Scene2dElementFactory.createLabel("Helloooo world :D", 96, Color.BLACK));
    websites.put("https://cloud.gogle.com/s?id=cf4PngLVZo6bbzm", createVirusPage());
  }

  @Override
  protected void updateState(ComputerStateComponent newStateComp) {}
}
