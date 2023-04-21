package client

import domain.SimulationCommand
import domain.SimulationOptions
import domain.WorldCoordinatesLocation
import domain.event.SimulationStateUpdateEvent
import groovy.json.JsonOutput
import groovyx.net.http.RESTClient
import org.apache.http.params.CoreConnectionPNames

class MikeAndConquerSimulationClient extends BaseClient {


    String hostUrl

    int port = 5000



    MikeAndConquerSimulationClient(String host,  boolean useTimeouts = true) {
        hostUrl = "http://$host:$port"
        restClient = new RESTClient(hostUrl)

        if(useTimeouts) {
            restClient.client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, new Integer(5000))
            restClient.client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, new Integer(5000))
        }
    }


    void doPostSimulationCommand(Object command) {
        doPostRestCall('/simulation/command', command)
    }

    void setSimulationOptions(SimulationOptions simulationOptions) {
        SimulationCommand command = new SimulationCommand()
        command.commandType = SimulationCommand.SET_OPTIONS

        def commandParams =
                [
                    gameSpeed: simulationOptions.gameSpeed
                ]

        command.jsonCommandData =  JsonOutput.toJson(commandParams)

        doPostSimulationCommand(command)
    }


    SimulationOptions getSimulationOptions() {

        def resp = doGetRestCall('/simulation/query/options')

        SimulationOptions simulationOptions = new SimulationOptions()

        simulationOptions.gameSpeed = resp.responseData.gameSpeed

        return simulationOptions
    }


    void removeUnit(int unitId) {

        SimulationCommand createUnitCommand = new SimulationCommand()
        createUnitCommand.commandType = SimulationCommand.REMOVE_UNIT

        def commandParams =
                [
                        unitId: unitId
                ]

        createUnitCommand.jsonCommandData =  JsonOutput.toJson(commandParams)
        doPostSimulationCommand( createUnitCommand)
    }


    void createGDIMinigunner(WorldCoordinatesLocation location) {

        SimulationCommand createUnitCommand = new SimulationCommand()
        createUnitCommand.commandType = SimulationCommand.CREATE_GDI_MINIGUNNER

        def commandParams =
            [
                startLocationXInWorldCoordinates: location.XInWorldCoordinates(),
                startLocationYInWorldCoordinates: location.YInWorldCoordinates()
            ]

        createUnitCommand.jsonCommandData =  JsonOutput.toJson(commandParams)

        doPostSimulationCommand( createUnitCommand)
    }

    void createNodMinigunner(WorldCoordinatesLocation location) {

        SimulationCommand createUnitCommand = new SimulationCommand()
        createUnitCommand.commandType = SimulationCommand.CREATE_NOD_MINIGUNNER

        def commandParams =
                [
                        startLocationXInWorldCoordinates: location.XInWorldCoordinates(),
                        startLocationYInWorldCoordinates: location.YInWorldCoordinates()
                ]

        createUnitCommand.jsonCommandData =  JsonOutput.toJson(commandParams)

        doPostSimulationCommand( createUnitCommand)
    }



    void createGDIMinigunnerAtRandomLocation() {
        SimulationCommand createUnitCommand = new SimulationCommand()
        createUnitCommand.commandType = SimulationCommand.CREATE_GDI_MINIGUNNER_AT_RANDOM_LOCATION
        doPostSimulationCommand( createUnitCommand)
    }

    void createDeactivatedNodMinigunnerAtRandomLocation() {

        SimulationCommand createUnitCommand = new SimulationCommand()
        createUnitCommand.commandType = SimulationCommand.CREATE_NOD_MINIGUNNER_AT_RANDOM_LOCATION

        def commandParams =
                [
                        deactivated: true
                ]

        createUnitCommand.jsonCommandData =  JsonOutput.toJson(commandParams)

        doPostSimulationCommand( createUnitCommand)
    }



    void addJeep(WorldCoordinatesLocation location) {

        SimulationCommand command = new SimulationCommand()
        command.commandType = SimulationCommand.CREATE_JEEP

        def commandParams =
                [
                        startLocationXInWorldCoordinates: location.XInWorldCoordinates(),
                        startLocationYInWorldCoordinates: location.YInWorldCoordinates()
                ]

        command.jsonCommandData =  JsonOutput.toJson(commandParams)

        doPostSimulationCommand( command)
    }

    void createMCV(WorldCoordinatesLocation location) {

        SimulationCommand command = new SimulationCommand()
        command.commandType = SimulationCommand.CREATE_MVC

        def commandParams =
                [
                        startLocationXInWorldCoordinates: location.XInWorldCoordinates(),
                        startLocationYInWorldCoordinates: location.YInWorldCoordinates()
                ]

        command.jsonCommandData =  JsonOutput.toJson(commandParams)

        doPostSimulationCommand( command)
    }


    void startScenario() {

        SimulationCommand command = new SimulationCommand()
        command.commandType = SimulationCommand.START_SCENARIO

        doPostSimulationCommand( command)
    }


    List<SimulationStateUpdateEvent> getSimulationStateUpdateEvents(int startIndex) {
        def resp = doGetRestCall('/simulation/query/events', ['startIndex': startIndex])

        int numItems = resp.responseData.size

        List<SimulationStateUpdateEvent> allSimulationStateUpdateEvents = []
        for (int i = 0; i < numItems; i++) {
            SimulationStateUpdateEvent simulationStateUpdateEvent = new SimulationStateUpdateEvent()
            simulationStateUpdateEvent.eventType = resp.responseData[i].eventType
            simulationStateUpdateEvent.eventData = resp.responseData[i].eventData
            allSimulationStateUpdateEvents.add(simulationStateUpdateEvent)
        }
        return allSimulationStateUpdateEvents
    }

    int getSimulationStateUpdateEventsCurrentIndex() {

        def resp = doGetRestCall('/simulation/query/eventscount')

        int numItems = resp.responseData

        return numItems
    }



    void moveUnit(int unitId, WorldCoordinatesLocation location) {

        SimulationCommand command = new SimulationCommand()
        command.commandType = SimulationCommand.ORDER_UNIT_TO_MOVE

        def commandParams =
                [
                        unitId: unitId,
                        destinationLocationXInWorldCoordinates: location.XInWorldCoordinates(),
                        destinationLocationYInWorldCoordinates: location.YInWorldCoordinates()
                ]

        command.jsonCommandData =  JsonOutput.toJson(commandParams)

        doPostSimulationCommand( command)
    }


}
