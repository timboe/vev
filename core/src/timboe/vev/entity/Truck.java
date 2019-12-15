package timboe.vev.entity;


import com.badlogic.gdx.graphics.g2d.Batch;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import timboe.vev.Param;
import timboe.vev.enums.Cardinal;
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
  private float holding = 0f, capacity = Param.TRUCK_INITIAL_CAPACITY, speed = Param.TRUCK_LOAD_SPEED;
  private int myBuilding;
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
    holding = (float) json.getDouble("holding");
    capacity = (float) json.getDouble("capacity");
    speed = (float) json.getDouble("speed");
    myBuilding = json.getInt("myBuilding");
    extraFrames = json.getInt("extraFrames");
  }


  public Truck(Tile t, Building myBuilding) {
    super(t);
    setTexture("pyramid", Param.N_TRUCK, false);
    moveBy(0, Param.TILE_S/2); // Floats
    this.myBuilding = myBuilding.id;
    this.frame = 0;
    this.extraFrames = 0;
    this.truckState = TruckState.kOFFLOAD;
  }

  private Building getBuilding() {
    return GameState.getInstance().getBuildingMap().get(myBuilding);
  }

  private Patch getPatch() {
    return World.getInstance().getTiberiumPatches().get(getBuilding().myPatch);
  }

  @Override
  protected void atFinalDestination(Tile next, boolean wasParked) {
    switch (truckState) {
      case kGO_TO_PATCH: this.truckState = TruckState.kLOAD; return;
      case kRETURN_FROM_PATCH: this.truckState = TruckState.kOFFLOAD; return;
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
    moveBy(.2f * (float)Math.sin(time), .2f * (float)Math.cos(time));

    switch (truckState) {
      case kOFFLOAD:
        Building myB = getBuilding();
        // Have to wait if building is upgrading
        if (myB.doUpgrade) return;
        float toRemove = this.speed * delta;
        if (toRemove > this.holding) toRemove = this.holding;
        this.holding -= toRemove;
        this.extraFrames = Math.round((this.holding / this.capacity) * (Param.N_TRUCK - 1));
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
        this.extraFrames = Math.round((holding / capacity) * (Param.N_TRUCK - 1));
        Patch myPatch = getPatch();
        if (curFrame != this.extraFrames) {
          myPatch.removeRandom();
        }
        if (holding >= capacity || myPatch.remaining() == 0) {
          holding = capacity;
          Tile t = World.getInstance().getTile( getBuilding().getPathingStartPoint(Particle.kBlank) );
          pathTo(t, null, null);
          truckState = TruckState.kRETURN_FROM_PATCH;
        }
        if (myPatch.remaining() == 0) {
          getBuilding().updateMyPatch();
        }
        break;
      default:
        break;
    }
  }

}
