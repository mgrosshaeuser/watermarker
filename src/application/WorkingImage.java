package application;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


/**
 * For each opened image-file an instance of WorkingImage (containing the actual image) is created.
 * 
 * @author Markus Grosshaeuser
 *
 */
public class WorkingImage {

	private static final String	WATERMARK_INDICATOR	  			= "start";
	
	private static final int 	STEPSIZE    					=     1;
	private static final int	INITIAL_EXTENDED_STEPSIZE 		=	  1;
	private static final int	INITIAL_WATERMARK_MAX_LENGTH 	= 	 30;
	
	private static final int	LENGTH_OF_BINARY_UNIT			= 	  8;
	private static final int	BASE_OF_BINARY_UNIT				=	  2;
	private static final int	MAX_EXPONENT_IN_BINARY_UNIT		=	  7;
	
	private static final int	MAX_PERMITTED_COLOR_VALUE		=	255;
	private static final int	BITSHIFT_ALPHA					= 	 24;
	private static final int	BITSHIFT_RED					= 	 16;
	private static final int	BITSHIFT_GREEN					= 	  8;
	
	
	
	private File 			imageFile;
	private BufferedImage 	image;

	private String 			watermark;
	
	private boolean 		imageSaved;
	private boolean			imageLoaded;
			
	private int				redundancy;
			
	private int 			extendedStepSize;
	private int 			wmMaxLength;
	

	

	public WorkingImage(File file){
		imageFile			= 	file;
		image 				=	loadImage();

		watermark			=	"";
		
		imageSaved			=	false;
		imageLoaded			= 	false;
		
		redundancy 			=	0;
		
		extendedStepSize 	= 	INITIAL_EXTENDED_STEPSIZE;
		wmMaxLength 		= 	INITIAL_WATERMARK_MAX_LENGTH;

		if (image != null){
			watermark 			=	readWatermark();
			imageSaved 	=	true;
			imageLoaded	= 	true;
		}
	}
	
	

		
	public boolean 	isImageSaved()				{		return imageSaved;	}
	public boolean 	isImageLoaded()				{		return imageLoaded;	}
		
	public String 	getEmbeddedWatermark()		{		return watermark;			}
	public File 	getImageFile()				{		return imageFile;			}
	public int 		getRedundancy()				{		return redundancy;			}
	
		

	public boolean saveImageFile(){
		try {
			ImageIO.write(image, "png", imageFile);
			imageSaved = true;
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	

	public boolean saveImageFileAs(File destination){
			try {	
			ImageIO.write(image, "png", destination);
			imageSaved = true;
		} 
		catch (IOException e) {
			return false;		
		}
		return true;
	}
	
	
	
	/**
	 * Writes a watermark to the image.
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
			

		ColorModel imgColorModel 	= 	image.getColorModel();
		WritableRaster imgRaster 	= 	image.getRaster();
		int imgRasterHeight 		= 	imgRaster.getHeight();
		int imgRasterWidth			= 	imgRaster.getWidth();
		
		redundancy = 0;
		int[] watermarkBinary = createWatermarkBinarySequence(payload);

		resetBlueByAddition(imgColorModel, imgRaster, imgRasterHeight, imgRasterWidth);
				
		writeDataToBlueChannel(imgColorModel, imgRaster, imgRasterHeight, imgRasterWidth, watermarkBinary);
	
		// Zur Überprüfung wird das Wasserzeichen ausgelsen.
		watermark = readWatermark();

		if (!watermark.equals("")){
			imageSaved = false;
			return true;
		} else {
			return false;
		}
	}



	/**
	 * Reads the watermark. The binary information from an image are extracted and decrypted using private methods. 
	 * 
	 * @return		The watermark as String.
	 */	
	public String readWatermark(){
		// Die Startmarkierung wird in Binärdarstellung übersetzt und zwischengespeichert.
		String binaryIndicator = "";
		for (int i = 0    ;    i < WATERMARK_INDICATOR.length()    ;    i++)
			binaryIndicator += characterToBinary(WATERMARK_INDICATOR.charAt(i));
		
		WritableRaster imgRaster	=	image.getRaster();
		int imgRasterHeight			=	imgRaster.getHeight();
		int imgRasterWidth			= 	imgRaster.getWidth();

		// Um Rechenzeit zu sparen ist die Zahl der zu untersuchenden Bildzeilen abhängig von der Bildgröße.
		extendedStepSize = (imgRasterHeight/100) + 1;

		int[][] binaryReadOut = new int[imgRasterHeight][imgRasterWidth];
			
		for (int y = 0    ;    y < imgRasterHeight    ;    y += STEPSIZE){
			for (int x = 0    ;    x < imgRasterWidth    ;    x += STEPSIZE){
				Color activePixel = new Color(image.getRGB(x, y), true);
				binaryReadOut[y][x] = (activePixel.getBlue()%2);
			}
		}

		return searchWatermark(binaryIndicator, binaryReadOut);
	}
	
	
	
	/**
	 * Erases the watermark by calling a private method for resetting all bits to zero (an even blue value).
	 * 
	 * @return		True or false depending on success.
	 */	
	public boolean removeWatermark(){
		// Relevante Bildwerte werden zwischengespeichert, damit weniger Methodenaufrufe in den Schleifen erfolgen.
		ColorModel imgColorModel 	=	image.getColorModel();
		WritableRaster imgRaster	=	image.getRaster();
		int imgRasterHeight			=	imgRaster.getHeight();
		int imgRasterWidth			= 	imgRaster.getWidth();

		
		resetBlueBySubstraction(imgColorModel, imgRaster, imgRasterHeight, imgRasterWidth);
		
	
		// Zur Überprüfung wird versucht ein Wasserzeichen auszulesen.
		watermark = readWatermark();
		
		if (watermark.equals("")){
			redundancy = 0;
			imageSaved = false;
			return true;
		}

		return false;
	}



	/**
	 * Sets the blue values of all pixels to a value v with v%2==0, by adding 1 where necessary.
	 * 
	 * @param imgColorModel		The ColorModel of the image.
	 * @param imgRaster			The Raster of the image.
	 * @param imgRasterHeight	The width of the image.
	 * @param imgRasterWidth	The height of the image.
	 */
	private void resetBlueByAddition(ColorModel imgColorModel, WritableRaster imgRaster, int imgRasterHeight,
			int imgRasterWidth) {
		
		for (int y = 0    ;    y < imgRasterHeight    ;    y += STEPSIZE){
			for (int x = 0    ;    x < imgRasterWidth    ;    x += STEPSIZE){
				Color activePixel = new Color(image.getRGB(x, y), true);
				int blue  =  activePixel.getBlue();
	
				if ( blue % 2  ==  1){
					if (blue < MAX_PERMITTED_COLOR_VALUE)
						blue++;
					else
						blue--;
					int newColor = ((activePixel.getAlpha()  <<  BITSHIFT_ALPHA)  |
									(activePixel.getRed()    <<  BITSHIFT_RED)  |
									(activePixel.getGreen()  <<  BITSHIFT_GREEN)  |
									 blue);
					imgRaster.setDataElements(x, y, imgColorModel.getDataElements(newColor, null));
				}
			}
		}
		image.setData(imgRaster);
	}



	/**
	 * Sets the blue values of all pixels to a value v with v%2==0, by subtracting 1 where necessary.
	 * 
	 * @param imgColorModel		The ColorModel of the image.
	 * @param imgRaster			The Raster of the image.
	 * @param imgRasterHeight	The width of the image.
	 * @param imgRasterWidth	The height of the image.
	 */
	private void resetBlueBySubstraction(ColorModel imgColorModel, WritableRaster imgRaster, int imgRasterHeight,
			int imgRasterWidth) {
		
		for (int y = 0    ;    y < imgRasterHeight    ;    y += STEPSIZE){
			for (int x = 0    ;    x < imgRasterWidth    ;    x += STEPSIZE){
				Color activePixel = new Color(image.getRGB(x, y), true);

				int blue  =  activePixel.getBlue();
				
				if (blue % 2  ==  1){
					blue--;
					
					int newColor = ((activePixel.getAlpha()  <<  BITSHIFT_ALPHA)  |  
									(activePixel.getRed()    <<  BITSHIFT_RED)  |  
									(activePixel.getGreen()  <<  BITSHIFT_GREEN)  | 
									 blue);
					
					imgRaster.setDataElements(x, y, imgColorModel.getDataElements(newColor, null));
				}
			}
		}
		image.setData(imgRaster);
	}
	
	
	
	/**
	 * Writes the given data to an image. Data has to be in binary form as an int-array.
	 * 
	 * @param imgColorModel		The ColorModel of the image.
	 * @param imgRaster			The Raster of the image.
	 * @param imgRasterHeight	The width of the image.
	 * @param imgRasterWidth	The height of the image.
	 * @param watermarkBinary	The int-Array holding the binary data.
	 */
	private void writeDataToBlueChannel(ColorModel imgColorModel, WritableRaster imgRaster, int imgRasterHeight,
			int imgRasterWidth, int[] watermarkBinary) {
		
		int currentBinary = 0;
	
		for (int y = 0    ;    y < imgRasterHeight    ;    y += STEPSIZE){
			for (int x = 0    ;    x < imgRasterWidth    ;    x += STEPSIZE){
				Color activePixel = new Color(image.getRGB(x, y), true);
	
				int blue  =  activePixel.getBlue();
	
				// Aufgrund der Vorbereitung kann hier eine einfache Addition erfolgen.
				blue += watermarkBinary[currentBinary];
	
				int newColor = ((activePixel.getAlpha()  <<  BITSHIFT_ALPHA)  | 
								(activePixel.getRed()    <<  BITSHIFT_RED)  | 
								(activePixel.getGreen()  <<  BITSHIFT_GREEN)  | 
								 blue);
				imgRaster.setDataElements(x, y, imgColorModel.getDataElements(newColor, null));
	
				if (currentBinary  <  watermarkBinary.length -1)
					currentBinary++;
				else{
					currentBinary = 0;
					redundancy++;
				}
	
			}
		}
		image.setData(imgRaster);
	}



	/**
	 * Packs a watermark-text into a binary sequence containing:
	 * (1) the start-indicator
	 * (2) the length of the payload and
	 * (3) the payload itself
	 * 
	 * @param payload	The watermark text.
	 * 
	 * @return			int[] holding the binary representation of the complete watermark.
	 */
	private int[] createWatermarkBinarySequence(String payload) {
		String watermarkTranslationString = "";
	
		for (int i = 0    ;    i < WATERMARK_INDICATOR.length()    ;    i++)
			watermarkTranslationString += characterToBinary(WATERMARK_INDICATOR.charAt(i));
	
		watermarkTranslationString += characterToBinary((char)(payload.length() * LENGTH_OF_BINARY_UNIT));
	
		for (int i = 0    ;    i < payload.length()    ;    i++)
			watermarkTranslationString += characterToBinary(payload.charAt(i));
	
		int watermarkBinary[] = new int[watermarkTranslationString.length()];
		for (int i = 0    ;    i < watermarkTranslationString.length()    ;    i++)
			watermarkBinary[i] = watermarkTranslationString.charAt(i)-'0';
		return watermarkBinary;
	}



	/**
	 * Loads the image from a given file.
	 * 	
	 * @return	The loaded image.
	 */
	private BufferedImage loadImage(){
		BufferedImage img;
		
		try {
			img = ImageIO.read(imageFile);
			imageLoaded = true;
			return img;
		} catch (IOException e) {
			imageLoaded = false;
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
		int availableSpace = width / LENGTH_OF_BINARY_UNIT - WATERMARK_INDICATOR.length() - 1;		
		if (availableSpace < wmMaxLength)
			return availableSpace;
		return wmMaxLength;		
	}
	
	
	
	/**
	 * Converts a character into binary representation.
	 * 
	 * @param character		A single character (or integer digit) for conversion into binary notation.
	 * 
	 * @return				A String representing the binary notation of a character with as one byte.
	 */
	private String characterToBinary(char character){
		char[] temp = {'0','0','0','0','0','0','0','0'};
		
		int characterDecimalValue = (int)character;
		
		for (int i = MAX_EXPONENT_IN_BINARY_UNIT    ;    i >= 0    ;    i--){
			temp[i] = (characterDecimalValue % 2 == 0)  ?  ('0')  :  ('1');
			characterDecimalValue /= BASE_OF_BINARY_UNIT;
		}
		
		String feedback = "";
		for (int i = 0    ;    i < LENGTH_OF_BINARY_UNIT    ;    i++){
			feedback += temp[i];
		}
		return feedback;
	}
	
	
	
	/**
	 * Converts a binary sequence to the corresponding character.
	 * 
	 * @param s		A string with size 8 representing one byte read from the watermark.
	 * 
	 * @return		The character that is represented by the given binary sequence.
	 */
	private char binaryToCharacter(String s){
		char[] binaryCharacter = s.toCharArray();
		
		int decimalValueOfCharacter = 0;
		for (int i = 0    ;    i < LENGTH_OF_BINARY_UNIT    ;    i++)
			decimalValueOfCharacter 	+= 	(binaryCharacter[i] - '0') * 
											 Math.pow(BASE_OF_BINARY_UNIT, (MAX_EXPONENT_IN_BINARY_UNIT-i));

		return (char) decimalValueOfCharacter;
	}


	
	/**
	 * Searchs for binary informations representing a watermark. 
	 * Reading is done using original and inverted bit-pattern and is stopped as soon as a 
	 * watermark-indicator is found.
	 * 
	 * @param binaryIndicator	The binary representation of the watermak start phrase.
	 * @param binaryReadOut		The two-dimensional int-array holding the binary data from an image.
	 * 
	 * @return		The watermark text read from the image after converting from binary to characters
	 */
	private String searchWatermark(String binaryIndicator, int[][] binaryReadOut){
		String readOutRegularForward = "";
		String readOutRegularBackward = "";
		String readOutInvertedForward = "";
		String readOutInvertedBackward = "";
		
		for (int y = 0    ;    y < binaryReadOut.length    ;    y += extendedStepSize){
			for (int x = 0    ;    x < binaryReadOut[y].length    ;    x += STEPSIZE){
				readOutRegularForward  	+= 	(binaryReadOut[y][x] == 0) ? '0' : '1';
				readOutInvertedForward 	+= 	(binaryReadOut[y][x] == 0) ? '1' : '0';
				readOutRegularBackward 	+= 	(binaryReadOut[y][binaryReadOut[y].length-1-x] == 0) ? '0' : '1';
				readOutInvertedBackward += 	(binaryReadOut[y][binaryReadOut[y].length-1-x] == 0) ? '1' : '0';
			}

			if (readOutRegularForward.contains(binaryIndicator))
				return decryptWatermark(binaryIndicator, readOutRegularForward);
			else if (readOutRegularBackward.contains(binaryIndicator))
				return decryptWatermark(binaryIndicator, readOutRegularBackward);
			else if (readOutInvertedForward.contains(binaryIndicator))
				return decryptWatermark(binaryIndicator, readOutInvertedForward);
			else if (readOutInvertedBackward.contains(binaryIndicator))
				return decryptWatermark(binaryIndicator, readOutInvertedBackward);
			
			readOutRegularForward 	= "";
			readOutRegularBackward	= "";
			readOutInvertedForward 	= "";
			readOutInvertedBackward	= "";
		}		
		return decryptWatermark(binaryIndicator, "");
	}

	

	/**
	 * Decrypts the binary information representing a watermark.
	 * First the start-indicator is located, then the length of the payload is read.
	 * The payload is translated from binary to character one character at a time.
	 * 
	 * @param binaryIndicator	The binary representation of the startmark.
	 * @param readOut			A String holding the binary data of one image row.
	 * 
	 * @return					The character representation of the binary watermark text.
	 */
	private String decryptWatermark(String binaryIndicator, String readOut) {
		int startOfPayload = 0;
		int sizeOfPayload  = 0;
	
		for (int i = 0    ;    i < readOut.length() - binaryIndicator.length() - 2    ;    i++){
			if (readOut.substring(i, (i+binaryIndicator.length())).equals(binaryIndicator)){
				sizeOfPayload = (int) binaryToCharacter(	readOut.substring(	i+binaryIndicator.length(), 
																				i+binaryIndicator.length()+8));
				startOfPayload = i + binaryIndicator.length() + 8;
				break;
			}
		}
		
				
		String watermark = "";
		
		if ( !(sizeOfPayload == 0)){
			for (int i = 0    ;    i < sizeOfPayload  &&  i < readOut.length()    ;    i += LENGTH_OF_BINARY_UNIT)
				watermark += binaryToCharacter(		readOut.substring(	startOfPayload + i, 
																		startOfPayload + i + LENGTH_OF_BINARY_UNIT));
		}
	
		return watermark;
	}


}
