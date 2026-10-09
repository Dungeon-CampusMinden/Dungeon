package feature.survey;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.language.Language;
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
import java.util.function.Supplier;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Client view of a survey dialog: the paged form, or the storage result after submitting.
 *
 * <p>The form validates each page with the same rules the server applies and sends all answers as
 * one JSON object keyed by question ID.
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
  private static final Color CARD = new Color(1, 1, 1, 0.8f);
  private static final Color CARD_ERROR = Color.valueOf("fbe9e7");
  private static final Color INFO = Color.valueOf("dcefff");
  private static final Color STRIPE = new Color(0.86f, 0.88f, 0.92f, 0.6f);
  private static final float CARD_PAD = 18;

  private SurveyDialog() {}

  /**
   * Builds the survey form or, when the context carries a result, the result view.
   *
   * @param context dialog context with the room ID and an optional result
   * @return dialog actor
   */
  static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    String roomId = context.require(SurveyFeature.ROOM_ID_KEY, String.class);
    SurveyDefinition survey =
        SurveyDefinition.load(roomId)
            .orElseThrow(() -> new DialogCreationException("No valid survey for room " + roomId));
    Optional<String> result = context.find(SurveyFeature.RESULT_KEY, String.class);
    Table window =
        result.isPresent()
            ? resultView(survey, SurveyFeature.Result.valueOf(result.get()), context)
            : new Form(survey, context).window;
    return new BaseContainerUI(window);
  }

  /** Paged form state; answers live in the input widgets until submission. */
  private static final class Form {
    private final SurveyDefinition survey;
    private final DialogContext context;
    private final Language language = Game.localization().currentLanguage();
    private final float width = Math.min(860f, Game.windowWidth() - 60f);
    // Window padding, scroll content padding, and card padding.
    private final float contentWidth = width - 52f - 16f - 2 * CARD_PAD;
    private final Table window = window(width);
    private final Label pageTitle = label("", 22, INK, FONT);
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
        Table content = new Table();
        content.top().left().pad(4, 2, 12, 14);
        List<Field> pageFields = new ArrayList<>();
        for (Question question : definition.questions()) {
          Field field = field(question);
          pageFields.add(field);
          content.add(field.card()).growX().padBottom(12).row();
        }
        pages.add(content);
        fields.add(pageFields);
      }

      window.add(title(survey.title().text(language))).width(width - 52).padBottom(22).row();
      Table pageHeader = new Table();
      pageHeader.add(pageTitle).growX().left();
      pageHeader.add(pageCounter).right().bottom();
      window.add(pageHeader).growX().padBottom(10).row();
      scroll = Scene2dElementFactory.createScrollPane(pages.getFirst(), false, true);
      scroll.setStyle(new ScrollPane.ScrollPaneStyle(scroll.getStyle()));
      scroll.getStyle().background = null;
      scroll.setFadeScrollBars(false);
      scroll.setScrollbarsOnTop(false);
      window
          .add(scroll)
          .width(width - 52)
          .height(Math.max(240f, Math.min(460f, Game.windowHeight() - 250f)))
          .row();
      window.add(footer).growX().padTop(14).row();
      showPage(0);
    }

    private void showPage(int index) {
      page = index;
      SurveyDefinition.Page definition = survey.pages().get(index);
      pageTitle.setText(definition.title().map(text -> text.text(language)).orElse(""));
      pageCounter.setText(T.text("page", index + 1, survey.pages().size()));
      scroll.setActor(pages.get(index));
      Scene2dElementFactory.scrollPaneScrollTo(scroll, 0, 0);
      showNavigation();
    }

    private void showNavigation() {
      footer.clearChildren();
      if (survey.skippable()) {
        footer.add(button(T.text("skip"), "red-outline", this::confirmSkip)).left();
      }
      footer.add().growX();
      if (page > 0) {
        footer.add(button(T.text("back"), "blue-outline", () -> showPage(page - 1))).padRight(10);
      }
      boolean last = page == survey.pages().size() - 1;
      footer.add(
          button(T.text(last ? "submit" : "next"), "green", last ? this::submit : this::next));
    }

    private void confirmSkip() {
      footer.clearChildren();
      footer.add(label(T.text("skip_confirm"), 18, INK, FONT)).growX().left();
      footer.add(button(T.text("skip_cancel"), "blue-outline", this::showNavigation)).padRight(10);
      footer.add(
          button(
              T.text("skip"),
              "red-outline",
              () ->
                  DialogCallbackResolver.createButtonCallback(
                          context.dialogId(), DialogContextKeys.ON_CANCEL)
                      .accept(null)));
    }

    private void next() {
      if (validate(page)) showPage(page + 1);
    }

    private void submit() {
      for (int index = 0; index < pages.size(); index++) {
        boolean valid =
            fields.get(index).stream()
                .allMatch(field -> field.question().problem(field.value().get()).isEmpty());
        if (!valid) {
          if (index != page) showPage(index);
          validate(index);
          return;
        }
      }
      ObjectNode answers = JSON.objectNode();
      for (List<Field> pageFields : fields) {
        for (Field field : pageFields) {
          JsonNode value = field.value().get();
          if (value != null) answers.set(field.question().id(), value);
        }
      }
      showSending();
      DialogCallbackResolver.createButtonCallback(context.dialogId(), DialogContextKeys.ON_CONFIRM)
          .accept(new DialogResponseMessage.StringValue(answers.toString()));
    }

    /** Marks invalid questions of one page and scrolls to the first one. */
    private boolean validate(int index) {
      Field first = null;
      for (Field field : fields.get(index)) {
        Optional<Problem> problem = field.question().problem(field.value().get());
        field.show(problem.map(p -> message(field.question(), p)));
        if (problem.isPresent() && first == null) first = field;
      }
      if (first == null) return true;
      // Error lines changed the card heights; lay out again before measuring.
      scroll.invalidate();
      scroll.validate();
      Table card = first.card();
      float top = pages.get(index).getHeight() - card.getY() - card.getHeight();
      scroll.setScrollY(Math.max(0, top - 4));
      return false;
    }

    private void showSending() {
      Label sending = label(T.text("sending"), 22, INK, FONT);
      sending.setAlignment(Align.center);
      sending.addAction(dots(sending, T.text("sending")));
      Table content = new Table();
      content.add(sending).expand().center();
      scroll.setActor(content);
      pageTitle.setText("");
      pageCounter.setText("");
      footer.clearChildren();
      footer.add().height(48);
    }

    private Field field(Question question) {
      Table card = new Table();
      card.setBackground(tint(question instanceof Info ? INFO : CARD));
      card.pad(14, CARD_PAD, 14, CARD_PAD);
      card.defaults().left();
      String title = "[#" + INK + "]" + escape(question.text().text(language)) + "[]";
      if (question.required()) title += " [#" + ERROR + "]*[]";
      card.add(markupLabel(title)).width(contentWidth).row();
      question
          .description()
          .ifPresent(
              description ->
                  card.add(wrappedLabel(description.text(language), 16, MUTED, FONT_REGULAR))
                      .width(contentWidth)
                      .padTop(4)
                      .row());
      Supplier<JsonNode> value =
          switch (question) {
            case Text text -> text(card, text);
            case NumberInput number -> number(card, number);
            case Scale scale -> scale(card, scale);
            case Choice choice ->
                choice.kind() == ChoiceKind.DROPDOWN
                    ? dropdown(card, choice)
                    : choice(card, choice);
            case Matrix matrix -> matrix(card, matrix);
            case Info ignored -> () -> null;
          };
      Label error = wrappedLabel("", 16, ERROR, FONT);
      Cell<Label> errorCell = card.add((Label) null).width(contentWidth);
      Field field = new Field(question, card, value, error, errorCell);
      // Input widgets fire bubbling change events; a shown error disappears once it is fixed.
      card.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              if (errorCell.hasActor()) {
                field.show(question.problem(value.get()).map(p -> message(question, p)));
              }
            }
          });
      return field;
    }

    private Supplier<JsonNode> text(Table card, Text question) {
      TextField field = question.multiline() ? new TextArea("", textStyle()) : textField();
      field.setMaxLength(question.maxLength());
      field.setMessageText(T.text("placeholder"));
      field.setUserObject(Cursors.TEXT);
      card.add(field).width(contentWidth).height(question.multiline() ? 118 : 42).padTop(10).row();
      return () -> field.getText().isBlank() ? null : JSON.stringNode(field.getText().trim());
    }

    private Supplier<JsonNode> number(Table card, NumberInput question) {
      TextField field = textField();
      field.setTextFieldFilter(
          (ignored, c) -> Character.isDigit(c) || c == '-' || c == ',' || c == '.');
      field.setMaxLength(20);
      Table row = new Table();
      row.add(field).width(180).height(42);
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

    private Supplier<JsonNode> scale(Table card, Scale question) {
      ButtonGroup<TextButton> group = group();
      Table row = new Table();
      question
          .minLabel()
          .ifPresent(
              text -> {
                Label label = wrappedLabel(text.text(language), 15, MUTED, FONT_REGULAR);
                label.setAlignment(Align.right);
                row.add(label).width(130).padRight(12);
              });
      for (int value = question.min(); value <= question.max(); value++) {
        TextButton button = new TextButton(String.valueOf(value), toggleStyle());
        button.pad(4);
        button.setUserObject(Cursors.INTERACT);
        group.add(button);
        row.add(button).size(48, 44).padRight(6);
      }
      question
          .maxLabel()
          .ifPresent(
              text ->
                  row.add(wrappedLabel(text.text(language), 15, MUTED, FONT_REGULAR)).width(130));
      card.add(row).padTop(12).row();
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
        card.add(box).width(contentWidth).padTop(8).row();
      }
      TextField otherText = textField();
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
        row.add(otherText).growX().height(40);
        card.add(row).width(contentWidth).padTop(8).row();
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
        if (ids.size() > question.options().size()
            && boxes.getLast().isChecked()
            && !otherText.getText().isBlank()) {
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
      style.fontColor = INK;
      style.listStyle = new com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle(style.listStyle);
      style.listStyle.font = style.font;
      style.listStyle.fontColorUnselected = INK;
      style.listStyle.fontColorSelected = Color.WHITE;
      SelectBox<String> box = new SelectBox<>(style);
      List<String> items = new ArrayList<>();
      items.add(T.text("select"));
      question.options().forEach(option -> items.add(option.text().text(language)));
      box.setItems(items.toArray(String[]::new));
      box.setUserObject(Cursors.INTERACT);
      card.add(box).width(Math.min(420f, contentWidth)).height(44).padTop(10).row();
      return () -> {
        int index = box.getSelectedIndex();
        if (index <= 0) return null;
        return JSON.objectNode().put("selected", question.options().get(index - 1).id());
      };
    }

    private Supplier<JsonNode> matrix(Table card, Matrix question) {
      float rowLabelWidth = contentWidth * 0.36f;
      float columnWidth = (contentWidth - rowLabelWidth) / question.columns().size();
      Table header = new Table();
      header.add().width(rowLabelWidth);
      for (Option column : question.columns()) {
        Label label = wrappedLabel(column.text().text(language), 14, MUTED, FONT_REGULAR);
        label.setAlignment(Align.center);
        header.add(label).width(columnWidth - 6).pad(0, 3, 0, 3).bottom();
      }
      card.add(header).padTop(10).padBottom(4).row();
      List<ButtonGroup<CheckBox>> groups = new ArrayList<>();
      for (int index = 0; index < question.rows().size(); index++) {
        Option rowOption = question.rows().get(index);
        Table row = new Table();
        if (index % 2 == 0) row.setBackground(tint(STRIPE));
        row.add(wrappedLabel(rowOption.text().text(language), 17, INK, FONT_REGULAR))
            .width(rowLabelWidth - 10)
            .padLeft(10)
            .left();
        ButtonGroup<CheckBox> group = group();
        for (int column = 0; column < question.columns().size(); column++) {
          CheckBox box = checkBox("", false);
          group.add(box);
          row.add(box).width(columnWidth).center();
        }
        groups.add(group);
        card.add(row).width(contentWidth).height(44).row();
      }
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

    /** Question titles use markup to color the required marker; the font itself stays white. */
    private Label markupLabel(String text) {
      Label.LabelStyle style = new Label.LabelStyle();
      style.font =
          FontHelper.getFont(FontSpec.of(FONT, 20, Color.WHITE), FontHelper.FontRole.MARKUP);
      style.font.getData().markupEnabled = true;
      Label label = new Label(text, style);
      label.setWrap(true);
      return label;
    }

    private TextField textField() {
      TextField field = new TextField("", textStyle());
      field.setUserObject(Cursors.TEXT);
      return field;
    }
  }

  /**
   * Input of one question with its card and error line.
   *
   * @param question displayed question
   * @param card container of the question
   * @param value current answer, {@code null} when unanswered
   * @param error label showing the current problem
   * @param errorCell cell that holds the error label only while a problem exists
   */
  private record Field(
      Question question, Table card, Supplier<JsonNode> value, Label error, Cell<Label> errorCell) {
    void show(Optional<String> problem) {
      if (question instanceof Info) return;
      error.setText(problem.orElse(""));
      errorCell.setActor(problem.isPresent() ? error : null).padTop(problem.isPresent() ? 8 : 0);
      card.setBackground(tint(problem.isPresent() ? CARD_ERROR : CARD));
      card.invalidateHierarchy();
    }
  }

  private static Table resultView(
      SurveyDefinition survey, SurveyFeature.Result result, DialogContext context) {
    String key = "result." + result.name().toLowerCase();
    float width = Math.min(620f, Game.windowWidth() - 60f);
    Table window = window(width);
    window.add(title(T.text(key + ".title"))).width(width - 52).padBottom(30).row();

    Color statusColor =
        switch (result) {
          case CONFIRMED -> Color.valueOf("1e8449");
          case SAVED_LOCALLY -> ACCENT;
          case PENDING -> Color.valueOf("b9770e");
          case FAILED -> ERROR;
        };
    Table status = new Table();
    status.setBackground(tint(statusColor));
    status.pad(6, 14, 6, 14);
    status.add(label(T.text(key + ".status"), 17, Color.WHITE, FONT));
    window.add(status).padBottom(16).row();

    Label message = wrappedLabel(T.text(key + ".message"), 19, INK, FONT_REGULAR);
    message.setAlignment(Align.center);
    window.add(message).width(width - 80).padBottom(10).row();
    Label questionnaire =
        wrappedLabel(
            survey.title().text(Game.localization().currentLanguage()), 15, MUTED, FONT_REGULAR);
    questionnaire.setAlignment(Align.center);
    window.add(questionnaire).width(width - 80).padBottom(24).row();

    window
        .add(
            button(
                T.text("continue"),
                "green",
                () ->
                    DialogCallbackResolver.createButtonCallback(
                            context.dialogId(), DialogContextKeys.ON_CONFIRM)
                        .accept(null)))
        .minWidth(180);
    return window;
  }

  private static Table window(float width) {
    Table window = new Table();
    window.setBackground(UIUtils.defaultSkin().getDrawable("window_background_big_blue"));
    window.pad(14, 26, 22, 26);
    window.top();
    window.setWidth(width);
    window.defaults().center();
    return window;
  }

  private static Label title(String text) {
    Label title = label(text, 28, Color.WHITE, FONT);
    title.setAlignment(Align.center);
    title.setEllipsis(true);
    return title;
  }

  private static TextButton button(String text, String style, Runnable action) {
    TextButton button = Scene2dElementFactory.createButton(text, style, 20);
    button.getLabelCell().pad(0, 12, 0, 12);
    if (action != null) {
      button.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              action.run();
            }
          });
    }
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
    box.getLabel().setAlignment(Align.left);
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

  private static TextField.TextFieldStyle textStyle() {
    TextField.TextFieldStyle style =
        new TextField.TextFieldStyle(UIUtils.defaultSkin().get(TextField.TextFieldStyle.class));
    style.font = font(FONT_REGULAR, 18);
    style.messageFont = style.font;
    style.fontColor = INK;
    style.messageFontColor = MUTED;
    return style;
  }

  private static <T extends Button> ButtonGroup<T> group() {
    ButtonGroup<T> group = new ButtonGroup<>();
    group.setMinCheckCount(0);
    group.setMaxCheckCount(1);
    group.setUncheckLast(true);
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
    Label label = Scene2dElementFactory.createLabel(text, FontSpec.of(font, size, color));
    label.setAlignment(Align.left);
    return label;
  }

  /** Wrapping label; its cell must set a width, otherwise it wraps after every character. */
  private static Label wrappedLabel(String text, int size, Color color, String font) {
    Label label = label(text, size, color, font);
    label.setWrap(true);
    return label;
  }

  private static com.badlogic.gdx.graphics.g2d.BitmapFont font(String path, int size) {
    return FontHelper.getFont(FontSpec.of(path, size, Color.WHITE));
  }

  private static Drawable tint(Color color) {
    return UIUtils.defaultSkin().newDrawable("white", color);
  }

  /** Escapes LibGDX color markup in authored text. */
  private static String escape(String text) {
    return text.replace("[", "[[");
  }

  /** Animates trailing dots so a pending submission visibly waits. */
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
