package io.github.vitalijr2.telegram.googlecloudfunction;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import io.github.vitalijr2.telegram.webhookbot.TelegramWebhookBot;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.System.Logger.Level;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

/**
 * This function is activated by incoming requests from Telegram Bot API server.
 */
public class TelegramWebhookFunction implements HttpFunction {

  private static final String HTTP_POST_METHOD = "POST";

  private final System.Logger logger = System.getLogger(getClass().getName());
  private final TelegramWebhookBot webhookBot;

  public TelegramWebhookFunction() {
    this(TelegramWebhookBot.getInstance());
  }

  @VisibleForTesting
  TelegramWebhookFunction(TelegramWebhookBot webhookBot) {
    this.webhookBot = webhookBot;
  }

  /**
   * Get request body and send response back.
   *
   * @param request  a Telegram update.
   * @param response a Telegram webhook answer.
   * @see <a href="https://core.telegram.org/bots/api#update">Telegram Bot API: Update</a>
   * @see <a href="https://core.telegram.org/bots/api#making-requests-when-getting-updates">Telegram
   * Bot API: Making requests when getting updates</a>
   */
  @Override
  public void service(@NotNull HttpRequest request, @NotNull HttpResponse response) {
    if (HTTP_POST_METHOD.equals(request.getMethod())) {
      try {
        processRequestBody(request.getReader()).ifPresentOrElse(
            body -> FunctionTools.okWithBody(response, body),
            () -> FunctionTools.ok(response));
      } catch (IOException | RuntimeException exception) {
        logger.log(Level.WARNING, "Could not parse request body: {}", exception.getMessage());
        FunctionTools.internalError(response);
      }
    } else {
      logger.log(Level.INFO, "Method {} isn't implemented: {}", request.getMethod(),
          request.getFirstHeader("X-Forwarded-For").orElse("address not known"));
      FunctionTools.badMethod(response, HTTP_POST_METHOD);
    }
  }

  /**
   * If the Telegram update is a regular message or an inline query, pass it to the appropriate method:
   *
   * @param requestBodyReader a request body.
   * @return the webhook answer if available.
   */
  @VisibleForTesting
  @NotNull
  private Optional<String> processRequestBody(BufferedReader requestBodyReader) {
    return webhookBot.processPayload(requestBodyReader);
  }

}
