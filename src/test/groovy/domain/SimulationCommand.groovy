package domain

class SimulationCommand {


    String commandType
    String jsonCommandData

    static final String CREATE_GDI_MINIGUNNER_AT_RANDOM_LOCATION = "CreateGDIMinigunnerAtRandomLocation"
    static final String CREATE_NOD_MINIGUNNER_AT_RANDOM_LOCATION = "CreateNodMinigunnerAtRandomLocation"
    static final String CREATE_GDI_MINIGUNNER = "CreateGDIMinigunner"
    static final String CREATE_NOD_MINIGUNNER = "CreateNodMinigunner"
    static final String CREATE_MVC = "CreateMCV"
    static final String CREATE_JEEP = "CreateJeep"
    static final String SET_OPTIONS = "SetOptions"
    static final String REMOVE_UNIT = "RemoveUnit"
    static final String START_SCENARIO = "StartScenario"
    static final String ORDER_UNIT_TO_MOVE = "OrderUnitMove"
    static final String APPLY_DAMAGE_TO_UNIT = "ApplyDamageToUnit"


    SimulationCommand() {

    }

    SimulationCommand(String commandType, String jsonCommandData) {
        this.commandType = commandType
        this.jsonCommandData = jsonCommandData
    }



}
