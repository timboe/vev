package timboe.destructor.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import timboe.destructor.Pair;
import timboe.destructor.Param;
import timboe.destructor.enums.Cardinal;
import timboe.destructor.enums.Colour;
import timboe.destructor.enums.TileType;
import timboe.destructor.manager.GameState;
import timboe.destructor.pathfinding.IVector2;
import timboe.destructor.pathfinding.Node;

import java.util.*;

import static timboe.destructor.enums.Colour.kBLACK;
import static timboe.destructor.enums.Colour.kGREEN;

public class Tile extends Entity implements Node {

  public TileType type; // Ground, building, foliage, queue, cliff, stairs
  public Cardinal direction; // If stairs, then my direction (EW or NS)

  public List<Cardinal> pathFindDebug = new ArrayList<Cardinal>(); // Neighbours - but only used to draw debug gfx
  public Set<Tile> pathFindNeighbours = new HashSet<Tile>(); // Neighbours - used in pathfinding

  public Vector3 centreScaleTile = new Vector3(); // My centre in TILE coordinates
  public Vector3 centreScaleSprite = new Vector3(); // My centre in SPRITE coordinated (scaled x2)
  public IVector2 coordinates = new IVector2(); // X-Y tile grid coordinates

  private Set<Sprite> containedSprites = new HashSet<Sprite>(); // Moving sprites on this tile
  public Map<Sprite, Cardinal> parkingSpaces = new HashMap<Sprite, Cardinal>(); // Four sprites allowed to "park" here
  public Entity mySprite = null; // For buildings and foliage

  public Map<Cardinal, Tile> n8; // Neighbours, cached for speed

  public List<Cardinal> queue; // the order sprites move through this tile when it is a queue

  public Tile(int x, int y) {
    super(x, y);
    setType(TileType.kGROUND, kBLACK, 0);
    mask = false;
    coordinates.set(x,y);
    centreScaleTile.set(getX() + getHeight()/2, getY() + getHeight()/2, 0); // Tile scale
    centreScaleSprite.set(centreScaleTile);
    centreScaleSprite.scl(Param.SPRITE_SCALE); // Sprite scale
  }

  public Set<Tile> getPathFindNeighbours() {
    return pathFindNeighbours;
  }

  private void removeSprite() {
    if (mySprite == null) return;
    mySprite.remove(); // Foliage (from sprite batch)
    mySprite = null;
  }

  public void setBuilding(Building b) {
    type = TileType.kBUILDING;
    removeSprite();
    mySprite = b;
  }

  public void setQueue(Cardinal from, Cardinal to, Building b, List<Cardinal> q, boolean setType) {
    if (setType) type = TileType.kQUEUE;
    removeSprite();
    setTexture("queue_"+tileColour.getString()+"_"+from.getString()+"_"+to.getString(), 1, false);
    queue = q;
    mySprite = b;
  }

  public boolean buildable() {
    return tileColour == kGREEN && (type == TileType.kGROUND || type == TileType.kFOILAGE);
  }

  public boolean setHighlight(boolean queueTint) {
    doTint = true;
    if (mySprite != null) mySprite.doTint = true;
    if (queueTint) {
      setColor(Param.HIGHLIGHT_YELLOW);
      if (mySprite != null) mySprite.setColor(Param.HIGHLIGHT_YELLOW);
      return true;
    } else if (buildable()) {
      setColor(Param.HIGHLIGHT_GREEN);
      if (mySprite != null) mySprite.setColor(Param.HIGHLIGHT_GREEN);
      return true;
    } else {
      setColor(Param.HIGHLIGHT_RED);
      if (mySprite != null) mySprite.setColor(Param.HIGHLIGHT_RED);
      return false;
    }
  }

  public Cardinal regSprite(Sprite s) {
    for (Tile t : pathFindNeighbours) { // De-reg from neighbours
      if(t.deRegSprite(s)) break;
    }
    containedSprites.add(s);
    boolean isStartOfQueue = (mySprite != null && mySprite.getClass() == Building.class);
    if (isStartOfQueue && s.pathingList.size() == 0) { // I am finishing at the entrance to the buildings queue
      Pair<Tile,Cardinal> slot = ((Building)mySprite).getFreeLocationInQueue();
      if (slot == null) return Cardinal.kNONE;
      // TODO not nice that sprite cannot call this on itself, but how to send both tile & cardinal?
      slot.getKey().parkingSpaces.put(s, slot.getValue());
      s.setNudgeDestination(slot.getKey(), slot.getValue());
      return Cardinal.kBUILDING_CONTROLLED;
    } else if (isStartOfQueue) {
      return Cardinal.kNONE; // Do not give passing through sprites a temp parking slot either
    } else if (parkingSpaces.size() < Cardinal.corners.size()) { // I am a regular tile, and have free slots
      for (Cardinal D : Cardinal.corners) {
        if (parkingSpaces.containsValue(D)) continue;
        parkingSpaces.put(s, D);
        return D;
      }
    }
    return Cardinal.kNONE; // No room on the tile for parking
  }

  // Can no longer stay here
  public void moveOnSprites() {
    if (!getPathFindNeighbours().isEmpty()) {
      Gdx.app.error("moveOnSprites", "Called on " + coordinates + ", but this tile is pathable");
      return; // Sanity check - should be true
    }
    Set<Sprite> set = new HashSet<Sprite>();
    for (Sprite s : containedSprites) {
      // If I am parked here, or just passing through but my destination is also now invalid
      // Cannot issue pathTo here as it will invalidate the containedSprites container
      if (s.pathingList.isEmpty() || s.getDestination().getPathFindNeighbours().isEmpty()) {
        set.add(s);
      }
    }
    for (Sprite s : set) s.pathTo(Sprite.findPathingNearbyLocation(this), null, null);
  }

  public boolean hasParkingSpace() {
    return (!pathFindNeighbours.isEmpty() && parkingSpaces.size() < Cardinal.corners.size());
  }

  public boolean deRegSprite(Sprite s) {
    boolean contained = containedSprites.remove(s);
    return parkingSpaces.remove(s) != null || contained; // force both "remove"s to be evaluated
  }

  public void setType(TileType t, Colour c, int l) {
    tileColour = c;
    type = t;
    level = l;
  }

  public void renderDebug(ShapeRenderer sr) {
    float x1 = getX() + getWidth()/2;
    float y1 = getY() + getHeight()/2;
    for (Cardinal D : pathFindDebug) {
      float y2 = y1, x2 = x1;
      switch (D) {
        case kN: y2 += getHeight()/2; break;
        case kNE: y2 += getHeight()/2; x2 += getHeight()/2; break;
        case kE: x2 += getWidth()/2; break;
        case kSE: x2 += getWidth()/2; y2 -= getHeight()/2; break;
        case kS: y2 -= getHeight()/2; break;
        case kSW: y2 -= getHeight()/2; x2 -= getWidth()/2; break;
        case kW: x2 -= getWidth()/2; break;
        case kNW: x2 -= getWidth()/2; y2 += getHeight()/2; break;
      }
      sr.line(x1, y1, x2, y2);
    }
    for (Cardinal D : parkingSpaces.values()) {
      x1 = getX() + getWidth()/8;
      y1 = getY() + getHeight()/8;
      switch (D) {
        case kSW: break;
        case kSE: x1 += getWidth()/2; break;
        case kNE: x1 += getWidth()/2; //fallthrough
        case kNW: y1 += getHeight()/2; break;
      }
      sr.rect(x1, y1, getWidth()/4, getHeight()/4);
    }
  }

  @Override
  public void act(float delta) {
    // So far - this will only be called on WARPs
    rotateBy((Float)getUserObject() * delta);
  }

  @Override
  public double getHeuristic(Object goal) {
    Tile g = (Tile)goal;
    return Math.hypot(x - g.x, y - g.y);
  }

  @Override
  public double getTraversalCost(Object neighbour) {
    return 1; // TODO tweak
  }

  @Override
  public Set getNeighbours() {
    return pathFindNeighbours;
  }
}
