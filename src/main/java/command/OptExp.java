package command;

public enum OptExp {

  PX {
    @Override
    public long expireAt(long expiration) {
      return System.currentTimeMillis() + expiration;
    }
  },
  EX {
    @Override
    public long expireAt(long expiration) {
      return System.currentTimeMillis() + (expiration * 1000);
    }
  };

  private static final OptExp[] OPT_EXPS;

  static {
    OPT_EXPS = values();
  }

  public abstract long expireAt(long expiration);

  public static OptExp resolve(String opt) {
    for (OptExp exp : OPT_EXPS) {
      if (exp.name().equalsIgnoreCase(opt)) {
        return exp;
      }
    }
    return null;
  }

}
