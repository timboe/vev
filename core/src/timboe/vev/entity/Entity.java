package timboe.vev.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.List;

import timboe.vev.Param;
import timboe.vev.Util;
import timboe.vev.enums.Cardinal;
import timboe.vev.enums.Colour;
import timboe.vev.enums.Particle;
import timboe.vev.manager.GameState;
import timboe.vev.manager.Textures;
import timboe.vev.manager.UI;
import timboe.vev.pathfinding.IVector2;

public class Entity extends Actor implements Serializable {

  // Persistable
  public int id;
  public boolean mask;
  public Colour tileColour;
  public int level;
  protected int scale;
  public int x, y;
  int frames;
  int frame;
  float time;
  public boolean selected;
  public boolean doTint = false;
  public final IVector2 coordinates = new IVector2(); // (initial) X-Y tile grid coordinates
  protected List<Tile> pathingList; // Used by building and sprite
  Particle pathingParticle; // Used only by building
  protected EnumMap<Particle, List<Tile>> buildingPathingLists;
  private String texString;
  private int texFrames;
  private boolean texFlipped;

  transient public final TextureRegion[] textureRegion = new TextureRegion[Param.MAX_FRAMES];

  public JSONObject serialise() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("id", id);
    json.put("mask", mask);
    json.put("tileColour", tileColour == null ? null : tileColour.getString());
    json.put("level", level);
    json.put("x",x);
    json.put("y",y);
    json.put("frames", frames);
    json.put("frame", frame);
    json.put("time", time);
    json.put("selected", selected);
    json.put("doTint", doTint);
    json.put("coordinates", coordinates.serialise());
    // pathingList
    json.put("pathingParticle", pathingParticle == null ? null : pathingParticle.toString());
    // buildingPathingLists
    json.put("texString", texString);
    json.put("texFrames", texFrames);
    json.put("texFlipped", texFlipped);
    return json;
  }

  public List<Tile> getPathingList() {
    return pathingList;
  }

  public Entity(int x, int y, int scale) {
    construct(x, y, scale);
  }

  public Entity(int x, int y) {
    construct(x ,y, Param.TILE_S);
  }

  private void construct(int x, int y, int scale) {
    this.id = GameState.getInstance().entitiyID++;
    this.scale = scale;
    this.frames = 1;
    this.frame = 0;
    this.time = 0;
    coordinates.set(x,y);
    textureRegion[0] = null;
    selected = false;
    texString = "";
    texFrames = 0;
    texFlipped = false;
    setBounds(x * scale, y * scale, scale, scale);
  }

  protected Entity() {
    textureRegion[0] = null;
  }

  public boolean setTexture(final String name, final int frames, boolean flipped) {
    texString = name;
    texFrames = frames;
    texFlipped = flipped;
    return loadTexture();
  }

  public boolean loadTexture() {
    boolean ok = true;
    for (int f = 0; f < texFrames; ++f) {
      final String texName = texString + (texFrames > 1 ? "_" + f : "");
      TextureRegion r = Textures.getInstance().getTexture(texName, texFlipped);
      if (r == null) {
        Gdx.app.error("setTexture", "Texture error " + texName);
        r = Textures.getInstance().getTexture("missing3", false);
        ok = false;
      }
      setTexture(r, f);
    }
    this.frame = Util.R.nextInt(texFrames); // TODO check this doesn't mess anything up
    this.frames = texFrames;
    return ok;
  }

  public Tile getDestination() {
    if (pathingList == null || pathingList.isEmpty()) return null;
    return pathingList.get( pathingList.size() - 1 );
  }

  public Tile getBuildingDestination(Particle p) {
    if (buildingPathingLists.get(p) == null || buildingPathingLists.get(p).isEmpty()) return null;
    final int s = buildingPathingLists.get(p).size();
    return buildingPathingLists.get(p).get( s - 1 );
  }

  public List<Tile> getBuildingPathingList(Particle p) {
    return buildingPathingLists.get(p);
  }

  protected void setTexture(TextureRegion r, int frame) {
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

  protected void doDraw(Batch batch) {
    batch.draw(textureRegion[frame],this.getX(),this.getY(),this.getOriginX(),this.getOriginY(),this.getWidth(),this.getHeight(),this.getScaleX(),this.getScaleY(),this.getRotation());
  }

  public void drawSelected(ShapeRenderer sr) {
    if (!selected) return;
    sr.setColor(1, 0, 0, 1);
    float off = Param.FRAME * 0.25f / (float)Math.PI;
    final float xC = getX() + getWidth()/2f, yC = getY() + getHeight()/2f;
    for (float a = (float)-Math.PI; a < Math.PI; a += 2f*Math.PI/3f) {
      sr.rectLine(xC + getWidth()/2f * ((float) Math.cos(a + off)),
          yC + getHeight()/2f * ((float) Math.sin(a + off)),
          xC + getWidth()/2f * ((float) Math.cos(a + off + Math.PI / 6f)),
          yC + getHeight()/2f * ((float) Math.sin(a + off + Math.PI / 6f)),
          2);
    }
  }

  public void drawPath(ShapeRenderer sr) {
    if (!selected && !UI.getInstance().showPathsButton.isChecked()) return;
    if (pathingList != null) { // in-progress
      sr.setColor(pathingParticle.getHighlightColour());
      drawList(pathingList, sr, pathingParticle.getStandingOrderOffset());
    }
    if (buildingPathingLists == null) return; // If not building
    for (Particle p : Particle.values()) {
      if (!buildingPathingLists.containsKey(p)) continue;
      if (p == pathingParticle) continue; // we already drew this
      sr.setColor( p.getHighlightColour() );
      drawList(buildingPathingLists.get(p), sr, p.getStandingOrderOffset());
    }
  }

  private void drawList(List<Tile> l, ShapeRenderer sr, int standingOrderOffset) {
    if (l == null || l.size() == 0) return;
    final int off = ((Param.FRAME / 2) + standingOrderOffset) % Param.TILE_S;
    Tile fin = l.get( l.size() - 1 );
    for (int i = 0; i < l.size(); ++i) {
      Tile previous = null;
      Tile current = l.get(i);
      if (i == 0) {
        for (Cardinal D : Cardinal.n8) {
          if (current.n8.get(D).mySprite == this) { // This connects to the queue too.... TODO change
            previous = current.n8.get(D);
            break;
          }
        }
      } else {
        previous = l.get(i - 1);
      }
      if (previous == null) continue;
      sr.rectLine(previous.centreScaleTile.x, previous.centreScaleTile.y,
          current.centreScaleTile.x, current.centreScaleTile.y, 2);
      if (current == previous.n8.get(Cardinal.kN)) {
        sr.rectLine(previous.centreScaleTile.x, previous.centreScaleTile.y + off,
            previous.centreScaleTile.x + 5, previous.centreScaleTile.y - 5 + off, 2);
        sr.rectLine(previous.centreScaleTile.x, previous.centreScaleTile.y + off,
            previous.centreScaleTile.x - 5, previous.centreScaleTile.y - 5 + off, 2);
      } else if (current == previous.n8.get(Cardinal.kS)) {
        sr.rectLine(previous.centreScaleTile.x, previous.centreScaleTile.y - off,
            previous.centreScaleTile.x + 5, previous.centreScaleTile.y + 5 - off, 2);
        sr.rectLine(previous.centreScaleTile.x, previous.centreScaleTile.y - off,
            previous.centreScaleTile.x - 5, previous.centreScaleTile.y + 5 - off, 2);
      } else if (current == previous.n8.get(Cardinal.kE)) {
        sr.rectLine(previous.centreScaleTile.x + off, previous.centreScaleTile.y,
            previous.centreScaleTile.x - 5 + off, previous.centreScaleTile.y - 5, 2);
        sr.rectLine(previous.centreScaleTile.x + off, previous.centreScaleTile.y,
            previous.centreScaleTile.x - 5 + off, previous.centreScaleTile.y + 5, 2);
      } else if (current == previous.n8.get(Cardinal.kW)) {
        sr.rectLine(previous.centreScaleTile.x - off, previous.centreScaleTile.y,
            previous.centreScaleTile.x + 5 - off, previous.centreScaleTile.y - 5, 2);
        sr.rectLine(previous.centreScaleTile.x - off, previous.centreScaleTile.y,
            previous.centreScaleTile.x + 5 - off, previous.centreScaleTile.y + 5, 2);
      } else if (current == previous.n8.get(Cardinal.kNE)) {
        sr.rectLine(previous.centreScaleTile.x + off, previous.centreScaleTile.y + off,
            previous.centreScaleTile.x + off - 7, previous.centreScaleTile.y + off, 2);
        sr.rectLine(previous.centreScaleTile.x + off, previous.centreScaleTile.y + off,
            previous.centreScaleTile.x + off, previous.centreScaleTile.y + off - 7, 2);
      } else if (current == previous.n8.get(Cardinal.kSW)) {
        sr.rectLine(previous.centreScaleTile.x - off, previous.centreScaleTile.y - off,
            previous.centreScaleTile.x - off + 7, previous.centreScaleTile.y - off, 2);
        sr.rectLine(previous.centreScaleTile.x - off, previous.centreScaleTile.y - off,
            previous.centreScaleTile.x - off, previous.centreScaleTile.y - off + 7, 2);
      } else if (current == previous.n8.get(Cardinal.kNW)) {
        sr.rectLine(previous.centreScaleTile.x - off, previous.centreScaleTile.y + off,
            previous.centreScaleTile.x - off, previous.centreScaleTile.y + off - 7, 2);
        sr.rectLine(previous.centreScaleTile.x - off, previous.centreScaleTile.y + off,
            previous.centreScaleTile.x - off + 7, previous.centreScaleTile.y + off, 2);
      } else if (current == previous.n8.get(Cardinal.kSE)) {
        sr.rectLine(previous.centreScaleTile.x + off, previous.centreScaleTile.y - off,
            previous.centreScaleTile.x + off, previous.centreScaleTile.y - off + 7, 2);
        sr.rectLine(previous.centreScaleTile.x + off, previous.centreScaleTile.y - off,
            previous.centreScaleTile.x + off - 7, previous.centreScaleTile.y - off, 2);
      }
    }
    sr.rect(fin.getX(), fin.getY(), fin.getOriginX(), fin.getOriginY(),
        fin.getWidth(), fin.getHeight(), 1f, 1f, 45f);
  }
}
