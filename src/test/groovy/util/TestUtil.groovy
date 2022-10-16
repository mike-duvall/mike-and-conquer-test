package util

import client.MikeAndConquerSimulationClient
import client.MikeAndConquerUIClient
import domain.event.SimulationStateUpdateEvent
import domain.Unit
import groovy.json.JsonSlurper
import spock.util.concurrent.PollingConditions

class TestUtil {


    static boolean  assertUnitIsSelected(MikeAndConquerUIClient uiClient, int unitId) {

        int timeoutInSeconds = 10
        def conditions = new PollingConditions(timeout: timeoutInSeconds, initialDelay: 1.5, factor: 1.25)
        conditions.eventually {
            Unit retrievedUnit = uiClient.getUnit(unitId)
            assert retrievedUnit.selected == true


        }
        return true
    }

    static void assertUnitOrderedToMoveEvent(
            SimulationStateUpdateEvent expectedUnitOrderedToMoveEvent,
            int minigunnerId,
            int destinationXInWorldCoordinates,
            int destinationYInWorldCoordinates) {
        assert expectedUnitOrderedToMoveEvent.eventType == "UnitOrderedToMove"

        def jsonSlurper = new JsonSlurper()
        def unitOrderedToMveDataAsObject = jsonSlurper.parseText(expectedUnitOrderedToMoveEvent.eventData)

        assert unitOrderedToMveDataAsObject.DestinationXInWorldCoordinates == destinationXInWorldCoordinates
        assert unitOrderedToMveDataAsObject.DestinationYInWorldCoordinates == destinationYInWorldCoordinates
        assert unitOrderedToMveDataAsObject.UnitId == minigunnerId
    }


    static void assertUnitArrivedAtDestinationEvent(SimulationStateUpdateEvent expectedUnitArrivedAtDestinationEvent, int minigunnerId) {
        def jsonSlurper = new JsonSlurper()

        assert expectedUnitArrivedAtDestinationEvent.eventType == "UnitArrivedAtDestination"
        def unitArrivedAtDestinationDataAsObject = jsonSlurper.parseText(expectedUnitArrivedAtDestinationEvent.eventData)
        assert unitArrivedAtDestinationDataAsObject.UnitId == minigunnerId

    }
}
