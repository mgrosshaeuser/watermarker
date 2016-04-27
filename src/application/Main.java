package application;
	
import java.io.File;
import java.net.MalformedURLException;
import java.util.Optional;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;


/**
 * GUI of watermarker.
 * 
 * @author Markus Grosshaeuser
 *
 */

public class Main extends Application{
	private static final int	SCENE_WIDTH				= 1000;
	private static final int	SCENE_HEIGHT			= 700;
	
	private static final double	MENU_INITIAL_OPACITY 	= 0.2;
	private static final double	MENU_MIN_OPACITY 		= 0.0;
	private static final double	MENU_MAX_OPACITY 		= 0.9;
	private static final int	MENU_ELEMENT_GAPS		= 12;
	private static final Insets MENU_PADDING			= new Insets(20,20,20,20);
	
	
	private static WorkingImage 	activeImage;

		
	
	
	/**
	 * Does nothing but start the GUI.
	 */
	public static void main(String[] args) {
		launch(args);
	}
	

	
	/**
	 * Start of the GUI.
	 */
	@Override
	public void start(Stage primaryStage) {
		BorderPane rootPane = new BorderPane();
		rootPane.setId("root");
				
		Label topBar 		= new Label("");
		Label statusBar 	= new Label("");

		rootPane.setRight(makeMenu(primaryStage, rootPane, statusBar));
		rootPane.setTop(topBar);
		rootPane.setBottom(statusBar);
		
		// Neue Scene erzeugen, Stylesheet laden und der Stage zuweisen.
		Scene scene = new Scene(rootPane,SCENE_WIDTH,SCENE_HEIGHT);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.setTitle("Watermarker Beta");
		primaryStage.setOnCloseRequest(e->{		primaryStageDefaultClosingOperation(e);		});
		primaryStage.show();
	}



	/**
	 * Defines the default closing operation of the primary stage.
	 * If there are any unsaved changes, the program prompts a dialog asking what to do.
	 * 
	 * @param e		Eventhandler(MouseEvent)
	 */
	private void primaryStageDefaultClosingOperation(WindowEvent e) {
		if (activeImage == null || activeImage.isImageSaved())		
			System.exit(0);
		else {
			Alert unsaved = new Alert(AlertType.CONFIRMATION);
			unsaved.setTitle(Messages.getString("Main.unsavedChangesTitle"));
			unsaved.setHeaderText(Messages.getString("Main.unsavedChangesHeader"));
			unsaved.setContentText(Messages.getString("Main.closeAnyway"));
			Optional<ButtonType> result = unsaved.showAndWait();
			if (result.get() == ButtonType.OK)
				System.exit(0);
			else
				e.consume();
		}
	}
	
	
	
	
	/**
	 * Sets an image as background of the rootPane.
	 * 
	 * @param file		The image-file.
	 */
	private static void showLoadedImage(File file, BorderPane rootPane){
		Image img 		= null;
		String source 	= null;
		
		try {
			source = file.toURI().toURL().toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		
		img = new Image(source);
		
		if (img.getHeight() > rootPane.getHeight()   ||    img.getWidth() > rootPane.getWidth())
			img =  new Image(source,rootPane.getWidth(), rootPane.getHeight(),true, true);
		
		
		rootPane.setBackground(new Background(new BackgroundImage	(img, 
																BackgroundRepeat.NO_REPEAT, 
																BackgroundRepeat.NO_REPEAT, 
																BackgroundPosition.CENTER, 
																BackgroundSize.DEFAULT)));
	}

	
	
	/**
	 * Creates the main menu as a GridPane containing Labels as menu-items.
	 * 
	 * @return	The main menu including lambda expressions.
	 */
	
	private static GridPane makeMenu(Stage primaryStage, BorderPane rootPane, Label statusBar){
		// Erzeugen der Menüleiste
		GridPane menuPane = new GridPane();
		menuPane.setId("menuPane");
		menuPane.setOpacity(MENU_INITIAL_OPACITY);
		menuPane.setVgap(MENU_ELEMENT_GAPS);
		menuPane.setHgap(MENU_ELEMENT_GAPS);
		menuPane.setPadding(MENU_PADDING);
		menuPane.setAlignment(Pos.CENTER);
				
		createMenuFadeTransitions(menuPane);
		
		
		// Menüeinträge werden durch Labels realisert.
		Label lblOpen 	= new Label(Messages.getString("Main.MenuOpen"));
		Label lblSave 	= new Label(Messages.getString("Main.MenuSave"));
		Label lblSaveAs	= new Label(Messages.getString("Main.MenuSaveAs"));
		Label lblRead 	= new Label(Messages.getString("Main.MenuRead"));
		Label lblWrite 	= new Label(Messages.getString("Main.MenuWrite"));
		Label lblErase 	= new Label(Messages.getString("Main.MenuDelete"));
		Label lblClose	= new Label(Messages.getString("Main.MenuExit"));

		lblOpen.setId	("menuLabel");
		lblSave.setId	("menuLabel");
		lblSaveAs.setId	("menuLabel");
		lblRead.setId	("menuLabel");
		lblWrite.setId	("menuLabel");
		lblErase.setId	("menuLabel");
		lblClose.setId	("menuLabel");
		
		setLambdaExpressionsforMenuItems(primaryStage, rootPane, statusBar, lblOpen, lblSave, lblSaveAs, lblRead,
				lblWrite, lblErase, lblClose);
		
		finalizeMenuItems(menuPane, lblOpen, lblSave, lblSaveAs, lblRead, lblWrite, lblErase, lblClose);

		return menuPane;
	}



	/**
	 * Defines the lambda expressions associated with the menu-items.
	 * 
	 * @param primaryStage		Stage. The primary stage of this program.
	 * @param rootPane			BorderPane used as container for all ui-elements of this stage.
	 * @param statusBar			Label used to display status information.
	 * @param lblOpen			Label as menu-item for file-open-operation.
	 * @param lblSave			Label as menu-item for file-save-operation.
	 * @param lblSaveAs			Label as menu-item for file-save-as-operation.
	 * @param lblRead			Label as menu-item for read-watermark-operation.
	 * @param lblWrite			Label as menu-item for write-watermark-operation.
	 * @param lblErase			Label as menu-item for erase-watermark-operation.
	 * @param lblClose			Label as menu-item for close-operation.
	 */
	private static void setLambdaExpressionsforMenuItems(Stage primaryStage, BorderPane rootPane, Label statusBar,
			Label lblOpen, Label lblSave, Label lblSaveAs, Label lblRead, Label lblWrite, Label lblErase,
			Label lblClose) {
		
		// Menüeintrag Datei Öffnen.
		lblOpen.setOnMouseEntered(		e-> {	lblOpen.setId("menuLabelMouseOver");							});
		lblOpen.setOnMouseExited(		e-> {	lblOpen.setId("menuLabel");										});
		lblOpen.setOnMouseClicked(		e-> {	menuActionOpenFile(primaryStage, rootPane, lblSave, 
												lblSaveAs, lblRead, lblWrite, lblErase); 						});
				
		// Menüeintrag Datei speichern.
		lblSave.setOnMouseEntered( 		e-> {	lblSave.setId("menuLabelMouseOver");							});
		lblSave.setOnMouseExited(		e-> {	lblSave.setId("menuLabel");										});
		lblSave.setOnMouseClicked(		e-> {	menuActionSaveFile(primaryStage, statusBar, lblSave);			});
		
		// Menüeintrag Datei speichern unter..
		lblSaveAs.setOnMouseEntered(	e-> {	lblSaveAs.setId("menuLabelMouseOver");							});
		lblSaveAs.setOnMouseExited(		e-> {	lblSaveAs.setId("menuLabel");									});
		lblSaveAs.setOnMouseClicked(	e-> {	menuActionSaveFileAs(primaryStage, statusBar, lblSave);			});
				
		// Menüeintrag Wasserzeichen auslesen. 
		lblRead.setOnMouseEntered(		e-> {	lblRead.setId("menuLabelMouseOver");							});
		lblRead.setOnMouseExited(		e-> {	lblRead.setId("menuLabel");										});
		lblRead.setOnMouseClicked(		e-> {	showInformationDialog(Messages.getString("Main.readWatermark"), 
												activeImage.getEmbeddedWatermark());							});
				
		// Menüeintrag Wasserzeichen schreiben.
		lblWrite.setOnMouseEntered(		e-> {	lblWrite.setId("menuLabelMouseOver");							});
		lblWrite.setOnMouseExited(		e-> {	lblWrite.setId("menuLabel");									});
		lblWrite.setOnMouseClicked(		e-> {	menuActionWriteWatermark(primaryStage, rootPane, statusBar, 
												lblSave, lblSaveAs, lblRead, lblWrite, lblErase);				});
				
		// Menüeintrag Wasserzeichen entfernen.
		lblErase.setOnMouseEntered(		e-> {	lblErase.setId("menuLabelMouseOver");							});
		lblErase.setOnMouseExited(		e-> {	lblErase.setId("menuLabel");									});
		lblErase.setOnMouseClicked(		e-> {	menuActionRemoveWatermark(primaryStage, rootPane, statusBar, 
												lblSave, lblSaveAs, lblRead, lblWrite, lblErase);				});
	
		// Menüeintrag Programm beenden.
		lblClose.setOnMouseEntered(		e-> {	lblClose.setId("menuLabelMouseOver");							});
		lblClose.setOnMouseExited(		e-> {	lblClose.setId("menuLabel");									});
		lblClose.setOnMouseClicked(		e-> {	menuActionExitWatermarker();									});
	}



	/**
	 * Sets the initial 'enabled/disabled' status of the menu-items and adding items to menuPane.
	 * 
	 * @param menuPane			GridPane used as menu.
 	 * @param lblOpen			Label as menu-item for file-open-operation.
	 * @param lblSave			Label as menu-item for file-save-operation.
	 * @param lblSaveAs			Label as menu-item for file-save-as-operation.
	 * @param lblRead			Label as menu-item for read-watermark-operation.
	 * @param lblWrite			Label as menu-item for write-watermark-operation.
	 * @param lblErase			Label as menu-item for erase-watermark-operation.
	 * @param lblClose			Label as menu-item for close-operation.
	 */
	private static void finalizeMenuItems(GridPane menuPane, Label lblOpen, Label lblSave, Label lblSaveAs,
			Label lblRead, Label lblWrite, Label lblErase, Label lblClose) {
		
		// Menüeinträge die beim Start nicht verfügbar sein sollen.
		lblSave.setDisable		(true);
		lblSaveAs.setDisable	(true);
		lblRead.setDisable		(true);
		lblWrite.setDisable		(true);
		lblErase.setDisable		(true);
		
		
		// Menüeinträge zum Menü hinzufügen.
		menuPane.add(lblOpen, 	0, 0);
		menuPane.add(lblSave, 	0, 1);
		menuPane.add(lblSaveAs, 0, 2);
		menuPane.add(lblRead, 	0, 5);
		menuPane.add(lblWrite, 	0, 6);
		menuPane.add(lblErase, 	0, 7);
		menuPane.add(lblClose,  0, 12);
	}



	/**
	 * Creates the FadeTransition for the menuPane on mouse-entered and mouse-exited.
	 * 
	 * @param menuPane		GridPane used as menu.
	 */
	private static void createMenuFadeTransitions(GridPane menuPane) {
		// Ein- und Ausblendeffekte der Menüleiste
		
		menuPane.setOnMouseEntered(	e-> {
			FadeTransition fadeIn = new FadeTransition(Duration.millis(100), menuPane);
			fadeIn.setFromValue(MENU_MIN_OPACITY);
			fadeIn.setToValue(MENU_MAX_OPACITY);
			fadeIn.play();
		});
		
		menuPane.setOnMouseExited( 	e-> {
			FadeTransition fadeOut = new FadeTransition(Duration.millis(200), menuPane);
			fadeOut.setFromValue(MENU_MAX_OPACITY);
			fadeOut.setToValue(MENU_MIN_OPACITY);
			fadeOut.play();
		});
	}



	/**
	 * Specifies the lambda-expression for the menu-item 'open file'.
	 * A new file will only be opened if:
	 * (1) there is no open file,
	 * (2) the open file was saved or
	 * (3) the user accepts (via prompt) the loss of unsaved changes.
	 * 
	 * @param primaryStage		Stage. The primary stage of this program.
	 * @param rootPane			BorderPane used as container for all ui-elements of this stage.
	 * @param lblSave			Label as menu-item for file-save-operation.
	 * @param lblSaveAs			Label as menu-item for file-save-as-operation.
	 * @param lblRead			Label as menu-item for read-watermark-operation.
	 * @param lblWrite			Label as menu-item for write-watermark-operation.
	 * @param lblErase			Label as menu-item for erase-watermark-operation.
	 */
	private static void menuActionOpenFile(Stage primaryStage, BorderPane rootPane, Label lblSave, Label lblSaveAs,
			Label lblRead, Label lblWrite, Label lblErase) {
		
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(new ExtensionFilter("PNG-Files", "*.png"));
		File file = null;;
		
		Alert unsaved = new Alert(AlertType.CONFIRMATION);
		unsaved.setTitle(Messages.getString("Main.unsavedChangesTitle")); 
		unsaved.setHeaderText(Messages.getString("Main.unsavedChangesHeader")); 
		unsaved.setContentText(Messages.getString("Main.openAnyway")); 
	
		if (activeImage == null || activeImage.isImageSaved() || unsaved.showAndWait().get() == ButtonType.OK)		
			file = chooser.showOpenDialog(primaryStage);
			if (file != null){
				activeImage = new WorkingImage(file);
				if (activeImage.isImageLoaded()){
					if (activeImage.getEmbeddedWatermark().equals("")){ 
						lblWrite.setDisable	(false);
						lblRead.setDisable	(true);
						lblErase.setDisable	(true);
					} else {
						lblRead.setDisable	(false);
						lblErase.setDisable	(false);
						lblWrite.setDisable	(true);
					}
					lblSave.setDisable		(true);
					lblSaveAs.setDisable	(false);
					primaryStage.setTitle(file.toString());
					showLoadedImage(file, rootPane);
				} else {
					showInformationDialog(Messages.getString("Main.error"), Messages.getString("Main.fileOpenError"));
				}
			}
		else {
				return;
		}
	}

	
	
	/**
 	 * Specifies the lambda-expression for the menu-item 'save file'.
	 * 
	 * @param primaryStage		Stage. The primary stage of this program.
	 * @param statusBar			Label used to display status-information.
	 * @param lblSave			Label as menu-item for file-save-operation.
	 */
	private static void menuActionSaveFile(Stage primaryStage, Label statusBar, Label lblSave) {
		if(activeImage.saveImageFile()){
			primaryStage.setTitle(activeImage.getImageFile().toString());
			statusBar.setText(Messages.getString("Main.fileSaved"));
			lblSave.setDisable(true);
		} else
			showInformationDialog(Messages.getString("Main.error"), Messages.getString("Main.errorFileNotSaved"));
	}



	/**
 	 * Specifies the lambda-expression for the menu-item 'save  file as'.
	 * 
	 * @param primaryStage		Stage. The primary stage of this program.
	 * @param statusBar			Label used to display status-information.
	 * @param lblSave			Label as menu-item for file-save-operation.
	 */
	private static void menuActionSaveFileAs(Stage primaryStage, Label statusBar, Label lblSave) {
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(new ExtensionFilter("PNG-Files", "*.png"));
		File file = chooser.showSaveDialog(primaryStage);
		if (file != null){
			String preCheck = file.getAbsoluteFile().toString();
			if (!preCheck.toLowerCase().endsWith(".png"))
					file = new File(preCheck + ".png");
			if(activeImage.saveImageFileAs(file)){
				activeImage = new WorkingImage(file);
				primaryStage.setTitle(file.toString());
				statusBar.setText(Messages.getString("Main.fileSaved"));
				lblSave.setDisable(true);
			}
		}
	}


	/**
 	 * Specifies the lambda-expression for the menu-item 'write watermark'.
	 * 
	 * @param primaryStage		Stage. The primary stage of this program.
	 * @param rootPane			BorderPane used as container for all ui-elements of this stage.
	 * @param statusBar			Label used to display status-information.
	 * @param lblSave			Label as menu-item for file-save-operation.
	 * @param lblSaveAs			Label as menu-item for file-save-as-operation.
	 * @param lblRead			Label as menu-item for read-watermark-operation.
	 * @param lblWrite			Label as menu-item for write-watermark-operation.
	 * @param lblErase			Label as menu-item for erase-watermark-operation.
	 */
	private static void menuActionWriteWatermark(Stage primaryStage, BorderPane rootPane, Label statusBar,
			Label lblSave, Label lblSaveAs, Label lblRead, Label lblWrite, Label lblErase) {

		TextInputDialog input = new TextInputDialog(Messages.getString("Main.watermark"));
		input.setTitle(Messages.getString("Main.watermarkInputTitle"));
		input.setHeaderText(Messages.getString("Main.watermarkInputHeader"));
		Optional<String> payload = input.showAndWait();
		payload.ifPresent(watermark -> {  
			if(activeImage.writeWatermark(watermark)){
				lblSave.setDisable	(false);
				lblSaveAs.setDisable(false);
				lblRead.setDisable	(false);
				lblErase.setDisable	(false);
				lblWrite.setDisable	(true);
				
				statusBar.setText(	Messages.getString("Main.watermarkSet") + 
									activeImage.getRedundancy() + 
									Messages.getString("Main.watermarkSetRedundancy"));
				
				primaryStage.setTitle(	activeImage.getImageFile().toString() + 
									Messages.getString("Main.statusUnsaved"));
				
				showLoadedImage(activeImage.getImageFile(), rootPane);
			} else {
				showInformationDialog (Messages.getString("Main.error"), Messages.getString("Main.errorWatermarkNotSet"));
			}
		});
	}



	/**
 	 * Specifies the lambda-expression for the menu-item 'remove watermark'.
	 * 
	 * @param primaryStage		Stage. The primary stage of this program.
	 * @param rootPane			BorderPane used as container for all ui-elements of this stage.
	 * @param statusBar			Label used to display status-information.
	 * @param lblSave			Label as menu-item for file-save-operation.
	 * @param lblSaveAs			Label as menu-item for file-save-as-operation.
	 * @param lblRead			Label as menu-item for read-watermark-operation.
	 * @param lblWrite			Label as menu-item for write-watermark-operation.
	 * @param lblErase			Label as menu-item for erase-watermark-operation.
	 */
	private static void menuActionRemoveWatermark(Stage primaryStage, BorderPane rootPane, Label statusBar,
			Label lblSave, Label lblSaveAs, Label lblRead, Label lblWrite, Label lblErase) {
		
		if(activeImage.removeWatermark()){
			lblSave.setDisable		(false);
			lblSaveAs.setDisable	(false);
			lblWrite.setDisable		(false);
			lblRead.setDisable		(true);
			lblErase.setDisable		(true);
			
			primaryStage.setTitle(activeImage.getImageFile().toString() + Messages.getString("Main.statusUnsaved"));
			statusBar.setText(Messages.getString("Main.watermarkRemoved"));
			showLoadedImage(activeImage.getImageFile(), rootPane);;
		}
	}



	/**
	 * Specifies the lambda-expression for the menu-item 'exit program'.
	 */
	private static void menuActionExitWatermarker() {
		if (activeImage == null || activeImage.isImageSaved())		
			System.exit(0);
		else {
			Alert unsaved = new Alert(AlertType.CONFIRMATION);
			unsaved.setTitle(Messages.getString("Main.unsavedChangesTitle"));
			unsaved.setHeaderText(Messages.getString("Main.unsavedChangesHeader"));
			unsaved.setContentText(Messages.getString("Main.closeAnyway"));
			Optional<ButtonType> result = unsaved.showAndWait();
			if (result.get() == ButtonType.OK)
				System.exit(0);
			else
				return;
		}
	}



	/**
	 * Prompts a standard dialog for informing the user.
	 * 
	 * @param head		Message header-text.
	 * @param content	Message content-text.
	 */
	private static void showInformationDialog(String head, String content){
		Alert message = new Alert(AlertType.INFORMATION);
		message.setHeaderText(head);
		message.setContentText(content);
		message.show();
	}
	
}
