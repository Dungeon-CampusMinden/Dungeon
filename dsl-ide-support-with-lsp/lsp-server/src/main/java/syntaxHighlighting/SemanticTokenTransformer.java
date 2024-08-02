package syntaxHighlighting;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class that collects the information about found syntax tokens and transform it into the required
 * format to send to a client.
 */
public class SemanticTokenTransformer {
  private final List<Integer> result = new ArrayList<>();
  private int lastTokenAbsoluteLine = 0;
  private int lastTokenAbsoluteCharInLine = 0;

  /**
   * Adds the token to the result in the necessary format to send to a client.
   *
   * @param absoluteLineIndex the 0 based line index of the token in the document.
   * @param absoluteCharIndex the 0 based char index of the token in the {@code absoluteLineIndex}.
   * @param tokenLength the length of the token.
   * @param semanticTokenType the type of the token.
   * @param semanticTokenModifiers the modifiers of the token.
   */
  public void addNextToken(
      int absoluteLineIndex,
      int absoluteCharIndex,
      int tokenLength,
      SemanticTokenType semanticTokenType,
      SemanticTokenModifier... semanticTokenModifiers) {
    int nextTokenRelativeLine = absoluteLineIndex - lastTokenAbsoluteLine;

    int nextTokenRelativeCharInLine = absoluteCharIndex;
    if (lastTokenAbsoluteLine == absoluteLineIndex) {
      nextTokenRelativeCharInLine = absoluteCharIndex - lastTokenAbsoluteCharInLine;
    }

    result.add(nextTokenRelativeLine);
    result.add(nextTokenRelativeCharInLine);
    result.add(tokenLength);
    result.add(semanticTokenType.ordinal());
    result.add(
        createIntWithThisBitIndices1(
            Arrays.stream(semanticTokenModifiers).toList().stream()
                .map(SemanticTokenModifier::ordinal)
                .toList()));
    lastTokenAbsoluteLine = absoluteLineIndex;
    lastTokenAbsoluteCharInLine = absoluteCharIndex;
  }

  /**
   * Returns the collects the information about found syntax tokens in the required format to send
   * to a client.
   *
   * @return The collects the information about found syntax tokens in the required format to send
   *     to a client.
   */
  public List<Integer> getResult() {
    return result;
  }

  private int createIntWithThisBitIndices1(List<Integer> indicesToBe1) {
    int result = 0;

    for (int indexToBe1 : indicesToBe1) {
      if (indexToBe1 >= 0 && indexToBe1 < 32) {
        result |= (1 << indexToBe1);
      } else {
        throw new InvalidParameterException();
      }
    }

    return result;
  }
}
