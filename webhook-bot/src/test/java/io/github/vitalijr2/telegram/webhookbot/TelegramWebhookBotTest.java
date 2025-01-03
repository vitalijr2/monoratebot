package io.github.vitalijr2.telegram.webhookbot;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("slow")
class TelegramWebhookBotTest {

  @DisplayName("Get an instance")
  @Test
  void getInstance() {
    // when and then
    assertNotNull(assertDoesNotThrow(TelegramWebhookBot::getInstance));
  }

}