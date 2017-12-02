package timboe.destructor.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import timboe.destructor.Param;
import timboe.destructor.Util;
import timboe.destructor.enums.Colour;
import timboe.destructor.manager.Textures;

public class Entity extends Actor {

  public boolean mask;
  public Colour colour;
  public int level;
  private int scale;
  public int x, y;

  protected TextureRegion[] textureRegion = new TextureRegion[Param.MAX_FRAMES];

  public Entity(int x, int y, int scale, int wiggle) {
    this.scale = scale;
    this.x = x;
    this.y = y;
    textureRegion[0] = null;
    setBounds(x * scale, y * scale, scale, scale);
    moveBy((-wiggle) + Util.R.nextInt(wiggle*2), (-wiggle) + Util.R.nextInt(wiggle*2));
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

  public void setTexture(String name, int frames) {
    textureRegion[0] = Textures.getInstance().getTexture(name);
    if (textureRegion[0] == null) {
      Gdx.app.error("setTexture", "Texture error " + name);
      textureRegion[0] = Textures.getInstance().getTexture("missing3");
    }
  }
//
//  public void flip() {
//    textureRegion[0].flip(true,false);
//  }

  @Override
  public void draw(Batch batch, float alpha) {
    if (textureRegion[0] == null) return;
    batch.draw(textureRegion[0], this.getX(), this.getY());
  }
}
