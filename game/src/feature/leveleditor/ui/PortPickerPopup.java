package feature.leveleditor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import engine.utils.Scene2dElementFactory;
import feature.hud.UIUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Searchable popup listing the ports a port can be linked to, grouped by prefab instance.
 *
 * <p>Only compatible ports are offered. Options that are compatible but cannot be chosen right now,
 * e.g. because they are already linked, are shown disabled with the reason. The popup closes when
 * an option is chosen, on Escape, or when clicking outside of it.
 *
 * <p>Optionally, tabs at the top split the options into ports of prefabs with a world position
 * ("In-World") and without one ("Off-World"), and a "New" tab offers ports of prefabs that are
 * created when chosen. The chosen tab is remembered for the runtime. If the remembered tab has no
 * options but another one has, the popup opens on the first such tab.
 */
public final class PortPickerPopup extends Table {

  /** Width of the popup, wide enough for three tabs. */
  public static final float WIDTH = 380f;

  private static final float MAX_LIST_HEIGHT = 380f;
  private static final int FONT_SIZE = 14;
  private static final Color GROUP_COLOR = ModeDetailsPanel.TEXT_COLOR.cpy().mul(1f, 1f, 1f, .65f);
  private static final Color DISABLED_COLOR = new Color(0.45f, 0.45f, 0.45f, 1f);
  private static final Color WARNING_COLOR = new Color(0.75f, 0.42f, 0f, 1f);

  /** The tab chosen last; shared by all popups showing tabs. */
  private static Tab rememberedTab = Tab.IN_WORLD;

  /**
   * One choosable port.
   *
   * @param instance name of the prefab instance owning the port, or the type of a new prefab
   * @param group group header, usually describing the instance
   * @param label port label
   * @param detail additional information, e.g. a warning or why the option is disabled; may be
   *     {@code null}
   * @param enabled whether the option can be chosen
   * @param inWorld whether the owning instance has a world position, used by the tabs
   * @param choose links the port when the option is chosen
   */
  public record Option(
      String instance,
      String group,
      String label,
      String detail,
      boolean enabled,
      boolean inWorld,
      Runnable choose) {}

  private final List<Option> options;
  private final List<Option> newOptions;
  private final String newHint;
  private final Runnable onClose;
  private final Table list = new Table();
  private final TextField filter;
  // Shown tabs, empty if the options are not split into tabs.
  private final List<Tab> tabs;
  private final Table tabBar = new Table();
  private Cell<ScrollPane> scrollCell;
  private InputListener outsideClickListener;
  private boolean closed;
  private Tab tab;

  /**
   * Creates a popup, optionally offering ports of new prefabs in a "New" tab next to the world
   * tabs.
   *
   * @param title text shown above the filter
   * @param options options in display order; options of one group must be adjacent
   * @param worldTabs whether to show "In-World" / "Off-World" tabs filtering the options
   * @param newOptions options creating a new prefab, in display order; options of one group must be
   *     adjacent. Only shown with world tabs, the "New" tab is left out if there are none.
   * @param newHint explanation shown above the new options, or {@code null}
   * @param onClose called once when the popup closes
   */
  public PortPickerPopup(
      String title,
      List<Option> options,
      boolean worldTabs,
      List<Option> newOptions,
      String newHint,
      Runnable onClose) {
    this.options = List.copyOf(options);
    this.newOptions = List.copyOf(newOptions);
    this.newHint = newHint;
    this.onClose = Objects.requireNonNull(onClose, "onClose");
    List<Tab> shownTabs = new ArrayList<>();
    if (worldTabs) {
      shownTabs.add(Tab.IN_WORLD);
      shownTabs.add(Tab.OFF_WORLD);
      if (!this.newOptions.isEmpty()) shownTabs.add(Tab.NEW);
    }
    tabs = List.copyOf(shownTabs);
    setTouchable(Touchable.enabled);
    setBackground(UIUtils.defaultSkin().getDrawable("generic-area"));
    pad(10f);
    top().left();
    defaults().growX().minWidth(0f);

    Label titleLabel = Scene2dElementFactory.createLabel(title, 16, ModeDetailsPanel.TEXT_COLOR);
    titleLabel.setWrap(true);
    add(titleLabel).padBottom(6f).row();

    tab = initialTab();
    if (!tabs.isEmpty()) {
      tabBar.defaults().minWidth(0f).growX().uniformX().height(34f).pad(0f, 2f, 0f, 2f);
      rebuildTabs();
      add(tabBar).padBottom(6f).row();
    }

    filter = Scene2dElementFactory.createTextField("", 16);
    filter.setMessageText("Filter...");
    Scene2dElementFactory.addTextFieldChangeListener(filter, text -> rebuildList());
    filter.addListener(
        new InputListener() {
          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            if (keycode == Input.Keys.ESCAPE) {
              close();
              return true;
            }
            if (keycode == Input.Keys.ENTER) {
              chooseSingleMatch();
              return true;
            }
            return false;
          }
        });
    add(filter).height(36f).padBottom(6f).row();

    list.top().left();
    list.defaults().growX().minWidth(0f);
    ScrollPane scroll = Scene2dElementFactory.createScrollPane(list, false, true);
    ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle(scroll.getStyle());
    scrollStyle.background = null;
    scroll.setStyle(scrollStyle);
    scroll.setFlickScroll(false);
    scroll.setFadeScrollBars(false);
    rebuildList();
    scrollCell = add(scroll).height(listHeight());
    row();

    TextButton cancel = Scene2dElementFactory.createButton("Cancel", "default", FONT_SIZE);
    cancel.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            close();
          }
        });
    add(cancel).height(30f).padTop(6f).row();
  }

  /**
   * Shows the popup inside a parent group, near a stage position and clamped to the stage.
   *
   * @param parent group to add the popup to, usually the level editor UI
   * @param stageX preferred left edge in stage coordinates
   * @param stageY preferred top edge in stage coordinates
   */
  public void show(Group parent, float stageX, float stageY) {
    parent.addActor(this);
    setWidth(WIDTH);
    validate();
    setHeight(getPrefHeight());
    Stage stage = parent.getStage();
    if (stage == null) return;
    float x = Math.max(0f, Math.min(stageX, stage.getWidth() - getWidth()));
    float y = Math.max(0f, Math.min(stageY - getHeight(), stage.getHeight() - getHeight()));
    setPosition(x, y);
    toFront();
    focusFilter();
    outsideClickListener =
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            Actor target = event.getTarget();
            if (target == null || !target.isDescendantOf(PortPickerPopup.this)) close();
            return false;
          }
        };
    stage.addCaptureListener(outsideClickListener);
  }

  /** Closes the popup without choosing an option. */
  public void close() {
    if (closed) return;
    closed = true;
    Stage stage = getStage();
    if (stage != null) {
      if (outsideClickListener != null) stage.removeCaptureListener(outsideClickListener);
      if (stage.getKeyboardFocus() == filter) stage.setKeyboardFocus(null);
    }
    remove();
    onClose.run();
  }

  /**
   * Moves the keyboard focus to the filter, also in the next frame: the level editor drops the
   * focus of text fields on clicks outside of them, e.g. on the button that opened the popup, after
   * the click was handled.
   */
  private void focusFilter() {
    if (getStage() != null) getStage().setKeyboardFocus(filter);
    Gdx.app.postRunnable(
        () -> {
          if (!closed && getStage() != null) getStage().setKeyboardFocus(filter);
        });
  }

  // The remembered tab, unless it has no options and another tab has some.
  private Tab initialTab() {
    Tab remembered = tabs.contains(rememberedTab) ? rememberedTab : Tab.IN_WORLD;
    if (!tabOptions(remembered).isEmpty()) return remembered;
    return tabs.stream()
        .filter(candidate -> !tabOptions(candidate).isEmpty())
        .findFirst()
        .orElse(remembered);
  }

  private void rebuildTabs() {
    tabBar.clearChildren();
    for (Tab shown : tabs) {
      TextButton button =
          Scene2dElementFactory.createButton(
              shown.label + " (" + tabOptions(shown).size() + ")",
              shown == tab ? "blue-outline" : "default",
              14);
      button.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              selectTab(shown);
            }
          });
      tabBar.add(button);
    }
  }

  private void selectTab(Tab selected) {
    focusFilter();
    rememberedTab = selected;
    if (tab == selected) return;
    tab = selected;
    rebuildTabs();
    rebuildList();
    resizeKeepingTop();
  }

  private float listHeight() {
    return Math.min(MAX_LIST_HEIGHT, Math.max(40f, list.getPrefHeight()));
  }

  private void resizeKeepingTop() {
    scrollCell.height(listHeight());
    invalidateHierarchy();
    float top = getY() + getHeight();
    validate();
    setHeight(getPrefHeight());
    setY(Math.max(0f, top - getHeight()));
  }

  private void chooseSingleMatch() {
    List<Option> enabled = matching().stream().filter(Option::enabled).toList();
    if (enabled.size() == 1) choose(enabled.getFirst());
  }

  private void choose(Option option) {
    close();
    option.choose().run();
  }

  private List<Option> tabOptions(Tab shown) {
    if (tabs.isEmpty()) return options;
    return switch (shown) {
      case IN_WORLD -> options.stream().filter(Option::inWorld).toList();
      case OFF_WORLD -> options.stream().filter(option -> !option.inWorld()).toList();
      case NEW -> newOptions;
    };
  }

  private List<Option> matching() {
    return matching(tab);
  }

  private List<Option> matching(Tab shown) {
    String query = filter.getText().strip().toLowerCase(Locale.ROOT);
    if (query.isEmpty()) return tabOptions(shown);
    List<Option> result = new ArrayList<>();
    for (Option option : tabOptions(shown)) {
      String text = (option.group() + " " + option.label()).toLowerCase(Locale.ROOT);
      if (text.contains(query)) result.add(option);
    }
    return result;
  }

  private void rebuildList() {
    list.clearChildren();
    if (tab == Tab.NEW && newHint != null) {
      Label hint = Scene2dElementFactory.createLabel(newHint, 12, GROUP_COLOR);
      hint.setWrap(true);
      list.add(hint).pad(0f, 2f, 2f, 2f).row();
    }
    Map<String, List<Option>> groups = new LinkedHashMap<>();
    for (Option option : matching()) {
      groups.computeIfAbsent(option.group(), ignored -> new ArrayList<>()).add(option);
    }
    if (groups.isEmpty()) {
      Label empty = Scene2dElementFactory.createLabel(emptyMessage(), FONT_SIZE, GROUP_COLOR);
      empty.setWrap(true);
      list.add(empty).pad(4f).row();
      return;
    }
    groups.forEach(
        (group, groupOptions) -> {
          Label header = Scene2dElementFactory.createLabel(group, FONT_SIZE, GROUP_COLOR);
          header.setEllipsis(true);
          list.add(header).padTop(6f).padLeft(2f).row();
          for (Option option : groupOptions) list.add(optionActor(option)).padTop(2f).row();
        });
  }

  private String emptyMessage() {
    if (tabs.isEmpty()) {
      return options.isEmpty() ? "No compatible ports in this level." : "No matches.";
    }
    String message =
        tabOptions(tab).isEmpty()
            ? "No compatible " + tab.label.toLowerCase(Locale.ROOT) + " ports."
            : "No matches.";
    List<String> elsewhere = new ArrayList<>();
    for (Tab other : tabs) {
      int count = other == tab ? 0 : matching(other).size();
      if (count > 0) elsewhere.add(count + " in the " + other.label + " tab");
    }
    return elsewhere.isEmpty() ? message : message + " " + String.join(", ", elsewhere) + ".";
  }

  private Actor optionActor(Option option) {
    if (!option.enabled()) {
      String text = option.label() + (option.detail() == null ? "" : " - " + option.detail());
      Label label = Scene2dElementFactory.createLabel(text, FONT_SIZE, DISABLED_COLOR);
      label.setWrap(true);
      Table row = new Table();
      row.add(label).growX().minWidth(0f).pad(4f, 10f, 4f, 4f);
      return row;
    }
    TextButton button = Scene2dElementFactory.createButton(option.label(), "default", FONT_SIZE);
    button.getLabel().setAlignment(Align.left);
    button.getLabel().setEllipsis(true);
    button.getLabelCell().growX().minWidth(0f).padLeft(6f);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            choose(option);
          }
        });
    Table row = new Table();
    row.defaults().growX().minWidth(0f);
    row.add(button).height(30f).row();
    if (option.detail() == null) return row;
    Label detail = Scene2dElementFactory.createLabel(option.detail(), 12, WARNING_COLOR);
    detail.setWrap(true);
    row.add(detail).padLeft(10f).row();
    return row;
  }

  /** Tabs splitting the options. */
  private enum Tab {
    IN_WORLD("In-World"),
    OFF_WORLD("Off-World"),
    NEW("New");

    private final String label;

    Tab(String label) {
      this.label = label;
    }
  }
}
