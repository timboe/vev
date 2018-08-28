package timboe.vev;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
    config.numSamples = 2;
//    config.resolutionStrategy = new ResolutionStrategy() {
//      @Override
//      public MeasuredDimension calcMeasures(int widthMeasureSpec, int heightMeasureSpec) {
//        return new MeasuredDimension(1920, 1080);
//      }
//    };
		initialize(new VEVGame(), config);
	}
}


