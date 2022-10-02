package main

import client.MikeAndConquerSimulationClient
import client.MikeAndConquerUIClient
import client.SequentialEventReader
import domain.*
import domain.event.EventBlock
import domain.event.EventType
import domain.event.FindEventResult
import domain.event.PathStep
import domain.event.SimulationStateUpdateEvent
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.concurrent.PollingConditions
import util.TestUtil



class MiscTests extends Specification {

    MikeAndConquerSimulationClient simulationClient
    MikeAndConquerUIClient uiClient
    SequentialEventReader sequentialEventReader
    JsonSlurper jsonSlurper



    enum GameSpeed
    {
        Slowest,
        Slower,
        Slow,
        Moderate,
        Normal,
        Fast,
        Faster,
        Fastest

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

        UIOptions uiOptions = new UIOptions(drawShroud: false, mapZoomLevel: 2.0)
        setAndAssertUIOptions(uiOptions)

        simulationClient.startScenario()
        sleep(1000)

    }

    @Unroll
    def "Assert #unitType travel time is #expectedTimeInMillis ms when gameSpeed is #gameSpeed"() {
        given:

        WorldCoordinatesLocation unitStartLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(10)
                .worldMapTileCoordinatesY(17)
                .build()

        WorldCoordinatesLocation unitDestinationLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(unitStartLocation.XInWorldMapTileCoordinates() + 14)
                .worldMapTileCoordinatesY(unitStartLocation.YInWorldMapTileCoordinates())
                .build()

        long startingTick = -1
        long endingTick = -1


        SimulationOptions simulationOptions = new SimulationOptions(gameSpeed: gameSpeed)
        setAndAssertSimulationOptions(simulationOptions)

        int allowedDelta = 300

        when:
        if(unitType == "Jeep") {
            simulationClient.addJeep(unitStartLocation)
        }
        else if (unitType == "MCV") {
            simulationClient.addMCV(unitStartLocation)
        }
        else {
            throw new Exception ("Unexpected unit type": + unitType)
        }

        then:
        sequentialEventReader.waitForEventOfType(EventType.SCENARIO_INITIALIZED)

        String expectedCreationEventType = unitType + "Created"
        SimulationStateUpdateEvent unitCreatedEvent = sequentialEventReader.waitForEventOfType(expectedCreationEventType)

        when:
        Unit createdUnit = parseUnitFromEventData(unitCreatedEvent.eventData)
        int createdUnitId = createdUnit.unitId

        then:
        assert createdUnit.x == unitStartLocation.XInWorldCoordinates()
        assert createdUnit.y == unitStartLocation.YInWorldCoordinates()

        when:
        simulationClient.moveUnit(createdUnit.unitId, unitDestinationLocation)

        sleep (expectedTimeInMillis - 10000)

        then:
        SimulationStateUpdateEvent unitOrderedToMoveEvent = sequentialEventReader.waitForEventOfType(EventType.UNIT_ORDERED_TO_MOVE)
        def secondEventDataAsObject = jsonSlurper.parseText(unitOrderedToMoveEvent.eventData)
        assert secondEventDataAsObject.DestinationXInWorldCoordinates == unitDestinationLocation.XInWorldCoordinates()
        assert secondEventDataAsObject.DestinationYInWorldCoordinates == unitDestinationLocation.YInWorldCoordinates()
        assert secondEventDataAsObject.UnitId == createdUnitId

        when:
        startingTick = secondEventDataAsObject.Timestamp

        then:
        SimulationStateUpdateEvent unitArrivedAtDestinationEvent = sequentialEventReader.waitForEventOfType(EventType.UNIT_ARRIVED_AT_DESTINATION)
        def thirdEventDataAsObject = jsonSlurper.parseText(unitArrivedAtDestinationEvent.eventData)
        assert thirdEventDataAsObject.UnitId == createdUnitId

        when:
        endingTick = thirdEventDataAsObject.Timestamp
        int startingMilliseconds = startingTick / 10000
        int endingMilliseconds = endingTick / 10000
        int totalTime = endingMilliseconds - startingMilliseconds
        println("totalTime was:" + totalTime)

        then:
        assert totalTime < expectedTimeInMillis + allowedDelta
        assert totalTime > expectedTimeInMillis - allowedDelta

        where:
        unitType    | expectedTotalEvents   | expectedTimeInMillis  | gameSpeed
        "Jeep"      |  140                  |  30236                | "Slowest"
        "Jeep"      |  140                  |  15120                | "Slower"
        "Jeep"      |  140                  |  10082                | "Slow"
        "Jeep"      |  140                  |  7560                 | "Moderate"
        "Jeep"      |  140                  |  5040                 | "Normal"
        "Jeep"      |  140                  | 3697                  | "Fast"
        "Jeep"      |  140                  | 3024                  | "Faster"
        "Jeep"      |  140                  | 2855                  | "Fastest"
        "MCV"       |  302                  | 75597                 | "Slowest"
        "MCV"       |  302                  | 37801                 | "Slower"
        "MCV"       |  302                  | 25201                 | "Slow"
        "MCV"       |  302                  | 18900                 | "Moderate"
        "MCV"       |  302                  | 12601                 | "Normal"
        "MCV"       |  302                  | 9240                  | "Fast"
        "MCV"       |  302                  | 7560                  | "Faster"
        "MCV"       |  302                  | 7139                  | "Fastest"
    }



    def "Move a minigunner and assert correct path is followed"() {
        given:
        int minigunnerId = -1

        SimulationOptions simulationOptions = new SimulationOptions(gameSpeed: GameSpeed.Fastest)
//        simulationOptions.gameSpeed = GameSpeed.Fastest
        setAndAssertSimulationOptions(simulationOptions)


        when:
        WorldCoordinatesLocation startLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(14)
                .worldMapTileCoordinatesY(13)
                .build()

        simulationClient.addMinigunner(startLocation)

        then:
        String expectedCreationEventType = EventType.MINIGUNNER_CREATED
        SimulationStateUpdateEvent minigunnerCreatedEvent = sequentialEventReader.waitForEventOfType(expectedCreationEventType)

        assert minigunnerCreatedEvent.eventType == expectedCreationEventType

        when:
        def unitDataObject = jsonSlurper.parseText(minigunnerCreatedEvent.eventData)
        Unit createdUnit = new Unit()
        createdUnit.unitId = unitDataObject.UnitId
        createdUnit.x = unitDataObject.X
        createdUnit.y = unitDataObject.Y
        minigunnerId = createdUnit.unitId


        then:
        assert minigunnerId != -1

        when:
        WorldCoordinatesLocation destinationLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(7)
                .worldMapTileCoordinatesY(15)
                .build()

        simulationClient.moveUnit(minigunnerId, destinationLocation )


        then:
        SimulationStateUpdateEvent expectedUnitOrderedToMoveEvent = sequentialEventReader.waitForEventOfType(EventType.UNIT_ORDERED_TO_MOVE)

        ArrayList<PathStep> expectedPathSteps = []
        expectedPathSteps.add( new PathStep(x: 14, y:13))
        expectedPathSteps.add( new PathStep(x:14, y:14 ))
        expectedPathSteps.add( new PathStep(x:14 , y:15))
        expectedPathSteps.add( new PathStep(x:13 , y:16))
        expectedPathSteps.add( new PathStep(x:12 , y:17))
        expectedPathSteps.add( new PathStep(x:11 , y:17))
        expectedPathSteps.add( new PathStep(x:10 , y:17))
        expectedPathSteps.add( new PathStep(x: 9, y:17))
        expectedPathSteps.add( new PathStep(x: 8, y:17))
        expectedPathSteps.add( new PathStep(x: 7, y:16))
        expectedPathSteps.add( new PathStep(x: 7, y:15))

        TestUtil.assertUnitOrderedToMoveEvent(
                expectedUnitOrderedToMoveEvent,
                minigunnerId,
                destinationLocation.XInWorldCoordinates(),
                destinationLocation.YInWorldCoordinates())

        and: "Planned path is equal to expected path"
        SimulationStateUpdateEvent expectedUnitMovementPlanCreatedEvent = sequentialEventReader.waitForEventOfType(EventType.UNIT_MOVEMENT_PLAN_CREATED)
//        assert expectedUnitMovementPlanCreatedEvent.eventType == "UnitMovementPlanCreated"
//
//        def jsonSlurper = new JsonSlurper()
        def expectedUnitMovementPlanCreatedEventDataAsObject = jsonSlurper.parseText(expectedUnitMovementPlanCreatedEvent.eventData)

        int expectedNumPathSteps = 11
        assert expectedUnitMovementPlanCreatedEventDataAsObject.UnitId == minigunnerId
        assert expectedUnitMovementPlanCreatedEventDataAsObject.PathSteps.size() == expectedNumPathSteps

        def pathStepList = expectedUnitMovementPlanCreatedEventDataAsObject.PathSteps

        int expectedPathStepIndex = 0
        for(def nextPathStep  : pathStepList) {
            assert nextPathStep.X == expectedPathSteps[expectedPathStepIndex].x
            assert nextPathStep.Y == expectedPathSteps[expectedPathStepIndex].y
            expectedPathStepIndex++
        }

        and: "Actual traveled path is equal to expected path"
        for(expectedPathStepIndex = 0; expectedPathStepIndex < expectedNumPathSteps; expectedPathStepIndex++) {
            assertReceivedUnitArrivedAtPathStepEvent(
                    expectedPathSteps[expectedPathStepIndex].x,
                    expectedPathSteps[expectedPathStepIndex].y
            )
        }

        and: "Assert we are at the end of the event list, no more ArrivedAtPathStep events"
        SimulationStateUpdateEvent expectedUnitArrivedAtDestinationEvent = sequentialEventReader.waitForEventOfType(EventType.UNIT_ARRIVED_AT_DESTINATION)
        TestUtil.assertUnitArrivedAtDestinationEvent(expectedUnitArrivedAtDestinationEvent, minigunnerId)

    }

//    def "Move a jeep and assert correct path is followed"() {
//        given:
//        int unitId = -1
//
//
//        when:
//        WorldCoordinatesLocation startLocation = new WorldCoordinatesLocationBuilder()
//                .worldMapTileCoordinatesX(14)
//                .worldMapTileCoordinatesY(13)
//                .build()
//
//        simulationClient.addJeep(startLocation)
//
//        then:
//        TestUtil.assertNumberOfSimulationStateUpdateEvents(simulationClient, 2)
//
//        when:
//        unitId = TestUtil.assertCreationOfUnitTypeReceived(simulationClient, "JeepCreated")
//
//        then:
//        assert unitId != -1
//
//        when:
//        WorldCoordinatesLocation destinationLocation = new WorldCoordinatesLocationBuilder()
//                .worldMapTileCoordinatesX(7)
//                .worldMapTileCoordinatesY(15)
//                .build()
//
//        simulationClient.moveUnit(unitId, destinationLocation )
//
//        and:
//        int expectedTotalEvents = 102
//
//        and:
//        TestUtil.assertNumberOfSimulationStateUpdateEvents(simulationClient,expectedTotalEvents)
//        ArrayList<PathStep> expectedPathSteps = []
//        expectedPathSteps.add( new PathStep(x: 14, y:13))
//        expectedPathSteps.add( new PathStep(x:14, y:14 ))
//        expectedPathSteps.add( new PathStep(x:14 , y:15))
//        expectedPathSteps.add( new PathStep(x:13 , y:16))
//        expectedPathSteps.add( new PathStep(x:12 , y:17))
//        expectedPathSteps.add( new PathStep(x:11 , y:17))
//        expectedPathSteps.add( new PathStep(x:10 , y:17))
//        expectedPathSteps.add( new PathStep(x: 9, y:17))
//        expectedPathSteps.add( new PathStep(x: 8, y:17))
//        expectedPathSteps.add( new PathStep(x: 7, y:16))
//        expectedPathSteps.add( new PathStep(x: 7, y:15))
//
//        then:
//        List<SimulationStateUpdateEvent> gameEventList = simulationClient.getSimulationStateUpdateEvents()
//        SimulationStateUpdateEvent expectedUnitOrderedToMoveEvent = gameEventList.get(2)
//        TestUtil.assertUnitOrderedToMoveEvent(
//                expectedUnitOrderedToMoveEvent,
//                unitId,
//                destinationLocation.XInWorldCoordinates(),
//                destinationLocation.YInWorldCoordinates())
//
//        and: "Planned path is equal to expected path"
//        SimulationStateUpdateEvent expectedUnitMovementPlanCreatedEvent = gameEventList.get(3)
//        assert expectedUnitMovementPlanCreatedEvent.eventType == "UnitMovementPlanCreated"
//
//        def jsonSlurper = new JsonSlurper()
//        def expectedUnitMovementPlanCreatedEventDataAsObject = jsonSlurper.parseText(expectedUnitMovementPlanCreatedEvent.eventData)
//
//        int expectedNumPathSteps = 11
//        assert expectedUnitMovementPlanCreatedEventDataAsObject.UnitId == unitId
//        assert expectedUnitMovementPlanCreatedEventDataAsObject.PathSteps.size() == expectedNumPathSteps
//
//        def pathStepList = expectedUnitMovementPlanCreatedEventDataAsObject.PathSteps
//
//        int expectedPathStepIndex = 0
//        for(def nextPathStep  : pathStepList) {
//            assert nextPathStep.X == expectedPathSteps[expectedPathStepIndex].x
//            assert nextPathStep.Y == expectedPathSteps[expectedPathStepIndex].y
//            expectedPathStepIndex++
//        }
//
//        and: "Actual traveled path is equal to expected path"
//        int currentGameEventListIndex = 3
//
//        for(expectedPathStepIndex = 0; expectedPathStepIndex < expectedNumPathSteps; expectedPathStepIndex++) {
//
//            currentGameEventListIndex =
//                    assertReceivedUnitArrivedAtPathStepEvent(
//                            currentGameEventListIndex,
//                            gameEventList,
//                            expectedPathSteps[expectedPathStepIndex].x,
//                            expectedPathSteps[expectedPathStepIndex].y
//                    )
//        }
//
//        and: "Assert we are at the end of the event list, no more ArrivedAtPathStep events"
//        assert currentGameEventListIndex == expectedTotalEvents - 1
//
//        then:
//        SimulationStateUpdateEvent expectedUnitArrivedAtDestinationEvent = gameEventList.get(expectedTotalEvents - 1)
//        TestUtil.assertUnitArrivedAtDestinationEvent(expectedUnitArrivedAtDestinationEvent, unitId)
//
//    }


    FindEventResult findNextEventAfter(int index,List<SimulationStateUpdateEvent> gameEventList, String eventType) {
        int totalNumEvents = gameEventList.size()
        SimulationStateUpdateEvent foundEvent = null
        boolean  done = false
        while(!done) {

            SimulationStateUpdateEvent nextEvent = gameEventList.get(index)
            if(nextEvent.eventType == eventType) {
                done = true
                foundEvent = nextEvent
            }

            if(index >= totalNumEvents - 1) {
                done = true
            }
            index++
        }
        FindEventResult result = new FindEventResult()
        result.event = foundEvent
        result.index = index
        return result

    }

    def assertReceivedUnitArrivedAtPathStepEvent(int index, gameEventList, int expectedXInMapTileSquareCoordinates, int expectedYInMapTileSquareCoordinates) {
        FindEventResult findEventResult = findNextEventAfter(index, gameEventList, "UnitArrivedAtPathStep")

        SimulationStateUpdateEvent unitArrivedAtPathStepEvent = findEventResult.event
        assert unitArrivedAtPathStepEvent.eventType == "UnitArrivedAtPathStep"
        def jsonSlurper = new JsonSlurper()
        def unitArrivedAtPathStepEventData = jsonSlurper.parseText(unitArrivedAtPathStepEvent.eventData)
        assert unitArrivedAtPathStepEventData.PathStep.X == expectedXInMapTileSquareCoordinates * 24 + 12
        assert unitArrivedAtPathStepEventData.PathStep.Y == expectedYInMapTileSquareCoordinates * 24 + 12

        return findEventResult.index

    }

    def assertReceivedUnitArrivedAtPathStepEvent( int expectedXInMapTileSquareCoordinates, int expectedYInMapTileSquareCoordinates) {
//        FindEventResult findEventResult = findNextEventAfter(index, gameEventList, "UnitArrivedAtPathStep")
        SimulationStateUpdateEvent unitArrivedAtPathStepEvent = sequentialEventReader.waitForEventOfType(EventType.UNIT_ARRIVED_AT_PATH_STEP)

//        SimulationStateUpdateEvent unitArrivedAtPathStepEvent = findEventResult.event
        assert unitArrivedAtPathStepEvent.eventType == "UnitArrivedAtPathStep"
        def jsonSlurper = new JsonSlurper()
        def unitArrivedAtPathStepEventData = jsonSlurper.parseText(unitArrivedAtPathStepEvent.eventData)
        assert unitArrivedAtPathStepEventData.PathStep.X == expectedXInMapTileSquareCoordinates * 24 + 12
        assert unitArrivedAtPathStepEventData.PathStep.Y == expectedYInMapTileSquareCoordinates * 24 + 12

//        return findEventResult.index

    }


    def assertNumberOfSimulationStateUpdateEvents(int numEventsToAssert) {
        int timeoutInSeconds = 30
        List<SimulationStateUpdateEvent>  gameEventList
        def conditions = new PollingConditions(timeout: timeoutInSeconds, initialDelay: 1.5, factor: 1.25)
        conditions.eventually {
            gameEventList = simulationClient.getSimulationStateUpdateEvents()
            assert gameEventList.size() == numEventsToAssert
        }
        return true

    }
    void assertExpectedEventList(List<SimulationStateUpdateEvent> simulationStateUpdateEvents, ArrayList<EventBlock> expectedEventList) {
        boolean done = false
        int actualEventIndex = 0

        for(EventBlock eventBlock in expectedEventList) {
            for(int i = 0; i < eventBlock.numberOfEvents; i++) {
                SimulationStateUpdateEvent simulationStateUpdateEvent = simulationStateUpdateEvents.get(actualEventIndex)
                assert simulationStateUpdateEvent.eventType == eventBlock.eventType
                actualEventIndex++
            }
        }

        assert actualEventIndex == simulationStateUpdateEvents.size()

    }

    Unit parseUnitFromEventData(String unitCreatedEventData) {
        def unitDataObject = jsonSlurper.parseText(unitCreatedEventData)
        Unit createdUnit = new Unit()
        createdUnit.unitId = unitDataObject.UnitId
        createdUnit.x = unitDataObject.X
        createdUnit.y = unitDataObject.Y

        return createdUnit

    }



}