package timboe.destructor.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum Edge {
  kFLAT,
  kIN,
  kOUT;

  private static final List<Edge> values = Collections.unmodifiableList(Arrays.asList(values()));
  private static final int size = values.size();
  private static final Random random = new Random();

  public static Edge random()  {
    return values.get(random.nextInt(size));
  }
}
