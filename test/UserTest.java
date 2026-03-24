import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void testUserConstructor() {
        User user = new User("John Doe", "password123", "john@example.com");
        assertEquals("John Doe", user.getName());
        assertEquals("password123", user.getPassword());
        assertEquals("john@example.com", user.getEmail());
        assertNotNull(user.getPlaylists());
    }
}
