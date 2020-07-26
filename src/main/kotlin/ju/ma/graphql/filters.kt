package ju.ma.graphql

import com.github.alexliesenfeld.querydsl.jpa.hibernate.JsonPath
import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.DateTimePath
import com.querydsl.core.types.dsl.EnumPath
import com.querydsl.core.types.dsl.NumberExpression
import com.querydsl.core.types.dsl.StringExpression
import java.math.BigDecimal

/**
 * An integer comparison object
 */
open class NumberComparison<T>(
    /**
     * Matches values equal to
     */
    var _eq: T? = null,
    /**
     * Matches values greater than
     */
    var _gt: T? = null,
    /**
     * Matches values greater than or equal to
     */
    var _gte: T? = null,
    /**
     * Matches values contained in the collection
     */
    var _in: MutableList<T>? = null,
    /**
     * Matches values that are null
     */
    var _is_null: Boolean? = null,
    /**
     * Matches values less than
     */
    var _lt: T? = null,
    /**
     * Matches values less than or equal to
     */
    var _lte: T? = null,
    /**
     * Matches values not equal to
     */
    var _neq: T? = null,
    /**
     * Matches values not in the collection
     */
    var _nin: MutableList<T>? = null,
    /**
     * Matches value by text comparision
     */
    var _like: String? = null,
    /**
     * Matches if value contains
     */
    var _contains: String? = null,
    /**
     * Filter by string comparison
     */
    var _string: TextComparison? = null
) where T : Number, T : Comparable<T> {
    /**
     * Returns a filter for the specified [path]
     */
    fun build(path: NumberExpression<T>): BooleanBuilder {
        val builder = BooleanBuilder()
        _eq?.let { builder.and(path.eq(it)) }
        _gt?.let { builder.and(path.gt(it)) }
        _gte?.let { builder.and(path.goe(it)) }
        _in?.let { builder.and(path.`in`(it)) }
        _is_null?.let {
            if (it) {
                builder.and(path.isNull)
            } else {
                builder.and(path.isNotNull)
            }
        }
        _lt?.let { builder.and(path.lt(it)) }
        _lte?.let { builder.and(path.loe(it)) }
        _neq?.let { builder.and(path.ne(it)) }
        _nin?.let { builder.and(path.notIn(it)) }
        _like?.let { builder.and(path.like(it)) }
        _contains?.let { builder.and(path.stringValue().contains(it)) }
        _string?.let { builder.and(it.build(path.stringValue())) }
        return builder
    }
}

/**
 * Comparison class for integers
 */
class IntegerComparision(
    _eq: Int? = null,
    _gt: Int? = null,
    _gte: Int? = null,
    _in: MutableList<Int>? = null,
    _is_null: Boolean? = null,
    _lt: Int? = null,
    _lte: Int? = null,
    _neq: Int? = null,
    _nin: MutableList<Int>? = null
) : NumberComparison<Int>(_eq, _gt, _gte, _in, _is_null, _lt, _lte, _neq, _nin)

/**
 * Comparison class for Long
 */
class LongComparision(
    _eq: Long? = null,
    _gt: Long? = null,
    _gte: Long? = null,
    _in: MutableList<Long>? = null,
    _is_null: Boolean? = null,
    _lt: Long? = null,
    _lte: Long? = null,
    _neq: Long? = null,
    _nin: MutableList<Long>? = null
) : NumberComparison<Long>(_eq, _gt, _gte, _in, _is_null, _lt, _lte, _neq, _nin)

/**
 * Comparison class for BigDecimal
 */
class BigDecimalComparision(
    _eq: BigDecimal? = null,
    _gt: BigDecimal? = null,
    _gte: BigDecimal? = null,
    _in: MutableList<BigDecimal>? = null,
    _is_null: Boolean? = null,
    _lt: BigDecimal? = null,
    _lte: BigDecimal? = null,
    _neq: BigDecimal? = null,
    _nin: MutableList<BigDecimal>? = null
) : NumberComparison<BigDecimal>(_eq, _gt, _gte, _in, _is_null, _lt, _lte, _neq, _nin)

/**
 * A string comparison object
 */
class TextComparison(
    /**
     * Matches values equal to
     */
    var _eq: String? = null,
    /**
     * Matches values greater than
     */
    var _gt: String? = null,
    /**
     * Matches values greater than or equal to
     */
    var _gte: String? = null,
    /**
     * Matches values contained in the collection
     */
    var _in: MutableList<String>? = null,
    /**
     * Matches values that are null
     */
    var _is_null: Boolean? = null,
    /**
     * Matches values less than
     */
    var _lt: String? = null,
    /**
     * Matches values less than or equal to
     */
    var _lte: String? = null,
    /**
     * Matches values not equal to
     */
    var _neq: String? = null,
    /**
     * Matches values not in the collection
     */
    var _nin: MutableList<String>? = null,
    /**
     * Matches values that contain
     */
    var _contains: String? = null,
    /**
     * Matches values that don't contain
     */
    var _ncontains: String? = null,
    /**
     * Matches values like : Case Sensitive
     */
    var _like: String? = null,
    /**
     * Matches values not like : Case Sensitive
     */
    var _nlike: String? = null,
    /**
     * Matches values like : Case Insensitive
     */
    var _ilike: String? = null,
    /**
     * Matches values not like : Case Insensitive
     */
    var _nilike: String? = null,
    /**
     * Matches values that are similar to the regex
     */
    var _matches: String? = null,
    /**
     * Matches values that are not similar to the regex
     */
    var _nmatches: String? = null,
    /**
     * Matches by converting the value to long and comparing
     */
    var _number: LongComparision? = null
) {
    /**
     * Returns a filter for the specified [path]
     */
    fun build(path: StringExpression): BooleanBuilder {
        val builder = BooleanBuilder()
        _eq?.let { builder.and(path.eq(it)) }
        _gt?.let { builder.and(path.gt(it)) }
        _gte?.let { builder.and(path.goe(it)) }
        _in?.let { builder.and(path.`in`(it)) }
        _is_null?.let {
            if (it) {
                builder.and(path.isNull)
            } else {
                builder.and(path.isNotNull)
            }
        }
        _lt?.let { builder.and(path.lt(it)) }
        _lte?.let { builder.and(path.loe(it)) }
        _neq?.let { builder.and(path.ne(it)) }
        _nin?.let { builder.and(path.notIn(it)) }
        _contains?.let { builder.and(path.containsIgnoreCase(it)) }
        _ncontains?.let { builder.and(path.containsIgnoreCase(it).not()) }
        _like?.let { builder.and(path.like(it)) }
        _nlike?.let { builder.and(path.notLike(it)) }
        _ilike?.let { builder.and(path.likeIgnoreCase(it)) }
        _nilike?.let { builder.and(path.likeIgnoreCase(it).not()) }
        _matches?.let { builder.and(path.matches(it)) }
        _nmatches?.let { builder.and(path.matches(it).not()) }
        _number?.let { builder.and(it.build(path.castToNum(Long::class.java))) }
        return builder
    }
}

/**
 * A string comparison object
 */
open class EnumComparison<T>(
    /**
     * Matches values equal to
     */
    var _eq: T? = null,
    /**
     * Matches values greater than
     */
    var _gt: T? = null,
    /**
     * Matches values greater than or equal to
     */
    var _gte: T? = null,
    /**
     * Matches values contained in the collection
     */
    var _in: MutableList<T>? = null,
    /**
     * Matches values that are null
     */
    var _is_null: Boolean? = null,
    /**
     * Matches values less than
     */
    var _lt: T? = null,
    /**
     * Matches values less than or equal to
     */
    var _lte: T? = null,
    /**
     * Matches values not equal to
     */
    var _neq: T? = null,
    /**
     * Matches values not in the collection
     */
    var _nin: MutableList<T>? = null
) where T : Enum<T> {
    /**
     * Returns a filter for the specified [path]
     */
    fun build(path: EnumPath<T>): BooleanBuilder {
        val builder = BooleanBuilder()
        _eq?.let { builder.and(path.eq(it)) }
        _gt?.let { builder.and(path.gt(it)) }
        _gte?.let { builder.and(path.goe(it)) }
        _in?.let { builder.and(path.`in`(it)) }
        _is_null?.let {
            if (it) {
                builder.and(path.isNull)
            } else {
                builder.and(path.isNotNull)
            }
        }
        _lt?.let { builder.and(path.lt(it)) }
        _lte?.let { builder.and(path.loe(it)) }
        _neq?.let { builder.and(path.ne(it)) }
        _nin?.let { builder.and(path.notIn(it)) }
        return builder
    }
}

/**
 * A time comparison object
 */
class TimeComparison<T : Comparable<*>>(
    /**
     * Matches values equal to
     */
    var _eq: T? = null,
    /**
     * Matches values greater than
     */
    var _gt: T? = null,
    /**
     * Matches values greater than or equal to
     */
    var _gte: T? = null,
    /**
     * Matches values contained in the collection
     */
    var _in: MutableList<T>? = null,
    /**
     * Matches values that are null
     */
    var _is_null: Boolean? = null,
    /**
     * Matches values less than
     */
    var _lt: T? = null,
    /**
     * Matches values less than or equal to
     */
    var _lte: T? = null,
    /**
     * Matches values not equal to
     */
    var _neq: T? = null,
    /**
     * Matches values not in the collection
     */
    var _nin: MutableList<T>? = null
) {
    /**
     * Returns a filter for the specified [path]
     */
    fun build(path: DateTimePath<T>): BooleanBuilder {
        val builder = BooleanBuilder()
        _eq?.let { builder.and(path.eq(it)) }
        _gt?.let { builder.and(path.gt(it)) }
        _gte?.let { builder.and(path.goe(it)) }
        _in?.let { builder.and(path.`in`(it)) }
        _is_null?.let {
            if (it) {
                builder.and(path.isNull)
            } else {
                builder.and(path.isNotNull)
            }
        }
        _lt?.let { builder.and(path.lt(it)) }
        _lte?.let { builder.and(path.loe(it)) }
        _neq?.let { builder.and(path.ne(it)) }
        _nin?.let { builder.and(path.notIn(it)) }
        return builder
    }
}

/**
 * A boolean comparison object
 */
class BooleanComparison(
    /**
     * Matches values equal to
     */
    var _eq: Boolean? = null,
    /**
     * Matches values contained in the collection
     */
    var _in: MutableList<Boolean>? = null,
    /**
     * Matches values that are null
     */
    var _is_null: Boolean? = null,
    /**
     * Matches values not equal to
     */
    var _neq: Boolean? = null,
    /**
     * Matches values not in the collection
     */
    var _nin: MutableList<Boolean>? = null
) {
    /**
     * Returns a filter for the specified [path]
     */
    fun build(path: BooleanExpression): BooleanBuilder {
        val builder = BooleanBuilder()
        _eq?.let { builder.and(path.eq(it)) }
        _in?.let { builder.and(path.`in`(it)) }
        _is_null?.let {
            if (it) {
                builder.and(path.isNull)
            } else {
                builder.and(path.isNotNull)
            }
        }
        _neq?.let { builder.and(path.ne(it)) }
        _nin?.let { builder.and(path.notIn(it)) }
        return builder
    }
}

class JsonComparison(
    var key: String,
    var _int: IntegerComparision? = null,
    var _long: LongComparision? = null,
    var _text: TextComparison? = null,
    var _bool: BooleanComparison? = null,
    var _length: IntegerComparision? = null,
    var _in: MutableList<Any>? = null,
    var _nin: MutableList<Any>? = null
) {
    /**
     * Returns a filter for the specified [path]
     */
    fun build(path: JsonPath): BooleanBuilder {
        val json = path.get(key)
        val builder = BooleanBuilder()
        _int?.let { builder.and(it.build(json.asInt())) }
        _long?.let { builder.and(it.build(json.asLong())) }
        _text?.let { builder.and(it.build(json.asText())) }
        _length?.let { builder.and(it.build(json.length())) }
        _in?.let { builder.and(json.contains(it)) }
        _nin?.let { builder.and(json.contains(it).not()) }
        _bool?.let { builder.and(it.build(json.asBool())) }
        return builder
    }
}
