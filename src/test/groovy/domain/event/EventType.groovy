package domain.event

class EventType {

    static final String SCENARIO_INITIALIZED =  "ScenarioInitialized"
    
    static final String MINIGUNNER_CREATED = "MinigunnerCreated"
    static final String MCV_CREATED = "MCVCreated"
    static final String UNIT_DELETED = "UnitDeleted"

    static final String BEGAN_MISSION_ATTACK = "BeganMissionAttack"
    static final String BEGAN_MISSION_IDLE = "BeganMissionIdle"
//    static final String UNIT_ORDERED_TO_MOVE = "UnitOrderedToMove"
    static final String BEGAN_MISSION_MOVE_TO_DESTINATION = "BeganMissionMoveToDestination"

    static final String UNIT_BEGAN_MOVING = "UnitBeganMoving"
    static final String UNIT_BEGAN_FIRING = "UnitBeganFiring"
    static final String BULLET_HIT_TARGET = "BulletHitTarget"
    static final String UNIT_TOOK_DAMAGE = "UnitTookDamage"
    static final String UNIT_RELOADED_WEAPON = "UnitReloadedWeapon"
    static final String UNIT_DESTROYED = "UnitDestroyed"



    static final String UNIT_MOVEMENT_PLAN_CREATED = "UnitMovementPlanCreated"
    static final String UNIT_ARRIVED_AT_PATH_STEP = "UnitArrivedAtPathStep"
    static final String UNIT_ARRIVED_AT_DESTINATION = "UnitArrivedAtDestination"

    static final String GDI_CONSTRUCTION_YARD_CREATED = "GDIConstructionYardCreated"
    static final String STARTED_BUILDING_BARRACKS = "StartedBuildingBarracks"
    static final String STARTED_BUILDING_MINIGUNNER = "StartedBuildingMinigunner"
    static final String BUILDING_BARRACKS_PERCENT_COMPLETED =  "BuildingBarracksPercentCompleted"
    static final String BUILDING_MINIGUNNER_PERCENT_COMPLETED = "BuildingMinigunnerPercentCompleted"
    static final String COMPLETED_BUILDING_BARRACKS = "CompletedBuildingBarracks"
    static final String COMPLETED_BUILDING_MINIGUNNER = "CompletedBuildingMinigunner"
    static final String GDI_BARRACKS_PLACED = "GDIBarracksPlaced"

}
