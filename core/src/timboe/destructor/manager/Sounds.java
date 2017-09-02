package timboe.destructor.manager;

public class Sounds {

  private static Sounds ourInstance;
  public static Sounds getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new Sounds(); }
  public void dispose() { ourInstance = null; }

  private Sounds() {
  }

  public void reset() {
  }

}
