package client

import domain.*
import domain.event.SimulationStateUpdateEvent
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.params.CoreConnectionPNames

class MikeAndConquerSimulationClient {


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
        SetOptionsUserCommand command = new SetOptionsUserCommand()
        command.commandType = "SetOptions"

        def commandParams =
                [
                    gameSpeed: simulationOptions.gameSpeed
                ]

        command.commandData =  JsonOutput.toJson(commandParams)


        def resp = restClient.post(
                path: '/simulation/command',
                body: command,
                requestContentType: 'application/json')

        assert resp.status == 200
    }


    SimulationOptions getSimulationOptions() {
        def resp
        try {
            resp = restClient.get(
                    path: '/simulation/query/options',
                    requestContentType: 'application/json')
            assert resp.status == 200
        }
        catch(HttpResponseException e) {
            throw e
        }

        SimulationOptions simulationOptions = new SimulationOptions()

        simulationOptions.gameSpeed = resp.responseData.gameSpeed

        return simulationOptions
    }


    void removeUnit(int unitId) {

        // TODO:  Do we need a generic Command class instead of CreateMinigunnerCOmmand?
        CreateMinigunnerCommand createUnitCommand = new CreateMinigunnerCommand()
        createUnitCommand.commandType = "RemoveUnit"

        def commandParams =
                [
                        unitId: unitId
                ]

        createUnitCommand.commandData =  JsonOutput.toJson(commandParams)

        def resp = restClient.post(
                path: '/simulation/command',
                body: createUnitCommand,
                requestContentType: 'application/json')

        assert resp.status == 200


    }


    void addMinigunner( WorldCoordinatesLocation location) {

        CreateMinigunnerCommand createUnitCommand = new CreateMinigunnerCommand()
        createUnitCommand.commandType = "CreateMinigunner"

        def commandParams =
            [
                startLocationXInWorldCoordinates: location.XInWorldCoordinates(),
                startLocationYInWorldCoordinates: location.YInWorldCoordinates()
            ]

        createUnitCommand.commandData =  JsonOutput.toJson(commandParams)

        def resp = restClient.post(
                path: '/simulation/command',
                body: createUnitCommand,
                requestContentType: 'application/json')

        assert resp.status == 200

    }

    void addJeep(WorldCoordinatesLocation location) {

        CreateJeepCommand command = new CreateJeepCommand()
        command.commandType = "CreateJeep"

        def commandParams =
                [
                        startLocationXInWorldCoordinates: location.XInWorldCoordinates(),
                        startLocationYInWorldCoordinates: location.YInWorldCoordinates()
                ]

        command.commandData =  JsonOutput.toJson(commandParams)

        try {
            def resp = restClient.post(
                    path: '/simulation/command',
                    body: command,
                    requestContentType: 'application/json')

            assert resp.status == 200
        }
        catch(HttpResponseException e) {
            ByteArrayInputStream byteArrayInputStream = e.response.responseData
            int n = byteArrayInputStream.available()
            byte[] bytes = new byte[n]
            byteArrayInputStream.read(bytes, 0, n)
            String s = new String(bytes )
            println("exception details:" + s)
            Map json = new JsonSlurper().parseText(s)
        }


    }

    void addMCV( WorldCoordinatesLocation location) {

        CreateMCVCommand command = new CreateMCVCommand()
        command.commandType = "CreateMCV"

        def commandParams =
                [
                        startLocationXInWorldCoordinates: location.XInWorldCoordinates(),
                        startLocationYInWorldCoordinates: location.YInWorldCoordinates()
                ]

        command.commandData =  JsonOutput.toJson(commandParams)

        try {
            def resp = restClient.post(
                    path: '/simulation/command',
                    body: command,
                    requestContentType: 'application/json')

            assert resp.status == 200
        }
        catch(HttpResponseException e) {
//            int x = 3
            ByteArrayInputStream byteArrayInputStream = e.response.responseData
            int n = byteArrayInputStream.available()
            byte[] bytes = new byte[n]
            byteArrayInputStream.read(bytes, 0, n)
            String s = new String(bytes )
            println("exception details:" + s)
            Map json = new JsonSlurper().parseText(s)
        }


    }


    void startScenario() {

        StartScenarioCommand command = new StartScenarioCommand()
        command.commandType = "StartScenario"

        try {
            def resp = restClient.post(
                    path: '/simulation/command',
                    body: command,
                    requestContentType: 'application/json')


            assert resp.status == 200
        }
        catch(HttpResponseException e) {
            int x = 3
            throw e
        }

        int y = 4

    }


//    List<SimulationStateUpdateEvent> getSimulationStateUpdateEvents() {
//        getSimulationStateUpdateEvents(0)
//    }

    List<SimulationStateUpdateEvent> getSimulationStateUpdateEvents(int startIndex) {
        def resp = restClient.get(
                path: '/simulation/query/events',
                query: ['startIndex': startIndex],
                requestContentType: 'application/json' )

        assert resp.status == 200

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
        def resp = restClient.get(
                path: '/simulation/query/eventscount',
                requestContentType: 'application/json' )

        assert resp.status == 200

        int numItems = resp.responseData

        return numItems

    }



    void moveUnit(int unitId, WorldCoordinatesLocation location) {

        MoveUnitCommand command = new MoveUnitCommand()
        command.commandType = "OrderUnitMove"

        def commandParams =
                [
                        unitId: unitId,
                        destinationLocationXInWorldCoordinates: location.XInWorldCoordinates(),
                        destinationLocationYInWorldCoordinates: location.YInWorldCoordinates()
                ]

        command.commandData =  JsonOutput.toJson(commandParams)


        def resp = restClient.post(
                path: '/simulation/command',
                body: command,
                requestContentType: 'application/json')

        assert resp.status == 200

    }


}
