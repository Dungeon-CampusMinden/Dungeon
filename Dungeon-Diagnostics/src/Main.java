import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import java.io.IOException;
import java.util.List;

import static org.antlr.v4.runtime.CharStreams.fromFileName;

public class Main {
    public static void main(String[] args) {
        try {
            if (args.length > 0) {
                String source = args[0];
                //System.out.println("Der übergebene Dokumentenpfad ist: " + source);
                CharStream cs = fromFileName(source);
                DungeonDiagnosticsLexer lexer = new DungeonDiagnosticsLexer(cs);
                CommonTokenStream tokens = new CommonTokenStream(lexer);
                DungeonDiagnosticsParser parser = new DungeonDiagnosticsParser(tokens);

                CustomErrorListener errorListener = new CustomErrorListener();
                parser.removeErrorListeners(); // Entferne Standard-Listener, um Standard-Fehlerausgabe zu verhindern
                parser.addErrorListener(errorListener); // Füge benutzerdefinierten Listener hinzu

                ParseTree tree = parser.program();
                DiagnosticsVisitor visitor = new DiagnosticsVisitor();
                visitor.visit(tree);
                List<ErrorInfo> errors = errorListener.getErrors();
                //System.out.println(visitor.getErrors());
                errors.addAll(visitor.getErrors());

                if (!errors.isEmpty()) {
                    String json = generateErrorJson(errors);
                    System.out.println(json);
                } else {
                    System.out.println("{\"message\": \"Parsing erfolgreich.\"}");
                }
            } else {
                System.out.println("{\"error\": \"Es wurde kein Pfad übergeben.\"}");
                return;
            }

        } catch (IOException e) {
            System.out.println("{\"error\": \"" + e.getMessage() + "\"}");
        }


//                // Fehler ausgeben
//                if (!errorListener.getErrors().isEmpty()) {
//                    for (ErrorInfo error : errorListener.getErrors()) {
//                        System.out.println(error);
//                    }
//                } else {
//                    System.out.println("Parsing erfolgreich.");
//                }

//            DiagnosticsVisitor visitor = new DiagnosticsVisitor();
//            visitor.visit(tree);
//
//            for (ErrorInfo error : visitor.getErrors()) {
//                System.out.println(error);
//            }
//            } else{
//                System.out.println("Es wurde kein Pfad übergeben.");
//                return;
//            }
//
//        }catch(IOException e){
//
//           e.printStackTrace();
//        }

    }
    public static String generateErrorJson(List<ErrorInfo> errors) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"errors\":[");

        for (int i = 0; i < errors.size(); i++) {
            ErrorInfo error = errors.get(i);
            jsonBuilder.append("{");
            jsonBuilder.append("\"message\":\"").append(error.message).append("\",");
            jsonBuilder.append("\"line\":").append(error.line).append(",");
            jsonBuilder.append("\"column\":").append(error.charPositionInLine);
            jsonBuilder.append("}");

            // Komma zwischen Fehlerobjekten, aber kein Komma nach dem letzten Objekt
            if (i < errors.size() - 1) {
                jsonBuilder.append(",");
            }
        }

        jsonBuilder.append("]}");
        return jsonBuilder.toString();
    }
}
