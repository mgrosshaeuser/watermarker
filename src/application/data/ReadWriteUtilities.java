package application.data;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

public class ReadWriteUtilities {
	public static final String  WATERMARK_INDICATOR           = "start";
	public static final int	    INITIAL_WATERMARK_MAX_LENGTH  =      30;
	public static final int     STEPSIZE                      =       1;
	public static final int	    INITIAL_EXTENDED_STEPSIZE     =       1;
	
	public static final int	    LENGTH_OF_BINARY_UNIT         =       8;
	public static final int	    BASE_OF_BINARY_UNIT           =       2;
	public static final int	    MAX_EXPONENT_IN_BINARY_UNIT   =       7;
	
	public static final int	    BITSHIFT_ALPHA                =      24;
	public static final int	    BITSHIFT_RED                  =      16;
	public static final int	    BITSHIFT_GREEN                =       8;
	
	
	private static int             extendedStepSize;
	private static String          binaryIndicator;
	private static BufferedImage   image;
	private static int             redundancy;
	
	private static ColorModel      imageColorModel;
	private static WritableRaster  imageRaster;
	private static int             imageHeight;
	private static int             imageWidth;
	
	
	
	private ReadWriteUtilities(){}
	
	
	/**
	 * Returns the number of redundant writes.
	 * 
	 * @return	Number of redundant writes.
	 */
	public static int getRedundancy(){
		return redundancy;
	}
	
	
	
	/**
	 * Performs all necessary method-calls to write a given watermark to a given image.
	 * 
	 * @param watermark		The watermark-payload as string.
	 * @param image			The image in which to place the watermark.
	 * 
	 * @return				The manipulated image containing the watermark.
	 */
	public static BufferedImage writeWatermarkToImage(String watermark, BufferedImage image) {
		try {
			updateImageSpecifications(image);
			int[] watermarkBinary = createWatermarkBinarySequence(watermark);
			resetBlueValues();
			writeDataToBlueChannel(watermarkBinary);
			return image;
		} catch (Exception e) {
			return null;
		}
	}
	
	
	/**
	 * Performs all necessary method-calls to read a watermark from an given image.
	 * 
	 * @param image			The image possibly containing a watermark.
	 * 
	 * @return				Payload of a watermark if present or NULL.
	 */
	public static String readWatermarkFromImage(BufferedImage image) {
		try {
			updateImageSpecifications(image);
			extendedStepSize = (imageHeight / 100) + 1;
			binaryIndicator = translateToBinaryString(WATERMARK_INDICATOR);
			int[][] binaryRawData = readBinaryData();
			String binaryReadout = searchInBinaryData(binaryRawData);
			String binaryWatermark = isolateWatermark(binaryReadout);
			String watermark = translateToReadableString(binaryWatermark);
			return watermark;
		} catch (Exception e) {
			return null;
		}
	}


	/**
	 * Performs all necessary actions to remove a watermark from a given image.
	 * 
	 * @param image			The image from which to remove a watermark.
	 * 
	 * @return				The image without watermark.
	 */
	public static boolean eraseWatermarkFromImage(BufferedImage image) {
		try {
			updateImageSpecifications(image);	
			resetBlueValues();
			return true;
		} catch (Exception e) {
			return false;
		}
	}



	/**
	 * Creating a watermark by concatenating the binary representations of the watermark-indicator,
	 * the length of the payload and the payload itself, to a single string. 
	 * That String is converted to an integer-array, to ease the writing-process.
	 * 
	 * @param payload		The watermark-payload.
	 * 
	 * @return				The watermark as integer-array.
	 */
	private static int[] createWatermarkBinarySequence(String payload) {
		String watermarkTranslationString = "";
		watermarkTranslationString += translateToBinaryString(WATERMARK_INDICATOR);
		watermarkTranslationString += characterToBinary((char) (payload.length() * LENGTH_OF_BINARY_UNIT));
		watermarkTranslationString += translateToBinaryString(payload);
		
		int watermarkBinary[] = new int[watermarkTranslationString.length()];
		for (int i = 0    ;    i < watermarkTranslationString.length()    ;    i++){
			watermarkBinary[i] = watermarkTranslationString.charAt(i) - '0';
		}
		
		return watermarkBinary;
	}
	
	
	/**
	 * The actual process of writing bits to the blue channel of an image.
	 * A counter for iterating through the binary data is defines, and the number of redundancies is set to zero.
	 * Each pixel is accessed using two intertwined for-loops. For each pixel the color-value is extracted, and 
	 * from that the blue-value is isolated. A new color is created by adding the element of the binary-array 
	 * corresponding to the loop-cycle to the blue-value. That new Color is written to the image-raster.
	 * For redundant writing the binary-counter is set back to zero each time a full writing-cycle is completed. 
	 * After manipulating all pixels the image-raster is written to the image.
	 * 
	 * @param binaries		An integer-array holding the watermark as a binary sequence.
	 * 
	 * @throws Exception	Unspecified Exception in case something goes wrong.
	 */
	private static void writeDataToBlueChannel(int[] binaries) throws Exception {
		int currentBinary = 0;
		redundancy = 0;
		
		for (int y = 0    ;    y < imageHeight    ;    y += STEPSIZE){
			for (int x = 0    ;    x < imageWidth    ;    x += STEPSIZE){
				Color currentPixel = new Color(image.getRGB(x, y), true);
				int blue = currentPixel.getBlue() + binaries[currentBinary];
				int newColor = makeColorWithNewBlueValue(currentPixel, blue);
				imageRaster.setDataElements(x, y, imageColorModel.getDataElements(newColor, null));
	
				if (currentBinary  <  binaries.length -1){
					currentBinary++;
				} else {
					currentBinary = 0;
					redundancy++;
				}
			}
		}
		
		image.setData(imageRaster);
	}
	
	
	/**
	 * A two-dimensional integer-array of the same dimensions as the image is created. Therefore each pixel of 
	 * the image gets a corresponding array-element.
	 * Each pixel is accessed through two intertwined for-loops. Each array-element receives the remainder of
	 * the integer-division of the blue-value by 2. 
	 * 
	 * @return		A two-dimensional integer-array holding the binary-data read from an image.
	 */
	private static int [][] readBinaryData() {
		int binaryReadout[][] = new int[imageHeight][imageWidth];
		
		for (int y = 0    ;    y < imageHeight    ;    y += STEPSIZE){
			for (int x = 0    ;    x < imageWidth    ;    x += STEPSIZE){
				Color activePixel = new Color(image.getRGB(x, y), true);
				binaryReadout[y][x] = (activePixel.getBlue() % 2);
			}
		}
		
		return binaryReadout;
	}
	
		
	/**
	 * Searching the binary raw-data, that were read from an image, for the appearance of a watermark-indicator.
	 * Four strings are defined. 
	 * The bits of one array-row are added to all four strings, which are then searched for the appearance of 
	 * a watermark-indicator. If one containts a watermark-indicator, that string is returned. Otherwise the
	 * string is emptied and the process continues until the the last array-row.
	 * Although all four string contain the same row, their content is different:
	 * >> readOutRegularForward contains the bits from left to right as read from the array.
	 * >> readOutRegularBackward contains the bits from right to left as read from the array.
	 * >> readOutInvertedForward contains the bits from left to right, but inverted (0 becomes 1 and vice versa).
	 * >> readOutInvertedBackward contains the bits from right to left, but inverted.
	 * 
	 * @param binaryRawData		The binary raw-data read by the method readBinaryData().
	 * 
	 * @return			A String containing the watermark (if present) or an empty String.
	 */
	private static String searchInBinaryData(int[][] binaryRawData) {
		String readOutRegularForward = "";
		String readOutRegularBackward = "";
		String readOutInvertedForward = "";
		String readOutInvertedBackward = "";
		
		for (int y = 0    ;    y < binaryRawData.length    ;    y += extendedStepSize){
			for (int x = 0    ;    x < binaryRawData[y].length    ;    x += STEPSIZE){
				readOutRegularForward  	+= 	(binaryRawData[y][x] == 0) ? '0' : '1';
				readOutInvertedForward 	+= 	(binaryRawData[y][x] == 0) ? '1' : '0';
				readOutRegularBackward 	+= 	(binaryRawData[y][binaryRawData[y].length-1-x] == 0) ? '0' : '1';
				readOutInvertedBackward += 	(binaryRawData[y][binaryRawData[y].length-1-x] == 0) ? '1' : '0';
			}

			if (readOutRegularForward.contains(binaryIndicator))
				return readOutRegularForward;
			else if (readOutRegularBackward.contains(binaryIndicator))
				return readOutRegularBackward;
			else if (readOutInvertedForward.contains(binaryIndicator))
				return readOutInvertedForward;
			else if (readOutInvertedBackward.contains(binaryIndicator))
				return readOutInvertedBackward;
			
			readOutRegularForward 	= "";
			readOutRegularBackward	= "";
			readOutInvertedForward 	= "";
			readOutInvertedBackward	= "";
		}		
		return "";
	}
	
	
	/**
	 * Isolating the watermark from a watermark-containing string found by searchInBinaryData().
	 * Variables for the start-index and the length of a payload are defined. 
	 * Variables for the start- and stop-index of the payload-length information are defined.
	 * A for-loop iterated bit by bit over the given string, searching for the watermark-indicator. Using 
	 * the watermark-indicator, the location of the size-information is determined. From there, the
	 * start-index and the size of the watermark-payload within the String is determined, and the end-index
	 * of the payload is calculated.
	 * If a payload is present, the isolated payload is returned. 
	 * 
	 * @param readOut	A String holding the binary data of a single image-row.
	 * 
	 * @return			The watermark payload (if present) or an empty String.
	 */
	private static String isolateWatermark(String readOut) {
		int startOfPayload 	= 0;
		int sizeOfPayload  	= 0;
		int unitStart 		= binaryIndicator.length();
		int unitStop 		= binaryIndicator.length() + LENGTH_OF_BINARY_UNIT;
		
		for (int i = 0    ;    i < readOut.length() - unitStart - 1    ;    i++){
			if (readOut.substring(i, (i + unitStart)).equals(binaryIndicator)){
				sizeOfPayload = (int) binaryToCharacter(readOut.substring(i + unitStart, i + unitStop));
				startOfPayload = i + unitStop;
				break;
			}
		}
						
		int endOfPayload = startOfPayload + sizeOfPayload + LENGTH_OF_BINARY_UNIT;		
		
		if ( !(sizeOfPayload == 0)){
			return readOut.substring(startOfPayload, endOfPayload);
		} else {
			return "";
		}
	}
	

	
	/**
	 * Setting the blue values of the image to values representing binary zeros.
	 * Each pixel is accessed through two intertwined for-loops. The blue-value of each pixel is isolated and,
	 * in case of an odd value, decreased by 1; the even blue-values remain.
	 */
	private static void resetBlueValues() {
		for (int y = 0    ;    y < imageHeight    ;    y += STEPSIZE){
			for (int x = 0    ;    x < imageWidth    ;    x += STEPSIZE){
				Color activePixel = new Color(image.getRGB(x, y), true);
				int blue  =  activePixel.getBlue();
				
				if (blue % 2  ==  1){
					blue--;
					int newColor = makeColorWithNewBlueValue(activePixel, blue);
					imageRaster.setDataElements(x, y, imageColorModel.getDataElements(newColor, null));
				}
			}
		}
		image.setData(imageRaster);
	}
	
	
	
	/**
	 * Creates a new color-value (int) with manipulated blue-value.
	 * 
	 * @param currentPixel		The Color of the pixel for which a new color is requested.
	 * @param blueValue			The new blue value to incorporate into the new color-value.
	 * 
	 * @return					An integer representing a new color-value.
	 */
	private static int makeColorWithNewBlueValue(Color currentPixel, int blueValue) {
		int newColor = ((currentPixel.getAlpha()  <<  BITSHIFT_ALPHA)  | 
						(currentPixel.getRed()    <<  BITSHIFT_RED)  | 
						(currentPixel.getGreen()  <<  BITSHIFT_GREEN)  | 
						 blueValue);
		
		return newColor;
	}

	
	
	/**
	 * Updated the local (static) variables holding the image, the color-model of the image, the raster of the
	 * image, and the height and width of the image-raster.
	 * 
	 * @param bufferedImage		The image for which to perform a read-, write- or erase-operation.
	 * 
	 * @throws Exception		Unspecified Exception in case something goes wrong.
	 */
	private static void updateImageSpecifications(BufferedImage bufferedImage) throws Exception {
		image 			= bufferedImage;
		imageColorModel = image.getColorModel();
		imageRaster 	= image.getRaster();
		imageHeight 	= imageRaster.getHeight();
		imageWidth 		= imageRaster.getWidth();
	}
	
	
	
	/**
	 * Translating a human-readable String to its binary representation.
	 * An empty String is defined and the binary representation of each letter of the original String is 
	 * appended by using the characterToBinary()-method.
	 * 		
	 * @param input		A human-readable String.
	 * 
	 * @return			The binary representation of input.
	 */
	private static String translateToBinaryString(String input) {
		String binaryString = "";
		
		for (int i = 0    ;    i < input.length()    ;    i++){
			binaryString += characterToBinary(input.charAt(i));
		}
		
		return binaryString;	
	}
	
	
	/**
	 * Translating a binary String to its human-readble representation.
	 * An empty String is defined. Every Byte of the original String is translated to the corresponding character,
	 * and that character is appended to the new String.
	 * 
	 * @param input		A binary String.
	 * 
	 * @return			The human-readable representation of input.
	 */
	private static String translateToReadableString(String input) {
		String readableString = "";
		
		for (int i = 0    ;    i < input.length() - LENGTH_OF_BINARY_UNIT - 1    ;    i += LENGTH_OF_BINARY_UNIT){
			readableString += binaryToCharacter(input.substring(i, i + LENGTH_OF_BINARY_UNIT));
		}
		
		return readableString;
	}
		
	
	/**
	 * Translates a single character to its binary representation with a size of one byte.
	 * A character-array representing a null-byte is defined.
	 * The values of the bits are calculated from the characters decimal value.
	 * The character-array is finally converted to a String.
	 * 
	 * @param character		The character which binary representation is requiered.
	 * 
	 * @return				The binary representation of the given character as String.
	 */
	private static String characterToBinary(char character) {
		char[] binaryUnit = {'0','0','0','0','0','0','0','0'};
		int characterDecimalValue = (int)character;
		
		for (int i = MAX_EXPONENT_IN_BINARY_UNIT    ;    i >= 0    ;    i--){
			binaryUnit[i] = (characterDecimalValue % 2 == 0)  ?  ('0')  :  ('1');
			characterDecimalValue /= BASE_OF_BINARY_UNIT;
		}
		
		String feedback = "";
		for (int i = 0    ;    i < LENGTH_OF_BINARY_UNIT    ;    i++){
			feedback += binaryUnit[i];
		}
		
		return feedback;
	}
		
	
	/**
	 * Translates a byte (representing one character) to its character representation.
	 * A newly defined char-array gets the converted input-String.
	 * Each bit is multiplied with its position-value. The sum of those values is the decimal 
	 * value of the character.
	 * 
	 * @param s		The binary sequence representing a single character.
	 * 
	 * @return		The character corresponding to the given binary-sequence.
	 */
	private static char binaryToCharacter(String s) {
		char[] binaryCharacter = s.toCharArray();
		int decimalValueOfCharacter = 0;
		
		for (int i = 0    ;    i < LENGTH_OF_BINARY_UNIT    ;    i++){
			double bitPositionValue = Math.pow(BASE_OF_BINARY_UNIT, (MAX_EXPONENT_IN_BINARY_UNIT - i));
			decimalValueOfCharacter 	+= 	(binaryCharacter[i] - '0') * Math.round(bitPositionValue);
		}

		return (char) decimalValueOfCharacter;
	}


}
