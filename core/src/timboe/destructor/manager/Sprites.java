package timboe.destructor.manager;

import timboe.destructor.Param;
import timboe.destructor.entity.Tile;

public class Sprites {

  private static Sprites ourInstance;
  public static Sprites getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new Sprites(); }
  public void dispose() { ourInstance = null; }


  private Sprites() {
    reset();
  }

  public void reset() {

  }

}
