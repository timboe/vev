package timboe.vev.manager;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

import timboe.vev.entity.Entity;

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

  private Stage uiStage;


  private IntroState() {
    reset();
  }

  public void reset() {
    if (introTileStage != null) introTileStage.dispose();
    if (introFoliageStage != null) introFoliageStage.dispose();
    if (introHelpStage != null) introHelpStage.dispose();
    if (introSpriteStage != null) introSpriteStage.dispose();
    if (uiStage != null) uiStage.dispose();
    introTileStage = new Stage(Camera.getInstance().getTileViewport());
    introSpriteStage = new Stage(Camera.getInstance().getSpriteViewport());
    introFoliageStage = new Stage(Camera.getInstance().getSpriteViewport());
    introHelpStage = new Stage(Camera.getInstance().getTileViewport());
    uiStage = new Stage(Camera.getInstance().getUiViewport());
  }

  public void dispose() {
    introTileStage.dispose();
    introSpriteStage.dispose();
    introFoliageStage.dispose();
    introHelpStage.dispose();
    uiStage.dispose();
    ourInstance = null;
  }

  public void act(float delta) {
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
    return uiStage;
  }

  public Stage getIntroHelpStage() {
    return introHelpStage;
  }
}
