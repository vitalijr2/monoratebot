package io.github.vitalijr2.telegram.googlecloudfunction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.collection.IsMapContaining.hasValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.cloud.functions.HttpResponse;
import io.github.vitalijr2.logging.mock.MockLoggerExtension;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class, MockLoggerExtension.class})
@Tag("fast")
class FunctionToolsTest {

  private static System.Logger logger;

  @Mock
  private HttpResponse httpResponse;
  @Mock
  private BufferedWriter writer;

  @BeforeAll
  static void setUpClass() {
    logger = System.getLogger(FunctionTools.class.getName());
  }

  @DisplayName("Do response: an empty response")
  @Test
  void doEmptyResponse() {
    // when
    FunctionTools.doResponse(httpResponse, 678, "Empty response", null);

    // then
    verifyNoInteractions(logger);
    verify(httpResponse).appendHeader("Server", "Test Bot - 3.2.1-RELEASE");
    verify(httpResponse).setStatusCode(678, "Empty response");
  }

  @DisplayName("Do response: response with body")
  @Test
  void doResponseWithBody() throws IOException {
    // given
    var writer = mock(BufferedWriter.class);

    when(httpResponse.getWriter()).thenReturn(writer);

    // when
    FunctionTools.doResponse(httpResponse, 200, "OK", "{\"a\":\"b\"}");

    // then
    verifyNoInteractions(logger);
    verify(httpResponse).appendHeader("Server", "Test Bot - 3.2.1-RELEASE");
    verify(httpResponse).setStatusCode(200, "OK");
    verify(writer).write("{\"a\":\"b\"}");
  }

  @DisplayName("Do response: throws IOException")
  @Test
  void doResponseThrowsException() throws IOException {
    // given
    doThrow(new IOException("test exception")).when(writer).write(anyString());
    when(httpResponse.getWriter()).thenReturn(writer);

    // when
    assertDoesNotThrow(() -> FunctionTools.doResponse(httpResponse, 678, "Test status", "test body"));

    // then
    verify(logger).log(Level.WARNING, "Could not make HTTP {} response: {}", 678, "test exception");
  }

  @DisplayName("Bad method")
  @Test
  void badMethod() throws IOException {
    // given
    var headers = new HashMap<String, List<String>>();

    when(httpResponse.getHeaders()).thenReturn(headers);
    when(httpResponse.getWriter()).thenReturn(writer);

    // when
    FunctionTools.badMethod(httpResponse, "METHOD_A", "METHOD_B");

    // then
    verify(httpResponse).setContentType("text/html;charset=utf-8");
    verify(httpResponse).setStatusCode(405, "Method Not Allowed");
    verify(writer).write("qwerty xyz");

    assertAll("Allow methods", () -> assertThat(headers, hasKey("Allow")),
        () -> assertThat(headers, hasValue(containsInAnyOrder("METHOD_B", "METHOD_A"))));
  }

  @DisplayName("Internal error")
  @Test
  void internalError() {
    // when
    FunctionTools.internalError(httpResponse);

    // then
    verify(httpResponse).setStatusCode(500, "Internal Server Error");
  }

  @DisplayName("OK")
  @Test
  void ok() {
    // when
    FunctionTools.ok(httpResponse);

    // then
    verify(httpResponse).appendHeader("Server", "Test Bot - 3.2.1-RELEASE");
    verify(httpResponse).setStatusCode(200, "OK");
    verifyNoMoreInteractions(httpResponse);
  }

  @DisplayName("OK with body")
  @Test
  void okWithBody() throws IOException {
    // given
    when(httpResponse.getWriter()).thenReturn(writer);

    // when
    FunctionTools.okWithBody(httpResponse, "{\"a\":\"b\"}");

    // then
    verify(httpResponse).appendHeader("Server", "Test Bot - 3.2.1-RELEASE");
    verify(httpResponse).setContentType("application/json;charset=utf-8");
    verify(httpResponse).setStatusCode(200, "OK");
    verifyNoMoreInteractions(httpResponse);
    verify(writer).write("{\"a\":\"b\"}");
  }

}