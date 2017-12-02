package timboe.destructor.entity;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import timboe.destructor.enums.Cardinal;
import timboe.destructor.enums.Colour;
import timboe.destructor.enums.TileType;
import timboe.destructor.pathfinding.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static timboe.destructor.enums.Colour.kBLACK;

public class Tile extends Entity implements Node {

  public TileType type;
  public Cardinal direction;
  public List<Cardinal> pathFindDebug = new ArrayList<Cardinal>();
  public Set<Tile> pathFindNeighbours = new HashSet<Tile>();

  public Tile(int x, int y) {
    super(x, y);
    setType(TileType.kGROUND, kBLACK, 0);
    mask = false;
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
