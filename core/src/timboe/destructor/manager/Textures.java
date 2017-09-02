package timboe.destructor.manager;

public class Textures {

  private static Textures ourInstance;
  public static Textures getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new Textures(); }
  public void dispose() { ourInstance = null; }

  private Textures() {
  }

  public void reset() {
  }

}
