package client

import domain.event.SimulationStateUpdateEvent
import spock.util.concurrent.PollingConditions

class SequentialEventReader {


    MikeAndConquerSimulationClient simulationClient

    private int indexOfFurthestEvaluatedEvent = 0;
    List<SimulationStateUpdateEvent> allReceivedEvents

    SequentialEventReader(MikeAndConquerSimulationClient client) {
        this.simulationClient = client
        this.allReceivedEvents = []
    }

    void reset() {
        this.allReceivedEvents = []
        indexOfFurthestEvaluatedEvent = 0;
    }



    SimulationStateUpdateEvent waitForEventMatchedBy(Closure eventMatcher) {

        int timeoutInSeconds = 30
        SimulationStateUpdateEvent foundEvent = null

        def conditions = new PollingConditions(timeout: timeoutInSeconds, initialDelay: 0.3, factor: 1.25)
        conditions.eventually {

            List<SimulationStateUpdateEvent> latestEventList = simulationClient.getSimulationStateUpdateEvents(allReceivedEvents.size())
            allReceivedEvents.addAll(latestEventList)

            boolean done = indexOfFurthestEvaluatedEvent >= allReceivedEvents.size()
            while(!done) {
                SimulationStateUpdateEvent nextEventToEvaluate = allReceivedEvents.get(indexOfFurthestEvaluatedEvent)
                indexOfFurthestEvaluatedEvent++

                if(eventMatcher(nextEventToEvaluate)) {
                    foundEvent = nextEventToEvaluate
                    done = true
                }
                else {
                    done = indexOfFurthestEvaluatedEvent >= allReceivedEvents.size()
                }

            }

//            assert (foundEvent != null) && (eventType == foundEvent.eventType)
            assert (foundEvent != null)


        }

        return foundEvent

    }



    SimulationStateUpdateEvent waitForEventOfType(String eventType) {

        int timeoutInSeconds = 30
        SimulationStateUpdateEvent foundEvent = null

        def conditions = new PollingConditions(timeout: timeoutInSeconds, initialDelay: 0.3, factor: 1.25)
        conditions.eventually {

            List<SimulationStateUpdateEvent> latestEventList = simulationClient.getSimulationStateUpdateEvents(allReceivedEvents.size())
            allReceivedEvents.addAll(latestEventList)

            boolean done = indexOfFurthestEvaluatedEvent >= allReceivedEvents.size()
            while(!done) {
                SimulationStateUpdateEvent nextEventToEvaluate = allReceivedEvents.get(indexOfFurthestEvaluatedEvent)
                indexOfFurthestEvaluatedEvent++

                if(nextEventToEvaluate.eventType == eventType) {
                    foundEvent = nextEventToEvaluate
                    done = true
                }
                else {
                    done = indexOfFurthestEvaluatedEvent >= allReceivedEvents.size()
                }
            }

            assert (foundEvent != null) && (eventType == foundEvent.eventType)


        }

        return foundEvent

    }


}
