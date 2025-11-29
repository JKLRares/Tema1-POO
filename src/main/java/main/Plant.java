package main;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class Plant extends Alive {
	private String level;
	private double growingLevel, stuckProb;

    private PlantMaturity maturity;

    public Plant(int x, int y, String type, String name, double mass, double growingLevel, String level,
                 boolean scanned) {
        super(x, y, mass, scanned, name, type);
        this.level = "YOUNG";
        this.growingLevel = 0.0;
        this.maturity = PlantMaturity.YOUNG;
        this.stuckProb = getStuckPossibility() / 100.0;
    }

    public abstract double getOxygenOutput();
    public abstract int getStuckPossibility();

    public double getMaturityModifier() {
        return switch (this.maturity) {
            case YOUNG -> 0.2;
            case MATURE -> 0.7;
            case OLD -> 0.4;
            default -> 0.0;
        };
    }

	public void grow() {
		if(this.growingLevel >= 1.0) {
			this.growingLevel = 0.0;
			if(this.level.equals("YOUNG")) {
				this.level = "MATURE";
                this.maturity = PlantMaturity.MATURE;
			} else if(this.level.equals("MATURE")) {
				this.level = "OLD";
                this.maturity = PlantMaturity.OLD;
			} else if(this.level.equals("OLD")) {
				this.level = "DEAD";
//				die();
			}
		}
	}

    @Override
    public void die(Entity entity) {

    }

    public EntityType getEntityType() {
        return EntityType.PLANT;
    }
}