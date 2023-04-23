package client

import domain.Building
import domain.UICommand
import domain.Point
import domain.Sidebar
import domain.UIOptions
import domain.Unit
import domain.WorldCoordinatesLocation
import domain.WorldCoordinatesLocationBuilder
import groovy.json.JsonOutput
import groovyx.net.http.RESTClient
import org.apache.http.params.CoreConnectionPNames


import javax.imageio.ImageIO
import java.awt.image.BufferedImage


class MikeAndConquerUIClient extends BaseClient {


    String hostUrl
    int port = 5010

//    private static final String SIDEBAR_BASE_URL = '/mac/Sidebar'
//    private static final String NOD_TURRET_BASE_URL = '/mac/NodTurret'


    MikeAndConquerUIClient(String host,  boolean useTimeouts = true) {
        hostUrl = "http://$host:$port"
        restClient = new RESTClient(hostUrl)

        if(useTimeouts) {
            restClient.client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, new Integer(5000))
            restClient.client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, new Integer(5000))
        }
    }


    void doPostUICommand(Object command) {
        doPostRestCall('/ui/command', command)
    }


    void setUIOptions(UIOptions uiOptions) {
        UICommand command = new UICommand()
        command.commandType = UICommand.SET_UI_OPTIONS

        def commandParams =
                [
                        DrawShroud: uiOptions.drawShroud,
                        MapZoomLevel: uiOptions.mapZoomLevel
                ]

        command.jsonCommandData =  JsonOutput.toJson(commandParams)
        doPostUICommand( command)
    }

    UIOptions getUIOptions() {
        def resp = doGetRestCall('/ui/query/uioptions')
        UIOptions uiOptions = new UIOptions()

        uiOptions.mapZoomLevel = resp.responseData.mapZoomLevel
        uiOptions.drawShroud = resp.responseData.drawShroud

        return uiOptions
    }

    void startScenario() {
        UICommand command = new UICommand()
        command.commandType = UICommand.START_SCENARIO
        doPostUICommand( command)
    }


    void selectUnit(int unitId) {
        UICommand command = new UICommand()
        command.commandType = UICommand.SELECT_UNIT

        def commandParams =
                [
                        UnitId: unitId
                ]

        command.jsonCommandData =  JsonOutput.toJson(commandParams)

        doPostUICommand( command)
    }


    void leftClick(WorldCoordinatesLocation location) {

        UICommand command = new UICommand()
        command.commandType = UICommand.LEFT_CLICK

        def commandParams =
                [
                        XInWorldCoordinates: location.XInWorldCoordinates(),
                        YInWorldCoordinates: location.YInWorldCoordinates()
                ]

        command.jsonCommandData =  JsonOutput.toJson(commandParams)

        doPostUICommand( command)
    }

    void leftClickSidebar(String sidebarIconName) {

        def commandParams =
                [
                        SidebarIconName: sidebarIconName
                ]

        UICommand command = new UICommand(
            UICommand.LEFT_CLICK_SIDEBAR,
            JsonOutput.toJson(commandParams)
        )

        doPostUICommand(command)


    }


    void rightClick(WorldCoordinatesLocation location) {

        UICommand command = new UICommand()
        command.commandType = UICommand.RIGHT_CLICK

        def commandParams =
                [
                        XInWorldCoordinates: location.XInWorldCoordinates(),
                        YInWorldCoordinates: location.YInWorldCoordinates()
                ]

        command.jsonCommandData =  JsonOutput.toJson(commandParams)

        doPostUICommand( command)
    }


    Unit getUnit(int unitId) {

        def resp = doGetRestCall('/ui/query/unit',['unitId': unitId] )

        Unit unit = new Unit()
        unit.unitId = resp.responseData.unitId
        unit.selected = resp.responseData.selected

        return unit
    }

    Building getGDIConstructionYard() {

        def resp = doGetRestCall('/ui/query/gdiConstructionYard' )

        Building building = new Building()
        building.x = resp.responseData.x
        building.y = resp.responseData.y

        return building

    }

    Sidebar getSidebar() {
        def resp = doGetRestCall('/ui/query/sidebar' )

        Sidebar sidebar = new Sidebar()
        sidebar.buildBarracksEnabled = resp.responseData.buildBarracksEnabled
        sidebar.barracksIsBuilding = resp.responseData.barracksIsBuilding
        sidebar.barracksReadyToPlace = resp.responseData.barracksReadyToPlace
        sidebar.buildMinigunnerEnabled = resp.responseData.buildMinigunnerEnabled
        sidebar.minigunnerIsBuilding = resp.responseData.minigunnerIsBuilding
//        sidebar.y = resp.responseData.y

        return sidebar


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
        UICommand command = new UICommand()
        command.commandType = UICommand.LEFT_CLICK_AND_HOLD

        def commandParams =
                [
                        XInWorldCoordinates: point1.x,
                        YInWorldCoordinates: point1.y
                ]

        command.jsonCommandData = JsonOutput.toJson(commandParams)

        doPostUICommand( command)
    }

    void moveMouseToLocation(WorldCoordinatesLocation location) {
        UICommand command = new UICommand()
        command.commandType = UICommand.MOVE_MOUSE

        def commandParams =
                [
                        XInWorldCoordinates: location.x,
                        YInWorldCoordinates: location.y
                ]

        command.jsonCommandData = JsonOutput.toJson(commandParams)
        doPostUICommand( command)
    }

    private void DoReleaseLeftMouseButton(Point point1) {
        UICommand command = new UICommand()
        command.commandType = UICommand.RELEASE_LEFT_MOUSE_BUTTON

        def commandParams =
                [
                        XInWorldCoordinates: point1.x,
                        YInWorldCoordinates: point1.y

                ]

        command.jsonCommandData = JsonOutput.toJson(commandParams)

        doPostUICommand( command)
    }

    BufferedImage  getScreenshot() {
        def resp = restClient.get( path : '/ui/screenshot' )
        ByteArrayInputStream byteArrayInputStream = resp.responseData
        BufferedImage screenShotImage = ImageIO.read(byteArrayInputStream)
        return screenShotImage
    }


}
