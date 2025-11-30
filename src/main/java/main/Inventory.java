package main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public final class Inventory {
    private static final double PLANT_VEGETATION_BONUS = 0.3;
    private static final double FERTILIZE_SOIL_BONUS = 0.3;
    private static final double INCREASE_HUMIDITY_BONUS = 0.2;
    private static final double WATER_RETENTION_BONUS = 0.2;
    private static final double INCREASE_MOISTURE_BONUS = 0.2;

    private final List<String> collectedItems;
    private final Map<String, List<String>> knowledgeBase;

    public Inventory() {
        this.collectedItems = new ArrayList<>();
        this.knowledgeBase = new HashMap<>();
    }

    /**
     * Adds an item to the collected items list if it is not already present.
     *
     * @param name the name of the item to add
     */
    public void addItem(final String name) {
        if (!collectedItems.contains(name)) {
            collectedItems.add(name);
        }
    }

    /**
     * Adds a fact to the knowledge base if the subject is in the collected items.
     *
     * @param subject the subject of the fact
     * @param fact    the fact to be added
     * @return true if the fact was added, false otherwise
     */
    public boolean addFact(final String subject, final String fact) {
        if (collectedItems.contains(subject)) {
            knowledgeBase.putIfAbsent(subject, new ArrayList<>());
            List<String> facts = knowledgeBase.get(subject);
            if (!facts.contains(fact)) {
                facts.add(fact);
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * Checks if a specific requirement is met for a given improvement type.
     *
     * @param name            the name of the entity
     * @param improvementType the type of improvement to check
     * @return true if the requirement is met, false otherwise
     */
    public boolean checkRequirement(final String name, final String improvementType) {
        if (!knowledgeBase.containsKey(name)) {
            return false;
        }

        List<String> facts = knowledgeBase.get(name);
        String requiredFact = "";

        if (improvementType.equals("plantVegetation")) {
            requiredFact = "Method to plant " + name;
        } else if (improvementType.equals("fertilizeSoil")) {
            requiredFact = "Method to fertilize with " + name;
        } else if (improvementType.equals("increaseHumidity")) {
            requiredFact = "Method to increase humidity.";
        } else if (improvementType.equals("waterRetention")) {
            requiredFact = "Method to improve water retention with " + name;
        } else if (improvementType.equals("increaseMoisture")) {
            requiredFact = "Method to increaseMoisture";
        }

        return facts.contains(requiredFact);
    }

    /**
     * Applies an improvement to the environment of a specific cell.
     *
     * @param cell the cell to improve
     * @param type the type of improvement to apply
     * @param name the name of the item used for improvement
     * @return a string message describing the result of the improvement
     */
    public String applyImprovement(final Cell cell, final String type, final String name) {
        if (type.equals("plantVegetation")) {
            double a = PLANT_VEGETATION_BONUS;
            cell.getAir().setOxygenLevel(cell.getAir().getOxygenLevel() + a);
            cell.getAir().setAirQual(cell.getAir().airQuality());
            return "The " + name + " was planted successfully.";

        } else if (type.equals("fertilizeSoil")) {
            double a = FERTILIZE_SOIL_BONUS;
            cell.getSoil().setOrganicMatter(cell.getSoil().getOrganicMatter() + a);
            return "The soil was successfully fertilized using " + name;

        } else if (type.equals("increaseHumidity")) {
            double a = INCREASE_HUMIDITY_BONUS;
            cell.getAir().setHumidity(cell.getAir().getHumidity() + a);
            cell.getAir().setAirQual(cell.getAir().airQuality());
            return "The humidity was successfully increased using " + name;

        } else if (type.equals("waterRetention")) {
            double a = WATER_RETENTION_BONUS;
            cell.getSoil().setWaterRetention(cell.getSoil().getWaterRetention() + a);
            return "Water retention was improved successfully using " + name + ".";

        } else if (type.equals("increaseMoisture")) {
            double a = INCREASE_MOISTURE_BONUS;
            cell.getSoil().setWaterRetention(cell.getSoil().getWaterRetention() + a);
            return "The moisture was successfully increased using " + name;
        }
        return "This environment has been improved";
    }
}
