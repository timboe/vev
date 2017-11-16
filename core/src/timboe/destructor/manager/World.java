package timboe.destructor.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import timboe.destructor.Param;
import timboe.destructor.Util;
import timboe.destructor.entity.Tile;
import timboe.destructor.entity.Zone;
import timboe.destructor.enums.*;

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
  Zone[][] zones;

  private World() {
    generate();
  }

  public void reset() {
    GameState.getInstance().reset();
    tiles = new Tile[Param.TILES_X][Param.TILES_Y];
    for (int x = 0; x < Param.TILES_X; ++x) {
      for (int y = 0; y < Param.TILES_Y; ++y) {
        tiles[x][y] = new Tile(x,y);
        //tiles[x][y].setDebug(true);
        GameState.getInstance().getStage().addActor(tiles[x][y]);
      }
    }
    zones = new Zone[Param.ZONES_X][Param.ZONES_Y];
    for (int x = 0; x < Param.ZONES_X; ++x) {
      for (int y = 0; y < Param.ZONES_Y; ++y) {
        zones[x][y] = new Zone(x,y);
        GameState.getInstance().getStage().addActor(tiles[x][y]);
      }
    }
  }

  public void generate() {
    boolean success = false;
    final Vector2 start = new Vector2(0,0);
    final Vector2 end = new Vector2(Param.TILES_X, Param.TILES_Y);
    while (!success) {
      reset();
      success = doZones();
      if (success) success = doEdges(start, end, WorldPass.kSET_TO_VOID);
      //if (success) success = doGreenZones();
    }
    applyTileGraphics();
  }

  private boolean doZones() {
    int nGreen = 0;
    int nHill = 0;
    int nGreenHill = 0;
    for (int x = 0; x < Param.ZONES_X; ++x) {
      for (int y = 0; y < Param.ZONES_Y; ++y) {
        zones[x][y].colour = (R.nextBoolean() == true ? Colour.kGREEN : Colour.kRED);
        zones[x][y].hill = R.nextBoolean();
        if (zones[x][y].colour == Colour.kGREEN) ++nGreen;
        if (zones[x][y].hill) ++nHill;
        if (zones[x][y].colour == Colour.kGREEN && zones[x][y].hill) ++nGreenHill;
      }
    }
    return (nGreen >= Param.MIN_GREEN_ZONE);
  }

  private boolean increaseStep(Vector2 v, Cardinal D, int endOffset, Vector2 start, Vector2 end) {
    switch (D) {
      case kN: return (++v.y == end.y - endOffset);
      case kE: return (++v.x == end.x - endOffset);
      case kS: return (--v.y == start.y + endOffset);
      case kW: return (--v.x == start.x + endOffset);
      default: Gdx.app.error("increaseStep","Unknown direction");
    }
    return false;
  }

  private boolean krinkle(Vector2 v, final Cardinal D, final Edge direction, final int maxIncursion, final Vector2 start, final Vector2 end) {
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
    if (D == Cardinal.kN && Util.needsClamp((int)v.x, start.x + 1, maxIncursion)) {
      v.x = (int)Util.clamp((int)v.x, start.x + 1, maxIncursion);
      return true;
    } else if (D == Cardinal.kS && Util.needsClamp((int)v.x, end.x - maxIncursion, end.x - 1)) {
      v.x = (int) Util.clamp((int) v.x, end.x - maxIncursion, end.x - 1);
      return true;
    } else if (D == Cardinal.kW && Util.needsClamp((int)v.y, start.y, maxIncursion)) {
      v.y = (int)Util.clamp((int)v.y, start.y, maxIncursion);
      return true;
    } else if (D == Cardinal.kE && Util.needsClamp((int)v.y, end.y - maxIncursion, end.y - 1)) {
      v.y = (int) Util.clamp((int) v.y, end.y - maxIncursion, end.y - 1);
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

  private void kornerKiller(Vector2 v, Cardinal D, Colour from, Colour to) {
    final int level = (to == Colour.kBLACK ? -1 : 1);
    switch (D) {
      case kN:
        for (int x = (int)v.x; x >= 0; --x) {
          for (int y = (int)v.y; y < Param.TILES_Y; ++y)
            if (tiles[x][y].colour == from) tiles[x][y].setType(TileType.kGROUND, to, level);
        }
        break;
      case kE:
        for (int x = (int)v.x; x < Param.TILES_X; ++x) {
          for (int y = (int)v.y; y < Param.TILES_Y; ++y)
            if (tiles[x][y].colour == from) tiles[x][y].setType(TileType.kGROUND, to, level);
        }
        break;
      case kS:
        for (int x = (int)v.x; x < Param.TILES_X; ++x) {
          for (int y = (int)v.y; y >= 0; --y)
            if (tiles[x][y].colour == from) tiles[x][y].setType(TileType.kGROUND, to, level);
        }
        break;
      case kW:
        for (int x = (int)v.x; x >= 0; --x) {
          for (int y = (int)v.y; y >= 0; --y)
            if (tiles[x][y].colour == from) tiles[x][y].setType(TileType.kGROUND, to, level);
        }
        break;
      default: Gdx.app.error("kornerKiller","Unknown direction");
    }
  }

  private boolean doGreenZones() {
    for (int x = 0; x < Param.ZONES_X; ++x) {
      for (int y = 0; y < Param.ZONES_Y; ++y) {
        if (zones[x][y].colour == Colour.kRED) continue;
        Gdx.app.log("doGreenZone","Start " + zones[x][y].lowerLeft.toString() + " end " + zones[x][y].upperRight.toString() );
        boolean success = doEdges(zones[x][y].lowerLeft, zones[x][y].upperRight, WorldPass.kSET_GREEN);
        if (!success) return false;
      }
    }
    return true;
  }

  private boolean doEdges(final Vector2 start, final Vector2 end, final WorldPass pass) {
    final int EDGE_OFFSET = 5; // Starting point between EDGE_OFFSET and 2*EDGE_OFFSET in X and Y
    final int MAX_INCURSION = EDGE_OFFSET * 5; // Can go no further inland than MAX_INCURSION
    final int MIN_DIST = 2; // Minimum number of steps to do for a feature
    final int MAX_DIST = 7; // Maximum number of steps to do for a feature is MIN_DIST + MAX_DIST
    final int startingXOffset = EDGE_OFFSET + R.nextInt(EDGE_OFFSET);
    Vector2 location = new Vector2(start.x + startingXOffset, start.y + EDGE_OFFSET + R.nextInt(EDGE_OFFSET));
    if (pass == WorldPass.kSET_TO_VOID) kornerKiller(location, Cardinal.kW, Colour.kRED, Colour.kBLACK);

    for (Cardinal D = Cardinal.kN; /*noop*/; D = D.next90()) { // Direction
      final int endOffset = (D == Cardinal.kW ? startingXOffset : EDGE_OFFSET + R.nextInt(EDGE_OFFSET));
      boolean reachedEnd = false;
      Edge direction = Edge.kOUT;
      while (!reachedEnd) {
        int duration = MIN_DIST + R.nextInt(MAX_DIST);
        for (int step = 0; step < duration; ++step) {
          reachedEnd = increaseStep(location, D, endOffset, start, end);
          if (reachedEnd) break;
          boolean reachedEdge = krinkle(location, D, direction, MAX_INCURSION, start, end);
          if (reachedEdge && direction == Edge.kIN) direction = Edge.kOUT;
          else if (reachedEdge && direction == Edge.kOUT) direction = Edge.kIN;
          if (pass == WorldPass.kSET_TO_VOID) {
//            setToVoid(location, D);
            tiles[(int)location.x][(int)location.y].setType(TileType.kGROUND, Colour.kRED, 1);
          } else if (pass == WorldPass.kSET_GREEN) {
            tiles[(int)location.x][(int)location.y].setType(TileType.kGROUND, Colour.kGREEN, 1);
          }
        }
        direction = Edge.random();
      }

      if (pass == WorldPass.kSET_TO_VOID) {
        kornerKiller(location, D, Colour.kRED, Colour.kBLACK); // A second pass of the korner killer to round off the shape
      } else if (pass == WorldPass.kSET_GREEN) {
        shapeFiller(start, end, Colour.kGREEN);  // We have a hollow shape, need to fill it
      }


      if (D == Cardinal.kW) break; // Done
    }
    return true;
  }

  private void shapeFiller(final Vector2 start, final Vector2 end, Colour paint) {
    boolean painting;
    for (int x = (int)start.x; x < (int)end.x; ++x) {
      painting = false;
      for (int y = (int) start.y; y < (int) end.y; ++y) {
        if (painting) tiles[x][y].colour = paint;
        // Look at current and next square to decide if to start painting
        if (y + 1 >= Param.TILES_Y) break;
        if (tiles[x][y].colour != tiles[x][y + 1].colour) painting = !painting;
      }
    }
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
