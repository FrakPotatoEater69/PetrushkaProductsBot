package pl.shakhner.PetrushkaProductsBot.services;

import org.telegram.telegrambots.meta.api.objects.Update;
import pl.shakhner.PetrushkaProductsBot.models.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    public List<User> findAll();

    public void saveUser(Update update);

    public Optional<User> findByChatId(Long chatId);
}
