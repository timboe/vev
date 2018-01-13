package timboe.destructor.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.scenes.scene2d.Actor;

import timboe.destructor.Pair;
import timboe.destructor.Param;
import timboe.destructor.Util;
import timboe.destructor.entity.Entity;
import timboe.destructor.entity.Sprite;
import timboe.destructor.entity.Tile;
import timboe.destructor.entity.Warp;
import timboe.destructor.entity.Zone;
import timboe.destructor.enums.Cardinal;
import timboe.destructor.enums.Colour;
import timboe.destructor.enums.Edge;
import timboe.destructor.enums.TileType;
import timboe.destructor.pathfinding.IVector2;

import java.util.*;

public class World {

  private static World ourInstance;

  public static World getInstance() {
    return ourInstance;
  }

  public static void create() {
    ourInstance = new World();
  }

  public void dispose() {
    ourInstance = null;
  }

  private final Random R = new Random();
  private final Vector<Zone> allZones = new Vector<Zone>();
  public final Map<Warp, ParticleEffect> warps = new HashMap<Warp, ParticleEffect>();
  public final Vector<IVector2> tiberium = new Vector<IVector2>();
  public final Vector<ParticleEffect> warpClouds = new Vector<ParticleEffect>();
  private boolean generated = false;
  private int stage;
  private Vector<IVector2> worldEdges = new Vector<IVector2>();

  private Tile[][] tiles;
  private Zone[][] zones;

  private World() {
    worldEdges.add(new IVector2(0, 0));
    worldEdges.add(new IVector2(0, Param.TILES_Y));
    worldEdges.add(new IVector2(Param.TILES_X, Param.TILES_Y));
    worldEdges.add(new IVector2(Param.TILES_X, 0));
    worldEdges.add(new IVector2(0, 0));

    if (Param.KRINKLE_OFFSET <= Param.KRINKLE_GAP) {
      Gdx.app.error("generate", "Kinnkle offset " + Param.KRINKLE_OFFSET + " less than gap " + Param.KRINKLE_GAP);
      System.exit(0);
    }

    reset();
  }

  public boolean getGenerated() {
    return generated;
  }

  public Tile getTile(float x, float y) {
    return getTile((int) x, (int) y);
  }

  public Tile getTile(int x, int y) {
    // Removed bounds check for speed....
//    if (x < 0 || y < 0 || x >= Param.TILES_X-1 || y >= Param.TILES_Y-1) return null;
    return tiles[x][y];
  }

  private void reset() {
    GameState.getInstance().reset();
    stage = 0;
    generated = false;
    warps.clear();
    warpClouds.clear();
    tiberium.clear();
    tiles = new Tile[Param.TILES_X][Param.TILES_Y];
    for (int x = 0; x < Param.TILES_X; ++x) {
      for (int y = 0; y < Param.TILES_Y; ++y) {
        tiles[x][y] = new Tile(x, y);
        GameState.getInstance().getTileStage().addActor(tiles[x][y]);
      }
    }
    setNeighbours();
    zones = new Zone[Param.ZONES_X][Param.ZONES_Y];
    allZones.clear();
    for (int x = 0; x < Param.ZONES_X; ++x) {
      for (int y = 0; y < Param.ZONES_Y; ++y) {
        zones[x][y] = new Zone(x, y);
        allZones.add(zones[x][y]);
        GameState.getInstance().getTileStage().addActor(zones[x][y]);
      }
    }
  }

  public void act(float delta) {
    generate();
  }

  public void generate() {
    boolean success = false;
    switch (stage) {
      case 0: success = doZones(); break;
      case 1: success = doEdges(worldEdges, allZones, Colour.kBLACK, Colour.kRED, 0, 1, Param.KRINKLE_OFFSET, (2 * Param.KRINKLE_OFFSET) - Param.KRINKLE_GAP); break;
      case 2: success = doSpecialZones(true); break;
      case 3: success = removeGreenStubs(); break;
      case 4: success = doSpecialZones(false); break;
      case 5: success = removeEWSingleStairs(); break;
      case 6: success = addWarp(); break;
      case 7: success = markCliffs(); break;
      case 8: success = addPatch(false, Param.TIBERIUM_SIZE, Param.MIN_TIBERIUM_PATCH, Param.MAX_TIBERIUM_PATCH); break;
      case 9: success = addPatch(true, Param.FOREST_SIZE, Param.MIN_FORESTS, Param.MAX_FORESTS); break;
      case 10: success = addFoliage(); break;
      case 11: success = doPathGrid(); break;
      case 12: success = finaliseWarp(); break;
      case 13: success = applyTileGraphics(); break;
    }

    if (success || (stage == 13 && Param.ALLOW_TILING_ERRORS)) {
      ++stage;
    } else {
      reset();
    }

    if (stage == 14) {
      UI.getInstance().reset();
      GameState.getInstance().getGameScreen().setMultiplexerInputs();
      Gdx.app.log("World", "Generation finished");
      generated = true;
      Sounds.getInstance().doMusic();
      for (int y = Param.ZONES_Y - 1; y >= 0; --y)
        Gdx.app.log("", (zones[0][y].tileColour == Colour.kRED ? "R " : "G ") + (zones[1][y].tileColour == Colour.kRED ? "R " : "G ") + (zones[2][y].tileColour == Colour.kRED ? "R " : "G "));
      for (int y = Param.ZONES_Y - 1; y >= 0; --y)
        Gdx.app.log("", zones[0][y].level + " " + zones[1][y].level + " " + zones[2][y].level);
    }
  }

  private boolean shouldLink(Tile A, Tile B) {
    if (A.tileColour == Colour.kBLACK || B.tileColour == Colour.kBLACK) return false;
    if (A.type == TileType.kFOILAGE || B.type == TileType.kFOILAGE) return false;
    if (A.type == TileType.kCLIFF || B.type == TileType.kCLIFF) return false;
    if (A.type == TileType.kBUILDING || B.type == TileType.kBUILDING) return false;
    if (A.type == TileType.kQUEUE || B.type == TileType.kQUEUE) return false;
    if (A.type == TileType.kSTAIRS && B.type == TileType.kSTAIRS) return true;
    if (B.type == TileType.kSTAIRS)
      return shouldLink(B, A); // Do it from the perspective of the stairs
    if (A.type == TileType.kSTAIRS) {
      if (A.direction == Cardinal.kN || A.direction == Cardinal.kS) { // I'm N-S stairs
        if (B.y > A.y) return (A.n8.get(Cardinal.kN).type == TileType.kSTAIRS);
        else if (B.y < A.y) return (A.n8.get(Cardinal.kS).type == TileType.kSTAIRS);
      } else { // E-W stairs
        if (B.x > A.x) return (A.n8.get(Cardinal.kE).type == TileType.kSTAIRS);
        else if (B.x < A.x) return (A.n8.get(Cardinal.kW).type == TileType.kSTAIRS);
      }
      return true;
    }
    return (A.level == B.level);
  }

  public void updateTilePathfinding(Tile t) {
    updateTilePathfinding(t.coordinates.x, t.coordinates.y);
  }

  private void updateTilePathfinding(int x, int y) {
    tiles[x][y].pathFindDebug.clear();
    tiles[x][y].pathFindNeighbours.clear();
    for (Cardinal D : Cardinal.n8) {
      if (shouldLink(tiles[x][y], tiles[x][y].n8.get(D))) {
        tiles[x][y].pathFindDebug.add(D);
        tiles[x][y].pathFindNeighbours.add(tiles[x][y].n8.get(D));
      }
    }
  }

  private boolean doPathGrid() {
    for (int x = 1; x < Param.TILES_X - 1; ++x) {
      for (int y = 1; y < Param.TILES_Y - 1; ++y) {
        updateTilePathfinding(x, y);
      }
    }
    return true;
  }

  private String randomFoliage(Colour c) {
    if (R.nextFloat() < Param.TREE_PROB)
      return "tree_" + c.getString() + "_" + R.nextInt(Param.N_TREE);
    return "bush_" + c.getString() + "_" + R.nextInt(Param.N_BUSH);
  }

  private Sprite newSprite(int x, int y, String name, boolean isFoliage) {
    Sprite s = new Sprite(tiles[x][y]);
    if (isFoliage) GameState.getInstance().getFoliageStage().addActor(s);
    else GameState.getInstance().getSpriteStage().addActor(s);
    s.setTexture(name, 1, R.nextBoolean());
    return s;
  }

  private boolean finaliseWarp() {
    for (Actor a : GameState.getInstance().getWarpStage().getActors()) {
      ((Warp) a).updatePathingStartPoint();
    }
    return true;
  }


  private boolean addWarp() {
    int fTry = 0, fPlaced = 0;
    do {
      final int _x = Param.WARP_SIZE + R.nextInt(Param.TILES_X - Param.WARP_SIZE * 2); // Twice as far from edge
      final int _y = Param.WARP_SIZE + R.nextInt(Param.TILES_Y - Param.WARP_SIZE * 2);
      final int level = tiles[_x][_y].level;
      boolean tooClose = false; // Check whole area is clear
      for (int x = _x - Param.WARP_SIZE / 2 - Param.KRINKLE_GAP; x < _x + Param.WARP_SIZE / 2 + Param.KRINKLE_GAP; ++x) {
        for (int y = _y - Param.WARP_SIZE / 2 - Param.KRINKLE_GAP; y < _y + Param.WARP_SIZE / 2 + Param.KRINKLE_GAP; ++y) {
          if (Param.WARP_SIZE / 2 + Param.KRINKLE_GAP <= Math.hypot(x - _x, y - _y)) continue;
          if (tiles[x][y].type != TileType.kGROUND
              || tiles[x][y].tileColour != Colour.kRED
              || tiles[x][y].level != level) tooClose = true;
        }
      }
      for (Zone z : allZones) {
        if (z.inZone(_x, _y)) {
          if (z.hasWarp) tooClose = true;
          else z.hasWarp = true;
          break;
        }
      }
      if (tooClose) continue;
      // Apply the area
      ++fPlaced;
      if (GameState.getInstance().debug > 0) Gdx.app.log("addWarp", "Adding WARP at (" + _x + "," + _y + ")");
      for (int x = _x - Param.WARP_SIZE / 2; x < _x + Param.WARP_SIZE / 2; ++x) {
        for (int y = _y - Param.WARP_SIZE / 2; y < _y + Param.WARP_SIZE / 2; ++y) {
          if (Param.WARP_SIZE / 2 <= Math.hypot(x - _x, y - _y)) continue;
          tiles[x][y].level = level - 1;
          tiles[x][y].tileColour = Colour.kBLACK;
        }
      }

      Warp w = new Warp(tiles[_x][_y]);
      GameState.getInstance().getWarpStage().addActor(w);
      GameState.getInstance().buildingSet.add(w);

      ParticleEffect clouds = new ParticleEffect();
      clouds.load(Gdx.files.internal("hell_portal_effect.txt"), Textures.getInstance().getAtlas());
      clouds.setPosition(Param.TILE_S * _x + Param.TILE_S / 2, Param.TILE_S * _y);
      clouds.start();
      warpClouds.add(clouds);

      ParticleEffect zap = new ParticleEffect();
      zap.load(Gdx.files.internal("lightning_effect.txt"), Textures.getInstance().getAtlas());
      zap.setPosition(Param.TILE_S * _x + Param.TILE_S / 2, Param.TILE_S * _y);
      zap.allowCompletion();

      warps.put(w, zap);

    } while (++fTry < Param.N_PATCH_TRIES && fPlaced < Param.MAX_WARP);
    if (fPlaced < Param.MIN_WARP) {
      Gdx.app.error("addPatch", "Could not add warp. N:" + fPlaced);
      return false;
    }
    return true;
  }

  private boolean tryPatchOfStuff(final int _x, final int _y, final boolean isForest, final int patchSize) { // Otherwise, is Tiberium
    for (int x = _x - patchSize; x < _x + patchSize; ++x) {
      if (tiles[x][_y].type != TileType.kGROUND || tiles[x][_y].tileColour != tiles[_x][_y].tileColour)
        return false;
    }
    for (int y = _y - patchSize; y < _y + patchSize; ++y) {
      if (tiles[_x][y].type != TileType.kGROUND || tiles[_x][y].tileColour != tiles[_x][_y].tileColour)
        return false;
    }
    if (!isForest) { // One tiberium patch per zone
      for (Zone z : allZones) {
        if (z.inZone(_x, _y)) {
          if (z.hasTiberium) return false;
          z.hasTiberium = true;
          break;
        }
      }
    }
    final String forestTexture = "tree_" + tiles[_x][_y].tileColour.getString() + "_" + R.nextInt(Param.N_TREE);
    final double maxD = Math.sqrt(2 * Math.pow(patchSize, 2));
    for (int x = _x - patchSize; x < _x + patchSize; ++x) {
      for (int y = _y + patchSize - 1; y >= _y - patchSize; --y) {
        if (tiles[x][y].tileColour != tiles[_x][_y].tileColour || tiles[x][y].type != TileType.kGROUND)
          continue;
        final double d = Math.hypot(x - _x, y - _y);
        if (isForest) tryTree(d, maxD, x, y, forestTexture);
        else tryTiberium(d, maxD, x, y);
      }
    }
    return true;
  }

  private void tryTree(final double distance, final double maxDistance, final int x, final int y, final String forestTexture) {
    if (distance > Math.abs(R.nextGaussian() * Param.PATCH_DENSITY * maxDistance)) return;
    Sprite s = newSprite(x, y, forestTexture, true);
    s.moveBy((-Param.WIGGLE) + Util.R.nextInt(Param.WIGGLE * 2), (-Param.WIGGLE) + Util.R.nextInt(Param.WIGGLE * 2));
    tiles[x][y].type = TileType.kFOILAGE;
    tiles[x][y].mySprite = s;
  }

  private void tryTiberium(final double distance, final double maxDistance, final int x, final int y) {
    for (int subX = 0; subX < 2; ++subX) {
      for (int subY = 0; subY < 2; ++subY) {
        if (distance > Math.abs(R.nextGaussian() * Param.PATCH_DENSITY * maxDistance)) continue;
        Sprite s = newSprite(x, y, "tiberium_" + R.nextInt(Param.N_TIBERIUM), false);
        s.moveBy(subX * Param.TILE_S, subY * Param.TILE_S);
        s.moveBy((-Param.WIGGLE / 2) + Util.R.nextInt(Param.WIGGLE), (-Param.WIGGLE / 2) + Util.R.nextInt(Param.WIGGLE));
      }
    }
  }

  private boolean addPatch(final boolean isForest, final int patchSize, final int min, final int max) { // Otherwise, is tiberium
    int fTry = 0, fPlaced = 0;
    do {
      final int x = patchSize + R.nextInt(Param.TILES_X - (2 * patchSize));
      final int y = patchSize + R.nextInt(Param.TILES_Y - (2 * patchSize));
      if (tiles[x][y].type != TileType.kGROUND || tiles[x][y].tileColour != (isForest ? Colour.kGREEN : Colour.kRED))
        continue;
      if (tryPatchOfStuff(x, y, isForest, patchSize)) {
        ++fPlaced;
        if (!isForest) tiberium.add(new IVector2(x, y));
      }
    } while (++fTry < Param.N_PATCH_TRIES && fPlaced < max);
    if (fPlaced < min) {
      Gdx.app.error("addPatch", "Could not add enough. " + (isForest ? "forest" : "tiberium") + ", placed:" + fPlaced);
      return false;
    }
    return true;
  }

  private boolean addFoliage() {
    for (int x = 1; x < Param.TILES_X - 1; ++x) {
      for (int y = Param.TILES_Y - 1; y >= 0; --y) {
        if (tiles[x][y].type != TileType.kGROUND || tiles[x][y].tileColour == Colour.kBLACK)
          continue;
        if (R.nextFloat() < Param.FOLIAGE_PROB) {
          Sprite s = newSprite(x, y, randomFoliage(tiles[x][y].tileColour), true);
          tiles[x][y].type = TileType.kFOILAGE;
          tiles[x][y].mySprite = s;
//        } else if (tiles[x][y].tileColour == Colour.kGREEN && R.nextFloat() < 0.01) {
//          Tile s = new Tile(x,y);
//          GameState.getInstance().getStage().addActor(s);
//          s.setTexture("building_" + R.nextInt(5), 1);
        }
      }
    }
    return true;
  }

  private boolean markCliffs() {
    for (int x = 1; x < Param.TILES_X - 1; ++x) {
      for (int y = 1; y < Param.TILES_Y - 1; ++y) {
        if (tiles[x][y].type != TileType.kGROUND) continue;
        if (tiles[x][y].n8.get(Cardinal.kN).level > tiles[x][y].level && tiles[x][y].n8.get(Cardinal.kN).type != TileType.kSTAIRS) {
          tiles[x][y].type = TileType.kCLIFF;
        } else {
          for (Cardinal D : Cardinal.NESW) {
            if (tiles[x][y].n8.get(D).level < tiles[x][y].level) {
              tiles[x][y].type = TileType.kCLIFF_EDGE;
              break;
            }
          }
          for (Cardinal D : Cardinal.n8) {
            if (tiles[x][y].tileColour == Colour.kGREEN && tiles[x][y].n8.get(D).tileColour != Colour.kGREEN) {
              tiles[x][y].type = TileType.kGRASS_EDGE;
              break;
            }
          }
        }
      }
    }
    return true;
  }

  private boolean removeGreenStubs() {
    boolean removed;
    do { //Iterative
      removed = false;
      for (int x = 1; x < Param.TILES_X - 1; ++x) {
        for (int y = 1; y < Param.TILES_Y - 1; ++y) {
          if (tiles[x][y].tileColour != Colour.kGREEN) continue;
          int c = 0;
          for (Cardinal D : Cardinal.NESW) if (tiles[x][y].n8.get(D).tileColour == Colour.kRED) ++c;
          if (c >= 3) {
            tiles[x][y].tileColour = Colour.kRED;
            removed = true;
          }
        }
      }
    } while (removed);
    return true;
  }

  // The way stairs work, we cannot have a single stair tile in the E-W orientation
  private boolean removeEWSingleStairs() {
    for (int x = 1; x < Param.TILES_X - 1; ++x) {
      for (int y = 1; y < Param.TILES_Y - 1; ++y) {
        if (tiles[x][y].type != TileType.kSTAIRS) continue;
        if (tiles[x][y].direction == Cardinal.kE || tiles[x][y].direction == Cardinal.kW)
          continue; // Only applies to E-W
        int count = 0;
        for (Cardinal D : Cardinal.NESW)
          if (tiles[x][y].n8.get(D).type == TileType.kSTAIRS) ++count;
        if (count > 0) continue;
        setGround(new IVector2(x, y), tiles[x][y].tileColour, tiles[x][y].level, Edge.kFLAT);
        if (GameState.getInstance().debug > 0)
          Gdx.app.log("removeEWSingleStairs", "Removed single stair at " + x + "," + y);
      }
    }
    return true;
  }

  private boolean doZones() {
    final int nGreen = Param.MIN_GREEN_ZONE + R.nextInt(Param.MAX_GREEN_ZONE - Param.MIN_GREEN_ZONE + 1);
    final int nGreenHill = Param.MIN_GREEN_HILL + R.nextInt(nGreen - Param.MIN_GREEN_HILL + 1);
    final int nRed = (Param.ZONES_X * Param.ZONES_Y) - nGreen;
    final int nRedHill = Param.MIN_RED_HILL + R.nextInt(nRed - Param.MIN_RED_HILL + 1);
    if (nGreenHill > nGreen || nRedHill > nRed) {
      Gdx.app.error("doZones", "Logic issue, more coloured zones than types of that tileColour");
      return false;
    }

    int fPlacedGreen = 0, fPlacedGreenHill = 0, fTry = 0;
    do {
      final int x = R.nextInt(Param.ZONES_X);
      final int y = R.nextInt(Param.ZONES_Y);
      if (zones[x][y].tileColour == Colour.kGREEN) continue;
      zones[x][y].tileColour = Colour.kGREEN;
      ++fPlacedGreen;
      if (fPlacedGreenHill >= nGreenHill) continue;
      zones[x][y].level++;
      ++fPlacedGreenHill;
      if (R.nextFloat() >= Param.HILL_IN_HILL_PROB) continue;
      zones[x][y].hillWithinHill = true;
    } while (++fTry < Param.N_PATCH_TRIES && fPlacedGreen < nGreen);
    if (fPlacedGreen < nGreen) {
      Gdx.app.error("doZones", "Could not place green zones");
      return false;
    }

    int fPlacedRedHill = 0;
    fTry = 0;
    do {
      final int x = R.nextInt(Param.ZONES_X);
      final int y = R.nextInt(Param.ZONES_Y);
      if (zones[x][y].tileColour == Colour.kGREEN || zones[x][y].level != 1) continue;
      zones[x][y].level--;
      ++fPlacedRedHill;
      if (R.nextFloat() >= Param.HILL_IN_HILL_PROB) continue;
      zones[x][y].hillWithinHill = true;
    } while (++fTry < Param.N_PATCH_TRIES && fPlacedRedHill < nRedHill);
    if (fPlacedRedHill < nRedHill) {
      Gdx.app.error("doZones", "Could not place red hills");
      return false;
    }
    return true;
  }

  private void increaseStep(IVector2 v, Cardinal D, Colour c, int level) {
    switch (D) {
      case kN:
        ++v.y;
        break;
      case kE:
        ++v.x;
        break;
      case kS:
        --v.y;
        break;
      case kW:
        --v.x;
        break;
      default:
        Gdx.app.error("increaseStep", "Unknown direction");
    }
    if (Util.inBounds(v)) setGround(v, c, level, Edge.kFLAT);
  }

  private int distanceToDestination(final IVector2 v, final Cardinal D, final int endOffset,
                                    final IVector2 destination, final boolean rightTurnNext) {
    final int flip = (rightTurnNext ? 1 : -1);
    switch (D) {
      case kN:
        return Math.abs(v.y - (destination.y - (endOffset * flip)));
      case kE:
        return Math.abs(v.x - (destination.x - (endOffset * flip)));
      case kS:
        return Math.abs(v.y - (destination.y + (endOffset * flip)));
      case kW:
        return Math.abs(v.x - (destination.x + (endOffset * flip)));
      default:
        Gdx.app.error("increaseStep", "Unknown direction");
    }
    return 0;
  }

  private boolean notSameColourAndLevel(final IVector2 start, final int offX, final int offY,
                                        final Colour fromC, final Colour toC,
                                        final int fromLevel, final int toLevel) {
    //if (true) return true;
    IVector2 test = new IVector2(start.x + offX, start.y + offY);
    return (test.x >= 0 && test.y >= 0 && test.x < Param.TILES_X && test.y < Param.TILES_Y
        && ((tiles[test.x][test.y].tileColour != fromC || tiles[test.x][test.y].level != fromLevel)
        && (tiles[test.x][test.y].tileColour != toC || tiles[test.x][test.y].level != toLevel)));
  }

  private Edge krinkle(IVector2 v, final Cardinal D, Edge direction, final int distanceToDest,
                       final int maxIncursion, final IVector2 destination,
                       final Colour fromC, final Colour toC,
                       final int fromLevel, final int toLevel) {
    int dist = R.nextInt(Param.MAX_KRINKLE);
    switch (direction) {
      case kFLAT:
        dist -= Math.floor(Param.MAX_KRINKLE / 2.);
        break;
      case kOUT:
        dist *= -1;
        break;
      case kIN:
        break;
      case kSTAIRS:
      case kSTAIRS_IN:
      case kSTAIRS_OUT:
        dist = 0;
        break;
      default:
        Gdx.app.error("krinkle", "Unknown enum");
    }
    do {
      int step = 0;
      if (dist > 0) {
        step = +1;
        --dist;
      } else if (dist < 0) {
        step = -1;
        ++dist;
      }

      int inTest = 0, inThreshold = 0; // Proposed krinkle vs. outermost allowed value
      int outTest = 0, outThreshold = 0; // Proposed krinkle vs. innermost allowed value
      int outModX = 0, outModY = 0; // X and Y needed to force us outwards if innermost criteria met
      int outOfBoundX = 0, outOfBoundY = 0; // Distance to check for out-of-bounds due to meeting another krinkle
      int outOfBoundModX = 0, outOfBoundModY = 0; // X and Y needed to force us inwards away from the OOB
      int lookAheadOutOfBoundsX = 0, lookAheadOutOfBoundsY = 0; // Looking ahead to make sure we don't hit a right-angle we cannot krinkle around
      int modX = 0, modY = 0; // The krinkle to apply - if all is good

      boolean testInvert = false;
      switch (D) {
        case kN:
          inTest = v.x + step;
          inThreshold = destination.x + 1;
          outTest = v.x + step;
          outThreshold = destination.x + maxIncursion;
          outModX = -1;
          outOfBoundX = -Param.NEAR_TO_EDGE;
          outOfBoundModX = +1;
          lookAheadOutOfBoundsY = Param.NEAR_TO_EDGE;
          modX = step;
          break;
        case kS:
          inTest = v.x - step;
          inThreshold = destination.x - 2;
          testInvert = true;
          outTest = v.x - step;
          outThreshold = destination.x - maxIncursion;
          outModX = +1;
          outOfBoundX = Param.NEAR_TO_EDGE;
          outOfBoundModX = -1;
          lookAheadOutOfBoundsY = -Param.NEAR_TO_EDGE;
          modX = -step;
          break;
        case kW:
          inTest = v.y + step;
          inThreshold = destination.y + 2; // Space for cliff bottom
          outTest = v.y + step;
          outThreshold = destination.y + maxIncursion;
          outModY = -1;
          outOfBoundY = -(Param.NEAR_TO_EDGE + 1);
          outOfBoundModY = +1; // Space for cliff bottom
          lookAheadOutOfBoundsX = Param.NEAR_TO_EDGE;
          modY = step;
          break;
        case kE:
          inTest = v.y - step;
          inThreshold = destination.y - 2;
          testInvert = true;
          outTest = v.y - step;
          outThreshold = destination.y - maxIncursion;
          outModY = +1;
          outOfBoundY = Param.NEAR_TO_EDGE;
          outOfBoundModY = -1;
          lookAheadOutOfBoundsX = -Param.NEAR_TO_EDGE;
          modY = -step;
          break;
      }

      if (testInvert) {
        int tempA = inTest;
        int tempB = outTest;
        inTest = inThreshold;
        outTest = outThreshold;
        inThreshold = tempA;
        outThreshold = tempB;
      }

      if (inTest < inThreshold) {
        if (GameState.getInstance().debug > 0)
          Gdx.app.log("      krinkle", v.toString() + " Too close to outer edge (" + inTest + " < " + inThreshold + ") -> kIN");
        return Edge.kIN; // Too close to edge of map
      } else if (outTest > outThreshold) { // Too close to centre
        v.x += outModX;
        v.y += outModY;
        setGround(v, toC, toLevel, Edge.kFLAT); // Correct now
        if (GameState.getInstance().debug > 0)
          Gdx.app.log("      krinkle", v.toString() + " Too close to centre -> kOUT");
        return Edge.kOUT;
      } else if (notSameColourAndLevel(v, outOfBoundX, outOfBoundY, fromC, toC, fromLevel, toLevel)
          || (distanceToDest > Param.MAX_DIST && notSameColourAndLevel(v, lookAheadOutOfBoundsX, lookAheadOutOfBoundsY, fromC, toC, fromLevel, toLevel))) {
        for (int i = 0; i < Param.EDGE_ADJUSTMENT; ++i) {
          v.x += outOfBoundModX;
          v.y += outOfBoundModY;
          setGround(v, toC, toLevel, Edge.kFLAT);
        }
        if (direction == Edge.kSTAIRS || direction == Edge.kSTAIRS_IN || direction == Edge.kSTAIRS_OUT)
          direction = Edge.kIN;// Kill any on-going stairs at this point
        if (GameState.getInstance().debug > 0)
          Gdx.app.log("      krinkle", v.toString() + " Too close to other krinkle -> CORRECT");
      } else {
        if (GameState.getInstance().debug > 1)
          Gdx.app.log("      krinkle", v.toString() + " Krinkle by x:" + modX + ", y:" + modY);
        v.x += modX;
        v.y += modY;
      }

      IVector2 v2 = v.clone();
      if (direction == Edge.kSTAIRS && toLevel > fromLevel) { // We are going UPHILL. Need to tweak stair placement
        setGround(v, toC, toLevel, Edge.kFLAT);
        switch (D) {
          case kN:
            v2.x--;
            break;
          case kS:
            v2.x++;
            break;
          case kE:
            v2.y++;
            break;
          case kW:
            v2.y--;
            break;
          default:
            Gdx.app.error("krinkle", "Unknown direction " + D);
        }
      }
      setGround(v2, toC, toLevel, direction);
      tiles[v2.x][v2.y].direction = D;

    } while (dist != 0);
    return direction;
  }


  private void setGround(IVector2 v, Colour c, int level, Edge direction) {
    tiles[v.x][v.y].setType(direction == Edge.kSTAIRS ? TileType.kSTAIRS : TileType.kGROUND, c, level);
  }

  private void collateFloodFill(Vector<Pair<IVector2, IVector2>> edgePairs, Vector<Zone> zone) {
    for (int x = 0; x < Param.ZONES_X; ++x) {
      for (int y = 0; y < Param.ZONES_Y; ++y) {
        if (zones[x][y].mask) {
          zones[x][y].addEdgePairs(edgePairs);
          zones[x][y].merged = true;
          zone.add(zones[x][y]);
        }
        zones[x][y].mask = false;
      }
    }
  }

  private int pairFrequency(final Vector<Pair<IVector2, IVector2>> vec, final Pair<IVector2, IVector2> match) {
    // Insensitive to the direction of the 2-vector formed by the two points
    int n = 0;
    for (Pair<IVector2, IVector2> test : vec) {
      if (test.getKey().equals(match.getKey()) && test.getValue().equals(match.getValue())) ++n;
      else if (test.getKey().equals(match.getValue()) && test.getValue().equals(match.getKey()))
        ++n;
    }
    return n;
  }

  private Vector<IVector2> edgePairsToEdges(Vector<Pair<IVector2, IVector2>> edgePairs) {
    Vector<Pair<IVector2, IVector2>> edgePairsReduced = new Vector<Pair<IVector2, IVector2>>();
    for (Pair<IVector2, IVector2> e : edgePairs) { // Remove duplicates
      if (pairFrequency(edgePairs, e) == 1) edgePairsReduced.add(e);
    }

    // Trace the worldEdges
    Vector<IVector2> edges = new Vector<IVector2>();
    edges.add(edgePairsReduced.firstElement().getKey()); // Add starting location
    int infLoopProtection = 0;
    do { // Now get back to it
      IVector2 currentLocation = edges.lastElement();
      for (Pair<IVector2, IVector2> find : edgePairsReduced) {
        if (find.getKey().equals(currentLocation)) {
          edges.add(find.getValue()); // Follow the vector
          break;
        }
      }
      if (++infLoopProtection > 1e3) {
        Gdx.app.error("edgePairsToEdges", "Encounterd non-cyclic pattern");
        for (Pair<IVector2, IVector2> find : edgePairsReduced) {
          Gdx.app.log("edgePairsToEdges", find.getKey().toString() + " - > " + find.getValue().toString());
        }
        return null;
      }
    } while (!edges.firstElement().equals(edges.lastElement()));

    // Remove un-needed elements
    Vector<IVector2> edgesReduced = new Vector<IVector2>();
    edgesReduced.add(edges.firstElement());
    for (int i = 1; i < edges.size() - 1; ++i) {
      // Find the direction
      if (edges.elementAt(i - 1).x == edges.elementAt(i).x) { // Same x, therefore in y
        if (edges.elementAt(i + 1).x != edges.elementAt(i).x) edgesReduced.add(edges.elementAt(i));
      } else { // Same y, therefore in x
        if (edges.elementAt(i + 1).y != edges.elementAt(i).y) edgesReduced.add(edges.elementAt(i));
      }
    }
    edgesReduced.add(edges.lastElement());
    return edgesReduced;
  }


  private boolean doSpecialZones(final boolean doGreenZones) {
    final boolean doHills = !doGreenZones;

    for (int x = 0; x < Param.ZONES_X; ++x) { // Reset merged status
      for (int y = 0; y < Param.ZONES_Y; ++y) zones[x][y].merged = false;
    }

    for (int x = 0; x < Param.ZONES_X; ++x) {
      for (int y = 0; y < Param.ZONES_Y; ++y) {
        if (doGreenZones && zones[x][y].tileColour != Colour.kGREEN) continue;
        if (doHills && zones[x][y].level == 1) continue;
        if (zones[x][y].merged) continue;
        final boolean hwh = zones[x][y].hillWithinHill;
        final int hwh_level = zones[x][y].level + (zones[x][y].level > 1 ? 1 : -1);

        if (GameState.getInstance().debug > 0)
          Gdx.app.log("doSpecialZones - " + (doGreenZones ? "GreenZone" : "Hill"),
              "Start z(" + x + "," + y + ") L(" + zones[x][y].getLowerX() + "," + zones[x][y].getLowerY() +
                  ") HWH:" + zones[x][y].hillWithinHill);

        // Do a flood-fill
        final IVector2 floodStart = new IVector2(x, y);
        floodFillMask(floodStart, false, doHills); // False as onZones

        // Collate results of the flood fill
        Vector<Pair<IVector2, IVector2>> edgePairs = new Vector<Pair<IVector2, IVector2>>();
        Vector<Zone> zone = new Vector<Zone>();
        collateFloodFill(edgePairs, zone);

        final Vector<IVector2> edges = edgePairsToEdges(edgePairs);
        if (edges == null) {
          Gdx.app.error("doSpecialZone", "Failed edgePairsToEdges doGreen:" + doGreenZones + " doHill:" + doHills + " doHWH:" + hwh);
          return false;
        }

        boolean success = true;
        if (doGreenZones)
          success = doEdges(edges, zone, Colour.kRED, Colour.kGREEN, 1, 1, 2 * Param.KRINKLE_OFFSET, (3 * Param.KRINKLE_OFFSET) - Param.KRINKLE_GAP);
        if (doHills)
          success = doEdges(edges, zone, zones[x][y].tileColour, zones[x][y].tileColour, 1, zones[x][y].level, 3 * Param.KRINKLE_OFFSET, (4 * Param.KRINKLE_OFFSET) - Param.KRINKLE_GAP);
        if (doHills && success && hwh) {
          if (GameState.getInstance().debug > 0) Gdx.app.log("doSpecialZones - HillWithinHill",
              "Start z(" + x + "," + y + ") L(" + zones[x][y].getLowerX() + "," + zones[x][y].getLowerY() + ")");
          success = doEdges(edges, zone, zones[x][y].tileColour, zones[x][y].tileColour, zones[x][y].level, hwh_level, 4 * Param.KRINKLE_OFFSET, (5 * Param.KRINKLE_OFFSET) - Param.KRINKLE_GAP);
        }
        if (!success) {
          if (GameState.getInstance().debug > 0)
            Gdx.app.error("  doSpecialZone", "Failed doEdges doGreen:" + doGreenZones + " doHill:" + doHills + " doHWH:" + hwh);
          return false;
        }
      }
    }
    return true;
  }

  private void floodFillMask(final IVector2 start, final boolean onTiles, final boolean compareLevel) {
    Queue<IVector2> queue = new PriorityQueue<IVector2>();
    queue.add(start);
    Colour fill;
    int level;
    if (onTiles) {
      tiles[start.x][start.y].mask = true;
      fill = tiles[start.x][start.y].tileColour;
      level = tiles[start.x][start.y].level;
    } else {
      zones[start.x][start.y].mask = true;
      fill = zones[start.x][start.y].tileColour;
      level = zones[start.x][start.y].level;
    }

    while (!queue.isEmpty()) {
      IVector2 node = queue.remove();
      if (node.y < (onTiles ? Param.TILES_Y - 1 : Param.ZONES_Y - 1)) {
        Entity N = (onTiles ? tiles[node.x][node.y + 1] : zones[node.x][node.y + 1]);
        if (N.tileColour == fill && (!compareLevel || N.level == level) && !N.mask) {
          N.mask = true;
          queue.add(new IVector2(node.x, node.y + 1));
        }
      }
      if (node.x < (onTiles ? Param.TILES_X - 1 : Param.ZONES_X - 1)) {
        Entity E = (onTiles ? tiles[node.x + 1][node.y] : zones[node.x + 1][node.y]);
        if (E.tileColour == fill && (!compareLevel || E.level == level) && !E.mask) {
          E.mask = true;
          queue.add(new IVector2(node.x + 1, node.y));
        }
      }
      if (node.y > 0) {
        Entity S = (onTiles ? tiles[node.x][node.y - 1] : zones[node.x][node.y - 1]);
        if (S.tileColour == fill && (!compareLevel || S.level == level) && !S.mask) {
          S.mask = true;
          queue.add(new IVector2(node.x, node.y - 1));
        }
      }
      if (node.x > 0) {
        Entity W = (onTiles ? tiles[node.x - 1][node.y] : zones[node.x - 1][node.y]);
        if (W.tileColour == fill && (!compareLevel || W.level == level) && !W.mask) {
          W.mask = true;
          queue.add(new IVector2(node.x - 1, node.y));
        }
      }
    }
  }

  private void floodFill(final Zone z, final Colour fromC, final Colour toC, final int fromLevel, final int toLevel) {
    for (int x = z.lowerLeft.x; x < z.upperRight.x; ++x) {
      for (int y = z.lowerLeft.y; y < z.upperRight.y; ++y) {
//        Gdx.app.log("DBG","x " + x + " y " + y + " | " + z.upperRight.x + " " + z.upperRight.y);
        if (!tiles[x][y].mask && tiles[x][y].tileColour == fromC && tiles[x][y].level == fromLevel) {
          setGround(new IVector2(x, y), toC, toLevel, Edge.kFLAT);
        }
      }
    }
  }

  private Cardinal getDirection(final IVector2 from, final IVector2 to) {
    if (from.x == to.x) return (to.y > from.y ? Cardinal.kN : Cardinal.kS);
    return (to.x > from.x ? Cardinal.kE : Cardinal.kW);
  }

  private boolean doEdges(final Vector<IVector2> edges, final Vector<Zone> zones,
                          final Colour fromC, final Colour toC, final int fromLevel, final int toLevel,
                          final int offset, final int maxIncursion) {

    if (GameState.getInstance().debug > 0)
      Gdx.app.log("  doEdges", "FromC " + fromC + " toC " + toC + ", fromL " + fromLevel + " toL " + toLevel);

    IVector2 location = new IVector2(edges.firstElement().x + offset, edges.firstElement().y + offset);
    final IVector2 start = new IVector2(location);
    final boolean doingHill = (fromLevel != toLevel && fromC != Colour.kBLACK);
    int stairCount = 0;

    for (int section = 1; section < edges.size(); ++section) {
      final IVector2 current = edges.elementAt(section - 1).clone();
      final IVector2 destination = edges.elementAt(section).clone();
      final IVector2 next = (section < edges.size() - 1 ? edges.elementAt(section + 1).clone() : null);
      final Cardinal D = getDirection(current, destination);
      final boolean rightTurnNext = (next == null || getDirection(destination, next) == D.next90(true));
      if (GameState.getInstance().debug > 0) Gdx.app.log("    Edge",
          "From " + edges.elementAt(section - 1).toString() +
              " to " + destination.toString() +
              ", D: " + D + ", RightTurnNext:" + rightTurnNext);
      int distanceToDest = 999;
      Edge direction = Edge.kOUT;
      while (distanceToDest > 0) {
        int duration = Param.MIN_DIST + R.nextInt(Param.MAX_DIST - Param.MIN_DIST + 1);
        if (direction == Edge.kSTAIRS_IN || direction == Edge.kSTAIRS_OUT)
          duration = 2 + R.nextInt(Param.MIN_DIST); // Keep flat buffers shorter
        for (int step = 0; step < duration; ++step) {
          // Special block: to try and match up at the end
          if (section == edges.size() - 1 && location.x - start.x < maxIncursion) { // TODO maxIncursions - offset?
            direction = (location.y > start.y ? Edge.kOUT : Edge.kIN);
          }
          increaseStep(location, D, toC, toLevel);
          distanceToDest = distanceToDestination(location, D, offset, destination, rightTurnNext);
          direction = krinkle(location, D, direction, distanceToDest, maxIncursion, destination, fromC, toC, fromLevel, toLevel);
          if (!Util.inBounds(location)) {
            Gdx.app.error("doEdges", "Serious krinkle error, gone out-of-bounds");
            return true; // TODO make this an error, return false
          }
          if (distanceToDest == 0) break;
        }
        if (direction == Edge.kSTAIRS_IN) {
          direction = Edge.kSTAIRS;
        } else if (direction == Edge.kSTAIRS) {
          direction = Edge.kSTAIRS_OUT;
          ++stairCount;
        } else {
          direction = Edge.random(doingHill
              && direction != Edge.kSTAIRS_OUT
              && distanceToDest > Param.MAX_DIST * 2, zones.size());
        }
      }
    }

    if (doingHill && stairCount < Param.MIN_STAIRCASES) {
      Gdx.app.error("doEdges", "Staircases " + stairCount + " smaller than min " + Param.MIN_STAIRCASES);
      return false;
    }

    // Connect back up to the start
    while (location.y != start.y) {
      location.y += (start.y > location.y ? 1 : -1);
      setGround(location, toC, toLevel, Edge.kFLAT);
    }

    IVector2 floodStart = edges.firstElement().clone();
    while (tiles[floodStart.x][floodStart.y].tileColour != fromC || tiles[floodStart.x][floodStart.y].level != fromLevel) {
      floodStart.x++;
      floodStart.y++;
    }
    floodFillMask(floodStart, true, true);

    for (Zone z : zones) {
      floodFill(z, fromC, toC, fromLevel, toLevel);
    }
    for (int x = 0; x < Param.TILES_X; ++x) { // Can spread outside of the area
      for (int y = 0; y < Param.TILES_Y; ++y) tiles[x][y].mask = false;
    }

    return true;
  }

  private void setNeighbours() {
    for (int x = 1; x < Param.TILES_X - 1; ++x) {
      for (int y = 1; y < Param.TILES_Y - 1; ++y) {
        Map<Cardinal, Tile> map = new EnumMap<Cardinal, Tile>(Cardinal.class);
        map.put(Cardinal.kN, tiles[x][y + 1]);
        map.put(Cardinal.kNE, tiles[x + 1][y + 1]);
        map.put(Cardinal.kE, tiles[x + 1][y]);
        map.put(Cardinal.kSE, tiles[x + 1][y - 1]);
        map.put(Cardinal.kS, tiles[x][y - 1]);
        map.put(Cardinal.kSW, tiles[x - 1][y - 1]);
        map.put(Cardinal.kW, tiles[x - 1][y]);
        map.put(Cardinal.kNW, tiles[x - 1][y + 1]);
        tiles[x][y].n8 = map;
      }
    }
  }

  private boolean applyTileGraphics() {
    // Outer are forced to be void
    // Do these first so we don't try and access outside of the tilespace
    for (int x = 0; x < Param.TILES_X; ++x) {
      tiles[x][0].setTexture(TileType.getTextureString(TileType.kGROUND, Colour.kBLACK), 1, false);
      tiles[x][Param.TILES_Y - 1].setTexture(TileType.getTextureString(TileType.kGROUND, Colour.kBLACK), 1, false);
    }
    for (int y = 0; y < Param.TILES_Y; ++y) {
      tiles[0][y].setTexture(TileType.getTextureString(TileType.kGROUND, Colour.kBLACK), 1, false);
      tiles[Param.TILES_X - 1][y].setTexture(TileType.getTextureString(TileType.kGROUND, Colour.kBLACK), 1, false);
    }
    // Set the rest
    boolean ok = true;
    for (int x = 1; x < Param.TILES_X - 1; ++x) {
      for (int y = 1; y < Param.TILES_Y - 1; ++y) {
        String tex = TileType.getTextureString(tiles[x][y]);
        ok &= (!tex.contains("missing")); // Should not return "missingX"
        ok &= tiles[x][y].setTexture(tex, 1, false); // Should be found in atlas
      }
    }
    if (!ok) Gdx.app.error("applyTileGraphics", "Graphics errors reported");
    return ok;
  }

}