package timboe.destructor.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum Colour {
  kRED,
  kGREEN,
  kBLUE,
  kBLACK;

  private static final List<Colour> values = Collections.unmodifiableList(Arrays.asList(values()));
  private static final int size = values.size();
  private static final Random R = new Random();

  public static Colour random() {
    return values.get(R.nextInt(size));
  }

  public String getString() {
    switch (this) {
      case kRED: return "r";
      case kGREEN: return "g";
      case kBLUE: return "blue";
      default: return "b";
    }
  }
}
