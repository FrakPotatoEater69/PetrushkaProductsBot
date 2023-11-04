package pl.shakhner.PetrushkaProductsBot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class BotConfig {

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String token;

    @Value("${bot.uri}")
    private String botUri;

    public BotConfig() {
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public String getToken() {
        return token;
    }

    public void setKey(String key) {
        this.token = key;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getBotUri() {
        return botUri;
    }
}
