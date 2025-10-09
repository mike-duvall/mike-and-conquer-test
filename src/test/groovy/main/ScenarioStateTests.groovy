package main


import domain.UIOptions
import domain.SimulationOptions
import domain.SimulationState
import domain.event.EventType

class ScenarioStateTests extends MikeAndConquerTestBase {


    def setup() {
        UIOptions uiOptions = new UIOptions(drawShroud: false, mapZoomLevel: 1.8)
        setAndAssertUIOptions(uiOptions)
        sleep(1000)
    }


    def "Verify initial simulation state is InitializingScenario"() {

        when:
        SimulationState simulationState = simulationClient.getCurrentSimulationState()

        then:
        assert simulationState.simulationState == SimulationState.InitializingScenario

        when:
        simulationClient.startScenario()

        // Update here to wait for event instead of sleep

        and:
        sequentialEventReader.waitForEventOfType(EventType.SCENARIO_INITIALIZED)

        simulationState = simulationClient.getCurrentSimulationState()

        then:
        assert simulationState.simulationState == SimulationState.RunningScenario




    }

}