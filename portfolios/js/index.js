
exports.portfolioRemoveShortsAndNormalizeWeights = function portfolio_remove_shorts_and_normalize(portfolio) {
  const new_portfolio = JSON.parse(JSON.stringify(portfolio));
  const securities = new_portfolio.securities
  const securities_without_shorts = securities.filter(s => s.percentage > 0)

  if(securities_without_shorts.length >= securities.length) {
    return portfolio
  }

  const new_total_weight = securities_without_shorts
    .map(s => s.percentage)
    .reduce((total, current) => total + current, 0)

  securities_without_shorts.forEach(security => {
    security.percentage = parseFloat((security.percentage * 100/ new_total_weight).toFixed(4))
  })

  new_portfolio.securities = securities_without_shorts

  return new_portfolio
}

