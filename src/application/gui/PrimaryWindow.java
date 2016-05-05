package application.gui;


import application.gui.PrimaryMenu;
import application.gui.Control;

import javafx.scene.Scene;

import javafx.scene.control.Label;

import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class PrimaryWindow {
	
	public static final int		SCENE_WIDTH		= 1000;
	public static final int		SCENE_HEIGHT	= 700;
	public static final String 	WINDOW_TITLE 	= "Watermarker Beta II";

	
	private static 	Stage			primaryStage;
	private static 	Scene			scene;
	private static	BorderPane 		primaryGuiContainer;
	private static	PrimaryMenu 	primaryMenu;
	
	private static	Label			headerBar;
	private static	Label			statusBar;

	
	
	
	public PrimaryWindow(Stage primaryStage) {
		PrimaryWindow.primaryStage = primaryStage;

		createSceneElements();
		configureSceneElements();
		createScene();
		configureStage();
	}
	

	
	
	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	
	protected static PrimaryMenu getMenu() {
		return primaryMenu;
	}


	protected static Label getStatusBar() {
		return statusBar;
	}

	
	
	
	protected static void showLoadedImage() {
		Control.setBackgroundImage(primaryGuiContainer);
	}
	
	
	
	
	private static void createSceneElements() {
		headerBar 			= new Label("");
		statusBar 			= new Label("");
		
		primaryGuiContainer	= new BorderPane();
		primaryMenu			= new PrimaryMenu(primaryStage);
	}
	
	
	private static void configureSceneElements(){
		headerBar.setId("headerBar");
		statusBar.setId("statusBar");
	
		primaryGuiContainer.setTop		(headerBar);
		primaryGuiContainer.setRight	(primaryMenu.getPrimaryMenuPane());
		primaryGuiContainer.setBottom	(statusBar);
		
		statusBar.prefWidthProperty().bind(primaryGuiContainer.widthProperty());
	}
		
	
	private static void createScene() {
		scene = new Scene(primaryGuiContainer, SCENE_WIDTH, SCENE_HEIGHT);
		scene.getStylesheets().add("application/gui/application.css");
	}

	
	private static void configureStage() {
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.setTitle(WINDOW_TITLE);
		primaryStage.setOnCloseRequest(e -> { 	
				Control.setPrimaryStageDefaultClosingOperation();	
				e.consume();
			});
	}
	
}
