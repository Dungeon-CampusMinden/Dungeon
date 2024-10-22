package newdsl.semanticanalysis;

import newdsl.NewDSLHandler;
import org.junit.jupiter.api.Test;

public class AliasTest {

    String path = "/test_resources/newdsl/semanticanalysis/alias.task";
    NewDSLHandler.SemanticAnalysisResult sem = new NewDSLHandler(path).getSemanticAnalysisResult();

    @Test
    public void definesSymbols() {

        assert (sem.getSymbolTable().isDefined("KuchenBacken"));

        sem.getSymbolTable().enterScope("KuchenBacken");
        assert (sem.getSymbolTable().isDefined("vz"));
    }

}
