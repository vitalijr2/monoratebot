package io.github.vitalijr2.telegram.googlecloudfunction;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import io.github.vitalijr2.logging.mock.MockLoggerExtension;
import io.github.vitalijr2.telegram.webhookbot.TelegramWebhookBot;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.System.Logger.Level;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class, MockLoggerExtension.class})
@Tag("fast")
class TelegramWebhookFunctionFastTest {

  private static System.Logger logger;

  @Mock
  private HttpRequest httpRequest;
  @Mock
  private HttpResponse httpResponse;
  @Mock
  private TelegramWebhookBot webhookBot;

  @InjectMocks
  @Spy
  private TelegramWebhookFunction webhookFunction;

  @BeforeAll
  static void setUpClass() {
    logger = System.getLogger(TelegramWebhookFunction.class.getName());
  }

  @DisplayName("HTTP method not allowed")
  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"GET", "HEAD", "PUT", "DELETE", "CONNECT", "OPTIONS", "TRACE", "PATCH"})
  void methodNotAllowed(String methodName) {
    // given
    when(httpRequest.getMethod()).thenReturn(methodName);
    when(httpRequest.getFirstHeader(anyString())).thenReturn(Optional.of("1.2.3.4"));

    try (var functionTools = mockStatic(FunctionTools.class)) {
      // when
      assertDoesNotThrow(() -> webhookFunction.service(httpRequest, httpResponse));

      // then
      verify(webhookBot, never()).processPayload(isA(Reader.class));
      verify(logger).log(eq(Level.INFO), eq("Method {} isn't implemented: {}"), eq(methodName), eq("1.2.3.4"));
      functionTools.verify(() -> FunctionTools.badMethod(isA(HttpResponse.class), eq("POST")));
    }
  }

  @DisplayName("Internal error")
  @Test
  void internalError() throws IOException {
    // given
    var reader = new CharArrayReader("{\"a\":\"b\"}".toCharArray());

    when(httpRequest.getMethod()).thenReturn("POST");
    when(httpRequest.getReader()).thenReturn(new BufferedReader(reader));
    when(webhookBot.processPayload(isA(Reader.class))).thenThrow(new RuntimeException("test exception"));

    try (var functionTools = mockStatic(FunctionTools.class)) {
      // when
      assertDoesNotThrow(() -> webhookFunction.service(httpRequest, httpResponse));

      // then
      verify(logger).log(eq(Level.WARNING), eq("Could not parse request body: {}"), eq("test exception"));
      functionTools.verify(() -> FunctionTools.internalError(isA(HttpResponse.class)));
    }
  }

  @DisplayName("Happy path")
  @ParameterizedTest(name = "{0}")
  @CsvSource(value = {"Empty answer,N/A", "Webhook answer,{\"qwerty\":\"xyz\"}"}, nullValues = "N/A")
  void happyPath(String title, String webhookAnswer) throws IOException {
    // given
    var reader = new CharArrayReader("{\"a\":\"b\"}".toCharArray());

    when(httpRequest.getMethod()).thenReturn("POST");
    when(httpRequest.getReader()).thenReturn(new BufferedReader(reader));
    when(webhookBot.processPayload(isA(Reader.class))).thenReturn(Optional.ofNullable(webhookAnswer));

    try (var functionTools = mockStatic(FunctionTools.class)) {
      // when
      assertDoesNotThrow(() -> webhookFunction.service(httpRequest, httpResponse));

      // then
      verifyNoInteractions(logger);
      if (webhookAnswer != null) {
        functionTools.verify(() -> FunctionTools.okWithBody(isA(HttpResponse.class), eq(webhookAnswer)));
      } else {
        functionTools.verify(() -> FunctionTools.ok(isA(HttpResponse.class)));
      }
    }
  }

}