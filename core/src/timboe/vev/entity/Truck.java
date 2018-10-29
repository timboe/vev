package timboe.vev.entity;


import com.badlogic.gdx.graphics.g2d.Batch;

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
  private int extraFrames;

  public Truck(Tile t, Building myBuilding) {
    super(t);
    setTexture("pyramid", Param.N_TRUCK, false);
    moveBy(0, Param.TILE_S/2); // Floats
    this.myBuilding = myBuilding;
    frame = 0;
    extraFrames = 0;
  }

  @Override
  protected void atFinalDestination(Tile next, boolean wasParked) {
    switch (truckState) {
      case kGO_TO_PATCH: truckState = TruckState.kOFFLOAD; return;
      case kRETURN_FROM_PATCH: truckState = TruckState.kLOAD; return;
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
    moveBy(0, .05f * (float)Math.cos(time));

    switch (truckState) {
      case kLOAD:
        // Have to wait if building is upgrading
        if (myBuilding.doUpgrade) return;
        holding += speed * delta;
        extraFrames = Math.round((holding / capacity) * (Param.N_TRUCK - 1));
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
        extraFrames = Math.round((holding / capacity) * (Param.N_TRUCK - 1));
        GameState.getInstance().playerEnergy -= toRemove;
        if (holding < 1f) {
          pathTo(myBuilding.getPathingStartPoint(Particle.kBlank), null, null);
          truckState = TruckState.kRETURN_FROM_PATCH;
        }
        break;
      default:
        break;
    }
  }

}
