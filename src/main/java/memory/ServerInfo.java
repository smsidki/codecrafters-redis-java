package memory;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class ServerInfo {

  private static ServerInfo INSTANCE;

  private String role;
  private String replicationId;
  private long replicationOffset;

  private ServerInfo() {}

  public static ServerInfo getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ServerInfo();
      INSTANCE.setReplicationOffset(0L);
      INSTANCE.setReplicationId("8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb");
    }
    return INSTANCE;
  }




}
