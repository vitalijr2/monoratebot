package io.github.vitalijr2.telegram.webhookbot;

import java.util.ServiceLoader;

class WebhookTools {

  private WebhookTools() {
  }

  static TelegramWebhookBot getTelegramWebhookBot(Class<? extends TelegramWebhookBot> clazz) {
    return ServiceLoader.load(clazz).findFirst()
        .orElseThrow(() -> new IllegalStateException("Unable to find a TelegramWebhookBot implementation"));
  }

}
