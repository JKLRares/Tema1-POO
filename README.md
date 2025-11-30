# CONSTANTINESCU RARES 323CA

# Ecosystem Simulation & TerraBot Explorer

This project implements a complex ecosystem simulation on a grid-based map, where a robot
named TerraBot moves around, scans the environment, and interacts with various entities.

## Core Simulation Engine

The simulation is driven by the `CommandExecutor` class, which is the heart of the project.
Its key method, `executeCommands`, iterates through the input and delegates tasks. It relies
on `processRuntimeCommand` to handle robot actions like moving or scanning, and the crucial
`updateEnvironment` method. This method is called periodically to advance the world state—
triggering plant growth via `grow` and updating entity stats. The entry point is the `Main`
class, which sets up the `InputLoader` and triggers the `action` method to start the run

## Map & Robot Mechanics

The world is defined by the `Matrix` class, which holds a grid of `Cell` objects.
 **Matrix**: Uses `placeEntity` to populate the grid
 **Cell**: Acts as a container. It uses `accept` methods to safely set its contents
    (`setPlant`, `setAnimal`, etc.) without type casting
 **Robot**: The `Robot` class explores this map. Its most important method, `moveRobot`,
    calls `bestNeigh` to scan surroundings. It calculates a safety score using `calculateCost`
    to avoid hazards like toxic air or enemies
 **Inventory**: As the robot scans, `Inventory` stores items via `addItem`. It also manages
    learned facts (when the learnFact command is used) with `addFact` and checks if the robot
    can perform actions using `checkRequirement` and `applyImprovement` (e.g., `fertilizeSoil`)
 **ScannedAnimal**: Keeps track of animals the robot has scanned, using `updateAll` to track
    their movements even when the robot isn't looking

## Entity Hierarchy & Key Behaviors

The `Entity` abstract class is the parent of all map objects. Here is how the specific
hierarchies break down:

### 1. Animals
The `Animal` abstract class drives behavior with `tryToAct`, which decides if an animal's
    interactions
 **Concrete Classes**: `Carnivore`, `Herbivore`, `Omnivore`, `Parasites`, and `Detritivores`
 **Key Methods**: They all implement `getAttackPossibility`. The `Carnivore` and `Parasites`
    also override `isPredator` to define their hunting logic

### 2. Plants
The `Plant` class manages lifecycle stages (Young, Mature, Old) through the `grow` method.
 **Concrete Classes**: `FloweringPlants`, `Ferns`, `Mosses`, `Algae`, and `Gymnosperms`
 **Key Methods**: Each subclass defines `getOxygenOutput` (affecting air quality) and
    `getStuckPossibility` (trapping the robot). They also use `getMaturityModifier` to change
    their stats over time. `PlantMaturity` is a helper enum here

### 3. Environment (Soil, Air, Water)
These entities make the planet dynamic
 **Soil**: The base `Soil` class defines `soilQuality`. Subclasses `ForestSoil`, `SwampSoil`,
    `TundraSoil`, `DesertSoil`, and `GrasslandSoil` implement `initialSoilQuality` and
    `probFormula` to calculate distinct bonuses based on parameters like nutrient density or
    leaf litte
 **Air**: The `Air` class calculates `airQuality` and `airToxicity`. Subclasses `Montan`,
    `Temperat`, `Tropical`, `Desert`, and `Polar` implement the `event` method to react to
    weather changes (e.g., storms or pollen) and `maxScore` to cap their quality
 **Water**: The `Water` class is unique; it doesn't have subclasses but uses `normalizeIsFrozen`
    and internal logic to calculate purity and salinity effects on the ecosystem

## Interaction Design

The system relies on the interaction between these methods. For instance, in `CommandExecutor`,
the `updateEnvironment` call might trigger a `Polar` air `event`. This lowers air quality.
In the next turn, an `Animal` calling `tryToAct` might check that air quality, become "Sick"
(changing its internal state), and fail to `eatPlant` successfully. This chain of events
showcases the interactions between the elements of a planet perfectly
