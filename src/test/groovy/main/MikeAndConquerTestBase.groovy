package main

import client.MikeAndConquerSimulationClient
import client.MikeAndConquerUIClient
import client.SequentialEventReader
import domain.SimulationOptions
import domain.UIOptions
import domain.Unit
import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class MikeAndConquerTestBase extends Specification {


    MikeAndConquerSimulationClient simulationClient
    MikeAndConquerUIClient uiClient
    SequentialEventReader sequentialEventReader
    JsonSlurper jsonSlurper


    def setup() {
        String localhost = "localhost"
        String remoteHost = "192.168.0.110"

//        String host = localhost
        String host = remoteHost

        boolean useTimeouts = true
//        boolean useTimeouts = false
        simulationClient = new MikeAndConquerSimulationClient(host,  useTimeouts )
        sequentialEventReader = new SequentialEventReader(simulationClient)
        jsonSlurper = new JsonSlurper()

        uiClient = new MikeAndConquerUIClient(host, useTimeouts )

//        UIOptions uiOptions = new UIOptions(drawShroud: false, mapZoomLevel: 2.0)
//        setAndAssertUIOptions(uiOptions)
//
//        simulationClient.startScenario()
//        sleep(1000)

    }


    void setAndAssertUIOptions(UIOptions uiOptions) {
        uiClient.setUIOptions(uiOptions)
        assertUIOptionsAreSetTo(uiOptions)
    }


    def assertUIOptionsAreSetTo(UIOptions desiredUIOptions) {
        def conditions = new PollingConditions(timeout: 10, initialDelay: 0.5, factor: 1.25)
        conditions.eventually {
            UIOptions uiOptions = uiClient.getUIOptions()
            assert uiOptions.mapZoomLevel == desiredUIOptions.mapZoomLevel
            assert uiOptions.drawShroud == desiredUIOptions.drawShroud
        }
        return true
    }

    void setAndAssertSimulationOptions(SimulationOptions simulationOptions) {
        simulationClient.setSimulationOptions(simulationOptions)
        assertSimulationOptionsAreSetTo(simulationOptions)
    }

    def assertSimulationOptionsAreSetTo(SimulationOptions desiredOptions) {
        def conditions = new PollingConditions(timeout: 10, initialDelay: 0.5, factor: 1.25)
        conditions.eventually {
            SimulationOptions retrievedOptions = simulationClient.getSimulationOptions()
            assert retrievedOptions.gameSpeed == desiredOptions.gameSpeed
        }
        return true
    }

    Unit parseUnitFromEventData(String unitCreatedEventData) {
        def unitDataObject = jsonSlurper.parseText(unitCreatedEventData)
        Unit createdUnit = new Unit()
        createdUnit.unitId = unitDataObject.UnitId
        createdUnit.x = unitDataObject.X
        createdUnit.y = unitDataObject.Y

        return createdUnit

    }


}
