package ai.clarity.portfolio;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PortfolioComposition {

    private final List<PortfolioComponent> components;

    private class PortfolioComponent{
        final String securityId;
        final double weight;

        private PortfolioComponent(String securityId, double weight){
            this.securityId = securityId;
            this.weight = weight;
        }
    }

    private PortfolioComposition(List<PortfolioComponent> components) {
        this.components = components;
    }

    public static PortfolioComposition create() {
        return new PortfolioComposition(new ArrayList<>());
    }

    public PortfolioComposition add(String securityId, double weight) {
        components.add(new PortfolioComponent(securityId, weight));
        return this;
    }

    public double getTotalWeight() {
        return getTotalWeightFor(components);
    }

    public List<PortfolioComponent> getComponents() { return components; }

    /**
     * Check if there are negative weights in this Portfolio Composition and in that case remove them and
     * normalize remaining weights to 100, returning a new PortfolioComposition object
     *
     * @return   The new normalized PortfolioComposition
     */
    public PortfolioComposition removeShortsAndNormalizeWeights() {
        List<PortfolioComponent> portfolioComponentsWithoutShorts = components.stream()
                .filter(c -> c.weight > 0)
                .collect(Collectors.toList());

        if(portfolioComponentsWithoutShorts.size() < components.size()) {
            PortfolioComposition newPortfolioComposition = PortfolioComposition.create();
            double totalWeightWithoutShorts = getTotalWeightFor(portfolioComponentsWithoutShorts);

            for(PortfolioComponent component : portfolioComponentsWithoutShorts) {
                double newWeight = component.weight * 100 / totalWeightWithoutShorts;
                newPortfolioComposition.add(component.securityId, newWeight);
            }
            return newPortfolioComposition;
        }

        return this;
    }

    private double getTotalWeightFor(List<PortfolioComponent> componentList) {
        return componentList.stream().mapToDouble(c -> c.weight).sum();
    }

}
