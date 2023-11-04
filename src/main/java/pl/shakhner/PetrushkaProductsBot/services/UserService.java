package pl.shakhner.PetrushkaProductsBot.services;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import pl.shakhner.PetrushkaProductsBot.exceptions.UserNotFoundException;
import pl.shakhner.PetrushkaProductsBot.models.User;
import pl.shakhner.PetrushkaProductsBot.repositories.UserRepository;
import pl.shakhner.PetrushkaProductsBot.util.ExceptionUtils;
import pl.shakhner.PetrushkaProductsBot.util.Extractor;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll(){
        List<User> userList = userRepository.findAll();

        if(userList.isEmpty())
            throw new UserNotFoundException("User database is empty");

        return userList;
    }

    public void saveUser(Update update) {
        User user = new User();
        user.setChatId(Extractor.extractChatIdFromUpdate(update));
        user.setUserName(update.getMessage().getFrom().getUserName());
        user.setFirstName(update.getMessage().getFrom().getFirstName());
        user.setLastName(update.getMessage().getFrom().getLastName());
        user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

        userRepository.save(user);
    }

    public Optional<User> findByChatId(Long chatId) {
        return userRepository.findById(chatId);
    }
}
