package feature.survey;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.language.Language;
import engine.language.LocalizedText;
import engine.language.Translation;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.utils.BaseContainerUI;
import engine.utils.Cursors;
import engine.utils.FontHelper;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogCreationException;
import feature.hud.dialogs.HeadlessDialogGroup;
import feature.survey.SurveyDefinition.Choice;
import feature.survey.SurveyDefinition.ChoiceKind;
import feature.survey.SurveyDefinition.Info;
import feature.survey.SurveyDefinition.Matrix;
import feature.survey.SurveyDefinition.NumberInput;
import feature.survey.SurveyDefinition.Option;
import feature.survey.SurveyDefinition.Problem;
import feature.survey.SurveyDefinition.Question;
import feature.survey.SurveyDefinition.Scale;
import feature.survey.SurveyDefinition.Text;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Client view of a survey dialog: the paged form, or the storage result after submitting.
 *
 * <p>The form validates each page with the same rules the server applies and sends all answers as
 * one JSON object keyed by question ID. Sizes derive from the current window on every layout, so
 * the dialog fits again after the window is resized.
 */
final class SurveyDialog {

  private static final Translation T = new Translation("dialog.survey");
  private static final JsonNodeFactory JSON = JsonNodeFactory.instance;
  private static final String FONT = "fonts/Roboto-SemiBold.ttf";
  private static final String FONT_REGULAR = "fonts/Roboto-Regular.ttf";
  private static final Color INK = Color.valueOf("1f2430");
  private static final Color MUTED = Color.valueOf("5b6270");
  private static final Color ERROR = Color.valueOf("c0392b");
  private static final Color ACCENT = Color.valueOf("1b8fc4");
  private static final Color ACCENT_LIGHT = Color.valueOf("d6ecf8");
  private static final Color CARD = new Color(1, 1, 1, 0.8f);
  private static final Color CARD_ERROR = Color.valueOf("fbe9e7");
  private static final Color ROW_MISSING = Color.valueOf("f6cdc7");
  private static final Color INFO = Color.valueOf("dcefff");
  private static final Color STRIPE = new Color(0.86f, 0.88f, 0.92f, 0.6f);
  private static final float WINDOW_MARGIN = 24;
  private static final float WINDOW_PAD_X = 26;
  private static final float CARD_PAD = 18;
  private static final float SCROLL_PAD_LEFT = 2;
  private static final float GAP = 6;
  private static final float INPUT_HEIGHT = 42;

  private SurveyDialog() {}

  /**
   * Builds the survey form or, when the context carries a result, the result view.
   *
   * @param context dialog context with the room ID and an optional result
   * @return dialog actor
   */
  static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    Optional<String> result = context.find(SurveyFeature.RESULT_KEY, String.class);
    if (result.isPresent()) {
      return resizable(resultView(SurveyFeature.Result.valueOf(result.get()), context));
    }
    String roomId = context.require(SurveyFeature.ROOM_ID_KEY, String.class);
    SurveyDefinition survey =
        SurveyDefinition.load(roomId)
            .orElseThrow(() -> new DialogCreationException("No valid survey for room " + roomId));
    return resizable(new Form(survey, context).frame);
  }

  /**
   * Wraps the window so it re-measures on resize. Nested tables cache their preferred sizes, so the
   * window-dependent {@link #value} widths need the whole tree invalidated.
   *
   * @param dialog dialog content
   * @return container that keeps the dialog centered
   */
  private static BaseContainerUI resizable(Actor dialog) {
    return new BaseContainerUI(dialog) {
      @Override
      public void onResize(int width, int height) {
        invalidateTree(dialog);
        super.onResize(width, height);
      }
    };
  }

  private static void invalidateTree(Actor actor) {
    if (actor instanceof Layout layout) layout.invalidate();
    if (actor instanceof Group group) group.getChildren().forEach(SurveyDialog::invalidateTree);
  }

  /**
   * Dialog width for the current window, leaving a margin on both sides.
   *
   * @param max width on large windows
   * @return dialog width
   */
  private static float dialogWidth(float max) {
    return Math.max(320f, Math.min(max, Game.windowWidth() - 2 * WINDOW_MARGIN));
  }

  /** Paged form state; answers live in the input widgets until submission. */
  private static final class Form {
    private final SurveyDefinition survey;
    private final DialogContext context;
    private final Language language = Game.localization().currentLanguage();
    // The scroll bar sits in this gutter to the right of the cards; header and footer keep it too,
    // so every right edge lines up with the cards.
    private final float gutter =
        UIUtils.defaultSkin().get(ScrollPane.ScrollPaneStyle.class).vScrollKnob.getMinWidth() + 12;
    private final Value inner = value(() -> dialogWidth(860) - 2 * WINDOW_PAD_X);
    private final Value column = value(this::cardWidth);
    private final Value content = value(this::contentWidth);
    private final Table window = window();
    // The form uses the whole window height minus a margin above and below.
    private final Container<Table> frame =
        new Container<>(window).height(value(() -> Game.windowHeight() - 2 * WINDOW_MARGIN)).fill();
    private final Label pageTitle = label("", 24, INK, FONT);
    private final Label pageCounter = label("", 16, MUTED, FONT_REGULAR);
    private final ScrollPane scroll;
    private final Table footer = new Table();
    private final List<Table> pages = new ArrayList<>();
    private final List<List<Field>> fields = new ArrayList<>();
    private int page;

    private Form(SurveyDefinition survey, DialogContext context) {
      this.survey = survey;
      this.context = context;
      for (SurveyDefinition.Page definition : survey.pages()) {
        Table pageContent = new Table();
        pageContent.top().left().pad(4, SCROLL_PAD_LEFT, 12, gutter);
        List<Field> pageFields = new ArrayList<>();
        for (Question question : definition.questions()) {
          Table card = card(question);
          pageContent.add(card).width(column).padBottom(12).row();
          // Info cards take no answer, so they need no field or validation.
          if (!(question instanceof Info)) pageFields.add(field(question, card));
        }
        pages.add(pageContent);
        fields.add(pageFields);
      }

      window.add(title(survey.title().text(language))).width(inner).padBottom(22).row();
      Table pageHeader = new Table();
      pageHeader.add(pageTitle).growX().left();
      pageHeader.add(pageCounter).right().bottom();
      window.add(pageHeader).width(column).padLeft(SCROLL_PAD_LEFT).left().row();
      scroll = Scene2dElementFactory.createScrollPane(pages.getFirst(), false, true);
      scroll.setStyle(new ScrollPane.ScrollPaneStyle(scroll.getStyle()));
      scroll.getStyle().background = null;
      scroll.setFadeScrollBars(false);
      scroll.setScrollbarsOnTop(false);
      // The questions take the height that title, page header and footer leave.
      window.add(scroll).width(inner).growY().minHeight(140).padTop(10).row();
      window.add(footer).width(column).padTop(14).padLeft(SCROLL_PAD_LEFT).left().row();
      // A click on any non-text control ends typing, so keys no longer go into a hidden field.
      // Without text focus the pause menu stays reachable, e.g. to change the volume.
      window.addCaptureListener(
          new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
              if (!(event.getTarget() instanceof TextField) && event.getStage() != null) {
                event.getStage().setKeyboardFocus(null);
              }
              return false;
            }
          });
      showPage(0);
    }

    private float cardWidth() {
      return dialogWidth(860) - 2 * WINDOW_PAD_X - SCROLL_PAD_LEFT - gutter;
    }

    private float contentWidth() {
      return cardWidth() - 2 * CARD_PAD;
    }

    private void showPage(int index) {
      page = index;
      SurveyDefinition.Page definition = survey.pages().get(index);
      pageTitle.setText(definition.title().map(text -> text.text(language)).orElse(""));
      pageCounter.setText(T.text("page", index + 1, survey.pages().size()));
      // A hidden page kept the layout of the window size it was last shown at.
      invalidateTree(pages.get(index));
      scroll.setActor(pages.get(index));
      Scene2dElementFactory.scrollPaneScrollTo(scroll, 0, 0);
      showNavigation();
    }

    private void showNavigation() {
      footer.clearChildren();
      if (survey.skippable()) {
        footer.add(button(T.text("skip"), "default", 18, this::confirmSkip)).left();
      }
      footer.add().growX();
      if (page > 0) {
        footer.add(button(T.text("back"), "blue-outline", 20, () -> showPage(page - 1)));
      }
      boolean last = page == survey.pages().size() - 1;
      footer
          .add(
              button(
                  T.text(last ? "submit" : "next"), "green", 20, last ? this::submit : this::next))
          .padLeft(10);
    }

    /**
     * Replaces the footer in place. The harmless "back to survey" lands where the player just
     * clicked, so a double click cannot skip by accident.
     */
    private void confirmSkip() {
      footer.clearChildren();
      footer.add(button(T.text("skip_cancel"), "blue-outline", 18, this::showNavigation));
      footer
          .add(
              button(
                  T.text("skip_yes"),
                  "red-outline",
                  18,
                  () ->
                      DialogCallbackResolver.createButtonCallback(
                              context.dialogId(), DialogContextKeys.ON_CANCEL)
                          .accept(null)))
          .padLeft(10);
      footer.add(label(T.text("skip_hint"), 16, MUTED, FONT_REGULAR)).padLeft(14).growX().left();
    }

    private void next() {
      if (validate(page)) showPage(page + 1);
    }

    /** Earlier pages passed their check on "Weiter"; only the last page is still unchecked. */
    private void submit() {
      if (!validate(page)) return;
      ObjectNode answers = JSON.objectNode();
      for (List<Field> pageFields : fields) {
        for (Field field : pageFields) {
          JsonNode value = field.value.get();
          if (value != null) answers.set(field.question.id(), value);
        }
      }
      showSending();
      DialogCallbackResolver.createButtonCallback(context.dialogId(), DialogContextKeys.ON_CONFIRM)
          .accept(new DialogResponseMessage.StringValue(answers.toString()));
    }

    /**
     * Marks invalid questions of one page and scrolls the first one to the top.
     *
     * @param index page index
     * @return whether every question on the page can be submitted
     */
    private boolean validate(int index) {
      Field first = null;
      for (Field field : fields.get(index)) {
        if (!field.check(true) && first == null) first = field;
      }
      if (first == null) return true;
      // Error lines changed the card heights; lay out again before measuring.
      scroll.invalidate();
      scroll.validate();
      float top = pages.get(index).getHeight() - first.card.getY() - first.card.getHeight();
      scroll.setScrollY(Math.max(0, top - 4));
      return false;
    }

    private void showSending() {
      Label sending = label(T.text("sending"), 22, INK, FONT);
      sending.setAlignment(Align.center);
      sending.addAction(dots(sending, T.text("sending")));
      Table sendingContent = new Table();
      sendingContent.add(sending).expand().center();
      scroll.setActor(sendingContent);
      pageTitle.setText("");
      pageCounter.setText("");
      footer.clearChildren();
      footer.add().height(48);
    }

    private Table card(Question question) {
      Table card = new Table();
      card.setBackground(tint(question instanceof Info ? INFO : CARD));
      card.pad(14, CARD_PAD, 14, CARD_PAD);
      card.defaults().left();
      String title = "[#" + INK + "]" + escape(question.text().text(language)) + "[]";
      if (question.required()) title += " [#" + ERROR + "]*[]";
      Table header = new Table();
      header.add(markupLabel(title)).growX().top().left();
      if (question instanceof Text text && text.maxLength().isPresent()) {
        header.add(counter(card, text.maxLength().get())).top().right().padLeft(12);
      }
      card.add(header).width(content).row();
      // An info card's description is its body; on questions it is a smaller hint.
      int descriptionSize = question instanceof Info ? 16 : 14;
      question
          .description()
          .ifPresent(
              description ->
                  card.add(
                          wrappedLabel(
                              description.text(language), descriptionSize, MUTED, FONT_REGULAR))
                      .width(content)
                      .padTop(4)
                      .row());
      return card;
    }

    /**
     * Shows how many of the allowed characters a text answer uses, such as "12/50".
     *
     * @param card card whose text field reports its edits
     * @param max allowed characters
     * @return counter label
     */
    private Label counter(Table card, int max) {
      Label counter = label("0/" + max, 15, MUTED, FONT_REGULAR);
      card.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              if (actor instanceof TextField field) {
                counter.setText(field.getText().length() + "/" + max);
              }
            }
          });
      return counter;
    }

    /**
     * Adds the input of an answerable question and its error line to the question's card.
     *
     * @param question question other than {@link Info}
     * @param card card of the question
     * @return field reading and checking the answer
     */
    private Field field(Question question, Table card) {
      Field field = new Field(question, card);
      field.value =
          switch (question) {
            case Text text -> text(card, text);
            case NumberInput number -> number(card, number);
            case Scale scale -> scale(card, scale);
            case Choice choice ->
                choice.kind() == ChoiceKind.DROPDOWN
                    ? dropdown(card, choice)
                    : choice(card, choice);
            case Matrix matrix -> matrix(card, matrix, field);
            case Info info -> throw new IllegalArgumentException(info.id() + " takes no answer");
          };
      field.errorCell = card.add((Label) null).width(content);
      // Input widgets fire bubbling change events; once checked, the card follows every edit.
      card.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              if (field.checked) field.check(false);
            }
          });
      return field;
    }

    private Supplier<JsonNode> text(Table card, Text question) {
      TextField field = textField(question.multiline());
      field.setMaxLength(question.maxLength().orElse(0)); // 0 means no limit
      field.setMessageText(T.text("placeholder"));
      card.add(field)
          .width(content)
          .height(question.multiline() ? 118 : INPUT_HEIGHT)
          .padTop(10)
          .row();
      return () -> field.getText().isBlank() ? null : JSON.stringNode(field.getText().trim());
    }

    private Supplier<JsonNode> number(Table card, NumberInput question) {
      TextField field = textField(false);
      // Only characters the question accepts: a minus below zero, a separator with decimals.
      boolean negative = question.min().map(min -> min.signum() < 0).orElse(true);
      field.setTextFieldFilter(
          (ignored, c) ->
              Character.isDigit(c)
                  || (negative && c == '-')
                  || (question.decimals() && (c == ',' || c == '.')));
      field.setMaxLength(20);
      Table row = new Table();
      row.add(field).width(180).height(INPUT_HEIGHT);
      row.add(label(numberHint(question), 16, MUTED, FONT_REGULAR)).padLeft(14);
      card.add(row).padTop(10).row();
      return () -> {
        String raw = field.getText().trim();
        if (raw.isEmpty()) return null;
        try {
          BigDecimal value = new BigDecimal(raw.replace(',', '.')).stripTrailingZeros();
          return value.scale() <= 0
              ? JSON.numberNode(value.toBigIntegerExact())
              : JSON.numberNode(value);
        } catch (NumberFormatException | ArithmeticException exception) {
          return JSON.stringNode(raw);
        }
      };
    }

    /**
     * Number buttons in one row, shrinking to fit; the end labels sit below the outer buttons.
     *
     * @param card question card
     * @param question scale question
     * @return reads the chosen value
     */
    private Supplier<JsonNode> scale(Table card, Scale question) {
      int count = question.max() - question.min() + 1;
      Value buttonWidth = value(() -> Math.min(48f, (contentWidth() - (count - 1) * GAP) / count));
      ButtonGroup<TextButton> group = group();
      Table buttons = new Table();
      for (int value = question.min(); value <= question.max(); value++) {
        TextButton button = new TextButton(String.valueOf(value), toggleStyle());
        button.pad(4);
        button.setUserObject(Cursors.INTERACT);
        group.add(button);
        buttons
            .add(button)
            .width(buttonWidth)
            .height(44)
            .padRight(value < question.max() ? GAP : 0);
      }
      Table scale = new Table();
      scale.add(buttons).left().row();
      if (question.minLabel().isPresent() || question.maxLabel().isPresent()) {
        Value half = value(() -> (count * buttonWidth.get(null) + (count - 1) * GAP) / 2);
        Label min = wrappedLabel(localized(question.minLabel()), 15, MUTED, FONT_REGULAR);
        Label max = wrappedLabel(localized(question.maxLabel()), 15, MUTED, FONT_REGULAR);
        max.setAlignment(Align.right);
        Table ends = new Table();
        ends.add(min).width(half).top();
        ends.add(max).width(half).top();
        scale.add(ends).left().padTop(6);
      }
      card.add(scale).padTop(12).row();
      return () ->
          group.getCheckedIndex() < 0
              ? null
              : JSON.numberNode(question.min() + group.getCheckedIndex());
    }

    private Supplier<JsonNode> choice(Table card, Choice question) {
      boolean multi = question.kind() == ChoiceKind.MULTI;
      ButtonGroup<CheckBox> group = multi ? null : group();
      List<CheckBox> boxes = new ArrayList<>();
      List<String> ids = new ArrayList<>();
      for (Option option : question.options()) {
        CheckBox box = checkBox(option.text().text(language), multi);
        boxes.add(box);
        ids.add(option.id());
        card.add(box).width(content).padTop(8).row();
      }
      TextField otherText = textField(false);
      if (question.other()) {
        CheckBox other = checkBox(T.text("other"), multi);
        other.getLabel().setWrap(false);
        boxes.add(other);
        ids.add(SurveyDefinition.OTHER_ID);
        otherText.setMaxLength(SurveyDefinition.OTHER_MAX_LENGTH);
        otherText.setMessageText(T.text("other_placeholder"));
        otherText.addListener(
            new ChangeListener() {
              @Override
              public void changed(ChangeEvent event, Actor actor) {
                if (!otherText.getText().isBlank()) other.setChecked(true);
              }
            });
        Table row = new Table();
        row.add(other).padRight(12);
        row.add(otherText).growX().height(INPUT_HEIGHT);
        card.add(row).width(content).padTop(8).row();
      }
      if (group != null) boxes.forEach(group::add);
      return () -> {
        ArrayNode selected = JSON.arrayNode();
        for (int index = 0; index < boxes.size(); index++) {
          if (boxes.get(index).isChecked()) selected.add(ids.get(index));
        }
        if (selected.isEmpty()) return null;
        ObjectNode answer = JSON.objectNode();
        answer.set("selected", multi ? selected : selected.get(0));
        if (question.other() && boxes.getLast().isChecked() && !otherText.getText().isBlank()) {
          answer.put("otherText", otherText.getText().trim());
        }
        return answer;
      };
    }

    private Supplier<JsonNode> dropdown(Table card, Choice question) {
      Skin skin = UIUtils.defaultSkin();
      SelectBox.SelectBoxStyle style =
          new SelectBox.SelectBoxStyle(skin.get("small", SelectBox.SelectBoxStyle.class));
      style.font = font(FONT_REGULAR, 18);
      style.fontColor = MUTED;
      style.overFontColor = MUTED;
      style.listStyle = new com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle(style.listStyle);
      style.listStyle.font = style.font;
      style.listStyle.fontColorUnselected = INK;
      style.listStyle.fontColorSelected = Color.WHITE;
      style.listStyle.selection = padded(tint(ACCENT));
      style.listStyle.over = padded(tint(ACCENT_LIGHT));
      SelectBox<String> box = new SelectBox<>(style);
      List<String> items = new ArrayList<>();
      items.add(T.text("select"));
      question.options().forEach(option -> items.add(option.text().text(language)));
      box.setItems(items.toArray(String[]::new));
      box.setUserObject(Cursors.INTERACT);
      // The placeholder stays muted until a real option is chosen.
      box.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              Color color = box.getSelectedIndex() <= 0 ? MUTED : INK;
              style.fontColor = color;
              style.overFontColor = color;
            }
          });
      card.add(box)
          .width(value(() -> Math.min(420f, contentWidth())))
          .height(INPUT_HEIGHT)
          .padTop(10)
          .row();
      return () -> {
        int index = box.getSelectedIndex();
        if (index <= 0) return null;
        return JSON.objectNode().put("selected", question.options().get(index - 1).id());
      };
    }

    private Supplier<JsonNode> matrix(Table card, Matrix question, Field field) {
      int columns = question.columns().size();
      Value rowLabelWidth = value(() -> contentWidth() * 0.36f);
      Value columnWidth = value(() -> contentWidth() * 0.64f / columns);
      Table header = new Table();
      header.add().width(rowLabelWidth);
      for (Option column : question.columns()) {
        Label label = wrappedLabel(column.text().text(language), 14, MUTED, FONT_REGULAR);
        label.setAlignment(Align.center);
        header.add(label).width(value(() -> columnWidth.get(null) - 6)).pad(0, 3, 0, 3).bottom();
      }
      card.add(header).padTop(10).padBottom(4).row();
      List<Table> rows = new ArrayList<>();
      List<ButtonGroup<CheckBox>> groups = new ArrayList<>();
      for (Option rowOption : question.rows()) {
        Table row = new Table();
        row.add(wrappedLabel(rowOption.text().text(language), 17, INK, FONT_REGULAR))
            .width(value(() -> rowLabelWidth.get(null) - 14))
            .padLeft(10)
            .padRight(4)
            .left();
        ButtonGroup<CheckBox> group = group();
        for (int column = 0; column < columns; column++) {
          CheckBox box = checkBox("", false);
          group.add(box);
          row.add(new Container<>(box)).width(columnWidth);
        }
        rows.add(row);
        groups.add(group);
        card.add(row).width(content).minHeight(44).row();
      }
      // Stripes aid reading; after a failed check, unanswered rows turn red.
      field.marker =
          invalid -> {
            for (int index = 0; index < rows.size(); index++) {
              boolean missing = invalid && groups.get(index).getCheckedIndex() < 0;
              Color color = missing ? ROW_MISSING : index % 2 == 0 ? STRIPE : null;
              rows.get(index).setBackground(color == null ? null : tint(color));
            }
          };
      field.marker.accept(false);
      return () -> {
        ObjectNode answer = JSON.objectNode();
        for (int index = 0; index < groups.size(); index++) {
          int column = groups.get(index).getCheckedIndex();
          if (column >= 0) {
            answer.put(question.rows().get(index).id(), question.columns().get(column).id());
          }
        }
        return answer.isEmpty() ? null : answer;
      };
    }

    private String message(Question question, Problem problem) {
      return switch (problem) {
        case REQUIRED ->
            T.text(question instanceof Matrix ? "problem.required_rows" : "problem.required");
        case NUMBER_FORMAT -> T.text("problem.number_format");
        case NUMBER_RANGE -> T.text("problem.number_range");
        case TOO_LONG -> T.text("problem.too_long");
        case OTHER_TEXT -> T.text("problem.other_text");
        case INVALID -> T.text("problem.invalid");
      };
    }

    private String localized(Optional<LocalizedText> text) {
      return text.map(value -> value.text(language)).orElse("");
    }

    /**
     * Question titles use markup to color the required marker; the font itself stays white.
     *
     * @param text title with color markup
     * @return wrapping title label
     */
    private Label markupLabel(String text) {
      Label.LabelStyle style = new Label.LabelStyle();
      style.font =
          FontHelper.getFont(FontSpec.of(FONT, 20, Color.WHITE), FontHelper.FontRole.MARKUP);
      style.font.getData().markupEnabled = true;
      Label label = new Label(text, style);
      label.setWrap(true);
      return label;
    }

    /**
     * Input of one question with its card and error line.
     *
     * <p>The first page check collapses fixed errors and opens new ones. Later edits keep a fixed
     * error's line reserved, so the content below does not jump under the cursor.
     */
    private final class Field {
      private final Question question;
      private final Table card;
      private Supplier<JsonNode> value;
      private final Label error = wrappedLabel("", 16, ERROR, FONT);
      private Cell<Label> errorCell;
      private Consumer<Boolean> marker = invalid -> {};
      private boolean checked;

      private Field(Question question, Table card) {
        this.question = question;
        this.card = card;
      }

      boolean check(boolean pageCheck) {
        checked = true;
        Optional<Problem> problem = question.problem(value.get());
        if (problem.isPresent()) {
          error.setText(message(question, problem.get()));
          error.setVisible(true);
          errorCell.setActor(error).padTop(8);
        } else if (pageCheck) {
          errorCell.setActor(null).padTop(0);
        } else {
          error.setVisible(false);
        }
        marker.accept(problem.isPresent());
        card.setBackground(tint(problem.isPresent() ? CARD_ERROR : CARD));
        card.invalidateHierarchy();
        return problem.isEmpty();
      }
    }
  }

  /**
   * Shows where the answers ended up; the status color carries the outcome.
   *
   * @param result storage state of the answers
   * @param context dialog context for the confirm callback
   * @return result window
   */
  private static Table resultView(SurveyFeature.Result result, DialogContext context) {
    String key = "result." + result.name().toLowerCase();
    Table window = window();
    window
        .add(title(T.text(key + ".title")))
        .width(value(() -> dialogWidth(620) - 2 * WINDOW_PAD_X))
        .padBottom(34)
        .row();

    Color statusColor =
        switch (result) {
          case CONFIRMED -> Color.valueOf("1e8449");
          case SAVED_LOCALLY -> ACCENT;
          case UNREACHABLE, PENDING -> Color.valueOf("9a6409");
          case FAILED -> ERROR;
        };
    Label message = wrappedLabel(T.text(key + ".message"), 20, statusColor, FONT);
    message.setAlignment(Align.center);
    window.add(message).width(value(() -> dialogWidth(620) - 100)).padBottom(30).row();

    window
        .add(
            button(
                T.text("continue"),
                "green",
                20,
                () ->
                    DialogCallbackResolver.createButtonCallback(
                            context.dialogId(), DialogContextKeys.ON_CONFIRM)
                        .accept(null)))
        .minWidth(180);
    return window;
  }

  private static Table window() {
    Table window = new Table();
    window.setBackground(UIUtils.defaultSkin().getDrawable("window_background_big_blue"));
    window.pad(14, WINDOW_PAD_X, 22, WINDOW_PAD_X);
    window.top();
    window.defaults().center();
    return window;
  }

  private static Label title(String text) {
    Label title = label(text, 28, Color.WHITE, FONT);
    title.setAlignment(Align.center);
    title.setEllipsis(true);
    return title;
  }

  private static TextButton button(String text, String styleName, int size, Runnable action) {
    TextButton button = Scene2dElementFactory.createButton(text, styleName, size);
    button.getLabelCell().pad(0, 12, 0, 12);
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            action.run();
          }
        });
    return button;
  }

  private static CheckBox checkBox(String text, boolean square) {
    CheckBox.CheckBoxStyle style =
        new CheckBox.CheckBoxStyle(
            UIUtils.defaultSkin().get(square ? "default" : "radio", CheckBox.CheckBoxStyle.class));
    style.font = font(FONT_REGULAR, 18);
    style.fontColor = INK;
    CheckBox box = new CheckBox(text, style);
    box.getImageCell().size(26).padRight(text.isEmpty() ? 0 : 10);
    box.getLabel().setWrap(true);
    box.getLabelCell().growX();
    box.left();
    box.setUserObject(Cursors.INTERACT);
    return box;
  }

  private static TextButton.TextButtonStyle toggleStyle() {
    Skin skin = UIUtils.defaultSkin();
    TextButton.TextButtonStyle style =
        new TextButton.TextButtonStyle(skin.get("blue-outline", TextButton.TextButtonStyle.class));
    style.font = font(FONT, 18);
    style.fontColor = INK;
    style.over = style.up;
    style.overFontColor = ACCENT;
    style.down = skin.getDrawable("blue_square_flat");
    style.checked = skin.getDrawable("blue_square_depth_flat");
    style.checkedFontColor = Color.WHITE;
    style.checkedOverFontColor = Color.WHITE;
    return style;
  }

  /**
   * Text input with a thin border at rest and a strong border while typing, so focus is clearly
   * visible.
   *
   * @param multiline whether to create a text area
   * @return text field or text area
   */
  private static TextField textField(boolean multiline) {
    Skin skin = UIUtils.defaultSkin();
    TextField.TextFieldStyle style =
        new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
    style.font = font(FONT_REGULAR, 18);
    style.messageFont = style.font;
    style.fontColor = INK;
    style.messageFontColor = MUTED;
    style.background = skin.newDrawable("input_square");
    // The skin's padding puts the first line against the top border of a text area.
    if (multiline) style.background.setTopHeight(style.background.getTopHeight() + 6);
    style.focusedBackground = outlined(style.background, ACCENT);
    TextField field = multiline ? new TextArea("", style) : new TextField("", style);
    field.setUserObject(Cursors.TEXT);
    return field;
  }

  /**
   * Draws a two-pixel border over a drawable while keeping its padding.
   *
   * @param base drawable below the border
   * @param color border color
   * @return bordered drawable
   */
  private static Drawable outlined(Drawable base, Color color) {
    Drawable line = tint(color);
    return new BaseDrawable(base) {
      @Override
      public void draw(Batch batch, float x, float y, float width, float height) {
        base.draw(batch, x, y, width, height);
        line.draw(batch, x, y, width, 2);
        line.draw(batch, x, y + height - 2, width, 2);
        line.draw(batch, x, y, 2, height);
        line.draw(batch, x + width - 2, y, 2, height);
      }
    };
  }

  /**
   * List rows get room around their text, like the other inputs.
   *
   * @param drawable unshared drawable to pad
   * @return the same drawable
   */
  private static Drawable padded(Drawable drawable) {
    drawable.setTopHeight(8);
    drawable.setBottomHeight(8);
    drawable.setLeftWidth(10);
    drawable.setRightWidth(10);
    return drawable;
  }

  private static <T extends Button> ButtonGroup<T> group() {
    ButtonGroup<T> group = new ButtonGroup<>();
    group.setMinCheckCount(0);
    return group;
  }

  private static String numberHint(NumberInput question) {
    String kind = T.text(question.decimals() ? "number_hint.decimal" : "number_hint.integer");
    if (question.min().isPresent() && question.max().isPresent()) {
      return T.text(
          "number_hint.between",
          kind,
          question.min().get().toPlainString(),
          question.max().get().toPlainString());
    }
    if (question.min().isPresent()) {
      return T.text("number_hint.min", kind, question.min().get().toPlainString());
    }
    if (question.max().isPresent()) {
      return T.text("number_hint.max", kind, question.max().get().toPlainString());
    }
    return kind;
  }

  private static Label label(String text, int size, Color color, String font) {
    return Scene2dElementFactory.createLabel(text, FontSpec.of(font, size, color));
  }

  /**
   * Wrapping label; its cell must set a width, otherwise it wraps after every character.
   *
   * @param text label text
   * @param size font size
   * @param color text color
   * @param font font path
   * @return wrapping label
   */
  private static Label wrappedLabel(String text, int size, Color color, String font) {
    Label label = label(text, size, color, font);
    label.setWrap(true);
    return label;
  }

  private static BitmapFont font(String path, int size) {
    return FontHelper.getFont(FontSpec.of(path, size, Color.WHITE));
  }

  private static Drawable tint(Color color) {
    return UIUtils.defaultSkin().newDrawable("white", color);
  }

  /**
   * Width or height that follows the current window; evaluated on every layout.
   *
   * @param supplier computes the size
   * @return layout value
   */
  private static Value value(DoubleSupplier supplier) {
    return new Value() {
      @Override
      public float get(Actor context) {
        return (float) supplier.getAsDouble();
      }
    };
  }

  /**
   * Escapes LibGDX color markup in authored text.
   *
   * @param text authored text
   * @return text that renders literally in a markup label
   */
  private static String escape(String text) {
    return text.replace("[", "[[");
  }

  /**
   * Animates trailing dots so a pending submission visibly waits.
   *
   * @param label label to animate
   * @param text text before the dots
   * @return endless action
   */
  private static Action dots(Label label, String text) {
    return new Action() {
      private float time;

      @Override
      public boolean act(float delta) {
        time += delta;
        label.setText(text + ".".repeat(1 + (int) (time * 2) % 3));
        return false;
      }
    };
  }
}
