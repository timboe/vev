package timboe.destructor.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Textures {

  private static Textures ourInstance;
  public static Textures getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new Textures(); }
  public void dispose() {
    atlas.dispose();
    ui.dispose();
    ourInstance = null;
  }

  private TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("sprites.txt"));
  private TextureAtlas ui = new TextureAtlas(Gdx.files.internal("uiskin.atlas"));

  private Textures() {
  }

  public TextureAtlas getUIAtlas() {
    return ui;
  }

  public TextureRegion getTexture(String name) {
    return atlas.findRegion(name);
  }


}
