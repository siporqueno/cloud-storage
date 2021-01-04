import com.porejemplo.nube.client.ConsoleClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestInputForAuth {

    @Test
    public void firstTestInputCheck() {
        Assertions.assertTrue(ConsoleClient.checkIfInputIsEmptyOrContainsSpaces("", "password"));
    }

    @Test
    public void secondTestInputCheck() {
        Assertions.assertTrue(ConsoleClient.checkIfInputIsEmptyOrContainsSpaces(" a b c ", "username"));
    }

    @Test
    public void thirdTestInputCheck() {
        Assertions.assertFalse(ConsoleClient.checkIfInputIsEmptyOrContainsSpaces("test", "password"));
    }

}
