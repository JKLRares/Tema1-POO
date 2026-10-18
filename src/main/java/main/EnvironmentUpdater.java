package main;

/** Advances deterministic ecological rules without performing input or output. */
public final class EnvironmentUpdater {
    private static final double PLANT_GROWTH_INCREMENT = 0.2;
    private static final double ENV_VAL_INCREMENT = 0.1;
    private static final double ROUNDING_FACTOR = 10.0;
    private static final double SOIL_ROUNDING_FACTOR = 100.0;

    /** Advances ecological interactions by one tick, then lets tracked animals act. */
    public void update(final Matrix matrix, final ScannedAnimal scannedAnimals,
                       final int timestamp) {
        for (int x = 0; x < matrix.getWidth(); x++) {
            for (int y = 0; y < matrix.getHeight(); y++) {
                Cell cell = matrix.getCell(x, y);
                cell.getAir().expireWeather(timestamp);

                if (cell.getSoil() != null && cell.getPlant() != null
                        && cell.getPlant().isScanned()) {
                    cell.getPlant().setGrowingLevel(cell.getPlant().getGrowingLevel()
                            + PLANT_GROWTH_INCREMENT);
                }

                if (cell.getWater() != null && cell.getWater().isScanned()) {
                    if (timestamp > cell.getWater().getScanTimestamp()
                            && (timestamp - cell.getWater().getScanTimestamp()) % 2 == 0) {
                        if (cell.getAir() != null) {
                            cell.getAir().updateEnvironmentParams(
                                    ENV_VAL_INCREMENT, ROUNDING_FACTOR);
                        }
                        if (cell.getSoil() != null) {
                            double newRet = cell.getSoil().getWaterRetention() + ENV_VAL_INCREMENT;
                            double rounded = Math.round(newRet * SOIL_ROUNDING_FACTOR)
                                    / SOIL_ROUNDING_FACTOR;
                            cell.getSoil().setWaterRetention(rounded);
                        }
                    }
                    if (cell.getPlant() != null && cell.getPlant().isScanned()) {
                        cell.getPlant().setGrowingLevel(cell.getPlant().getGrowingLevel()
                                + PLANT_GROWTH_INCREMENT);
                    }
                }
                if (cell.getPlant() != null && cell.getPlant().isScanned()) {
                    cell.getPlant().grow();
                    if (cell.getPlant().getLevel().equals("DEAD")) {
                        cell.setPlant(null);
                    }
                }
                if (cell.getPlant() != null && cell.getAir() != null
                        && cell.getPlant().isScanned()) {
                    double oxygenAdd = cell.getPlant().getOxygenOutput()
                            + cell.getPlant().getMaturityModifier();
                    double newOx = cell.getAir().getOxygenLevel() + oxygenAdd;
                    cell.getAir().setOxygenLevel(Math.round(newOx * ROUNDING_FACTOR)
                            / ROUNDING_FACTOR);
                    cell.getAir().setAirQual(cell.getAir().airQuality());
                }
            }
        }
        scannedAnimals.updateAll(timestamp, matrix);
    }

}
