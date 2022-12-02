package main

import domain.Building
import domain.SimulationOptions
import domain.UIOptions
import domain.Unit
import domain.WorldCoordinatesLocation
import domain.WorldCoordinatesLocationBuilder
import domain.event.EventType
import domain.event.SimulationStateUpdateEvent
import util.TestUtil


//import domain.GDIConstructionYard
//import domain.MCV
//import domain.Minigunner
//import domain.Point
//import domain.Sidebar
import util.Util


class BuldingPlacementTests extends MikeAndConquerTestBase {


    def setup() {

//        boolean showShroud = false
//        float initialMapZoom = 1
//        int gameSpeedDelayDivisor = 50
//        setAndAssertGameOptions(showShroud, initialMapZoom, gameSpeedDelayDivisor)

        UIOptions uiOptions = new UIOptions(drawShroud: false, mapZoomLevel: 1.0)
        setAndAssertUIOptions(uiOptions)

        SimulationOptions simulationOptions = new SimulationOptions(gameSpeed: "Normal")
        setAndAssertSimulationOptions(simulationOptions)

        uiClient.startScenario()

//        // Add bogus minigunner to not delete so game state stays in "Playing"
//        gameClient.addGDIMinigunnerAtMapSquare(4,5)
    }


    def "should be able to build construction yard, then barracks, then minigunner"() {
        given:
        WorldCoordinatesLocation mcvStartLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(16)
                .worldMapTileCoordinatesY(8)
                .build()

        simulationClient.addMCV(mcvStartLocation)

        SimulationStateUpdateEvent mcvCreatedEvent = sequentialEventReader.waitForEventOfType(EventType.MCV_CREATED)
        Unit createdMCV = parseUnitFromEventData(mcvCreatedEvent.eventData)

        when:
        int mcvId = createdMCV.unitId

        then:
        assert mcvId != -1

        when:
        uiClient.selectUnit(mcvId)

        then:
        TestUtil.assertUnitIsSelected(uiClient, mcvId)

        when:
        WorldCoordinatesLocation mcvDestinationLocation = new WorldCoordinatesLocationBuilder()
                .worldCoordinatesX(350)
                .worldCoordinatesY(160)
                .build()

        uiClient.leftClick(mcvDestinationLocation)

        and:
        WorldCoordinatesLocation rightClickLocation = new WorldCoordinatesLocationBuilder()
                .worldCoordinatesX(200)
                .worldCoordinatesY(200)
                .build()

        uiClient.rightClick(rightClickLocation)

        then:
        SimulationStateUpdateEvent unitArrivedAtDestinationEvent = sequentialEventReader.waitForEventOfType(EventType.UNIT_ARRIVED_AT_DESTINATION)
        def unitArrivedEventData = jsonSlurper.parseText(unitArrivedAtDestinationEvent.eventData)

        when: "Test scenario 1"
        int testScenarioNumber = 1
        String scenarioPrefix = 'mcv'
        int startX = 306
        int startY = 124
        int screenshotCompareWidth = 73
        int screenshotCompareHeight = 57

        then:
        assertScreenshotMatches(scenarioPrefix, testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)

        when:
        uiClient.leftClick(mcvDestinationLocation)   // First select it
        uiClient.leftClick(mcvDestinationLocation)   // Then click to create construction yard

        then:
        SimulationStateUpdateEvent mcvRemovedEvent = sequentialEventReader.waitForEventOfType(EventType.UNIT_DELETED)
        int removedUnitId = parseUnitIdFromEventData(mcvRemovedEvent.eventData)
        assert removedUnitId == mcvId

        and:
        SimulationStateUpdateEvent gdiConstructionYardCreatedEvent = sequentialEventReader.waitForEventOfType(EventType.GDI_CONSTRUCTION_YARD_CREATED)
        Building createdConstructionYard = parseBuildingFromEventData(gdiConstructionYardCreatedEvent.eventData)

        // TODO Determine why I have to fudge these values
        assert createdConstructionYard.x == mcvDestinationLocation.XInWorldCoordinates() - 2
        assert createdConstructionYard.y == mcvDestinationLocation.YInWorldCoordinates() - 4


//        GDIConstructionYard constructionYard = gameClient.getGDIConstructionYard()
//        Building constructionYard = uiClient.getGDIConstructionYard()
//        assert constructionYard != null

//        Point expectedConstructionyardMapSquareLocation = Util.convertWorldCoordinatesToMapSquareCoordinates(mcvDestinationX, mcvDestinationY)
//        Point expectedConstructionYardLocationInWorldCoordinates = Util.convertMapSquareCoordinatesToWorldCoordinates(expectedConstructionyardMapSquareLocation.x,
//                expectedConstructionyardMapSquareLocation.y)
//
//        assert expectedConstructionYardLocationInWorldCoordinates.x == constructionYard.x
//        assert expectedConstructionYardLocationInWorldCoordinates.y == constructionYard.y
//
//        when:
//        MCV anMCV = gameClient.getMCV()
//
//        then:
//        assert anMCV == null
//
//        when:
//        Sidebar sidebar = gameClient.getSidebar()
//
//        then:
//        assert sidebar != null
//        assert sidebar.buildBarracksEnabled == true
//        assert sidebar.buildMinigunnerEnabled == false
//
        when:
        testScenarioNumber = 1
        scenarioPrefix = 'construction-yard-placed'
        startX = 340
        startY = 117
        screenshotCompareWidth = 43
        screenshotCompareHeight = 22

        then:
        assertScreenshotMatches(scenarioPrefix, testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)

//        when:
//        gameClient.leftClickSidebar("Barracks")
//
//        then:
//        assertBarracksIsBuilding()
//
//        and:
//        assertBarracksIsReadyToPlace()
//
//        when:
//        gameClient.leftClickSidebar("Barracks")
//
//        and:
//        gameClient.moveMouseToMapSquareCoordinates( new Point(15,3))
//
//        and:
//        testScenarioNumber = 1
//        scenarioPrefix = 'barracks-placement-indicator'
//        startX = 344
//        startY = 105
//        screenshotCompareWidth = 70
//        screenshotCompareHeight = 46
//
//        then:
//        assertScreenshotMatchesWithoutMovingCursor(scenarioPrefix, testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)
//
//
//        when:
//        gameClient.leftClickInMapSquareCoordinates(16,5)
//
//        and:
//        assertGDIBarracksExists()
//
//        and:
//        testScenarioNumber = 1
//        scenarioPrefix = 'barracks-placed'
//        startX = 387
//        startY = 118
//        screenshotCompareWidth = 47
//        screenshotCompareHeight = 6
//
//        then:
//        assertScreenshotMatches(scenarioPrefix, testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)
//
//        when:
//        sidebar = gameClient.getSidebar()
//
//        then:
//        assert sidebar != null
//        assert sidebar.buildBarracksEnabled == true
//        assert sidebar.buildMinigunnerEnabled == true
//
//        when:
//        gameClient.leftClickSidebar("Minigunner")
//
//        then:
//        assertOneMinigunnerExists()
    }



//    def "should be able to build barracks when a minigunner is selected"() {
//        given:
//        Point mcvLocation = new Point(21,12)
//        gameClient.addMCVAtMapSquare(mcvLocation.x, mcvLocation.y)
//        Minigunner minigunner = gameClient.addGDIMinigunnerAtMapSquare(18,10)
//
//        when:
//        gameClient.leftClickMCV(666)
//        gameClient.leftClickMCV(666)
//
//        and:
//        gameClient.leftClickMinigunner(minigunner.id)
//
//        and:
//        gameClient.leftClickSidebar("Barracks")
//
//        then:
//        assertBarracksIsBuilding()
//
//        and:
//        assertBarracksIsReadyToPlace()
//
//    }
//
//    def "should place sandbags in correct location"() {
//
//        given:
//        gameClient.addSandbag(12,16,2)
//        gameClient.addSandbag(13,16,9)
//        gameClient.addSandbag(13,15,6)
//        gameClient.addSandbag(14,15,8)
//
//        when: "Test scenario 1"
//        int testScenarioNumber = 1
//        String scenarioPrefix = 'sandbag-placement'
//        int startX = 291
//        int startY = 359
//        int screenshotCompareWidth = 46
//        int screenshotCompareHeight = 58
//
//        then:
//        assertScreenshotMatches(scenarioPrefix, testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)
//
//    }
//
//    def "should place sandbags Nod turrets in correct location"() {
//
//        given:
//        gameClient.addSandbag(12,16,2)
//        gameClient.addSandbag(13,16,9)
//        gameClient.addSandbag(13,15,6)
//        gameClient.addSandbag(14,15,8)
//
//        and:
//        float direction = 90.0 - 11.25
//        gameClient.addNodTurret(11,16,direction, 0)
//        gameClient.addNodTurret(14,16,direction, 2)
//
//        when: "Test scenario 1"
//        int testScenarioNumber = 1
//        String scenarioPrefix = 'nod-turret-placement'
//        int startX = 263
//        int startY = 362
//        int screenshotCompareWidth = 100
//        int screenshotCompareHeight = 52
//
//        then:
//        assertScreenshotMatches(scenarioPrefix, testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)
//
//    }




}