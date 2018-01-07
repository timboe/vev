package timboe.destructor.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import timboe.destructor.Param;
import timboe.destructor.enums.Colour;
import timboe.destructor.manager.Textures;

public class Entity extends Actor {

  public boolean mask;
  public Colour tileColour;
  public int level;
  protected int scale;
  public int x, y;
  protected int frames;
  protected int frame;
  protected float time;
  public boolean selected;
  public Rectangle boundingBox = new Rectangle();
  public boolean doTint = false;

  public TextureRegion[] textureRegion = new TextureRegion[Param.MAX_FRAMES];

  public Entity(int x, int y, int scale) {
    construct(x, y, scale);
  }

  public Entity(int x, int y) {
    construct(x ,y, Param.TILE_S);
  }

  private void construct(int x, int y, int scale) {
    this.scale = scale;
    this.x = x;
    this.y = y;
    this.frames = 1;
    this.frame = 0;
    this.time = 0;
    textureRegion[0] = null;
    selected = false;
    setBounds(x * scale, y * scale, scale, scale);
  }

  protected Entity() {
    textureRegion[0] = null;
  }

  public void setTexture(final String name, final int frames, boolean flipped) {
    for (int frame = 0; frame < frames; ++frame) {
      final String texName = name + (frames > 1 ? "_" + frame : "");
      TextureRegion r = Textures.getInstance().getTexture(texName, flipped);
      if (r == null) {
        Gdx.app.error("setTexture", "Texture error " + texName);
        r = Textures.getInstance().getTexture("missing3", false);
      }
      setTexture(r, frame);
    }
    this.frames = frames;
  }

  private void setTexture(TextureRegion r, int frame) {
    textureRegion[frame] = r;
    setWidth(textureRegion[frame].getRegionWidth());
    setHeight(textureRegion[frame].getRegionHeight());
    setOrigin(Align.center);
  }

  @Override
  public void draw(Batch batch, float alpha) {
    if (textureRegion[frame] == null) return;
    if (doTint) {
      batch.setColor(getColor());
      doDraw(batch);
      batch.setColor(1f,1f,1f,1f);
      doTint = false; // Only lasts one frame - needs to be re-set in act
    } else {
      doDraw(batch);
    }
//    draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation)
  }

  private void doDraw(Batch batch) {
    batch.draw(textureRegion[frame],this.getX(),this.getY(),this.getOriginX(),this.getOriginY(),this.getWidth(),this.getHeight(),this.getScaleX(),this.getScaleY(),this.getRotation());
  }

  public void draw(ShapeRenderer sr) {
    if (!selected) return;
    float off = Param.FRAME * 0.25f / (float)Math.PI;
    final float xC = getX() + getWidth()/2f, yC = getY() + getHeight()/2f;
    for (float a = (float)-Math.PI; a < Math.PI; a += 2f*Math.PI/3f) {

      sr.rectLine(xC + getWidth() * ((float) Math.cos(a + off)),
          yC + getHeight() * ((float) Math.sin(a + off)),
          xC + getWidth()/2f * ((float) Math.cos(a + off + Math.PI / 6f)),
          yC + getHeight()/2f * ((float) Math.sin(a + off + Math.PI / 6f)),
          2);
    }
  }
}
