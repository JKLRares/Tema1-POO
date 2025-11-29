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
            }
            return true;
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
            requiredFact = "Method to fertilize soil with " + name;
        } else if (improvementType.equals("increaseHumidity")) {
            requiredFact = "Method to increase humidity with " + name;
        } else if (improvementType.equals("waterRetention")) {
            requiredFact = "Method to improve water retention with " + name;
        }

        return facts.contains(requiredFact);
    }

    public String applyImprovement(Cell cell, String type, String name) {
        if (type.equals("plantVegetation")) {
            double a = 0.3;
            if (cell.getAir() != null) {
                cell.getAir().setOxygenLevel(cell.getAir().getOxygenLevel() + a);
                cell.getAir().setAirQual(cell.getAir().airQuality());
            }
            return "The " + name + " was planted successfully.";

        } else if (type.equals("fertilizeSoil")) {
            double a = 0.3;
            if (cell.getSoil() != null) {
                cell.getSoil().setOrganicMatter(cell.getSoil().getOrganicMatter() + a);
            }
            return "The " + name + " was used to fertilize the soil successfully.";

        } else if (type.equals("increaseHumidity")) {
            double a = 0.2;
            if (cell.getAir() != null) {
                cell.getAir().setHumidity(cell.getAir().getHumidity() + a);
                cell.getAir().setAirQual(cell.getAir().airQuality());
            }
            return "The humidity was increased successfully using " + name + ".";

        } else if (type.equals("waterRetention")) {
            double a = 0.2;
            if (cell.getSoil() != null) {
                cell.getSoil().setWaterRetention(cell.getSoil().getWaterRetention() + a);
            }
            return "Water retention was improved successfully using " + name + ".";
        }

        return "The environment has been improved.";
    }
}