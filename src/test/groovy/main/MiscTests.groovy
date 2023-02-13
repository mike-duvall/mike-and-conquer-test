package main

import domain.*

import domain.event.EventType
import domain.event.FindEventResult
import domain.event.PathStep
import domain.event.SimulationStateUpdateEvent
import spock.lang.Unroll

import util.TestUtil


class MiscTests extends MikeAndConquerTestBase {

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


    def setup() {
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
            simulationClient.createMCV(unitStartLocation)
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
        def unitOrderedToMoveEventData = jsonSlurper.parseText(unitOrderedToMoveEvent.eventData)
        assert unitOrderedToMoveEventData.DestinationXInWorldCoordinates == unitDestinationLocation.XInWorldCoordinates()
        assert unitOrderedToMoveEventData.DestinationYInWorldCoordinates == unitDestinationLocation.YInWorldCoordinates()
        assert unitOrderedToMoveEventData.UnitId == createdUnitId

        when:
        startingTick = unitOrderedToMoveEventData.Timestamp

        then:
        SimulationStateUpdateEvent unitArrivedAtDestinationEvent = sequentialEventReader.waitForEventOfType(EventType.UNIT_ARRIVED_AT_DESTINATION)
        def unitArrivedAtDestinationEventData = jsonSlurper.parseText(unitArrivedAtDestinationEvent.eventData)
        assert unitArrivedAtDestinationEventData.UnitId == createdUnitId

        when:
        endingTick = unitArrivedAtDestinationEventData.Timestamp
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
        setAndAssertSimulationOptions(simulationOptions)

        when:
        WorldCoordinatesLocation startLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(14)
                .worldMapTileCoordinatesY(13)
                .build()

        simulationClient.createGDIMinigunner(startLocation)

        then:
        String expectedCreationEventType = EventType.MINIGUNNER_CREATED
        SimulationStateUpdateEvent minigunnerCreatedEvent = sequentialEventReader.waitForEventOfType(expectedCreationEventType)

        assert minigunnerCreatedEvent.eventType == expectedCreationEventType

        when:
        Unit createdUnit = parseUnitFromEventData(minigunnerCreatedEvent.eventData)
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
        TestUtil.assertUnitOrderedToMoveEvent(
                expectedUnitOrderedToMoveEvent,
                minigunnerId,
                destinationLocation.XInWorldCoordinates(),
                destinationLocation.YInWorldCoordinates())

        and: "Planned path is equal to expected path"
        SimulationStateUpdateEvent expectedUnitMovementPlanCreatedEvent = sequentialEventReader.waitForEventOfType(EventType.UNIT_MOVEMENT_PLAN_CREATED)
        def expectedUnitMovementPlanCreatedEventDataAsObject = jsonSlurper.parseText(expectedUnitMovementPlanCreatedEvent.eventData)

        int expectedNumPathSteps = 11
        assert expectedUnitMovementPlanCreatedEventDataAsObject.UnitId == minigunnerId
        assert expectedUnitMovementPlanCreatedEventDataAsObject.PathSteps.size() == expectedNumPathSteps
        def pathStepList = expectedUnitMovementPlanCreatedEventDataAsObject.PathSteps

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

        and:
        SimulationStateUpdateEvent expectedUnitArrivedAtDestinationEvent = sequentialEventReader.waitForEventOfType(EventType.UNIT_ARRIVED_AT_DESTINATION)
        TestUtil.assertUnitArrivedAtDestinationEvent(expectedUnitArrivedAtDestinationEvent, minigunnerId)

    }

    def "two gdi minigunners attack two nod minigunners" () {
        given:
        SimulationOptions simulationOptions = new SimulationOptions(gameSpeed: GameSpeed.Slow)
        setAndAssertSimulationOptions(simulationOptions)

        uiClient.startScenario()
//        Minigunner gdiMinigunner1 = createRandomGDIMinigunner()
//        Minigunner gdiMinigunner2 = createRandomGDIMinigunner()

//        Unit gdiMinigunner1 = createGDIMinigunnerAtRandomLocation()
//        Unit gdiMinigunner1 = createGDIMinigunnerAtWorldCoordinates(20,20)
        Unit gdiMinigunner1 = createGDIMinigunnerAtWorldMapTileCoordinates(2,2)

//        Unit gdiMinigunner2 = createGDIMinigunnerAtRandomLocation()

//        Minigunner nodMinigunner1 = createRandomNodMinigunnerWithAiTurnedOff()
//        Minigunner nodMinigunner2 = createRandomNodMinigunnerWithAiTurnedOff()

//        Unit nodMinigunner1 = createNodMinigunnerAtRandomLocation()
//        Unit nodMinigunner1 = createNodMinigunnerAtWorldCoordinates(20,80)
//        Unit nodMinigunner1 = createNodMinigunnerAtWorldCoordinates(20,80)
        Unit nodMinigunner1 = createNodMinigunnerAtWorldMapTileCoordinates(1,8)

        when:
        uiClient.selectUnit(gdiMinigunner1.unitId)
        uiClient.selectUnit(nodMinigunner1.unitId)
        WorldCoordinatesLocation neutralLocation = new WorldCoordinatesLocationBuilder()
            .worldMapTileCoordinatesX(5)
            .worldMapTileCoordinatesY(5)
            .build()
//        uiClient.moveMouseToLocation(neutralLocation)
        uiClient.rightClick(neutralLocation)

        and:
        gdiMinigunner1 = uiClient.getUnit(gdiMinigunner1.unitId)
        nodMinigunner1 = uiClient.getUnit(nodMinigunner1.unitId)

        then:
        assert gdiMinigunner1.selected == false
        assert nodMinigunner1.selected == false


        and:
        assertReceivedBeganMissionAttackEvent(gdiMinigunner1.unitId, nodMinigunner1.unitId)

        assertReceviedBeganMovingEvent(gdiMinigunner1.unitId)
        assertReceviedBeganFiringEvent(gdiMinigunner1.unitId)

        assertBulletHitTargetEvent(gdiMinigunner1.unitId, nodMinigunner1.unitId)


        assertReceviedBeganIdleEvent(gdiMinigunner1.unitId)


//        assertReceivedFiredOnTargetEvent(gdiMinigunner1.unitId, nodMinigunner1.unitId)
//
////        assertReceivedBulletHitTargetEvent(nodMinigunner1.unitId)
//
//        assertUnitTakesDamageEvent(nodMinigunner1.unitId)
//
//        assertUnitWeaponReloadedEvent(gdiMinigunner1.unitId)
//
//        assertUnitDestroyedEvent(nodMinigunner1.unitId)
//
//        assertNoneCommandEvent(gdiMinigunner1.unitId)



        // Event4
        // xx BEGIN_MISSION_ATTACK
        // BEGIN_MOVING

        // UnitMovementPlanCreated
        // UnitPositionChanged
        // UnitArrivedAtPathStep
        // END_MOVING
        // BEGIN_FIRING
        // FIRED_ON_UNIT
        // BULLET_HIT_TARGET
        // UNIT_TAKES_DAMAGE
        // UNIT_DESTROYED
        // END_FIRING
        // END_MISSION_ATTACK
        // BEGIN_MISSION_NONE


        // Events3
        // BEGIN_COMMAND - ATTACK_TARGET
        // UnitMovementPlanCreated
        // BEGIN_MOVEMENT
        // UnitPositionChanged
        // UnitArrivedAtPathStep
        // END_MOVEMENT
        // FIRED_ON_UNIT
        // BULLET_HIT_TARGET
        // UNIT_TAKES_DAMAGE
        // UNIT_DESTROYED
        // END_COMMAND - ATTACK_TARGET

        // BEGIN_COMMAND - NONE


        // Events2:
        //  BEGIN_COMMAND - ATTACK_TARGET
        //      BEGIN_STATE - MOVING
        //      BEGIN_STATE - ATTACKING
        //      ARRIVED_IN_RANGE_OF_TARGET
        //      FIRED_ON_TARGET
        //            BULLET_HIT_UNIT
        //            UNIT_TAKES_DAMAGE
        //         WEAPON_RELOADED
        //
        //       UNIT_DESTROYED
        //  BEGIN_COMMAND - NONE
        //  BEGIN_STATE - IDLE


//        Events:
//            BEGIN_ATTACK_COMMAND
//            ARRIVED_IN_RANGE_OF_TARGET
//            FIRED_ON_TARGET
//                BULLET_HIT_UNIT
//                UNIT_TAKES_DAMAGE
//            WEAPON_RELOADED
//
//            UNIT_DESTROYED
//
//            END_ATTACK_COMMAND
//
//
//        assertMinigunnerMovesToFiringPosition(1,4)
//
////        and:
////        assertNodMinigunnerTakesDamageAndDies(nodMinigunner1.id)
//////        then:
//////        assertNodMinigunnerDies(nodMinigunner1.id)
//////
//////        and:
//////        assertNodMinigunnerDies(nodMinigunner2.id)

    }




    Point createRandomMinigunnerPosition()
    {
        Random rand = new Random()

        int minX = 10
        int minY = 10


        // Capping max so it will fit on screen
        // In future, make this use actual map size to cap
        int maxX = 600
        int maxY = 400

        int randomX = rand.nextInt(maxX) + minX
        int randomY = rand.nextInt(maxY) + minY

        Point point = new Point()
        point.x = randomX
        point.y = randomY
        return point

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

    def assertReceivedUnitArrivedAtPathStepEvent(int index, List<SimulationStateUpdateEvent> gameEventList, int expectedXInMapTileSquareCoordinates, int expectedYInMapTileSquareCoordinates) {
        FindEventResult findEventResult = findNextEventAfter(index, gameEventList, EventType.UNIT_ARRIVED_AT_PATH_STEP)

        SimulationStateUpdateEvent unitArrivedAtPathStepEvent = findEventResult.event
        assert unitArrivedAtPathStepEvent.eventType == EventType.UNIT_ARRIVED_AT_PATH_STEP
        def unitArrivedAtPathStepEventData = jsonSlurper.parseText(unitArrivedAtPathStepEvent.eventData)
        assert unitArrivedAtPathStepEventData.PathStep.X == expectedXInMapTileSquareCoordinates * 24 + 12
        assert unitArrivedAtPathStepEventData.PathStep.Y == expectedYInMapTileSquareCoordinates * 24 + 12

        return findEventResult.index
    }

    def assertReceivedBeganMissionAttackEvent(int attackerUnitId, int targetUnitId) {
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.BEGAN_MISSION_ATTACK)
        def eventData = jsonSlurper.parseText(event.eventData)

        assert attackerUnitId == eventData.AttackerUnitId
        assert targetUnitId == eventData.TargetUnitId

        return event
    }


    def assertReceviedBeganMovingEvent(int unitId) {
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.UNIT_BEGAN_MOVING)
        def eventData = jsonSlurper.parseText(event.eventData)

        assert unitId == eventData.UnitId

        return event
    }

//    def assertReceviedStoppedMovingEvent(int unitId) {
//        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.UNIT_STOPPED_MOVING)
//        def eventData = jsonSlurper.parseText(event.eventData)
//
//        assert unitId == eventData.UnitId
//
//        return event
//
//    }

    def assertReceviedBeganFiringEvent(int unitId) {
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.UNIT_BEGAN_FIRING)
        def eventData = jsonSlurper.parseText(event.eventData)

        assert unitId == eventData.UnitId

        return event
    }

    def assertBulletHitTargetEvent(int attackerUnitId, int targetUnitId) {
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.BULLET_HIT_TARGET)
        def eventData = jsonSlurper.parseText(event.eventData)

        assert attackerUnitId == eventData.AttackerUnitId
        assert targetUnitId == eventData.TargetUnitId

        return event
    }


    def assertReceviedBeganIdleEvent(int unitId) {
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.UNIT_BEGAN_IDLE)
        def eventData = jsonSlurper.parseText(event.eventData)

        assert unitId == eventData.UnitId

        return event
    }


//    def assertReceviedStoppedFiringEvent(int unitId) {
//        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.UNIT_STOPPED_FIRING)
//        def eventData = jsonSlurper.parseText(event.eventData)
//
//        assert unitId == eventData.UnitId
//
//        return event
//
//    }



    def assertNoneCommandEvent(int unitId) {
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.NONE_COMMAND_BEGIN)
        def eventData = jsonSlurper.parseText(event.eventData)

        assert unitId == eventData.UnitId

        return event
    }



    def assertReceivedFiredOnTargetEvent(int attackerUnitId, int targetUnitId) {
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.FIRED_ON_UNIT)
        def eventData = jsonSlurper.parseText(event.eventData)

        assert attackerUnitId == eventData.AttackerUnitId
        assert targetUnitId == eventData.TargetUnitId

        return event
    }


    def assertReceivedBulletHitTargetEvent(int targetUnitId) {
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.BULLET_HIT_TARGET)
        def eventData = jsonSlurper.parseText(event.eventData)
        assert targetUnitId == eventData.TargetUnitId
        return event
    }


    def assertUnitTakesDamageEvent(int unitId) {
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.UNIT_TOOK_DAMAGE)
        def eventData = jsonSlurper.parseText(event.eventData)
        assert unitId == eventData.UnitId
        return event
    }

    def assertUnitWeaponReloadedEvent(int unitId) {
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.UNIT_RELOADED_WEAPON)
        def eventData = jsonSlurper.parseText(event.eventData)
        assert unitId == eventData.UnitId
        return event
    }

    def assertUnitDestroyedEvent(int unitId) {
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventOfType(EventType.UNIT_DESTROYED)
        def eventData = jsonSlurper.parseText(event.eventData)
        assert unitId == eventData.UnitId
        return event
    }








    def assertReceivedUnitArrivedAtPathStepEvent( int expectedXInMapTileSquareCoordinates, int expectedYInMapTileSquareCoordinates) {
        SimulationStateUpdateEvent unitArrivedAtPathStepEvent = sequentialEventReader.waitForEventOfType(EventType.UNIT_ARRIVED_AT_PATH_STEP)
        assert unitArrivedAtPathStepEvent.eventType == EventType.UNIT_ARRIVED_AT_PATH_STEP
        def unitArrivedAtPathStepEventData = jsonSlurper.parseText(unitArrivedAtPathStepEvent.eventData)
        assert unitArrivedAtPathStepEventData.PathStep.X == expectedXInMapTileSquareCoordinates * 24 + 12
        assert unitArrivedAtPathStepEventData.PathStep.Y == expectedYInMapTileSquareCoordinates * 24 + 12
    }


}