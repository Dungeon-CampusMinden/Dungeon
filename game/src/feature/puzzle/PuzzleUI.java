package feature.puzzle;

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
import engine.Game;
import engine.sound.CoreSounds;
import engine.sound.Sounds;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;
import engine.utils.logging.DungeonLogger;
import feature.components.InventoryComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.elements.RichLabel;
import feature.inventory.Item;
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
 * PuzzlePieceItem} is currently in the interacting hero's inventory are shown. Neighboring pieces
 * snap together when their relative positions match, and connected pieces can be dragged as a
 * group. Piece positions are written back to the {@link Puzzle} so progress survives
 * closing/reopening the dialog. The completion callback is fired once every piece belongs to the
 * same connected assembly, regardless of its position relative to the outline.
 *
 * <p>If {@link Puzzle#debug()} is true, a small panel below the playfield shows the current seed
 * and exposes "Apply" / "Random" buttons to re-slice the puzzle on the fly. Re-slicing resets all
 * placement progress.
 */
public class PuzzleUI extends Group {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(PuzzleUI.class);

  /** Minimum displayed length of the longer image dimension in pixels. */
  private static final float MIN_PLAYFIELD_SIZE = 300f;

  /** Maximum displayed playfield width in pixels (image is scaled down to fit). */
  private static final float MAX_PLAYFIELD_W = 600f;

  /** Maximum displayed playfield height in pixels (image is scaled down to fit). */
  private static final float MAX_PLAYFIELD_H = 500f;

  /** Side tray width as factor of the playfield width. 1.2 means "playfield width + 20%". */
  private static final float SIDE_TRAY_W_FACTOR = 1.2f;

  /**
   * Snap radius as a fraction of the smaller piece-bounding-box dimension of two neighboring
   * pieces.
   */
  private static final float SNAP_THRESHOLD_FACTOR = 0.05f;

  private static final float MIN_SNAP_DISTANCE = 8f;
  private static final float CONNECTION_EPSILON = 0.01f;

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
  private int dragPointer = -1;
  private boolean completionNotified;

  private final List<PieceActor> pieceActors = new ArrayList<>();

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
    pieceActors.clear();
    dragPointer = -1;
    completionNotified = puzzle.isFullySolved();

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

    scale = Math.max(1f, MIN_PLAYFIELD_SIZE / Math.max(imageW, imageH));
    scale = Math.min(scale, Math.min(MAX_PLAYFIELD_W / imageW, MAX_PLAYFIELD_H / imageH));
    pfW = imageW * scale;
    pfH = imageH * scale;

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
    PuzzlePieceItem[] ownedPieces = new PuzzlePieceItem[puzzle.pieceCount()];
    if (inv != null) {
      for (Item item : inv.items()) {
        if (!(item instanceof PuzzlePieceItem piece)
            || !piece.pieceFamilyId().equals(puzzle.pieceFamilyId())) {
          continue;
        }
        int index = piece.pieceIndex();
        if (index >= 0 && index < ownedPieces.length && ownedPieces[index] == null) {
          ownedPieces[index] = piece;
        }
      }
    }
    for (int idx = 0; idx < ownedPieces.length; idx++) {
      PuzzlePieceItem piece = ownedPieces[idx];
      if (piece == null) {
        continue;
      }
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

      // Reference positions determine relative alignment, not a fixed target on the playfield.
      // Scene Y is bottom-up; image Y is top-down.
      float imageX = sideTrayW + vp.bbX * scale;
      float imageY = (imageH - (vp.bbY + vp.bbH)) * scale;

      PieceActor actor = new PieceActor(vp.idx, img, imageX, imageY, actorW, actorH);

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

      pieceActors.add(actor);
      playfield.addActor(actor);
    }

    restoreConnections();
    updatePlacedState();

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
    content.add(new RichLabel("Snap matching pieces together in the image area.")).padTop(8).row();
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

  private void restoreConnections() {
    for (int i = 0; i < pieceActors.size(); i++) {
      PieceActor piece = pieceActors.get(i);
      for (int j = 0; j < i; j++) {
        PieceActor other = pieceActors.get(j);
        if (!PuzzleSlicer.shareEdge(
            puzzle.polygons().get(piece.idx), puzzle.polygons().get(other.idx))) {
          continue;
        }
        piece.neighbors.add(other);
        other.neighbors.add(piece);
        if (piece.offsetDistance2(other) <= CONNECTION_EPSILON * CONNECTION_EPSILON) {
          piece.connectTo(other);
        }
      }
    }
  }

  private void updatePlacedState() {
    for (int i = 0; i < puzzle.pieceCount(); i++) {
      puzzle.unmarkPlaced(i);
    }
    List<PieceActor> largest = List.of();
    for (PieceActor piece : pieceActors) {
      if (piece.cluster.size() > largest.size()) {
        largest = piece.cluster;
      }
    }
    if (largest.size() > 1) {
      for (PieceActor piece : largest) {
        puzzle.markPlaced(piece.idx);
      }
    }
  }

  private void notifyCompletion() {
    if (completionNotified || !puzzle.isFullySolved()) return;
    completionNotified = true;
    if (statusLabel != null) {
      statusLabel.setText("[size=28][color=green]SOLVED");
      Sounds.playUi(CoreSounds.INTERFACE_BUTTON_FORWARD);
    }
    DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_COMPLETE)
        .accept(null);
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
    // Restored connections can complete the puzzle; wait until dialog callbacks are registered.
    notifyCompletion();
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

  /** A draggable puzzle piece that moves together with its connected neighbors. */
  private class PieceActor extends Group {
    private final int idx;
    private final float imageX;
    private final float imageY;
    private final float w;
    private final float h;
    private final Vector2 dragOffset = new Vector2();
    private final List<PieceActor> neighbors = new ArrayList<>();
    private List<PieceActor> cluster = new ArrayList<>();

    PieceActor(int idx, Image img, float imageX, float imageY, float w, float h) {
      this.idx = idx;
      this.imageX = imageX;
      this.imageY = imageY;
      this.w = w;
      this.h = h;
      cluster.add(this);
      addActor(img);
      setSize(w, h);
      setTouchable(Touchable.enabled);

      addListener(
          new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
              if (puzzle.isFullySolved() || dragPointer != -1) return false;
              dragPointer = pointer;
              for (PieceActor piece : cluster) {
                piece.toFront();
              }
              dragOffset.set(x, y);
              return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
              if (pointer != dragPointer) return;
              Vector2 position = localToParentCoordinates(new Vector2(x, y));
              setClusterOffset(
                  position.x - dragOffset.x - imageX, position.y - dragOffset.y - imageY);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
              if (pointer != dragPointer) return;
              dragPointer = -1;
              if (event.isTouchFocusCancel()) return;

              snapToNeighbors();
              updatePlacedState();
              notifyCompletion();
            }
          });
    }

    @Override
    public Actor hit(float x, float y, boolean touchable) {
      if (!isVisible()
          || (touchable && getTouchable() != Touchable.enabled)
          || x < 0f
          || x >= w
          || y < 0f
          || y >= h) {
        return null;
      }
      float sourceX = (imageX - sideTrayW + x) / scale;
      float sourceY = imageH - (imageY + y) / scale;
      return PuzzleSlicer.contains(puzzle.polygons().get(idx), sourceX, sourceY) ? this : null;
    }

    private float offsetDistance2(PieceActor other) {
      float dx = (getX() - imageX) - (other.getX() - other.imageX);
      float dy = (getY() - imageY) - (other.getY() - other.imageY);
      return dx * dx + dy * dy;
    }

    private void setClusterOffset(float offsetX, float offsetY) {
      for (PieceActor piece : cluster) {
        piece.setPosition(piece.imageX + offsetX, piece.imageY + offsetY);
        puzzle.setPiecePosition(piece.idx, new Vector2(piece.getX(), piece.getY()));
      }
    }

    private void connectTo(PieceActor other) {
      if (cluster == other.cluster) return;
      float offsetX = other.getX() - other.imageX;
      float offsetY = other.getY() - other.imageY;
      cluster.addAll(other.cluster);
      for (PieceActor piece : cluster) {
        piece.cluster = cluster;
      }
      setClusterOffset(offsetX, offsetY);
    }

    private boolean overlapsImageArea() {
      // Requiring full containment would force the assembly back onto the original fixed target.
      for (PieceActor piece : cluster) {
        if (piece.getX() < sideTrayW + pfW
            && piece.getX() + piece.w > sideTrayW
            && piece.getY() < pfH
            && piece.getY() + piece.h > 0f) {
          return true;
        }
      }
      return false;
    }

    private void snapToNeighbors() {
      if (!overlapsImageArea()) return;
      PieceActor closest = null;
      float closestDistance2 = Float.POSITIVE_INFINITY;
      for (PieceActor piece : cluster) {
        for (PieceActor neighbor : piece.neighbors) {
          if (cluster == neighbor.cluster || !neighbor.overlapsImageArea()) continue;
          float threshold =
              Math.max(
                  MIN_SNAP_DISTANCE,
                  Math.min(Math.min(piece.w, piece.h), Math.min(neighbor.w, neighbor.h))
                      * SNAP_THRESHOLD_FACTOR);
          float distance2 = piece.offsetDistance2(neighbor);
          if (distance2 <= threshold * threshold && distance2 < closestDistance2) {
            closest = neighbor;
            closestDistance2 = distance2;
          }
        }
      }
      if (closest == null) return;
      connectTo(closest);

      // Join any other neighbors already aligned by this snap without shifting the assembly again.
      for (int i = 0; i < cluster.size(); i++) {
        PieceActor piece = cluster.get(i);
        for (PieceActor neighbor : piece.neighbors) {
          if (cluster != neighbor.cluster
              && neighbor.overlapsImageArea()
              && piece.offsetDistance2(neighbor) <= CONNECTION_EPSILON * CONNECTION_EPSILON) {
            connectTo(neighbor);
          }
        }
      }
    }
  }
}
