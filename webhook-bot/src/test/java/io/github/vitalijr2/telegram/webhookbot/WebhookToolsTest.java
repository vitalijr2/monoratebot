package io.github.vitalijr2.telegram.webhookbot;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import test.provider.FakeServiceProvider;
import test.provider.TestServiceProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("slow")
class WebhookToolsTest {

  @DisplayName("Custom Telegram Webhook Bot")
  @Test
  void customTelegramWebhookBot() {
    // when
    var serviceProvider = (TestServiceProvider) Assertions.assertDoesNotThrow(
        () -> WebhookTools.getTelegramWebhookBot(TestServiceProvider.class));

    // then
    assertEquals("hello world", serviceProvider.hello());
  }

  @DisplayName("No service providers found")
  @Test
  void noServiceProviders() {
    // when
    var exception = assertThrows(IllegalStateException.class,
        () -> WebhookTools.getTelegramWebhookBot(FakeServiceProvider.class));

    // then
    assertEquals("Unable to find a TelegramWebhookBot implementation", exception.getMessage());
  }

}