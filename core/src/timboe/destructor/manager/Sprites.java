package timboe.destructor.manager;

public class Sprites {

  private static Sprites ourInstance;
  public static Sprites getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new Sprites(); }
  public void dispose() { ourInstance = null; }

  private Sprites() {
  }

  public void reset() {
  }

}
