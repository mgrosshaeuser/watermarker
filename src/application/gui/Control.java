package application.gui;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Optional;

import application.Main;

import javafx.animation.FadeTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;

public class Control {
	
	private static final double	MENU_MIN_OPACITY 		= 0.0;
	private static final double	MENU_MAX_OPACITY 		= 0.9;
	

	
	
	protected static void menuActionOpenFile() {
		File imageFile = null;
		if (isLossOfUnsavedChangesImpossible()    ||    isLossOfUnsavedChangesAccepted()){
			imageFile = chooseFileToOpen();
		}
		if (imageFile != null){
			Main.setActiveImage(imageFile);
			performPostActionUpdates();
		}
	}
		
		
	protected static void menuActionSaveFile() {
		Main.getActiveImage().saveImage(null);
		performPostActionUpdates();
	}
	
	
	protected static void menuActionSaveFileAs() {
		File imageFile = null;
		imageFile = chooseFileToSave();
		if (imageFile != null){
			Main.getActiveImage().saveImage(imageFile);
		}
		performPostActionUpdates();
	}
	
	
	protected static void menuActionReadWatermark() {
		showInformationDialog("Ausgelesenes Wasserzeichen", Main.getActiveImage().getWatermark());
	}
	
	
	protected static void menuActionWriteWatermark() {
		String watermark = getWatermarkTextFromDialog();
		if (watermark != null    &&    ! watermark.equals("")){
			Main.getActiveImage().writeWatermark(watermark);
		}
		performPostActionUpdates();
	}
	
	
	protected static void menuActionEraseWatermark() {
		Main.getActiveImage().eraseWatermark();
		performPostActionUpdates();
	}
	
	
	protected static void menuActionExitProgram() {
		if (isLossOfUnsavedChangesImpossible()    ||    isLossOfUnsavedChangesAccepted())
			System.exit(0);
	}
	
	
	
	
	protected static void makeMenuFadeIn(GridPane primaryMenuPane) {
			FadeTransition fadeIn = new FadeTransition(Duration.millis(100), primaryMenuPane);
			fadeIn.setFromValue(MENU_MIN_OPACITY);
			fadeIn.setToValue(MENU_MAX_OPACITY);
			fadeIn.play();
	}


	protected static void makeMenuFadeOut(GridPane primaryMenuPane) {
			FadeTransition fadeOut = new FadeTransition(Duration.millis(200), primaryMenuPane);
			fadeOut.setFromValue(MENU_MAX_OPACITY);
			fadeOut.setToValue(MENU_MIN_OPACITY);
			fadeOut.play();
	}

	
	

	protected static void setMenuItemHighlighted(boolean status, Label menuItem) {
		String statusLabel = "";
		
		if (status == true){
			statusLabel = "menuLabelMouseOver";
		} else {
			statusLabel = "menuLabel";
		}
		
		menuItem.setId(statusLabel);
	}

	
	

	protected static void setPrimaryStageDefaultClosingOperation() {
		if (isLossOfUnsavedChangesImpossible()    ||    isLossOfUnsavedChangesAccepted()){		
			System.exit(0);
		}
	}


	
	
	protected static void setBackgroundImage(BorderPane primaryGuiContainer) {
		String source 	= "";
		
		try {
			source = Main.getActiveImage().getImageFile().toURI().toURL().toString();
		} catch (MalformedURLException e) {
			return;
		}
		
		BackgroundImage backImage = customizeImageForDisplay(source, primaryGuiContainer);
		primaryGuiContainer.setBackground(new Background(backImage));
	}


	private static BackgroundImage customizeImageForDisplay(String imageSource, BorderPane gui) {
		Image image = new Image(imageSource);
		
		if (image.getHeight() > gui.getHeight()   ||    image.getWidth() > gui.getWidth())
			image = new Image(imageSource, gui.getWidth(), gui.getHeight(), true, true);
	
		BackgroundImage backImage = new BackgroundImage(	
				image, 	BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
						BackgroundPosition.CENTER, BackgroundSize.DEFAULT	);
		
		return backImage;
	}


	
	
	private static void performPostActionUpdates() {
		updateWindowTitleAndStatusBar();
		PrimaryWindow.showLoadedImage();
		
		PrimaryMenu menu = PrimaryWindow.getMenu();
		menu.getMenuItemSaveFile().setDisable(Main.getActiveImage().isImageSaved());
		menu.getMenuItemSaveFileAs().setDisable(! Main.getActiveImage().isImageLoaded());
		menu.getMenuItemReadMark().setDisable(! Main.getActiveImage().isImageWatermarked());
		menu.getMenuItemWriteMark().setDisable(Main.getActiveImage().isImageWatermarked());
		menu.getMenuItemEraseMark().setDisable(! Main.getActiveImage().isImageWatermarked());	
	}
		
	
	private static void updateWindowTitleAndStatusBar() {
		String newWindowTitle= "";
		newWindowTitle += PrimaryWindow.WINDOW_TITLE;
		newWindowTitle += " - ";
		newWindowTitle += Main.getActiveImage();
		if (! Main.getActiveImage().isImageSaved()){
			newWindowTitle += " <<< unsaved >>>";
		}
		
		PrimaryWindow.getPrimaryStage().setTitle(newWindowTitle);
		PrimaryWindow.getStatusBar().setText(Main.getActiveImage().getImageStatus());
	}


	
	
	private static File chooseFileToOpen() {
		FileChooser imageFileChooser = getFileChooserDialog(); 
		File selectedFile = imageFileChooser.showOpenDialog(null);
		return selectedFile;
	}
	

	private static File chooseFileToSave() {
		FileChooser imageFileChooser = getFileChooserDialog();
		File selectedFile = imageFileChooser.showSaveDialog(null);
		return selectedFile;
	}
	
	
	private static FileChooser getFileChooserDialog() {
		FileChooser imageFileChooser = new FileChooser();
		imageFileChooser.getExtensionFilters().add(new ExtensionFilter("PNG_Files", "*.png", "*.PNG"));
		imageFileChooser.getExtensionFilters().add(new ExtensionFilter("BMP-Files", "*.bmp", "*.BMP"));
		return imageFileChooser;
	}
	
	
	
	
	
	private static boolean isLossOfUnsavedChangesImpossible() {
		boolean noImageOpen = Main.getActiveImage() == null;
		boolean noUnsavedChanges;
		if (noImageOpen){
			noUnsavedChanges = true;
		} else {
			noUnsavedChanges = Main.getActiveImage().isImageSaved();
		}
		return noImageOpen  ||  noUnsavedChanges;
	}


	private static boolean isLossOfUnsavedChangesAccepted() {
		Alert unsavedChangesAlert = new Alert(AlertType.CONFIRMATION);
		unsavedChangesAlert.setTitle("Nicht gespeicherte Änderungen.");
		unsavedChangesAlert.setHeaderText("Nicht gespeicherte Änderungen gehen verloren.");
		unsavedChangesAlert.setContentText("Wollen Sie dennoch fortfahren?");
		Optional<ButtonType> userDecision = unsavedChangesAlert.showAndWait();
		return (userDecision.get()  ==  ButtonType.OK);
	}


	
	
	private static String getWatermarkTextFromDialog() {
		TextInputDialog input = new TextInputDialog("Eingabe");
		input.setTitle("Eingabe");
		input.setHeaderText("Bitte Wasserzeichen eingeben.");
		Optional<String> payload = input.showAndWait();
		String watermark = "";
		if (payload.isPresent())
			watermark = payload.get();
		return watermark;
	}


	private static void showInformationDialog(String head, String content) {
		Alert message = new Alert(AlertType.INFORMATION);
		message.setHeaderText(head);
		message.setContentText(content);
		message.show();
	}

}
