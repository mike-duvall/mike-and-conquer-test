package client

import domain.event.SimulationStateUpdateEvent
import spock.util.concurrent.PollingConditions

class SequentialEventReader {

    private int currentIndex = 0
    MikeAndConquerSimulationClient simulationClient

    SequentialEventReader(MikeAndConquerSimulationClient client) {
        this.simulationClient = client
    }

    SimulationStateUpdateEvent waitForEventOfType(String eventType) {
        waitForEventOfType(eventType, 0)
    }

    SimulationStateUpdateEvent waitForEventOfType(String eventType, int startIndex) {

        int timeoutInSeconds = 30
        SimulationStateUpdateEvent foundEvent = null
        List<SimulationStateUpdateEvent> gameEventList
        def conditions = new PollingConditions(timeout: timeoutInSeconds, initialDelay: 0.3, factor: 1.25)
        conditions.eventually {
            gameEventList = simulationClient.getSimulationStateUpdateEvents(startIndex)
            int readToIndex = 0
            if( gameEventList.size() > currentIndex) {
                for(SimulationStateUpdateEvent event : gameEventList) {

                    if(readToIndex < currentIndex) {
                        readToIndex++
                        continue
                    }
                    currentIndex++
                    foundEvent = event
                    assert event.eventType == eventType
                    return foundEvent
                }
            }
        }

        return foundEvent


    }
}
