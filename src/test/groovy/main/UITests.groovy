package main

import domain.Cursor
import domain.Point
import domain.UIOptions
import domain.Unit
import domain.WorldCoordinatesLocation
import domain.WorldCoordinatesLocationBuilder
import domain.event.EventType
import domain.event.SimulationStateUpdateEvent
import spock.lang.Unroll
import util.BuildDirectoryUtil
import util.TestUtil

import java.awt.image.BufferedImage

class UITests extends MikeAndConquerTestBase {


    def setup() {
        UIOptions uiOptions = new UIOptions(drawShroud: false, mapZoomLevel: 1.8)
        setAndAssertUIOptions(uiOptions)
    }

    def "Select and move a minigunner with mouse clicks"() {
        given:
        uiClient.startScenario()
        int minigunnerId = -1

        when:
        WorldCoordinatesLocation minigunnerStartLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(18)
                .worldMapTileCoordinatesY(14)
                .build()

        simulationClient.createGDIMinigunner(minigunnerStartLocation)

        then:
        SimulationStateUpdateEvent minigunnerCreatedEvent = sequentialEventReader.waitForEventOfType(EventType.MINIGUNNER_CREATED)
        Unit createdMinigunner = parseUnitFromEventData(minigunnerCreatedEvent.eventData)

        when:
        minigunnerId = createdMinigunner.unitId

        then:
        assert minigunnerId != -1

        when:
        uiClient.selectUnit(minigunnerId)

        then:
        TestUtil.assertUnitIsSelected(uiClient, minigunnerId)

        when:
        WorldCoordinatesLocation leftClickLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(18)
                .worldMapTileCoordinatesY(12)
                .build()

        uiClient.leftClick(leftClickLocation)
        int destinationXInWorldCoordinates = leftClickLocation.XInWorldCoordinates()
        int destinationYInWorldCoordinates =leftClickLocation.YInWorldCoordinates()


        then:
        SimulationStateUpdateEvent expectedBegnMissionMoveToDestinationEvent = sequentialEventReader.waitForEventOfType(EventType.BEGAN_MISSION_MOVE_TO_DESTINATION)
        TestUtil.assertBeganMissionMoveToDestinationEvent(expectedBegnMissionMoveToDestinationEvent, minigunnerId, destinationXInWorldCoordinates, destinationYInWorldCoordinates)

        and:
        SimulationStateUpdateEvent expectedUnitArrivedAtDestinationEvent = sequentialEventReader.waitForEventOfType(EventType.UNIT_ARRIVED_AT_DESTINATION)
        TestUtil.assertUnitArrivedAtDestinationEvent(expectedUnitArrivedAtDestinationEvent, minigunnerId)

    }

    // Unfortunately, these have to be static(or @Shared) to be accessible in the "where" block
    // https://stackoverflow.com/questions/22707195/how-to-use-instance-variable-in-where-section-of-spock-test
    static int selectionBoxLeftmostX = 75
    static int selectionBoxRightmostX = 100
    static int selectionBoxTopmostY = 350
    static int selectionBoxBottommostY = 400


    @Unroll
    def "should be able to drag select multiple GDI minigunners" () {

        given:
        uiClient.startScenario()

        when:
        Unit gdiMinigunner1= createGDIMinigunnerAtWorldCoordinates(82, 369)
        Unit gdiMinigunner2 = createGDIMinigunnerAtWorldCoordinates(92, 380)
        Unit gdiMinigunner3 = createGDIMinigunnerAtWorldCoordinates(230, 300)
        Unit gdiMinigunner4 = createGDIMinigunnerAtWorldCoordinates(82, 300)

        Set<Integer> uniqueMinigunnerIds = []
        uniqueMinigunnerIds.add(gdiMinigunner1.unitId)
        uniqueMinigunnerIds.add(gdiMinigunner2.unitId)
        uniqueMinigunnerIds.add(gdiMinigunner3.unitId)
        uniqueMinigunnerIds.add(gdiMinigunner4.unitId)

        then:
        assert uniqueMinigunnerIds.size() == 4

        when:
        gdiMinigunner1 = uiClient.getUnit(gdiMinigunner1.unitId)
        gdiMinigunner2 = uiClient.getUnit(gdiMinigunner2.unitId)
        gdiMinigunner3 = uiClient.getUnit(gdiMinigunner3.unitId)
        gdiMinigunner4 = uiClient.getUnit(gdiMinigunner4.unitId)

        then:
        assert gdiMinigunner1.selected == false
        assert gdiMinigunner2.selected == false
        assert gdiMinigunner3.selected == false
        assert gdiMinigunner4.selected == false

        when:
        uiClient.dragSelect(dragStartX, dragStartY, dragEndX, dragEndY)

        gdiMinigunner1 = uiClient.getUnit(gdiMinigunner1.unitId)
        gdiMinigunner2 = uiClient.getUnit(gdiMinigunner2.unitId)
        gdiMinigunner3 = uiClient.getUnit(gdiMinigunner3.unitId)
        gdiMinigunner4 = uiClient.getUnit(gdiMinigunner4.unitId)

        then:
        assert gdiMinigunner1.selected == true
        assert gdiMinigunner2.selected == true
        assert gdiMinigunner3.selected == false
        assert gdiMinigunner4.selected == false

        when:
        WorldCoordinatesLocation rightClickLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(10)
                .worldMapTileCoordinatesY(10)
                .build()

        uiClient.rightClick(rightClickLocation)

        and:
        gdiMinigunner1 = uiClient.getUnit(gdiMinigunner1.unitId)
        gdiMinigunner2 = uiClient.getUnit(gdiMinigunner2.unitId)
        gdiMinigunner3 = uiClient.getUnit(gdiMinigunner3.unitId)
        gdiMinigunner4 = uiClient.getUnit(gdiMinigunner4.unitId)

        then:
        assert gdiMinigunner1.selected == false
        assert gdiMinigunner2.selected == false
        assert gdiMinigunner3.selected == false
        assert gdiMinigunner4.selected == false

        where:
        dragStartX              | dragStartY                | dragEndX                  | dragEndY
        selectionBoxLeftmostX   | selectionBoxTopmostY      | selectionBoxRightmostX    | selectionBoxBottommostY    // Top left to bottom right
        selectionBoxRightmostX  | selectionBoxBottommostY   | selectionBoxLeftmostX     | selectionBoxTopmostY    // Bottom right to top left
        selectionBoxRightmostX  | selectionBoxTopmostY      | selectionBoxLeftmostX     | selectionBoxBottommostY    // Top right to bottom left
        selectionBoxLeftmostX   | selectionBoxBottommostY   | selectionBoxRightmostX    | selectionBoxTopmostY    // Bottom left to top right

    }

    @Unroll
    def "When MCV is clicked at xoffset #xoffset and yoffset #yoffset, selection state should be #shouldBeSelected" () {

            given:
            uiClient.startScenario()
//            uiClient.setUIOptions(new UIOptions(drawShroud: false, mapZoomLevel: 1.0))
            uiClient.setUIOptions(new UIOptions(drawShroud: false, mapZoomLevel: 3.0))
            int mcvWorldMapTileX = 21
            int mcvWorldMapTileY = 12

        when:
            Unit mcv = createMCVAtWorldMapTileCoordinates(mcvWorldMapTileX, mcvWorldMapTileY)
            int mcvId = mcv.unitId

            then:
            assert mcv.selected == false
//            assert mcv.selected == true

            when:
            WorldCoordinatesLocation worldCoordinatesLocation = new WorldCoordinatesLocationBuilder()
                    .worldCoordinatesX(mcv.xInWorldCoordinates + xoffset)
                    .worldCoordinatesY(mcv.yInWorldCoordinates + yoffset)
                    .build()

            moveMouseToWorldCoordinates(worldCoordinatesLocation.XInWorldCoordinates(), worldCoordinatesLocation.YInWorldCoordinates())
            takeDebugScreenshot("mcv-selection-test-${xoffset}-${yoffset}-${shouldBeSelected}-1")
            leftClickAtWorldCoordinates(worldCoordinatesLocation.XInWorldCoordinates(), worldCoordinatesLocation.YInWorldCoordinates())
            mcv = uiClient.getUnit(mcvId)

            then:
            assert mcv.selected == shouldBeSelected

        where:
            xoffset |   yoffset | shouldBeSelected
            14      |   -14     | false
            -14     |   -14     | false
            -11     |   -11     | true
            -5      |   -5      | true
            5       |      5    | true
            -14     |   14      | false
            -12     |   12      | true
            -12     |   -12     | false
            12      |   -12     | false
            12      |   12      | true
            14      |   14      | false
     }


    @Unroll
    def "When Minigunner is clicked at xoffset #xoffset and yoffset #yoffset, selection state should be #shouldBeSelected" () {

        given:
        uiClient.startScenario()
//        uiClient.setUIOptions(new UIOptions(drawShroud: false, mapZoomLevel: 1.0))
        uiClient.setUIOptions(new UIOptions(drawShroud: false, mapZoomLevel: 3.0))
        int minigunnerWorldMapTileX = 21
        int minigunnerWorldMapTileY = 12


        when:

        Unit minigunner = createGDIMinigunnerAtWorldMapTileCoordinates(minigunnerWorldMapTileX, minigunnerWorldMapTileY)
        int minigunnerId = minigunner.unitId

        then:
        assert minigunner.selected == false
//        assert minigunner.selected == true   // Uncomment this to make test fail, to facilitate seeing failure screenshot


        when:
        WorldCoordinatesLocation worldCoordinatesLocation = new WorldCoordinatesLocationBuilder()
                .worldCoordinatesX(minigunner.xInWorldCoordinates + xoffset)
                .worldCoordinatesY(minigunner.yInWorldCoordinates + yoffset)
                .build()

        moveMouseToWorldCoordinates(worldCoordinatesLocation.XInWorldCoordinates(), worldCoordinatesLocation.YInWorldCoordinates())
//        takeDebugScreenshot("minigunner-selection-test-${xoffset}-${yoffset}-${shouldBeSelected}-1")
        leftClickAtWorldCoordinates(worldCoordinatesLocation.XInWorldCoordinates(), worldCoordinatesLocation.YInWorldCoordinates())
        minigunner = uiClient.getUnit(minigunnerId)

        then:
        assert minigunner.selected == shouldBeSelected

        where:
        xoffset |   yoffset | shouldBeSelected
        0       |   0       | true
        3       |   3       | true
        5       |   3       | true
        6       |   3       | true
        7       |   4       | false
        7       |   5       | false
        -6      |   4       | false
        -6      |   -12     | false
        -5      | -12       | true
        8       | -12       | false
        7       | -12       | false
    }

    @Unroll
    def "When Jeep is clicked at xoffset #xoffset and yoffset #yoffset, selection state should be #shouldBeSelected" () {

        given:
        uiClient.startScenario()
        uiClient.setUIOptions(new UIOptions(drawShroud: false, mapZoomLevel: 3.0))
        int jeepWorldMapTileX = 21
        int jeepWorldMapTileY = 12

        when:
        Unit jeep = createJeepAtWorldMapTileCoordinates(jeepWorldMapTileX, jeepWorldMapTileY)
        int jeepId = jeep.unitId

        then:
        assert jeep.selected == false

        when:
        WorldCoordinatesLocation worldCoordinatesLocation = new WorldCoordinatesLocationBuilder()
                .worldCoordinatesX(jeep.xInWorldCoordinates + xoffset)
                .worldCoordinatesY(jeep.yInWorldCoordinates + yoffset)
                .build()

        moveMouseToWorldCoordinates(worldCoordinatesLocation.XInWorldCoordinates(), worldCoordinatesLocation.YInWorldCoordinates())
        leftClickAtWorldCoordinates(worldCoordinatesLocation.XInWorldCoordinates(), worldCoordinatesLocation.YInWorldCoordinates())
        jeep = uiClient.getUnit(jeepId)

        then:
        assert jeep.selected == shouldBeSelected

        where:
        xoffset | yoffset | shouldBeSelected
        0       | 0       | true
//        3       | 3       | true
//        6       | 3       | true
//        8       | 3       | false
//        -8      | 3       | false
//        -6      | 3       | true
//        3       | 6       | true
//        3       | 8       | false
//        3       | -8      | false
//        3       | -6      | true
    }



    void takeDebugScreenshot(String screenshotName) {
        String fileName = screenshotName + ".png"
        BufferedImage fullScreenShot = uiClient.getScreenshot()
        String screenshotFileNameWithPath = BuildDirectoryUtil.writeImageToFileInBuildDirectory(fullScreenShot, "debug-screenshots", fileName)
        println "Taking screenshot. Screenshot location=" + screenshotFileNameWithPath

    }

    def "should set mouse cursor correctly when MCV is selected" () {

        given:
        uiClient.startScenario()
        int mcvWorldMapTileX = 21
        int mcvWorldMapTileY = 12

        and:
        Unit mcv = createMCVAtWorldMapTileCoordinates(mcvWorldMapTileX, mcvWorldMapTileY)
        int mcvId = mcv.unitId

        WorldCoordinatesLocation mountainSquareLocation = createLocationFromWorldMapTileCoordinates(3,0)
        WorldCoordinatesLocation clearSquareLocation = createLocationFromWorldMapTileCoordinates(10, 10)

        when:
        uiClient.selectUnit(mcvId)

        and:
        uiClient.moveMouseToLocation(mountainSquareLocation)

        then:
        String mouseCursorState = uiClient.getMouseCursorState()
        assert mouseCursorState == Cursor.MOVEMENT_NOT_ALLOWED_CURSOR

        when:
        uiClient.moveMouseToLocation(clearSquareLocation)
        mouseCursorState = uiClient.getMouseCursorState()

        then:
        assert mouseCursorState == Cursor.MOVE_TO_LOCATION_CURSOR

        when:
        WorldCoordinatesLocation mcvLocation = createLocationFromWorldMapTileCoordinates(mcvWorldMapTileX, mcvWorldMapTileY)
        uiClient.moveMouseToLocation(mcvLocation)
        mouseCursorState = uiClient.getMouseCursorState()

        then:
        assert mouseCursorState == Cursor.BUILD_CONSTRUCTION_YARD_CURSOR

        when:
        uiClient.rightClick(clearSquareLocation)
        mouseCursorState = uiClient.getMouseCursorState()

        then:
        assert mouseCursorState == Cursor.DEFAULT_ARROW_CURSOR

    }

    def "should set mouse cursor correctly when minigunner is selected" () {

        given:
        uiClient.startScenario()
        Unit gdiMinigunner = createGDIMinigunnerAtRandomLocation()
        Point mountainSquareLocation = new Point(79, 20)
        Point clearSquare = new Point(10,10)
        Point overMapButNotOverTerrain = new Point(675,20)


        when:
        uiClient.selectUnit(gdiMinigunner.unitId)
        then:
        TestUtil.assertUnitIsSelected(uiClient, gdiMinigunner.unitId)


        and:
        moveMouseToWorldCoordinates(overMapButNotOverTerrain.x, overMapButNotOverTerrain.y)
        then:
        String mouseCursorState = uiClient.getMouseCursorState()
        assert mouseCursorState == Cursor.MOVEMENT_NOT_ALLOWED_CURSOR

        when:
        moveMouseToWorldCoordinates(mountainSquareLocation.x, mountainSquareLocation.y)
        mouseCursorState = uiClient.getMouseCursorState()

        then:
        assert mouseCursorState == Cursor.MOVEMENT_NOT_ALLOWED_CURSOR

        when:
        moveMouseToWorldCoordinates(clearSquare.x, clearSquare.y)
        mouseCursorState = uiClient.getMouseCursorState()

        then:
        assert mouseCursorState == Cursor.MOVE_TO_LOCATION_CURSOR

        when:
        Unit nodMinigunner = creatNodMinigunnerAtRandomLocationWithAITurnedOff()
        moveMouseToWorldCoordinates(nodMinigunner.xInWorldCoordinates, nodMinigunner.yInWorldCoordinates)
        mouseCursorState = uiClient.getMouseCursorState()

        then:
        assert mouseCursorState == Cursor.ATTACK_ENEMY_CURSOR

        when:
        WorldCoordinatesLocation rightClickLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(20)
                .worldMapTileCoordinatesY(20)
                .build()

        uiClient.rightClick(rightClickLocation)
        mouseCursorState = uiClient.getMouseCursorState()

        then:
        assert mouseCursorState == Cursor.DEFAULT_ARROW_CURSOR

    }

    def "should set mouse cursor correctly when no unit is selected" () {

        given:
        uiClient.startScenario()

        when:

        Point mountainSquareLocation = new Point(79, 20)
        Point clearSquare = new Point(10,10)
        Point overMapButNotOverTerrain = new Point(675,20)

        and:
        moveMouseToWorldCoordinates(overMapButNotOverTerrain.x, overMapButNotOverTerrain.y)

        then:
        String mouseCursorState = uiClient.getMouseCursorState()
        assert mouseCursorState == Cursor.DEFAULT_ARROW_CURSOR

        when:
        moveMouseToWorldCoordinates(mountainSquareLocation.x, mountainSquareLocation.y)
        mouseCursorState = uiClient.getMouseCursorState()

        then:
        assert mouseCursorState == Cursor.DEFAULT_ARROW_CURSOR

        when:
        moveMouseToWorldCoordinates(clearSquare.x, clearSquare.y)
        mouseCursorState = uiClient.getMouseCursorState()

        then:
        assert mouseCursorState == Cursor.DEFAULT_ARROW_CURSOR

        when:
        Unit nodMinigunner = creatNodMinigunnerAtRandomLocationWithAITurnedOff()
        moveMouseToWorldCoordinates(nodMinigunner.xInWorldCoordinates, nodMinigunner.yInWorldCoordinates)
        mouseCursorState = uiClient.getMouseCursorState()

        then:
        assert mouseCursorState == Cursor.DEFAULT_ARROW_CURSOR

        when:
        WorldCoordinatesLocation rightClickLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(20)
                .worldMapTileCoordinatesY(20)
                .build()

        uiClient.rightClick(rightClickLocation)
        mouseCursorState = uiClient.getMouseCursorState()

        then:
        assert mouseCursorState == Cursor.DEFAULT_ARROW_CURSOR

    }

    @Unroll
    def "Minigunner health bar and selection cursor show up correctly with #health health"() {
        given:
        UIOptions uiOptions = new UIOptions(drawShroud: false, mapZoomLevel: 1.0)
        setAndAssertUIOptions(uiOptions)

        uiClient.startScenario()


        Unit gdiMinigunner = createGDIMinigunnerAtWorldCoordinatesWithHealth(520, 406, health)

        when:
        uiClient.selectUnit(gdiMinigunner.unitId)

        int testScenarioNumber = health
        String scenarioPrefix = 'minigunner-selection-and-health-bar'

        int startX = 514
        int startY = 392
        int screenshotCompareWidth = 13
        int screenshotCompareHeight = 17


        then:
        assertScreenshotMatches(scenarioPrefix, testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)

        // Data tables must have at least two columns:  https://spockframework.org/spock/docs/1.1/all_in_one.html#data-tables
        // So adding dummy column
        where:
        health  | _
        50      | _
        45      | _
        40      | _
        35      | _
        30      | _
        25      | _
        20      | _
        15      | _
        10      | _
        5       | _

    }


    @Unroll
    def "MCV health bar and selection cursor show up correctly with #health health"() {
        given:
        UIOptions uiOptions = new UIOptions(drawShroud: false, mapZoomLevel: 1.0)
        setAndAssertUIOptions(uiOptions)

        uiClient.startScenario()

        int mcvX = 516
        int mcvY = 348

        Unit mcv = createMCVAtWorldCoordinatesWithHealth(mcvX, mcvY, health)

        when:
        uiClient.selectUnit(mcv.unitId)

        then:
        true

        int testScenarioNumber = health
        String scenarioPrefix = 'mcv-selection-and-health-bar'

        int startX = mcvX - 18
        int startY = mcvY - 18
        int screenshotCompareWidth = 37
        int screenshotCompareHeight = 37


        then:
        assertScreenshotMatches(scenarioPrefix, testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)

        // Data tables must have at least two columns:  https://spockframework.org/spock/docs/1.1/all_in_one.html#data-tables
        // So adding dummy column
        where:
        health  | _
        600     | _
        570     | _
        564     | _
        465     | _
        300     | _
        159     | _
        145     | _



    }








}
