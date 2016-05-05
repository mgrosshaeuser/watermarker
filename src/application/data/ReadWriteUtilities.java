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
	
	
	
	
	public static int getRedundancy(){
		return redundancy;
	}
	
	
	
	
	public static BufferedImage writeWatermarkToImage(WorkingImage wImage, String watermark, BufferedImage image) {
		try {
			updateImageSpecifications(image);
			int[] watermarkBinary = createWatermarkBinarySequence(watermark);
			resetBlueValues();
			writeDataToBlueChannel(watermarkBinary);
			wImage.setRedundancy(redundancy);
			return image;
		} catch (Exception e) {
			return null;
		}
	}
	
	
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
	
	
	
	
	public static String readWatermarkFromImage(BufferedImage bufferedImage) {
		try {
			updateImageSpecifications(bufferedImage);
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
	

	
	
	public static boolean eraseWatermarkFromImage(BufferedImage bufferedImage) {
		try {
			updateImageSpecifications(bufferedImage);	
			resetBlueValues();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	
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
	
	
	
	
	private static int makeColorWithNewBlueValue(Color currentPixel, int blueValue) {
		int newColor = ((currentPixel.getAlpha()  <<  BITSHIFT_ALPHA)  | 
						(currentPixel.getRed()    <<  BITSHIFT_RED)  | 
						(currentPixel.getGreen()  <<  BITSHIFT_GREEN)  | 
						 blueValue);
		
		return newColor;
	}

	


	private static void updateImageSpecifications(BufferedImage bufferedImage) throws Exception {
		image 			= bufferedImage;
		imageColorModel = image.getColorModel();
		imageRaster 	= image.getRaster();
		imageHeight 	= imageRaster.getHeight();
		imageWidth 		= imageRaster.getWidth();
	}
	
	
	
		
	private static String translateToBinaryString(String input) {
		String binaryString = "";
		
		for (int i = 0    ;    i < input.length()    ;    i++){
			binaryString += characterToBinary(input.charAt(i));
		}
		
		return binaryString;	
	}
	
	
	private static String translateToReadableString(String input) {
		String readableString = "";
		
		for (int i = 0    ;    i < input.length() - LENGTH_OF_BINARY_UNIT - 1    ;    i += LENGTH_OF_BINARY_UNIT){
			readableString += binaryToCharacter(input.substring(i, i + LENGTH_OF_BINARY_UNIT));
		}
		
		return readableString;
	}
		
	
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
