package autocompletion;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Stream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNState;
import org.antlr.v4.runtime.atn.AtomTransition;
import org.antlr.v4.runtime.atn.RuleStartState;
import org.antlr.v4.runtime.atn.RuleStopState;
import org.antlr.v4.runtime.atn.RuleTransition;
import org.antlr.v4.runtime.atn.SetTransition;
import org.antlr.v4.runtime.atn.Transition;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.IntervalSet;

/**
 * Class that computes which tokens to suggest by tokenizing the already typed text and using the
 * antlr state machine.
 */
public class AutocompletionUsingAntlrStateMachine {

  private static final Token caretToken = new CommonToken(-10);

  /** A custom token type to represent the suggestion of already defined ids. */
  public static final int idUsageTokenType = -20;

  private static final ATNState STOP =
      new ATNState() {
        @Override
        public int getStateType() {
          return -1984912851;
        }
      };

  private record StackPair(int ruleIndex, ATNState followState) {}

  private record Iteration(
      int tokenStreamIndex,
      ATNState state,
      List<Integer> alreadyPassedStateNumbers,
      Stack<StackPair> enteredRulesWithFollowStateStack) {}

  /**
   * Function that computes the next possible token types.
   *
   * <p>The Function takes grammar parameters and the tokens until the caret to compute the next
   * possible token types by using the antlr state machine.
   *
   * @param tokenList the list of tokens until the caret.
   * @param parser the parser for the text.
   * @param startStateIndex the index of the start state of the grammar.
   * @param idUsageRuleIndex the rule index of the parser rule that matches id usages.
   * @param idTokenType the token type number of id tokens.
   * @return the literal names of literal tokens and the symbolic names of complex tokens that are
   *     suggested.
   */
  public static HashSet<Integer> computeTokenTypesThatCouldFollow(
      List<Token> tokenList,
      Parser parser,
      int startStateIndex,
      int idUsageRuleIndex,
      int idTokenType) {
    tokenList.add(caretToken);

    ATN augmentedTransitionNetwork = parser.getATN();
    RuleStartState initialState = augmentedTransitionNetwork.ruleToStartState[startStateIndex];

    Stack<StackPair> enteredRulesWithFollowStateStack = new Stack<>();
    enteredRulesWithFollowStateStack.push(new StackPair(initialState.ruleIndex, STOP));

    Stack<Iteration> stack = new Stack<>();
    stack.push(new Iteration(0, initialState, new ArrayList<>(), enteredRulesWithFollowStateStack));

    return process(tokenList, stack, idUsageRuleIndex, idTokenType);
  }

  private static HashSet<Integer> process(
      List<Token> tokens, Stack<Iteration> stack, int idUsageRuleIndex, int idTokenType) {
    HashSet<Integer> suggestedTokenType = new HashSet<>();

    while (!stack.isEmpty()) {
      Iteration iteration = stack.pop();
      int tokenStreamIndex = iteration.tokenStreamIndex;
      ATNState state = iteration.state;
      List<Integer> alreadyPassedStateNumbers = iteration.alreadyPassedStateNumbers;
      Stack<StackPair> enteredRulesWithFollowStateStack =
          iteration.enteredRulesWithFollowStateStack;
      ATNState ruleIndexNecessaryToFollow = null;

      if (state instanceof RuleStopState && !enteredRulesWithFollowStateStack.isEmpty()) {
        int checkedRuleIndex = enteredRulesWithFollowStateStack.lastElement().ruleIndex;
        ATNState stateToFollowCheckedRule =
            enteredRulesWithFollowStateStack.lastElement().followState;

        if (state.ruleIndex != checkedRuleIndex)
          throw new Error(
              "Unexpected situation. Exited a rule that isn't the last one that was entered");

        ruleIndexNecessaryToFollow = stateToFollowCheckedRule;

        Stack<StackPair> stackAfterRuleStop = cloneStack(enteredRulesWithFollowStateStack);
        if (!stackAfterRuleStop.isEmpty()) {
          stackAfterRuleStop.removeLast();
        }
        enteredRulesWithFollowStateStack = stackAfterRuleStop;
      }

      for (Transition transition : state.getTransitions()) {
        if (transition.isEpsilon()
            && !alreadyPassedStateNumbers.contains(transition.target.stateNumber)) {
          Stack<StackPair> newEnteredRulesWithFollowStateStack =
              cloneStack(enteredRulesWithFollowStateStack);

          if (transition instanceof RuleTransition ruleTransition) {
            newEnteredRulesWithFollowStateStack.add(
                new StackPair(ruleTransition.ruleIndex, ruleTransition.followState));
          }

          if (ruleIndexNecessaryToFollow != null && transition.target != ruleIndexNecessaryToFollow)
            continue;

          List<Integer> alreadyPassedStateNumbersPlusCurrent =
              Stream.concat(
                      alreadyPassedStateNumbers.stream(), Stream.of(transition.target.stateNumber))
                  .toList();

          stack.push(
              new Iteration(
                  tokenStreamIndex,
                  transition.target,
                  alreadyPassedStateNumbersPlusCurrent,
                  newEnteredRulesWithFollowStateStack));
        } else if (doesTransitionMatchTokenOrTokenInterval(transition)) {
          Token nextToken = tokens.get(tokenStreamIndex);
          if (nextToken == caretToken
              || (nextToken.getType() == idTokenType
                  && tokenStreamIndex + 1 < tokens.size()
                  && tokens.get(tokenStreamIndex + 1) == caretToken)) {
            if (transition.target.ruleIndex == idUsageRuleIndex) {
              suggestedTokenType.add(idUsageTokenType);
            } else {
              suggestedTokenType.addAll(getTokenTypesThatCouldFollowCaret(transition.label()));
            }
          }

          if (doesTokenAllowTransition(transition, nextToken)) {
            stack.push(
                createIterationToCheckAfterMatch(
                    transition, tokenStreamIndex, enteredRulesWithFollowStateStack));
          }
        }
      }
    }
    return suggestedTokenType;
  }

  private static boolean doesTransitionMatchTokenOrTokenInterval(Transition transition) {
    return transition instanceof AtomTransition || transition instanceof SetTransition;
  }

  private static Iteration createIterationToCheckAfterMatch(
      Transition transition,
      int tokenStreamIndex,
      Stack<StackPair> enteredRulesWithFollowStateStack) {
    return new Iteration(
        tokenStreamIndex + 1, transition.target, List.of(), enteredRulesWithFollowStateStack);
  }

  private static boolean doesTokenAllowTransition(Transition transition, Token nextToken) {
    return transition.label().contains(nextToken.getType());
  }

  private static Stack<StackPair> cloneStack(Stack<StackPair> parserStack) {
    Stack<StackPair> newParserStack = new Stack<>();
    newParserStack.addAll(parserStack);
    return newParserStack;
  }

  private static Set<Integer> getTokenTypesThatCouldFollowCaret(
      IntervalSet intervalsOfPossibleTokenTypes) {
    HashSet<Integer> result = new HashSet<>();
    for (int i = 0; i < intervalsOfPossibleTokenTypes.getIntervals().size(); i++) {
      Interval I = intervalsOfPossibleTokenTypes.getIntervals().get(i);
      int smallestTokenTypeInsideInterval = I.a;
      int greatestTokenTypeInsideInterval = I.b;
      for (int tokenType = smallestTokenTypeInsideInterval;
          tokenType <= greatestTokenTypeInsideInterval;
          tokenType++) {
        result.add(tokenType);
      }
    }
    return result;
  }
}
