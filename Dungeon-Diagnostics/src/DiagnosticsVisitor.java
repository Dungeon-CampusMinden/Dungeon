import java.util.*;

public class DiagnosticsVisitor extends DungeonDiagnosticsBaseVisitor<Object> {
    private Map<String, Integer> declaredVariables = new HashMap<>();

    // List to store all errors encountered
    private List<ErrorInfo> errors = new ArrayList<>();

    // Method to retrieve the list of errors
    public List<ErrorInfo> getErrors() {
        return errors;
    }



}
