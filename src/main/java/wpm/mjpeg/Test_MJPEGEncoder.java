package wpm.mjpeg;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * 
 * @author polincdev
 */
public class Test_MJPEGEncoder {

    /**
     * 
     * @param args 
     */
    public static void main(String[] args) {

        //---------------------ASSUMPTION---------------------
        //You got 274 files containing frames, named from 640_360_0.jpg to  640_360_273.jpg 
        //placed in D:/tmp and you want to generate mjpeg named 640_360.mjpg in D:/
        //The files are of dimension 640/360. The output file should be of the same size.

        //Target file - full path. 
        File outputFile = new File("D://640_360.mjpg");
        //Output movie dimension. In general it doesnt need to match input images dimension, 
        //but it is better to resize outside of java and input files which are already resized
        int outputWidth = 640;
        int outputHeight = 360;
        //Target fps. In general should match the input fps. Not really required though. 
        //This info is encoded into mjpeg but the playback is dependent on the value provided later on
        int fps = 30;
        //Order number of first and last frame.
        int startNumber = 0;
        int endNumber = 273;
        //Folder containing frame files
        String inputFolder = "D://tmp";
        //Any prefix. Must be the same for every file
        String imagePrefix = "640_360_";
        //Extension name
        String extName = "jpg";
        //GENERATE - watch console!
        encodeImages(outputFile, outputWidth, outputHeight, fps, startNumber, endNumber, inputFolder, imagePrefix, extName);
    }

    /**
     * Encodes separate jpgs files into mjpg file. See console for debugging.
     *
     * @param outputFile   - Target file - full path with file e.g. C://Media//myFile.mjpg.
     * @param outputWidth  - Output movie dimension. In general it doesn't need to
     *                     match input images dimension, but it is better to resize
     *                     outside of java and input files which are already resized.
     * @param outputHeight @see outputWidth 0
     * @param fps          - Target fps. In general should match the input fps. Not
     *                     really required though. This info is encoded into mjpeg
     *                     but the playback is dependent on the value provided later on.
     * @param startNumber  - order number of first and last frame. All inclusive. 0
     *                     and 273 mean there is file prefix000.extName to
     *                     prefix273.extName Important. Convention assumes that the
     *                     padding is consistent with the number of digits of
     *                     endNumber. It forces the file names to be as follows
     *                     (with files from 0 to 273): prefix000.extName to
     *                     prefix273.extName. If the frames were for example 0 to 12
     *                     the files would be: prefix00.extName to prefix12.extName
     * @param endNumber    @see startNumber
     * @param inputFolder  - Folder containing frame jpg files
     * @param imagePrefix  - Any prefix(before digits). Must be the same for every file
     * @param extName      - Extension name
     */
    private static void encodeImages(File outputFile, int outputWidth, int outputHeight, int fps,
        int startNumber, int endNumber, String inputFolder, String imagePrefix, String extName) {

        System.out.println("Running " + inputFolder + imagePrefix + extName +
            " with " + outputWidth + ":" + outputHeight +
            " at " + fps +
            " frames from " + startNumber +
            " to " + endNumber +
            " into " + outputFile);

        try {
            String filler = "00000000";
            MJPEGGenerator mjpegGenerator = new MJPEGGenerator(outputFile, outputWidth, outputHeight, fps, endNumber - startNumber);

            for (int i = startNumber; i <= endNumber; i++) {
                String counter = filler.substring(0, String.valueOf(endNumber).length() - String.valueOf(i).length()) + i;
                String fileName = inputFolder + "//" + imagePrefix + counter + "." + extName;
                System.out.println(i + "/" + endNumber + " with " + fileName);

                BufferedImage image = ImageIO.read(new File(fileName));
                mjpegGenerator.addImage(image);
            }
            mjpegGenerator.finishAVI();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
