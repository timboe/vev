package co.uk.timmartin.vev.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import co.uk.timmartin.vev.Pair;
import co.uk.timmartin.vev.Param;
import co.uk.timmartin.vev.Util;
import co.uk.timmartin.vev.enums.Cardinal;
import co.uk.timmartin.vev.enums.Colour;
import co.uk.timmartin.vev.enums.Particle;
import co.uk.timmartin.vev.enums.TileType;
import co.uk.timmartin.vev.manager.GameState;
import co.uk.timmartin.vev.manager.Textures;
import co.uk.timmartin.vev.manager.World;
import co.uk.timmartin.vev.pathfinding.IVector2;

import static co.uk.timmartin.vev.enums.Colour.kBLACK;
import static co.uk.timmartin.vev.enums.Colour.kGREEN;

public class Tile extends Entity {

  public final List<Cardinal> pathFindDebug = new ArrayList<Cardinal>(); // Neighbours - but only used to draw debug gfx. Hence not worth persisting?
  public Map<Cardinal, Tile> n8; // Neighbours, cached for speed

  // Persistent
  public TileType type; // Ground, building, foliage, queue, cliff, stairs
  public Cardinal direction; // If stairs, then my direction (EW or NS)
  public Vector3 centreScaleTile = new Vector3(); // My centre in TILE coordinates
  public Vector3 centreScaleSprite = new Vector3(); // My centre in SPRITE coordinated (scaled x2)
  public final Set<Integer> containedSprites = new HashSet<Integer>(); // Moving sprites on this tile
  public final Map<Integer, Cardinal> parkingSpaces = new HashMap<Integer, Cardinal>(); // Four sprites allowed to "park" here
  public int mySprite = 0; // For buildings and foliage
  public Cardinal queueExit; // Which sub-space is my last
  public boolean queueClockwise; // If true, clockwise - if false, counterclockwise
  private String queueTex; // Delayed rendering
  public Boolean queueTexSet; // Delayed rendering activated

  public JSONObject serialise() throws JSONException {
    JSONObject json = super.serialise(true);
    json.put("type", type.name());
    json.put("direction", direction.name());
    json.put("centreScaleTile", Util.serialiseVec3(centreScaleTile));
    json.put("centreScaleSprite", Util.serialiseVec3(centreScaleSprite));
    JSONObject jsonContained = new JSONObject();
    Integer countContained = 0;
    for (int id : containedSprites) {
      jsonContained.put(countContained.toString(), id);
      ++countContained;
    }
    json.put("containedSprites", jsonContained);
    JSONObject jsonParking = new JSONObject();
    for (Map.Entry<Integer, Cardinal> entry : parkingSpaces.entrySet()) {
      jsonParking.put(entry.getKey().toString(), entry.getValue().name());
    }
    json.put("parkingSpaces", jsonParking);
    json.put("mySprite", mySprite);
    json.put("queueExit", queueExit.name());
    json.put("queueClockwise", queueClockwise);
    json.put("queueTex", queueTex);
    json.put("queueTexSet", queueTexSet);
    // NOTE: n8 not stored. Needs to be regenerated on load
    return json;
  }

  public Tile(JSONObject json) throws JSONException {
    super(json);
    this.queueTex = json.getString("queueTex");
    this.queueClockwise = json.getBoolean("queueClockwise");
    this.queueExit = Cardinal.valueOf(json.getString("queueExit"));
    this.queueTexSet = json.getBoolean("queueTexSet");
    this.mySprite = json.getInt("mySprite");
    JSONObject jsonParking = json.getJSONObject("parkingSpaces");
    Iterator it = jsonParking.keys();
    while (it.hasNext()) {
      String key = (String) it.next();
      this.parkingSpaces.put(Integer.parseInt(key), Cardinal.valueOf(jsonParking.getString(key)));
    }
    JSONObject jsonContained = json.getJSONObject("containedSprites");
    it = jsonContained.keys();
    while (it.hasNext()) {
      this.containedSprites.add(Integer.parseInt(jsonContained.getString((String) it.next())));
    }
    this.centreScaleSprite = Util.deserialiseVec3(json.getJSONObject("centreScaleSprite"));
    this.centreScaleTile = Util.deserialiseVec3(json.getJSONObject("centreScaleTile"));
    this.direction = Cardinal.valueOf(json.getString("direction"));
    this.type = TileType.valueOf(json.getString("type"));
    if (this.queueTex != "" && this.queueTexSet) { // Delayed rendering was activated
      setQueueTexture();
    }
  }

  public Tile(int x, int y) {
    super(x, y);
    setType(TileType.kGROUND, kBLACK, 0);
    this.mask = false;
    this.centreScaleTile.set(getX() + getHeight() / 2, getY() + getHeight() / 2, 0); // Tile scale
    this.centreScaleSprite.set(centreScaleTile);
    this.centreScaleSprite.scl(Param.SPRITE_SCALE); // Sprite scale
    this.direction = Cardinal.kNONE;
    this.queueExit = Cardinal.kNONE;
    this.queueClockwise = true;
    this.queueTex = "";
    this.queueTexSet = false;
  }

  public Set<IVector2> getPathFindNeighbours() {
    return coordinates.pathFindNeighbours;
  }

  public Entity getMySprite() {
    if (mySprite == 0) {
      return null;
    }
    if (GameState.getInstance().getBuildingMap().containsKey(mySprite)) {
      return GameState.getInstance().getBuildingMap().get(mySprite);
    }
    if (World.getInstance().foliage.containsKey(mySprite)) {
      return World.getInstance().foliage.get(mySprite);
    }
    if (isIntro && World.getInstance().introFoliage.containsKey(mySprite)) {
      return World.getInstance().introFoliage.get(mySprite);
    }
    Gdx.app.error("getMySprite", "Cannot resolve " + mySprite + " I am " + coordinates.toString() + " I am intro? " + isIntro);
    return null;
  }

  private void removeSprite() {
    Entity s = getMySprite();
    if (s == null) return;
    if (s.texString.contains("tree")) {
      ++GameState.getInstance().treesBulldozed;
    }
    s.remove(); // Foliage (from sprite batch)
    World.getInstance().foliage.remove(s.id);
    mySprite = 0;
  }

  public void setBuilding(Building b) {
    type = TileType.kBUILDING;
    removeSprite();
    mySprite = b.id;
  }

  public void removeBuilding() {
    this.type = TileType.kGROUND;
    this.mySprite = 0;
    this.direction = Cardinal.kNONE;
    this.queueExit = Cardinal.kNONE;
    this.queueClockwise = true;
    this.queueTexSet = false;
    this.queueTex = "";
    loadTexture();
  }

  public void setQueue(Cardinal from, Cardinal to, int buildingID, Cardinal queueExit, boolean queueClockwise) {
    type = TileType.kQUEUE;
    removeSprite();
    this.queueTex = "queue_" + tileColour.getString() + "_" + from.getString() + "_" + to.getString();
    this.queueExit = queueExit;
    this.queueClockwise = queueClockwise;
    this.queueTexSet = false;
    mySprite = buildingID;
  }

  public void setQueueTexture() {
    TextureRegion r = Textures.getInstance().getTexture(queueTex, false);
    if (r == null) {
      Gdx.app.error("setTexture", "Texture error " + queueTex);
      r = Textures.getInstance().getTexture("missing3", false);
    }
    setTexture(r, 0);
    this.queueTexSet = true;
    this.frames = 1;
  }

  public boolean buildable() {
    return tileColour == kGREEN && (type == TileType.kGROUND || type == TileType.kFOLIAGE);
  }

  public void setHighlightColour(Color c, Cardinal direction) {
    if (tileColour == kBLACK) return;
    setColor(c);
    doTint = true;
    tintArrow = direction;
    Entity e = getMySprite();
    if (e != null) {
      e.setColor(c);
      e.doTint = true;
    }
  }

  public boolean setBuildableHighlight() {
    if (buildable()) {
      setHighlightColour(Param.HIGHLIGHT_GREEN, Cardinal.kNONE);
      return true;
    } else {
      setHighlightColour(Param.HIGHLIGHT_RED, Cardinal.kNONE);
      return false;
    }
  }

  // Return wasParked
  public boolean tryRegSprite(Sprite s) {
    // De-reg from current
    Tile t = World.getInstance().getTile(s.myTile, isIntro);
    t.deRegSprite(s);

    Entity e = getMySprite();
    boolean isTruck = (s.getClass() == Truck.class);
    boolean isStartOfQueue = (e != null && e.getClass() == Building.class);

    if (isStartOfQueue && !isTruck) {
      // If this is not my final destination - you cannot reg here, but you're just about to move on so OK
      if (s.pathingList.size() > 0) {
        visitingSprite(s);
        return false;
      }

      Building b = (Building) e;
      // This *is* my final destination. Can I stay here?
      if (!b.canJoinQueue(s)) {
        // No - my type are not allowed to join this queue (or the building is still being built)
        visitingSprite(s);
        return false;
      }
      Pair<Tile, Cardinal> slot = b.getFreeLocationInQueue(s);
      if (slot == null) { // Cannot stay here
        // Do we have a standing order for overflow? This is the kBlank particle type
        List<IVector2> pList = b.getBuildingPathingList(Particle.kBlank);
        // We don't allow cyclic loops, however
        final boolean returning = (s.bouncedBuildings.contains(b.id));
        if (pList != null && !returning) { // We have an overflow destination configured
          s.pathingList = new LinkedList<IVector2>(pList); // Off you go little one!
          s.bouncedBuildings.add(b.id); // But don't come back!
          return true;
        } else { // You're on your own. Find somewhere nearby to loiter
          s.bouncedBuildings.clear();
          visitingSprite(s);
          return false;
        }
      } else { // Accepting to the building. We reg the sprite to (potentially) ANOTHER tile
        s.bouncedBuildings.clear();
        s.myTile = slot.getKey().coordinates;
        GameState.getInstance().removeFromSelectedSet(s);
        slot.getKey().parkSprite(s, slot.getValue());
        return true;
      }
    }

    // Regular tile - add the sprite
    if (!isTruck && parkingSpaces.size() < Cardinal.corners.size()) { // I am a regular tile, and have free slots
      for (Cardinal D : Cardinal.corners) {
        if (parkingSpaces.containsValue(D)) continue;
        parkSprite(s, D);
        return true;
      }
    }
    // Otherwise just visiting
    visitingSprite(s);
    return false; // No room on the tile for parking
  }

  public void visitingSprite(Sprite s) {
    s.myTile = coordinates;
    containedSprites.add(s.id);
  }

  public void parkSprite(Sprite s, Cardinal parking) {
    s.myTile = coordinates;
    containedSprites.add(s.id);
    parkingSpaces.put(s.id, parking);
    s.setNudgeDestination(this, parking);
  }

  public void deRegSprite(Sprite s) {
    containedSprites.remove(s.id);
    parkingSpaces.remove(s.id);
  }

  // Can no longer stay here
  public void moveOnSprites() {
    Set<Sprite> set = new HashSet<Sprite>();
    for (int id : containedSprites) {
      Sprite s = GameState.getInstance().getParticleMap().get(id);
      if (s == null) {
        Truck t = GameState.getInstance().getTrucksMap().get(id);
        if (t == null) {
          Gdx.app.error("moveOnSprites", "Cannot find hosted sprite " + id);
        }
        continue;
      }
      // If I am parked here, or just passing through but my destination is also now invalid
      // Cannot issue pathTo here as it will invalidate the containedSprites container
      if (s.pathingList.isEmpty() || s.getDestination().getPathFindNeighbours().isEmpty()) {
        set.add(s);
      }
    }
    for (Sprite s : set) {
      Tile newDest = Sprite.findPathingLocation(this, true, true, true, false); // Reproducible=True, requiresParking=True, requireSameHeight=True.
      if (newDest != null) s.pathTo(newDest, null, null); // Try path to
      if (s.pathingList == null && newDest != null) newDest.tryRegSprite(s); // Else go straight to
    }
  }

  public boolean hasParkingSpace() {
    return (!coordinates.pathFindNeighbours.isEmpty() && parkingSpaces.size() < Cardinal.corners.size());
  }

  public void setType(TileType t, Colour c, int l) {
    tileColour = c;
    type = t;
    level = l;
  }

  public void renderDebug(ShapeRenderer sr) {
    float x1 = getX() + getWidth() / 2;
    float y1 = getY() + getHeight() / 2;
    for (Cardinal D : pathFindDebug) {
      float y2 = y1, x2 = x1;
      switch (D) {
        case kN:
          y2 += getHeight() / 2;
          break;
        case kNE:
          y2 += getHeight() / 2;
          x2 += getHeight() / 2;
          break;
        case kE:
          x2 += getWidth() / 2;
          break;
        case kSE:
          x2 += getWidth() / 2;
          y2 -= getHeight() / 2;
          break;
        case kS:
          y2 -= getHeight() / 2;
          break;
        case kSW:
          y2 -= getHeight() / 2;
          x2 -= getWidth() / 2;
          break;
        case kW:
          x2 -= getWidth() / 2;
          break;
        case kNW:
          x2 -= getWidth() / 2;
          y2 += getHeight() / 2;
          break;
      }
      sr.line(x1, y1, x2, y2);
    }
    for (Cardinal D : parkingSpaces.values()) {
      x1 = getX() + getWidth() / 8;
      y1 = getY() + getHeight() / 8;
      switch (D) {
        case kSW:
          break;
        case kSE:
          x1 += getWidth() / 2;
          break;
        case kNE:
          x1 += getWidth() / 2; //fallthrough
        case kNW:
          y1 += getHeight() / 2;
          break;
      }
      sr.rect(x1, y1, getWidth() / 4, getHeight() / 4);
    }
  }


}
