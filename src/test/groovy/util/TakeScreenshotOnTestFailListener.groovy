package util

import client.MikeAndConquerUIClient
import groovy.transform.CompileStatic
import org.spockframework.runtime.AbstractRunListener
import org.spockframework.runtime.model.ErrorInfo

import javax.imageio.ImageIO
import java.awt.image.BufferedImage


@CompileStatic
class TakeScreenshotOnTestFailListener extends AbstractRunListener {


    MikeAndConquerUIClient uiClient



    void error(ErrorInfo error) {

        String testName = error.method.name
        String testNameNoSpaces = testName.replace(" ", "_")
        String fileName = "screenshot-on-failed-test---" + testNameNoSpaces + ".png"

        BufferedImage fullScreenShot = uiClient.getScreenshot()
        String screenshotFileNameWithPath = BuildDirectoryUtil.writeImageToFileInBuildDirectory(fullScreenShot, "test-failure-screenshots", fileName )
        println "Taking screenshot on test failure.  Screenshot location=" + screenshotFileNameWithPath
    }

    String getAbsolutePathToBuildDirectory() {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath)
        boolean done = false

        while(!done) {
            if(targetDir.getName().equals("build")) {
                done = true
            } else {
                targetDir = targetDir.getParentFile()
            }
        }
        return targetDir.getAbsolutePath()
    }


//    void writeImageToFileInBuildDirectory(BufferedImage bufferedImage, String fileName) {
//        String buildDirectoryPath = getAbsolutePathToBuildDirectory()
//        File targetDir = new File(  buildDirectoryPath + "/test-failure-screenshots")
//
//        if(!targetDir.exists()) {
//            targetDir.mkdir();
//        }
//
//        String targetDirectorAbsolutePath = targetDir.getAbsolutePath()
//        String fileNameWithFullAbsolutePath = targetDirectorAbsolutePath + "\\" + fileName
//        File outputFile = new File(fileNameWithFullAbsolutePath)
//        println "Writing screenshot to file: " + fileNameWithFullAbsolutePath
//        ImageIO.write(bufferedImage, "png", outputFile);
//    }


}