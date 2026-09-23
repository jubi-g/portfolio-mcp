package dev.jhubie.portfoliomcp.repository

import dev.jhubie.portfoliomcp.domain.Portfolio

/** The seam that makes the data source swappable; callers depend only on this. */
interface PortfolioRepository {
    fun getPortfolio(): Portfolio
}
