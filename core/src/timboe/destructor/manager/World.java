package timboe.destructor.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import timboe.destructor.Param;
import timboe.destructor.Util;
import timboe.destructor.entity.Tile;
import timboe.destructor.enums.Edge;

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
        tiles[x][y].setTexture("floor_r_" + Util.rndInt( Param.N_GRASS_TILES ).toString(), 1);
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
  }

  private boolean increaseStep(Vector2 v, int D, int end) {
    switch (D) {
      case 0: return (++v.y == Param.TILES_Y - end);
      case 1: return (++v.x == Param.TILES_X - end);
      case 2: return (--v.y == end);
      case 3: return (--v.x == end);
      default: Gdx.app.error("increaseStep","Unknown direction: " + Integer.toString(D));
    }
    return false;
  }

  private boolean krinkle(Vector2 v, int D, Edge direction, int maxIncursion) {
    final int MAX_KRINKLE = 3; // Must be ODD
    int dist = R.nextInt(MAX_KRINKLE);
    switch (direction) {
      case kFLAT: dist -= Math.floor(MAX_KRINKLE/2.); break;
      case kOUT: dist *= -1; break;
      case kIN: break;
      default: Gdx.app.error("krinkle","Unknown enum");
    }
    switch (D) {
      case 0: v.x += dist; break;
      case 1: v.y -= dist; break;
      case 2: v.x -= dist; break;
      case 3: v.y += dist; break;
      default: Gdx.app.error("krinkle","Unknown direction: " + Integer.toString(D));
    }
    if (D == 0 && Util.needsClamp((int)v.x, 1, maxIncursion)) {
      v.x = (int)Util.clamp((int)v.x, 1, maxIncursion);
      return true;
    } else if (D == 2 && Util.needsClamp((int)v.x, Param.TILES_X - maxIncursion, Param.TILES_X - 1)) {
      v.x = (int) Util.clamp((int) v.x, Param.TILES_X - maxIncursion, Param.TILES_X - 1);
      return true;
    } else if (D == 3 && Util.needsClamp((int)v.y, 1, maxIncursion)) {
      v.y = (int)Util.clamp((int)v.y, 1, maxIncursion);
      return true;
    } else if (D == 1 && Util.needsClamp((int)v.y, Param.TILES_Y - maxIncursion, Param.TILES_Y - 1)) {
      v.y = (int) Util.clamp((int) v.y, Param.TILES_Y - maxIncursion, Param.TILES_Y - 1);
      return true;
    }
    return false;
  }

  private void setToVoid(Vector2 v, int D) {
    switch (D) {
      case 0: for (int x = (int)v.x; x >= 0; --x) tiles[x][(int)v.y].setTexture("b",1);
        break;
      case 1: for (int y = (int)v.y; y < Param.TILES_Y; ++y) tiles[(int)v.x][y].setTexture("b",1);
        break;
      case 2: for (int x = (int)v.x; x < Param.TILES_X; ++x) tiles[x][(int)v.y].setTexture("b",1);
        break;
      case 3: for (int y = (int)v.y; y >= 0; --y) tiles[(int)v.x][y].setTexture("b",1);
        break;
      default: Gdx.app.error("setToVoid","Unknown direction");
    }
  }

  private void kornerKiller(Vector2 v, int D) {
    switch (D) {
      case 0:
        for (int x = (int)v.x; x >= 0; --x) {
          for (int y = (int)v.y; y < Param.TILES_Y; ++y) tiles[x][y].setTexture("b",1);
        }
        break;
      case 1:
        for (int x = (int)v.x; x < Param.TILES_X; ++x) {
          for (int y = (int)v.y; y < Param.TILES_Y; ++y) tiles[x][y].setTexture("b",1);
        }
        break;
      case 2:
        for (int x = (int)v.x; x < Param.TILES_X; ++x) {
          for (int y = (int)v.y; y >= 0; --y) tiles[x][y].setTexture("b",1);
        }
        break;
      case 3:
        for (int x = (int)v.x; x >= 0; --x) {
          for (int y = (int)v.y; y >= 0; --y) tiles[x][y].setTexture("b",1);
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
    kornerKiller(location, 3);
    final int startingX = (int)location.x;
    for (int D = 0; D < 4; ++D) { // Direction
      final int end = (D == 3 ? startingX : EDGE_OFFSET + R.nextInt(EDGE_OFFSET));
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
    }
    return true;
  }


}
