package main


import client.MikeAndConquerSimulationClient
import client.MikeAndConquerUIClient
import client.SequentialEventReader
import domain.UIOptions
import domain.Unit
import domain.WorldCoordinatesLocation
import domain.WorldCoordinatesLocationBuilder
import domain.event.EventType
import domain.event.SimulationStateUpdateEvent
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.Unroll
import util.TestUtil





class UITests extends Specification {

    MikeAndConquerSimulationClient simulationClient
    MikeAndConquerUIClient uiClient
    SequentialEventReader sequentialEventReader

    def setup() {
        String localhost = "localhost"
        String remoteHost = "192.168.0.110"

//        String host = localhost
        String host = remoteHost

        boolean useTimeouts = true
//        boolean useTimeouts = false
        uiClient = new MikeAndConquerUIClient(host, useTimeouts )
        UIOptions uiOptions = new UIOptions()
        uiOptions.drawShroud = false
        uiOptions.mapZoomLevel = 2.0
        uiClient.setUIOptions(uiOptions)


        simulationClient = new MikeAndConquerSimulationClient(host,  useTimeouts)
        sequentialEventReader = new SequentialEventReader(simulationClient)
//        simulationClient.resetScenario()
//        sleep(1000)


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
//        TestUtil.assertNumberOfSimulationStateUpdateEvents(simulationClient, 2)
//        TestUtil.assertNumberOfSimulationStateUpdateEvents(simulationClient, 35)
        TestUtil.assertNumberOfSimulationStateUpdateEvents(simulationClient, 31)

        when:
        minigunnerId = TestUtil.assertMinigunnerCreatedEventReceived(simulationClient)

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

        and:
//        int expectedTotalEvents = 51
        int expectedTotalEvents = 120

        and:
        TestUtil.assertNumberOfSimulationStateUpdateEvents(simulationClient,expectedTotalEvents)

        then:
//        List<SimulationStateUpdateEvent> gameEventList = simulationClient.getSimulationStateUpdateEvents()
//        SimulationStateUpdateEvent expectedUnitOrderedToMoveEvent = gameEventList.get(2)
        SimulationStateUpdateEvent expectedUnitOrderedToMoveEvent = sequentialEventReader.waitForEventOfType(EventType.UNIT_ORDERED_TO_MOVE)
        TestUtil.assertUnitOrderedToMoveEvent(expectedUnitOrderedToMoveEvent, minigunnerId, destinationXInWorldCoordinates, destinationYInWorldCoordinates)



        and:
//        SimulationStateUpdateEvent expectedUnitArrivedAtDestinationEvent = gameEventList.get(expectedTotalEvents - 1)
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
        int gdiMinigunner1Id = createGDIMinigunnerAtWorldCoordinates(82,369)
        int gdiMinigunner2Id = createGDIMinigunnerAtWorldCoordinates(92,380)
        int gdiMinigunner3Id = createGDIMinigunnerAtWorldCoordinates(230,300)
        int gdiMinigunner4Id = createGDIMinigunnerAtWorldCoordinates(82,300)

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
        sleep(1000)


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


    int createGDIMinigunnerAtWorldCoordinates(int xInWorldCoordinates, int yInWorldCoordinates) {
        WorldCoordinatesLocationBuilder minigunnerLocationBuilder = new WorldCoordinatesLocationBuilder()

        simulationClient.addMinigunner(minigunnerLocationBuilder
                .worldCoordinatesX(xInWorldCoordinates)
                .worldCoordinatesY(yInWorldCoordinates)
                .build() )

        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType("MinigunnerCreated")

        def jsonSlurper = new JsonSlurper()
        def eventData = jsonSlurper.parseText(event.eventData)
        return eventData.UnitId

    }



}
