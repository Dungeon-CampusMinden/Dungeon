package autocompletion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import antlr_gen.Epsilon_AtomLexer;
import antlr_gen.SetTransitionLexer;
import antlr_gen.SetTransitionParser;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.junit.jupiter.api.Test;

/** Tests that all task types are parsed as expected. */
public class SetTransitionTest {
  @Test
  void test() {
    Lexer lexer = new SetTransitionLexer(CharStreams.fromString("A"));

    HashSet<Integer> suggestedTokenTypes =
        AutocompletionUsingAntlrStateMachine.computeTokenTypesThatCouldFollow(
            new ArrayList<>(
                lexer.getAllTokens().stream().filter(t -> t.getChannel() == 0).toList()),
            new SetTransitionParser(new CommonTokenStream(lexer)),
            SetTransitionParser.RULE_r,
            -1,
            -1);
    Set<String> suggestions =
        suggestedTokenTypes.stream()
            .map(Epsilon_AtomLexer.VOCABULARY::getDisplayName)
            .collect(Collectors.toSet());

    List<String> expected = List.of("'B'", "'C'", "'D'");
    assertEquals(expected.size(), suggestions.size());
    assert suggestions.contains(expected.getFirst());
    assert suggestions.contains(expected.get(0));
    assert suggestions.contains(expected.get(1));
  }
}
