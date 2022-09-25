package client

import domain.event.SimulationStateUpdateEvent
import spock.util.concurrent.PollingConditions

class SequentialEventReader2 {

    private int currentIndex = 0
    MikeAndConquerSimulationClient simulationClient

    SequentialEventReader2(MikeAndConquerSimulationClient client) {
        this.simulationClient = client
    }


    SimulationStateUpdateEvent waitForEventOfType(String eventType) {

        int timeoutInSeconds = 30
        SimulationStateUpdateEvent foundEvent = null
        List<SimulationStateUpdateEvent> gameEventList
        def conditions = new PollingConditions(timeout: timeoutInSeconds, initialDelay: 0.3, factor: 1.25)
        conditions.eventually {
            gameEventList = simulationClient.getSimulationStateUpdateEvents(currentIndex)
            for(SimulationStateUpdateEvent event : gameEventList) {

                currentIndex++
//                assert event.eventType == eventType
                if(event.eventType == eventType) {
                    foundEvent = event
                }
            }

        }

        assert foundEvent.eventType == eventType
        return foundEvent

    }
}
