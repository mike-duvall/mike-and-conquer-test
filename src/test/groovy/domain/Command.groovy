package domain

class Command {

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
    static final String CREATE_GDI_MINIGUNNER_AT_RANDOM_LOCATION = "CreateGDIMinigunnerAtRandomLocation"
    static final String CREATE_NOD_MINIGUNNER_AT_RANDOM_LOCATION = "CreateNodMinigunnerAtRandomLocation"

    Command() {

    }

    Command(String commandType, String commandData) {
        this.commandType = commandType
        this.commandData = commandData
    }




}
