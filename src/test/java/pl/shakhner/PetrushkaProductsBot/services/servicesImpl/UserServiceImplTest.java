package pl.shakhner.PetrushkaProductsBot.services.servicesImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.annotation.Repeat;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import pl.shakhner.PetrushkaProductsBot.exceptions.UserNotFoundException;
import pl.shakhner.PetrushkaProductsBot.models.User;
import pl.shakhner.PetrushkaProductsBot.repositories.UserRepository;
import pl.shakhner.PetrushkaProductsBot.util.Extractor;

import java.sql.Timestamp;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getUserByIdReturnsUserWhenUserExists() {
        long userId = 1;
        User user = new User();
        user.setChatId(userId);
        user.setUserName("testUser");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> optionalUser = userService.findByChatId(userId);

        assertTrue(optionalUser.isPresent());
    }

    @Test
    void getUserByIdThrowsUserNotFoundExceptionWhenUserDoesNotExist() {
        long userId = 1;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertTrue(userService.findByChatId(userId).isEmpty());
    }

    @Test
    void saveUserSuccessfully() {
        Update update = new Update();
        update.setMessage(new Message());
        update.getMessage().setChat(new Chat(11L, "s"));
        org.telegram.telegrambots.meta.api.objects.User tgUser = new org.telegram.telegrambots.meta.api.objects.User(11L, "Alex", false);
        update.getMessage().setFrom(tgUser);
        tgUser.setUserName("AlShah");
        tgUser.setLastName("Shakhner");

        User localUser = new User();
        localUser.setChatId(11L);
        localUser.setUserName(update.getMessage().getFrom().getUserName());
        localUser.setFirstName(update.getMessage().getFrom().getFirstName());
        localUser.setLastName(update.getMessage().getFrom().getLastName());
        localUser.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

        userService.saveUser(update);
        verify(userRepository, times(1)).save(localUser);
    }

}

