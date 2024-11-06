package newdsl.semanticanalysis;

import newdsl.NewDSLHandler;
import org.junit.jupiter.api.Test;

public class ParamNotDefinedTest {

    String path = "/test_resources/newdsl/semanticanalysis/noparam.task";
    NewDSLHandler.SemanticAnalysisResult sem = new NewDSLHandler(path).getSemanticAnalysisResult();

    @Test
    public void detectsError() {
        assert (!sem.getErrors().isEmpty());
    }

}
