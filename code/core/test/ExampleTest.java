import jdk.jfr.Description;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** show how we setup tests in this project and how to use mockito */
public class ExampleTest {

    /*
     * How to name tests:
     * MethodName_StateUnderTest_ExpectedBehavior
     * example: isAdult_AgeLessThan18_False
     */

    private List mockedList = mock(List.class);

    @Test
    @Description("Example on how to use mockito")
    public void get_positiveParameter_True() {
        when(mockedList.get(0)).thenReturn("First");
        assertEquals("First", mockedList.get(0));
    }

    @Test
    @Description("Example on how to use mockito")
    public void get_negativeParameter_False() {
        when(mockedList.get(-1)).thenReturn(false);
        assertFalse((boolean) mockedList.get(-1));
    }
}
