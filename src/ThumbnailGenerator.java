/*
Copyright (c) 2014 Venugopal Madathil
 */
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import org.imgscalr.Scalr;

/**
 * Creates thumbnail with given targetsize using imgscalr library. 
 * 
 * How to run? 
 * Put images to originals directory of the project. Add lib folder 
 * to the buld path before running the program or if its a maven project 
 * include imgscalr lib and follows.
 * 
 * ++++++++++++++++++++++++++++++++++++++++++++++++++++
 * +                                                  +
 * +	<dependency>                                  + 
 * +	     <groupId>org.imgscalr</groupId>          + 
 * +	     <artifactId>imgscalr-lib</artifactId>    + 
 * +	     <version>4.2</version>                   +
 * +	</dependency>                                 +
 * +                                                  +
 * +++++++++++++++++++++++++++++++++++++++++++++++++++
 * 
 * 
 * @author Venugopal Madathil
 * 
 */

public class ThumbnailGenerator {

	public static void main(String[] args) {
		File inputDir = new File("originals");
		File outputDir = new File("output");
		outputDir.mkdir();
		//Change target size here
		int targetSize = 150;

		// Process each file inside the originals/ directory (sub-directories
		// are ignored)
		for (File img : inputDir.listFiles()) {
			if (img.isFile())
				process(img, outputDir, targetSize);
		}

	}

	private static void process(File inputFile, File baseDir, int targetSize) {

		// The output name e.g. 'test.jpg' will be the same as the original
		String name = inputFile.getName();
		File outputFile = new File(baseDir, name);
		createThumbnail(inputFile, outputFile, targetSize);
	}

	private static void createThumbnail(File imageFile, File outputFile, int targetSize) {
		System.out.println("Processing " + imageFile);
		BufferedImage source;
		try {
			source = ImageIO.read(imageFile);
			BufferedImage output = Scalr.resize(source, targetSize);
			boolean hasAlpha = source.getColorModel().hasAlpha();
			if (hasAlpha) {
				System.out.println("Has alpha (i.e. transparency):" + hasAlpha);
				output = dropAlphaChannel(output);
			}

			int sourceW = source.getWidth();
			int sourceH = source.getHeight();

			int w = output.getWidth();
			int h = output.getHeight();

			writeImage(outputFile, output);
			System.out.println("Source width,height : " + sourceW + "," + sourceH);
			System.out.println("Output width,height : " + w + "," + h);
			System.out.println("Output file:" + outputFile + ", file size : " + outputFile.length() / 1024 + " KB\n");
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return;
		}
	}

	/**
	 * Writes out an image to the file using the file extension to determine the
	 * image format.
	 * 
	 * @param outputFile
	 *            the target file. The filetype is determined by the extension
	 *            e.g. png, jpg
	 * @param image
	 *            the image to save
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static void writeImage(File outputFile, BufferedImage image) throws FileNotFoundException, IOException {

		// A lot of 'boilerplate' code to ask Java to save an image!

		// Extract the file extension e.g. 'jpg' 'png'
		String name = outputFile.getName().toLowerCase();
		String suffix = name.substring(name.lastIndexOf('.') + 1);

		boolean isJPG = suffix.endsWith("jpg");

		Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix(suffix);
		if (!writers.hasNext())
			System.err.println("Unrecognized image file extension " + suffix);

		// Check we can create a new, empty output file
		outputFile.createNewFile();

		ImageWriter writer = writers.next();
		writer.setOutput(new FileImageOutputStream(outputFile));

		ImageWriteParam param = writer.getDefaultWriteParam();
		// png files don't support compression and will throw an exception if we
		// try to set compression mode
		if (isJPG) {
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(1); // High quality
		}
		IIOImage iioImage = new IIOImage(image, null, null);
		writer.write(null, iioImage, param);
	}

	/**
	 * Drops alpha channel which may give a pink background for the output image.
	 * 
	 * @param src
	 * @return
	 */
	public static BufferedImage dropAlphaChannel(BufferedImage src) {
		BufferedImage convertedImg = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
		convertedImg.getGraphics().drawImage(src, 0, 0, null);
		return convertedImg;
	}
}
