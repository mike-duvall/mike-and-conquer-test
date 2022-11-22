package client

import domain.*
import domain.event.SimulationStateUpdateEvent
import groovy.json.JsonOutput
import groovyx.net.http.RESTClient
import org.apache.http.params.CoreConnectionPNames

class MikeAndConquerSimulationClient extends BaseClient {


    String hostUrl
    RESTClient  restClient
    int port = 5000

//    private static final String GDI_MINIGUNNERS_BASE_URL = '/mac/gdiMinigunners'
//    private static final String NOD_MINIGUNNERS_BASE_URL = '/mac/nodMinigunners'
//    private static final String MCV_BASE_URL = '/mac/MCV'
//    private static final String GDI_CONSTRUCTION_YARD = '/mac/GDIConstructionYard'
//    private static final String SIDEBAR_BASE_URL = '/mac/Sidebar'
//    private static final String NOD_TURRET_BASE_URL = '/mac/NodTurret'
//    private static final String GAME_OPTIONS_URL = '/mac/gameOptions'
//    private static final String GAME_HISTORY_EVENTS_URL = '/mac/gameHistoryEvents'


    MikeAndConquerSimulationClient(String host,  boolean useTimeouts = true) {
        hostUrl = "http://$host:$port"
        restClient = new RESTClient(hostUrl)

        if(useTimeouts) {
            restClient.client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, new Integer(5000))
            restClient.client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, new Integer(5000))
        }
    }

    void setSimulationOptions(SimulationOptions simulationOptions) {
        Command command = new Command()
        command.commandType = "SetOptions"

        def commandParams =
                [
                    gameSpeed: simulationOptions.gameSpeed
                ]

        command.commandData =  JsonOutput.toJson(commandParams)

        doPostRestCall('/simulation/command', command)
    }


    SimulationOptions getSimulationOptions() {

        def resp = doGetRestCall('/simulation/query/options')

        SimulationOptions simulationOptions = new SimulationOptions()

        simulationOptions.gameSpeed = resp.responseData.gameSpeed

        return simulationOptions
    }


    void removeUnit(int unitId) {

        // TODO:  Do we need a generic Command class instead of CreateMinigunnerCOmmand?
        Command createUnitCommand = new Command()
        createUnitCommand.commandType = "RemoveUnit"

        def commandParams =
                [
                        unitId: unitId
                ]

        createUnitCommand.commandData =  JsonOutput.toJson(commandParams)
        doPostRestCall('/simulation/command', createUnitCommand)
    }


    void addMinigunner( WorldCoordinatesLocation location) {

        Command createUnitCommand = new Command()
        createUnitCommand.commandType = "CreateMinigunner"

        def commandParams =
            [
                startLocationXInWorldCoordinates: location.XInWorldCoordinates(),
                startLocationYInWorldCoordinates: location.YInWorldCoordinates()
            ]

        createUnitCommand.commandData =  JsonOutput.toJson(commandParams)

        doPostRestCall('/simulation/command', createUnitCommand)
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

        doPostRestCall('/simulation/command', command)
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

        doPostRestCall('/simulation/command', command)
    }


    void startScenario() {

        Command command = new Command()
        command.commandType = "StartScenario"

        doPostRestCall('/simulation/command', command)
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

        //int x = 3

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

        doPostRestCall('/simulation/command', command)
    }


}
