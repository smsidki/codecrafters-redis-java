package command;

import lombok.Builder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Response {

  private final String text;
  private final Charset charset;
  private final DataType dataType;

  @Builder
  public Response(String text, Charset charset, DataType dataType) {
    this.text = text;
    this.charset = Objects.requireNonNullElse(charset, StandardCharsets.UTF_8);
    this.dataType = Objects.requireNonNullElse(dataType, DataType.SIMPLE_STRINGS);
  }

  public byte[] toBytes() {
    return this.dataType.toBytes(this.text, this.charset);
  }

}
