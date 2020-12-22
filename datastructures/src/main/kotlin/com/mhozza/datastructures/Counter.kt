package com.mhozza.datastructures

class Counter<T> : HashMap<T, Int> {
    constructor(iterable: Iterable<T>) {
        super.putAll(iterable.groupingBy { it }.eachCount())
    }
    constructor(other: Map<out T, Int>) : super(other)
    constructor(item: T?) : this(listOfNotNull(item))

    operator fun plus(other: Counter<T>) : Counter<T>{
        val counter = Counter(this)
        counter += other
        return counter
    }

    operator fun plusAssign(other: Counter<T>) {
        other.forEach {
            this[it.key] = this.getOrDefault(it.key, 0) + it.value
        }
    }

    operator fun minusAssign(other: Counter<T>) {
        other.forEach {
            this[it.key] = this.getOrDefault(it.key, 0) - it.value
        }
        this.filter { it.value <= 0 }.forEach {this.remove(it.key) }
    }

    operator fun minus(other: Counter<T>) : Counter<T> {
        val counter = Counter(this)
        counter -= other
        return counter
    }

    override operator fun get(key: T): Int {
        return this.getOrDefault(key, 0)
    }

    override fun isEmpty(): Boolean {
        return super.isEmpty() || this.values.all { it == 0 }
    }

    companion object {
        fun fromCharSequence(charSequence: CharSequence): Counter<Char> = Counter(charSequence.asIterable())
    }
}

fun CharSequence.toCounter() = Counter.fromCharSequence(this)

