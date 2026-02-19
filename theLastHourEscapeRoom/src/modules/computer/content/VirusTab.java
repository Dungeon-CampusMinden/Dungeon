package modules.computer.content;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.DialogContext;
import core.sound.CoreSounds;
import core.sound.Sounds;
import core.utils.Scene2dElementFactory;
import java.util.*;
import modules.computer.ComputerDialog;
import modules.computer.ComputerFactory;
import modules.computer.ComputerStateComponent;
import util.LastHourSounds;

public class VirusTab extends ComputerTab {

  public static String KEY = "virus";

  private String virusType;
  private final Map<String, String> typeToCode = Map.of(
    "Trojan", "ESCAPE",
    "Ransomware", "ESCAPE",
    "Spyware", "ESCAPE",
    "Adware", "ESCAPE",
    "Worm", "ESCAPE"
  );

  public VirusTab(ComputerStateComponent sharedState) {
    super(sharedState, "virus", "*+*+* VIRUS *+*+*", false);
  }

  protected void createActors() {
    virusType = sharedState().virusType();
    this.clearChildren();

    Label virusLabel = Scene2dElementFactory.createLabel("COMPUTER IS INFECTED", 48, Color.WHITE);
    virusLabel.setColor(Color.RED);
    virusLabel.setAlignment(Align.center);
    this.add(virusLabel).expandX().center().row();

    Label typeLabel = Scene2dElementFactory.createLabel("Virus Type: "+virusType, 20, Color.RED);
    typeLabel.setAlignment(Align.center);
    this.add(typeLabel).expandX().center().padTop(5).row();

    Label explainLabel = Scene2dElementFactory.createLabel("The system has been locked down for protection.\nA reboot with the security pass phrase is required!", 20, Color.RED);
    explainLabel.setAlignment(Align.center);
    this.add(explainLabel).expandX().center().padTop(5).row();

    TextField codeField = Scene2dElementFactory.createTextField("");
    codeField.setMessageText("Security Code");
    this.add(codeField).width(400).center().padTop(20).row();

    Button submitButton = Scene2dElementFactory.createButton("Submit", "clean-green", 24);
    submitButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        String inputCode = codeField.getText();
        if (virusType == null || inputCode.equals(typeToCode.get(virusType))) {
          virusLabel.setText("Virus Neutralized!");
          virusLabel.setColor(new Color(0, 0.8f, 0, 1));
          virusLabel.addAction(Actions.sequence(
            Actions.delay(1f),
            Actions.run(() -> {
              DialogCallbackResolver.createButtonCallback(context().dialogId(), ComputerFactory.UPDATE_STATE_KEY).accept(ComputerStateComponent.getState().withInfection(false));
            })
          ));
          Sounds.play(LastHourSounds.COMPUTER_LOGIN_SUCCESS);
        } else {
          Sounds.play(LastHourSounds.COMPUTER_LOGIN_FAILED);
        }
      }
    });
    this.add(submitButton).width(400).center().padTop(10);

    this.center();
  }



  @Override
  protected void updateState(ComputerStateComponent newStateComp) {

  }
}
