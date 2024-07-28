package command;

import lombok.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Command {

  public static final List<String> COMMAND_NAMES = List.of(
    "COMMAND", "DOCS", "PING", "ECHO"
  );

  private final String name;
  private final List<String> arguments;

  @Builder
  public Command(String name, List<String> arguments) {
    this.name = name;
    this.arguments = Objects.requireNonNullElseGet(arguments, ArrayList::new);
  }

  public void addArgument(String argument) {
    this.arguments.add(argument);
  }

  public Response execute() {
    switch (this.name) {
      case "COMMAND", "DOCS", "PING" -> {
        return Response.builder()
          .dataType(DataType.SIMPLE_STRINGS)
          .text("PONG")
          .build();
      }
      case "ECHO" -> {
        return Response.builder()
          .dataType(DataType.BULK_STRINGS)
          .text(String.join(" ", this.arguments))
          .build();
      }
      default -> {
        return Response.builder().build();
      }
    }
  }

}
