package com.gorthaur.financetracker.core.model

/**
 * Tipos de cambio respecto al euro: cuántas unidades de cada moneda equivalen
 * a 1 €. Permite convertir importes entre monedas para las estadísticas, sin
 * tocar la moneda real con la que se guardó cada movimiento.
 *
 * Los valores son editables en Ajustes y se pueden actualizar por internet.
 */
data class ExchangeRates(
    val unitsPerEur: Map<CurrencyCode, Double> = DEFAULT
) {
    /** Unidades de [currency] que equivalen a 1 € (EUR = 1.0). */
    fun unitsPerEur(currency: CurrencyCode): Double =
        unitsPerEur[currency]?.takeIf { it > 0 } ?: DEFAULT[currency] ?: 1.0

    /** Convierte [amount] de la moneda [from] a la moneda [to]. */
    fun convert(amount: Double, from: CurrencyCode, to: CurrencyCode): Double {
        if (from == to) return amount
        val amountInEur = amount / unitsPerEur(from)
        return amountInEur * unitsPerEur(to)
    }

    /** Convierte un importe expresado por su código de moneda. */
    fun convert(amount: Double, fromCode: String, to: CurrencyCode): Double {
        val from = CurrencyCode.fromCode(fromCode) ?: to
        return convert(amount, from, to)
    }

    fun withRate(currency: CurrencyCode, unitsPerEur: Double): ExchangeRates =
        copy(unitsPerEur = this.unitsPerEur.toMutableMap().apply { put(currency, unitsPerEur) })

    companion object {
        /** Valores aproximados de referencia (editables / actualizables). */
        val DEFAULT: Map<CurrencyCode, Double> = mapOf(
            CurrencyCode.EUR to 1.0,
            CurrencyCode.USD to 1.08,
            CurrencyCode.CAD to 1.47,
            CurrencyCode.KRW to 1450.0
        )
    }
}
