package newdsl.common;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DSLErrorHandler {

    private static int countUniqueFilePaths(List<DSLError> errors) {
        Set<String> uniqueFilePaths = new HashSet<>();
        for (SourceLocation location : errors.stream().map(DSLError::getSourceLocation).toList()) {
            uniqueFilePaths.add(location.getAbsoluteFilePath());
        }
        return uniqueFilePaths.size();
    }

    public static void printErrors(List<DSLError> errors) {
        if (!errors.isEmpty()) {
            errors.sort(Comparator.comparing((DSLError e) -> e.getSourceLocation().getAbsoluteFilePath()).thenComparingInt(e -> e.getSourceLocation().getRow()).thenComparingInt(e -> e.getSourceLocation().getColumn()));

            System.err.println(String.format("Found %s errors in %s files.\n", errors.size(), countUniqueFilePaths(errors)));

            for (DSLError error : errors) {
                System.err.println(String.format("ERROR %s\n\t%s\n", error.getSourceLocation(), error.getMessage()));
            }
        }
    }

}
