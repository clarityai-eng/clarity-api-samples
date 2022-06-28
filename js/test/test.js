const ClarityAIUtils = require('../index');
const assert  = require('assert').strict;

const portfolio_with_shorts = {
  "securities": [
    {
      "isin": "ISIN_1",
      "percentage": 23.45
    },
    {
      "isin": "ISIN_2",
      "percentage": 11.55
    },
    {
      "isin": "ISIN_3",
      "percentage": -21
    },
    {
      "isin": "ISIN_4",
      "percentage": 33
    },
    {
      "isin": "ISIN_5",
      "percentage": 53
    }
  ]
}

const portfolio_without_shorts = {
  "securities": [
    {
      "isin": "ISIN_1",
      "percentage": 23.45
    },
    {
      "isin": "ISIN_2",
      "percentage": 23.55
    },
    {
      "isin": "ISIN_3",
      "percentage": 33
    },
    {
      "isin": "ISIN_4",
      "percentage": 20
    }
  ]
}


describe('ClarityAIUtils', function () {
  describe('#portfolioRemoveShortsAndNormalizeWeights()', function () {
    it('should remove short position from Portfolio composition and weights still sum up 100', function () {
      console.log('Before:');
      console.log(portfolio_with_shorts.securities)

      portfolio_with_shorts_removed = ClarityAIUtils.portfolioRemoveShortsAndNormalizeWeights(portfolio_with_shorts)

      console.log('After:')
      console.log(portfolio_with_shorts_removed.securities)

      assert.equal(portfolio_with_shorts_removed.securities.length, 4);

      portfolio_without_shorts_total_weight = portfolio_with_shorts_removed.securities.map(s => s.percentage).reduce((total, current) => total + current, 0)
      
      assert.ok(Math.abs(portfolio_without_shorts_total_weight - 100.00) <= 0.001)
    });

    it('Passing a Portfolio without shorts should return exactly the same portfolio', function () {
      non_modified_portfolio = ClarityAIUtils.portfolioRemoveShortsAndNormalizeWeights(portfolio_without_shorts)

      assert.equal(portfolio_without_shorts, non_modified_portfolio);
    });
  });
});

