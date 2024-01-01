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
import util.TestUtil


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

    def "should set mouse cursor correctly when MCV is selected" () {

        given:
        uiClient.startScenario()
        int mcvWorldMapTileX = 21
        int mcvWorldMapTileY = 12

        and:
        int mcvId = createMCVAtWorldMapTileCoordinates(mcvWorldMapTileX, mcvWorldMapTileY)

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
        moveMouseToWorldCoordinates(nodMinigunner.x, nodMinigunner.y)
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
        moveMouseToWorldCoordinates(nodMinigunner.x, nodMinigunner.y)
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

    def "Minigunner health bar and selection cursor show up correctly with 50 health"() {
        given:
        UIOptions uiOptions = new UIOptions(drawShroud: false, mapZoomLevel: 1.0)
        setAndAssertUIOptions(uiOptions)

        uiClient.startScenario()

        int health = 30;
        Unit gdiMinigunner = createGDIMinigunnerAtWorldCoordinates(520, 406)

        when:
        uiClient.selectUnit(gdiMinigunner.unitId)

        int testScenarioNumber = 10
        String scenarioPrefix = 'minigunner-selection-and-health-bar'

        int startX = 514
        int startY = 392
        int screenshotCompareWidth = 13
        int screenshotCompareHeight = 17

        then:
        assertScreenshotMatches(scenarioPrefix, testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)

    }


    def "Minigunner health bar and selection cursor show up correctly with 45 health"() {
        given:
        UIOptions uiOptions = new UIOptions(drawShroud: false, mapZoomLevel: 1.0)
        setAndAssertUIOptions(uiOptions)

        uiClient.startScenario()

        int health = 30;
        Unit gdiMinigunner = createGDIMinigunnerAtWorldCoordinatesWithHealth(520, 406, 45)
        sleep(2000)

        when:
        uiClient.selectUnit(gdiMinigunner.unitId)

        int testScenarioNumber = 9
        String scenarioPrefix = 'minigunner-selection-and-health-bar'

        int startX = 514
        int startY = 392
        int screenshotCompareWidth = 13
        int screenshotCompareHeight = 17

        then:
        assertScreenshotMatches(scenarioPrefix, testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)

    }



}
