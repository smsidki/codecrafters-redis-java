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
      var length = -1;
      var content = "";
      if (text != null && !text.isEmpty()) {
        length = text.length();
        content = "%s\r\n".formatted(text);
      }
      return "%s%d\r\n%s".formatted(this.getFirstByte(), length, content).getBytes(charset);
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
