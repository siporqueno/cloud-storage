import com.porejemplo.nube.server.auth.entity.User;
import com.porejemplo.nube.server.auth.repository.UserDAO;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDAOTest {

    @Test
    @Order(1)
    public void testConnect() {
        Assertions.assertDoesNotThrow(UserDAO::connect);
    }

    @Test
    @Order(2)
    public void testFindUserByUsername(){
        Assertions.assertEquals(UserDAO.findUserByUsername("user1").orElse(new User()).getUsername(), "user1");
    }

    @Test
    @Order(3)
    public void testDisconnect() {
        Assertions.assertDoesNotThrow(UserDAO::disconnect);
    }
}
