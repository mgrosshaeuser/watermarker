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

	protected int           redundantWrites;
	
	
	
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

	protected void          setRedundancy(int redundantWrites)	{	this.redundantWrites = redundantWrites;	}
	
	
	
	
	public void saveImage(File file) {
		if (file == null){
			file = this.file;
		}

		file = getFileWitchAppropriateFileExtension(file);

		try {
			saveImageDataInFile(file);
		} catch (IOException e) {
			imageSaved = false;
		}
	}
	
		
	
	
	public void readWatermark() {
		watermark = ReadWriteUtilities.readWatermarkFromImage(image);
		if (! (watermark == null)    &    ! watermark.equals("")){
			imageWatermarked = true;
		}				
	}


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
		}
	}
	
	
	public void eraseWatermark() {
		boolean isErased = ReadWriteUtilities.eraseWatermarkFromImage(image);
		if (isErased){
			imageSaved = false;
			imageWatermarked = false;
			imageStatus = "Wasserzeichen entfernt.";
		}
	}



	
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


	private void saveImageDataInFile(File file) throws IOException {
		ImageIO.write(image, "png", file);
		Main.setActiveImage(file);
		imageSaved = true;
		imageStatus = "Datei gespeichert.";
	}


	private File getFileWitchAppropriateFileExtension(File file) {
		String fileName = file.getAbsolutePath().toLowerCase();
		if (! fileName.endsWith(".png")    &&    ! fileName.endsWith(".bmp")){
			file = new File(file.getAbsolutePath().toString() + ".png");
		}
		return file;
	}


	
	
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
