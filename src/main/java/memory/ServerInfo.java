package memory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ServerInfo {

  private static ServerInfo INSTANCE;

  private String role;

  private ServerInfo() {}

  public static ServerInfo getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ServerInfo();
    }
    return INSTANCE;
  }




}
