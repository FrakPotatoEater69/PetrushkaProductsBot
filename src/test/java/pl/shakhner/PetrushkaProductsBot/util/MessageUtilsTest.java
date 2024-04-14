package pl.shakhner.PetrushkaProductsBot.util;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MessageUtilsTest {
    @Test
    public void testGenerateSendMessageValidUpdateAndTextReturnsExpectedSendMessage() {
        Update update = new Update();
        String text = "Hello, world!";
        update.setMessage(new Message());
        update.getMessage().setChat(new Chat(123L, "s"));


        MessageUtils messageUtils = new MessageUtils(null, null, null, null);
        SendMessage sendMessage = messageUtils.generateSendMessage(update, text);

        assertNotNull(sendMessage);
        assertEquals(update.getMessage().getChatId().toString(), sendMessage.getChatId());
        assertEquals(text, sendMessage.getText());
    }

    @Test
    public void testGenerateMainCatalog_ValidUpdate_ReturnsExpectedSendPhoto() {
        Update update = new Update();
        KeyboardUtils keyboardUtils = mock(KeyboardUtils.class);
        when(keyboardUtils.getFruitsOrVegetablesKeyboard()).thenReturn(new InlineKeyboardMarkup());
        update.setMessage(new Message());
        update.getMessage().setChat(new Chat(123L, "s"));


        MessageUtils messageUtils = new MessageUtils(null, null, null, keyboardUtils);
        SendPhoto sendPhoto = messageUtils.generateMainCatalog(update);

        assertNotNull(sendPhoto);
        assertEquals(update.getMessage().getChatId().toString(), sendPhoto.getChatId());
        assertNotNull(sendPhoto.getPhoto());
        assertNotNull(sendPhoto.getReplyMarkup());
    }
}
