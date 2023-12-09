package co.uk.timmartin.vev.manager;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import java.util.Random;

import co.uk.timmartin.vev.Param;
import co.uk.timmartin.vev.Util;
import co.uk.timmartin.vev.entity.Building;
import co.uk.timmartin.vev.entity.Entity;
import co.uk.timmartin.vev.entity.Sprite;
import co.uk.timmartin.vev.enums.FSM;
import co.uk.timmartin.vev.enums.UIMode;

public class Camera {

  private static Camera ourInstance;

  public static Camera getInstance() {
    return ourInstance;
  }

  public static void create() {
    ourInstance = new Camera();
  }

  public void dispose() {
    ourInstance = null;
  }

  private final Rectangle cullBoxTile = new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
  private final Rectangle cullBoxSprite = new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

  private float currentZoom = 1f;
  private float desiredZoom = 1f;

  private final Vector2 currentPos = new Vector2(0, 0);
  private final Vector2 desiredPos = new Vector2(0, 0);
  private final Vector2 velocity = new Vector2(0, 0);
  private float shake;
  private boolean cheats = false;

  private final Random R = new Random();

  private OrthographicCamera tileCamera;
  private OrthographicCamera spriteCamera;
  private OrthographicCamera uiCamera;

  private FitViewport tileViewport;
  private FitViewport spriteViewport;
  private FitViewport uiViewport;

  private Camera() {
    reset();
  }

  public JSONObject serialise() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("desiredPos.x", desiredPos.x);
    json.put("desiredPos.y", desiredPos.y);
    json.put("desiredZoom", desiredZoom);
    return json;
  }

  public void deserialise(JSONObject json) throws JSONException {
    desiredPos.set((float) json.getDouble("desiredPos.x"), (float) json.getDouble("desiredPos.y"));
    desiredZoom = (float) json.getDouble("desiredZoom");
    currentPos.set(desiredPos);
    currentZoom = desiredZoom;
  }

  public void setCurrentPos(float x, float y) {
    this.currentPos.set(x, y);
    this.desiredPos.set(x, y);
  }

  public void setHelpPos(int level, boolean instant) {
    this.desiredPos.set(Param.TILES_INTRO_X_MID * Param.TILE_S,
            (Param.TILES_INTRO_Y_MID * Param.TILE_S) - (level * Param.DISPLAY_Y * Param.TILES_INTRO_ZOOM)); // TODO another magic number to deduce :(
    setCurrentZoom(Param.TILES_INTRO_ZOOM);
    if (instant) {
      this.currentPos.set(this.desiredPos);
    }
  }

  public void setCurrentZoom(float currentZoom) {
    this.currentZoom = currentZoom;
    this.desiredZoom = currentZoom;
  }

  public FitViewport getTileViewport() {
    return tileViewport;
  }

  public FitViewport getSpriteViewport() {
    return spriteViewport;
  }

  public FitViewport getUiViewport() {
    return uiViewport;
  }

  public void resize(int width, int height) {
    tileViewport.update(width, height, true);
    spriteViewport.update(width, height, true);
    uiViewport.update(width, height, true);
  }

  public OrthographicCamera getTileCamera() {
    return tileCamera;
  }

  public OrthographicCamera getSpriteCamera() {
    return spriteCamera;
  }

  public OrthographicCamera getUiCamera() {
    return uiCamera;
  }

  public Rectangle getCullBoxTile() {
    return cullBoxTile;
  }

  public Rectangle getCullBoxSprite() {
    return cullBoxSprite;
  }

  public Vector3 unproject(Vector3 v) {
    return tileViewport.unproject(v);
  }

  boolean addShake(Entity e, float amount) {
    if (!onScreen(e)) return false;
    addShake(amount);
    return true;
  }

  public void addShake(float amount) {
    shake += amount;
    if (Gdx.app.getType() == Application.ApplicationType.Android && Persistence.getInstance().vibrate) {
      Gdx.input.vibrate((int)(amount * 25));
    }
  }

  public float distanceToCamera(int x, int y) {
    return (float) Math.hypot(x - currentPos.x, y - currentPos.y);
  }

  public boolean onScreen(Sprite s) {
    Rectangle.tmp.set(s.getX(), s.getY(), s.getWidth(), s.getHeight());
    return cullBoxSprite.contains(Rectangle.tmp) || cullBoxSprite.overlaps(Rectangle.tmp);
  }

  public boolean onScreen(Building b) {
    Rectangle.tmp.set(b.getX(), b.getY(), b.getWidth(), b.getHeight());
    return cullBoxTile.contains(Rectangle.tmp) || cullBoxSprite.overlaps(Rectangle.tmp);
  }

  public boolean onScreen(Entity t) {
    Rectangle.tmp.set(t.getX(), t.getY(), t.getWidth(), t.getHeight());
    return cullBoxTile.contains(Rectangle.tmp) || cullBoxTile.overlaps(Rectangle.tmp);
  }

  private void reset() {
    tileCamera = new OrthographicCamera();
    spriteCamera = new OrthographicCamera();
    uiCamera = new OrthographicCamera();

    tileViewport = new FitViewport(Param.DISPLAY_X, Param.DISPLAY_Y, tileCamera);
    spriteViewport = new FitViewport(Param.DISPLAY_X, Param.DISPLAY_Y, spriteCamera);
    uiViewport = new FitViewport(Param.DISPLAY_X, Param.DISPLAY_Y, uiCamera);
  }

  void pollInputs() {

    if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT)) {
      UIIntro.getInstance().fsListener.toggle();
    }

    if ((Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) && StateManager.getInstance().fsm == FSM.kGAME) {
      if (UI.getInstance().uiMode == UIMode.kSETTINGS) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
          GameState.getInstance().showMainUITable(true);
        } else {
          StateManager.getInstance().transitionToTitleScreen();
        }
      } else {
        UI.getInstance().showSettings();
      }
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && StateManager.getInstance().fsm == FSM.kINTRO) {
      Gdx.app.exit();
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
      cheats = !cheats;
      Gdx.app.log("pollInputs", "Cheats:" + cheats);
    }

    if (cheats) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
        GameState.getInstance().cht = true;
        GameState.getInstance().killSelectedParticles();
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
        GameState.getInstance().cht = true;
        GameState.getInstance().playerEnergy += Param.PLAYER_STARTING_ENERGY;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
        if (++GameState.getInstance().debug == 4) GameState.getInstance().debug = 0;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
        GameState.getInstance().cht = true;
        GameState.getInstance().tryNewParticles(true, GameState.getInstance().toFocusOn, 1);
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
        GameState.getInstance().cht = true;
        GameState.getInstance().warpParticles += 100;
        GameState.getInstance().tryNewParticles(false, GameState.getInstance().toFocusOn, 1);
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
        GameState.getInstance().cht = true;
        StateManager.getInstance().gameOver();
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.U)) {
        GameState.getInstance().uiOn = !GameState.getInstance().uiOn;
        Gdx.app.log("Cheats", "UI toggled");
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
        Sounds.getInstance().doMusic(false);
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
        GameState.getInstance().cht = true;
        GameState.getInstance().newParticlesMean += Param.WARP_SPAWN_MEAN_INCREASE;
        GameState.getInstance().newParticlesWidth += Param.WARP_SPAWN_WIDTH_INCREASE;
        GameState.getInstance().warpSpawnTime -= Param.WARP_SPAWN_TIME_REDUCTION;
      }
    }

    // Following are only allowed in-game or game over mode
    if (StateManager.getInstance().fsm != FSM.kGAME && StateManager.getInstance().fsm != FSM.kGAME_OVER) {
      return;
    }

    float vMod = 2f;
    if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
      vMod = 0.3f;
    }

    if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
      modVelocity(0, +vMod);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
      modVelocity(-vMod, 0);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
      modVelocity(0, -vMod);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
      modVelocity(+vMod, 0);
    }

    if (Gdx.input.isKeyPressed(Input.Keys.Q) || Gdx.input.isKeyPressed(Input.Keys.PAGE_DOWN)) {
      modZoom(0.01f * vMod);
    }
    if (Gdx.input.isKeyPressed(Input.Keys.E) || Gdx.input.isKeyPressed(Input.Keys.PAGE_UP)) {
      modZoom(-0.01f * vMod);
    }
  }

  public void translate(float x, float y) {
    float x2 = (x > 0 ? 1 : -1) * Math.min(Math.abs(x), Param.MAX_TRANSLATE);
    float y2 = (y > 0 ? 1 : -1) * Math.min(Math.abs(y), Param.MAX_TRANSLATE);
    final float xMod = (float) Param.DISPLAY_X / (float) getUiViewport().getScreenWidth();
    final float yMod = (float) Param.DISPLAY_Y / (float) getUiViewport().getScreenHeight();
    desiredPos.add(xMod * x2 * currentZoom, yMod * y2 * currentZoom);
  }

  public void velocity(float x, float y) {
    this.velocity.set(x * Param.FLING_MOD, y * Param.FLING_MOD);
  }

  public void modVelocity(float x, float y) {
    this.velocity.x += x;
    this.velocity.y += y;
  }

  public void modZoom(float z) {
    setZoom(desiredZoom + z);
  }

  public void setZoom(float z) {
    desiredZoom = z;
    desiredZoom = Util.clamp(desiredZoom, Param.ZOOM_MIN, Param.ZOOM_MAX);
    float sfx = Util.clamp(1f / (desiredZoom + 0.75f), 0.2f, 1f);
    Sounds.getInstance().sfxLevel(sfx);
  }

  public float getZoom() {
    return desiredZoom;
  }


  public void update(float delta) {
    pollInputs();

    float frames = delta / Param.FRAME_TIME;
    final float scale = (float) Math.pow(0.9f, frames);

    desiredPos.add(velocity);
    velocity.scl(scale);

    shake *= (float) Math.pow(0.9f, frames);
    float shakeAngle = R.nextFloat() * (float) Math.PI * 2f;

    if (StateManager.getInstance().fsm == FSM.kINTRO) {
      currentPos.x += (desiredPos.x - currentPos.x) * 0.1f;
      currentPos.y += (desiredPos.y - currentPos.y) * 0.1f;
    } else {
      currentPos.set(desiredPos); // Direct control in-game
    }

    if (desiredPos.x - Param.DISPLAY_X / 2 < -1000) modVelocity(10, 0);
    else if (desiredPos.x - Param.DISPLAY_X / 2 > (Param.TILES_X * Param.TILE_S) + 1000)
      modVelocity(-10, 0);

    if (desiredPos.y + Param.DISPLAY_Y / 2 < -1000) modVelocity(0, 10);
    else if (desiredPos.y + Param.DISPLAY_Y / 2 > (Param.TILES_Y * Param.TILE_S) + 1000)
      modVelocity(0, -10);

    currentPos.add(shake * (float) Math.cos(shakeAngle), shake * (float) Math.sin(shakeAngle));
    currentZoom += (desiredZoom - currentZoom) * 0.1f;

    tileCamera.position.set(currentPos, 0);
    tileCamera.zoom = currentZoom;
    tileCamera.update();

    spriteCamera.position.set(currentPos, 0);
    spriteCamera.zoom = currentZoom;
    spriteCamera.zoom *= (float) Param.SPRITE_SCALE;
    spriteCamera.position.scl((float) Param.SPRITE_SCALE);
    spriteCamera.update();

    uiCamera.position.set(uiViewport.getWorldWidth() / 2, uiViewport.getWorldHeight() / 2, 0f);
    // Note - sin & cos are inverted for the UI vs. the game world
    uiCamera.position.add(shake * (float) Math.sin(shakeAngle) / currentZoom, shake * (float) Math.cos(shakeAngle) / currentZoom, 0);
    uiCamera.update();

    cullBoxTile.set(
            tileCamera.position.x - (tileViewport.getWorldWidth() / 2) * currentZoom,
            tileCamera.position.y - (tileViewport.getWorldHeight() / 2) * currentZoom,
            tileViewport.getWorldWidth() * currentZoom,
            tileViewport.getWorldHeight() * currentZoom);

    cullBoxSprite.set(
            spriteCamera.position.x - (Param.SPRITE_SCALE * spriteViewport.getWorldWidth() / 2) * currentZoom,
            spriteCamera.position.y - (Param.SPRITE_SCALE * spriteViewport.getWorldHeight() / 2) * currentZoom,
            Param.SPRITE_SCALE * spriteViewport.getWorldWidth() * currentZoom,
            Param.SPRITE_SCALE * spriteViewport.getWorldHeight() * currentZoom);
  }

}