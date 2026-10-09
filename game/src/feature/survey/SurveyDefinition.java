package feature.survey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import engine.language.LocalizedText;
import engine.utils.logging.DungeonLogger;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import tracking.core.TrackingJson;

/**
 * Immutable, validated survey loaded from {@code surveys/<roomId>.json}.
 *
 * <p>The same definition renders the client form and validates the submitted answers on the
 * authoritative server. See {@code README.md} in this package for the file format.
 *
 * @param roomId tracking room ID matching the file name
 * @param questionnaireId stable ID stored with every recorded answer
 * @param title localized survey title
 * @param skippable whether players may leave without submitting
 * @param pages ordered pages of questions
 */
public record SurveyDefinition(
    String roomId,
    String questionnaireId,
    LocalizedText title,
    boolean skippable,
    List<Page> pages) {

  /** Current JSON schema version. */
  public static final int SCHEMA_VERSION = 1;

  /** Reserved option ID of the free-text "other" choice. */
  public static final String OTHER_ID = "other";

  /** Maximum length of the free text attached to the "other" choice. */
  public static final int OTHER_MAX_LENGTH = 200;

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(SurveyDefinition.class);
  private static final String ID_PATTERN = "[A-Za-z0-9_-]+";

  /**
   * Creates and validates a survey definition.
   *
   * @param roomId tracking room ID matching the file name
   * @param questionnaireId stable ID stored with every recorded answer
   * @param title localized survey title
   * @param skippable whether players may leave without submitting
   * @param pages ordered pages of questions
   */
  public SurveyDefinition {
    requireId(roomId, "roomId");
    requireId(questionnaireId, "questionnaireId");
    Objects.requireNonNull(title, "title");
    pages = List.copyOf(pages);
    if (pages.isEmpty()) throw new IllegalArgumentException("survey needs at least one page");
    Set<String> questionIds = new HashSet<>();
    for (Page page : pages) {
      for (Question question : page.questions()) {
        if (!questionIds.add(question.id())) {
          throw new IllegalArgumentException("duplicate question id: " + question.id());
        }
      }
    }
  }

  /**
   * Loads one room's survey when the asset exists and is valid.
   *
   * @param roomId tracking room ID used in {@code surveys/<roomId>.json}
   * @return the parsed survey, or empty if the asset is absent or invalid
   */
  public static Optional<SurveyDefinition> load(String roomId) {
    requireId(roomId, "roomId");
    String path = "surveys/" + roomId + ".json";
    if (Gdx.files == null) return Optional.empty();
    FileHandle file = Gdx.files.internal(path);
    if (!file.exists()) return Optional.empty();
    try {
      SurveyDefinition survey = parse(file.readString(StandardCharsets.UTF_8.name()));
      if (!roomId.equals(survey.roomId())) {
        throw new IllegalArgumentException(
            "roomId '" + survey.roomId() + "' does not match file name '" + roomId + "'");
      }
      return Optional.of(survey);
    } catch (RuntimeException exception) {
      LOGGER.warn("Ignoring invalid survey file '{}': {}", path, exception.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Parses and validates a survey from JSON.
   *
   * @param json survey document
   * @return parsed survey
   * @throws IllegalArgumentException if the document violates the format
   */
  public static SurveyDefinition parse(String json) {
    JsonNode root = TrackingJson.object(json);
    allowOnly(
        root,
        "survey",
        "schemaVersion",
        "roomId",
        "questionnaireId",
        "title",
        "skippable",
        "pages");
    JsonNode version = require(root, "schemaVersion", "survey");
    if (!version.isIntegralNumber() || version.intValue() != SCHEMA_VERSION) {
      throw new IllegalArgumentException("Unsupported survey schemaVersion: " + version);
    }
    List<Page> pages = new ArrayList<>();
    for (JsonNode page : array(root, "pages", "survey")) {
      allowOnly(page, "page", "title", "questions");
      List<Question> questions = new ArrayList<>();
      for (JsonNode question : array(page, "questions", "page")) {
        questions.add(question(question));
      }
      pages.add(new Page(optionalText(page, "title", "page"), questions));
    }
    JsonNode skippable = require(root, "skippable", "survey");
    if (!skippable.isBoolean()) throw new IllegalArgumentException("skippable must be a boolean");
    return new SurveyDefinition(
        string(root, "roomId", "survey"),
        string(root, "questionnaireId", "survey"),
        text(root, "title", "survey"),
        skippable.booleanValue(),
        pages);
  }

  /**
   * Returns all questions in display order.
   *
   * @return flattened questions of every page
   */
  public List<Question> questions() {
    return pages.stream().flatMap(page -> page.questions().stream()).toList();
  }

  /**
   * Checks a complete submission keyed by question ID.
   *
   * @param answers JSON object mapping question IDs to answers; missing or {@code null} values
   *     count as unanswered
   * @return problems by question ID in display order, empty when the submission is valid
   */
  public Map<String, Problem> problems(JsonNode answers) {
    Map<String, Problem> problems = new LinkedHashMap<>();
    if (answers == null || !answers.isObject()) {
      problems.put("", Problem.INVALID);
      return problems;
    }
    Set<String> known = new HashSet<>();
    for (Question question : questions()) {
      known.add(question.id());
      question.problem(answers.get(question.id())).ifPresent(p -> problems.put(question.id(), p));
    }
    for (String id : answers.propertyNames()) {
      if (!known.contains(id)) problems.put(id, Problem.INVALID);
    }
    return problems;
  }

  /** Reason why an answer cannot be submitted. */
  public enum Problem {
    /** A required question has no answer. */
    REQUIRED,
    /** A number question contains text that is not a number. */
    NUMBER_FORMAT,
    /** A number lies outside the allowed range or has forbidden decimals. */
    NUMBER_RANGE,
    /** A text answer exceeds its maximum length. */
    TOO_LONG,
    /** The "other" choice is selected without describing it. */
    OTHER_TEXT,
    /** The answer does not match the question; only a modified client sends this. */
    INVALID
  }

  /**
   * One page of the survey.
   *
   * @param title optional page heading
   * @param questions ordered questions on this page
   */
  public record Page(Optional<LocalizedText> title, List<Question> questions) {
    /**
     * Creates a page.
     *
     * @param title optional page heading
     * @param questions ordered questions on this page
     */
    public Page {
      Objects.requireNonNull(title, "title");
      questions = List.copyOf(questions);
      if (questions.isEmpty()) throw new IllegalArgumentException("page needs questions");
    }
  }

  /**
   * Selectable option, matrix row, or matrix column.
   *
   * @param id stable ID stored in answers
   * @param text localized label
   */
  public record Option(String id, LocalizedText text) {
    /**
     * Creates an option.
     *
     * @param id stable ID stored in answers
     * @param text localized label
     */
    public Option {
      requireId(id, "option id");
      Objects.requireNonNull(text, "text");
    }
  }

  /** One survey element; every type except {@link Info} accepts an answer. */
  public sealed interface Question permits Text, NumberInput, Scale, Choice, Matrix, Info {
    /**
     * Returns the stable question ID.
     *
     * @return question ID used in recorded answers
     */
    String id();

    /**
     * Returns the question text.
     *
     * @return localized question text
     */
    LocalizedText text();

    /**
     * Returns optional help text shown below the question.
     *
     * @return localized description
     */
    Optional<LocalizedText> description();

    /**
     * Returns whether a submission must answer this question.
     *
     * @return whether the question is required
     */
    boolean required();

    /**
     * Checks one answer of this question.
     *
     * @param answer submitted answer, or {@code null} when unanswered
     * @return the problem, or empty when the answer can be recorded
     */
    default Optional<Problem> problem(JsonNode answer) {
      if (answer == null || answer.isNull() || answer.isMissingNode()) {
        return required() ? Optional.of(Problem.REQUIRED) : Optional.empty();
      }
      return answeredProblem(answer);
    }

    /**
     * Checks an answer that is present.
     *
     * @param answer submitted answer
     * @return the problem, or empty when the answer can be recorded
     */
    Optional<Problem> answeredProblem(JsonNode answer);
  }

  /**
   * Free text; the answer is a non-blank string.
   *
   * @param id question ID
   * @param text question text
   * @param description optional help text
   * @param required whether an answer is required
   * @param multiline whether the answer is a paragraph rather than one line
   * @param maxLength maximum answer length
   */
  public record Text(
      String id,
      LocalizedText text,
      Optional<LocalizedText> description,
      boolean required,
      boolean multiline,
      int maxLength)
      implements Question {
    @Override
    public Optional<Problem> answeredProblem(JsonNode answer) {
      if (!answer.isString() || answer.stringValue().isBlank()) return Optional.of(Problem.INVALID);
      if (!multiline && answer.stringValue().contains("\n")) return Optional.of(Problem.INVALID);
      if (answer.stringValue().length() > maxLength) return Optional.of(Problem.TOO_LONG);
      return Optional.empty();
    }
  }

  /**
   * Number entry; the answer is a JSON number.
   *
   * @param id question ID
   * @param text question text
   * @param description optional help text
   * @param required whether an answer is required
   * @param min optional inclusive minimum
   * @param max optional inclusive maximum
   * @param decimals whether non-integral numbers are allowed
   */
  public record NumberInput(
      String id,
      LocalizedText text,
      Optional<LocalizedText> description,
      boolean required,
      Optional<BigDecimal> min,
      Optional<BigDecimal> max,
      boolean decimals)
      implements Question {
    @Override
    public Optional<Problem> answeredProblem(JsonNode answer) {
      if (!answer.isNumber()) return Optional.of(Problem.NUMBER_FORMAT);
      BigDecimal value = answer.decimalValue();
      boolean integral = value.stripTrailingZeros().scale() <= 0;
      if ((!decimals && !integral)
          || min.filter(bound -> value.compareTo(bound) < 0).isPresent()
          || max.filter(bound -> value.compareTo(bound) > 0).isPresent()) {
        return Optional.of(Problem.NUMBER_RANGE);
      }
      return Optional.empty();
    }
  }

  /**
   * Linear scale such as a 1-5 rating; the answer is an integer.
   *
   * @param id question ID
   * @param text question text
   * @param description optional help text
   * @param required whether an answer is required
   * @param min lowest value
   * @param max highest value
   * @param minLabel optional label of the lowest value
   * @param maxLabel optional label of the highest value
   */
  public record Scale(
      String id,
      LocalizedText text,
      Optional<LocalizedText> description,
      boolean required,
      int min,
      int max,
      Optional<LocalizedText> minLabel,
      Optional<LocalizedText> maxLabel)
      implements Question {
    /** Validates the range. */
    public Scale {
      if (min < 0 || max <= min || max - min > 10) {
        throw new IllegalArgumentException("scale " + id + " needs 0 <= min < max <= min + 10");
      }
    }

    @Override
    public Optional<Problem> answeredProblem(JsonNode answer) {
      if (!answer.isIntegralNumber() || answer.intValue() < min || answer.intValue() > max) {
        return Optional.of(Problem.INVALID);
      }
      return Optional.empty();
    }
  }

  /** Presentation of a {@link Choice}. */
  public enum ChoiceKind {
    /** One option, shown as radio buttons. */
    SINGLE,
    /** Any number of options, shown as check boxes. */
    MULTI,
    /** One option, shown as a drop-down list. */
    DROPDOWN
  }

  /**
   * Choice among options; the answer is {@code {"selected": id}} or, for {@link ChoiceKind#MULTI},
   * {@code {"selected": [ids]}}, plus {@code "otherText"} when {@link #OTHER_ID} is selected.
   *
   * @param id question ID
   * @param text question text
   * @param description optional help text
   * @param required whether an answer is required
   * @param kind single, multi, or drop-down presentation
   * @param options selectable options
   * @param other whether an extra "other" option with free text is offered
   */
  public record Choice(
      String id,
      LocalizedText text,
      Optional<LocalizedText> description,
      boolean required,
      ChoiceKind kind,
      List<Option> options,
      boolean other)
      implements Question {
    /** Validates the options. */
    public Choice {
      options = uniqueOptions(options, "option", id);
      if (other && kind == ChoiceKind.DROPDOWN) {
        throw new IllegalArgumentException("dropdown " + id + " cannot offer 'other'");
      }
    }

    @Override
    public Optional<Problem> answeredProblem(JsonNode answer) {
      if (!answer.isObject()) return Optional.of(Problem.INVALID);
      for (String property : answer.propertyNames()) {
        if (!property.equals("selected") && !property.equals("otherText")) {
          return Optional.of(Problem.INVALID);
        }
      }
      JsonNode selected = answer.get("selected");
      List<String> ids = new ArrayList<>();
      if (kind == ChoiceKind.MULTI && selected != null && selected.isArray()) {
        for (JsonNode value : selected) {
          if (!value.isString()) return Optional.of(Problem.INVALID);
          ids.add(value.stringValue());
        }
      } else if (kind != ChoiceKind.MULTI && selected != null && selected.isString()) {
        ids.add(selected.stringValue());
      } else {
        return Optional.of(Problem.INVALID);
      }
      if (ids.isEmpty() || new HashSet<>(ids).size() != ids.size()) {
        return Optional.of(Problem.INVALID);
      }
      Set<String> allowed = new HashSet<>();
      options.forEach(option -> allowed.add(option.id()));
      if (other) allowed.add(OTHER_ID);
      if (!allowed.containsAll(ids)) return Optional.of(Problem.INVALID);
      JsonNode otherText = answer.get("otherText");
      if (!ids.contains(OTHER_ID)) {
        return otherText == null ? Optional.empty() : Optional.of(Problem.INVALID);
      }
      if (otherText == null || !otherText.isString() || otherText.stringValue().isBlank()) {
        return Optional.of(Problem.OTHER_TEXT);
      }
      if (otherText.stringValue().length() > OTHER_MAX_LENGTH) return Optional.of(Problem.TOO_LONG);
      return Optional.empty();
    }
  }

  /**
   * Grid of statements rated on shared columns, such as a Likert block; the answer maps row IDs to
   * column IDs. A required matrix needs an answer in every row.
   *
   * @param id question ID
   * @param text question text
   * @param description optional help text
   * @param required whether every row must be answered
   * @param rows rated statements
   * @param columns shared answer columns
   */
  public record Matrix(
      String id,
      LocalizedText text,
      Optional<LocalizedText> description,
      boolean required,
      List<Option> rows,
      List<Option> columns)
      implements Question {
    /** Validates rows and columns. */
    public Matrix {
      rows = uniqueOptions(rows, "row", id);
      columns = uniqueOptions(columns, "column", id);
      if (columns.size() < 2) throw new IllegalArgumentException("matrix " + id + " needs columns");
    }

    @Override
    public Optional<Problem> answeredProblem(JsonNode answer) {
      if (!answer.isObject() || answer.isEmpty()) return Optional.of(Problem.INVALID);
      Set<String> rowIds = new HashSet<>();
      rows.forEach(row -> rowIds.add(row.id()));
      Set<String> columnIds = new HashSet<>();
      columns.forEach(column -> columnIds.add(column.id()));
      for (Map.Entry<String, JsonNode> cell : answer.properties()) {
        if (!rowIds.contains(cell.getKey())
            || !cell.getValue().isString()
            || !columnIds.contains(cell.getValue().stringValue())) {
          return Optional.of(Problem.INVALID);
        }
      }
      if (required && answer.size() < rows.size()) return Optional.of(Problem.REQUIRED);
      return Optional.empty();
    }
  }

  /**
   * Text block without an answer, such as an introduction or a privacy note.
   *
   * @param id element ID
   * @param text heading or main text
   * @param description optional additional text
   */
  public record Info(String id, LocalizedText text, Optional<LocalizedText> description)
      implements Question {
    @Override
    public boolean required() {
      return false;
    }

    @Override
    public Optional<Problem> answeredProblem(JsonNode answer) {
      return Optional.of(Problem.INVALID);
    }
  }

  private static Question question(JsonNode node) {
    String id = string(node, "id", "question");
    requireId(id, "question id");
    String type = string(node, "type", "question " + id);
    String where = "question " + id;
    LocalizedText text = text(node, "text", where);
    Optional<LocalizedText> description = optionalText(node, "description", where);
    List<String> common = List.of("id", "type", "text", "description", "required");
    boolean required = false;
    if (!type.equals("info")) {
      JsonNode value = node.get("required");
      if (value != null && !value.isBoolean()) {
        throw new IllegalArgumentException(where + ": required must be a boolean");
      }
      required = value != null && value.booleanValue();
    }
    return switch (type) {
      case "shortText", "longText" -> {
        allowOnly(node, where, common, "maxLength");
        boolean multiline = type.equals("longText");
        int maxLength = optionalInt(node, "maxLength", where).orElse(multiline ? 2000 : 200);
        if (maxLength < 1) throw new IllegalArgumentException(where + ": maxLength must be > 0");
        yield new Text(id, text, description, required, multiline, maxLength);
      }
      case "number" -> {
        allowOnly(node, where, common, "min", "max", "decimals");
        Optional<BigDecimal> min = optionalDecimal(node, "min", where);
        Optional<BigDecimal> max = optionalDecimal(node, "max", where);
        if (min.isPresent() && max.isPresent() && min.get().compareTo(max.get()) > 0) {
          throw new IllegalArgumentException(where + ": min must not exceed max");
        }
        JsonNode decimals = node.get("decimals");
        if (decimals != null && !decimals.isBoolean()) {
          throw new IllegalArgumentException(where + ": decimals must be a boolean");
        }
        yield new NumberInput(
            id, text, description, required, min, max, decimals != null && decimals.booleanValue());
      }
      case "scale" -> {
        allowOnly(node, where, common, "min", "max", "minLabel", "maxLabel");
        yield new Scale(
            id,
            text,
            description,
            required,
            optionalInt(node, "min", where).orElse(1),
            optionalInt(node, "max", where).orElse(5),
            optionalText(node, "minLabel", where),
            optionalText(node, "maxLabel", where));
      }
      case "singleChoice", "multiChoice", "dropdown" -> {
        allowOnly(node, where, common, "options", "other");
        ChoiceKind kind =
            switch (type) {
              case "singleChoice" -> ChoiceKind.SINGLE;
              case "multiChoice" -> ChoiceKind.MULTI;
              default -> ChoiceKind.DROPDOWN;
            };
        JsonNode other = node.get("other");
        if (other != null && !other.isBoolean()) {
          throw new IllegalArgumentException(where + ": other must be a boolean");
        }
        yield new Choice(
            id,
            text,
            description,
            required,
            kind,
            options(node, "options", where),
            other != null && other.booleanValue());
      }
      case "matrix" -> {
        allowOnly(node, where, common, "rows", "columns");
        yield new Matrix(
            id,
            text,
            description,
            required,
            options(node, "rows", where),
            options(node, "columns", where));
      }
      case "info" -> {
        allowOnly(node, where, List.of("id", "type", "text", "description"));
        yield new Info(id, text, description);
      }
      default -> throw new IllegalArgumentException(where + ": unknown type '" + type + "'");
    };
  }

  private static List<Option> uniqueOptions(List<Option> options, String kind, String questionId) {
    options = List.copyOf(options);
    if (options.isEmpty()) {
      throw new IllegalArgumentException("question " + questionId + " needs " + kind + "s");
    }
    Set<String> ids = new HashSet<>();
    for (Option option : options) {
      if (option.id().equals(OTHER_ID) || !ids.add(option.id())) {
        throw new IllegalArgumentException(
            "question " + questionId + ": reserved or duplicate " + kind + " id " + option.id());
      }
    }
    return options;
  }

  private static List<Option> options(JsonNode node, String field, String where) {
    List<Option> options = new ArrayList<>();
    for (JsonNode option : array(node, field, where)) {
      allowOnly(option, where + "." + field, "id", "text");
      options.add(
          new Option(string(option, "id", where + "." + field), text(option, "text", where)));
    }
    return options;
  }

  private static void allowOnly(JsonNode node, String where, String... fields) {
    allowOnly(node, where, List.of(fields));
  }

  private static void allowOnly(
      JsonNode node, String where, List<String> common, String... specific) {
    List<String> fields = new ArrayList<>(common);
    fields.addAll(List.of(specific));
    allowOnly(node, where, fields);
  }

  private static void allowOnly(JsonNode node, String where, List<String> fields) {
    if (!node.isObject()) throw new IllegalArgumentException(where + " must be an object");
    for (String property : node.propertyNames()) {
      if (!fields.contains(property)) {
        throw new IllegalArgumentException(where + ": unknown field '" + property + "'");
      }
    }
  }

  private static JsonNode require(JsonNode node, String field, String where) {
    JsonNode value = node.get(field);
    if (value == null || value.isNull()) {
      throw new IllegalArgumentException(where + ": missing field '" + field + "'");
    }
    return value;
  }

  private static String string(JsonNode node, String field, String where) {
    JsonNode value = require(node, field, where);
    if (!value.isString() || value.stringValue().isBlank()) {
      throw new IllegalArgumentException(where + ": " + field + " must be a non-empty string");
    }
    return value.stringValue().trim();
  }

  private static JsonNode array(JsonNode node, String field, String where) {
    JsonNode value = require(node, field, where);
    if (!value.isArray())
      throw new IllegalArgumentException(where + ": " + field + " must be a list");
    return value;
  }

  private static Optional<Integer> optionalInt(JsonNode node, String field, String where) {
    JsonNode value = node.get(field);
    if (value == null) return Optional.empty();
    if (!value.isIntegralNumber() || !value.canConvertToInt()) {
      throw new IllegalArgumentException(where + ": " + field + " must be an integer");
    }
    return Optional.of(value.intValue());
  }

  private static Optional<BigDecimal> optionalDecimal(JsonNode node, String field, String where) {
    JsonNode value = node.get(field);
    if (value == null) return Optional.empty();
    if (!value.isNumber())
      throw new IllegalArgumentException(where + ": " + field + " not a number");
    return Optional.of(value.decimalValue());
  }

  private static LocalizedText text(JsonNode node, String field, String where) {
    JsonNode value = require(node, field, where);
    if (!value.isObject()) {
      throw new IllegalArgumentException(where + ": " + field + " must map 'de'/'en' to text");
    }
    Map<String, String> values = new LinkedHashMap<>();
    for (Map.Entry<String, JsonNode> entry : value.properties()) {
      if (!entry.getValue().isString()) {
        throw new IllegalArgumentException(where + ": " + field + " texts must be strings");
      }
      values.put(entry.getKey(), entry.getValue().stringValue());
    }
    return new LocalizedText(values);
  }

  private static Optional<LocalizedText> optionalText(JsonNode node, String field, String where) {
    return node.get(field) == null ? Optional.empty() : Optional.of(text(node, field, where));
  }

  private static void requireId(String id, String field) {
    if (id == null || !id.matches(ID_PATTERN)) {
      throw new IllegalArgumentException(field + " must contain only letters, digits, '_' or '-'");
    }
  }
}
