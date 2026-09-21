package rooms.programming.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import engine.Entity;
import engine.Game;
import engine.components.CameraComponent;
import engine.components.DrawComponent;
import engine.components.InputComponent;
import engine.components.PositionComponent;
import engine.game.ECSManagement;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.systems.CameraSystem;
import engine.systems.DrawSystem;
import engine.utils.Cursors;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.components.draw.shader.MagicBallShader;
import feature.canvas.CanvasGraphics;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.HeadlessDialogGroup;
import feature.utils.EntityUtils;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

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
    ProgrammingProgress.interaction("loop-observation", "open", who);
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
        feature.canvas.CanvasUI.EVENT_CLOSE,
        ignored -> {
          ProgrammingProgress.interaction("loop-observation", "close", who);
          feature.hud.UIUtils.closeDialog(ui);
        });
    ui.registerCallback(
        "torch",
        payload -> {
          if (Game.isMultiplayerClient() || !(payload instanceof DialogResponseMessage.IntValue id))
            return;
          Game.levelEntities()
              .filter(torch -> torch.id() == id.value() && cellarTorch(torch))
              .findFirst()
              .ifPresent(torch -> ProgrammingProps.toggleTorch(torch, who));
        });
  }

  private static boolean cellarTorch(Entity entity) {
    return entity.name().startsWith("programming-prop-torch-cellar-");
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
    private final Actor torches;
    private final com.badlogic.gdx.scenes.scene2d.ui.Table header;
    private float curtain = 1;
    private final MagicBallShader shader =
        new MagicBallShader(
                "images/shader-bg.png",
                .84f,
                new com.badlogic.gdx.math.Vector2(-.018f, 0),
                Color.valueOf("95b8f0"),
                .6f,
                Color.valueOf("BCDFFFFF"))
            .curvedEdgeWidth(1);
    private final String shaderKey;
    private DrawSystem drawSystem;

    View(int golemId, String dialogId, boolean cinematic) {
      setUserObject(engine.utils.Cursors.DEFAULT);
      this.golemId = golemId;
      this.cinematic = cinematic;
      shaderKey = "programming-observation-" + dialogId;
      setSize(Game.windowWidth(), Game.windowHeight());
      torches =
          new Actor() {
            @Override
            public Actor hit(float x, float y, boolean touchable) {
              return !cinematic && torchAt(x, y).isPresent() ? this : null;
            }
          };
      torches.setUserObject(Cursors.INTERACT);
      torches.addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              torchAt(x, y)
                  .ifPresent(
                      torch ->
                          DialogCallbackResolver.createButtonCallback(dialogId, "torch")
                              .accept(new DialogResponseMessage.IntValue(torch.id())));
            }
          });
      addActor(torches);
      header =
          ProgrammingUI.header(
              "Nox · Kellerbeobachtung",
              torchHint(),
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
      torches.setSize(getWidth(), getHeight());
      float headerHeight = getWidth() < 1180 ? 112 : 68;
      header.setBounds(20, getHeight() - headerHeight - 20, getWidth() - 40, headerHeight);
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
      if (!cinematic) {
        float width = Math.min(1, getHeight() / getWidth());
        float height = Math.min(1, getWidth() / getHeight());
        shader.textureRegion(new Rectangle(width, height, (1 - width) / 2, (1 - height) / 2));
        updateCurvedEdge();
      }
    }

    private static com.badlogic.gdx.scenes.scene2d.ui.Table torchHint() {
      var hint = new com.badlogic.gdx.scenes.scene2d.ui.Table();
      hint.add(ProgrammingUI.label("Fackeln anklicken: an / aus", 17, ProgrammingUI.TEXT))
          .width(260);
      return hint;
    }

    /**
     * Repeats magic_ball.frag's sampling transform to pick the displayed torch, including the rim.
     *
     * @param x horizontal position within the observation view
     * @param y vertical position within the observation view
     * @return torch at the displayed position, or empty when none can be picked
     */
    private Optional<Entity> torchAt(float x, float y) {
      if (followed == null || curtain > 0 || getWidth() <= 0 || getHeight() <= 0)
        return Optional.empty();
      float shortSide = Math.min(getWidth(), getHeight());
      var offset = shader.ballOffset();
      float radius = shader.ballSize() * shortSide / 2;
      double sx = (x - (.5f + offset.x) * getWidth()) / radius;
      double sy = (y - (.5f + offset.y) * getHeight()) / radius;
      double distance = Math.hypot(sx, sy);
      if (distance >= 1) return Optional.empty();
      double z = Math.sqrt(1 - distance * distance);
      double u = Math.atan2(sx, z) / Math.PI + .5;
      double v = Math.asin(Math.clamp(sy, -.92, .92)) / (2 * Math.asin(.92)) + .5;
      double edge = Math.clamp((1 - distance) * radius / shader.curvedEdgeWidth(), 0, 1);
      double flatWeight = edge * edge * (3 - 2 * edge);
      u += (sx * .5 + .5 - u) * flatWeight;
      v += (sy * .5 + .5 - v) * flatWeight;
      var region = shader.textureRegion();
      if (region == null) return Optional.empty();
      u = region.x() + u * region.width();
      v = region.y() + v * region.height();
      var camera = CameraSystem.camera();
      var world =
          new Point(
              (float) (camera.position.x + (u - .5) * camera.viewportWidth * camera.zoom),
              (float) (camera.position.y + (v - .5) * camera.viewportHeight * camera.zoom));
      return EntityUtils.findEntityAtPoint(
          world, Game.levelEntities().filter(ProgrammingObservation::cellarTorch));
    }

    /** Keeps Nox's complete sprite and four surrounding tiles inside the flat view. */
    private void updateCurvedEdge() {
      if (followed == null) return;
      var position = followed.fetch(PositionComponent.class).orElseThrow();
      var draw = followed.fetch(DrawComponent.class).orElseThrow();
      var camera = CameraSystem.camera();
      var size = draw.size().scale(position.scale());
      float left = position.position().x() - camera.position.x;
      float bottom = position.position().y() - camera.position.y;
      float horizontal = Math.max(Math.abs(left), Math.abs(left + size.x()));
      float vertical = Math.max(Math.abs(bottom), Math.abs(bottom + size.y()));
      float flatRadiusTiles = (float) Math.hypot(horizontal, vertical) + 4;
      float diameterPixels = shader.ballSize() * Math.min(getWidth(), getHeight());
      float visibleTiles = Math.min(camera.viewportWidth, camera.viewportHeight) * camera.zoom;
      float flatRadiusPixels = flatRadiusTiles * diameterPixels / visibleTiles;
      // A positive width keeps the center flat even when the protected area fills the ball.
      shader.curvedEdgeWidth(Math.max(1, diameterPixels / 2 - flatRadiusPixels));
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
          drawSystem.sceneShaders().remove(shaderKey);
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
