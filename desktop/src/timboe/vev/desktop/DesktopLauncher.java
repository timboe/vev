package timboe.vev.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import timboe.vev.VEVGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "VEV";
		config.useHDPI = true;
		config.vSyncEnabled = true;
		config.foregroundFPS = 60;
		config.samples = 2;
		config.forceExit = false;
		config.addIcon("ic_launcher_128.png", Files.FileType.Internal);
		config.addIcon("ic_launcher_32.png", Files.FileType.Internal);
		config.addIcon("ic_launcher_16.png", Files.FileType.Internal);
		config.width = LwjglApplicationConfiguration.getDesktopDisplayMode().width;
		config.height = LwjglApplicationConfiguration.getDesktopDisplayMode().height;
		config.fullscreen = false;
		config.resizable = false;
		new LwjglApplication(new VEVGame(), config);
	}
}
