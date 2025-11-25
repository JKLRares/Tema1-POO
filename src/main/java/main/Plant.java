package main;
import lombok.Getter;

@Getter
public abstract class Plant extends Alive {
	private String level;
	private double growingLevel;

    private PlantMaturity maturity;

    public abstract double getOxygenOutput();
    public abstract int getStuck();

    public double getMaturityModifier() {
        return switch (this.maturity) {
            case YOUNG -> 0.2;
            case MATURE -> 0.7;
            case OLD -> 0.4;
            default -> 0.0;
        };
    }

	public Plant(int x, int y, String type, String name, double mass, double growingLevel, String level,
                 boolean scanned) {
		super(x, y, mass, scanned, name, type);
		this.level = "YOUNG";
		this.growingLevel = 0.0;
        this.maturity = PlantMaturity.YOUNG;
	}

//	public double calculateOxygenOutput(double oxygenLevel) {
//		double modifier = this.getMaturityModifier();
//		oxygenLevel = oxygenLevel + this.getType().getOxygenOutput() + modifier;
//		return oxygenLevel;
//	}

	public void grow() {
        double modifier = this.getMaturityModifier();
		this.growingLevel += modifier;
		if(this.growingLevel >= 1.0) {
			this.growingLevel = 0.0;
			if(this.level.equals("YOUNG")) {
				this.level = "MATURE";
			} else if(this.level.equals("MATURE")) {
				this.level = "OLD";
			} else if(this.level.equals("OLD")) {
				this.level = "DEAD";
				die();
			}
		}
	}

    @Override
    public void die() {

    }

    public EntityType getEntityType() {
        return EntityType.PLANT;
    }
}