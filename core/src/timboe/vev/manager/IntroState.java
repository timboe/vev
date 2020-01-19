package timboe.vev.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;

import timboe.vev.Param;
import timboe.vev.Util;
import timboe.vev.VEVGame;
import timboe.vev.entity.Building;
import timboe.vev.entity.Entity;
import timboe.vev.entity.Sprite;
import timboe.vev.entity.Tile;
import timboe.vev.entity.Truck;
import timboe.vev.entity.Warp;
import timboe.vev.enums.BuildingType;
import timboe.vev.enums.Colour;
import timboe.vev.enums.Particle;
import timboe.vev.pathfinding.IVector2;
import timboe.vev.pathfinding.OrderlyQueue;
import timboe.vev.screen.TitleScreen;

import static timboe.vev.enums.Particle.kW;

public class IntroState {

  private static IntroState ourInstance;
  public static IntroState getInstance() {
    return ourInstance;
  }
  public static void create() { ourInstance = new IntroState(); }
  public static boolean constructed() { return ourInstance != null; }

  private Stage introTileStage;
  private Stage introSpriteStage;
  private Stage introFoliageStage;
  private Stage introBuildingStage;
  private Stage introHelpStage;
  private Stage introUIStage;
  private Stage introWarpStage;

  public HashSet<Entity> demoBuildings = new HashSet<Entity>();
  public Warp demoWarp;

  private IntroState() {
    reset();
  }

  public void reset() {
    if (introTileStage != null) introTileStage.dispose();
    if (introFoliageStage != null) introFoliageStage.dispose();
    if (introBuildingStage != null) introBuildingStage.dispose();
    if (introHelpStage != null) introHelpStage.dispose();
    if (introSpriteStage != null) introSpriteStage.dispose();
    if (introUIStage != null) introUIStage.dispose();
    if (introWarpStage != null) introWarpStage.dispose();
    introTileStage = new Stage(Camera.getInstance().getTileViewport());
    introSpriteStage = new Stage(Camera.getInstance().getSpriteViewport());
    introFoliageStage = new Stage(Camera.getInstance().getSpriteViewport());
    introBuildingStage = new Stage(Camera.getInstance().getTileViewport());
    introHelpStage = new Stage(Camera.getInstance().getTileViewport());
    introUIStage = new Stage(Camera.getInstance().getUiViewport());
    introWarpStage = new Stage(Camera.getInstance().getTileViewport());
    Color warpStageC = introWarpStage.getBatch().getColor();
    warpStageC.a = Param.WARP_TRANSPARENCY;
    introWarpStage.getBatch().setColor(warpStageC);
    demoBuildings.clear();
  }

  public void addParticles() {
    if (getIntroSpriteStage().getActors().size > 0) {
      return;
    }
    Gdx.app.log("addParticles", "Doing intro particle population.");
    int pType = 0;
    for (double a = -Math.PI; a <= Math.PI; a += (2*Math.PI) / (double)(Particle.values().length - 1) ) { // -1 due to kBlank
      if (pType == (Particle.values().length - 1)) break; // Else rely on floating point in for loop
      Particle p = Particle.values()[ pType++ ];
      int tileX = Param.TILES_INTRO_X_MID + (int)Math.round( Param.TILES_INTRO_X_MID * 0.66f * Math.cos(a) );
      int tileY = Param.TILES_INTRO_Y_MID + (int)Math.round( Param.TILES_INTRO_X_MID * 0.66f * Math.sin(a) );
      Tile genTile = World.getInstance().getTile(tileX, tileY, true);
      for (int i = 0; i < 200; ++i) {
        Sprite s = new Sprite(genTile);
        s.isIntro = true;
        s.moveBy(Param.TILE_S / 2, Param.TILE_S / 2);
        s.pathTo(s.findPathingLocation(genTile, false, true, true, true), null, null);
        s.setTexture("ball_" + p.getColourFromParticle().getString(), 6, false);
        s.setParticle(p);
        s.moveBy(Util.R.nextInt(Param.TILE_S), Util.R.nextInt(Param.TILE_S));
        s.idleTime = s.boredTime; // Start the wanderlust right away
        getIntroSpriteStage().addActor(s);
      }
    }
    World.getInstance().genPatch(38, 46, false, Param.TIBERIUM_SIZE, true, null);
    //
    Entity demoMine = new Entity(18,40);
    demoMine.isIntro = true;
    demoMine.selected = true;
    demoMine.setTexture("building_" + BuildingType.kMINE.ordinal(), 1, false);
    demoMine.buildingPathingLists = new EnumMap<Particle, List<IVector2>>(Particle.class);
    demoMine.buildingPathingLists.put(Particle.kBlank, new ArrayList<IVector2>());
    for (int x = 20; x < 35; ++x) {
      demoMine.buildingPathingLists.get(Particle.kBlank).add(new IVector2(x,39));
    }
    for (int x = 35, y = 40; x < 39; ++x, ++y) {
      demoMine.buildingPathingLists.get(Particle.kBlank).add(new IVector2(x,y));
    }
    demoBuildings.add(demoMine);
    getIntroBuildingStage().addActor(demoMine);
    //
    Truck demoTruck = new Truck(World.getInstance().getTile(28,40, true), null);
    demoTruck.extraFrames = 5;
    getIntroSpriteStage().addActor(demoTruck);
    //
    demoWarp = new Warp(World.getInstance().getTile(37, 27, true));
    getIntroWarpStage().addActor(demoWarp);
    demoBuildings.add(demoWarp);
    demoWarp.buildingPathingLists = new EnumMap<Particle, List<IVector2>>(Particle.class);
    demoMine.buildingPathingLists.put(kW, new ArrayList<IVector2>());
    demoMine.buildingPathingLists.get(kW).add(new IVector2(41,23));
    demoMine.buildingPathingLists.get(kW).add(new IVector2(40,22));
    for (int x = 39; x > 20; --x) {
      demoMine.buildingPathingLists.get(kW).add(new IVector2(x,21));
    }
    //
    final int b1_x = 19, b1_y = 24;
    final BuildingType b1_t = BuildingType.kHWM;
    Entity demoBuilding1 = new Entity(b1_x, b1_y);
    demoBuilding1.isIntro = true;
    demoBuilding1.selected = true;
    demoBuilding1.setTexture("building_" + b1_t.ordinal(), 1, false);
    demoBuildings.add(demoBuilding1);
    getIntroBuildingStage().addActor(demoBuilding1);
    Entity banner1 = new Entity(b1_x + 2, b1_y);
    banner1.setTexture("board_vertical", 1, false);
    getIntroBuildingStage().addActor(banner1);
    for (int i = 0; i < BuildingType.N_MODES; ++i) {
      Entity p = new Entity(Param.SPRITE_SCALE*(b1_x), Param.SPRITE_SCALE*(b1_y + 1));
      p.moveBy(73, -5 + (20 * i)); // Fine tune-position of
      p.setTexture("ball_" +  b1_t.getInput(BuildingType.N_MODES - i - 1).getColourFromParticle().getString(), 1, false);
      getIntroSpriteStage().addActor(p);
    }
    OrderlyQueue q_1 = new OrderlyQueue(b1_x , b1_y - 1, null, null, true);
    demoBuilding1.buildingPathingLists = new EnumMap<Particle, List<IVector2>>(Particle.class);
    demoBuilding1.buildingPathingLists.put(Particle.kE, new ArrayList<IVector2>());
    demoBuilding1.buildingPathingLists.get(Particle.kE).add(new IVector2(21,21));
    demoBuilding1.buildingPathingLists.get(Particle.kE).add(new IVector2(20,20));
    demoBuilding1.buildingPathingLists.get(Particle.kE).add(new IVector2(19,20));
    for (int y = 21; y <= 26; ++y) {
      demoBuilding1.buildingPathingLists.get(Particle.kE).add(new IVector2(18,y));
    }
    demoBuilding1.buildingPathingLists.get(Particle.kE).add(new IVector2(19,27));
    demoBuilding1.buildingPathingLists.get(Particle.kE).add(new IVector2(20,27));
    demoBuilding1.buildingPathingLists.get(Particle.kE).add(new IVector2(21,28));
    //
    final int b2_x = 19, b2_y = 31;
    final BuildingType b2_t = BuildingType.kHZE;
    Entity demoBuilding2 = new Entity(b2_x, b2_y);
    demoBuilding2.isIntro = true;
    demoBuilding2.selected = true;
    demoBuilding2.setTexture("building_" + b2_t.ordinal(), 1, false);
    demoBuildings.add(demoBuilding2);
    getIntroBuildingStage().addActor(demoBuilding2);
    Entity banner2 = new Entity(b2_x + 2, b2_y);
    banner2.setTexture("board_vertical", 1, false);
    getIntroBuildingStage().addActor(banner2);
    for (int i = 0; i < BuildingType.N_MODES; ++i) {
      Entity p = new Entity(Param.SPRITE_SCALE*(b2_x), Param.SPRITE_SCALE*(b2_y + 1));
      p.moveBy(73, -5 + (20 * i)); // Fine tune-position of
      p.setTexture("ball_" +  b2_t.getInput(BuildingType.N_MODES - i - 1).getColourFromParticle().getString(), 1, false);
      getIntroSpriteStage().addActor(p);
    }
    OrderlyQueue q_2 = new OrderlyQueue(b2_x , b2_y - 1, null, null, true);
    //
    for (int i = 0; i < 4; ++i) {
      Sprite demoSprite = new Sprite(World.getInstance().getTile(31+i, 22, true));
      demoSprite.setTexture("ball_" + kW.getColourFromParticle().getString(), 6, false);
      demoSprite.setParticle(kW);
      demoSprite.moveBy(Param.TILE_S / 2, Param.TILE_S / 2);
      demoSprite.idleTime = -9999f; // Never wander
      getIntroSpriteStage().addActor(demoSprite);
    }

  }

  public void dispose() {
    introTileStage.dispose();
    introSpriteStage.dispose();
    introFoliageStage.dispose();
    introBuildingStage.dispose();
    introHelpStage.dispose();
    introUIStage.dispose();
    introWarpStage.dispose();
    ourInstance = null;
  }

  public void act(float delta) {
    introSpriteStage.act(delta);
    introWarpStage.act(delta);
  }

  public void retextureSprites() {
    for (Actor a : introSpriteStage.getActors()) {
      ((Entity)a).loadTexture();
    }
  }

  public Stage getIntroTileStage() {
    return introTileStage;
  }

  public Stage getIntroSpriteStage() {
    return introSpriteStage;
  }

  public Stage getIntroFoliageStage() {
    return introFoliageStage;
  }

  public Stage getUIStage() {
    return introUIStage;
  }

  public Stage getIntroHelpStage() {
    return introHelpStage;
  }

  public Stage getIntroWarpStage() { return introWarpStage; }

  public Stage getIntroBuildingStage() { return introBuildingStage; }

}
