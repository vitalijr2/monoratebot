package test.provider;

import java.io.Reader;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class TestServiceProviderImplementation implements TestServiceProvider {

  @Override
  public String hello() {
    return "hello world";
  }

  @Override
  public @NotNull Optional<String> processPayload(@NotNull Reader payloadReader) {
    return Optional.empty();
  }

}
