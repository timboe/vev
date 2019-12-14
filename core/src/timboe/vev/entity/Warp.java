package timboe.vev.entity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Random;

import timboe.vev.Param;
import timboe.vev.Util;
import timboe.vev.enums.BuildingType;
import timboe.vev.enums.Particle;
import timboe.vev.manager.GameState;
import timboe.vev.manager.Textures;
import timboe.vev.manager.UI;
import timboe.vev.manager.World;
import timboe.vev.pathfinding.IVector2;

/**
 * Created by Tim on 10/01/2018.
 */

public class Warp extends Building {

  private float[] rotAngle = {0f, 90f, 0f, -90f};
  private final float[] rotV = {Param.WARP_ROTATE_SPEED, Param.WARP_ROTATE_SPEED, -Param.WARP_ROTATE_SPEED, -Param.WARP_ROTATE_SPEED};
  private final int pathingStartPointSeed = Util.R.nextInt();
  public final ParticleEffect warpCloud;
  public final ParticleEffect zap;

  // Persistent
  private final EnumMap<Particle, IVector2> pathingStartPointWarp = new EnumMap<Particle, IVector2>(Particle.class);

  public JSONObject serialise() throws JSONException {
    JSONObject json = super.serialise();
    JSONObject startPointJson = new JSONObject();
    for (EnumMap.Entry<Particle, IVector2> entry : pathingStartPointWarp.entrySet()) {
      startPointJson.put(entry.getKey().toString(), entry.getValue().serialise());
    }
    json.put("pathingStartPointWarp", startPointJson);
    return json;
  }

  public Warp(JSONObject json) throws JSONException {
    super(json);
    setTexture( Textures.getInstance().getTexture("void", false), 0);
    setTexture( Textures.getInstance().getTexture("void", true), 1);
    setTexture( Textures.getInstance().getTexture("void", false), 2);
    setTexture( Textures.getInstance().getTexture("void", true), 3);

    JSONObject startPointJson = json.getJSONObject("pathingStartPointWarp");
    Iterator startIt = startPointJson.keys();
    while (startIt.hasNext()) {
      String key = (String) startIt.next();
      IVector2 v = new IVector2( startPointJson.getJSONObject(key));
      pathingStartPointWarp.put( Particle.valueOf(key), v );
      Gdx.app.log("warp deserialise","Particle "+key+" starts from "+v);
    }

    int fxX = coordinates.x + (Param.WARP_SIZE/2) - 2;
    int fxY = coordinates.y + (Param.WARP_SIZE/2) - 2;

    warpCloud = new ParticleEffect();
    warpCloud.load(Gdx.files.internal("hell_portal_effect.txt"), Textures.getInstance().getAtlas());
    warpCloud.setPosition(Param.TILE_S * fxX + Param.TILE_S / 2, Param.TILE_S * fxY);
    warpCloud.start();

    zap = new ParticleEffect();
    zap.load(Gdx.files.internal("lightning_effect.txt"), Textures.getInstance().getAtlas());
    zap.setPosition(Param.TILE_S * fxX + Param.TILE_S / 2, Param.TILE_S * fxY);
    zap.allowCompletion();

    // We are still crashing here...
    updatePathingStartPoint();
    updatePathingDestinations();
  }

  public Warp(Tile t) {
    super(t, BuildingType.kWARP);
    setTexture( Textures.getInstance().getTexture("void", false), 0);
    setTexture( Textures.getInstance().getTexture("void", true), 1);
    setTexture( Textures.getInstance().getTexture("void", false), 2);
    setTexture( Textures.getInstance().getTexture("void", true), 3);
    moveBy(0, -Param.TILE_S/2);
    // UpdatePathingStartPoint is called later by World - we don't have a pathing grid yet!

    warpCloud = new ParticleEffect();
    warpCloud.load(Gdx.files.internal("hell_portal_effect.txt"), Textures.getInstance().getAtlas());
    warpCloud.setPosition(Param.TILE_S * t.coordinates.x + Param.TILE_S / 2, Param.TILE_S * t.coordinates.y);
    warpCloud.start();

    zap = new ParticleEffect();
    zap.load(Gdx.files.internal("lightning_effect.txt"), Textures.getInstance().getAtlas());
    zap.setPosition(Param.TILE_S * t.coordinates.x + Param.TILE_S / 2, Param.TILE_S * t.coordinates.y);
    zap.allowCompletion();
  }

  @Override
  public void updatePathingStartPoint() {
    // We make our own random here so that the Warp's start points do not jump around on every building place
    Random R = new Random(pathingStartPointSeed);
    for (Particle p : Particle.values()) {
      int placeTry = 0;
      do {
        double rAngle = -Math.PI + (R.nextFloat() * Math.PI * 2);
        int tryX = (int) Math.round(coordinates.x + Param.WARP_SIZE/2 - 2 + ((Param.WARP_SIZE/2 + 1) * Math.cos(rAngle)));
        int tryY = (int) Math.round(coordinates.y + Param.WARP_SIZE/2 - 2 + ((Param.WARP_SIZE/2 + 1) * Math.sin(rAngle)));
        Tile tryTile = World.getInstance().getTile(tryX, tryY);
        if (tryTile.coordinates.getNeighbours().size() == 0) continue; // Non-pathable
        boolean used = false;
        for (Particle p2 : Particle.values()) {
          if (p == p2) continue;
          if (pathingStartPointWarp.get(p2) == null) continue;;
//          Gdx.app.log("TIMM", ""+pathingStartPointWarp.get(p2));
          Tile startingPoint = coordinateToTile( pathingStartPointWarp.get(p2) );
          if ( startingPoint == tryTile) used = true;
        }
        if (used) continue; // Another particle has this starting point
        pathingStartPointWarp.put(p, tryTile.coordinates);
        break;
      } while (++placeTry < Param.N_PATCH_TRIES);
    }
  }

  @Override
  protected IVector2 getPathingStartPoint(Particle p) {
//    Gdx.app.log("getPathingStartPoint WARP","Returning for "+p+" "+pathingStartPointWarp.get(p));
    return pathingStartPointWarp.get(p);
  }

  public boolean newParticles(int toPlace, boolean stressTest) {
    boolean placed = false;
    float rand = Util.R.nextFloat() + 0.1f; // This extra allows for random
    Particle toPlaceParticle = null;
    for (Particle p : Particle.values()) {
      if (p == Particle.kBlank) continue;
      rand -= p.getCreateChance();
      if (rand <= 0) {
        toPlaceParticle = p;
        break;
      }
    }
    if (stressTest) toPlaceParticle = null;
    for (int tp = 0; tp < toPlace; ++tp) {
      // Occasional random spawn mode
      Particle p = (toPlaceParticle == null ? Particle.random() : toPlaceParticle);
      if (GameState.getInstance().warpEnergy > 0) {
        placeParticle(p);
        GameState.getInstance().warpEnergy -= p.getCreateEnergy();
        placed = true;
        if (GameState.getInstance().warpEnergy < 0) {
          GameState.getInstance().warpEnergy = 0;
        }
      }
    }
    return placed;
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    for (int i = 0; i < 4; ++i) {
      rotAngle[i] += delta * rotV[i];
    }
  }

  @Override
  public void draw(Batch batch, float alpha) {
    for (int i = 0; i < 4; ++i) {
      batch.draw(textureRegion[i],this.getX(),this.getY(),this.getOriginX(),this.getOriginY(),this.getWidth(),this.getHeight(),this.getScaleX(),this.getScaleY(),rotAngle[i]);
    }
  }

}
