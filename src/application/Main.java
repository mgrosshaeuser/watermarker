package application;
	
import java.io.File;

import application.data.WorkingImage;
import application.gui.PrimaryWindow;
import javafx.application.Application;
import javafx.stage.Stage;



public class Main extends Application {
	
	private static  WorkingImage   activeImage;
	private static  PrimaryWindow  primaryWindow;

	
	
	public static void main(String[] args) {
		launch(args);
	}
	
	
	
	@Override
	public void start(Stage primaryStage) {
		primaryWindow = new PrimaryWindow(primaryStage);
		primaryStage = PrimaryWindow.getPrimaryStage();
		primaryStage.show();
	}
	
		

	public static PrimaryWindow getWindow() {
		return primaryWindow;
	}
	
	
	public static WorkingImage getActiveImage() {
		return activeImage;
	}
	
	
	public static void setActiveImage(File file) {
		activeImage = new WorkingImage(file);
	}
}
