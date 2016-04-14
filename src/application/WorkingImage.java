package application;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


/**
 * For each opened image an instance of WorkingImage (containing the actual image) is created.
 * 
 * @author Markus Grosshaeuser
 *
 */
public class WorkingImage {

	private final String	watermarkIndicator	  		= "start";
	
	private final int 		stepSize    				=     1;
	private final int		initialExtendesStepSize 	=	  1;
	private final int		initialWatermarkMaxLength 	= 	 30;
	
	private final int		lengthOfBinaryUnit			= 	  8;
	private final int		baseOfBinaryUnit			=	  2;
	private final int		maxExponentInBinaryUnit		=	  7;
	
	private final int		maxPermittedColorValue		=	255;
	private final int		bitshiftAlpha				= 	 24;
	private final int		bitshiftRed					= 	 16;
	private final int		bitshiftGreen				= 	  8;
	
	
	
	private File 			imageFile;
	private BufferedImage 	image;

	private String 			watermark;
	
	private boolean 		statusImageSaved;
	private boolean			statusImageLoaded;
			
	private int				redundancy;
			
	private int 			extendedStepSize;
	private int 			wmMaxLength;
	

	

	public WorkingImage(File file){
		imageFile			= 	file;
		image 				=	loadImage();

		watermark			=	"";
		
		statusImageSaved	=	false;
		statusImageLoaded	= 	false;
		
		redundancy 			=	0;
		
		extendedStepSize 	= 	initialExtendesStepSize;
		wmMaxLength 		= 	initialWatermarkMaxLength;

		if (image != null){
			watermark 			=	readWatermark();
			statusImageSaved 	=	true;
			statusImageLoaded	= 	true;
		}
	}
	
	

	
	
	
	public boolean 	getStatusImageSaved()		{		return statusImageSaved;	}
	public boolean 	getStatusImageLoaded()		{		return statusImageLoaded;	}
		
	public String 	getEmbeddedWatermark()		{		return watermark;			}
	public File 	getImageFile()				{		return imageFile;			}
	public int 		getRedundancy()				{		return redundancy;			}
	
		

	public boolean saveImageFile(){
		try {
			ImageIO.write(image, "png", imageFile);
			statusImageSaved = true;
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	

	public boolean saveImageFileAs(File destination){
			try {	
			ImageIO.write(image, "png", destination);
			statusImageSaved = true;
		} 
		catch (IOException e) {
			return false;		
		}
		return true;
	}
	
	

	
	
	
/**
 * Method for writing the watermark to a given image.
 * 
 * @param payload	The text of the watermark to be written.
 * 
 * @return			True or false, depending on success.
 */	

	public boolean writeWatermark(String payload) {
		if (payload == null || payload == "")
			return false;
		
		if (payload.length() > calculateWatermarkMaxLength())
			return false;
			

		// Relevante Bildwerte werden zwischengespeichert, damit weniger Methodenaufrufe in den Schleifen erfolgen.
		ColorModel imgColorModel 	= 	image.getColorModel();
		WritableRaster imgRaster 	= 	image.getRaster();
		int imgRasterHeight 		= 	imgRaster.getHeight();
		int imgRasterWidth			= 	imgRaster.getWidth();
		
		redundancy = 0;
		String watermarkTranslationString = "";

		// Die Startmarke wird in Binärdarstellung übersetzt und dem String hinzugefügt.
		for (int i = 0    ;    i < watermarkIndicator.length()    ;    i++)
			watermarkTranslationString += characterToBinary(watermarkIndicator.charAt(i));

		// Die Länge des eingegebenen Textes wird in Binärdarstellung übersetzt und dem String hinzugefügt.
		watermarkTranslationString += characterToBinary((char)(payload.length() * lengthOfBinaryUnit));

		// Der Text wird in Binärdarstellung übersetzt und dem String hinzugefügt.
		for (int i = 0    ;    i < payload.length()    ;    i++)
			watermarkTranslationString += characterToBinary(payload.charAt(i));

		
		// Der Binärstring wird in ein int-Array umgewandelt.
		int watermarkBinary[] = new int[watermarkTranslationString.length()];
		for (int i = 0    ;    i < watermarkTranslationString.length()    ;    i++)
			watermarkBinary[i] = watermarkTranslationString.charAt(i)-'0';

		
		// In einem ersten Durchlauf werden alle ungeraden Blauwerte 'begradigt'.
		for (int y = 0    ;    y < imgRasterHeight    ;    y += stepSize){
			for (int x = 0    ;    x < imgRasterWidth    ;    x += stepSize){
				// Für jeden Pixel wird die Farbe vom Typ Color ermittelt und der Blauwert isoliert.
				Color activePixel = new Color(imgColorModel.getRGB(imgRaster.getDataElements(x, y, null)));
				int blue  =  activePixel.getBlue();

				// Der Blau-Wert wird manipuliert, sofern nötig.
				if ( blue % 2  ==  1){
					if (blue < maxPermittedColorValue)
						blue++;
					else
						blue--;
					// Mit dem manipulieten Blau-Wert wird eine neue Farbe erzeugt und dem Pixel zugewiesen.
					int newColor = ((activePixel.getAlpha()  <<  bitshiftAlpha)  |
									(activePixel.getRed()    <<  bitshiftRed)  |
									(activePixel.getGreen()  <<  bitshiftGreen)  |
									 blue);
					imgRaster.setDataElements(x, y, imgColorModel.getDataElements(newColor, null));
				}
			}
		}
		// Die Änderungen werden in das Bild geschrieben.
		image.setData(imgRaster);

		
		// Im zweiten Durchgang werden die Informationen in die Blau-Werte der Pixel geschrieben.
		
		// Zähler für die aktuell zu schreibende Binärziffer.
		int currentBinary = 0;

		// Im zweiten Durchgang wird das Wasserzeichen geschrieben.
		for (int y = 0    ;    y < imgRasterHeight    ;    y += stepSize){
			for (int x = 0    ;    x < imgRasterWidth    ;    x += stepSize){
				Color activePixel = new Color(imgColorModel.getRGB(imgRaster.getDataElements(x, y, null)));
				int blue  =  activePixel.getBlue();

				// Aufgrund der Vorbereitung kann hier eine einfache Addition erfolgen.
				blue += watermarkBinary[currentBinary];

				int newColor = ((activePixel.getAlpha()  <<  bitshiftAlpha)  | 
								(activePixel.getRed()    <<  bitshiftRed)  | 
								(activePixel.getGreen()  <<  bitshiftGreen)  | 
								 blue);
				imgRaster.setDataElements(x, y, imgColorModel.getDataElements(newColor, null));

				// Der Zähler wird nach einem erfolgreichen Schreibzyklus wieder auf null gestellt (Redundanz).
				if (currentBinary  <  watermarkBinary.length -1)
					currentBinary++;
				else{
					currentBinary = 0;
					// Jeder Abgeschlossene Schreibzyklus wird gezählt.
					redundancy++;
				}

			}
		}
		// Die Änderungen werden in das Bild geschrieben.
		image.setData(imgRaster);
	
		// Zur Überprüfung wird das Wasserzeichen ausgelsen.
		watermark = readWatermark();

		if (!watermark.equals("")){
			statusImageSaved = false;
			return true;
		} else {
			return false;
		}
	}
	
	
	

	
	
/**
 * Method for reading the watermark. The binary information from an image are extracted and decrypted using the
 * private decryptBinary() method. If the the first attempt returns an empty string, the informations are vertically
 * mirrored and decryptBinary() is called a second time.
 * 
 * @return		The watermark as String.
 */	
	
	public String readWatermark(){
		// Die Startmarkierung wird in Binärdarstellung übersetzt und zwischengespeichert.
		String binaryIndicator = "";
		for (int i = 0    ;    i < watermarkIndicator.length()    ;    i++)
			binaryIndicator += characterToBinary(watermarkIndicator.charAt(i));
		
		// Relevante Bildwerte werden zwischengespeichert, damit weniger Methodenaufrufe in den Schleifen erfolgen.
		ColorModel imgColorModel 	=	image.getColorModel();
		WritableRaster imgRaster	=	image.getRaster();
		int imgRasterHeight			=	imgRaster.getHeight();
		int imgRasterWidth			= 	imgRaster.getWidth();

		// Um Rechenzeit zu sparen ist die Zahl der zu untersuchenden Bildzeilen abhängig von der Bildgröße.
		extendedStepSize = (imgRasterHeight/100) + 1;

		// int-Array um die Binärdaten des gesamtes Bildes zu speichern.
		int[][] binaryReadOut = new int[imgRasterHeight][imgRasterWidth];
	
		String readOut = "";
			
		// Die Binärdaten werden aus den Blauwerten ausgelesen und im oben angelegten int-Array zwischengespeichert. 
		for (int y = 0    ;    y < imgRasterHeight    ;    y += stepSize){
			for (int x = 0    ;    x < imgRasterWidth    ;    x += stepSize){
				Color activePixel =  new Color(imgColorModel.getRGB(imgRaster.getDataElements(x, y, null)));
				binaryReadOut[y][x] = (activePixel.getBlue()%2);
			}
		}

		// Die Methode decryptBinary() wird gerufen um die Binärdaten auf ein Wasserzeichen hin zu untersuchen.
		readOut = searchAndDecryptWatermark(binaryIndicator, binaryReadOut);
		
		
		// Wenn beim ersten Aufruf von decryptBinary() kein Wasserzeichen gefunden wird, wird das Array vertikal
		// gespiegelt um eine ggf. erfolgte Spiegelung oder Rotation um 180 Grad des Bildes auszugleichen.
		if (readOut == ""){
			int retry[][] = new int[imgRasterHeight][imgRasterWidth];
			for (int y = 0    ;    y < imgRasterHeight    ;    y += stepSize){
				for (int x = 0    ;    x < imgRasterWidth    ;    x += stepSize){
					retry[y][x] = binaryReadOut[y][imgRasterWidth-1-x];
				}
			}
			// decryptBinary() wird mit dem neu angelegten (gespiegelten) Array erneut aufgerufen.
			readOut = searchAndDecryptWatermark(binaryIndicator, retry);
		}
		
		// Die Zeichenkette (Wasserzeichen oder ein leerere String" wird zurückgegeben.
		return readOut;
	}
	
	
	
	
	
/**
 * Method for removing the watermark by resetting all bits to zero (an even blue value).
 * 
 * @return		True or false depending on success.
 */	
	public boolean removeWatermark(){
		// Relevante Bildwerte werden zwischengespeichert, damit weniger Methodenaufrufe in den Schleifen erfolgen.
		ColorModel imgColorModel 	=	image.getColorModel();
		WritableRaster imgRaster	=	image.getRaster();
		int imgRasterHeight			=	imgRaster.getHeight();
		int imgRasterWidth			= 	imgRaster.getWidth();

		
		for (int y = 0    ;    y < imgRasterHeight    ;    y += stepSize){
			for (int x = 0    ;    x < imgRasterWidth    ;    x += stepSize){
				// Für jedes Pixel wird der Blauwert isoliert.
				Color activePixel = new Color(imgColorModel.getRGB(imgRaster.getDataElements(x, y, null)));
				int blue  =  activePixel.getBlue();
				
				// Ist der Blauwert nicht durch zwei teilbar, wird 1 subtrahiert.
				if (blue % 2  ==  1){
					blue--;
					
					int newColor = ((activePixel.getAlpha()  <<  bitshiftAlpha)  |  
									(activePixel.getRed()    <<  bitshiftRed)  |  
									(activePixel.getGreen()  <<  bitshiftGreen)  | 
									 blue);
					
					imgRaster.setDataElements(x, y, imgColorModel.getDataElements(newColor, null));
				}
			}
		}
		
		// Die Änderungen werden in das Bild geschrieben.
		image.setData(imgRaster);

		// Zur Überprüfung wird versucht ein Wasserzeichen auszulesen.
		watermark = readWatermark();
		
		if (watermark.equals("")){
			redundancy = 0;
			statusImageSaved = false;
			return true;
		}

		return false;
	}
	
	
	
	
/**
 * Loading the image from a given file.
 * 	
 * @return	The loaded image.
 */
	
	private BufferedImage loadImage(){
		BufferedImage img;
		
		try {
			img = ImageIO.read(imageFile);
			statusImageLoaded = true;
			return img;
		} catch (IOException e) {
			statusImageLoaded = false;
			return null;
		}
	}
	
	
/**
 * Returns the maximum number of characters available for the watermark.	
 * 
 * @return	Maximum number of characters.
 */
	
	
	private int calculateWatermarkMaxLength(){
		int width = image.getRaster().getWidth();
		int availableSpace = width / lengthOfBinaryUnit - watermarkIndicator.length() - 1;		
		if (availableSpace < wmMaxLength)
			return availableSpace;
		return wmMaxLength;		
	}
	
	
	

/**
 * Helper method for converting a character into binary representation.
 * 
 * @param character		A single character (or integer digit) for conversion into binary notation.
 * 
 * @return				A String representing the binary notation of a character with as one byte.
 */
	private String characterToBinary(char character){
		char[] temp = {'0','0','0','0','0','0','0','0'};
		
		int characterDecimalValue = (int)character;
		
		for (int i = maxExponentInBinaryUnit    ;    i >= 0    ;    i--){
			temp[i] = (characterDecimalValue % 2 == 0)  ?  ('0')  :  ('1');
			characterDecimalValue /= baseOfBinaryUnit;
		}
		
		String feedback = "";
		for (int i = 0    ;    i < lengthOfBinaryUnit    ;    i++){
			feedback += temp[i];
		}
		return feedback;
	}
	
	
	
	
	
/**
 * Helper method for converting a binary sequence to the corresponding character.
 * 
 * @param s		A string with size 8 representing one byte read from the watermark.
 * 
 * @return		The character that is represented by the given binary sequence.
 */
	private char binaryToCharacter(String s){
		char[] binaryCharacter = s.toCharArray();
		
		int decimalValueOfCharacter = 0;
		for (int i = 0    ;    i < lengthOfBinaryUnit    ;    i++)
			decimalValueOfCharacter 	+= 	(binaryCharacter[i] - '0') * 
											 Math.pow(baseOfBinaryUnit, (maxExponentInBinaryUnit-i));

		return (char) decimalValueOfCharacter;
	}

	
	

	
/**
 * Method for the decryption of binary informations to a character string. 
 * 
 * @param binaryIndicator	The binary representation of the watermak start phrase.
 * @param binaryReadOut		The two-dimensional int-array holding the binary data from an image.
 * 
 * @return		The watermark read from the image after converting from binary to characters
 */
	private String searchAndDecryptWatermark(String binaryIndicator, int[][] binaryReadOut){
		String readOut = "";
		String readOutRegular = "";
		String readOutInverted = "";
		
		for (int y = 0    ;    y < binaryReadOut.length    ;    y += extendedStepSize){
			for (int x = 0    ;    x < binaryReadOut[y].length    ;    x += stepSize){
				// Das Bitmuster der ersten Bildzeile wie gelesen. (Keine oder geradzahlige Farbänderungen).
				readOutRegular     +=    (binaryReadOut[y][x] == 0) ? '0' : '1';
				// Das Bitmuster der ersten Bildzeile, jedoch invertiert. (Ungeradzahlige Farbänderungen).
				readOutInverted    +=    (binaryReadOut[y][x] == 0) ? '1' : '0';
			}

			// Wird in Bildzeile y eine Startmarke gefunden, wird der Rest des Bildes nicht überprüft.
			if (readOutRegular.contains(binaryIndicator)){
				readOut = readOutRegular;
				break;
			} else if (readOutInverted.contains(binaryIndicator)){
				readOut = readOutInverted;
				break;
			} else {
				readOutRegular  = "";
				readOutInverted = "";
			}
		}		

		
		// Ist das Ergebnis nicht leer, wird der Startindex des Textes ermittelt und seine Länge ausgelesen.

		int startOfPayload = 0;
		int sizeOfPayload  = 0;

		for (int i = 0    ;    i < readOut.length() - binaryIndicator.length() - 2    ;    i++){
			// Zunächst wird die Startmarkierung gesucht.
			if (readOut.substring(i, (i+binaryIndicator.length())).equals(binaryIndicator)){
				// Auf die Startmarkierung folgt die Länge des Textes.
				sizeOfPayload = (int) binaryToCharacter(	readOut.substring(	i+binaryIndicator.length(), 
																				i+binaryIndicator.length()+8));
				
				// Nach der Längenangabe folgt der Text.
				startOfPayload = i + binaryIndicator.length() + 8;
				break;
			}
		}
		
		
		// Ist das Ergebnis nicht leer, werden die Binärdaten mittels der binaryToCharacter()-Methode übersetzt.
		
		String watermark = "";
		
		if ( !(sizeOfPayload == 0)){
			for (int i = 0    ;    i < sizeOfPayload  &&  i < readOut.length()    ;    i += lengthOfBinaryUnit)
				watermark += binaryToCharacter(		readOut.substring(	startOfPayload + i, 
																		startOfPayload + i + lengthOfBinaryUnit));
		}

		return watermark;
	}


}
