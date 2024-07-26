package antlrListener;

import antlr_gen.AntlrGrammarBaseListener;
import antlr_gen.AntlrGrammarParser;
import lsp.DocumentInformation;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import syntaxHighlighting.SemanticTokenModifier;
import syntaxHighlighting.SemanticTokenTransformer;
import syntaxHighlighting.SemanticTokenType;

/**
 * Antlr Listener that adds all tokens that shall be highlighted to the {@code
 * semanticTokenTransformer} field.
 */
public class AntlrListener extends AntlrGrammarBaseListener {

  /**
   * The transformer collects all tokens that shall be highlighted and transform their information
   * into the lsp specified form. It is reset for every parsetree in {@code enterStart()} method.
   */
  public SemanticTokenTransformer semanticTokenTransformer;

  /** The information of the document to fill. */
  public DocumentInformation documentInformation;

  /**
   * Initializes a new {@code AntlrHighlightingListener} instance.
   *
   * @param semanticTokenTransformer the semanticTokenTransformer to fill.
   * @param documentInformation the documentInformation to fill.
   */
  public AntlrListener(
      SemanticTokenTransformer semanticTokenTransformer, DocumentInformation documentInformation) {
    this.semanticTokenTransformer = semanticTokenTransformer;
    this.documentInformation = documentInformation;
  }

  private void addTerminalNodeAsToken(
      TerminalNode node,
      SemanticTokenType semanticTokenType,
      SemanticTokenModifier... semanticTokenModifiers) {
    if (node != null) {
      int zeroBasedLineIndex = node.getSymbol().getLine() - 1;
      int zeroBasedCharIndex = node.getSymbol().getCharPositionInLine();
      int tokenLength = node.getText().length();
      semanticTokenTransformer.addNextToken(
          zeroBasedLineIndex,
          zeroBasedCharIndex,
          tokenLength,
          semanticTokenType,
          semanticTokenModifiers);
    }
  }

  @Override
  public void exitId_definition(AntlrGrammarParser.Id_definitionContext ctx) {
    TerminalNode id = ctx.ID();
    addTerminalNodeAsToken(id, SemanticTokenType.variable, SemanticTokenModifier.definition);
    if (id != null) {
      documentInformation
          .getDefinitionIdCollector()
          .collect(id.getText(), getRangeForToken(id.getSymbol()));
    }
  }

  @Override
  public void exitId_definition_only_used_by_dungeon_system(
      AntlrGrammarParser.Id_definition_only_used_by_dungeon_systemContext ctx) {
    addTerminalNodeAsToken(ctx.ID(), SemanticTokenType.variable, SemanticTokenModifier.definition);
  }

  private Range getRangeForToken(Token token) {
    int zeroBasedLineIndex = token.getLine() - 1;
    int zeroBasedCharIndex = token.getCharPositionInLine();
    int tokenLength = token.getText().length();
    return new Range(
        new Position(zeroBasedLineIndex, zeroBasedCharIndex),
        new Position(zeroBasedLineIndex, zeroBasedCharIndex + tokenLength));
  }

  @Override
  public void exitId_usage(AntlrGrammarParser.Id_usageContext ctx) {
    addTerminalNodeAsToken(ctx.ID(), SemanticTokenType.variable);
    TerminalNode id = ctx.ID();
    if (id != null) {
      documentInformation
          .getUsageIdCollector()
          .collect(id.getText(), getRangeForToken(id.getSymbol()));
    }
  }

  @Override
  public void visitTerminal(TerminalNode node) {
    super.visitTerminal(node);
    switch (node.getSymbol().getType()) {
      case AntlrGrammarParser.ID:
        addTerminalNodeAsToken(node, SemanticTokenType.variable, SemanticTokenModifier.definition);
        break;
      case AntlrGrammarParser.STRING:
        addTerminalNodeAsToken(node, SemanticTokenType.string);
        break;
      case AntlrGrammarParser.INT:
      case AntlrGrammarParser.DECIMAL:
        addTerminalNodeAsToken(node, SemanticTokenType.number);
        break;
    }
    switch (node.getText()) {
      case "single_choice_task":
      case "multiple_choice_task":
      case "assign_task":
      case "graph":
      case "dungeon_config":
        addTerminalNodeAsToken(node, SemanticTokenType.type);
        break;
      case "solution":
      case "correct_answer_index":
      case "correct_answer_indices":
      case "description":
      case "answers":
      case "points":
      case "points_to_pass":
      case "explanation":
      case "grading_function":
      case "scenario_builder":
      case "dependency_graph":
        addTerminalNodeAsToken(node, SemanticTokenType.keyword);
        break;
    }
  }
}
