package io.github.vitalijr2.telegram.webhookbot;

import java.io.Reader;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * Telegram Webhook Bot
 */
public interface TelegramWebhookBot {

  /**
   * Find and load a service provider of {@link TelegramWebhookBot}
   *
   * @return the Telegram bot instance.
   */
  @NotNull
  static TelegramWebhookBot getInstance() {
    return WebhookTools.getTelegramWebhookBot(TelegramWebhookBot.class);
  }

  /**
   * Read a Telegram update and optionally send webhook answer.
   *
   * @param payloadReader a request payload.
   * @return the webhook answer if available.
   */
  @NotNull
  Optional<String> processPayload(@NotNull Reader payloadReader);

}
