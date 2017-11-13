package timboe.destructor.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import timboe.destructor.Param;
import timboe.destructor.manager.Textures;

public class Entity extends Actor {

  protected TextureRegion[] textureRegion = new TextureRegion[Param.MAX_FRAMES];

  public Entity(int x, int y) {
    setBounds(x * Param.TILE_S, y * Param.TILE_S, Param.TILE_S, Param.TILE_S);
  }

  public void setTexture(String name, int frames) {
    textureRegion[0] = Textures.getInstance().getTexture(name);
    if (textureRegion[0] == null) Gdx.app.error("setTexture", "Texture error " + name);
  }

  @Override
  public void draw(Batch batch, float alpha) {
    batch.draw(textureRegion[0], this.getX(), this.getY());
  }
}
