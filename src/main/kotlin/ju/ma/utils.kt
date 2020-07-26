package ju.ma

import java.util.Optional
import java.util.concurrent.Callable

/**
 * Utility for converting kotlin lambda into a java runnable
 */
fun runnable(f: () -> Unit): Runnable = Runnable { f() }

/**
 * Utility for converting a kotlin lambda into a java callable
 */
fun <T> callable(f: () -> T): Callable<T> = Callable { f() }

/**
 * Utility for transforming a Java Optional into a Kotlin Nullable
 */
fun <T : Any> Optional<T>.toNullable(): T? = this.orElse(null)
