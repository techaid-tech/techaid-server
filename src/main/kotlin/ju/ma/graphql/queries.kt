package ju.ma.graphql

import com.querydsl.core.types.Expression
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

enum class PageOrderBy {
    /**
     * in the ascending order, nulls last
     */
    ASC,
    /**
     * in the ascending order, nulls first
     */
    ASC_NULLS_FIRST,
    /**
     * in the ascending order, nulls last
     */
    ASC_NULLS_LAST,
    /**
     * in the descending order, nulls first
     */
    DESC,
    /**
     * in the descending order, nulls first
     */
    DESC_NULLS_FIRST,
    /**
     * in the descending order, nulls last
     */
    DESC_NULLS_LAST
}

/**
 * A representation of a filter for requesting paginated data
 */
class PaginationInput(
    /**
     * The current page being requested
     */
    var page: Int = 0,
    /**
     * The number of elements in the page
     */
    var size: Int = 10,
    /**
     * How to sort the returned data. The key is the name of the column
     * with the value being the sort order ASC|DESC
     */
    var sort: List<KeyValuePair>? = null
) {
    companion object {
        fun sortBy(sorted: List<KeyValuePair>): Sort {
            return Sort.by(sorted.map { Sort.Order(Sort.Direction.fromString(it.value), it.key) })
        }

        fun <T : Comparable<*>> createOrder(order: PageOrderBy, target: Expression<T>): OrderSpecifier<T> {
            return when (order) {
                PageOrderBy.ASC -> OrderSpecifier(Order.ASC, target)
                PageOrderBy.ASC_NULLS_FIRST -> OrderSpecifier(Order.ASC, target, OrderSpecifier.NullHandling.NullsFirst)
                PageOrderBy.ASC_NULLS_LAST -> OrderSpecifier(Order.ASC, target, OrderSpecifier.NullHandling.NullsLast)
                PageOrderBy.DESC -> OrderSpecifier(Order.DESC, target)
                PageOrderBy.DESC_NULLS_FIRST -> OrderSpecifier(
                    Order.DESC,
                    target,
                    OrderSpecifier.NullHandling.NullsFirst
                )
                PageOrderBy.DESC_NULLS_LAST -> OrderSpecifier(Order.DESC, target, OrderSpecifier.NullHandling.NullsLast)
            }
        }
    }

    /**
     * Returns a page request that can be issued directly to a Spring Repository
     */
    fun create(): PageRequest {
        val sorted = sort ?: listOf()
        val sort: Sort = Sort.by(sorted.map { Sort.Order(Sort.Direction.fromString(it.value), it.key) })
        return PageRequest.of(page, size, sort)
    }

    fun create(sort: Sort): PageRequest {
        return PageRequest.of(page, size, sort)
    }
}

/**
 * Representation of a key value pair for graphQL
 */
class KeyValuePair(
    /**
     * The name of the key
     */
    var key: String,
    /**
     * The value for the key
     */
    var value: String
)

/**
 *  Global error handling class
 */
data class AppError(
    /**
     * Descriptive error message
     */
    var message: String,
    /**
     * The class of the error
     */
    var type: String = "Exception",
    /**
     * The extra details about the exception
     */
    var context: MutableList<KeyValuePair> = mutableListOf()
) {
    companion object {
        fun fromException(exception: Exception, message: String? = null): AppError {
            return AppError(
                message = message ?: exception.message ?: "",
                type = exception.javaClass.simpleName
            )
        }

        fun create(message: String, type: String = "Exception", context: Map<String, String> = mapOf()): AppError {
            return AppError(
                message = message,
                type = type,
                context = context.map { KeyValuePair(it.key, it.value) }.toMutableList()
            )
        }
    }
}
