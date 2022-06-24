package ai.clarity.portfolio;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PortfolioComposition {

    private List<PortfolioComponent> components;

    private class PortfolioComponent{
        String securityId;
        double weight;

        private PortfolioComponent(String securityId, double weight){
            this.securityId = securityId;
            this.weight = weight;
        }
    }

    private PortfolioComposition(List<PortfolioComponent> components) {
        this.components = components;
    }

    public static PortfolioComposition build() {
        return new PortfolioComposition(new ArrayList<>());
    }

    public PortfolioComposition add(String securityId, double weight) {
        components.add(new PortfolioComponent(securityId, weight));
        return this;
    }

    public double getTotalWeight() {
        return getTotalWeightFor(components);
    }

    private double getTotalWeightFor(List<PortfolioComponent> componentList) {
        return componentList.stream().mapToDouble(c -> c.weight).sum();
    }

    public List<PortfolioComponent> getComponents() { return components; }

    public PortfolioComposition removeShortsAndNormalizeWeights() {
        List<PortfolioComponent> newPortfolioComponents = components.stream()
                .filter(c -> c.weight > 0)
                .collect(Collectors.toList());

        if(newPortfolioComponents.size() < components.size()) {
            double totalWeightWithoutShorts = getTotalWeightFor(newPortfolioComponents);
            for(PortfolioComponent newComponent : newPortfolioComponents) {
                newComponent.weight = newComponent.weight * 100 / totalWeightWithoutShorts;
            }
            this.components = newPortfolioComponents;
        }

        return this;
    }

}
