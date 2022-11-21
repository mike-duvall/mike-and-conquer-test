package client

import domain.*
import groovy.json.JsonOutput
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.params.CoreConnectionPNames


import javax.imageio.ImageIO
import java.awt.image.BufferedImage


class MikeAndConquerUIClient {


    String hostUrl
    RESTClient  restClient
    int port = 5010

//    private static final String GDI_MINIGUNNERS_BASE_URL = '/mac/gdiMinigunners'
//    private static final String NOD_MINIGUNNERS_BASE_URL = '/mac/nodMinigunners'
//    private static final String MCV_BASE_URL = '/mac/MCV'
//    private static final String GDI_CONSTRUCTION_YARD = '/mac/GDIConstructionYard'
//    private static final String SIDEBAR_BASE_URL = '/mac/Sidebar'
//    private static final String NOD_TURRET_BASE_URL = '/mac/NodTurret'
//    private static final String GAME_OPTIONS_URL = '/mac/gameOptions'
//    private static final String GAME_HISTORY_EVENTS_URL = '/mac/gameHistoryEvents'


    MikeAndConquerUIClient(String host,  boolean useTimeouts = true) {
        hostUrl = "http://$host:$port"
        restClient = new RESTClient(hostUrl)

        if(useTimeouts) {
            restClient.client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, new Integer(5000))
            restClient.client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, new Integer(5000))
        }
    }

    def doGetRestCall(String path) {

        def resp
        try {
            resp = restClient.get(
                    path: path,
                    requestContentType: 'application/json')
            assert resp.status == 200
        }
        catch(HttpResponseException e) {
            throw e
        }

        return resp
    }

    def doGetRestCall(String path, Object query) {

        def resp
        try {
            resp = restClient.get(
                    path: path,
                    query: query,
                    requestContentType: 'application/json')
            assert resp.status == 200
        }
        catch(HttpResponseException e) {
            throw e
        }

        return resp
    }



    def doPostRestCall(String path, Object body) {
        def resp
        try {
            resp = restClient.post(
                    path: path,
                    body: body,
                    requestContentType: 'application/json')


            assert resp.status == 200
        }
        catch(HttpResponseException e) {
            throw e
        }

        return resp

    }

    void setUIOptions(UIOptions uiOptions) {
        Command command = new Command()
        command.commandType = Command.SET_UI_OPTIONS

        def commandParams =
                [
                        DrawShroud: uiOptions.drawShroud,
                        MapZoomLevel: uiOptions.mapZoomLevel
                ]

        command.commandData =  JsonOutput.toJson(commandParams)
        doPostRestCall('/ui/command', command)
    }

    UIOptions getUIOptions() {
        def resp = doGetRestCall('/ui/query/uioptions')
        UIOptions uiOptions = new UIOptions()

        uiOptions.mapZoomLevel = resp.responseData.mapZoomLevel
        uiOptions.drawShroud = resp.responseData.drawShroud

        return uiOptions
    }

    void startScenario() {

        Command command = new Command()
        command.commandType = Command.START_SCENARIO
        doPostRestCall('/ui/command', command)
    }


    void selectUnit(int unitId) {
        Command command = new Command()
        command.commandType = Command.SELECT_UNIT

        def commandParams =
                [
                        UnitId: unitId
                ]

        command.commandData =  JsonOutput.toJson(commandParams)

        doPostRestCall('/ui/command', command)
    }


    void leftClick(WorldCoordinatesLocation location) {

        Command command = new Command()
        command.commandType = Command.LEFT_CLICK

        def commandParams =
                [
                        XInWorldCoordinates: location.XInWorldCoordinates(),
                        YInWorldCoordinates: location.YInWorldCoordinates()
                ]

        command.commandData =  JsonOutput.toJson(commandParams)

        doPostRestCall('/ui/command', command)
    }

    void rightClick(WorldCoordinatesLocation location) {

        Command command = new Command()
        command.commandType = Command.RIGHT_CLICK

        def commandParams =
                [
                        XInWorldCoordinates: location.XInWorldCoordinates(),
                        YInWorldCoordinates: location.YInWorldCoordinates()
                ]

        command.commandData =  JsonOutput.toJson(commandParams)

        doPostRestCall('/ui/command', command)
    }



    Unit getUnit(int unitId) {

        def resp = doGetRestCall('/ui/query/unit',['unitId': unitId] )

        Unit unit = new Unit()
        unit.unitId = resp.responseData.unitId
        unit.selected = resp.responseData.selected

        return unit
    }

    String getMouseCursorState() {
        def resp = doGetRestCall('/ui/query/mouseCursor')
        String cursorState =  resp.responseData.str
        return cursorState
    }

    void dragSelect(int x1, int y1, int x2, int y2) {

        Point point1 = new Point(x1, y1)
        Point point2 = new Point(x2,y2)


        DoLeftClickAndHold(point1)

        WorldCoordinatesLocation worldCoordinatesLocation = new WorldCoordinatesLocationBuilder()
                .worldCoordinatesX(x2)
                .worldCoordinatesY(y2)
                .build()

        moveMouseToLocation(worldCoordinatesLocation)

        DoReleaseLeftMouseButton(point2)
    }

    private void DoLeftClickAndHold(Point point1) {
        Command command = new Command()
        command.commandType = Command.LEFT_CLICK_AND_HOLD

        def commandParams =
                [
                        XInWorldCoordinates: point1.x,
                        YInWorldCoordinates: point1.y
                ]

        command.commandData = JsonOutput.toJson(commandParams)

        doPostRestCall('/ui/command', command)
    }

    void moveMouseToLocation(WorldCoordinatesLocation location) {
        Command command = new Command()
        command.commandType = Command.MOVE_MOUSE

        def commandParams =
                [
                        XInWorldCoordinates: location.x,
                        YInWorldCoordinates: location.y
                ]

        command.commandData = JsonOutput.toJson(commandParams)
        doPostRestCall('/ui/command', command)
    }

    private void DoReleaseLeftMouseButton(Point point1) {
        Command command = new Command()
        command.commandType = Command.RELEASE_LEFT_MOUSE_BUTTON

        def commandParams =
                [
                        XInWorldCoordinates: point1.x,
                        YInWorldCoordinates: point1.y

                ]

        command.commandData = JsonOutput.toJson(commandParams)

        doPostRestCall('/ui/command', command)
    }

    BufferedImage  getScreenshot() {
        def resp = restClient.get( path : '/ui/screenshot' )
        ByteArrayInputStream byteArrayInputStream = resp.responseData
        BufferedImage screenShotImage = ImageIO.read(byteArrayInputStream)
        return screenShotImage
    }


}
