package client

import domain.Command
import domain.SimulationOptions
import domain.WorldCoordinatesLocation
import domain.event.SimulationStateUpdateEvent
import groovy.json.JsonOutput
import groovyx.net.http.RESTClient
import org.apache.http.params.CoreConnectionPNames

class MikeAndConquerSimulationClient extends BaseClient {


    String hostUrl

    int port = 5000

//    private static final String SIDEBAR_BASE_URL = '/mac/Sidebar'
//    private static final String NOD_TURRET_BASE_URL = '/mac/NodTurret'


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
        Command command = new Command()
        command.commandType = "SetOptions"

        def commandParams =
                [
                    gameSpeed: simulationOptions.gameSpeed
                ]

        command.commandData =  JsonOutput.toJson(commandParams)

        doPostSimulationCommand(command)
    }


    SimulationOptions getSimulationOptions() {

        def resp = doGetRestCall('/simulation/query/options')

        SimulationOptions simulationOptions = new SimulationOptions()

        simulationOptions.gameSpeed = resp.responseData.gameSpeed

        return simulationOptions
    }


    void removeUnit(int unitId) {

        Command createUnitCommand = new Command()
        createUnitCommand.commandType = "RemoveUnit"

        def commandParams =
                [
                        unitId: unitId
                ]

        createUnitCommand.commandData =  JsonOutput.toJson(commandParams)
        doPostSimulationCommand( createUnitCommand)
    }


    void addMinigunner(WorldCoordinatesLocation location) {

        Command createUnitCommand = new Command()
        createUnitCommand.commandType = "CreateMinigunner"

        def commandParams =
            [
                startLocationXInWorldCoordinates: location.XInWorldCoordinates(),
                startLocationYInWorldCoordinates: location.YInWorldCoordinates()
            ]

        createUnitCommand.commandData =  JsonOutput.toJson(commandParams)

        doPostSimulationCommand( createUnitCommand)
    }

    void addMinigunnerAtRandomLocation() {
        Command createUnitCommand = new Command()
        createUnitCommand.commandType = Command.CREATE_GDI_MINIGUNNER_AT_RANDOM_LOCATION
        doPostSimulationCommand( createUnitCommand)
    }

    void createDeactivatedNodMinigunnerAtRandomLocation() {

        Command createUnitCommand = new Command()
        createUnitCommand.commandType = Command.CREATE_NOD_MINIGUNNER_AT_RANDOM_LOCATION

        def commandParams =
                [
                        deactivated: true
                ]

        createUnitCommand.commandData =  JsonOutput.toJson(commandParams)

        doPostSimulationCommand( createUnitCommand)
    }



    void addJeep(WorldCoordinatesLocation location) {

        Command command = new Command()
        command.commandType = "CreateJeep"

        def commandParams =
                [
                        startLocationXInWorldCoordinates: location.XInWorldCoordinates(),
                        startLocationYInWorldCoordinates: location.YInWorldCoordinates()
                ]

        command.commandData =  JsonOutput.toJson(commandParams)

        doPostSimulationCommand( command)
    }

    void addMCV( WorldCoordinatesLocation location) {

        Command command = new Command()
        command.commandType = "CreateMCV"

        def commandParams =
                [
                        startLocationXInWorldCoordinates: location.XInWorldCoordinates(),
                        startLocationYInWorldCoordinates: location.YInWorldCoordinates()
                ]

        command.commandData =  JsonOutput.toJson(commandParams)

        doPostSimulationCommand( command)
    }


    void startScenario() {

        Command command = new Command()
        command.commandType = "StartScenario"

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

        Command command = new Command()
        command.commandType = "OrderUnitMove"

        def commandParams =
                [
                        unitId: unitId,
                        destinationLocationXInWorldCoordinates: location.XInWorldCoordinates(),
                        destinationLocationYInWorldCoordinates: location.YInWorldCoordinates()
                ]

        command.commandData =  JsonOutput.toJson(commandParams)

        doPostSimulationCommand( command)
    }


}
