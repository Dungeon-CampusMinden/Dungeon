package autocompletion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import antlr_gen.Epsilon_AtomLexer;
import antlr_gen.RulesLexer;
import antlr_gen.RulesParser;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.junit.jupiter.api.Test;

/** Tests that all task types are parsed as expected. */
public class RulesTest {
  @Test
  void test() {
    Lexer lexer = new RulesLexer(CharStreams.fromString("F"));

    HashSet<Integer> suggestedTokenTypes =
        AutocompletionUsingAntlrStateMachine.computeTokenTypesThatCouldFollow(
            new ArrayList<>(
                lexer.getAllTokens().stream().filter(t -> t.getChannel() == 0).toList()),
            new RulesParser(new CommonTokenStream(lexer)),
            RulesParser.RULE_first,
            -1,
            -1);
    Set<String> suggestions =
        suggestedTokenTypes.stream()
            .map(Epsilon_AtomLexer.VOCABULARY::getDisplayName)
            .collect(Collectors.toSet());

    assertEquals(1, suggestions.size());
    assert suggestions.contains("'A'");
  }
}
