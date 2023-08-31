package petriNet;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

public class PlaceTest {

    @Test
    public void placeToken() {
        Place place = new Place();
        assertEquals(0, place.tokenCount());
        place.placeToken();
        assertEquals(1, place.tokenCount());
        place.placeToken();
        assertEquals(2, place.tokenCount());
    }

    @Test
    public void register_transient() {
        Place place = new Place();
        Transition transitionA = Mockito.mock(Transition.class);
        Transition transitionB = Mockito.mock(Transition.class);
        place.register(transitionA);
        place.register(transitionB);
        place.placeToken();
        Mockito.verify(transitionA).notify(place);
        Mockito.verify(transitionB).notify(place);
    }

    @Test
    public void deregister_transition() {
        Place place = new Place();
        Transition transitionA = Mockito.mock(Transition.class);
        Transition transitionB = Mockito.mock(Transition.class);
        place.register(transitionA);
        place.register(transitionB);
        place.deregister(transitionA);
        Mockito.verifyNoMoreInteractions(transitionA);
        place.placeToken();
        Mockito.verify(transitionB).notify(place);
    }
}
