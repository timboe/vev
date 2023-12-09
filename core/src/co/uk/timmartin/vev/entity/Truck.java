package co.uk.timmartin.vev.entity;


import com.badlogic.gdx.graphics.g2d.Batch;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import co.uk.timmartin.vev.Param;
import co.uk.timmartin.vev.Util;
import co.uk.timmartin.vev.enums.Particle;
import co.uk.timmartin.vev.manager.GameState;
import co.uk.timmartin.vev.manager.World;
import co.uk.timmartin.vev.pathfinding.IVector2;

/**
 * Created by Tim on 21/01/2018.
 */

public class Truck extends Sprite {

  private enum TruckState {
    kGO_TO_PATCH,
    kOFFLOAD,
    kRETURN_FROM_PATCH,
    kLOAD,
    kDORMANT // Nothing left to get
  }

  // Persistent
  private TruckState truckState;
  public int holding = 0;
  public int level = 0;
  public int myBuilding;
  public int extraFrames;
  private float toRemove = 0, toAdd = 0;


  public JSONObject serialise() throws JSONException {
    JSONObject json = super.serialise();
    json.put("truckState", truckState.name());
    json.put("holding", holding);
    json.put("level", level);
    json.put("myBuilding", myBuilding);
    json.put("extraFrames", extraFrames);
    json.put("toRemove", toRemove);
    json.put("toAdd", toAdd);
    return json;
  }

  public float getUpgradeFactor() {
    return (float) Math.pow(Param.TRUCK_SPEED_BONUS, level);
  }

  public float getNextUpgradeFactor() {
    return (float) Math.pow(Param.TRUCK_SPEED_BONUS, level + 1);
  }

  public Truck(JSONObject json) throws JSONException {
    super(json);
    truckState = TruckState.valueOf(json.getString("truckState"));
    holding = json.getInt("holding");
    level = json.getInt("level");
    myBuilding = json.getInt("myBuilding");
    extraFrames = json.getInt("extraFrames");
    toAdd = (float) json.getDouble("toAdd");
    toRemove = (float) json.getDouble("toRemove");
  }

  @Override
  protected float spriteVelocity() {
    return Param.PARTICLE_VELOCITY * getUpgradeFactor();
  }

  private float getSpeed() {
    return Param.TRUCK_LOAD_SPEED * getUpgradeFactor();
  }

  public int getCapacity() {
    return Math.round(Param.TRUCK_INITIAL_CAPACITY * getUpgradeFactor());
  }

  public Truck(Tile t, Building myBuilding) {
    super(t);
    setTexture("pyramid", Param.N_TRUCK_SPRITES, false);
    moveBy(0, Param.TILE_S / 2); // Floats
    this.myBuilding = myBuilding == null ? 0 : myBuilding.id;
    this.frame = 0;
    this.extraFrames = 0;
    this.truckState = TruckState.kOFFLOAD;
  }

  public void orphan() {
    myBuilding = 0;
    truckState = TruckState.kDORMANT;
  }

  private Building getBuilding() {
    if (myBuilding == 0) return null;
    return GameState.getInstance().getBuildingMap().get(myBuilding);
  }

  @Override
  protected void atFinalDestination(Tile next, boolean wasParked) {
    switch (truckState) {
      case kGO_TO_PATCH:
        this.truckState = TruckState.kLOAD;
        return;
      case kRETURN_FROM_PATCH:
        this.truckState = TruckState.kOFFLOAD;
        return;
      case kDORMANT:
        return;
    }
  }

  @Override
  protected void doDraw(Batch batch) {
    super.doDraw(batch);
    for (int i = 1; i < extraFrames; ++i) {
      batch.draw(textureRegion[i], this.getX(), this.getY(), this.getOriginX(), this.getOriginY(), this.getWidth(), this.getHeight(), this.getScaleX(), this.getScaleY(), this.getRotation());
    }
  }

  @Override
  public void act(float delta) {

    Building myB = getBuilding();
    if (myB == null) {
      truckState = TruckState.kDORMANT;
      return;
    }
    Patch myPatch = World.getInstance().getTiberiumPatches().get(myB.myPatch);
    if (myPatch == null) {
      truckState = TruckState.kDORMANT;
      return;
    }
    final boolean actionsPaused = (myB.doUpgrade || myB.built > 0);
    final int capacity = getCapacity();
    this.extraFrames = Math.round((this.holding / (float) capacity) * (Param.N_TRUCK_SPRITES - 1));

    if (!actionsPaused) {
      actMovement(delta);
      time += delta;
      moveBy(0f, .2f * (float) Math.cos(time));
    }

    switch (truckState) {
      case kOFFLOAD:
        // Was deconstructed?
        if (actionsPaused) return;
        if (!myB.tryGetLock(this)) return; // Only one can unload at a time
        this.toRemove += getSpeed() * delta;
        if (this.toRemove > this.holding) this.toRemove = this.holding;
        int toRemoveInt = Math.round(toRemove);
        if (toRemoveInt > 0) {
          this.holding -= toRemoveInt;
          this.toRemove -= toRemoveInt;
          if (Util.R.nextFloat() > 0.8f) {
            IVector2 v = myB.coordinates;
            GameState.getInstance().upgradeDustEffect(World.getInstance().getTile(v.x + 1, v.y + 2, isIntro));
          }
          GameState.getInstance().addEnergy(toRemoveInt);
        }
        if (holding < 1f) {
          Tile destination = myB.getBuildingDestination(Particle.kBlank);
          myB.releaseLock(this);
          if (destination == null) {
            truckState = TruckState.kDORMANT;
          } else {
            pathTo(destination, null, null);
            truckState = TruckState.kGO_TO_PATCH;
          }
        }
        break;
      case kLOAD:
        if (actionsPaused) return; // Have to wait if building is upgrading
        this.toAdd += getSpeed() * delta;
        int toAddInt = Math.round(this.toAdd);
        if (toAddInt > 0) {
          this.holding += toAddInt;
          this.toAdd -= toAddInt;
          myPatch.remove(toAddInt);
          boolean doReturn = false;
          if (this.holding >= capacity) {
            doReturn = true;
            holding = capacity;
          }
          if (myPatch.remaining() == 0) {
            doReturn = true;
            myB.updateMyPatch();
          }
          if (doReturn) {
            Tile t = World.getInstance().getTile(myB.getPathingStartPoint(Particle.kBlank), isIntro);
            pathTo(t, null, null);
            truckState = TruckState.kRETURN_FROM_PATCH;
          }
        }
        break;
      case kDORMANT:
        // If we have made it this far, we have a new home
        Tile t = World.getInstance().getTile(myB.getPathingStartPoint(Particle.kBlank), isIntro);
        pathTo(t, null, null);
        truckState = TruckState.kRETURN_FROM_PATCH;
      default:
        break;
    }
  }

}
