package client

import domain.event.SimulationStateUpdateEvent
import spock.util.concurrent.PollingConditions

class SequentialEventReader3 {


    MikeAndConquerSimulationClient simulationClient

    private int indexOfFurthestEvaluatedEvent = 0;
//    private int indexOfFurthestEventReadFromSimulation = 0;
    List<SimulationStateUpdateEvent> allReceivedEvents

    SequentialEventReader3(MikeAndConquerSimulationClient client) {
        this.simulationClient = client
        this.allReceivedEvents = []
    }


//    SimulationStateUpdateEvent waitForEventOfType(String eventType) {
//
////        int timeoutInSeconds = 30
//        int timeoutInSeconds = 5
//        SimulationStateUpdateEvent foundEvent = null
//
//        def conditions = new PollingConditions(timeout: timeoutInSeconds, initialDelay: 0.3, factor: 1.25)
//        conditions.eventually {
//
//            List<SimulationStateUpdateEvent> latestEventList = simulationClient.getSimulationStateUpdateEvents(allReceivedEvents.size())
//            allReceivedEvents.addAll(latestEventList)
//
//            boolean done = indexOfFurthestEvaluatedEvent >= allReceivedEvents.size()
//            while(!done) {
//                SimulationStateUpdateEvent nextEventToEvaluate = allReceivedEvents.get(indexOfFurthestEvaluatedEvent)
//                indexOfFurthestEvaluatedEvent++
//                if(nextEventToEvaluate.eventType == eventType) {
//                    foundEvent = nextEventToEvaluate
//                    done = true
//                }
//                else {
//                    done = indexOfFurthestEvaluatedEvent >= allReceivedEvents.size()
//                }
//            }
//
//            assert (foundEvent != null) && (foundEvent.eventType == eventType)
////            assert foundEvent.eventType == eventType
//
//
//        }
//
////        assert foundEvent != null
////        assert foundEvent.eventType == eventType
//        return foundEvent
//
//    }

    SimulationStateUpdateEvent waitForEventOfType(String eventType) {

        int timeoutInSeconds = 30
//        int timeoutInSeconds = 5
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
//            assert foundEvent.eventType == eventType



        }

//        assert foundEvent != null
//        assert foundEvent.eventType == eventType
        return foundEvent

    }


}