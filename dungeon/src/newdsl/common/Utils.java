package newdsl.common;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static int getLine(ParserRuleContext ctx) {
        return ctx.getStart().getLine();
    }

    public static int getCharPosInLine(ParserRuleContext ctx) {
        return ctx.getStart().getCharPositionInLine();
    }

    public static String getFileName(ParserRuleContext ctx) {
        return ctx.getStart().getTokenSource().getSourceName();
    }

    public static int getLine(Token t) {
        if (t == null) {
            return -1;
        }
        return t.getLine();
    }

    public static int getCharPosInLine(Token t) {
        if (t == null) {
            return -1;
        }
        return t.getCharPositionInLine();
    }

    public static String getFileName(Token t) {
        if (t == null) {
            return null;
        }
        return t.getTokenSource().getSourceName();
    }

    public static List<String> extractParams(String input) {
        List<String> matches = new ArrayList<>();
        Pattern pattern = Pattern.compile("<<(.*?)>>");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            matches.add(matcher.group(1));
        }

        return matches;
    }

    public static <T> List<T> shuffle(List<T> originalList) {
        List<T> shuffledList = new ArrayList<>(originalList);
        Random random = new Random();
        int size = shuffledList.size();

        for (int i = size - 1; i > 0; i--) {
            // Generate a random index between 0 and i (inclusive)
            int j = random.nextInt(i + 1);

            // Swap elements at indices i and j
            T temp = shuffledList.get(i);
            shuffledList.set(i, shuffledList.get(j));
            shuffledList.set(j, temp);
        }

        return shuffledList;
    }

}
