package newdsl.semanticanalysis;

import newdsl.NewDSLHandler;
import org.junit.jupiter.api.Test;

public class MismatchedAnswersTest {

    String path = "/test_resources/newdsl/semanticanalysis/mismatched.task";
    NewDSLHandler.SemanticAnalysisResult sem = new NewDSLHandler(path).getSemanticAnalysisResult();

    @Test
    public void detectsError() {
        assert (!sem.getErrors().isEmpty());
    }

}
