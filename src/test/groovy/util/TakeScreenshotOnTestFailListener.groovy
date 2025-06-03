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
        try {
            String testName = error.method.name
            String testNameNoSpaces = testName.replace(" ", "_")
            String fileName = "screenshot-on-failed-test---" + testNameNoSpaces + ".png"

            BufferedImage fullScreenShot = uiClient.getScreenshot()
            String screenshotFileNameWithPath = BuildDirectoryUtil.writeImageToFileInBuildDirectory(fullScreenShot, "test-failure-screenshots", fileName)
            println "Taking screenshot on test failure.  Screenshot location=" + screenshotFileNameWithPath
        }
        catch(Exception e) {
            println "Error trying to log test failure:  Exception: " + e.printStackTrace()
        }
    }

}