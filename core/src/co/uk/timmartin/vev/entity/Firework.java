package co.uk.timmartin.vev.entity;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayDeque;

import co.uk.timmartin.vev.Pair;
import co.uk.timmartin.vev.Param;
import co.uk.timmartin.vev.Util;
import co.uk.timmartin.vev.enums.Particle;
import co.uk.timmartin.vev.manager.Camera;
import co.uk.timmartin.vev.manager.Sounds;
import co.uk.timmartin.vev.manager.Textures;

public class Firework extends Entity {

  // All members are transient (class does not need serialisation)
  private final float g = 600f;
  private final float angleJitter = 0.1f;
  private ArrayDeque<Pair<Vector2, ArrayDeque<Vector2>>> locations = new ArrayDeque<Pair<Vector2, ArrayDeque<Vector2>>>();

  public Firework(int x) {
    super(x, 0, 1);
    setTexture(Textures.getInstance().getTexture("ball_grey", false), 0);
    setTexture(Textures.getInstance().getTexture("ball_" + Particle.random().getColourFromParticle().getString(), false), 1);
    this.frame = 0;
    Pair<Vector2, ArrayDeque<Vector2>> last = extend();
    last.getKey().set(-80 + Util.R.nextFloat() * 160, randomVel());
    last.getValue().addLast(new Vector2(x, 0));
    Sounds.getInstance().woosh();
  }

  private float randomVel() {
    return 800 + Util.R.nextInt(300);
  }

  public void act(float delta) {
    boolean isActive = false;
    boolean isBooming = (this.locations.size() > 1);

    for (Pair<Vector2, ArrayDeque<Vector2>> p : this.locations) {
      if (p.getValue().size() == 0) {
        continue;
      }
      isActive = true;

      // Movement
      boolean isStick = (p == this.locations.getFirst());
      boolean isDead = p.getKey().isZero();

      if ((isStick && !isBooming) || (!isStick && !isDead)) {
        Vector2 current = p.getValue().getLast();
        p.getValue().addLast(new Vector2(current.x + p.getKey().x * delta, current.y + p.getKey().y * delta));
      }

      // Gravity
      if (!isDead) {
        p.getKey().y -= g * delta;
      }

      // Cull
      if ((isStick && isBooming) || p.getValue().size() > 20 || isDead) {
        p.getValue().removeFirst();
      }

      // Twinkle
      if (!isStick) {
        for (Vector2 v : p.getValue()) {
          if (Util.R.nextFloat() < 0.1f) {
            v.y -= (v.y > 0 ? 1e4 : -1e4);
          }
        }
      }

      // Extinguish
      if (!isStick) {
        if (Util.R.nextFloat() < 0.01f) {
          p.getKey().setZero();
        }
      }
    }

    if (!isBooming && locations.getFirst().getKey().y < 100) {
      explode();
    }

    if (!isActive) {
      remove(); // From parent stage
    }
  }

  private void explode() {
    Sounds.getInstance().boom();
    Camera.getInstance().addShake(Param.BUILDING_SHAKE);
    final int N = Util.clamp(50 + (int) Util.R.nextGaussian() * 20, 20, 100);
    float a = Util.R.nextFloat();
    for (int n = 0; n < N; ++n) {
      Pair<Vector2, ArrayDeque<Vector2>> last = extend();
      last.getKey().set(this.locations.getFirst().getKey());
      float boomM = randomVel() * (n % 2 == 0 ? 1 : 0.5f);
      Vector2 boomV = new Vector2((float) Math.sin(a) * boomM, (float) Math.cos(a) * boomM);
      last.getKey().add(boomV);
      last.getValue().addLast(this.locations.getFirst().getValue().getLast());
      a += ((Math.PI * 2) / N) + (-angleJitter + Util.R.nextFloat() * angleJitter * 2);
    }
  }

  protected void doDraw(Batch batch) {
    this.frame = 0;
    for (Pair<Vector2, ArrayDeque<Vector2>> p : this.locations) {
      for (Vector2 v : p.getValue()) {
        batch.draw(this.textureRegion[this.frame], v.x, v.y, this.getOriginX(), this.getOriginY(), this.getWidth(), this.getHeight(), this.getScaleX(), this.getScaleY(), this.getRotation());
      }
      this.frame = 1;
    }
  }

  private Pair<Vector2, ArrayDeque<Vector2>> extend() {
    locations.addLast(new Pair<Vector2, ArrayDeque<Vector2>>(new Vector2(), new ArrayDeque<Vector2>()));
    return locations.getLast();
  }
}
