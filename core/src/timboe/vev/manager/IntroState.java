package timboe.vev.manager;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

import timboe.vev.Param;
import timboe.vev.Util;
import timboe.vev.entity.Entity;
import timboe.vev.entity.Sprite;
import timboe.vev.entity.Tile;
import timboe.vev.enums.Particle;

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
  private Stage introHelpStage;
  private Stage introUIStage;

  private boolean addedParticles;


  private IntroState() {
    reset();
  }

  public void reset() {
    if (introTileStage != null) introTileStage.dispose();
    if (introFoliageStage != null) introFoliageStage.dispose();
    if (introHelpStage != null) introHelpStage.dispose();
    if (introSpriteStage != null) introSpriteStage.dispose();
    if (introUIStage != null) introUIStage.dispose();
    introTileStage = new Stage(Camera.getInstance().getTileViewport());
    introSpriteStage = new Stage(Camera.getInstance().getSpriteViewport());
    introFoliageStage = new Stage(Camera.getInstance().getSpriteViewport());
    introHelpStage = new Stage(Camera.getInstance().getTileViewport());
    introUIStage = new Stage(Camera.getInstance().getUiViewport());
    addedParticles = false;
  }

  public void addParticles() {
    if (addedParticles) {
      return;
    }
    int pType = 0;
    addedParticles = true;
    for (double a = -Math.PI; a <= Math.PI; a += (2*Math.PI) / (double)(Particle.values().length - 1) ) { // -1 due to kBlank
      if (pType == (Particle.values().length - 1)) break; // Else rely on floating point in for loop
      Particle p = Particle.values()[ pType++ ];
      int tileX = Param.TILES_INTRO_X_MID + (int)Math.round( Param.TILES_INTRO_X_MID * 0.66f * Math.cos(a) );
      int tileY = Param.TILES_INTRO_Y_MID + (int)Math.round( Param.TILES_INTRO_X_MID * 0.66f * Math.sin(a) );
      Tile genTile = World.getInstance().getIntroTile(tileX, tileY);
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
  }

  public void dispose() {
    introTileStage.dispose();
    introSpriteStage.dispose();
    introFoliageStage.dispose();
    introHelpStage.dispose();
    introUIStage.dispose();
    ourInstance = null;
  }

  public void act(float delta) {
    introSpriteStage.act();
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
}
