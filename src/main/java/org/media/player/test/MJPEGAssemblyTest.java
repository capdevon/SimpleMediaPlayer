/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.media.player.test;

import java.io.File;

import org.media.player.MJPEGGenerator;

/**
 *
 */
public class MJPEGAssemblyTest {

    /**
     * 
     * @param args 
     */
    public static void main(String[] args) {

        //---------------------ASSUMPTION---------------------
        //You got 274 files containing frames, named from 640_360_0.jpg to  640_360_273.jpg 
        //placed in D:/tmp and you want to generate mjped named 640_360.mjpg in D:/
        //The files are of dimention 640/360. The output file should be of the same size.
        
        //Target file - full path. 
        File outputFile = new File("D://640_360.mjpg");
        //Output movie dimention. In general it doesnt need to match input images dimention, but it is better to resize outside of java and input files which are already resized
        int outputWidth = 640;
        int outputHeght = 360;
        //Target fps. In geneal should match the input fps. Not really required though. This info is encoded into mjpeg but the playback is dependant on the value provided later on
        int fps = 30;
        //Order numer of first and last frame. All inclusive. 0 and 273 mean there is file prefix000.extName to prefix273extName
        //Important. Convention assumes that the padding is consistents with the number of digits of endNumber
        //It forces the file names to be as follows (with files from 0 to 273): prefix000.extName to prefix273extName.
        //If the frames were for example 0 to 12 the files would be:  prefix00.extName to prefix12extName 
        int startNumber = 0;
        int endNumber = 273;
        //Folder containing frame files
        String inputFolder = "D://tmp";
        //Any prefix. Must be the same for every file
        String imagePrefix = "640_360_";
        //Extension name
        String extName = "jpg";
        //GENERATE - watch console!
        MJPEGGenerator.encodeImages(outputFile, outputWidth, outputHeght, fps, startNumber, endNumber, inputFolder, imagePrefix, extName);
    }

}
