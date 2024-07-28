package memory;

import java.util.HashMap;
import java.util.Map;

public class KV {

  private static final Map<String, String> DATA = new HashMap<>();

  public static void set(String key, String value) {
    DATA.put(key, value);
  }

  public static String get(String key) {
    return DATA.get(key);
  }

}
