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
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.HeadlessDialogGroup;
import contrib.utils.components.Debugger;
import core.Game;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.SimpleIPath;

import java.util.Optional;

public class TrashMinigameUI extends Group {

  private static final String CRUMBLED_TEXTURE = "images/crumbled-paper.png";
  public static final String KEY_NOTE_PATH = "note_path";

  private final Table root;
  private final Table content;
  private final Group playfield;

  private String importantNotePath = "images/note-password-1.png";

  public static Group build(DialogContext dialogContext) {
    Optional<String> path = dialogContext.find(KEY_NOTE_PATH, String.class);

    if (Game.isHeadless()) {
      return new HeadlessDialogGroup();
    }
    return new TrashMinigameUI(path.orElse(null));
  }

  public TrashMinigameUI(String importantNotePath) {
    setSize(Game.windowWidth(), Game.windowHeight());
    this.importantNotePath = importantNotePath;

    // Root fills screen, only responsible for centering
    root = new Table();
    root.setFillParent(true);
    addActor(root);

    // Actual designed content
    content = new Table(UIUtils.defaultSkin());
    content.setBackground("trashcan-inside");
    root.add(content).pad(20).padTop(100).center();

    playfield = new Group();
    content.add(playfield).grow();

//    placePapers();
    addAction(Actions.run(this::placePapers));
  }

  /**
   * Place crumbled papers randomly in the playfield. They should be draggable.
   * At the very bottom there should be an important note that the player needs to uncover
   * and drag to the top of the playfield to win.
   */
  private void placePapers() {
    if(this.importantNotePath != null){
      TrashItemActor note = new TrashItemActor(
        TextureMap.instance().textureAt(new SimpleIPath(importantNotePath)), true
      );
      note.setPosition(
        (playfield.getWidth() - note.getWidth()) / 2,
        (playfield.getHeight() - note.getHeight()) / 2
      );
      playfield.addActor(note);
    }

    int numPapers = 50;
    for (int i = 0; i < numPapers; i++) {
      Texture texture = TextureMap.instance().textureAt(new SimpleIPath(CRUMBLED_TEXTURE));
      TrashItemActor paper = new TrashItemActor(texture, false);

      float x = (float) Math.random() * (playfield.getWidth() - texture.getWidth());
      float y = (float) Math.random() * (playfield.getHeight() - texture.getHeight());
      paper.setPosition(x, y);

      playfield.addActor(paper);
    };
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    resize(Game.windowWidth(), Game.windowHeight());
  }

  /** Call this from Screen#resize */
  public void resize(int width, int height) {
    setSize(width, height);
  }


  private class TrashItemActor extends Group {
    private final Image image;
    private Vector2 dragOffsetStage = new Vector2();

    public TrashItemActor(Texture texture, boolean isSpecial) {
      image = new Image(texture);
      addActor(image);

      setSize(image.getWidth(), image.getHeight());
      setOrigin(Align.center);
      setTransform(true);
      setTouchable(Touchable.enabled);
      setScale(0.3f);

      // Random variation
      if(!isSpecial){
        setScale(MathUtils.random(0.3f, 0.7f));
        setRotation(MathUtils.random(-180f, 180f));
      }

      addListener(new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
          Vector2 stagePos = localToStageCoordinates(new com.badlogic.gdx.math.Vector2(x, y));
          dragOffsetStage.set(stagePos.x - getX(), stagePos.y - getY());
          return true;
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
          Vector2 stagePos = localToStageCoordinates(new Vector2(x, y));

          float newX = stagePos.x - dragOffsetStage.x;
          float newY = stagePos.y - dragOffsetStage.y;

          if(!isSpecial){
            // Check bounds to keep within playfield
            float ownScaledWidth = getWidth() * getScaleX();
            float ownScaledHeight = getHeight() * getScaleY();
            float areaWidth = playfield.getWidth() - ownScaledWidth;
            float areaHeight = playfield.getHeight() - ownScaledHeight;
            float xOffset = 0.5f * ownScaledWidth - getWidth() * 0.5f;
            float yOffset = 0.5f * ownScaledHeight - getHeight() * 0.5f;

            newX = MathUtils.clamp(newX, xOffset, xOffset + areaWidth);
            newY = MathUtils.clamp(newY, yOffset, yOffset + areaHeight);
          }

          setPosition(newX, newY);
        }
      });
    }
  }
}
