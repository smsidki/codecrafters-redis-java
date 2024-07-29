package memory;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public final class ServerInfo {

  private static final Map<String, Object> STATISTIC = new HashMap<>();

  public static void role(String role) {
    STATISTIC.put("role", role);
  }

  public static String role() {
    return (String) STATISTIC.get("role");
  }

  public static void masterReplID(String masterReplID) {
    STATISTIC.put("master_replid", masterReplID);
  }

  public static String masterReplID() {
    return (String) STATISTIC.get("master_replid");
  }

  public static void masterReplOffset(int masterReplOffset) {
    STATISTIC.put("master_repl_offset", masterReplOffset);
  }

  public static int masterReplOffset() {
    return (Integer) STATISTIC.get("master_repl_offset");
  }

}
