package main

import domain.UIOptions
import domain.Unit
import domain.WorldCoordinatesLocation
import domain.WorldCoordinatesLocationBuilder
import domain.event.EventType
import domain.event.SimulationStateUpdateEvent
import groovy.json.JsonSlurper
import spock.lang.Unroll
import util.TestUtil


class UITests extends MikeAndConquerTestBase {


    def setup() {
        UIOptions uiOptions = new UIOptions(drawShroud: false, mapZoomLevel: 2.0)
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

        simulationClient.addMinigunner(minigunnerStartLocation)

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
        SimulationStateUpdateEvent expectedUnitOrderedToMoveEvent = sequentialEventReader.waitForEventOfType(EventType.UNIT_ORDERED_TO_MOVE)
        TestUtil.assertUnitOrderedToMoveEvent(expectedUnitOrderedToMoveEvent, minigunnerId, destinationXInWorldCoordinates, destinationYInWorldCoordinates)

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
        int gdiMinigunner1Id = addGDIMinigunnerAtWorldCoordinates(82,369)
        int gdiMinigunner2Id = addGDIMinigunnerAtWorldCoordinates(92,380)
        int gdiMinigunner3Id = addGDIMinigunnerAtWorldCoordinates(230,300)
        int gdiMinigunner4Id = addGDIMinigunnerAtWorldCoordinates(82,300)

        Set<Integer> uniqueMinigunnerIds = []
        uniqueMinigunnerIds.add(gdiMinigunner1Id)
        uniqueMinigunnerIds.add(gdiMinigunner2Id)
        uniqueMinigunnerIds.add(gdiMinigunner3Id)
        uniqueMinigunnerIds.add(gdiMinigunner4Id)

        then:
        assert uniqueMinigunnerIds.size() == 4

        Unit gdiMinigunner1 = uiClient.getUnit(gdiMinigunner1Id)
        Unit gdiMinigunner2 = uiClient.getUnit(gdiMinigunner2Id)
        Unit gdiMinigunner3 = uiClient.getUnit(gdiMinigunner3Id)
        Unit gdiMinigunner4 = uiClient.getUnit(gdiMinigunner4Id)

        assert gdiMinigunner1.selected == false
        assert gdiMinigunner2.selected == false
        assert gdiMinigunner3.selected == false
        assert gdiMinigunner4.selected == false

        when:
        uiClient.dragSelect(dragStartX, dragStartY, dragEndX, dragEndY)

        gdiMinigunner1 = uiClient.getUnit(gdiMinigunner1Id)
        gdiMinigunner2 = uiClient.getUnit(gdiMinigunner2Id)
        gdiMinigunner3 = uiClient.getUnit(gdiMinigunner3Id)
        gdiMinigunner4 = uiClient.getUnit(gdiMinigunner4Id)

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
        gdiMinigunner1 = uiClient.getUnit(gdiMinigunner1Id)
        gdiMinigunner2 = uiClient.getUnit(gdiMinigunner2Id)
        gdiMinigunner3 = uiClient.getUnit(gdiMinigunner3Id)
        gdiMinigunner4 = uiClient.getUnit(gdiMinigunner4Id)



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
        int mcvId = addMCVAtWorldMapTileCoordinates(mcvWorldMapTileX, mcvWorldMapTileY)

        WorldCoordinatesLocation mountainSquareLocation = createLocationFromWorldMapTileCoordinates(3,0)
        WorldCoordinatesLocation clearSquareLocation = createLocationFromWorldMapTileCoordinates(10, 10)

        when:
        uiClient.selectUnit(mcvId)

        and:
        uiClient.moveMouseToLocation(mountainSquareLocation)

        then:
        String mouseCursorState = uiClient.getMouseCursorState()
        assert mouseCursorState == "MovementNotAllowedCursor"

        when:
        uiClient.moveMouseToLocation(clearSquareLocation)
        mouseCursorState = uiClient.getMouseCursorState()

        then:
        assert mouseCursorState == "MoveToLocationCursor"

        when:
        WorldCoordinatesLocation mcvLocation = createLocationFromWorldMapTileCoordinates(mcvWorldMapTileX, mcvWorldMapTileY)
        uiClient.moveMouseToLocation(mcvLocation)
        mouseCursorState = uiClient.getMouseCursorState()

        then:
        assert mouseCursorState == "BuildConstructionYardCursor"

        when:
        uiClient.rightClick(clearSquareLocation)
        mouseCursorState = uiClient.getMouseCursorState()

        then:
        assert mouseCursorState == "DefaultArrowCursor"

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
