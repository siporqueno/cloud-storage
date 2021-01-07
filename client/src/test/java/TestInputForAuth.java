import com.porejemplo.nube.client.ConsoleClient;
import com.porejemplo.nube.client.ConsoleClientOldMonolith;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestInputForAuth {

    @Test
    public void firstTestInputCheck() {
        Assertions.assertTrue(ConsoleClientOldMonolith.checkIfInputIsEmptyOrContainsSpaces("", "password"));
    }

    @Test
    public void secondTestInputCheck() {
        Assertions.assertTrue(ConsoleClientOldMonolith.checkIfInputIsEmptyOrContainsSpaces(" a b c ", "username"));
    }

    @Test
    public void thirdTestInputCheck() {
        Assertions.assertFalse(ConsoleClientOldMonolith.checkIfInputIsEmptyOrContainsSpaces("test", "password"));
    }

}
