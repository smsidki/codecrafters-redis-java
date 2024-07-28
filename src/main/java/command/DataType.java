package command;

import lombok.Getter;

import java.nio.charset.Charset;

@Getter
public enum DataType {

  NONE(null, null) {
    @Override
    public byte[] toBytes(String text, Charset charset) {
      return new byte[0];
    }
  },
  SIMPLE_STRINGS("Simple", "+") {
    @Override
    public byte[] toBytes(String text, Charset charset) {
      return "%s%s\r\n".formatted(this.getFirstByte(), text).getBytes(charset);
    }
  },
  BULK_STRINGS("Aggregate", "$") {
    @Override
    public byte[] toBytes(String text, Charset charset) {
      return "%s%d\r\n%s\r\n".formatted(this.getFirstByte(), text.length(), text).getBytes(charset);
    }
  };

  private final String category;
  private final String firstByte;

  DataType(String category, String firstByte) {
    this.category = category;
    this.firstByte = firstByte;
  }

  public abstract byte[] toBytes(String text, Charset charset);

}
