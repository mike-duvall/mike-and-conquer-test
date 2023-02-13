package domain.event

class EventType {

    static String MINIGUNNER_CREATED = "MinigunnerCreated"
    static String MCV_CREATED = "MCVCreated"
    static String UNIT_DELETED = "UnitDeleted"
    static String UNIT_ORDERED_TO_MOVE = "UnitOrderedToMove"
    static String UNIT_MOVEMENT_PLAN_CREATED = "UnitMovementPlanCreated"
    static String UNIT_ARRIVED_AT_PATH_STEP = "UnitArrivedAtPathStep"
    static String UNIT_ARRIVED_AT_DESTINATION = "UnitArrivedAtDestination"
    static String SCENARIO_INITIALIZED =  "ScenarioInitialized"
    static String GDI_CONSTRUCTION_YARD_CREATED = "GDIConstructionYardCreated"
    static String STARTED_BUILDING_BARRACKS = "StartedBuildingBarracks"
    static String STARTED_BUILDING_MINIGUNNER = "StartedBuildingMinigunner"

    static String BUILDING_BARRACKS_PERCENT_COMPLETED =  "BuildingBarracksPercentCompleted"
    static String BUILDING_MINIGUNNER_PERCENT_COMPLETED = "BuildingMinigunnerPercentCompleted"
    static String COMPLETED_BUILDING_BARRACKS = "CompletedBuildingBarracks"
    static String COMPLETED_BUILDING_MINIGUNNER = "CompletedBuildingMinigunner"
    static String GDI_BARRACKS_PLACED = "GDIBarracksPlaced"
    static final String BEGAN_MISSION_ATTACK = "BeganMissionAttack"

    static final String UNIT_BEGAN_MOVING = "UnitBeganMoving"

    static final String UNIT_BEGAN_FIRING = "UnitBeganFiring"


    static final String UNIT_BEGAN_IDLE = "UnitBeganIdle"

    static final String NONE_COMMAND_BEGIN = "NoneCommandBegan"
    static final String FIRED_ON_UNIT = "FiredOnUnit"
    static final String BULLET_HIT_TARGET = "BulletHitTarget"
    static final String UNIT_TOOK_DAMAGE = "UnitTookDamage"
    static final String UNIT_RELOADED_WEAPON = "UnitReloadedWeapon"
    static final String UNIT_DESTROYED = "UnitDestroyed"
}
