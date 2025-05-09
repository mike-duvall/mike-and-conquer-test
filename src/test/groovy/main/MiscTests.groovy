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
        UIOptions uiOptions = new UIOptions(drawShroud: false, mapZoomLevel: 1.8)
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
        assert createdUnit.xInWorldCoordinates == unitStartLocation.XInWorldCoordinates()
        assert createdUnit.yInWorldCoordinates == unitStartLocation.YInWorldCoordinates()

        when:
        simulationClient.moveUnit(createdUnit.unitId, unitDestinationLocation)

        sleep (expectedTimeInMillis - 10000)

        then:
        SimulationStateUpdateEvent beganMissionMoveToDestinationEvent = sequentialEventReader.waitForEventOfType(EventType.BEGAN_MISSION_MOVE_TO_DESTINATION)
        def beganMessionMoveToDestinationEventData = jsonSlurper.parseText(beganMissionMoveToDestinationEvent.eventData)
        assert beganMessionMoveToDestinationEventData.DestinationXInWorldCoordinates == unitDestinationLocation.XInWorldCoordinates()
        assert beganMessionMoveToDestinationEventData.DestinationYInWorldCoordinates == unitDestinationLocation.YInWorldCoordinates()
        assert beganMessionMoveToDestinationEventData.UnitId == createdUnitId

        when:
        startingTick = beganMessionMoveToDestinationEventData.Timestamp

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



        then: "Planned path is equal to expected path"
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

        and:
        SimulationStateUpdateEvent expectedBeganMissionMoveToDestinationEvent = sequentialEventReader.waitForEventOfType(EventType.BEGAN_MISSION_MOVE_TO_DESTINATION)
        TestUtil.assertBeganMissionMoveToDestinationEvent(
                expectedBeganMissionMoveToDestinationEvent,
                minigunnerId,
                destinationLocation.XInWorldCoordinates(),
                destinationLocation.YInWorldCoordinates())


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


    def "Move an MCV and assert correct path is followed"() {
        given:
        int mcvId = -1

        SimulationOptions simulationOptions = new SimulationOptions(gameSpeed: GameSpeed.Fastest)
        setAndAssertSimulationOptions(simulationOptions)

        when:
        WorldCoordinatesLocation startLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(14)
                .worldMapTileCoordinatesY(13)
                .build()

        simulationClient.createMCV(startLocation)

        then:
        String expectedCreationEventType = "MCVCreated"
        SimulationStateUpdateEvent mcvCreatedEvent = sequentialEventReader.waitForEventOfType(expectedCreationEventType)

        assert mcvCreatedEvent.eventType == expectedCreationEventType

        when:
        Unit createdUnit = parseUnitFromEventData(mcvCreatedEvent.eventData)
        mcvId = createdUnit.unitId

        then:
        assert mcvId != -1

        when:
        WorldCoordinatesLocation destinationLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(7)
                .worldMapTileCoordinatesY(15)
                .build()

        simulationClient.moveUnit(mcvId, destinationLocation)

        then: "Planned path is equal to expected path"
        SimulationStateUpdateEvent expectedUnitMovementPlanCreatedEvent = sequentialEventReader.waitForEventOfType(EventType.UNIT_MOVEMENT_PLAN_CREATED)
        def expectedUnitMovementPlanCreatedEventDataAsObject = jsonSlurper.parseText(expectedUnitMovementPlanCreatedEvent.eventData)

        int expectedNumPathSteps = 11
        assert expectedUnitMovementPlanCreatedEventDataAsObject.UnitId == mcvId
        assert expectedUnitMovementPlanCreatedEventDataAsObject.PathSteps.size() == expectedNumPathSteps
        def pathStepList = expectedUnitMovementPlanCreatedEventDataAsObject.PathSteps

        ArrayList<PathStep> expectedPathSteps = []
        expectedPathSteps.add(new PathStep(x: 14, y: 13))
        expectedPathSteps.add(new PathStep(x: 14, y: 14))
        expectedPathSteps.add(new PathStep(x: 14, y: 15))
        expectedPathSteps.add(new PathStep(x: 13, y: 16))
        expectedPathSteps.add(new PathStep(x: 12, y: 17))
        expectedPathSteps.add(new PathStep(x: 11, y: 17))
        expectedPathSteps.add(new PathStep(x: 10, y: 17))
        expectedPathSteps.add(new PathStep(x: 9, y: 17))
        expectedPathSteps.add(new PathStep(x: 8, y: 17))
        expectedPathSteps.add(new PathStep(x: 7, y: 16))
        expectedPathSteps.add(new PathStep(x: 7, y: 15))

        int expectedPathStepIndex = 0
        for (def nextPathStep : pathStepList) {
            assert nextPathStep.X == expectedPathSteps[expectedPathStepIndex].x
            assert nextPathStep.Y == expectedPathSteps[expectedPathStepIndex].y
            expectedPathStepIndex++
        }

        and:
        SimulationStateUpdateEvent expectedBeganMissionMoveToDestinationEvent = sequentialEventReader.waitForEventOfType(EventType.BEGAN_MISSION_MOVE_TO_DESTINATION)
        TestUtil.assertBeganMissionMoveToDestinationEvent(
                expectedBeganMissionMoveToDestinationEvent,
                mcvId,
                destinationLocation.XInWorldCoordinates(),
                destinationLocation.YInWorldCoordinates())

        and: "Actual traveled path is equal to expected path"
        for (expectedPathStepIndex = 0; expectedPathStepIndex < expectedNumPathSteps; expectedPathStepIndex++) {
            assertReceivedUnitArrivedAtPathStepEvent(
                    expectedPathSteps[expectedPathStepIndex].x,
                    expectedPathSteps[expectedPathStepIndex].y
            )
        }

        and:
        SimulationStateUpdateEvent expectedUnitArrivedAtDestinationEvent = sequentialEventReader.waitForEventOfType(EventType.UNIT_ARRIVED_AT_DESTINATION)
        TestUtil.assertUnitArrivedAtDestinationEvent(expectedUnitArrivedAtDestinationEvent, mcvId)
    }

    def "two gdi minigunners attack two nod minigunners" () {
        given:
        SimulationOptions simulationOptions = new SimulationOptions(gameSpeed: GameSpeed.Slow)
        setAndAssertSimulationOptions(simulationOptions)

        uiClient.startScenario()

        Unit gdiMinigunner1 = createGDIMinigunnerAtRandomLocation()
        Unit gdiMinigunner2 = createGDIMinigunnerAtRandomLocation()

        Unit nodMinigunner1 = createNodMinigunnerAtRandomLocation()
        Unit nodMinigunner2 = createNodMinigunnerAtRandomLocation()

        when:
        uiClient.selectUnit(gdiMinigunner1.unitId)
        uiClient.selectUnit(nodMinigunner1.unitId)
        WorldCoordinatesLocation neutralLocation = new WorldCoordinatesLocationBuilder()
            .worldMapTileCoordinatesX(5)
            .worldMapTileCoordinatesY(5)
            .build()
        uiClient.rightClick(neutralLocation)

        and:
        gdiMinigunner1 = uiClient.getUnit(gdiMinigunner1.unitId)
        nodMinigunner1 = uiClient.getUnit(nodMinigunner1.unitId)

        then:
        assert gdiMinigunner1.selected == false
        assert nodMinigunner1.selected == false

        when:
        uiClient.selectUnit(gdiMinigunner2.unitId)
        uiClient.selectUnit(nodMinigunner2.unitId)
        uiClient.rightClick(neutralLocation)

        and:
        gdiMinigunner2 = uiClient.getUnit(gdiMinigunner2.unitId)
        nodMinigunner2 = uiClient.getUnit(nodMinigunner2.unitId)

        then:
        assert gdiMinigunner2.selected == false
        assert nodMinigunner2.selected == false


        and:
        assertMinigunnerAttacksAndKillsMinigunner(gdiMinigunner1.unitId, nodMinigunner1.unitId)

        and:
        sequentialEventReader.reset();
        assertMinigunnerAttacksAndKillsMinigunner(gdiMinigunner2.unitId, nodMinigunner2.unitId)
    }

    def assertMinigunnerAttacksAndKillsMinigunner(int minigunner1Id, int minigunner2Id) {

        int expectedAmountOfDamage = 10

        assertReceivedBeganMissionAttackEvent(minigunner1Id, minigunner2Id)

        assertReceivedBeganMovingEvent(minigunner1Id)
        assertReceviedBeganFiringEvent(minigunner1Id)

        assertBulletHitTargetEvent(minigunner1Id, minigunner2Id)

        assertUnitTookDamageEvent(minigunner2Id, expectedAmountOfDamage, 40)
        assertUnitWeaponReloadedEvent(minigunner1Id)

        assertBulletHitTargetEvent(minigunner1Id, minigunner2Id)
        assertUnitTookDamageEvent(minigunner2Id, expectedAmountOfDamage, 30)
        assertUnitWeaponReloadedEvent(minigunner1Id)

        assertBulletHitTargetEvent(minigunner1Id, minigunner2Id)
        assertUnitTookDamageEvent(minigunner2Id, expectedAmountOfDamage, 20)
        assertUnitWeaponReloadedEvent(minigunner1Id)

        assertBulletHitTargetEvent(minigunner1Id, minigunner2Id)
        assertUnitTookDamageEvent(minigunner2Id, expectedAmountOfDamage, 10)
        assertUnitWeaponReloadedEvent(minigunner1Id)

        assertBulletHitTargetEvent(minigunner1Id, minigunner2Id)
        assertUnitTookDamageEvent(minigunner2Id, expectedAmountOfDamage, 0)

        assertUnitDestroyedEvent(minigunner2Id)

        assertBeganMissionNoneEvent(minigunner1Id)


        assertUnitWeaponReloadedEvent(minigunner1Id)

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

    def unitBeganMissionAttackMatcher(int attackerUnitId, int targetUnitId) {
        return {SimulationStateUpdateEvent e ->
            def eventData = jsonSlurper.parseText(e.eventData)
            return (e.eventType == EventType.BEGAN_MISSION_ATTACK) && (attackerUnitId == eventData.AttackerUnitId) && (targetUnitId == eventData.TargetUnitId)
        }

    }

    def assertReceivedBeganMissionAttackEvent(int attackerUnitId, int targetUnitId) {
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventMatchedBy(
                unitBeganMissionAttackMatcher(attackerUnitId, targetUnitId)
        )
        return event
    }



    def unitBeganMovingMatcher(int unitId  ) {
        return {SimulationStateUpdateEvent e ->
            def eventData = jsonSlurper.parseText(e.eventData)
            return (e.eventType == EventType.UNIT_BEGAN_MOVING) && (unitId == eventData.UnitId)
        }
    }

    def assertReceivedBeganMovingEvent(int unitId) {
        SimulationStateUpdateEvent event =
                sequentialEventReader.waitForEventMatchedBy(unitBeganMovingMatcher(unitId))

        return event
    }


    def unitBeganFiringMatcher(int unitId  ) {
        return {SimulationStateUpdateEvent e ->
            def eventData = jsonSlurper.parseText(e.eventData)
            return (e.eventType == EventType.UNIT_BEGAN_FIRING) && (unitId == eventData.UnitId)
        }
    }

    def assertReceviedBeganFiringEvent(int unitId) {
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventMatchedBy(unitBeganFiringMatcher(unitId))
        return event
    }

    def bulletHitTargetMatcher(int attackerUnitId, int targetUnitId) {
        return {SimulationStateUpdateEvent e ->
            def eventData = jsonSlurper.parseText(e.eventData)
            return (e.eventType == EventType.BULLET_HIT_TARGET) && (attackerUnitId == eventData.AttackerUnitId) && (targetUnitId == eventData.TargetUnitId)
        }
    }

    def assertBulletHitTargetEvent(int attackerUnitId, int targetUnitId) {
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventMatchedBy(bulletHitTargetMatcher(attackerUnitId, targetUnitId))
        return event
    }


    def unitTookDamageMatcher(int unitId, int expectedAmountOfDamage, int expectedNewHealthAmount) {
        return {SimulationStateUpdateEvent e ->
            def eventData = jsonSlurper.parseText(e.eventData)
            return  (e.eventType == EventType.UNIT_TOOK_DAMAGE) &&
                    (unitId == eventData.UnitId) &&
                    (expectedAmountOfDamage == eventData.AmountOfDamage) &&
                    (expectedNewHealthAmount == eventData.NewHealthAmount)
        }

    }

    def assertUnitTookDamageEvent(int unitId, int expectedAmountOfDamage, int expectedNewHealthAmount) {
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventMatchedBy(unitTookDamageMatcher(unitId, expectedAmountOfDamage, expectedNewHealthAmount))
        return event
    }

    def weaponReloadedMatcher(int unitId  ) {
        return {SimulationStateUpdateEvent e ->
            def eventData = jsonSlurper.parseText(e.eventData)
            return (e.eventType == EventType.UNIT_RELOADED_WEAPON) && (unitId == eventData.UnitId)
        }
    }


    def assertUnitWeaponReloadedEvent(int unitId) {
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventMatchedBy(weaponReloadedMatcher(unitId))
        return event
    }

    def unitDestroyedMatcher(int unitId  ) {
        return {SimulationStateUpdateEvent e ->
            def eventData = jsonSlurper.parseText(e.eventData)
            return (e.eventType == EventType.UNIT_DESTROYED) && (unitId == eventData.UnitId)
        }
    }

    def assertUnitDestroyedEvent(int unitId) {
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventMatchedBy(unitDestroyedMatcher(unitId))
        return event
    }

    def unitBeganMissionNoneMatcher(int unitId  ) {
        return {SimulationStateUpdateEvent e ->
            def eventData = jsonSlurper.parseText(e.eventData)
            return (e.eventType == EventType.BEGAN_MISSION_NONE) && (unitId == eventData.UnitId)
        }
    }


    def assertBeganMissionNoneEvent(int unitId) {
        SimulationStateUpdateEvent event = sequentialEventReader.waitForEventMatchedBy(unitBeganMissionNoneMatcher(unitId))
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