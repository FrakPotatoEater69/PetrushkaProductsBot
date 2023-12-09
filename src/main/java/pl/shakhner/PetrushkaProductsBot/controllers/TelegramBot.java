package pl.shakhner.PetrushkaProductsBot.controllers;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pl.shakhner.PetrushkaProductsBot.config.BotConfig;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TelegramBot extends TelegramWebhookBot {

    private BotConfig config;
    private final UpdateController updateController;


    @Autowired
    public TelegramBot(BotConfig config, UpdateController updateController) {
        this.config = config;
        this.updateController = updateController;
        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("/help", "Get help"));
        botCommands.add(new BotCommand("/developer", "Contact the developer"));
        try {
            execute(new SetMyCommands(botCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error while bot initialization and command adding");
        }
    }

    @PostConstruct
    private void init() {
        this.updateController.registerBot(this);
        try {
            var setWebhook = SetWebhook.builder().url(config.getBotUri()).build();
            this.setWebhook(setWebhook);
        } catch (TelegramApiException e) {
            log.error("Error while setting WebHook " + e.getMessage());
        }
    }


    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return null;
    }

    @Override
    public String getBotPath() {
        return "/update";
    }
}
