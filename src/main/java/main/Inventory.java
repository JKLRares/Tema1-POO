package main;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Getter @Setter
public class Inventory {
    private final List<String> collectedItems;
    private final Map<String, List<String>> knowledgeBase;

    public Inventory() {
        this.collectedItems = new ArrayList<>();
        this.knowledgeBase = new HashMap<>();
    }

    public void addItem(String name) {
        if (!collectedItems.contains(name)) {
            collectedItems.add(name);
        }
    }

    public boolean addFact(String subject, String fact) {
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

    public boolean checkRequirement(String name, String improvementType) {
        if (!knowledgeBase.containsKey(name)) return false;

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

    public String applyImprovement(Cell cell, String type, String name) {
        if (type.equals("plantVegetation")) {
            double a = 0.3;
            cell.getAir().setOxygenLevel(cell.getAir().getOxygenLevel() + a);
            cell.getAir().setAirQual(cell.getAir().airQuality());
            return "The " + name + " was planted successfully.";

        } else if (type.equals("fertilizeSoil")) {
            double a = 0.3;
            cell.getSoil().setOrganicMatter(cell.getSoil().getOrganicMatter() + a);
            return "The soil was successfully fertilized using " + name;

        } else if (type.equals("increaseHumidity")) {
            double a = 0.2;
            cell.getAir().setHumidity(cell.getAir().getHumidity() + a);
            cell.getAir().setAirQual(cell.getAir().airQuality());
            return "The humidity was successfully increased using " + name;

        } else if (type.equals("waterRetention")) {
            double a = 0.2;
            cell.getSoil().setWaterRetention(cell.getSoil().getWaterRetention() + a);
            return "Water retention was improved successfully using " + name + ".";

        } else if (type.equals("increaseMoisture")) {
            double a = 0.2;
            cell.getSoil().setWaterRetention(cell.getSoil().getWaterRetention() + a);
            return "The moisture was successfully increased using " + name;
        }
        return "This environment has been improved";
    }
}