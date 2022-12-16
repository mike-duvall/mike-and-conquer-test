package main

import client.MikeAndConquerSimulationClient
import client.MikeAndConquerUIClient
import client.SequentialEventReader
import domain.Building
import domain.SimulationOptions
import domain.UIOptions
import domain.Unit
import domain.WorldCoordinatesLocation
import domain.WorldCoordinatesLocationBuilder
import domain.event.EventType
import domain.event.SimulationStateUpdateEvent
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import util.ImageUtil

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class MikeAndConquerTestBase extends Specification {


    MikeAndConquerSimulationClient simulationClient
    MikeAndConquerUIClient uiClient
    SequentialEventReader sequentialEventReader
    JsonSlurper jsonSlurper


    def setup() {
        String localhost = "localhost"
        String remoteHost = "192.168.0.110"

//        String host = localhost
        String host = remoteHost

        boolean useTimeouts = true
//        boolean useTimeouts = false
        simulationClient = new MikeAndConquerSimulationClient(host,  useTimeouts )
        sequentialEventReader = new SequentialEventReader(simulationClient)
        jsonSlurper = new JsonSlurper()

        uiClient = new MikeAndConquerUIClient(host, useTimeouts )

    }


    void setAndAssertUIOptions(UIOptions uiOptions) {
        uiClient.setUIOptions(uiOptions)
        assertUIOptionsAreSetTo(uiOptions)
    }


    def assertUIOptionsAreSetTo(UIOptions desiredUIOptions) {
        def conditions = new PollingConditions(timeout: 10, initialDelay: 0.5, factor: 1.25)
        conditions.eventually {
            UIOptions uiOptions = uiClient.getUIOptions()
            assert uiOptions.mapZoomLevel == desiredUIOptions.mapZoomLevel
            assert uiOptions.drawShroud == desiredUIOptions.drawShroud
        }
        return true
    }

    void setAndAssertSimulationOptions(SimulationOptions simulationOptions) {
        simulationClient.setSimulationOptions(simulationOptions)
        assertSimulationOptionsAreSetTo(simulationOptions)
    }

    def assertSimulationOptionsAreSetTo(SimulationOptions desiredOptions) {
        def conditions = new PollingConditions(timeout: 10, initialDelay: 0.5, factor: 1.25)
        conditions.eventually {
            SimulationOptions retrievedOptions = simulationClient.getSimulationOptions()
            assert retrievedOptions.gameSpeed == desiredOptions.gameSpeed
        }
        return true
    }

    Unit parseUnitFromEventData(String unitCreatedEventData) {
        def unitDataObject = jsonSlurper.parseText(unitCreatedEventData)
        Unit createdUnit = new Unit()
        createdUnit.unitId = unitDataObject.UnitId
        createdUnit.x = unitDataObject.X
        createdUnit.y = unitDataObject.Y
        return createdUnit
    }

    int parseUnitIdFromEventData(String eventData) {
        def unitDataObject = jsonSlurper.parseText(eventData)
        return unitDataObject.UnitId
    }

    Building parseBuildingFromEventData(String unitCreatedEventData) {
        def buildingDataObject = jsonSlurper.parseText(unitCreatedEventData)
        Building createdBuilding = new Building()

        createdBuilding.x = buildingDataObject.X
        createdBuilding.y = buildingDataObject.Y
        return createdBuilding

    }

    void assertScreenshotMatches(int testScenarioNumber, int startX, int startY, int screenshotCompareWidth, int screenshotCompareHeight) {
        assertScreenshotMatches('shroud', testScenarioNumber,startX,startY,screenshotCompareWidth, screenshotCompareHeight)
    }

    void assertScreenshotMatches(String scenarioPrefix, int testScenarioNumber, int startX, int startY, int screenshotCompareWidth, int screenshotCompareHeight) {

        // Move cursor so it's not in the screenshot
        WorldCoordinatesLocation cursorLocation = new WorldCoordinatesLocationBuilder()
                .worldCoordinatesX(startX + screenshotCompareWidth + 50)
                .worldCoordinatesY(startY + screenshotCompareHeight + 50)
                .build()

        uiClient.moveMouseToLocation(cursorLocation)
        assertScreenshotMatchesWithoutMovingCursor(scenarioPrefix, testScenarioNumber, startX, startY, screenshotCompareWidth, screenshotCompareHeight)
    }

    void assertScreenshotMatchesWithoutMovingCursor(String scenarioPrefix, int testScenarioNumber, int startX, int startY, int screenshotCompareWidth, int screenshotCompareHeight) {

        String realGameFilename = "real-game-" + scenarioPrefix + "-" + testScenarioNumber + "-start-x" + startX + "-y" + startY + "-" + screenshotCompareWidth + "x" + screenshotCompareHeight + ".png"

        File imageFile

        try {
            imageFile = new File(
                    getClass().getClassLoader().getResource(realGameFilename).getFile()
            )
        }
        catch(Exception e) {
            throw new RuntimeException("Unable to read file ${realGameFilename}")
        }

        BufferedImage realGameScreenshot = ImageIO.read(imageFile)
        BufferedImage fullScreenShot = uiClient.getScreenshot()
        BufferedImage screenshotSubImage = fullScreenShot.getSubimage(startX,startY,screenshotCompareWidth,screenshotCompareHeight)

        String realGameCopiedFilename = realGameFilename.replaceAll("real-game", "copied-real-game")
        String mikeAndConquerCopiedFilename = realGameFilename.replaceAll("real-game", "actual-mike-and-conquer")

        writeImageToFileInBuildDirectory(realGameScreenshot, realGameCopiedFilename )
        writeImageToFileInBuildDirectory(screenshotSubImage, mikeAndConquerCopiedFilename )

        assert ImageUtil.imagesAreEqual(screenshotSubImage, realGameScreenshot)
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
    int addGDIMinigunnerAtWorldCoordinates(int xInWorldCoordinates, int yInWorldCoordinates) {
        WorldCoordinatesLocationBuilder minigunnerLocationBuilder = new WorldCoordinatesLocationBuilder()

        simulationClient.addMinigunner(minigunnerLocationBuilder
                                               .worldCoordinatesX(xInWorldCoordinates)
                                               .worldCoordinatesY(yInWorldCoordinates)
                                               .build() )

        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.MINIGUNNER_CREATED)

        def jsonSlurper = new JsonSlurper()
        def eventData = jsonSlurper.parseText(event.eventData)
        return eventData.UnitId

    }

    WorldCoordinatesLocation createLocationFromWorldMapTileCoordinates(int x, int y) {
        return new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(x)
                .worldMapTileCoordinatesY(y)
                .build()
    }

    int addMCVAtWorldMapTileCoordinates(int x, int y) {
        WorldCoordinatesLocation worldCoordinatesLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(x)
                .worldMapTileCoordinatesY(y)
                .build()

        simulationClient.addMCV(worldCoordinatesLocation)

        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.MCV_CREATED)

        def jsonSlurper = new JsonSlurper()
        def eventData = jsonSlurper.parseText(event.eventData)
        return eventData.UnitId

    }

    void moveMouseToWorldMapTileCoordinates(int x, int y) {
        WorldCoordinatesLocation worldCoordinatesLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(x)
                .worldMapTileCoordinatesY(y)
                .build()

        uiClient.moveMouseToLocation(worldCoordinatesLocation)
    }


    void moveMouseToWorldCoordinates(int x, int y) {
        WorldCoordinatesLocation worldCoordinatesLocation = new WorldCoordinatesLocationBuilder()
                .worldCoordinatesX(x)
                .worldCoordinatesY(y)
                .build()

        uiClient.moveMouseToLocation(worldCoordinatesLocation)

    }



}
