package wpm.mjpeg;

import java.io.File;
import java.io.FilenameFilter;

import javax.swing.ImageIcon;

/**
 * 
 * @author monceaux
 */
public class Test_MJPEGGenerator {

    /**
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        double framerate = 12.0;
        double transitionDuration = 1; // seconds
        double slideDuration = 3; // seconds

        File photoDir = new File(args[0]);
        File[] files = photoDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith("jpg");
            }
        });

        int numFrames = (int)(files.length * framerate * (slideDuration + transitionDuration) + (transitionDuration * framerate));
        int width = 640;
        int height = 480;

        MJPEGGenerator m_generator = new MJPEGGenerator(new File(args[1]), width, height, framerate, numFrames);

        for (int i = 0; i < files.length; i++) {
            System.out.println("processing file " + i);
            ImageIcon ii = new ImageIcon(files[i].getCanonicalPath());
            m_generator.addImage(ii.getImage());
        }

        m_generator.finishAVI();
    }

}