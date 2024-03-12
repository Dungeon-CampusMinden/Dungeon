package dsl.error;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNState;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.misc.Pair;

public class ErrorStrategy extends DefaultErrorStrategy {
  private Vocabulary vocabulary;

  public String getDisplayName(int token) {
    return vocabulary.getDisplayName(token);
  }

  public List<String> getDisplayNameOfIntervalSet(IntervalSet set) {
    ArrayList<String> list = new ArrayList<>(set.size());
    for (int entry : set.toArray()) {
      list.add(getDisplayName(entry));
    }
    return list;
  }

  public ErrorStrategy(Vocabulary vocabulary) {
    this.vocabulary = vocabulary;
  }

  protected boolean errorRecoveryMode = false;
  protected int lastErrorIndex = -1;
  protected IntervalSet lastErrorStates;
  protected ParserRuleContext nextTokensContext;
  protected int nextTokensState;

  public void reset(Parser recognizer) {
    this.endErrorCondition(recognizer);
  }

  protected void beginErrorCondition(Parser recognizer) {
    this.errorRecoveryMode = true;
  }

  public boolean inErrorRecoveryMode(Parser recognizer) {
    return this.errorRecoveryMode;
  }

  protected void endErrorCondition(Parser recognizer) {
    this.errorRecoveryMode = false;
    this.lastErrorStates = null;
    this.lastErrorIndex = -1;
  }

  public void reportMatch(Parser recognizer) {
    this.endErrorCondition(recognizer);
  }

  public void reportError(Parser recognizer, RecognitionException e) {
    if (!this.inErrorRecoveryMode(recognizer)) {
      this.beginErrorCondition(recognizer);
      if (e instanceof NoViableAltException) {
        this.reportNoViableAlternative(recognizer, (NoViableAltException) e);
      } else if (e instanceof InputMismatchException) {
        this.reportInputMismatch(recognizer, (InputMismatchException) e);
      } else if (e instanceof FailedPredicateException) {
        this.reportFailedPredicate(recognizer, (FailedPredicateException) e);
      } else {
        System.err.println("unknown recognition error type: " + e.getClass().getName());
        recognizer.notifyErrorListeners(e.getOffendingToken(), e.getMessage(), e);
      }
    }
  }

  public void recover(Parser recognizer, RecognitionException e) {
    if (this.lastErrorIndex == recognizer.getInputStream().index()
        && this.lastErrorStates != null
        && this.lastErrorStates.contains(recognizer.getState())) {
      recognizer.consume();
    }

    this.lastErrorIndex = recognizer.getInputStream().index();
    if (this.lastErrorStates == null) {
      this.lastErrorStates = new IntervalSet(new int[0]);
    }

    this.lastErrorStates.add(recognizer.getState());
    IntervalSet followSet = this.getErrorRecoverySet(recognizer);
    this.consumeUntil(recognizer, followSet);
  }

  protected void reportNoViableAlternative(Parser recognizer, NoViableAltException e) {
    TokenStream tokens = recognizer.getInputStream();
    String input;
    if (tokens != null) {
      if (e.getStartToken().getType() == -1) {
        input = "<EOF>";
      } else {
        input = tokens.getText(e.getStartToken(), e.getOffendingToken());
      }
    } else {
      input = "<unknown input>";
    }

    String msg = "no viable alternative at input " + this.escapeWSAndQuote(input);
    recognizer.notifyErrorListeners(e.getOffendingToken(), msg, e);
  }

  protected void reportInputMismatch(Parser recognizer, InputMismatchException e) {
    String msg =
        "mismatched input "
            + this.getTokenErrorDisplay(e.getOffendingToken())
            + " expecting "
            + e.getExpectedTokens().toString(recognizer.getVocabulary());
    recognizer.notifyErrorListeners(e.getOffendingToken(), msg, e);
  }

  protected void reportFailedPredicate(Parser recognizer, FailedPredicateException e) {
    super.reportFailedPredicate(recognizer, e);
    // String ruleName = recognizer.getRuleNames()[recognizer._ctx.getRuleIndex()];
    // String msg = "rule " + ruleName + " " + e.getMessage();
    // recognizer.notifyErrorListeners(e.getOffendingToken(), msg, e);
  }

  protected void reportUnwantedToken(Parser recognizer) {
    if (!this.inErrorRecoveryMode(recognizer)) {
      this.beginErrorCondition(recognizer);
      Token t = recognizer.getCurrentToken();
      String tokenName = this.getTokenErrorDisplay(t);
      IntervalSet expecting = this.getExpectedTokens(recognizer);
      String msg =
          "extraneous input "
              + tokenName
              + " expecting "
              + expecting.toString(recognizer.getVocabulary());
      recognizer.notifyErrorListeners(t, msg, (RecognitionException) null);
    }
  }

  protected void reportMissingToken(Parser recognizer) {
    if (!this.inErrorRecoveryMode(recognizer)) {
      this.beginErrorCondition(recognizer);
      Token t = recognizer.getCurrentToken();
      IntervalSet expecting = this.getExpectedTokens(recognizer);
      String msg =
          "missing "
              + expecting.toString(recognizer.getVocabulary())
              + " at "
              + this.getTokenErrorDisplay(t);
      recognizer.notifyErrorListeners(t, msg, (RecognitionException) null);
    }
  }

  public Token recoverInline(Parser recognizer) throws RecognitionException {
    Token matchedSymbol = this.singleTokenDeletion(recognizer);
    if (matchedSymbol != null) {
      recognizer.consume();
      return matchedSymbol;
    } else if (this.singleTokenInsertion(recognizer)) {
      return this.getMissingSymbol(recognizer);
    } else {
      InputMismatchException e;
      if (this.nextTokensContext == null) {
        e = new InputMismatchException(recognizer);
      } else {
        e = new InputMismatchException(recognizer, this.nextTokensState, this.nextTokensContext);
      }

      throw e;
    }
  }

  protected boolean singleTokenInsertion(Parser recognizer) {
    return super.singleTokenInsertion(recognizer);
    /*int currentSymbolType = recognizer.getInputStream().LA(1);
    ATNState currentState = (ATNState)((ParserATNSimulator)recognizer.getInterpreter()).atn.states.get(recognizer.getState());
    ATNState next = currentState.transition(0).target;
    ATN atn = ((ParserATNSimulator)recognizer.getInterpreter()).atn;
    IntervalSet expectingAtLL2 = atn.nextTokens(next, recognizer._ctx);
    if (expectingAtLL2.contains(currentSymbolType)) {
        this.reportMissingToken(recognizer);
        return true;
    } else {
        return false;
    }*/
  }

  protected Token singleTokenDeletion(Parser recognizer) {
    int nextTokenType = recognizer.getInputStream().LA(2);
    IntervalSet expecting = this.getExpectedTokens(recognizer);
    if (expecting.contains(nextTokenType)) {
      this.reportUnwantedToken(recognizer);
      recognizer.consume();
      Token matchedSymbol = recognizer.getCurrentToken();
      this.reportMatch(recognizer);
      return matchedSymbol;
    } else {
      return null;
    }
  }

  protected Token getMissingSymbol(Parser recognizer) {
    Token currentSymbol = recognizer.getCurrentToken();
    IntervalSet expecting = this.getExpectedTokens(recognizer);
    int expectedTokenType = 0;
    if (!expecting.isNil()) {
      expectedTokenType = expecting.getMinElement();
    }

    String tokenText;
    if (expectedTokenType == -1) {
      tokenText = "<missing EOF>";
    } else {
      tokenText = "<missing " + recognizer.getVocabulary().getDisplayName(expectedTokenType) + ">";
    }

    Token current = currentSymbol;
    Token lookback = recognizer.getInputStream().LT(-1);
    if (currentSymbol.getType() == -1 && lookback != null) {
      current = lookback;
    }

    return recognizer
        .getTokenFactory()
        .create(
            new Pair(current.getTokenSource(), current.getTokenSource().getInputStream()),
            expectedTokenType,
            tokenText,
            0,
            -1,
            -1,
            current.getLine(),
            current.getCharPositionInLine());
  }

  protected IntervalSet getExpectedTokens(Parser recognizer) {
    return recognizer.getExpectedTokens();
  }

  protected String getTokenErrorDisplay(Token t) {
    if (t == null) {
      return "<no token>";
    } else {
      String s = this.getSymbolText(t);
      if (s == null) {
        if (this.getSymbolType(t) == -1) {
          s = "<EOF>";
        } else {
          s = "<" + this.getSymbolType(t) + ">";
        }
      }

      return this.escapeWSAndQuote(s);
    }
  }

  protected String getSymbolText(Token symbol) {
    return symbol.getText();
  }

  protected int getSymbolType(Token symbol) {
    return symbol.getType();
  }

  protected String escapeWSAndQuote(String s) {
    s = s.replace("\n", "\\n");
    s = s.replace("\r", "\\r");
    s = s.replace("\t", "\\t");
    return "'" + s + "'";
  }

  protected IntervalSet getErrorRecoverySet(Parser recognizer) {
    var rc = recognizer.getRuleContext();
    var parentRc = rc.parent;
    var currentToken = recognizer.getCurrentToken();
    var expectedSet = recognizer.getExpectedTokens();
    HashSet<String> expectedNameSet = new HashSet<>();
    for (int entry : expectedSet.toSet()) {
      expectedNameSet.add(getDisplayName(entry));
    }

    var set = super.getErrorRecoverySet(recognizer);
    HashSet<String> nameSet = new HashSet<>();
    for (int entry : set.toSet()) {
      nameSet.add(getDisplayName(entry));
    }
    return set;
    /*ATN atn = ((ParserATNSimulator)recognizer.getInterpreter()).atn;
    RuleContext ctx = recognizer._ctx;

    IntervalSet recoverSet;
    for(recoverSet = new IntervalSet(new int[0]); ctx != null && ((RuleContext)ctx).invokingState >= 0; ctx = ((RuleContext)ctx).parent) {
        ATNState invokingState = (ATNState)atn.states.get(((RuleContext)ctx).invokingState);
        RuleTransition rt = (RuleTransition)invokingState.transition(0);
        IntervalSet follow = atn.nextTokens(rt.followState);
        recoverSet.addAll(follow);
    }

    recoverSet.remove(-2);
    return recoverSet;
     */
  }

  protected void consumeUntil(Parser recognizer, IntervalSet set) {
    for (int ttype = recognizer.getInputStream().LA(1);
        ttype != -1 && !set.contains(ttype);
        ttype = recognizer.getInputStream().LA(1)) {
      recognizer.consume();
    }
  }

  @Override
  public void sync(Parser recognizer) throws RecognitionException {
    // super.sync(recognizer);

    var currentToken = recognizer.getCurrentToken();
    var context = recognizer.getContext();
    var ruleContext = recognizer.getRuleContext();

    // THIS IS THE DEFAULT IMPLEMENTATION COPIED HERE FOR BETTER DEBUGGABILITY
    ATNState s =
        (ATNState)
            ((ParserATNSimulator) recognizer.getInterpreter())
                .atn.states.get(recognizer.getState());
    if (!this.inErrorRecoveryMode(recognizer)) {
      TokenStream tokens = recognizer.getInputStream();
      int la = tokens.LA(1);
      IntervalSet nextTokens = recognizer.getATN().nextTokens(s);
      if (nextTokens.contains(la)) {
        this.nextTokensContext = null;
        this.nextTokensState = -1;
      } else if (nextTokens.contains(-2)) {
        if (this.nextTokensContext == null) {
          this.nextTokensContext = recognizer.getContext();
          this.nextTokensState = recognizer.getState();
        }
      } else {
        switch (s.getStateType()) {
          case 3: // BLOCK_START
          case 4: // INITIAL_NUM_TRANSITIONS
          case 5: // STAR_BLOCK_START
          case 10: // STAR_LOOP_ENTRY
            if (this.singleTokenDeletion(recognizer) != null) {
              return;
            }

            throw new InputMismatchException(recognizer);
          case 9: // STAR_LOOP_BACK
          case 11: // PLUS_LOOP_BACK
            this.reportUnwantedToken(recognizer);
            IntervalSet expecting = recognizer.getExpectedTokens();
            IntervalSet whatFollowsLoopIterationOrRule =
                expecting.or(this.getErrorRecoverySet(recognizer));
            this.consumeUntil(recognizer, whatFollowsLoopIterationOrRule);
          case 6: // TOKEN_START
          case 7: // RULE_STOP
          case 8: // BLOCK_END
          default:
        }
      }
    }
  }
}
