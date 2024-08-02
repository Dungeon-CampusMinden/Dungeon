package autocompletion;

import antlr_gen.AntlrGrammarLexer;
import antlr_gen.AntlrGrammarParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;

/** Query to fetch the completion items for a position in a document. */
public class CompletionItemQuery {
  /**
   * Fetches the completion items to show for the {@code position} in the {@code document}.
   *
   * @param position the position of the caret in the document.
   * @param document the document to complete in.
   * @param namesOfDefinedIds the names of all defined ids.
   * @return the completion items to show for the {@code position} in the {@code document}.
   */
  public static List<CompletionItem> fetchCompletionItems(
      Position position, String document, Set<String> namesOfDefinedIds) {
    String[] linesOfDocument = document.split("\n");
    String textUntilCaret = getTextUntilCaret(position, linesOfDocument);
    Lexer lexerUntilCaret = new AntlrGrammarLexer(CharStreams.fromString(textUntilCaret));
    AntlrGrammarParser parser = new AntlrGrammarParser(new CommonTokenStream(lexerUntilCaret));

    lexerUntilCaret.removeErrorListeners();
    lexerUntilCaret.addErrorListener(new ThrowErrorListener());

    List<Token> tokensUntilCaret =
        new ArrayList<>(
            lexerUntilCaret.getAllTokens().stream().filter(t -> t.getChannel() == 0).toList());
    HashSet<Integer> suggestedTokenTypes =
        AutocompletionUsingAntlrStateMachine.computeTokenTypesThatCouldFollow(
            tokensUntilCaret,
            parser,
            AntlrGrammarParser.RULE_start,
            AntlrGrammarParser.RULE_id_usage,
            AntlrGrammarLexer.ID);

    String wordBeforeCaret = getCharactersAfterLastWhitespace(textUntilCaret);

    List<String> taskPropertiesUsedBeforeCaretInSameBlock =
        tokensUntilCaret.reversed().stream()
            .takeWhile(
                t -> t.getText() == null || !(t.getText().equals("{") | t.getText().equals("}")))
            .map(Token::getText)
            .takeWhile(t -> t == null || !(t.equals("{") | t.equals("}")))
            .filter(allTaskPropertyKeywords::contains)
            .toList();

    ArrayList<CompletionItem> completionItems = new ArrayList<>();
    for (Integer suggestedTokenType : suggestedTokenTypes) {
      switch (suggestedTokenType) {
        case Token.EOF:
          continue;
        case AutocompletionUsingAntlrStateMachine.idUsageTokenType:
          completionItems.addAll(
              namesOfDefinedIds.stream().map(CompletionItemQuery::createCompletionItem).toList());
          break;
        case AntlrGrammarLexer.STRING:
        case AntlrGrammarLexer.INT:
        case AntlrGrammarLexer.DECIMAL:
          completionItems.add(
              createCompletionItem(namedTokensDefaultSuggestions.get(suggestedTokenType)));
          break;
        default:
          String literal = AntlrGrammarLexer.VOCABULARY.getLiteralName(suggestedTokenType);
          if (literal != null) {
            String literalWithoutApostrophes = literal.substring(1, literal.length() - 1);
            if (!literalWithoutApostrophes.equals(wordBeforeCaret)
                && !taskPropertiesUsedBeforeCaretInSameBlock.contains(literalWithoutApostrophes)) {

              completionItems.add(createCompletionItem(literalWithoutApostrophes));
              if (templateCompletionItems.containsKey(literalWithoutApostrophes)) {
                completionItems.add(templateCompletionItems.get(literalWithoutApostrophes));
              } else {
                addSuggestionWithExampleValueIfPossible(completionItems, literalWithoutApostrophes);
              }
            }
          }
      }
    }

    if (!wordBeforeCaret.isEmpty()) {
      removeMatchingCompletionItems(completionItems, wordBeforeCaret);
      moveCompletingCompletionItemsToStart(completionItems, wordBeforeCaret);
    }
    return completionItems;
  }

  private static void removeMatchingCompletionItems(
      ArrayList<CompletionItem> completionItems, String wordBeforeCaret) {
    List<CompletionItem> completionsThatMatchAlreadyTyped =
        completionItems.stream().filter(c -> c.getLabel().equals(wordBeforeCaret)).toList();
    completionItems.removeAll(completionsThatMatchAlreadyTyped);
  }

  private static void moveCompletingCompletionItemsToStart(
      ArrayList<CompletionItem> completionItems, String wordBeforeCaret) {
    List<CompletionItem> completionsThatMatchAlreadyTyped =
        completionItems.stream().filter(c -> c.getLabel().startsWith(wordBeforeCaret)).toList();
    completionItems.removeAll(completionsThatMatchAlreadyTyped);
    completionItems.addAll(0, completionsThatMatchAlreadyTyped);
  }

  private static String getCharactersAfterLastWhitespace(String textUntilCaret) {
    StringBuilder lettersBeforeCaret = new StringBuilder();
    for (int i = textUntilCaret.length() - 1; i > 0; i--) {
      char character = textUntilCaret.charAt(i);
      if (Character.isWhitespace(character)) {
        break;
      } else {
        lettersBeforeCaret.insert(0, character);
      }
    }
    return lettersBeforeCaret.toString();
  }

  private static String getTextUntilCaret(Position position, String[] linesOfDocument) {
    List<String> linesUntilCaret =
        new ArrayList<>(
            Arrays.stream(
                    linesOfDocument, 0, Math.min(position.getLine() + 1, linesOfDocument.length))
                .toList());
    linesUntilCaret.set(
        linesUntilCaret.size() - 1,
        linesUntilCaret.getLast().substring(0, position.getCharacter()));
    return String.join("\n", linesUntilCaret);
  }

  private static final Hashtable<String, CompletionItem> templateCompletionItems =
      new Hashtable<>(
          Map.of(
              "single_choice_task",
                  createTemplateCompletionItem(
                      "Single Choice Task Template",
                      "Template for a Single Choice task, see [documentation](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/task_definition.md#single-choice-aufgaben)",
                      """
                single_choice_task my_task {
                    description: "Dies ist der Aufgabentext",
                    answers: ["Antwort1", "Antwort2", "Antwort3"],
                    correct_answer_index: 1,
                    points: 1,
                    points_to_pass: 1,
                    explanation: "Dieser Text wird angezeigt, falls die Aufgabe falsch beantwortet wird"
                }
                """),
              "multiple_choice_task",
                  createTemplateCompletionItem(
                      "Multiple Choice Task Template",
                      "Template for a Multiple Choice task, see [documentation](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/task_definition.md#multiple-choice-aufgaben)",
                      """
                multiple_choice_task my_task {
                    description: "Dies ist der Aufgabentext",
                    answers: ["Antwort1", "Antwort2", "Antwort3"],
                    correct_answer_indices: [1,2],
                    points: 1,
                    points_to_pass: 1,
                    explanation: "Dieser Text wird angezeigt, falls die Aufgabe falsch beantwortet wird"
                }
                """),
              "assign_task",
                  createTemplateCompletionItem(
                      "Association Task Template",
                      "Template for an Association task, see [documentation](https://github.com/Dungeon-CampusMinden/Dungeon/blob/master/dungeon/doc/dsl/task_definition.md#zuordnungsaufgaben)",
                      """
                assign_task my_task {
                    description: "Dies ist der Aufgabentext",
                    solution: <
                        ["Term1", "Definition1"],
                        ["Term1", "Definition2"],
                        ["Term2", "Definition3"],
                        [_, "Definition4"],
                        ["Term3", _]
                        >,
                    points: 1,
                    points_to_pass: 1,
                    explanation: "Dieser Text wird angezeigt, falls die Aufgabe falsch beantwortet wird"
                }
                """)));

  private static final List<String> keywordsNumber =
      List.of("correct_answer_index", "points", "points_to_pass");
  private static final List<String> keywordsString = List.of("description", "explanation");
  private static final List<String> keywordsStringList = List.of("answers");
  private static final List<String> keywordsIntList = List.of("correct_answer_indices");
  private static final List<String> keywordsSet = List.of("solution");
  private static final List<String> allTaskPropertyKeywords =
      Stream.of(keywordsSet, keywordsIntList, keywordsStringList, keywordsString, keywordsNumber)
          .flatMap(Collection::stream)
          .toList();
  private static final Map<Integer, String> namedTokensDefaultSuggestions =
      Map.of(
          AntlrGrammarLexer.STRING, "\"\"",
          AntlrGrammarLexer.INT, "1",
          AntlrGrammarLexer.DECIMAL, "1.0");
  private static final Map<Integer, String> parserRuleDefaultSuggestions =
      Map.of(
          AntlrGrammarParser.RULE_string_list,
          "[\"\", \"\"]",
          AntlrGrammarParser.RULE_int_list,
          "[1, 2]",
          AntlrGrammarParser.RULE_set,
          """
                  <
                          ["", ""]
                      >""");

  private static void addSuggestionWithExampleValueIfPossible(
      ArrayList<CompletionItem> completionItems, String literal) {
    String defaultValue = null;
    if (keywordsNumber.contains(literal)) {
      defaultValue = namedTokensDefaultSuggestions.get(AntlrGrammarLexer.INT);
    } else if (keywordsString.contains(literal)) {
      defaultValue = namedTokensDefaultSuggestions.get(AntlrGrammarLexer.STRING);
    } else if (keywordsIntList.contains(literal)) {
      defaultValue = parserRuleDefaultSuggestions.get(AntlrGrammarParser.RULE_int_list);
    } else if (keywordsStringList.contains(literal)) {
      defaultValue = parserRuleDefaultSuggestions.get(AntlrGrammarParser.RULE_string_list);
    } else if (keywordsSet.contains(literal)) {
      defaultValue = parserRuleDefaultSuggestions.get(AntlrGrammarParser.RULE_set);
    }
    if (defaultValue != null) {
      CompletionItem completionItem = new CompletionItem();
      String textToInsert = literal + ": " + defaultValue + ",";
      completionItem.setLabel(textToInsert);
      completionItem.setInsertText(textToInsert);

      completionItem.setDetail("Keyword with example value");
      completionItem.setKind(CompletionItemKind.Keyword);
      completionItems.add(completionItem);
    }
  }

  private static CompletionItem createCompletionItem(String literal) {
    CompletionItem completionItem = new CompletionItem();
    completionItem.setLabel(literal);
    completionItem.setInsertText(literal);
    completionItem.setDetail("Literal");
    completionItem.setKind(CompletionItemKind.Keyword);
    return completionItem;
  }

  private static CompletionItem createTemplateCompletionItem(
      String labelShownInIntelliSense,
      String markdownDocumentationOfLabel,
      String textInsertedWhenOptionSelected) {
    CompletionItem completionItem = new CompletionItem();
    completionItem.setLabel(labelShownInIntelliSense);
    completionItem.setInsertText(textInsertedWhenOptionSelected);
    completionItem.setDetail("Snippet");
    completionItem.setDocumentation(
        new MarkupContent(MarkupKind.MARKDOWN, markdownDocumentationOfLabel));
    completionItem.setKind(CompletionItemKind.Snippet);
    return completionItem;
  }
}
