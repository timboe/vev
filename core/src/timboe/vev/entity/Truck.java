package timboe.vev.entity;


import com.badlogic.gdx.graphics.g2d.Batch;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import timboe.vev.Param;
import timboe.vev.Util;
import timboe.vev.enums.Particle;
import timboe.vev.manager.GameState;
import timboe.vev.manager.World;
import timboe.vev.pathfinding.IVector2;

/**
 * Created by Tim on 21/01/2018.
 */

public class Truck extends Sprite {

  private enum TruckState {
    kGO_TO_PATCH,
    kOFFLOAD,
    kRETURN_FROM_PATCH,
    kLOAD,
    kDORMANT; // Nothing left to get
  }

  // Persistent
  private TruckState truckState;
  private int holding = 0, capacity = Param.TRUCK_INITIAL_CAPACITY, speed = Param.TRUCK_LOAD_SPEED;
  public int myBuilding;
  private int extraFrames;

  public JSONObject serialise() throws JSONException {
    JSONObject json = super.serialise();
    json.put("truckState", truckState.name());
    json.put("holding", holding);
    json.put("capacity", capacity);
    json.put("speed", speed);
    json.put("myBuilding", myBuilding);
    json.put("extraFrames",extraFrames);
    return  json;
  }

  public Truck(JSONObject json) throws JSONException {
    super(json);
    truckState = TruckState.valueOf( json.getString("truckState") );
    holding = json.getInt("holding");
    capacity = json.getInt("capacity");
    speed = json.getInt("speed");
    myBuilding = json.getInt("myBuilding");
    extraFrames = json.getInt("extraFrames");
  }


  public Truck(Tile t, Building myBuilding) {
    super(t);
    setTexture("pyramid", Param.N_TRUCK_SPRITES, false);
    moveBy(0, Param.TILE_S/2); // Floats
    this.myBuilding = myBuilding.id;
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
      case kGO_TO_PATCH: this.truckState = TruckState.kLOAD; return;
      case kRETURN_FROM_PATCH: this.truckState = TruckState.kOFFLOAD; return;
      case kDORMANT: return;
    }
  }

  @Override
  protected void doDraw(Batch batch) {
    super.doDraw(batch);
    for (int i = 1; i < extraFrames; ++i) {
      batch.draw(textureRegion[i],this.getX(),this.getY(),this.getOriginX(),this.getOriginY(),this.getWidth(),this.getHeight(),this.getScaleX(),this.getScaleY(),this.getRotation());
    }
  }

  @Override
  public void act(float delta) {
    time += delta;
    actMovement(delta);
    moveBy(0f, .2f * (float)Math.cos(time));
    Building myB = getBuilding();
    Patch myPatch = myB != null ? World.getInstance().getTiberiumPatches().get(myB.myPatch) : null;

    switch (truckState) {
      case kOFFLOAD:
        // Was deconstructed?
        if (myB == null) {
          truckState = TruckState.kDORMANT;
          return;
        }
        // Have to wait if building is upgrading
        if (myB.doUpgrade) return;
        float toRemove = this.speed * delta;
        if (toRemove > this.holding) toRemove = this.holding;
        this.holding -= toRemove;
        if (Util.R.nextFloat() > 0.8f) {
          IVector2 v = myB.coordinates;
          GameState.getInstance().upgradeDustEffect( World.getInstance().getTile( v.x + 1, v.y + 2) );
        }
        this.extraFrames = Math.round((this.holding / (float)this.capacity) * (Param.N_TRUCK_SPRITES - 1));
        GameState.getInstance().playerEnergy += toRemove;
        if (holding < 1f) {
          Tile destination = myB.getBuildingDestination(Particle.kBlank);
          if (destination == null) {
            truckState = TruckState.kDORMANT;
          } else {
            pathTo(destination, null, null);
            truckState = TruckState.kGO_TO_PATCH;
          }
        }
        break;
      case kLOAD:
        this.holding += this.speed * delta;
        int curFrame = this.extraFrames;
        this.extraFrames = Math.round((this.holding / (float)this.capacity) * (Param.N_TRUCK_SPRITES - 1));
        if (myB == null || myPatch == null) {
          truckState = TruckState.kDORMANT;
          return;
        }
        if (curFrame != this.extraFrames) {
          myPatch.removeRandom();
        }
        if (holding >= capacity || myPatch.remaining() == 0) {
          holding = capacity;
          Tile t = World.getInstance().getTile( myB.getPathingStartPoint(Particle.kBlank) );
          pathTo(t, null, null);
          truckState = TruckState.kRETURN_FROM_PATCH;
        }
        if (myPatch.remaining() == 0) {
          myB.updateMyPatch();
        }
        break;
      case kDORMANT:
        if (myB == null || myPatch == null || myPatch.remaining() == 0) {
          return;
        }
        Tile t = World.getInstance().getTile( myB.getPathingStartPoint(Particle.kBlank) );
        pathTo(t, null, null);
        truckState = TruckState.kRETURN_FROM_PATCH;
      default:
        break;
    }
  }

}
