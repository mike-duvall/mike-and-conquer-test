package main

import domain.MovementDirection


import domain.Point
import domain.UIOptions
import domain.WorldCoordinatesLocation
import domain.WorldCoordinatesLocationBuilder
import domain.event.EventType
import domain.event.SimulationStateUpdateEvent
import groovy.json.JsonSlurper
import spock.lang.Ignore
import util.ImageUtil

import javax.imageio.ImageIO
import java.awt.image.BufferedImage


class ShroudTests extends MikeAndConquerTestBase {




    def setup() {

        UIOptions uiOptions = new UIOptions(drawShroud: true, mapZoomLevel: 1.0)
        setAndAssertUIOptions(uiOptions)
        uiClient.startScenario()
        sleep(1000)


//        // Add bogus minigunner to not delete so game state stays in "Playing"
//        WorldCoordinatesLocation unitStartLocation = new WorldCoordinatesLocationBuilder()
//                .worldMapTileCoordinatesX(4)
//                .worldMapTileCoordinatesY(5)
//                .build()

    }

    def "Shroud screenshot scenario 1"() {
        given:
        WorldCoordinatesLocation mcvLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(21)
                .worldMapTileCoordinatesY(12)
                .build()
        simulationClient.addMCV(mcvLocation)


        and:
        sequentialEventReader.waitForEventOfType(EventType.MCV_CREATED)

        when: "Test scenario 1"
        int testScenarioNumber = 1
        int startX = 408
        int startY = 129
        int screenshotCompareWidth = 232
        int screenshotCompareHeight = 159

        then:
        assertScreenshotMatches(testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)
    }

    def "Shroud screenshot scenario 2"() {
        given:
        WorldCoordinatesLocation mcvLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(21)
                .worldMapTileCoordinatesY(12)
                .build()

        simulationClient.addMCV(mcvLocation)


        when: "Test scenario 2"
        int testScenarioNumber = 2
        int startX = 408
        int startY = 129
        int screenshotCompareWidth = 232
        int screenshotCompareHeight = 159

        def movements = [MovementDirection.NORTH, MovementDirection.NORTH]

        doMinigunnerPlacements(mcvLocation, movements)

        then:
        assertScreenshotMatches(testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)
    }

    @Ignore
    def "Shroud screenshot scenario 3"() {
        given:
        WorldCoordinatesLocation mcvLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(21)
                .worldMapTileCoordinatesY(12)
                .build()

        simulationClient.addMCV(mcvLocation)

        when:  "Test scenario 3"
        int testScenarioNumber = 3
        int startX = 408
        int startY = 129
        int screenshotCompareWidth = 232
        int screenshotCompareHeight = 159

        def movements = [MovementDirection.NORTH, MovementDirection.NORTH, MovementDirection.NORTH, MovementDirection.EAST]
        doMinigunnerPlacements(mcvLocation, movements)

        then:
        assertScreenshotMatches(testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)
    }

    @Ignore
    def "Shroud screenshot scenario 4"() {
        given:
        WorldCoordinatesLocation mcvLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(21)
                .worldMapTileCoordinatesY(12)
                .build()

        simulationClient.addMCV(mcvLocation)

        when:  "Test scenario 4"
        int testScenarioNumber = 4
        int startX = 408
        int startY = 129
        int screenshotCompareWidth = 232
        int screenshotCompareHeight = 159

        def movements = [MovementDirection.NORTH, MovementDirection.NORTH, MovementDirection.NORTH, MovementDirection.EAST, MovementDirection.WEST, MovementDirection.WEST]
        doMinigunnerPlacements(mcvLocation, movements)

        then:
        assertScreenshotMatches(testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)
    }

    @Ignore
    def "Shroud screenshot scenario 5"() {
        given:
        WorldCoordinatesLocation mcvLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(21)
                .worldMapTileCoordinatesY(12)
                .build()

        simulationClient.addMCV(mcvLocation)

        when:  "Test scenario 5"
        int testScenarioNumber = 5
        int startX = 503
        int startY = 158
        int screenshotCompareWidth = 145
        int screenshotCompareHeight = 130

        def movements = [MovementDirection.NORTH, MovementDirection.NORTH, MovementDirection.NORTH, MovementDirection.EAST, MovementDirection.WEST, MovementDirection.WEST,
        MovementDirection.EAST, MovementDirection.EAST, MovementDirection.EAST]
        doMinigunnerPlacements(mcvLocation, movements)

        then:
        assertScreenshotMatches(testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)
    }


    def "Shroud screenshot scenario 6"() {
        given:
        WorldCoordinatesLocation mcvLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(21)
                .worldMapTileCoordinatesY(12)
                .build()

        simulationClient.addMCV(mcvLocation)

        when:  "Test scenario 6"
        int testScenarioNumber = 6
        int startX = 527
        int startY = 167
        int screenshotCompareWidth = 121
        int screenshotCompareHeight = 121

        def movements = [MovementDirection.NORTH, MovementDirection.NORTH, MovementDirection.NORTH, MovementDirection.EAST, MovementDirection.WEST, MovementDirection.WEST,
                         MovementDirection.EAST, MovementDirection.EAST, MovementDirection.EAST, MovementDirection.EAST]
        doMinigunnerPlacements(mcvLocation, movements)

        then:
        assertScreenshotMatches(testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)
    }


    def "Shroud screenshot scenario 7"() {
        given:
        WorldCoordinatesLocation mcvLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(21)
                .worldMapTileCoordinatesY(12)
                .build()

        simulationClient.addMCV(mcvLocation)

        when:  "Test scenario 7"
        int testScenarioNumber = 7
        int startX = 507
        int startY = 165
        int screenshotCompareWidth = 141
        int screenshotCompareHeight = 123

        def movements = [MovementDirection.NORTH, MovementDirection.NORTH, MovementDirection.NORTH,  MovementDirection.EAST,
                         MovementDirection.EAST, MovementDirection.EAST,MovementDirection.EAST]

        doMinigunnerPlacements(mcvLocation, movements)

        then:
        assertScreenshotMatches(testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)

    }

    def "Shroud screenshot scenario 8"() {
        given:
        WorldCoordinatesLocation mcvLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(21)
                .worldMapTileCoordinatesY(12)
                .build()

        simulationClient.addMCV(mcvLocation)

        when:  "Test scenario 8"
        int testScenarioNumber = 8
        int startX = 532
        int startY = 170
        int screenshotCompareWidth = 116
        int screenshotCompareHeight = 149

        def movements = [MovementDirection.NORTH, MovementDirection.NORTH, MovementDirection.NORTH,  MovementDirection.EAST,
                         MovementDirection.EAST, MovementDirection.EAST,MovementDirection.EAST, MovementDirection.EAST, MovementDirection.SOUTH]

        doMinigunnerPlacements(mcvLocation, movements)

        then:
        assertScreenshotMatches(testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)

    }

    def "Shroud screenshot scenario 9"() {
        given:
        WorldCoordinatesLocation mcvLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(21)
                .worldMapTileCoordinatesY(12)
                .build()

        simulationClient.addMCV(mcvLocation)

        when:
        int testScenarioNumber = 9
        int startX = 519
        int startY = 72
        int screenshotCompareWidth = 129
        int screenshotCompareHeight = 121

        def movements = [MovementDirection.NORTH, MovementDirection.NORTH, MovementDirection.NORTH,  MovementDirection.EAST,
                         MovementDirection.EAST, MovementDirection.EAST,MovementDirection.EAST, MovementDirection.EAST, MovementDirection.SOUTH,
        MovementDirection.NORTH, MovementDirection.NORTH,MovementDirection.NORTH,MovementDirection.NORTH,MovementDirection.NORTH,MovementDirection.WEST ]

        doMinigunnerPlacements(mcvLocation, movements)

        then:
        assertScreenshotMatches(testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)

    }


    def "Shroud screenshot scenario 10"() {
        given:
        WorldCoordinatesLocation mcvLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(21)
                .worldMapTileCoordinatesY(12)
                .build()

        simulationClient.addMCV(mcvLocation)

        when:
        int testScenarioNumber = 10
        int startX = 525
        int startY = 67
        int screenshotCompareWidth = 123
        int screenshotCompareHeight = 102

        def movements = [MovementDirection.NORTH, MovementDirection.NORTH, MovementDirection.NORTH,  MovementDirection.EAST,
                         MovementDirection.EAST, MovementDirection.EAST,MovementDirection.EAST, MovementDirection.EAST,
                         MovementDirection.NORTH,
                         MovementDirection.NORTH, MovementDirection.NORTH,MovementDirection.WEST, MovementDirection.WEST, MovementDirection.NORTH ]

        doMinigunnerPlacements(mcvLocation, movements)

        then:
        assertScreenshotMatches(testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)

    }

    @Ignore
    def "Shroud screenshot scenario 11"() {
        given:
        WorldCoordinatesLocation mcvLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(21)
                .worldMapTileCoordinatesY(12)
                .build()

        simulationClient.addMCV(mcvLocation)

        when:
        int testScenarioNumber = 11
        int startX = 310
        int startY = 92
        int screenshotCompareWidth = 157
        int screenshotCompareHeight = 183

        def movements = [MovementDirection.NORTH, MovementDirection.NORTH, MovementDirection.NORTH,  MovementDirection.NORTH,
                         MovementDirection.WEST, MovementDirection.NORTH,MovementDirection.WEST, MovementDirection.NORTH,
                         MovementDirection.WEST, MovementDirection.WEST, MovementDirection.WEST,MovementDirection.WEST,
                         MovementDirection.SOUTH, MovementDirection.SOUTH ]

        doMinigunnerPlacements(mcvLocation, movements)

        then:
        assertScreenshotMatches(testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)
    }

    @Ignore
    def "Shroud screenshot scenario 12"() {
        given:
        WorldCoordinatesLocation mcvLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(21)
                .worldMapTileCoordinatesY(12)
                .build()

        simulationClient.addMCV(mcvLocation)

        when:
        int testScenarioNumber = 12
        int startX = 314
        int startY = 191
        int screenshotCompareWidth = 144
        int screenshotCompareHeight = 98

        def movements = [MovementDirection.NORTH, MovementDirection.NORTH, MovementDirection.NORTH,  MovementDirection.NORTH,
                         MovementDirection.WEST, MovementDirection.NORTH,MovementDirection.WEST, MovementDirection.NORTH,
                         MovementDirection.WEST, MovementDirection.WEST, MovementDirection.WEST,MovementDirection.WEST,
                         MovementDirection.SOUTH, MovementDirection.SOUTH, MovementDirection.SOUTH ]

        doMinigunnerPlacements(mcvLocation, movements)

        then:
        assertScreenshotMatches(testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)
    }

    def "Shroud screenshot scenario 13"() {
        given:
        WorldCoordinatesLocation mcvLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(21)
                .worldMapTileCoordinatesY(12)
                .build()

        simulationClient.addMCV(mcvLocation)

        when:
        int testScenarioNumber = 13
        int startX = 303
        int startY = 87
        int screenshotCompareWidth = 302
        int screenshotCompareHeight = 189

        def movements = [MovementDirection.NORTH, MovementDirection.NORTH, MovementDirection.NORTH,  MovementDirection.NORTH,
                         MovementDirection.NORTHWEST, MovementDirection.NORTHWEST,
                         MovementDirection.WEST, MovementDirection.WEST, MovementDirection.WEST, MovementDirection.WEST,
                         MovementDirection.SOUTH ]

        doMinigunnerPlacements(mcvLocation, movements)

        then:
        assertScreenshotMatches(testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)
    }



    def "Shroud screenshot scenario 14"() {
        given:
        WorldCoordinatesLocation mcvLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(21)
                .worldMapTileCoordinatesY(12)
                .build()

        simulationClient.addMCV(mcvLocation)

        when:
        int testScenarioNumber = 14
        int startX = 307
        int startY = 174
        int screenshotCompareWidth = 171
        int screenshotCompareHeight = 107

        def movements = [MovementDirection.NORTH, MovementDirection.NORTH, MovementDirection.NORTH,  MovementDirection.NORTH,
                         MovementDirection.NORTHWEST, MovementDirection.NORTHWEST,
                         MovementDirection.WEST, MovementDirection.WEST, MovementDirection.WEST, MovementDirection.WEST,
                         MovementDirection.SOUTH, MovementDirection.SOUTH]

        doMinigunnerPlacements(mcvLocation, movements)

        then:
        assertScreenshotMatches(testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)
    }


    def "Shroud screenshot scenario 15"() {
        given:
        WorldCoordinatesLocation mcvLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(21)
                .worldMapTileCoordinatesY(12)
                .build()

        simulationClient.addMCV(mcvLocation)

        when:
        int testScenarioNumber = 15
        int startX = 311
        int startY = 93
        int screenshotCompareWidth = 190
        int screenshotCompareHeight = 242

        def movements = [MovementDirection.NORTH, MovementDirection.NORTH, MovementDirection.NORTH,  MovementDirection.NORTH,
                         MovementDirection.NORTHWEST, MovementDirection.NORTHWEST,
                         MovementDirection.WEST, MovementDirection.WEST, MovementDirection.WEST, MovementDirection.WEST,
                         MovementDirection.SOUTH, MovementDirection.SOUTH, MovementDirection.SOUTH]

        doMinigunnerPlacements(mcvLocation, movements)

        then:
        assertScreenshotMatches(testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)
    }

    def "Shroud screenshot scenario 16"() {
        given:
        WorldCoordinatesLocation mcvLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(21)
                .worldMapTileCoordinatesY(12)
                .build()

        simulationClient.addMCV(mcvLocation)

        when:
        int testScenarioNumber = 16
        int startX = 297
        int startY = 92
        int screenshotCompareWidth = 185
        int screenshotCompareHeight = 229

        def movements = [MovementDirection.NORTH, MovementDirection.NORTH, MovementDirection.NORTH,  MovementDirection.NORTH,
                         MovementDirection.NORTHWEST, MovementDirection.NORTHWEST,
                         MovementDirection.WEST, MovementDirection.WEST, MovementDirection.WEST, MovementDirection.WEST,
                         MovementDirection.SOUTH, MovementDirection.SOUTH, MovementDirection.SOUTH, MovementDirection.SOUTH]

        doMinigunnerPlacements(mcvLocation, movements)

        then:
        assertScreenshotMatches(testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)
    }

    def "Shroud screenshot scenario 17"() {
        given:
        WorldCoordinatesLocation mcvLocation = new WorldCoordinatesLocationBuilder()
                .worldMapTileCoordinatesX(21)
                .worldMapTileCoordinatesY(12)
                .build()

        simulationClient.addMCV(mcvLocation)

        when:
        int testScenarioNumber = 17
        int startX = 232
        int startY = 184
        int screenshotCompareWidth = 132
        int screenshotCompareHeight = 158

        def movements = [MovementDirection.NORTH, MovementDirection.NORTH, MovementDirection.NORTH,  MovementDirection.NORTH,
                         MovementDirection.NORTHWEST, MovementDirection.NORTHWEST,
                         MovementDirection.WEST, MovementDirection.WEST, MovementDirection.WEST, MovementDirection.WEST,
                         MovementDirection.SOUTH, MovementDirection.SOUTH, MovementDirection.SOUTH, MovementDirection.SOUTH, MovementDirection.SOUTH,
                         MovementDirection.WEST, MovementDirection.WEST
        ]

        doMinigunnerPlacements(mcvLocation, movements)

        then:
        assertScreenshotMatches(testScenarioNumber, startX , startY, screenshotCompareWidth, screenshotCompareHeight)
    }



    void assertScreenshotMatches(int testScenarioNumber, int startX, int startY, int screenshotCompareWidth, int screenshotCompareHeight) {
        assertScreenshotMatches('shroud', testScenarioNumber,startX,startY,screenshotCompareWidth, screenshotCompareHeight)
    }

    void assertScreenshotMatches(String scenarioPrefix, int testScenarioNumber, int startX, int startY, int screenshotCompareWidth, int screenshotCompareHeight) {

        // Move cursor so it's not in the screenshot
//        gameClient.moveMouseToWorldCoordinates(new Point(startX + screenshotCompareWidth + 50,startY + screenshotCompareHeight + 50))
        WorldCoordinatesLocation minigunnerStartLocation = new WorldCoordinatesLocationBuilder()
                .worldCoordinatesX(startX + screenshotCompareWidth + 50)
                .worldCoordinatesY(startY + screenshotCompareHeight + 50)
                .build()

//        uiClient.leftClick(minigunnerStartLocation)


        assertScreenshotMatchesWithoutMovingCursor(scenarioPrefix, testScenarioNumber, startX, startY, screenshotCompareWidth, screenshotCompareHeight)
    }

    void assertScreenshotMatchesWithoutMovingCursor(String scenarioPrefix, int testScenarioNumber, int startX, int startY, int screenshotCompareWidth, int screenshotCompareHeight) {

        String realGameFilename = "real-game-" + scenarioPrefix + "-" + testScenarioNumber + "-start-x" + startX + "-y" + startY + "-" + screenshotCompareWidth + "x" + screenshotCompareHeight + ".png"

        File imageFile

        try {
            imageFile = new File(
                    getClass().getClassLoader().getResource(realGameFilename).getFile()
            )
        }
        catch(Exception e) {
            throw new RuntimeException("Unable to read file ${realGameFilename}")
        }

        BufferedImage realGameScreenshot = ImageIO.read(imageFile)
        BufferedImage fullScreenShot = uiClient.getScreenshot()
        BufferedImage screenshotSubImage = fullScreenShot.getSubimage(startX,startY,screenshotCompareWidth,screenshotCompareHeight)

        String realGameCopiedFilename = realGameFilename.replaceAll("real-game", "copied-real-game")
        String mikeAndConquerCopiedFilename = realGameFilename.replaceAll("real-game", "actual-mike-and-conquer")

        writeImageToFileInBuildDirectory(realGameScreenshot, realGameCopiedFilename )
        writeImageToFileInBuildDirectory(screenshotSubImage, mikeAndConquerCopiedFilename )

        assert ImageUtil.imagesAreEqual(screenshotSubImage, realGameScreenshot)
    }

    void writeImageToFileInBuildDirectory(BufferedImage bufferedImage, String fileName) {
        String relPath = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        File targetDir = new File(relPath+"../../../../build/screenshot")
        // TODO:  come up with more reliable way to find this path
        // Seems to work differently between IntelliJ versions
//        File targetDir = new File(relPath+"../../../build/screenshot")
        if(!targetDir.exists()) {
            targetDir.mkdir();
        }
        String absPath = targetDir.getAbsolutePath()
        File outputfile = new File(absPath + "\\" + fileName);
        ImageIO.write(bufferedImage, "png", outputfile);
    }




    private void doMinigunnerPlacements(WorldCoordinatesLocation mcvLocation, List<MovementDirection> movements) {

        int screenshotIndex = 0;
        Point currentLocation = new Point(mcvLocation.XInWorldMapTileCoordinates(), mcvLocation.YInWorldMapTileCoordinates())
        movements.each { movementDirection ->
            if (movementDirection == MovementDirection.NORTH) {
                currentLocation.y--
            }
            else if (movementDirection == MovementDirection.NORTHEAST) {
                currentLocation.x++
                currentLocation.y--
            }
            else if (movementDirection == MovementDirection.EAST) {
                currentLocation.x++
            }
            else if (movementDirection == MovementDirection.SOUTHEAST) {
                currentLocation.x++
                currentLocation.y++
            }
            else if (movementDirection == MovementDirection.SOUTH) {
                currentLocation.y++
            }
            else if (movementDirection == MovementDirection.SOUTHWEST) {
                currentLocation.x--
                currentLocation.y++
            }
            else if (movementDirection == MovementDirection.WEST) {
                currentLocation.x--
            }
            else if (movementDirection == MovementDirection.NORTHWEST) {
                currentLocation.x--
                currentLocation.y--
            }


            WorldCoordinatesLocation minigunnerLocation = new WorldCoordinatesLocationBuilder()
                    .worldMapTileCoordinatesX(currentLocation.x)
                    .worldMapTileCoordinatesY(currentLocation.y)
                    .build()


//            int currentEventIndex = simulationClient.getSimulationStateUpdateEventsCurrentIndex()
            simulationClient.addMinigunner(minigunnerLocation)

            SimulationStateUpdateEvent simulationStateUpdateEvent = sequentialEventReader.waitForEventOfType(EventType.MINIGUNNER_CREATED)

            JsonSlurper jsonSlurper = new JsonSlurper()
            def minigunnerCreatedEventData = jsonSlurper.parseText(simulationStateUpdateEvent.eventData)

            int minigunnerId = minigunnerCreatedEventData.UnitId



            boolean debugScreenshot = false

            if(debugScreenshot) {
                BufferedImage fullScreenShot = uiClient.getScreenshot()

                String baseFileName = "test-full-screenshot-";
                String fileName = baseFileName + screenshotIndex + ".png"
                writeImageToFileInBuildDirectory(fullScreenShot, fileName)
                screenshotIndex++

            }

            simulationClient.removeUnit(minigunnerId)

            SimulationStateUpdateEvent minigunnerDeletedEvent = sequentialEventReader.waitForEventOfType(EventType.UNIT_DELETED)
            def unitDeletedEventData = jsonSlurper.parseText(minigunnerDeletedEvent.eventData)
            assert unitDeletedEventData.UnitId == minigunnerId
        }
    }





}