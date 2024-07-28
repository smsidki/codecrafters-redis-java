package command;

import lombok.Getter;
import memory.KV;

import java.util.List;

@Getter
public class CmdSet {

  private final String key;
  private final String value;
  private final long expiration;

  public CmdSet(List<String> args) {
    this.key = args.get(0);
    this.value = args.get(1);

    if (args.size() < 4) {
      this.expiration = 0L;
    } else {
      //noinspection DataFlowIssue
      this.expiration = OptExp.resolve(args.get(2)).expireAt(Long.parseLong(args.get(3)));
    }
  }

  public String execute() {
    KV.set(this.key, this.value, this.expiration);
    return "OK";
  }

}
