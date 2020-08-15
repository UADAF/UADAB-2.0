package utils

import kotlin.random.Random

class WeightedList<A> {

    private val list = mutableListOf<Pair<Float, A>>()

    constructor(iterable: Iterable<Pair<Float, A>>) {
        addAll(iterable)
    }

    private val accumulatedWeight: Float
        get() = list.lastOrNull()?.first ?: 0.0f

    fun add(weight: Float, item: A) =
        list.add(accumulatedWeight + weight to item)

    fun addAll(iterable: Iterable<Pair<Float, A>>) {
        iterable.forEach { (weight, item) ->  add(weight, item) }
    }

    fun random(): A = (Random.nextDouble() * accumulatedWeight).let {
        list.first { x -> x.first >= it }.second
    }

}

fun <A> List<Pair<Float, A>>.toWeightedList(): WeightedList<A> =
    WeightedList(this)