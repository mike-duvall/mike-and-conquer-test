package util

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class BuildDirectoryUtil {


     static String getAbsolutePathToBuildDirectory() {
        String relPath = BuildDirectoryUtil.class.getProtectionDomain().getCodeSource().getLocation().getFile();

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


    static String writeImageToFileInBuildDirectory(BufferedImage bufferedImage, String subdirectory, String fileName) {
        String buildDirectoryPath = getAbsolutePathToBuildDirectory()
        File targetDir = new File(  buildDirectoryPath + "/" + subdirectory)

        if(!targetDir.exists()) {
            targetDir.mkdir();
        }

        String targetDirectorAbsolutePath = targetDir.getAbsolutePath()
        String fileNameWithFullAbsolutePath = targetDirectorAbsolutePath + "\\" + fileName
        File outputFile = new File(fileNameWithFullAbsolutePath)
        ImageIO.write(bufferedImage, "png", outputFile);
//        return "Writing screenshot to file: " + fileNameWithFullAbsolutePath
        return fileNameWithFullAbsolutePath

    }


}
