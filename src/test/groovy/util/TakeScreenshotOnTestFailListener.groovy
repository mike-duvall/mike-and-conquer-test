package util

import client.MikeAndConquerUIClient
import org.spockframework.runtime.AbstractRunListener
import org.spockframework.runtime.model.ErrorInfo

import javax.imageio.ImageIO
import java.awt.image.BufferedImage


class TakeScreenshotOnTestFailListener extends AbstractRunListener {


    MikeAndConquerUIClient uiClient



    def void error(ErrorInfo error) {

        String testName = error.method.name
        String testNameNoSpaces = testName.replace(" ", "_")
        String fileName = "screenshot-on-failed-test---" + testNameNoSpaces + ".png"
        println "Taking screenshot on test failure.  Screenshot fileName=" + fileName
        BufferedImage fullScreenShot = uiClient.getScreenshot()
        writeImageToFileInBuildDirectory(fullScreenShot, fileName )
    }


    void writeImageToFileInBuildDirectory(BufferedImage bufferedImage, String fileName) {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath+"../../../../build/screenshot")
        // TODO:  come up with more reliable way to find this path
        // Seems to work differently between IntelliJ versions
//        File targetDir = new File(relPath+"../../../build/screenshot")
        if(!targetDir.exists()) {
            targetDir.mkdir();
        }
        String absPath = targetDir.getAbsolutePath()
        File outputfile = new File(absPath + "\\" + fileName);
        ImageIO.write(bufferedImage, "png", outputfile);
    }


}