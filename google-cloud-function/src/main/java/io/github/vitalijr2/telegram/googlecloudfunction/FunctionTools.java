package io.github.vitalijr2.telegram.googlecloudfunction;

import com.google.cloud.functions.HttpResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class FunctionTools {

  private static final String APPLICATION_JSON = "application/json;charset=utf-8";
  private static final String FULL_VERSION_STRING;
  private static final String HTTP_BAD_METHOD_RESPONSE;
  private static final System.Logger LOGGER = System.getLogger(FunctionTools.class.getName());
  private static final String SERVER_HEADER = "Server";
  private static final String TEXT_HTML = "text/html;charset=utf-8";

  static {
    var body = "";
    var name = "unknown";
    var version = "unknown";

    try (InputStream versionPropsStream = FunctionTools.class.getResourceAsStream(
        "/google-cloud-function.properties")) {
      var properties = new Properties();

      properties.load(versionPropsStream);
      body = properties.getProperty("http.bad-method");
      name = properties.getProperty("bot.name");
      version = properties.getProperty("bot.version");
    } catch (Exception exception) {
      System.getLogger(FunctionTools.class.getName())
          .log(Level.ERROR, "Could not initialize the bot tools: {}", exception.getMessage());
      System.exit(1);
    }
    FULL_VERSION_STRING = name + " - " + version;
    HTTP_BAD_METHOD_RESPONSE = body;
  }

  private FunctionTools() {
  }

  /**
   * Make HTTP response.
   *
   * @param httpResponse  instance of HTTP response
   * @param statusCode    HTTP status
   * @param statusMessage HTTP status message
   * @param body          HTTP response body
   * @return prepared HTTP response
   */
  static HttpResponse doResponse(@NotNull HttpResponse httpResponse, int statusCode, @NotNull String statusMessage,
      @Nullable String body) {
    try {
      httpResponse.setStatusCode(statusCode, statusMessage);
      httpResponse.appendHeader(SERVER_HEADER, FULL_VERSION_STRING);
      if (null != body) {
        httpResponse.getWriter().write(body);
      }
    } catch (IOException exception) {
      LOGGER.log(Level.WARNING, "Could not make HTTP {} response: {}", statusCode, exception.getMessage());
    }

    return httpResponse;
  }

  /**
   * &quot;Method Not Allowed&quot; HTTP response.
   *
   * @param httpResponse   an instance of HTTP response.
   * @param allowedMethods list of allowed methods.
   */
  static void badMethod(@NotNull HttpResponse httpResponse, String... allowedMethods) {
    httpResponse.setContentType(TEXT_HTML);
    doResponse(httpResponse, 405, "Method Not Allowed", HTTP_BAD_METHOD_RESPONSE).getHeaders()
        .put("Allow", List.of(allowedMethods));
  }

  /**
   * &quot;Internal Server Error&quot; HTTP response.
   *
   * @param httpResponse an instance of HTTP response.
   */
  static void internalError(@NotNull HttpResponse httpResponse) {
    doResponse(httpResponse, 500, "Internal Server Error", null);
  }

  /**
   * &quot;OK&quot; HTTP response without body.
   *
   * @param httpResponse an instance of HTTP response.
   */
  static void ok(@NotNull HttpResponse httpResponse) {
    okWithBody(httpResponse, null);
  }

  /**
   * &quot;OK&quot; HTTP response with body.
   *
   * @param httpResponse an instance of HTTP response.
   * @param body         a response body.
   */
  static void okWithBody(@NotNull HttpResponse httpResponse, String body) {
    if (body != null) {
      httpResponse.setContentType(APPLICATION_JSON);
    }
    doResponse(httpResponse, 200, "OK", body);
  }

}
