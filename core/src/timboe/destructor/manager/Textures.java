package timboe.destructor.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;

public class Textures {

  private Map<String, TextureRegion> flippedMap = new HashMap<String, TextureRegion>();
  private TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("sprites.txt"));
  private TextureAtlas ui = new TextureAtlas(Gdx.files.internal("uiskin.atlas"));
  private static Textures ourInstance;

  public static Textures getInstance() {
    return ourInstance;
  }

  public static void create() {
    ourInstance = new Textures();
  }

  public void dispose() {
    flippedMap.clear();
    atlas.dispose();
    ui.dispose();
    ourInstance = null;
  }

  private Textures() {
  }

  public TextureAtlas getUIAtlas() {
    return ui;
  }

  public TextureRegion getTexture(String name, boolean flipped) {
    if (flipped) {
      if (!flippedMap.containsKey(name)) {
        TextureRegion r = new TextureRegion(atlas.findRegion(name));
        r.flip(true, false);
        flippedMap.put(name, r);
      }
      return flippedMap.get(name);
    }
    return atlas.findRegion(name);
  }

}

