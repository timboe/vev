package timboe.destructor.entity;


import java.util.Map;

import timboe.destructor.Param;
import timboe.destructor.enums.Cardinal;
import timboe.destructor.enums.Particle;
import timboe.destructor.manager.GameState;

/**
 * Created by Tim on 21/01/2018.
 */

public class Truck extends Sprite {

  private enum TruckState {
    kGO_TO_PATCH,
    kOFFLOAD,
    kRETURN_FROM_PATCH,
    kLOAD;
  }

  private TruckState truckState = TruckState.kLOAD;
  float holding = 0f, capacity = 1000f, speed = 200f;
  private Building myBuilding;

  public Truck(Tile t, Building myBuilding) {
    super(t);
    setTexture("truck", Param.N_TRUCK, false);
    this.myBuilding = myBuilding;
  }

  @Override
  protected void atFinalDestination(Tile next, boolean wasParked) {
    switch (truckState) {
      case kGO_TO_PATCH: truckState = TruckState.kOFFLOAD; return;
      case kRETURN_FROM_PATCH: truckState = TruckState.kLOAD; return;
    }
  }

  @Override
  public void act(float delta) {
    time += delta;
    actMovement(delta);
    // Get direction from velocity vector
    frame = Cardinal.fromAngle( velocity.angle() ).ordinal();

    switch (truckState) {
      case kLOAD:
        holding += speed * delta;
        if (holding >= capacity) {
          holding = capacity;
          pathTo(myBuilding.getBuildingDestination(Particle.kH), null, null);
          truckState = TruckState.kGO_TO_PATCH;
        }
        break;
      case kOFFLOAD:
        float toRemove = speed * delta;
        if (toRemove > holding) toRemove = holding;
        holding -= toRemove;
        GameState.getInstance().playerEnergy -= toRemove;
        if (holding < 1f) {
          pathTo(myBuilding.getPathingStartPoint(Particle.kH), null, null);
          truckState = TruckState.kRETURN_FROM_PATCH;
        }
        break;
    }
  }

}
