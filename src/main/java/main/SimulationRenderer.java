package main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import main.air.Desert;
import main.air.Montan;
import main.air.Polar;
import main.air.Temperat;
import main.air.Tropical;
import main.soils.DesertSoil;
import main.soils.ForestSoil;
import main.soils.GrasslandSoil;
import main.soils.SwampSoil;
import main.soils.TundraSoil;

/** Defines the public JSON schema explicitly so internal getters cannot leak into output. */
public final class SimulationRenderer {
    private final ObjectMapper mapper;

    public SimulationRenderer(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /** Serializes the entities at one Cartesian coordinate. */
    public JsonNode conditions(final Matrix matrix, final int x, final int y) {
        Cell cell = matrix.getCell(x, y);
        ObjectNode output = mapper.createObjectNode();
        if (cell.getSoil() != null) {
            output.set("soil", soil(cell.getSoil()));
        }
        if (cell.getPlant() != null) {
            output.set("plants", entity(cell.getPlant()));
        }
        if (cell.getAnimal() != null) {
            output.set("animals", entity(cell.getAnimal()));
        }
        if (cell.getWater() != null) {
            output.set("water", entity(cell.getWater()));
        }
        if (cell.getAir() != null) {
            output.set("air", air(cell.getAir()));
        }
        return output;
    }

    private ObjectNode entity(final Entity entity) {
        return mapper.createObjectNode().put("type", entity.getType())
                .put("name", entity.getName()).put("mass", roundedMass(entity.getMass()));
    }

    private double roundedMass(final double mass) {
        return BigDecimal.valueOf(mass).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private ObjectNode soil(final Soil soil) {
        ObjectNode output = entity(soil).put("nitrogen", soil.getNitrogen())
                .put("waterRetention", soil.getWaterRetention()).put("soilpH", soil.getSoilph())
                .put("organicMatter", soil.getOrganicMatter())
                .put("soilQuality", soil.soilQuality());
        switch (soil) {
            case ForestSoil forest -> output.put("leafLitter", forest.getLeafLitter());
            case SwampSoil swamp -> output.put("waterLogging", swamp.getWaterLogging());
            case TundraSoil tundra -> output.put("permafrostDepth", tundra.getPermafrostDepth());
            case DesertSoil desert -> output.put("salinity", desert.getSalinity());
            case GrasslandSoil grassland -> output.put("rootDensity", grassland.getRootDensity());
            default -> throw new IllegalArgumentException("Unsupported soil: " + soil.getType());
        }
        return output;
    }

    private ObjectNode air(final Air air) {
        ObjectNode output = entity(air).put("humidity", air.getHumidity())
                .put("temperature", air.getTemperature()).put("oxygenLevel", air.getOxygenLevel())
                .put("airQuality", air.getAirQual());
        switch (air) {
            case Montan mountain -> output.put("altitude", mountain.getAltitude());
            case Temperat temperate -> output.put("pollenLevel", temperate.getPollenLevel());
            case Tropical tropical -> output.put("co2Level", tropical.getCo2Level());
            case Desert desert -> output.put("desertStorm", desert.isDesertStorm());
            case Polar polar -> output
                    .put("iceCrystalConcentration", polar.getIceCrystalConcentration())
                    .put("windSpeed", polar.getWindSpeed());
            default -> throw new IllegalArgumentException("Unsupported air: " + air.getType());
        }
        return output;
    }

    /** Serializes cells in ascending y, then ascending x order. */
    public ArrayNode map(final Matrix matrix) {
        ArrayNode output = mapper.createArrayNode();
        for (int y = 0; y < matrix.getHeight(); y++) {
            for (int x = 0; x < matrix.getWidth(); x++) {
                Cell cell = matrix.getCell(x, y);
                ObjectNode section = output.addObject();
                section.putArray("section").add(x).add(y);
                int objects = (cell.getPlant() == null ? 0 : 1)
                        + (cell.getAnimal() == null ? 0 : 1) + (cell.getWater() == null ? 0 : 1);
                section.put("totalNrOfObjects", objects);
                section.put("airQuality", cell.getAir().resultAirQaulity());
                section.put("soilQuality", cell.getSoil().resultSoilQuality());
            }
        }
        return output;
    }
}
