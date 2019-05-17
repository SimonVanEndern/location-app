package com.simonvanendern.tracking.aggregation

class AverageAggregationRequest<T : Datatype>(
    val t: T,
    private var n: Int,
    private var value: Float
) {
    fun execute() {
        val ownValue = t.getValue()
        value = ((n * value) + ownValue) / (++n)
    }
}