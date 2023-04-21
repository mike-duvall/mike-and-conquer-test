package domain

class UICommand {

    String commandType
    String commandData

    static final String SET_UI_OPTIONS = 'SetUIOptions'
    static final String START_SCENARIO = 'StartScenario'
    static final String SELECT_UNIT = 'SelectUnit'
    static final String LEFT_CLICK = 'LeftClick'
    static final String LEFT_CLICK_SIDEBAR = 'LeftClickSidebar'
    static final String LEFT_CLICK_AND_HOLD = 'LeftClickAndHold'
    static final String RIGHT_CLICK = 'RightClick'
    static final String MOVE_MOUSE = 'MoveMouse'
    static final String RELEASE_LEFT_MOUSE_BUTTON = 'ReleaseLeftMouseButton'


    UICommand() {

    }

    UICommand(String commandType, String commandData) {
        this.commandType = commandType
        this.commandData = commandData
    }




}
