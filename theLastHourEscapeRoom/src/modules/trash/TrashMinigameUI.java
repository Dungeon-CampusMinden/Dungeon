package modules.trash;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.HeadlessDialogGroup;
import core.Game;
import core.sound.Sounds;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.SimpleIPath;
import java.util.Optional;
import util.LastHourSounds;

/**
 * A minigame where the player has to find an important note (or item) hidden under crumbled papers
 * in a trashcan.
 *
 * <p>The minigame is "won" by clicking the special (non-crumpled) actor. On a win the actor is
 * raised to the front, briefly animated, a success sound is played, and after a short delay the
 * server is notified via {@link DialogCallbackResolver} using the configured callback key. The
 * server-side handler is responsible for awarding any item and closing the dialog.
 */
public class TrashMinigameUI extends Group {

  private static final String CRUMBLED_TEXTURE = "images/crumbled-paper.png";

  /** Key for the special (note or item) world-texture path for the Dialog API. */
  public static final String KEY_NOTE_PATH = "note_path";

  /** Key for the paper count for the Dialog API. */
  public static final String KEY_PAPER_COUNT = "paper_count";

  /** Key for the callback name fired on the server when the player wins. */
  public static final String KEY_CALLBACK_KEY = "callback_key";

  /** Default callback key used if {@link #KEY_CALLBACK_KEY} is not set. */
  public static final String DEFAULT_CALLBACK_KEY = "onTrashWin";

  private static final float WIN_SCALE_DURATION = 1.2f;
  private static final float WIN_HOLD_BEFORE_CALLBACK = 2.0f;

  /**
   * Minimum on-screen size (in stage units, before this actor's own scale) of the special actor's
   * underlying texture.
   */
  private static final float MIN_SPECIAL_RENDER_SIZE = 96f;

  private final Table root;
  private final Table content;
  private final Group playfield;

  private final String importantNotePath;
  private final int paperCount;
  private final String dialogId;
  private final String callbackKey;

  /**
   * Builds the TrashMinigameUI from the given DialogContext.
   *
   * @param dialogContext the DialogContext
   * @return a new instance of TrashMinigameUI, or a HeadlessDialogGroup if running in headless mode
   */
  public static Group build(DialogContext dialogContext) {
    Optional<String> path = dialogContext.find(KEY_NOTE_PATH, String.class);
    Optional<Integer> paperCount = dialogContext.find(KEY_PAPER_COUNT, Integer.class);
    String callbackKey =
        dialogContext.find(KEY_CALLBACK_KEY, String.class).orElse(DEFAULT_CALLBACK_KEY);

    if (Game.isHeadless()) {
      return new HeadlessDialogGroup();
    }
    return new TrashMinigameUI(
        path.orElse(null), paperCount.orElse(50), dialogContext.dialogId(), callbackKey);
  }

  /**
   * Creates a new TrashMinigameUI.
   *
   * @param importantNotePath the path to the special (note/item) texture, or null if there is no
   *     winnable special actor
   * @param paperCount the number of crumbled papers to place in the playfield
   * @param dialogId the id of the surrounding dialog (used to route the win callback)
   * @param callbackKey the callback key registered on the server for the win event
   */
  public TrashMinigameUI(
      String importantNotePath, int paperCount, String dialogId, String callbackKey) {
    setSize(Game.windowWidth(), Game.windowHeight());
    this.importantNotePath = importantNotePath;
    this.paperCount = paperCount;
    this.dialogId = dialogId;
    this.callbackKey = callbackKey;

    root = new Table();
    root.setFillParent(true);
    addActor(root);

    content = new Table(UIUtils.defaultSkin());
    content.setBackground("trashcan-inside");
    root.add(content).pad(20).padTop(100).center();

    playfield = new Group();
    content.add(playfield).grow();

    addAction(Actions.run(this::placePapers));
  }

  /**
   * Place crumbled papers randomly in the playfield. The special item is added first so it is
   * visually below all crumpled papers; the player has to drag papers away to uncover it.
   */
  private void placePapers() {
    if (this.importantNotePath != null) {
      TrashItemActor note =
          new TrashItemActor(
              TextureMap.instance().textureAt(new SimpleIPath(importantNotePath)), true);
      note.setPosition(
          (playfield.getWidth() - note.getWidth()) / 2,
          (playfield.getHeight() - note.getHeight()) / 2);
      playfield.addActor(note);
    }

    for (int i = 0; i < paperCount; i++) {
      Texture texture = TextureMap.instance().textureAt(new SimpleIPath(CRUMBLED_TEXTURE));
      TrashItemActor paper = new TrashItemActor(texture, false);

      float x = (float) Math.random() * (playfield.getWidth() - texture.getWidth());
      float y = (float) Math.random() * (playfield.getHeight() - texture.getHeight());
      paper.setPosition(x, y);

      playfield.addActor(paper);
    }
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    resize(Game.windowWidth(), Game.windowHeight());
  }

  /**
   * Resize the UI to fill the screen.
   *
   * @param width the new width of the window
   * @param height the new height of the window
   */
  public void resize(int width, int height) {
    setSize(width, height);
  }

  private class TrashItemActor extends Group {
    private final Image image;
    private final boolean isSpecial;
    private final Vector2 dragOffsetStage = new Vector2();
    private boolean won = false;

    /**
     * Creates a new TrashItemActor with the given texture.
     *
     * @param texture the texture to use for this item
     * @param isSpecial whether this item is the important note (true) or a regular crumbled paper
     *     (false).
     */
    public TrashItemActor(Texture texture, boolean isSpecial) {
      this.isSpecial = isSpecial;
      image = new Image(texture);
      addActor(image);

      setSize(image.getWidth(), image.getHeight());
      setOrigin(Align.center);
      setTransform(true);
      setTouchable(Touchable.enabled);

      if (isSpecial) {
        // Ensure the special actor reaches a sensible minimum on-screen size, scaling up small
        // item icons (e.g. 16x16 or 32x32 USB sticks) so they remain readable and clickable.
        float baseScale = 0.3f;
        float minDim = Math.min(image.getWidth(), image.getHeight());
        float minScale = MIN_SPECIAL_RENDER_SIZE / Math.max(minDim, 1f);
        setScale(Math.max(baseScale, minScale));
      } else {
        setScale(MathUtils.random(0.3f, 0.7f));
        setRotation(MathUtils.random(-180f, 180f));
      }

      addListener(
          new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
              if (isSpecial) {
                triggerWin();
                return true;
              }
              Vector2 stagePos = localToStageCoordinates(new com.badlogic.gdx.math.Vector2(x, y));
              dragOffsetStage.set(stagePos.x - getX(), stagePos.y - getY());
              return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
              if (isSpecial) return;
              Vector2 stagePos = localToStageCoordinates(new Vector2(x, y));

              float newX = stagePos.x - dragOffsetStage.x;
              float newY = stagePos.y - dragOffsetStage.y;

              float ownScaledWidth = getWidth() * getScaleX();
              float ownScaledHeight = getHeight() * getScaleY();
              float areaWidth = playfield.getWidth() - ownScaledWidth;
              float areaHeight = playfield.getHeight() - ownScaledHeight;
              float xOffset = 0.5f * ownScaledWidth - getWidth() * 0.5f;
              float yOffset = 0.5f * ownScaledHeight - getHeight() * 0.5f;

              newX = MathUtils.clamp(newX, xOffset, xOffset + areaWidth);
              newY = MathUtils.clamp(newY, yOffset, yOffset + areaHeight);

              setPosition(newX, newY);
            }
          });
    }

    /**
     * Plays win feedback (raise to front, scale animation and sound), then notifies the server via
     * the dialog callback after a short delay. The dialog is closed by the server-side handler.
     */
    private void triggerWin() {
      if (won) return;
      won = true;
      setTouchable(Touchable.disabled);
      toFront();
      Sounds.play(LastHourSounds.TRASH_MINIGAME_WIN);
      addAction(
          Actions.sequence(
              Actions.scaleTo(getScaleX() * 1.6f, getScaleY() * 1.6f, WIN_SCALE_DURATION),
              Actions.delay(WIN_HOLD_BEFORE_CALLBACK),
              Actions.run(
                  () ->
                      DialogCallbackResolver.createButtonCallback(dialogId, callbackKey)
                          .accept(null))));
    }
  }
}
