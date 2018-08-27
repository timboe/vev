package timboe.destructor.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import timboe.destructor.DestructorGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "VEV";
		config.useHDPI = true;
		config.width = 1920;
		config.height = Math.round(config.width * (9f/16f));
		config.vSyncEnabled = true;
		config.foregroundFPS = 60;
		config.samples = 2;
		config.fullscreen = false;
		new LwjglApplication(new DestructorGame(), config);
	}
}
