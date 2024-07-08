package dsl.error;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

public class ErrorListener extends BaseErrorListener {
  private static final Logger LOGGER = Logger.getLogger(ErrorListener.class.getName());
  private static HashSet<Class> differentErrorClasses = new HashSet<>();
  private final boolean trace;
  private List<ErrorRecord> errors = new ArrayList<>();

  public ErrorListener() {
    this.trace = false;
  }

  public ErrorListener(boolean trace) {
    this.trace = trace;
  }

  public List<ErrorRecord> getErrors() {
    return errors;
  }

  @Override
  public void syntaxError(
      Recognizer<?, ?> recognizer,
      Object offendingSymbol,
      int line,
      int charPositionInLine,
      String msg,
      RecognitionException e) {

    String additionalInformation = "";
    if (recognizer instanceof DungeonDSLParser parser) {
      var context = parser.getContext();
      additionalInformation = ErrorLinkVisitor.instance.getAdditionalInformation(context);
    }

    if (e != null) {
      var ctx = e.getCtx();
      if (ctx != null) {
        var ruleIdx = ctx.getRuleIndex();
        String ruleName = recognizer.getRuleNames()[ruleIdx];
      }

      var clazz = e.getClass();
      differentErrorClasses.add(clazz);
    }

    List<String> contextRuleNames = new ArrayList<>();
    RuleContext iterRule;
    String iterRuleName;
    if (recognizer instanceof Parser parser) {
      iterRule = parser.getRuleContext();
      while (iterRule != null) {
        iterRuleName = recognizer.getRuleNames()[iterRule.getRuleIndex()];
        contextRuleNames.add(iterRuleName);
        iterRule = iterRule.getParent();
      }
    }

    boolean b = true;

    // get parent of offending symbol?
    // this.errors.add(
    // new ErrorRecord(msg, (CommonToken) offendingSymbol, line, charPositionInLine, e));

    // TODO: how to figure out, that this is actually a lexing error?
    this.errors.add(
        ErrorRecordFactory.instance.errorRecord(
            msg,
            additionalInformation,
            (CommonToken) offendingSymbol,
            line,
            charPositionInLine,
            e,
            contextRuleNames));

    if (this.trace) {
      String warning =
          String.format(
              "Syntax error, recognizer: '%s', offendingSymbol: '%s', line: %x, charPosition: %x, msg: '%s', exception: '%s'",
              recognizer, offendingSymbol, line, charPositionInLine, msg, e);

      LOGGER.severe(warning);
    }
  }

  @Override
  public void reportAmbiguity(
      Parser recognizer,
      DFA dfa,
      int startIndex,
      int stopIndex,
      boolean exact,
      BitSet ambigAlts,
      ATNConfigSet configs) {
    Token currentToken = recognizer.getCurrentToken();
    Token LA2 = recognizer.getInputStream().LT(2);

    String dfaString = dfa.toString(recognizer.getVocabulary());
    String rule = recognizer.getRuleNames()[recognizer.getContext().getRuleIndex()];

    String warning =
        String.format(
            "Ambiguity, currentToken: '%s', LA2: '%s', startIndex: %x, stopIndex: %x\nconflicting alts: '%s'\nDFA: '%s'\nrule '%s'",
            currentToken, LA2, startIndex, stopIndex, ambigAlts, dfaString, rule);

    LOGGER.fine(warning);
  }

  @Override
  public void reportAttemptingFullContext(
      Parser recognizer,
      DFA dfa,
      int startIndex,
      int stopIndex,
      BitSet conflictingAlts,
      ATNConfigSet configs) {
    Token currentToken = recognizer.getCurrentToken();
    Token LA2 = recognizer.getInputStream().LT(2);

    String dfaString = dfa.toString(recognizer.getVocabulary());
    String rule = recognizer.getRuleNames()[recognizer.getContext().getRuleIndex()];

    String[] alternativeNames = recognizer.getRuleNames();
    var conflictingAltsArray = conflictingAlts.stream().toArray();
    var list = new ArrayList<>();

    for (int alt : conflictingAltsArray) {
      list.add(alternativeNames[alt]);
    }

    String warning =
        String.format(
            "Ambiguity full context, currentToken: '%s', LA2: '%s', startIndex: %x, stopIndex: %x\nconf alts: '%s', conflicting alts resolved: '%s'\nDFA: '%s'\nrule: '%s'",
            currentToken, LA2, startIndex, stopIndex, conflictingAlts, list, dfaString, rule);

    LOGGER.fine(warning);
  }

  @Override
  public void reportContextSensitivity(
      Parser recognizer,
      DFA dfa,
      int startIndex,
      int stopIndex,
      int prediction,
      ATNConfigSet configs) {
    String rule = recognizer.getRuleNames()[recognizer.getContext().getRuleIndex()];
    String warning =
        String.format(
            "Context sensitivity, recognizer: '%s', dfa: '%s', startIndex: %x, stopIndex: %x\nrule '%s'",
            recognizer, dfa, startIndex, stopIndex, rule);
    LOGGER.fine(warning);
  }
}
