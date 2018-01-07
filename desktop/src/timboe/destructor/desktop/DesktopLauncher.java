package timboe.destructor.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import timboe.destructor.DestructorGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Particle Destructor";
		config.useHDPI = true;
		config.width = 1920;
		config.height = 1080;
		config.vSyncEnabled = true;
		config.foregroundFPS = 60;
		config.samples = 2;
		config.fullscreen = false;
		new LwjglApplication(new DestructorGame(), config);
	}
}
