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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import timboe.vev.Param;
import timboe.vev.Util;
import timboe.vev.enums.Cardinal;
import timboe.vev.enums.Colour;
import timboe.vev.enums.Particle;
import timboe.vev.enums.UIMode;
import timboe.vev.manager.GameState;
import timboe.vev.manager.Textures;
import timboe.vev.manager.UI;
import timboe.vev.manager.World;
import timboe.vev.pathfinding.IVector2;

public class Entity extends Actor implements Serializable {

  // Persistable
  public int id;
  public boolean mask;
  public Colour tileColour;
  public int level;
  protected int scale;
  int frames;
  int frame;
  float time;
  public boolean selected;
  public boolean doTint = false;
  public final IVector2 coordinates; // (initial) X-Y tile grid coordinates
  protected List<IVector2> pathingList; // Used by building and sprite
  Particle pathingParticle; // Used only by building
  public boolean isIntro;
  EnumMap<Particle, List<IVector2>> buildingPathingLists;
  public String texString;
  private int texFrames;
  private boolean texFlipped;

  transient final TextureRegion[] textureRegion = new TextureRegion[Param.MAX_FRAMES];

  public JSONObject serialise(boolean isTile) throws JSONException {
    JSONObject json = new JSONObject();
    json.put("id", this.id);
    json.put("mask", this.mask);
    json.put("tileColour", this.tileColour == null ? JSONObject.NULL : tileColour.name());
    json.put("level", this.level);
    json.put("scale", this.scale);
    json.put("x", getX());
    json.put("y", getY());
    json.put("frames", this.frames);
    json.put("frame", this.frame);
    json.put("time", this.time);
    json.put("selected", this.selected);
    json.put("doTint", this.doTint);
    json.put("coordinates", isTile ? this.coordinates.serialiseTile() : this.coordinates.serialise());
    if (this.pathingList != null) {
      JSONObject pathing = new JSONObject();
      Integer count = 0;
      for (IVector2 v : this.pathingList) {
        pathing.put(count.toString(), v.serialise());
        ++count;
      }
      json.put("pathingList", pathing);
    } else {
      json.put("pathingList", JSONObject.NULL);
    }
    json.put("pathingParticle", this.pathingParticle == null ? JSONObject.NULL : this.pathingParticle.toString());
    json.put("isIntro", this.isIntro);
    if (this.buildingPathingLists != null) {
      JSONObject mapObj = new JSONObject();
      for (EnumMap.Entry<Particle, List<IVector2>> entry : this.buildingPathingLists.entrySet()) {
        JSONObject vecList = new JSONObject();
        Integer count = 0;
        //TODO got a null on the next line from one serialisation of a Warp
        for (IVector2 v : entry.getValue()) {
          vecList.put(count.toString(), v.serialise());
          Gdx.app.log("DBG_S",count.toString() + " = " + v.serialise().toString());
          ++count;
        }
        mapObj.put(entry.getKey().name(), vecList);
      }
      json.put("buildingPathingLists", mapObj);
    } else {
      json.put("buildingPathingLists", JSONObject.NULL);
    }
    json.put("texString", this.texString);
    json.put("texFrames", this.texFrames);
    json.put("texFlipped", this.texFlipped);
    return json;
  }

  public Entity(JSONObject json) throws JSONException {
    this.texFlipped = json.getBoolean("texFlipped");
    this.texFrames = json.getInt("texFrames");
    this.texString = json.getString("texString");
    //
    if (json.get("buildingPathingLists") == JSONObject.NULL) {
      this.buildingPathingLists = null;
    } else {
      this.buildingPathingLists = new EnumMap<Particle, List<IVector2>>(Particle.class);
      JSONObject bpJson = json.getJSONObject("buildingPathingLists");
      Iterator mapObjIt =  bpJson.keys();
      while (mapObjIt.hasNext()) {
        String particleStrKey = (String) mapObjIt.next();
        JSONObject vecList = bpJson.getJSONObject(particleStrKey);
        Iterator vecListIt = vecList.keys();
        List<IVector2> pl = new LinkedList<IVector2>();
        int maxKey = -1;
        while (vecListIt.hasNext()) {
          maxKey = Math.max(maxKey, Integer.valueOf((String) vecListIt.next()));
        }
        if (maxKey >= 0) {
          for (Integer i = 0; i <= maxKey; ++i) {
            IVector2 v = new IVector2(vecList.getJSONObject(i.toString()));
            Gdx.app.log("DBG_D",i.toString() + " = " + vecList.getJSONObject(i.toString()).toString());
            pl.add(v);
          }
        }
        this.buildingPathingLists.put(Particle.valueOf(particleStrKey), pl);
      }
    }
    //
    this.isIntro = json.getBoolean("isIntro");
    if (json.get("pathingParticle") == JSONObject.NULL) {
      this.pathingParticle = null;
    } else {
      this.pathingParticle = Particle.valueOf(json.getString("pathingParticle"));
    }
    //
    if (json.get("pathingList") == JSONObject.NULL) {
      this.pathingList = null;
    } else {
      JSONObject pathingListJson = json.getJSONObject("pathingList");
      this.pathingList = new Vector<IVector2>();
      Iterator plIt = pathingListJson.keys();
      int maxKey = -1;
      while (plIt.hasNext()) {
        maxKey = Math.max(maxKey, Integer.valueOf((String) plIt.next()));
      }
      if (maxKey >= 0) {
        for (Integer i = 0; i <= maxKey; ++i) {
          IVector2 v = new IVector2(pathingListJson.getJSONObject(i.toString()));
          this.pathingList.add(v);
        }
      }
    }
    //
    this.coordinates = new IVector2( json.getJSONObject("coordinates") );
    this.doTint = json.getBoolean("doTint");
    this.selected = json.getBoolean("selected");
    this.time = (float) json.getDouble("time");
    this.frame = json.getInt("frame");
    this.frames = json.getInt("frames");
    this.scale = json.getInt("scale");
    setY( (float) json.getDouble("y") );
    setX( (float) json.getDouble("x") );
    this.level = json.getInt("level");
    if (json.getString("tileColour") == JSONObject.NULL || json.getString("tileColour").equals("null")) { // Why this 2nd check?
      this.tileColour = null;
    } else {
      this.tileColour = Colour.valueOf( json.getString("tileColour") );
    }
    this.mask = json.getBoolean("mask");
    this.id = json.getInt("id");
    //
    this.textureRegion[0] = null;
    setBounds(this.getX(), this.getY(), this.scale, this.scale);
    if (!loadTexture() && !texString.equals("")) {
      Gdx.app.error("Load","Failed to load "+texString);
    }
  }

  public List<IVector2> getPathingList() {
    return pathingList;
  }

  public Entity(int x, int y, int scale) {
    this.coordinates = new IVector2(x,y);
    construct(x, y, scale);
  }

  public Entity(int x, int y) {
    this.coordinates = new IVector2(x,y);
    construct(x ,y, Param.TILE_S);
  }

  private void construct(int x, int y, int scale) {
    this.id = GameState.getInstance().entityID++;
    this.scale = scale;
    this.frames = 1;
    this.frame = -1;
    this.time = 0;
    this.textureRegion[0] = null;
    this.selected = false;
    this.texString = "";
    this.texFrames = 0;
    this.texFlipped = false;
    this.isIntro = false;
    setBounds(x * scale, y * scale, scale, scale);
  }

  public boolean setTexture(final String name, final int frames, boolean flipped) {
    texString = name;
    texFrames = frames;
    texFlipped = flipped;
    return loadTexture();
  }

  public boolean loadTexture() {
    if (texString.equals("")) {
      return false;
    }
    boolean ok = true;
    for (int f = 0; f < texFrames; ++f) {
      final String texName = texString + (texFrames > 1 ? "_" + f : "");
//      Gdx.app.log("DBG", "Get " + texName);

      TextureRegion r = Textures.getInstance().getTexture(texName, texFlipped);
      if (r == null) {
        Gdx.app.error("setTexture", "Texture error " + texName);
        r = Textures.getInstance().getTexture("missing3", false);
        ok = false;
      }
      setTexture(r, f);
    }
    if (this.frame == -1) {
      this.frame = Util.R.nextInt(texFrames); // TODO check this doesn't mess anything up
    }
    this.frames = texFrames;
    return ok;
  }

  Tile coordinateToTile(IVector2 v) {
    return (isIntro ? World.getInstance().getIntroTile(v) : World.getInstance().getTile(v));
  }

  public Tile getDestination() {
    if (pathingList == null || pathingList.isEmpty()) return null;
    return coordinateToTile( pathingList.get( pathingList.size() - 1 ));
  }

  public Tile getBuildingDestination(Particle p) {
    if (buildingPathingLists.get(p) == null || buildingPathingLists.get(p).isEmpty()) return null;
    final int s = buildingPathingLists.get(p).size();
    return coordinateToTile( buildingPathingLists.get(p).get( s - 1 ) );
  }

  public List<IVector2> getBuildingPathingList(Particle p) {
    return buildingPathingLists.get(p);
  }

  void setTexture(TextureRegion r, int frame) {
    textureRegion[frame] = r;
    setWidth(textureRegion[frame].getRegionWidth());
    setHeight(textureRegion[frame].getRegionHeight());
    setOrigin(Align.center);
  }

  @Override
  public void draw(Batch batch, float alpha) {
    if (frame == -1 || textureRegion[frame] == null) return;
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
    if (!selected && !(UI.getInstance().uiMode == UIMode.kSETTINGS)) return;
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

  private void drawList(List<IVector2> l, ShapeRenderer sr, int standingOrderOffset) {
    if (l == null || l.size() == 0) return;
    final int off = ((Param.FRAME / 2) + standingOrderOffset) % Param.TILE_S;
    Tile fin = coordinateToTile( l.get( l.size() - 1 ) );
    for (int i = 0; i < l.size(); ++i) {
      Tile previous = null;
      Tile current = coordinateToTile( l.get(i) );
      if (i == 0) {
        for (Cardinal D : Cardinal.n8) {
          Integer s = current.n8.get(D).mySprite;
          if (s != 0 && s == this.id) { // This connects to the queue too.... TODO change
            previous = current.n8.get(D);
            break;
          }
        }
      } else {
        previous = coordinateToTile( l.get(i - 1) );
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
