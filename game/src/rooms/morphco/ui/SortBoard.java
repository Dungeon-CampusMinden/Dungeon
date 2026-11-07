package rooms.morphco.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import engine.utils.Cursors;
import engine.utils.Scene2dElementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Items that players sort into bins, by drag and drop or by clicking the item and then the bin.
 *
 * <p>Every item keeps its chip in its home area (a sentence, a word, a tray). A placed item is
 * shown dimmed at home and as a chip inside its bin; clicking that chip, or dragging it out of all
 * bins, puts the item back.
 */
final class SortBoard {

  private static final Color CHIP_TEXT = Color.BLACK;
  private static final String CHIP = "morphCoChip";
  private static final String STYLE_FREE = "blue_square_flat";
  private static final String STYLE_SELECTED = "green_square_depth_flat";
  private static final String STYLE_PLACED = "grey_square_depth_flat";
  private static final String STYLE_IN_BIN = "blue_square_depth_flat";

  private final Skin skin;
  private final Runnable onChange;
  private final DragAndDrop dragAndDrop = new DragAndDrop();
  private final Map<String, String> labels = new LinkedHashMap<>();
  private final Map<String, String> placement = new LinkedHashMap<>();
  private final Map<String, Table> homeChips = new HashMap<>();
  private final Map<String, DragAndDrop.Source> homeSources = new HashMap<>();
  private final Map<String, HorizontalGroup> binContents = new LinkedHashMap<>();
  private final Map<String, Integer> capacities = new HashMap<>();
  private final Map<String, Drawable> backgrounds = new HashMap<>();
  private final List<DragAndDrop.Source> binSources = new ArrayList<>();
  private String selected;

  /**
   * Creates an empty board.
   *
   * @param skin UI skin
   * @param onChange called after every change of the placement
   */
  SortBoard(Skin skin, Runnable onChange) {
    this.skin = skin;
    this.onChange = onChange;
    // Quick flicks count as drops too; libGDX ignores drops shorter than 250 ms by default.
    dragAndDrop.setDragTime(0);
    dragAndDrop.setTapSquareSize(8);
    dragAndDrop.setKeepWithinStage(false);
  }

  /**
   * Registers an item.
   *
   * @param id stable item ID
   * @param label visible label
   */
  void item(String id, String label) {
    labels.put(id, label);
  }

  /**
   * Removes all items whose ID starts with a prefix, including their placements.
   *
   * @param prefix ID prefix
   */
  void removeItems(String prefix) {
    labels.keySet().removeIf(id -> id.startsWith(prefix));
    placement.keySet().removeIf(id -> id.startsWith(prefix));
    homeChips.keySet().removeIf(id -> id.startsWith(prefix));
    homeSources
        .entrySet()
        .removeIf(
            entry -> {
              if (!entry.getKey().startsWith(prefix)) return false;
              dragAndDrop.removeSource(entry.getValue());
              return true;
            });
    if (selected != null && selected.startsWith(prefix)) selected = null;
  }

  /**
   * Creates the chip that represents an item at home.
   *
   * @param id item ID
   * @return chip actor
   */
  Table homeChip(String id) {
    return homeChip(id, 22);
  }

  /**
   * Creates the chip that represents an item at home with a given font size.
   *
   * @param id item ID
   * @param size font size of the chip
   * @return chip actor
   */
  Table homeChip(String id, int size) {
    Table chip = chip(labels.get(id), size);
    chip.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            event.stop();
            selected = id.equals(selected) ? null : id;
            refresh();
          }
        });
    DragAndDrop.Source source = source(chip, id);
    DragAndDrop.Source previous = homeSources.put(id, source);
    if (previous != null) dragAndDrop.removeSource(previous);
    dragAndDrop.addSource(source);
    homeChips.put(id, chip);
    style(id);
    return chip;
  }

  /**
   * Creates a bin.
   *
   * @param id bin ID
   * @param title localized bin title
   * @param capacity maximum number of items, {@code 0} for unlimited
   * @param minHeight minimum height of the bin
   * @return bin actor
   */
  Table bin(String id, String title, int capacity, float minHeight) {
    Table bin = new Table(skin);
    bin.setBackground("generic-area");
    bin.pad(8);
    bin.top();
    bin.setTouchable(Touchable.enabled);
    bin.setUserObject(Cursors.INTERACT);
    bin.add(Scene2dElementFactory.createLabel(title, 20, MorphCoPanel.INK)).left().row();
    HorizontalGroup content = new HorizontalGroup();
    content.wrap(true);
    content.space(6);
    content.wrapSpace(6);
    bin.add(content).grow().minHeight(minHeight).top().left();
    binContents.put(id, content);
    capacities.put(id, capacity);
    bin.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            // A click on a chip inside the bin is handled by that chip.
            if (selected == null || insideChip(event.getTarget(), bin)) return;
            place(selected, id);
          }
        });
    dragAndDrop.addTarget(
        new DragAndDrop.Target(bin) {
          @Override
          public boolean drag(
              DragAndDrop.Source source,
              DragAndDrop.Payload payload,
              float x,
              float y,
              int pointer) {
            return payload.getObject() instanceof String;
          }

          @Override
          public void drop(
              DragAndDrop.Source source,
              DragAndDrop.Payload payload,
              float x,
              float y,
              int pointer) {
            place((String) payload.getObject(), id);
          }
        });
    return bin;
  }

  /**
   * Places an item in a bin; a full bin returns its oldest item home.
   *
   * @param item item ID
   * @param bin bin ID
   */
  void place(String item, String bin) {
    if (!labels.containsKey(item) || !binContents.containsKey(bin)) return;
    int capacity = capacities.getOrDefault(bin, 0);
    if (capacity > 0 && !bin.equals(placement.get(item))) {
      List<String> inBin =
          placement.entrySet().stream()
              .filter(entry -> entry.getValue().equals(bin))
              .map(Map.Entry::getKey)
              .toList();
      if (inBin.size() >= capacity) placement.remove(inBin.getFirst());
    }
    placement.remove(item);
    placement.put(item, bin);
    selected = null;
    refresh();
    onChange.run();
  }

  /**
   * Puts an item back home.
   *
   * @param item item ID
   */
  void remove(String item) {
    if (placement.remove(item) == null) return;
    refresh();
    onChange.run();
  }

  /** Puts all items back home. */
  void clear() {
    placement.clear();
    selected = null;
    refresh();
    onChange.run();
  }

  /**
   * Returns the current placement.
   *
   * @return copy of the bin of every placed item
   */
  Map<String, String> placement() {
    return new LinkedHashMap<>(placement);
  }

  /**
   * Returns the label of an item.
   *
   * @param id item ID
   * @return label, or {@code null} for unknown items
   */
  String label(String id) {
    return labels.get(id);
  }

  /** Redraws the bins and the chip styles at home. */
  void refresh() {
    binSources.forEach(dragAndDrop::removeSource);
    binSources.clear();
    binContents.values().forEach(HorizontalGroup::clearChildren);
    placement.forEach(
        (item, bin) -> {
          Table chip = chip(labels.get(item), 20);
          chip.setBackground(background(STYLE_IN_BIN));
          chip.addListener(
              new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                  event.stop();
                  // With another item selected, a click anywhere in the bin places that item.
                  if (selected != null && !selected.equals(item)) place(selected, bin);
                  else remove(item);
                }
              });
          DragAndDrop.Source source = source(chip, item);
          binSources.add(source);
          dragAndDrop.addSource(source);
          binContents.get(bin).addActor(chip);
        });
    homeChips.keySet().forEach(this::style);
  }

  private void style(String id) {
    Table chip = homeChips.get(id);
    if (chip == null) return;
    if (Objects.equals(id, selected)) chip.setBackground(background(STYLE_SELECTED));
    else if (placement.containsKey(id)) chip.setBackground(background(STYLE_PLACED));
    else chip.setBackground(background(STYLE_FREE));
  }

  private Table chip(String text, int size) {
    Table chip = new Table(skin);
    chip.setBackground(background(STYLE_FREE));
    chip.pad(4, 10, 4, 10);
    chip.setTouchable(Touchable.enabled);
    chip.setUserObject(Cursors.INTERACT);
    chip.setName(CHIP);
    chip.add(Scene2dElementFactory.createLabel(text, size, CHIP_TEXT));
    return chip;
  }

  private DragAndDrop.Source source(Table chip, String item) {
    return new DragAndDrop.Source(chip) {
      @Override
      public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
        DragAndDrop.Payload payload = new DragAndDrop.Payload();
        payload.setObject(item);
        Table dragged = chip(labels.get(item), 22);
        dragged.setBackground(background(STYLE_SELECTED));
        dragged.pack();
        payload.setDragActor(dragged);
        dragAndDrop.setDragActorPosition(dragged.getWidth() / 2f, -dragged.getHeight() / 2f);
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
        // A chip dragged out of every bin goes back home.
        if (target == null && placement.containsKey(item)) remove(item);
      }
    };
  }

  // The skin's button drawables have a large minimum height; chips size to their text instead.
  private Drawable background(String name) {
    return backgrounds.computeIfAbsent(
        name,
        key -> {
          Drawable drawable = skin.newDrawable(key);
          drawable.setMinHeight(0);
          drawable.setMinWidth(0);
          return drawable;
        });
  }

  private static boolean insideChip(Actor target, Actor bin) {
    for (Actor actor = target; actor != null && actor != bin; actor = actor.getParent()) {
      if (CHIP.equals(actor.getName())) return true;
    }
    return false;
  }
}
