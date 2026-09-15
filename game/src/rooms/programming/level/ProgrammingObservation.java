package rooms.programming.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import engine.Entity;
import engine.Game;
import engine.components.CameraComponent;
import engine.components.InputComponent;
import engine.components.PositionComponent;
import engine.game.ECSManagement;
import engine.systems.CameraSystem;
import engine.systems.DrawSystem;
import feature.canvas.CanvasGraphics;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.HeadlessDialogGroup;
import feature.utils.EntityUtils;
import java.util.LinkedHashMap;
import java.util.Map;

/** Live camera observation while the server continues executing the golem's program. */
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
    if (Game.hud().blocksGameplayInput(who)) return;
    ProgrammingTerminal.stopWalking(who);
    var ui =
        DialogFactory.show(
            DialogContext.builder()
                .type(ProgrammingTerminal.Type.OBSERVATION)
                .put(DialogContextKeys.BLOCKS_GAMEPLAY_INPUT, true)
                .put("golem", runtime.terminalState().golemId())
                .build(),
            false,
            true,
            false,
            who.id());
    ui.registerCallback(
        feature.canvas.CanvasUI.EVENT_CLOSE, ignored -> feature.hud.UIUtils.closeDialog(ui));
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
    private final boolean cinematic;
    private final Map<Entity, CameraComponent> previous = new LinkedHashMap<>();
    private final Map<InputComponent, Boolean> inputs = new LinkedHashMap<>();
    private Entity followed;
    private float previousZoom;
    private final Label label;
    private final com.badlogic.gdx.scenes.scene2d.ui.Table header;
    private float curtain = 1;
    private final ProgrammingObservationShader shader = new ProgrammingObservationShader();
    private final String shaderKey;
    private DrawSystem drawSystem;

    View(int golemId, String dialogId, boolean cinematic) {
      this.golemId = golemId;
      this.cinematic = cinematic;
      shaderKey = "programming-observation-" + dialogId;
      setSize(Game.windowWidth(), Game.windowHeight());
      header =
          ProgrammingUI.header(
              "Nox · Kellerbeobachtung",
              new com.badlogic.gdx.scenes.scene2d.ui.Table(),
              () ->
                  feature.hud.dialogs.DialogCallbackResolver.createButtonCallback(
                          dialogId, feature.canvas.CanvasUI.EVENT_CLOSE)
                      .accept(null));
      header.pad(12);
      header.setBackground(ProgrammingUI.background(ProgrammingUI.INK, false));
      header.setVisible(!cinematic);
      addActor(header);
      label = ProgrammingUI.label("", 19, ProgrammingUI.TEXT);
      label.setVisible(!cinematic);
      addActor(label);
    }

    @Override
    public void act(float delta) {
      super.act(delta);
      setSize(Game.windowWidth(), Game.windowHeight());
      setPosition(0, 0);
      header.setBounds(20, getHeight() - 88, getWidth() - 40, 68);
      label.setBounds(36, 32, Math.min(660, getWidth() - 72), 56);
      if (!cinematic) ProgrammingTerminal.state().ifPresent(state -> label.setText(state.status()));
      if (followed == null && getStage() != null) {
        Game.findEntityById(golemId)
            .ifPresent(
                golem -> {
                  if (cinematic) {
                    ECSManagement.entities()
                        .forEach(
                            e ->
                                e.fetch(InputComponent.class)
                                    .ifPresent(
                                        input -> {
                                          inputs.put(input, input.deactivateControls());
                                          input.deactivateControls(true);
                                        }));
                  }
                  ECSManagement.entities()
                      .filter(e -> e.isPresent(CameraComponent.class))
                      .toList()
                      .forEach(
                          e -> {
                            previous.put(e, e.fetch(CameraComponent.class).orElseThrow());
                            e.remove(CameraComponent.class);
                          });
                  followed = golem;
                  if (!cinematic
                      && Game.systems().get(DrawSystem.class) instanceof DrawSystem draw) {
                    drawSystem = draw;
                    draw.sceneShaders().add(shaderKey, shader, 100);
                  }
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
      if (!cinematic && followed != null && curtain < 1) shader.advance(delta, 1 - curtain);
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
        if (drawSystem != null) {
          var shaders = drawSystem.sceneShaders();
          float strength = shader.strength();
          Stage previousStage = getStage();
          if (previousStage != null) {
            previousStage.addAction(
                new TemporalAction(0.3f) {
                  @Override
                  protected void update(float percent) {
                    shader.strength(strength * (1 - percent * percent * (3 - 2 * percent)));
                  }

                  @Override
                  protected void end() {
                    shaders.remove(shaderKey);
                  }
                });
          } else {
            shaders.remove(shaderKey);
          }
          drawSystem = null;
        }
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
