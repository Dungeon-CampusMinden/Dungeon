package contrib.modules.puzzle;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import contrib.components.InventoryComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.elements.RichLabel;
import core.Game;
import core.sound.CoreSounds;
import core.sound.Sounds;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.DungeonLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Scene2D UI for a {@link Puzzle}.
 *
 * <p>Loads the puzzle image, slices it into {@link Puzzle#pieceCount()} convex polygonal pieces via
 * {@link PuzzleSlicer}, draws an outline of the assembled image area as a placement guide, and
 * creates one drag-and-drop {@link Group} per piece. Only pieces whose corresponding {@link
 * PuzzlePieceItem} is currently in the interacting hero's inventory are shown. Dropping a piece
 * within {@link #SNAP_THRESHOLD_FACTOR} of its correct slot snaps it into place; piece positions
 * are written back to the {@link Puzzle} so progress survives closing/reopening the dialog. The
 * completion callback is fired exactly once as soon as every piece is snapped to its slot.
 *
 * <p>If {@link Puzzle#debug()} is true, a small panel below the playfield shows the current seed
 * and exposes "Apply" / "Random" buttons to re-slice the puzzle on the fly. Re-slicing resets all
 * placement progress.
 */
public class PuzzleUI extends Group {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(PuzzleUI.class);

  /** Maximum displayed playfield width in pixels (image is scaled down to fit). */
  private static final float MAX_PLAYFIELD_W = 600f;

  /** Maximum displayed playfield height in pixels (image is scaled down to fit). */
  private static final float MAX_PLAYFIELD_H = 500f;

  /** Side tray width as factor of the playfield width. 1.2 means "playfield width + 20%". */
  private static final float SIDE_TRAY_W_FACTOR = 1.2f;

  /**
   * Snap radius as a fraction of the smaller piece-bounding-box dimension. A piece released within
   * this distance of its correct slot anchor snaps into place.
   */
  private static final float SNAP_THRESHOLD_FACTOR = 0.05f;

  private final Puzzle puzzle;
  private final int heroId;
  private final String dialogId;

  /** Outer container that owns {@link #window} and is rebuilt on seed changes. */
  private final Table root;

  private Window window;
  private Group playfield;
  private float pfW;
  private float pfH;
  private float scale;
  private int imageW;
  private int imageH;
  private float sideTrayW;
  private RichLabel statusLabel;

  /** Textures created by this UI for piece pixmaps; disposed on rebuild. */
  private final List<Texture> ownedTextures = new ArrayList<>();

  /**
   * Creates the UI.
   *
   * @param puzzle the puzzle whose state to render and mutate
   * @param heroId the entity id of the hero that opened the dialog (used for inventory filtering)
   * @param dialogId the dialog id used for callback resolution
   */
  public PuzzleUI(Puzzle puzzle, int heroId, String dialogId) {
    this.puzzle = puzzle;
    this.heroId = heroId;
    this.dialogId = dialogId;
    setSize(Game.windowWidth(), Game.windowHeight());

    root = new Table();
    root.setFillParent(true);
    addActor(root);

    rebuild();
  }

  /** Rebuilds the entire window contents, e.g. after a seed change. */
  private void rebuild() {
    disposeOwnedTextures();
    root.clear();
    statusLabel = null;

    Skin skin = UIUtils.defaultSkin();
    window = new Window("Puzzle", skin, "no-title");
    window.setMovable(false);

    Texture full;
    try {
      full = TextureMap.instance().textureAt(puzzle.imagePath());
    } catch (RuntimeException ex) {
      LOGGER.warn("PuzzleUI: failed to load image '{}': {}", puzzle.imagePath(), ex.getMessage());
      window.add("Failed to load puzzle image.").pad(20);
      root.add(window).center();
      return;
    }

    imageW = full.getWidth();
    imageH = full.getHeight();

    if (puzzle.polygons().isEmpty() || puzzle.polygons().size() != puzzle.pieceCount()) {
      puzzle.regenerate(puzzle.seed(), imageW, imageH);
    }

    PuzzleTextureGenerator.ensureRegistered(puzzle);

    pfW = imageW;
    pfH = imageH;
    scale = Math.min(MAX_PLAYFIELD_W / pfW, MAX_PLAYFIELD_H / pfH);
    if (scale > 1f) scale = 1f;
    pfW *= scale;
    pfH *= scale;

    sideTrayW = pfW * SIDE_TRAY_W_FACTOR;
    float totalW = pfW + 2f * sideTrayW;

    playfield = new Group();
    playfield.setSize(totalW, pfH);
    playfield.setTransform(false);

    // Outline is drawn in the center column; the side trays are intentionally left empty
    // so the player can drag pieces there for organizing.
    Actor outline = makeOutline(pfW, pfH);
    outline.setPosition(sideTrayW, 0f);
    playfield.addActor(outline);

    InventoryComponent inv =
        Game.findEntityById(heroId).flatMap(e -> e.fetch(InventoryComponent.class)).orElse(null);
    if (inv == null) {
      LOGGER.warn("PuzzleUI: hero {} has no InventoryComponent", heroId);
    }

    // -- Pass 1: collect every visible (= owned) piece together with its geometry.
    List<VisiblePiece> visible = new ArrayList<>();
    List<float[]> polys = puzzle.polygons();
    for (PuzzlePieceItem piece : puzzle.pieces()) {
      if (inv == null || !inv.hasItem(piece)) {
        puzzle.unmarkPlaced(piece.pieceIndex());
        continue;
      }
      int idx = piece.pieceIndex();
      float[] poly = polys.get(idx);
      int[] bb = PuzzleSlicer.boundingBox(poly, imageW, imageH);
      int bbX = bb[0], bbY = bb[1];
      int bbW = Math.max(1, bb[2] - bb[0]);
      int bbH = Math.max(1, bb[3] - bb[1]);

      Texture pieceTex =
          TextureMap.instance()
              .textureAt(new SimpleIPath(PuzzleTextureGenerator.texturePath(puzzle.id(), idx)));

      visible.add(new VisiblePiece(idx, pieceTex, bbX, bbY, bbW, bbH));
    }
    int totalPresent = visible.size();

    // -- Pass 2: figure out a tidy initial layout for never-placed pieces. We split the
    //    visible pieces into two columns (left tray / right tray) and space them out
    //    vertically so the player gets an easy overview at first sight.
    int leftCount = (totalPresent + 1) / 2; // assign odd remainder to the left side
    int rightCount = totalPresent - leftCount;
    int leftSeen = 0;
    int rightSeen = 0;

    for (int i = 0; i < visible.size(); i++) {
      VisiblePiece vp = visible.get(i);

      Image img = new Image(new TextureRegionDrawable(new TextureRegion(vp.tex)));
      float actorW = vp.bbW * scale;
      float actorH = vp.bbH * scale;
      img.setSize(actorW, actorH);

      // Slot position (scene Y = playfield bottom-up, image Y = top-down). The whole image
      // area is shifted right by sideTrayW to make room for the left tray.
      float slotX = sideTrayW + vp.bbX * scale;
      float slotY = (imageH - (vp.bbY + vp.bbH)) * scale;

      PieceActor actor = new PieceActor(vp.idx, img, slotX, slotY, actorW, actorH);

      Optional<Vector2> stored = puzzle.getPiecePosition(vp.idx);
      Vector2 startPos;
      if (stored.isPresent()) {
        startPos = stored.get();
      } else {
        boolean leftSide = (i % 2) == 0;
        int sideIndex = leftSide ? leftSeen++ : rightSeen++;
        int sideCount = leftSide ? leftCount : rightCount;
        startPos = trayPosition(leftSide, sideIndex, sideCount, actorW, actorH);
        puzzle.setPiecePosition(vp.idx, startPos);
      }
      actor.setPosition(startPos.x, startPos.y);

      if (actor.isAtSlot(startPos)) {
        puzzle.markPlaced(vp.idx);
      } else {
        puzzle.unmarkPlaced(vp.idx);
      }

      playfield.addActor(actor);
    }

    TextButton close = new TextButton("Close", skin, "green");
    close.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent e, float x, float y) {
            DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_CLOSE)
                .accept(null);
          }
        });

    Table content = new Table(skin);
    content.add(playfield).size(pfW + 2f * sideTrayW, pfH).row();
    if (totalPresent == 0) {
      statusLabel = new RichLabel("You don't own any pieces of this puzzle yet.");
    } else {
      statusLabel = new RichLabel("[size=28]" + totalPresent + " / " + puzzle.pieceCount());
      if (puzzle.isFullySolved()) {
        statusLabel.setText("[color=green]SOLVED");
      }
    }
    content.add(statusLabel).padTop(8).row();

    if (puzzle.debug()) {
      content.add(buildDebugPanel(skin)).padTop(10).row();
    }

    content.add(close).padTop(10);

    window.add(content).pad(10);
    window.pack();

    root.add(window).center();
  }

  /**
   * Builds the debug panel: seed text field + Apply + Random buttons.
   *
   * @param skin the skin used to style the panel widgets
   * @return the assembled debug panel
   */
  private Table buildDebugPanel(Skin skin) {
    Table panel = new Table(skin);
    panel.add(new RichLabel("[size=14]Debug")).colspan(3).padBottom(4).row();
    panel.add(new RichLabel("Seed:")).padRight(6);

    final TextField seedField = new TextField(Long.toString(puzzle.seed()), skin);
    seedField.setMessageText("seed");
    panel.add(seedField).width(180f).padRight(6);

    TextButton apply = new TextButton("Apply", skin);
    apply.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            applySeed(seedField.getText());
          }
        });
    panel.add(apply).padRight(6).row();

    TextButton random = new TextButton("Random", skin);
    random.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            long s = new Random().nextLong();
            seedField.setText(Long.toString(s));
            applySeed(seedField.getText());
          }
        });
    panel.add(random).colspan(3).padTop(4);
    return panel;
  }

  private void applySeed(String text) {
    long s;
    try {
      s = Long.parseLong(text.trim());
    } catch (NumberFormatException nfe) {
      // Fallback: hash any non-numeric input so the user still gets a deterministic seed.
      s = text.hashCode();
    }
    // Drop the previously generated piece textures so the next ensureRegistered call rebuilds
    // them from the freshly sliced polygons.
    PuzzleTextureGenerator.unregister(puzzle.id(), puzzle.pieceCount());
    puzzle.regenerate(s, imageW, imageH);
    rebuild();
  }

  private void disposeOwnedTextures() {
    for (Texture t : ownedTextures) {
      try {
        t.dispose();
      } catch (RuntimeException ignored) {
        // best-effort
      }
    }
    ownedTextures.clear();
  }

  private Actor makeOutline(float w, float h) {
    int iw = Math.max(1, (int) w);
    int ih = Math.max(1, (int) h);
    Pixmap px = new Pixmap(iw, ih, Pixmap.Format.RGBA8888);
    px.setColor(0f, 0f, 0f, 0.25f);
    px.fill();
    px.setColor(1f, 1f, 1f, 0.5f);
    px.drawRectangle(0, 0, iw, ih);
    Texture tex = new Texture(px);
    px.dispose();
    ownedTextures.add(tex);
    Image bg = new Image(tex);
    bg.setSize(w, h);
    bg.setTouchable(Touchable.disabled);
    return bg;
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    setSize(Game.windowWidth(), Game.windowHeight());
  }

  /**
   * Computes the initial position of a piece in one of the side trays. Pieces are stacked
   * vertically with even spacing so the player gets an immediate overview at first sight.
   *
   * @param leftSide whether to place the piece in the left tray (otherwise right)
   * @param indexInSide 0-based index of this piece among the pieces assigned to this side
   * @param countOnSide total number of pieces assigned to this side
   * @param actorW piece actor width
   * @param actorH piece actor height
   * @return the computed tray position for the piece
   */
  private Vector2 trayPosition(
      boolean leftSide, int indexInSide, int countOnSide, float actorW, float actorH) {
    float trayCenterX = leftSide ? (sideTrayW * 0.5f) : (sideTrayW + pfW + sideTrayW * 0.5f);
    float x = trayCenterX - actorW * 0.5f;

    float available = Math.max(0f, pfH - actorH);
    float y;
    if (countOnSide <= 1) {
      y = available * 0.5f;
    } else {
      // Stride between top-left corners; first piece at the top, last piece at the bottom
      // of the tray, with equal vertical spacing in between.
      float stride = available / (countOnSide - 1);
      y = available - indexInSide * stride;
    }
    return new Vector2(x, y);
  }

  /** Lightweight record used during the two-pass piece layout in {@link #rebuild()}. */
  private static final class VisiblePiece {
    final int idx;
    final Texture tex;
    final int bbX;
    final int bbY;
    final int bbW;
    final int bbH;

    VisiblePiece(int idx, Texture tex, int bbX, int bbY, int bbW, int bbH) {
      this.idx = idx;
      this.tex = tex;
      this.bbX = bbX;
      this.bbY = bbY;
      this.bbW = bbW;
      this.bbH = bbH;
    }
  }

  /** A single draggable puzzle piece. */
  private class PieceActor extends Group {
    private final int idx;
    private final float slotX;
    private final float slotY;
    private final float w;
    private final float h;
    private final Vector2 dragOffsetStage = new Vector2();

    PieceActor(int idx, Image img, float slotX, float slotY, float w, float h) {
      this.idx = idx;
      this.slotX = slotX;
      this.slotY = slotY;
      this.w = w;
      this.h = h;
      addActor(img);
      setSize(w, h);
      setTouchable(Touchable.enabled);

      addListener(
          new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
              // Solved puzzles are read-only: do not allow dragging pieces back out.
              if (puzzle.isFullySolved()) return false;
              toFront();
              Vector2 stage = localToStageCoordinates(new Vector2(x, y));
              dragOffsetStage.set(stage.x - getX(), stage.y - getY());
              return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
              Vector2 stage = localToStageCoordinates(new Vector2(x, y));
              setPosition(stage.x - dragOffsetStage.x, stage.y - dragOffsetStage.y);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
              if (isAtSlot(new Vector2(getX(), getY()))) {
                setPosition(slotX, slotY);
                puzzle.setPiecePosition(idx, new Vector2(slotX, slotY));
                puzzle.markPlaced(idx);
              } else {
                puzzle.setPiecePosition(idx, new Vector2(getX(), getY()));
                puzzle.unmarkPlaced(idx);
              }

              if (puzzle.isFullySolved()) {
                if (statusLabel != null) {
                  statusLabel.setText("[size=28][color=green]SOLVED");
                  Sounds.play(CoreSounds.INTERFACE_BUTTON_FORWARD);
                }
                DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_COMPLETE)
                    .accept(null);
              }
            }
          });
    }

    boolean isAtSlot(Vector2 pos) {
      float dx = pos.x - slotX;
      float dy = pos.y - slotY;
      float thr = Math.min(w, h) * SNAP_THRESHOLD_FACTOR;
      return Math.sqrt(dx * dx + dy * dy) <= thr;
    }
  }
}
