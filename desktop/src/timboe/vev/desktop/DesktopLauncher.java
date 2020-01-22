package timboe.vev.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import timboe.vev.VEVGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "VEV";
		config.useHDPI = true;
		config.width = 1920/2;
		config.height = Math.round(config.width * (9f/16f));
		config.vSyncEnabled = true;
		config.foregroundFPS = 60;
		config.samples = 2;
		config.fullscreen = false;
		config.forceExit = false;
		config.addIcon("ic_launcher_128.png", Files.FileType.Internal);
		config.addIcon("ic_launcher_32.png", Files.FileType.Internal);
		config.addIcon("ic_launcher_16.png", Files.FileType.Internal);
		new LwjglApplication(new VEVGame(), config);
	}
}
