package timboe.vev.entity;


import timboe.vev.Param;
import timboe.vev.enums.Cardinal;
import timboe.vev.enums.Particle;
import timboe.vev.manager.GameState;

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
        // Have to wait if building is upgrading
        if (!myBuilding.doUpgrade) holding += speed * delta;
        if (holding >= capacity) {
          holding = capacity;
          pathTo(myBuilding.getBuildingDestination(Particle.kBlank), null, null);
          truckState = TruckState.kGO_TO_PATCH;
        }
        break;
      case kOFFLOAD:
        float toRemove = speed * delta;
        if (toRemove > holding) toRemove = holding;
        holding -= toRemove;
        GameState.getInstance().playerEnergy -= toRemove;
        if (holding < 1f) {
          pathTo(myBuilding.getPathingStartPoint(Particle.kBlank), null, null);
          truckState = TruckState.kRETURN_FROM_PATCH;
        }
        break;
    }
  }

}
