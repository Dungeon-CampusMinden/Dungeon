import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import java.io.IOException;

import static org.antlr.v4.runtime.CharStreams.fromFileName;

public class Main {
    public static void main(String[] args) {
        try {
            String source = "test.dng";
            CharStream cs = fromFileName(source);
            DungeonDiagnosticsLexer lexer = new DungeonDiagnosticsLexer(cs);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            DungeonDiagnosticsParser parser = new DungeonDiagnosticsParser(tokens);

            CustomErrorListener errorListener = new CustomErrorListener();
            parser.removeErrorListeners(); // Entferne Standard-Listener, um Standard-Fehlerausgabe zu verhindern
            parser.addErrorListener(errorListener); // Füge benutzerdefinierten Listener hinzu

            ParseTree tree = parser.program();
            // Fehler ausgeben
            if (!errorListener.getErrors().isEmpty()) {
                for (ErrorInfo error : errorListener.getErrors()) {
                    System.out.println(error);
                }
            } else {
                System.out.println("Parsing erfolgreich.");
            }

//            DiagnosticsVisitor visitor = new DiagnosticsVisitor();
//            visitor.visit(tree);
//
//            for (ErrorInfo error : visitor.getErrors()) {
//                System.out.println(error);
//            }

        }catch(IOException e){

           e.printStackTrace();
        }

    }
}
