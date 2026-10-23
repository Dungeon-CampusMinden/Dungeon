package feature.leveleditor.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import engine.utils.Scene2dElementFactory;
import feature.hud.UIUtils;
import feature.leveleditor.PrefabCatalog;
import feature.prefabs.Prefab;
import java.util.List;

/**
 * Panel showing a row of prefab icons, which are dragged into the level to place new prefab
 * instances.
 *
 * <p>Only the prefabs of one category are shown at a time. The button at the left side of the panel
 * opens a list of all categories, and arrow buttons page through categories that are wider than the
 * panel.
 */
public class PrefabPalette extends Table {

  private static final String DEFAULT_HINT =
      "Drag a prefab into the level to place it. Drop dialogs, sounds and logic onto the prefab"
          + " that triggers them.";
  private static final float ITEM_SIZE = 56f;
  private static final float ITEM_PAD = 3f;
  private static final float ITEM_CELL = ITEM_SIZE + 2 * ITEM_PAD;
  private static final float IMAGE_SIZE = 38f;
  private static final float CATEGORY_WIDTH = 110f;
  private static final float ARROW_WIDTH = 28f;
  private static final float SCREEN_MARGIN = 24f;
  private static final int MAX_ITEMS_PER_PAGE = 10;
  private static final int CAPTION_FONT_SIZE = 14;

  // Remembered between mode switches, which recreate the palette.
  private static String lastCategory;

  private final Listener listener;
  private final List<PrefabCatalog.Category> categories = PrefabCatalog.categories();
  private final DragAndDrop dragAndDrop = new DragAndDrop();
  private final Label caption =
      Scene2dElementFactory.createLabel(
          DEFAULT_HINT, CAPTION_FONT_SIZE, ModeDetailsPanel.TEXT_COLOR);
  private final Label pageLabel =
      Scene2dElementFactory.createLabel(
          "", CAPTION_FONT_SIZE, ModeDetailsPanel.TEXT_COLOR.cpy().mul(1f, 1f, 1f, .65f));
  private int category;
  private int page;
  private float availableWidth = -1f;
  private String hoverText;
  private Actor categoryButton;
  private CategoryPopup categoryPopup;

  /**
   * Creates a palette showing the registered prefabs.
   *
   * @param listener receives drags and clicks of prefab icons
   */
  public PrefabPalette(Listener listener) {
    this.listener = listener;
    setTouchable(Touchable.enabled);
    setBackground(UIUtils.defaultSkin().getDrawable("generic-area"));
    pad(8f);
    dragAndDrop.setDragActorPosition(-ITEM_SIZE / 2f, ITEM_SIZE / 2f);
    for (int index = 0; index < categories.size(); index++) {
      if (categories.get(index).name().equals(lastCategory)) category = index;
    }
    rebuild();
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    float width = getParent() == null ? -1f : getParent().getWidth() - 2 * SCREEN_MARGIN;
    if (width != availableWidth) {
      availableWidth = width;
      rebuild();
    }
  }

  @Override
  protected void setStage(Stage stage) {
    // The popup lives in the parent group, so it has to be closed along with the palette.
    if (stage == null && categoryPopup != null) categoryPopup.close();
    super.setStage(stage);
  }

  private void rebuild() {
    clearChildren();
    dragAndDrop.clear();
    hoverText = null;
    List<Prefab> prefabs = categories.get(category).prefabs();
    int perPage = itemsPerPage();
    int pages = Math.max(1, (prefabs.size() + perPage - 1) / perPage);
    page = Math.clamp(page, 0, pages - 1);

    categoryButton = categoryButton();
    add(categoryButton).width(CATEGORY_WIDTH).height(ITEM_SIZE).padRight(6f);
    add(arrow("<", -1, page > 0)).width(ARROW_WIDTH).height(ITEM_SIZE);
    Table row = new Table();
    row.defaults().size(ITEM_SIZE).pad(ITEM_PAD);
    int end = Math.min(prefabs.size(), (page + 1) * perPage);
    for (int index = page * perPage; index < end; index++) row.add(item(prefabs.get(index)));
    // Empty slots keep the panel width the same for every category and page.
    for (int index = end; index < (page + 1) * perPage; index++) row.add();
    add(row);
    add(arrow(">", 1, page < pages - 1)).width(ARROW_WIDTH).height(ITEM_SIZE);
    row();

    Table captionRow = new Table();
    caption.setEllipsis(true);
    captionRow.add(caption).minWidth(0f).prefWidth(0f).growX().left();
    pageLabel.setText(pages > 1 ? "Page " + (page + 1) + "/" + pages : "");
    captionRow.add(pageLabel).right().padLeft(8f);
    add(captionRow).colspan(4).growX().padTop(4f).padLeft(4f).padRight(4f);
    updateCaption();
  }

  private int itemsPerPage() {
    if (availableWidth <= 0f) return MAX_ITEMS_PER_PAGE;
    float fixed = getPadLeft() + getPadRight() + CATEGORY_WIDTH + 6f + 2 * ARROW_WIDTH;
    return Math.clamp((int) ((availableWidth - fixed) / ITEM_CELL), 1, MAX_ITEMS_PER_PAGE);
  }

  private Actor categoryButton() {
    TextButton button =
        Scene2dElementFactory.createButton(categories.get(category).name(), "blue-outline", 15);
    button.getLabel().setWrap(true);
    button.getLabel().setAlignment(Align.center);
    button.addListener(
        new ClickListener(Input.Buttons.LEFT) {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            if (categoryPopup != null) categoryPopup.close();
            else openCategoryPopup();
          }
        });
    return button;
  }

  private void openCategoryPopup() {
    if (getParent() == null || getStage() == null) return;
    categoryPopup = new CategoryPopup();
    getParent().addActor(categoryPopup);
    categoryPopup.pack();
    Vector2 position =
        getParent().stageToLocalCoordinates(categoryButton.localToStageCoordinates(new Vector2()));
    categoryPopup.setPosition(position.x, position.y - categoryPopup.getHeight() - 4f);
    categoryPopup.toFront();
    getStage().addCaptureListener(categoryPopup.outsideClickListener);
  }

  private void selectCategory(int index) {
    if (categoryPopup != null) categoryPopup.close();
    category = index;
    lastCategory = categories.get(category).name();
    page = 0;
    rebuild();
  }

  private Actor arrow(String text, int direction, boolean enabled) {
    TextButton button = Scene2dElementFactory.createButton(text, "default", 18);
    button.setDisabled(!enabled);
    if (!enabled) button.getColor().a = 0.4f;
    button.addListener(
        new ClickListener(Input.Buttons.LEFT) {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            if (button.isDisabled()) return;
            page += direction;
            rebuild();
          }
        });
    return button;
  }

  private Actor item(Prefab prefab) {
    PrefabCatalog.Icon icon = PrefabCatalog.icon(prefab);
    Button button;
    if (icon.image() != null) {
      ImageButton imageButton = Scene2dElementFactory.createImageButton(icon.image(), "default");
      imageButton.getImageCell().size(IMAGE_SIZE);
      imageButton.getImage().setScaling(Scaling.fit);
      button = imageButton;
    } else {
      button = Scene2dElementFactory.createButton(wordPerLine(icon.label()), "default", 12);
    }
    Stack stack = new Stack();
    stack.add(button);
    if (icon.image() != null && icon.label() != null) {
      Label badge =
          Scene2dElementFactory.createLabel(icon.label(), 12, ModeDetailsPanel.TEXT_COLOR);
      Table overlay = new Table();
      overlay.setTouchable(Touchable.disabled);
      overlay.add(badge).expand().bottom().right().pad(2f, 2f, 3f, 5f);
      stack.add(overlay);
    }
    stack.addListener(
        new ClickListener(Input.Buttons.LEFT) {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            listener.clicked(prefab);
          }
        });
    addHover(stack, prefab.displayName());
    dragAndDrop.addSource(
        new DragAndDrop.Source(stack) {
          @Override
          public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
            DragAndDrop.Payload payload = new DragAndDrop.Payload();
            payload.setObject(prefab);
            payload.setDragActor(ghost(icon));
            listener.dragStarted(prefab);
            return payload;
          }

          @Override
          public void dragStop(
              InputEvent event,
              float x,
              float y,
              int pointer,
              DragAndDrop.Payload payload,
              DragAndDrop.Target target) {
            listener.dropped(prefab);
          }
        });
    return stack;
  }

  private static Actor ghost(PrefabCatalog.Icon icon) {
    Actor ghost;
    if (icon.image() != null) {
      Image image = new Image(icon.image());
      image.setScaling(Scaling.fit);
      ghost = image;
    } else {
      ghost = Scene2dElementFactory.createButton(wordPerLine(icon.label()), "blue-outline", 12);
    }
    ghost.setSize(ITEM_SIZE, ITEM_SIZE);
    ghost.getColor().a = 0.75f;
    ghost.setTouchable(Touchable.disabled);
    return ghost;
  }

  private static String wordPerLine(String text) {
    return text.trim().replaceAll("\\s+", "\n");
  }

  private void addHover(Actor actor, String text) {
    actor.addListener(
        new InputListener() {
          @Override
          public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            if (pointer != -1) return;
            hoverText = text;
            updateCaption();
          }

          @Override
          public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            if (pointer != -1 || (toActor != null && toActor.isDescendantOf(actor))) return;
            if (text.equals(hoverText)) hoverText = null;
            updateCaption();
          }
        });
  }

  private void updateCaption() {
    caption.setText(hoverText == null ? DEFAULT_HINT : hoverText);
  }

  /** List of all categories, shown below the category button. */
  private final class CategoryPopup extends Table {

    // Clicks outside the popup close it, except on the category button, which closes it itself.
    private final InputListener outsideClickListener =
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            Actor target = event.getTarget();
            if (target == null
                || !(target.isDescendantOf(CategoryPopup.this)
                    || target.isDescendantOf(categoryButton))) {
              close();
            }
            return false;
          }
        };

    private CategoryPopup() {
      setTouchable(Touchable.enabled);
      setBackground(UIUtils.defaultSkin().getDrawable("generic-area"));
      pad(6f);
      defaults().width(CATEGORY_WIDTH + 40f).height(32f).pad(2f);
      for (int index = 0; index < categories.size(); index++) {
        int chosen = index;
        TextButton button =
            Scene2dElementFactory.createButton(
                categories.get(index).name(), index == category ? "blue-outline" : "default", 15);
        button.addListener(
            new ClickListener(Input.Buttons.LEFT) {
              @Override
              public void clicked(InputEvent event, float x, float y) {
                selectCategory(chosen);
              }
            });
        add(button).row();
      }
    }

    private void close() {
      if (getStage() != null) getStage().removeCaptureListener(outsideClickListener);
      remove();
      if (categoryPopup == this) categoryPopup = null;
    }
  }

  /** Receives the interactions with the prefab icons of a palette. */
  public interface Listener {
    /**
     * Called when the user starts dragging a prefab icon.
     *
     * @param prefab dragged prefab
     */
    void dragStarted(Prefab prefab);

    /**
     * Called when a dragged prefab icon is released, wherever that is.
     *
     * @param prefab dragged prefab
     */
    void dropped(Prefab prefab);

    /**
     * Called when a prefab icon is clicked without dragging it.
     *
     * @param prefab clicked prefab
     */
    void clicked(Prefab prefab);
  }
}
