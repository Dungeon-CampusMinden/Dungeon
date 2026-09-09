package rooms.programming.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import engine.Entity;
import engine.Game;
import engine.components.CameraComponent;
import engine.components.InputComponent;
import engine.components.PositionComponent;
import engine.game.ECSManagement;
import engine.systems.CameraSystem;
import engine.utils.Scene2dElementFactory;
import feature.canvas.CanvasGraphics;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.HeadlessDialogGroup;
import feature.utils.EntityUtils;
import java.util.LinkedHashMap;
import java.util.Map;

/** Live camera observation; the dialog's existing input gate keeps the hero stationary. */
public final class ProgrammingObservation {
  private ProgrammingObservation() {}

  static void register() {
    DialogFactory.register(
        ProgrammingTerminal.Type.OBSERVATION,
        context ->
            Game.isHeadless()
                ? new HeadlessDialogGroup()
                : new View(
                    context.require("golem", Integer.class),
                    context.dialogId(),
                    context.find("cinematic", Boolean.class).orElse(false)));
  }

  static void open(Entity who, ProgrammingGolemRuntime runtime) {
    ProgrammingTerminal.stopWalking(who);
    var ui =
        DialogFactory.show(
            DialogContext.builder()
                .type(ProgrammingTerminal.Type.OBSERVATION)
                .put("golem", runtime.terminalState().golemId())
                .build(),
            true,
            true,
            false,
            who.id());
    ui.registerCallback("close", payload -> UIUtils.closeDialog(ui));
  }

  /**
   * Uses the same read-only camera for the short, server-timed mechanical accident.
   *
   * @param focusId entity followed by every player's camera
   * @return dialog closed by the server when the sequence finishes
   */
  static UIComponent sequence(int focusId) {
    return DialogFactory.show(
        DialogContext.builder()
            .type(ProgrammingTerminal.Type.OBSERVATION)
            .put("golem", focusId)
            .put("cinematic", true)
            .build(),
        false,
        false,
        false,
        Game.allPlayers().mapToInt(Entity::id).toArray());
  }

  private static final class View extends Group {
    private final int golemId;
    private final String dialogId;
    private final boolean cinematic;
    private final Map<Entity, CameraComponent> previous = new LinkedHashMap<>();
    private final Map<InputComponent, Boolean> inputs = new LinkedHashMap<>();
    private Entity followed;
    private float previousZoom;
    private final Label label;
    private float curtain = 1;

    View(int golemId, String dialogId, boolean cinematic) {
      this.golemId = golemId;
      this.dialogId = dialogId;
      this.cinematic = cinematic;
      setSize(Game.windowWidth(), Game.windowHeight());
      label =
          Scene2dElementFactory.createLabel(
              cinematic ? "" : "Beobachte: Nox · Keller - ESC zum Verlassen", 22, Color.WHITE);
      label.setAlignment(Align.center);
      addActor(label);
    }

    @Override
    public void act(float delta) {
      super.act(delta);
      setSize(Game.windowWidth(), Game.windowHeight());
      setPosition(0, 0);
      label.setBounds(0, getHeight() - 55, getWidth(), 40);
      if (!cinematic && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
        DialogCallbackResolver.createButtonCallback(dialogId, "close").accept(null);
      if (followed == null && getStage() != null) {
        Game.findEntityById(golemId)
            .ifPresent(
                golem -> {
                  ECSManagement.entities()
                      .forEach(
                          e ->
                              e.fetch(InputComponent.class)
                                  .ifPresent(
                                      input -> {
                                        inputs.put(input, input.deactivateControls());
                                        input.deactivateControls(true);
                                      }));
                  ECSManagement.entities()
                      .filter(e -> e.isPresent(CameraComponent.class))
                      .toList()
                      .forEach(
                          e -> {
                            previous.put(e, e.fetch(CameraComponent.class).orElseThrow());
                            e.remove(CameraComponent.class);
                          });
                  followed = golem;
                  previousZoom = CameraSystem.camera().zoom;
                  CameraSystem.camera().zoom = previousZoom * 1.2f;
                  golem.add(new CameraComponent());
                });
      }
      if (followed != null)
        followed
            .fetch(PositionComponent.class)
            .ifPresent(
                position -> {
                  var camera = CameraSystem.camera();
                  var focus = EntityUtils.getPosition(followed);
                  if (cinematic
                      && (Math.abs(camera.position.x - focus.x()) > 20
                          || Math.abs(camera.position.y - focus.y()) > 20)) {
                    curtain = 1;
                    camera.position.set(focus.x(), focus.y(), 0);
                    camera.update();
                  }
                  if (Math.abs(camera.position.x - focus.x()) < 1
                      && Math.abs(camera.position.y - focus.y()) < 1)
                    curtain = Math.max(0, curtain - delta * 4);
                });
    }

    @Override
    public void draw(Batch batch, float alpha) {
      if (curtain > 0)
        CanvasGraphics.fill(
            batch, Color.BLACK, curtain * alpha, getX(), getY(), getWidth(), getHeight());
      super.draw(batch, alpha);
    }

    @Override
    protected void setStage(Stage stage) {
      if (stage == null) {
        if (followed != null) {
          followed.remove(CameraComponent.class);
          CameraSystem.camera().zoom = previousZoom;
        }
        previous.forEach(Entity::add);
        previous.clear();
        inputs.forEach(InputComponent::deactivateControls);
        inputs.clear();
        followed = null;
      }
      super.setStage(stage);
    }
  }
}
