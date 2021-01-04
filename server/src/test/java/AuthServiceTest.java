import com.porejemplo.nube.server.auth.entity.User;
import com.porejemplo.nube.server.auth.repository.UserDAO;
import com.porejemplo.nube.server.auth.service.AuthService;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthServiceTest {

    @Test
    @Order(1)
    public void testConnect() {
        Assertions.assertDoesNotThrow(AuthService::connect);
    }

    @Test
    @Order(2)
    public void testTrueFirstVerifyUsernameAndPassword() {
        Assertions.assertTrue(AuthService.verifyUsernameAndPassword("user1", "pass1"));
    }

    @Test
    @Order(3)
    public void testTrueSecondVerifyUsernameAndPassword() {
        Assertions.assertTrue(AuthService.verifyUsernameAndPassword("user3", "pass3"));
    }

    @Test
    @Order(4)
    public void testFalsePassInVerifyUsernameAndPassword() {
        Assertions.assertFalse(AuthService.verifyUsernameAndPassword("user1", "abcd"));
    }

    @Test
    @Order(5)
    public void testFalseUsernameInVerifyUsernameAndPassword() {
        Assertions.assertFalse(AuthService.verifyUsernameAndPassword("abcdef", "pass2"));
    }

    @Test
    @Order(6)
    public void testFalseUsernameAndPassInVerifyUsernameAndPassword() {
        Assertions.assertFalse(AuthService.verifyUsernameAndPassword("abcdef", "ghijklmn"));
    }

    @Test
    @Order(7)
    public void testDisconnect() {
        Assertions.assertDoesNotThrow(AuthService::disconnect);
    }
}
