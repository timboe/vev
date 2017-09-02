package timboe.destructor.manager;

public class World {

  private static World ourInstance;
  public static World getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new World(); }
  public void dispose() { ourInstance = null; }

  private World() {
  }

  public void reset() {
  }

}
