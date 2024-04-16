package pl.shakhner.PetrushkaProductsBot.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pl.shakhner.PetrushkaProductsBot.botAPI.BotState;
import pl.shakhner.PetrushkaProductsBot.cache.UserDataCache;
import pl.shakhner.PetrushkaProductsBot.util.KeyboardUtils;
import pl.shakhner.PetrushkaProductsBot.util.MessageUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.mockito.Mockito.*;
public class UpdateControllerTest {

    @Mock
    private UserDataCache userDataCache;

    @Mock
    private MessageUtils messageUtils;

    @Mock
    private TelegramBot telegramBot;

    @InjectMocks
    private UpdateController updateController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testDeleteFromCartRequestReceived() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, TelegramApiException {
        MockitoAnnotations.openMocks(this);

        Update update = new Update();
        Message message = new Message();
        message.setChat(new Chat(1L,""));
        update.setMessage(message);

        when(messageUtils.generateSendMessage(eq(update), anyString())).thenReturn(new SendMessage());
        when(userDataCache.getCurrentUserBotState(update)).thenReturn(BotState.WAIT_FOR_ORDINAL_NUMBER_FOR_DELETING);
        when(telegramBot.execute(any(SendMessage.class))).thenReturn(null);
        KeyboardUtils keyboardUtils = mock(KeyboardUtils.class);
        when(keyboardUtils.getMainKeyboard()).thenReturn(new ReplyKeyboardMarkup());

        Method method = UpdateController.class.getDeclaredMethod("deleteFromCartRequestReceived", Update.class);
        method.setAccessible(true);
        method.invoke(updateController, update);

        verify(userDataCache).setCurrentBotState(eq(update), eq(BotState.WAIT_FOR_ORDINAL_NUMBER_FOR_DELETING));
        verify(messageUtils).generateSendMessage(eq(update), eq(MessageUtils.DELETE_ITEM_FROM_CART_MESSAGE));
        verify(updateController).setMainKeyboardAndExecute(any(SendMessage.class));
    }
}
