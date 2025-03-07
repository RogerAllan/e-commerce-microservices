import com.ecommerce.users.model.User;
import com.ecommerce.users.repositories.UserRepository;
import java.lang.SecurityException;
import com.ecommerce.users.service.UserAlreadyExistsException;
import com.ecommerce.users.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerUser_ThrowsException_WhenEmailExists() {
        User user = new User ();
        user.setEmail ("test@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(user));
    }

    @Test
    void registerUser_SavesUser_WhenValid() throws UserAlreadyExistsException {
        User user = new User();
        user.setEmail("new@example.com");
        user.setPassword("senhaSegura123");
        user.setToken ("token");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashSenha");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.registerUser(user);
        assertNotNull(savedUser);
        assertEquals("new@example.com", savedUser.getEmail());
    }


}