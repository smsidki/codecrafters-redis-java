package memory;

public class KV {

  private static final LRUCache CACHE = new LRUCache(100);

  public static void set(String key, String value, long expiration) {
    CACHE.put(key, value, expiration);
  }

  public static String get(String key) {
    return CACHE.get(key);
  }

}
