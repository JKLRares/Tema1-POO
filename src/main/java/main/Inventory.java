package main;
import lombok.Getter
import lombok.Setter
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class Inventory {
    private final List<String> collectedItems;

    public Inventory() {
        this.collectedItems = new ArrayList<>();
    }

    public void addItem(String name) {
        if (!collectedItems.contains(name)) {
            collectedItems.add(name);
        }
    }
}