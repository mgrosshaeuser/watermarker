package application.data;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import application.Main;




public class WorkingImage {

	private File            file;
	private BufferedImage   image;
	private String          watermark;
	private int             watermarkMaxLength;
	private String          imageStatus;
	
	private boolean         imageSaved;
	private boolean         imageLoaded;
	private boolean         imageWatermarked;

	private int             redundantWrites;
	
	
	
	/**
	 * Constructor. Accepts an image-file (of type File) and opens it.
	 * If the file contains an image, the status is updated and the image
	 * is searched for a watermark.
	 * 
	 * @param file	An image-file. Either PNG oder BMP.
	 */
	public WorkingImage(File file) {
		this.file = file;
		loadImageDataFromFile();
		
		if (isImageLoaded()){
			imageSaved = true;
			imageWatermarked = false;
			getWatermarkMaxLength();
			readWatermark();		
		}
	}
	


	public File 			getImageFile()						{	return file;					}
	public BufferedImage 	getImage()							{	return image;					}
	public String 			getWatermark()						{	return watermark;				}
	public String			getImageStatus()					{	return imageStatus;				}
	
	public boolean 			isImageSaved()						{	return imageSaved;				}
	public boolean 			isImageLoaded()						{	return imageLoaded;				}
	public boolean 			isImageWatermarked()				{	return imageWatermarked;		}

	protected void setRedundancy(int redundantWrites) {
		this.redundantWrites = redundantWrites;	
	}
	
	
	
	/**
	 * Performs necessary preparations and saves an image to a file.
	 * In case of a null-argument the image is saved to the original file.
	 * In case of a file-argument a valid file-extension is added if 
	 * necessary, and the image is saved to this file.
	 * The status is updated, depending on success.
	 * 
	 * @param file	Either null (for original file) or a new file.
	 */
	public void saveImage(File file) {
		if (file == null) {
			file = this.file;
		} else {
			file = getFileWithAppropriateFileExtension(file);
		}

		try {
			saveImageDataInFile(file);
		} catch (IOException e) {
			imageSaved = false;
		}
	}
	
		
	
	/**
	 * Reading a watermark from the image using ReadWriteUtilities. If a 
	 * watermark is present, the status is updated.
	 */
	public void readWatermark() {
		watermark = ReadWriteUtilities.readWatermarkFromImage(image);
		if (! (watermark == null)    &    ! watermark.equals("")){
			imageWatermarked = true;
		}				
	}


	
	/**
	 * Writing a watermark to the image using ReadWriteUtilities. 
	 * If the payload is neither null nor empty nor too large a writing-operation
	 * is started. If this writing-operation doesn't result in a null-reference,
	 * the status is updated.
	 * 
	 * @param watermark		The watermark-payload.
	 */
	public void writeWatermark(String watermark) {
		BufferedImage tempImage = null;
		if (! (watermark == null)   &&   ! watermark.equals("")   &&   ! (watermark.length() > watermarkMaxLength)){
			tempImage = ReadWriteUtilities.writeWatermarkToImage(watermark, image);
		}
		
		if (tempImage != null) {
			image = tempImage;
			redundantWrites = ReadWriteUtilities.getRedundancy();
			imageSaved = false;
			imageWatermarked = true;
			imageStatus = "Wasserzeichen geschrieben mit " + redundantWrites + "-facher Redundanz.";
			readWatermark();
		}
	}
	
	
	
	/**
	 * Erases a watermark from the image using ReadWriteUtilities. If 
	 * successful, the image status is updated.
	 */
	public void eraseWatermark() {
		boolean isErased = ReadWriteUtilities.eraseWatermarkFromImage(image);
		if (isErased){
			imageSaved = false;
			imageWatermarked = false;
			imageStatus = "Wasserzeichen entfernt.";
		}
	}



	/**
	 * Loads the image from the image-file and updates status depending on success.
	 */
	private void loadImageDataFromFile() {
		try {
			image = ImageIO.read(file);
			imageLoaded = true;
			imageSaved = true;
			imageStatus = "Datei geladen.";
		} catch (IOException e) {
			imageLoaded = false;
		}
	}


	
	/**
	 * Writes image-data to a file, reloads the image on view and updates 
	 * the status.
	 * 
	 * @param file			The destination file.
	 * 
	 * @throws IOException	Thrown if file access fails.
	 */
	private void saveImageDataInFile(File file) throws IOException {
		ImageIO.write(image, "png", file);
		Main.setActiveImage(file);
		imageSaved = true;
		imageStatus = "Datei gespeichert.";
	}


	
	/**
	 * Adds an appropriate file-extension, in case the user didn't specify one.
	 * 
	 * @param file	The file with (possibly) missing extension.
	 * 
	 * @return		The file with extension.
	 */
	private File getFileWithAppropriateFileExtension(File file) {
		String fileName = file.getAbsolutePath().toLowerCase();
		if (! fileName.endsWith(".png")    &&    ! fileName.endsWith(".bmp")){
			file = new File(file.getAbsolutePath().toString() + ".png");
		}
		return file;
	}


	
	/**
	 * The payload must not contain more than 30 characters even for large images.
	 * For smaller images that number is decreases to guarantee that every image-
	 * row contains the watermark at least once.
	 * 
	 * @return	Either the original maximum-length or a decreased value,
	 * 			depending on the image-dimensions.
	 */
	private int getWatermarkMaxLength() {
		watermarkMaxLength = ReadWriteUtilities.INITIAL_WATERMARK_MAX_LENGTH;
		int imageWidth = image.getRaster().getWidth();
		int binaryUnitsPerRow = imageWidth /ReadWriteUtilities.LENGTH_OF_BINARY_UNIT;
		int sizeOfWatermarkIndicator = ReadWriteUtilities.WATERMARK_INDICATOR.length();
		int sizeOfBinaryWatermarkIndicator = sizeOfWatermarkIndicator * ReadWriteUtilities.LENGTH_OF_BINARY_UNIT;
		int availableSpace = binaryUnitsPerRow - sizeOfBinaryWatermarkIndicator - 1;		
	
		if (availableSpace < watermarkMaxLength)
			return availableSpace;
		return watermarkMaxLength;		
	}



	@Override
	public String toString() {
		return file.getAbsolutePath().toString();
	}
}
