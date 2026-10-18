package main;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Stores discovered subjects and facts in first-discovery order. */
public final class Inventory {
    private static final double PLANT_AND_FERTILIZER_BONUS = 0.3;
    private static final double HUMIDITY_AND_RETENTION_BONUS = 0.2;
    private static final double ROUNDING_FACTOR = 100.0;
    private final Set<String> collectedItems = new LinkedHashSet<>();
    private final Map<String, Set<String>> knowledgeBase = new LinkedHashMap<>();

    /** Records a discovery once. */
    public void addItem(final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("An inventory item needs a name");
        }
        collectedItems.add(name);
    }

    /** Saves a new, nonblank fact only for an already discovered subject. */
    public boolean addFact(final String subject, final String fact) {
        if (!collectedItems.contains(subject) || fact == null || fact.isBlank()) {
            return false;
        }
        return knowledgeBase.computeIfAbsent(subject, key -> new LinkedHashSet<>()).add(fact);
    }

    /** Returns subjects in discovery order without exposing mutable storage. */
    public List<String> getCollectedItems() {
        return List.copyOf(collectedItems);
    }

    /** Returns a snapshot whose map and nested fact lists are immutable. */
    public Map<String, List<String>> getKnowledgeBase() {
        Map<String, List<String>> snapshot = new LinkedHashMap<>();
        knowledgeBase.forEach((subject, facts) -> snapshot.put(subject, List.copyOf(facts)));
        return java.util.Collections.unmodifiableMap(snapshot);
    }

    /** Tests the exact recipe, allowing an optional final period on the humidity fact. */
    public boolean checkRequirement(final String name, final String type) {
        Set<String> facts = knowledgeBase.getOrDefault(name, Set.of());
        String required = switch (type) {
            case "plantVegetation" -> "Method to plant " + name;
            case "fertilizeSoil" -> "Method to fertilize with " + name;
            case "increaseHumidity" -> "Method to increase humidity";
            case "waterRetention" -> "Method to improve water retention with " + name;
            case "increaseMoisture" -> "Method to increaseMoisture";
            default -> null;
        };
        return required != null && (facts.contains(required)
                || type.equals("increaseHumidity") && facts.contains(required + "."));
    }

    /** Applies one supported environmental recipe to a complete world cell. */
    public String applyImprovement(final Cell cell, final String type, final String name) {
        return switch (type) {
            case "plantVegetation" -> {
                cell.getAir().setOxygenLevel(add(cell.getAir().getOxygenLevel(),
                        PLANT_AND_FERTILIZER_BONUS));
                cell.getAir().setAirQual(cell.getAir().airQuality());
                yield "The " + name + " was planted successfully.";
            }
            case "fertilizeSoil" -> {
                cell.getSoil().setOrganicMatter(add(cell.getSoil().getOrganicMatter(),
                        PLANT_AND_FERTILIZER_BONUS));
                yield "The soil was successfully fertilized using " + name;
            }
            case "increaseHumidity" -> {
                cell.getAir().setHumidity(add(cell.getAir().getHumidity(),
                        HUMIDITY_AND_RETENTION_BONUS));
                cell.getAir().setAirQual(cell.getAir().airQuality());
                yield "The humidity was successfully increased using " + name;
            }
            case "waterRetention", "increaseMoisture" -> {
                cell.getSoil().setWaterRetention(add(cell.getSoil().getWaterRetention(),
                        HUMIDITY_AND_RETENTION_BONUS));
                yield type.equals("waterRetention")
                        ? "Water retention was improved successfully using " + name + "."
                        : "The moisture was successfully increased using " + name;
            }
            default -> throw new IllegalArgumentException("Unsupported improvement: " + type);
        };
    }

    private static double add(final double current, final double increment) {
        return Math.round((current + increment) * ROUNDING_FACTOR) / ROUNDING_FACTOR;
    }
}
