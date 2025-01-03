package test.provider;

import io.github.vitalijr2.telegram.webhookbot.TelegramWebhookBot;

public interface FakeServiceProvider extends TelegramWebhookBot {

  String hello();

}
