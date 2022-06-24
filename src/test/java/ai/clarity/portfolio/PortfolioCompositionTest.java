package ai.clarity.portfolio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PortfolioCompositionTest {

    @Test
    void test_create_regular_portfolio() {
        PortfolioComposition composition = PortfolioComposition.build().add("ISIN_1", 23.45)
                .add("ISIN_2", 11.55)
                .add("ISIN_3", 43)
                .add("ISIN_4", 22);

        assertEquals(100, composition.getTotalWeight());
    }

    @Test
    void test_create_portfolio_with_shorts() {
        PortfolioComposition composition = PortfolioComposition.build().add("ISIN_1", 23.45)
                .add("ISIN_2", 11.55)
                .add("ISIN_3", -21)
                .add("ISIN_4", 33)
                .add("ISIN_5", 53);

        assertEquals(100, composition.getTotalWeight());
    }

    @Test
    void test_create_portfolio_with_shorts_remove_shorts_and_normalize() {
        PortfolioComposition composition = PortfolioComposition.build().add("ISIN_1", 23.45)
                .add("ISIN_2", 11.55)
                .add("ISIN_3", -21)
                .add("ISIN_4", 33)
                .add("ISIN_5", 53);

        assertEquals(5, composition.getComponents().size());
        assertEquals(100, composition.getTotalWeight());

        composition.removeShortsAndNormalizeWeights();

        assertEquals(4, composition.getComponents().size());
        assertEquals(100, composition.getTotalWeight());
    }

    @Test
    void test_create_portfolio_without_shorts_and_normalize_has_no_effect() {
        PortfolioComposition composition = PortfolioComposition.build().add("ISIN_1", 23.45)
                .add("ISIN_2", 23.55)
                .add("ISIN_3", 33)
                .add("ISIN_4", 20);

        assertEquals(4, composition.getComponents().size());
        assertEquals(100, composition.getTotalWeight());

        composition.removeShortsAndNormalizeWeights();

        assertEquals(4, composition.getComponents().size());
        assertEquals(100, composition.getTotalWeight());
    }

}
