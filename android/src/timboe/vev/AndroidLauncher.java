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
    config.useAccelerometer = false;
    config.useCompass = false;
    config.useGyroscope = false;
    config.useRotationVectorSensor = true;
		initialize(new VEVGame(), config);
	}
}


