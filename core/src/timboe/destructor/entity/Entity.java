package timboe.destructor.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import timboe.destructor.Param;
import timboe.destructor.enums.Colour;
import timboe.destructor.manager.Textures;

public class Entity extends Actor {

  public boolean mask;
  public Colour colour;
  public int level;
  private int scale;
  public int x, y;

  protected TextureRegion[] textureRegion = new TextureRegion[Param.MAX_FRAMES];

  public TextureRegion[] getTextureRegion() {
    return textureRegion;
  }

  public Entity(int x, int y, int scale) {
    this.scale = scale;
    this.x = x;
    this.y = y;
    textureRegion[0] = null;
    setBounds(x * scale, y * scale, scale, scale);
  }

  public Entity(int x, int y) {
    this.scale = Param.TILE_S;
    this.x = x;
    this.y = y;
    textureRegion[0] = null;
    setBounds(x * scale, y * scale, scale, scale);
  }

  public Entity() {
    textureRegion[0] = null;
  }

  public void originToCentre() {
//    setOrigin(getX() + getWidth()/2, getY() + getHeight()/2);
  }

  public void setTexture(String name, int frames) {
    TextureRegion r = Textures.getInstance().getTexture(name);
    if (r == null) {
      Gdx.app.error("setTexture", "Texture error " + name);
      r = Textures.getInstance().getTexture("missing3");
    } else {
     setTexture(r);
    }
  }

  public void setTexture(TextureRegion r) {
    textureRegion[0] = r;
    setWidth(textureRegion[0].getRegionWidth());
    setHeight(textureRegion[0].getRegionHeight());
    setOrigin(Align.center);
  }

  @Override
  public void draw(Batch batch, float alpha) {
    if (textureRegion[0] == null) return;
    batch.draw(textureRegion[0], this.getX(), this.getY(), this.getOriginX(), this.getOriginY(), this.getWidth(), this.getHeight(), this.getScaleX(), this.getScaleY(), this.getRotation());
//    draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation)
  }
}
