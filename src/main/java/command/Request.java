package command;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Request {

  private final List<Command> commands;

  @Builder
  public Request(String reqLine) {
    this.commands = new ArrayList<>();
    var reqLines = reqLine.split("\r\n");

    var command = (Command) null;
    for (var i = 2; i <= Integer.parseInt(reqLines[0].substring(1)) * 2; i=i+2) {
      var reqContent = reqLines[i];
      log.debug("Request content: {}", reqContent);

      if (Command.COMMAND_NAMES.stream().anyMatch(commandName -> commandName.equalsIgnoreCase(reqContent))) {
        command = Command.builder().name(reqContent.toUpperCase()).build();
        this.commands.add(command);
      } else {
        if (command != null) {
          command.addArgument(reqContent);
        } else {
          throw new IllegalArgumentException("No command registered for argument: " + reqContent);
        }
      }
    }
  }

  public byte[] executeCommands() {
    return this
      .commands
      .stream()
      .map(Command::execute)
      .map(Response::toBytes)
      .collect(
        ByteArrayOutputStream::new, (os, bytes) -> os.write(bytes, 0, bytes.length), (osA, osB) -> {}
      )
      .toByteArray();
  }

}
