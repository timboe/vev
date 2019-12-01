package timboe.vev.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timboe.vev.Pair;
import timboe.vev.Param;
import timboe.vev.enums.Cardinal;
import timboe.vev.enums.Colour;
import timboe.vev.enums.Particle;
import timboe.vev.enums.TileType;
import timboe.vev.manager.World;
import timboe.vev.pathfinding.Node;

import static timboe.vev.enums.Colour.kBLACK;
import static timboe.vev.enums.Colour.kGREEN;

public class Tile extends Entity implements Node {

  public TileType type; // Ground, building, foliage, queue, cliff, stairs
  public Cardinal direction; // If stairs, then my direction (EW or NS)

  public final List<Cardinal> pathFindDebug = new ArrayList<Cardinal>(); // Neighbours - but only used to draw debug gfx
  public final Set<Tile> pathFindNeighbours = new HashSet<Tile>(); // Neighbours - used in pathfinding

  public final Vector3 centreScaleTile = new Vector3(); // My centre in TILE coordinates
  public final Vector3 centreScaleSprite = new Vector3(); // My centre in SPRITE coordinated (scaled x2)

  public final Set<Sprite> containedSprites = new HashSet<Sprite>(); // Moving sprites on this tile
  public final Map<Sprite, Cardinal> parkingSpaces = new HashMap<Sprite, Cardinal>(); // Four sprites allowed to "park" here
  public Entity mySprite = null; // For buildings and foliage

  public Map<Cardinal, Tile> n8; // Neighbours, cached for speed

  public Cardinal queueExit; // Which sub-space is my last
  public boolean queueClockwise; // If true, clockwise - if false, counterclockwise
  private String queueTex; // Delayed rendering

  public Tile(int x, int y) {
    super(x, y);
    setType(TileType.kGROUND, kBLACK, 0);
    mask = false;
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

  public void setQueue(Cardinal from, Cardinal to, Building b, Cardinal queueExit, boolean queueClockwise) {
    type = TileType.kQUEUE;
    removeSprite();
    queueTex = "queue_"+tileColour.getString()+"_"+from.getString()+"_"+to.getString();
    this.queueExit = queueExit;
    this.queueClockwise = queueClockwise;
    mySprite = b;
  }

  public void setQueueTexture() {
    setTexture(queueTex, 1, false);
  }

  public boolean buildable() {
    return tileColour == kGREEN && (type == TileType.kGROUND || type == TileType.kFOILAGE);
  }

  public void setHighlightColour(Color c) {
    setColor(c);
    doTint = true;
    if (mySprite != null) {
      mySprite.setColor(c);
      mySprite.doTint = true;
    }
  }

  public boolean setBuildableHighlight() {
    if (buildable()) {
      setHighlightColour(Param.HIGHLIGHT_GREEN);
      return true;
    } else {
      setHighlightColour(Param.HIGHLIGHT_RED);
      return false;
    }
  }

  // Return wasParked
  public boolean tryRegSprite(Sprite s) {
    // De-reg from current
    Tile t = World.getInstance().getTile(s.myTile);
    t.deRegSprite(s);

    boolean isTruck = (s.getClass() == Truck.class);
    boolean isStartOfQueue = (mySprite != null && mySprite.getClass() == Building.class);

    if (isStartOfQueue && !isTruck) {
      // If this is not my final destination - you cannot reg here, but you're just about to move on so OK
      if (s.pathingList.size() > 0) {
        visitingSprite(s);
        return false;
      }

      // This *is* my final destination. Can I stay here?
      Pair<Tile, Cardinal> slot = ((Building) mySprite).getFreeLocationInQueue(s);
      if (slot == null) { // Cannot stay here
        // Do we have a standing order for overflow? This is the kBlank particle type
        List<Tile> pList = mySprite.getBuildingPathingList(Particle.kBlank);
        if (pList != null) { // We have an overflow destination configured
          s.pathingList = new LinkedList<Tile>(pList); // Off you go little one!
          return true;
        } else { // You're on your own. Find somewhere nearby to loiter
          visitingSprite(s);
          return false;
        }
      } else { // We reg the sprite to (potentially) ANOTHER tile
        s.myTile = slot.getKey().coordinates;
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
    containedSprites.add(s);
  }

  public void parkSprite(Sprite s, Cardinal parking) {
    s.myTile = coordinates;
    containedSprites.add(s);
    parkingSpaces.put(s, parking);
    s.setNudgeDestination(this, parking);
  }

  public void deRegSprite(Sprite s) {
    containedSprites.remove(s);
    parkingSpaces.remove(s);
  }

  // Can no longer stay here
  public void moveOnSprites() {
    Set<Sprite> set = new HashSet<Sprite>();
    for (Sprite s : containedSprites) {
      // If I am parked here, or just passing through but my destination is also now invalid
      // Cannot issue pathTo here as it will invalidate the containedSprites container
      if (s.pathingList.isEmpty() || s.getDestination().getPathFindNeighbours().isEmpty()) {
        set.add(s);
      }
    }
    for (Sprite s : set) {
      Tile newDest = s.findPathingLocation(this, true, true, true, false); // Reproducible=True, requiresParking=True, requireSameHeight=True.
      if (newDest != null) s.pathTo(newDest, null, null); // Try path to
      if (s.pathingList == null && newDest != null) newDest.tryRegSprite(s); // Else go straight to
    }
  }

  public boolean hasParkingSpace() {
    return (!pathFindNeighbours.isEmpty() && parkingSpaces.size() < Cardinal.corners.size());
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
