package timboe.destructor.entity;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import timboe.destructor.Param;
import timboe.destructor.enums.Cardinal;
import timboe.destructor.enums.Colour;
import timboe.destructor.enums.TileType;
import timboe.destructor.pathfinding.IVector2;
import timboe.destructor.pathfinding.Node;

import java.util.*;

import static timboe.destructor.enums.Colour.kBLACK;

public class Tile extends Entity implements Node {

  public TileType type;
  public Cardinal direction;
  public List<Cardinal> pathFindDebug = new ArrayList<Cardinal>(); // Neighbours - but only used to draw debug gfx
  public Set<Tile> pathFindNeighbours = new HashSet<Tile>(); // Neighbours - used in pathfinding
  public Vector3 centreScaleTile = new Vector3(); // My centre in TILE coordinates
  public Vector3 centreScaleSprite = new Vector3(); // My centre in SPRITE coordinated (scaled x2)
  public IVector2 coordinates = new IVector2(); // X-Y tile coordinates
  private Set<Sprite> containedSprites = new HashSet<Sprite>(); // Sprites on this tile
  private Map<Sprite, Cardinal> parkingSpaces = new HashMap<Sprite, Cardinal>(); // Four sprites allowed to "park" here

  public Tile(int x, int y) {
    super(x, y);
    setType(TileType.kGROUND, kBLACK, 0);
    mask = false;
    coordinates.set(x,y);
    centreScaleTile.set(getX() + getHeight()/2, getY() + getHeight()/2, 0); // Tile scale
    centreScaleSprite.set(centreScaleTile);
    centreScaleSprite.scl(Param.SPRITE_SCALE); // Sprite scale
  }

  public Cardinal regSprite(Sprite s) {
    for (Tile t : pathFindNeighbours) { // De-reg from neighbours
      if(t.deRegSprite(s)) break;
    }
    containedSprites.add(s);
    if (parkingSpaces.size() < Cardinal.corners.size()) {
      for (Cardinal D : Cardinal.corners) {
        if (parkingSpaces.containsValue(D)) continue;
        parkingSpaces.put(s, D);
        return D;
      }
    }
    return Cardinal.kNONE;
  }

  public boolean hasParkingSpace() {
    return (parkingSpaces.size() < Cardinal.corners.size());
  }

  private boolean deRegSprite(Sprite s) {
    boolean contained = containedSprites.remove(s);
    return parkingSpaces.remove(s) != null || contained; // force both "remove"s to be evaluated
  }

  public void setType(TileType t, Colour c, int l) {
    colour = c;
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
