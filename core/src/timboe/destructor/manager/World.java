package timboe.destructor.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import timboe.destructor.Param;
import timboe.destructor.Util;
import timboe.destructor.entity.Tile;
import timboe.destructor.enums.Cardinal;
import timboe.destructor.enums.Colour;
import timboe.destructor.enums.Edge;
import timboe.destructor.enums.TileType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

public class World {

  private static World ourInstance;
  public static World getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new World(); }
  public void dispose() { ourInstance = null; }
  private Random R = new Random();

  Tile[][] tiles;

  private World() {
    generate();
  }

  public void reset() {
    tiles = new Tile[Param.TILES_X][Param.TILES_Y];
    for (int x = 0; x < Param.TILES_X; ++x) {
      for (int y = 0; y < Param.TILES_Y; ++y) {
        tiles[x][y] = new Tile(x,y);
        //tiles[x][y].setDebug(true);
        GameState.getInstance().getStage().addActor(tiles[x][y]);
      }
    }
  }

  public void generate() {
    boolean success = false;
    while (!success) {
      reset();
      success = doEdges();
    }
    applyTileGraphics();
  }

  private boolean increaseStep(Vector2 v, Cardinal D, int end) {
    switch (D) {
      case kN: return (++v.y == Param.TILES_Y - end);
      case kE: return (++v.x == Param.TILES_X - end);
      case kS: return (--v.y == end);
      case kW: return (--v.x == end);
      default: Gdx.app.error("increaseStep","Unknown direction");
    }
    return false;
  }

  private boolean krinkle(Vector2 v, Cardinal D, Edge direction, int maxIncursion) {
    final int MAX_KRINKLE = 3; // Must be ODD
    int dist = R.nextInt(MAX_KRINKLE);
    switch (direction) {
      case kFLAT: dist -= Math.floor(MAX_KRINKLE/2.); break;
      case kOUT: dist *= -1; break;
      case kIN: break;
      default: Gdx.app.error("krinkle","Unknown enum");
    }
    switch (D) {
      case kN: v.x += dist; break;
      case kE: v.y -= dist; break;
      case kS: v.x -= dist; break;
      case kW: v.y += dist; break;
      default: Gdx.app.error("krinkle","Unknown direction");
    }
    if (D == Cardinal.kN && Util.needsClamp((int)v.x, 1, maxIncursion)) {
      v.x = (int)Util.clamp((int)v.x, 1, maxIncursion);
      return true;
    } else if (D == Cardinal.kS && Util.needsClamp((int)v.x, Param.TILES_X - maxIncursion, Param.TILES_X - 1)) {
      v.x = (int) Util.clamp((int) v.x, Param.TILES_X - maxIncursion, Param.TILES_X - 1);
      return true;
    } else if (D == Cardinal.kW && Util.needsClamp((int)v.y, 1, maxIncursion)) {
      v.y = (int)Util.clamp((int)v.y, 1, maxIncursion);
      return true;
    } else if (D == Cardinal.kE && Util.needsClamp((int)v.y, Param.TILES_Y - maxIncursion, Param.TILES_Y - 1)) {
      v.y = (int) Util.clamp((int) v.y, Param.TILES_Y - maxIncursion, Param.TILES_Y - 1);
      return true;
    }
    return false;
  }

  private void setToVoid(Vector2 v, Cardinal D) {
    switch (D) {
      case kN: for (int x = (int)v.x; x >= 0; --x) tiles[x][(int)v.y].setType(TileType.kGROUND, Colour.kBLACK, 0);
        break;
      case kE: for (int y = (int)v.y; y < Param.TILES_Y; ++y) tiles[(int)v.x][y].setType(TileType.kGROUND, Colour.kBLACK, 0);
        break;
      case kS: for (int x = (int)v.x; x < Param.TILES_X; ++x) tiles[x][(int)v.y].setType(TileType.kGROUND, Colour.kBLACK, 0);
        break;
      case kW: for (int y = (int)v.y; y >= 0; --y) tiles[(int)v.x][y].setType(TileType.kGROUND, Colour.kBLACK, 0);
        break;
      default: Gdx.app.error("setToVoid","Unknown direction");
    }
  }

  private void kornerKiller(Vector2 v, Cardinal D) {
    switch (D) {
      case kN:
        for (int x = (int)v.x; x >= 0; --x) {
          for (int y = (int)v.y; y < Param.TILES_Y; ++y) tiles[x][y].setType(TileType.kGROUND, Colour.kBLACK, 0);
        }
        break;
      case kE:
        for (int x = (int)v.x; x < Param.TILES_X; ++x) {
          for (int y = (int)v.y; y < Param.TILES_Y; ++y) tiles[x][y].setType(TileType.kGROUND, Colour.kBLACK, 0);
        }
        break;
      case kS:
        for (int x = (int)v.x; x < Param.TILES_X; ++x) {
          for (int y = (int)v.y; y >= 0; --y) tiles[x][y].setType(TileType.kGROUND, Colour.kBLACK, 0);
        }
        break;
      case kW:
        for (int x = (int)v.x; x >= 0; --x) {
          for (int y = (int)v.y; y >= 0; --y) tiles[x][y].setType(TileType.kGROUND, Colour.kBLACK, 0);
        }
        break;
      default: Gdx.app.error("kornerKiller","Unknown direction");
    }
  }

  private boolean doEdges() {
    final int EDGE_OFFSET = 5;
    final int MAX_INCURSION = EDGE_OFFSET * 5;
    final int MIN_DIST = 2;
    final int MAX_DIST = 7;
    Vector2 location = new Vector2(EDGE_OFFSET + R.nextInt(EDGE_OFFSET), EDGE_OFFSET + R.nextInt(EDGE_OFFSET));
    kornerKiller(location, Cardinal.kW);
    final int startingX = (int)location.x;
    for (Cardinal D = Cardinal.kN; /*noop*/; D = D.next90()) { // Direction
      final int end = (D == Cardinal.kW ? startingX : EDGE_OFFSET + R.nextInt(EDGE_OFFSET));
      boolean reachedEnd = false;
      Edge direction = Edge.kOUT;
      while (!reachedEnd) {
        int duration = MIN_DIST + R.nextInt(MAX_DIST);
        for (int step = 0; step < duration; ++step) {
          reachedEnd = increaseStep(location, D, end);
          if (reachedEnd) break;
          boolean reachedEdge = krinkle(location, D, direction, MAX_INCURSION);
          if (reachedEdge && direction == Edge.kIN) direction = Edge.kOUT;
          else if (reachedEdge && direction == Edge.kOUT) direction = Edge.kIN;
          setToVoid(location, D);
        }
        direction = Edge.random();
      }
      kornerKiller(location, D);
      if (D == Cardinal.kW) break; // Done
    }
    return true;
  }

  final Map<Cardinal, Tile> collateNeighbours(int x, int y) {
    Map<Cardinal, Tile> map = new EnumMap<Cardinal, Tile>(Cardinal.class);
    map.put(Cardinal.kN, tiles[x][y+1]);
    map.put(Cardinal.kNE, tiles[x+1][y+1]);
    map.put(Cardinal.kE, tiles[x+1][y]);
    map.put(Cardinal.kSE, tiles[x+1][y-1]);
    map.put(Cardinal.kS, tiles[x][y-1]);
    map.put(Cardinal.kSW, tiles[x-1][y-1]);
    map.put(Cardinal.kW, tiles[x-1][y]);
    map.put(Cardinal.kNW, tiles[x-1][y+1]);
    return map;
  }

  void applyTileGraphics() {
    // Outer are forced to be void
    // Do these first so we don't try and access outside of the tilespace
    for (int x = 0; x < Param.TILES_X; ++x) {
      tiles[x][0].setTexture( TileType.getTextureString(TileType.kGROUND, Colour.kBLACK), 1 );
      tiles[x][Param.TILES_Y-1].setTexture( TileType.getTextureString(TileType.kGROUND, Colour.kBLACK), 1 );
    }
    for (int y = 0; y < Param.TILES_Y; ++y) {
      tiles[0][y].setTexture( TileType.getTextureString(TileType.kGROUND, Colour.kBLACK), 1 );
      tiles[Param.TILES_X-1][y].setTexture( TileType.getTextureString(TileType.kGROUND, Colour.kBLACK), 1 );
    }
    // Set the rest
    for (int x = 1; x < Param.TILES_X-1; ++x) {
      for (int y = 1; y < Param.TILES_Y-1; ++y) {
        tiles[x][y].setTexture( TileType.getTextureString(tiles[x][y], collateNeighbours(x,y)), 1 );
      }
    }
  }


}
