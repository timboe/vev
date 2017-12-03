package timboe.destructor.manager;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import timboe.destructor.entity.Tile;

import java.util.LinkedList;
import java.util.List;

public class Sprites {

  public List<Tile> warpSpritesA;
  public List<Tile> warpSpritesB;
  public Batch spriteBatch;

  private static Sprites ourInstance;
  public static Sprites getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new Sprites(); }
  public void dispose() {
    spriteBatch.dispose();
    ourInstance = null;
  }

  private Sprites() {
    reset();
  }

  public void reset() {
    if (spriteBatch != null) spriteBatch.dispose();
    spriteBatch = new SpriteBatch();
    warpSpritesA = new LinkedList<Tile>();
    warpSpritesB = new LinkedList<Tile>();
  }

}
