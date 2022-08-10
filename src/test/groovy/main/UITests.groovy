package main


import client.MikeAndConquerSimulationClient
import client.MikeAndConquerUIClient
import client.SequentialEventReader
import domain.Unit
import domain.WorldCoordinatesLocation
import domain.WorldCoordinatesLocationBuilder
import domain.event.SimulationStateUpdateEvent
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.Unroll
import util.TestUtil





class UITests extends Specification {

    MikeAndConquerSimulationClient simulationClient
    MikeAndConquerUIClient uiClient

    def setup() {
        String localhost = "localhost"
        String remoteHost = "192.168.0.110"

//        String host = localhost
        String host = remoteHost

        int port = 5010
        boolean useTimeouts = true
//        boolean useTimeouts = false
        uiClient = new MikeAndConquerUIClient(host, port, useTimeouts )

        simulationClient = new MikeAndConquerSimulationClient(host, 5000, useTimeouts)
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
        TestUtil.assertNumberOfSimulationStateUpdateEvents(simulationClient, 2)

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
        int expectedTotalEvents = 51

        and:
        TestUtil.assertNumberOfSimulationStateUpdateEvents(simulationClient,expectedTotalEvents)

        then:
        List<SimulationStateUpdateEvent> gameEventList = simulationClient.getSimulationStateUpdateEvents()
        SimulationStateUpdateEvent expectedUnitOrderedToMoveEvent = gameEventList.get(2)
        TestUtil.assertUnitOrderedToMoveEvent(expectedUnitOrderedToMoveEvent, minigunnerId, destinationXInWorldCoordinates, destinationYInWorldCoordinates)

        and:
        SimulationStateUpdateEvent expectedUnitArrivedAtDestinationEvent = gameEventList.get(expectedTotalEvents - 1)
        TestUtil.assertUnitArrivedAtDestinationEvent(expectedUnitArrivedAtDestinationEvent, minigunnerId)

    }

    // Unfortunately, these have to be static(or @Shared) to be accessible in the "where" block
    // https://stackoverflow.com/questions/22707195/how-to-use-instance-variable-in-where-section-of-spock-test
    static int selectionBoxLeftmostX = 75
    static int selectionBoxRightmostX = 100
    static int selectionBoxTopmostY = 350
    static int selectionBoxBottommostY = 400

    SequentialEventReader sequentialEventReader

    @Unroll
    def "should be able to drag select multiple GDI minigunners" () {

        given:
        uiClient.startScenario()
        sequentialEventReader = new SequentialEventReader(simulationClient)



        when:
//        Minigunner gdiMinigunner1 = createGDIMinigunnerAtLocation(82,369)
//        Minigunner gdiMinigunner2 = createGDIMinigunnerAtLocation(92,380)
//
//        Minigunner gdiMinigunner3 = createGDIMinigunnerAtLocation(230,300)
//        Minigunner gdiMinigunner4 = createGDIMinigunnerAtLocation(82,300)

        int gdiMinigunner1Id = createGDIMinigunnerAtWorldCoordinates(82,369)
        int gdiMinigunner2Id = createGDIMinigunnerAtWorldCoordinates(92,380)
        int gdiMinigunner3Id = createGDIMinigunnerAtWorldCoordinates(230,300)
        int gdiMinigunner4Id = createGDIMinigunnerAtWorldCoordinates(82,300)

        println gdiMinigunner1Id
        println gdiMinigunner2Id
        println gdiMinigunner3Id
        println gdiMinigunner4Id

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

//        when:
//        gameClient.rightClick(10,10)
//
//        and:
//        gdiMinigunner1 = gameClient.getGdiMinigunnerById(gdiMinigunner1.id)
//        gdiMinigunner2 = gameClient.getGdiMinigunnerById(gdiMinigunner2.id)
//        gdiMinigunner3 = gameClient.getGdiMinigunnerById(gdiMinigunner3.id)
//
//
//        then:
//        assert gdiMinigunner1.selected == false
//        assert gdiMinigunner2.selected == false
//        assert gdiMinigunner3.selected == false


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
