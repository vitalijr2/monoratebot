package io.github.vitalijr2.telegram.googlecloudfunction;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("slow")
class TelegramWebhookFunctionSlowTest {

  @DisplayName("Initialize a Telegram Webhook Function")
  @Test
  void telegramWebhookFunction() {
    // when and then
    assertDoesNotThrow(() -> new TelegramWebhookFunction());
  }

}