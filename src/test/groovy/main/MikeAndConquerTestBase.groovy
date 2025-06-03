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
import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.SpecInfo
import spock.lang.Specification
import spock.util.concurrent.PollingConditions
import util.BuildDirectoryUtil
import util.ImageUtil
import util.TakeScreenshotOnTestFailListener

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

class MikeAndConquerTestBase extends Specification implements IGlobalExtension  {


    MikeAndConquerSimulationClient simulationClient
    MikeAndConquerUIClient uiClient
    SequentialEventReader sequentialEventReader
    JsonSlurper jsonSlurper


    static TakeScreenshotOnTestFailListener takeScreenshotOnTestFailListener = null


    TakeScreenshotOnTestFailListener getTakeScreenshotOnTestFailListener() {
        if(takeScreenshotOnTestFailListener == null) {
            takeScreenshotOnTestFailListener = new TakeScreenshotOnTestFailListener()
        }
        return takeScreenshotOnTestFailListener
    }

    @Override
    void start() {

    }

    @Override
    void visitSpec(SpecInfo specInfo) {
        specInfo.addListener(getTakeScreenshotOnTestFailListener())
    }

    @Override
    void stop() {

    }



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
        getTakeScreenshotOnTestFailListener().uiClient = uiClient

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
        createdUnit.xInWorldCoordinates = unitDataObject.X
        createdUnit.yInWorldCoordinates = unitDataObject.Y
        createdUnit.health = unitDataObject.Health
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

        BuildDirectoryUtil.writeImageToFileInBuildDirectory(realGameScreenshot, "screenshot", realGameCopiedFilename )
        BuildDirectoryUtil.writeImageToFileInBuildDirectory(screenshotSubImage, "screenshot", mikeAndConquerCopiedFilename )

        assert ImageUtil.imagesAreEqual(screenshotSubImage, realGameScreenshot)
    }

    Unit createGDIMinigunnerAtRandomLocation() {
        simulationClient.createGDIMinigunnerAtRandomLocation()
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.MINIGUNNER_CREATED)

        def jsonSlurper = new JsonSlurper()
        def eventData = jsonSlurper.parseText(event.eventData)
        Unit unit = new Unit()
        unit.unitId = eventData.UnitId
        unit.xInWorldCoordinates = eventData.X
        unit.yInWorldCoordinates = eventData.Y
        return unit
    }

    Unit createNodMinigunnerAtRandomLocation() {
        simulationClient.createDeactivatedNodMinigunnerAtRandomLocation()
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.MINIGUNNER_CREATED)

        def jsonSlurper = new JsonSlurper()
        def eventData = jsonSlurper.parseText(event.eventData)
        Unit unit = new Unit()
        unit.unitId = eventData.UnitId
        unit.xInWorldCoordinates = eventData.X
        unit.yInWorldCoordinates = eventData.Y
        return unit
    }


    Unit parseUnitFromMinigunnerCreatedEventData(String stringEventData) {
        def jsonSlurper = new JsonSlurper()
        def eventData = jsonSlurper.parseText(stringEventData)
        Unit unit = new Unit()
        unit.unitId = eventData.UnitId
        unit.x = eventData.X
        unit.y = eventData.Y
        unit.player = eventData.Player
        return unit

    }

    Unit creatNodMinigunnerAtRandomLocationWithAITurnedOff() {
        simulationClient.createDeactivatedNodMinigunnerAtRandomLocation()
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.MINIGUNNER_CREATED)
        return parseUnitFromEventData(event.eventData)
    }


    Unit createGDIMinigunnerAtWorldCoordinates(int xInWorldCoordinates, int yInWorldCoordinates) {
        WorldCoordinatesLocationBuilder minigunnerLocationBuilder = new WorldCoordinatesLocationBuilder()

        simulationClient.createGDIMinigunner(minigunnerLocationBuilder
                                               .worldCoordinatesX(xInWorldCoordinates)
                                               .worldCoordinatesY(yInWorldCoordinates)
                                               .build() )

        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.MINIGUNNER_CREATED)

        return parseUnitFromEventData(event.eventData)
    }

    Unit createGDIMinigunnerAtWorldCoordinatesWithHealth(int xInWorldCoordinates, int yInWorldCoordinates, int desiredHealth) {
        WorldCoordinatesLocationBuilder minigunnerLocationBuilder = new WorldCoordinatesLocationBuilder()

        simulationClient.createGDIMinigunner(minigunnerLocationBuilder
                                                     .worldCoordinatesX(xInWorldCoordinates)
                                                     .worldCoordinatesY(yInWorldCoordinates)
                                                     .build() )

        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.MINIGUNNER_CREATED)
        Unit createdUnit = parseUnitFromEventData(event.eventData)

        int damageAmount = createdUnit.health - desiredHealth;
        simulationClient.applyDamageToUnit(createdUnit.unitId, damageAmount)

        return createdUnit
    }




    Unit createGDIMinigunnerAtWorldMapTileCoordinates(int x, int y) {
        WorldCoordinatesLocationBuilder minigunnerLocationBuilder = new WorldCoordinatesLocationBuilder()

        simulationClient.createGDIMinigunner(minigunnerLocationBuilder
                                                     .worldMapTileCoordinatesX(x)
                                                     .worldMapTileCoordinatesY(y)
                                                     .build() )

        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.MINIGUNNER_CREATED)
        return parseUnitFromEventData(event.eventData)
    }


    Unit createNodMinigunnerAtWorldCoordinates(int xInWorldCoordinates, int yInWorldCoordinates) {
        WorldCoordinatesLocationBuilder minigunnerLocationBuilder = new WorldCoordinatesLocationBuilder()

        simulationClient.createNodMinigunner(minigunnerLocationBuilder
                                                     .worldCoordinatesX(xInWorldCoordinates)
                                                     .worldCoordinatesY(yInWorldCoordinates)
                                                     .build() )

        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.MINIGUNNER_CREATED)
        return parseUnitFromEventData(event.eventData)
    }

    Unit createNodMinigunnerAtWorldMapTileCoordinates(int x, int y) {
        simulationClient.createNodMinigunner(createLocationFromWorldMapTileCoordinates(x,y))

        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.MINIGUNNER_CREATED)
        return parseUnitFromEventData(event.eventData)
    }



    WorldCoordinatesLocation createLocationFromWorldMapTileCoordinates(int x, int y) {
        return new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(x)
                .worldMapTileCoordinatesY(y)
                .build()
    }


    Unit createMCVAtWorldCoordinatesWithHealth(int xInWorldCoordinates, int yInWorldCoordinates, int desiredHealth) {
        WorldCoordinatesLocationBuilder locationBuilder = new WorldCoordinatesLocationBuilder()

        locationBuilder
                .worldCoordinatesX(xInWorldCoordinates)
                .worldCoordinatesY(yInWorldCoordinates)

        Unit createdUnit = createMCV(locationBuilder.build())
        int damageAmount = createdUnit.health - desiredHealth;
        simulationClient.applyDamageToUnit(createdUnit.unitId, damageAmount)
        return createdUnit
    }



    Unit createMCV(WorldCoordinatesLocation location) {
        simulationClient.createMCV(location)

        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.MCV_CREATED)

        return parseUnitFromEventData(event.eventData)

    }

    Unit createMCVAtWorldMapTileCoordinates(int x, int y) {
        WorldCoordinatesLocation worldCoordinatesLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(x)
                .worldMapTileCoordinatesY(y)
                .build()

        return createMCV(worldCoordinatesLocation)
    }




    void leftClickAtWorldMapTileCoordinates(int x, int y) {
        WorldCoordinatesLocation worldCoordinatesLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(x)
                .worldMapTileCoordinatesY(y)
                .build()

        uiClient.leftClick(worldCoordinatesLocation)

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

    void leftClickAtWorldCoordinates(int x, int y) {
        WorldCoordinatesLocation worldCoordinatesLocation = new WorldCoordinatesLocationBuilder()
                .worldCoordinatesX(x)
                .worldCoordinatesY(y)
                .build()

        uiClient.leftClick(worldCoordinatesLocation)
    }



}
