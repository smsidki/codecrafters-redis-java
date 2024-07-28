package memory;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache extends LinkedHashMap<String, LRUCache.Node> {

  private final int capacity;

  public LRUCache(int capacity) {
    super(capacity, 0.75f, true);
    this.capacity = capacity;
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<String, Node> eldest) {
    if (this.size() <= capacity) {
      return false;
    }
    var now = System.currentTimeMillis();
    if (eldest.getValue().expiration() < now) {
      return true;
    }
    var iterator = this.values().iterator();
    while (iterator.hasNext()) {
      var node = iterator.next();
      if (node.expiration() < now) {
        iterator.remove();
        return false;
      }
    }
    return true;
  }

  public String get(String key) {
    var node = super.get(key);
    if (node == null) {
      return null;
    }
    if (node.expiration() > 0 && node.expiration() < System.currentTimeMillis()) {
      remove(key);
      return null;
    }
    return node.value();
  }

  public void put(String key, String value, long expiration) {
    var node = new Node(key, value, expiration);
    super.put(key, node);
  }

  public record Node(
    String key,
    String value,
    long expiration
  ) { }


}
