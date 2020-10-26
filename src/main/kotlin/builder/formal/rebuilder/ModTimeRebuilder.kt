package builder.formal.rebuilder

import builder.formal.*
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

data class MakeInfo<K : Key> @ExperimentalTime constructor(
    val startTime: TimeMark,
    val timestamps: MutableMap<Key, TimeMark>
) // We can't use variable types on typealias so we are going to use a dataclass…

@ExperimentalTime
class ModTimeRebuilder<C : Condition, IR : PersistentInformation, K : Key, V : Value> : Rebuilder<C, IR, K, V> {
    val makeInfo = MakeInfo<K>(
        TimeSource.Monotonic.markNow(), mutableMapOf()
    )

    override fun rebuild(k: K, v: V, task: Task<C, K, V>): Task<MonadState<IR>, K, V> {
        val dirty: Boolean = when (makeInfo.timestamps[k]) {
            null -> true
            else -> task.input.any { // TODO: Finish that part can't use TimeMark as they are not comparable
            val time =     makeInfo.timestamps[it].
                  time == null || time!! > makeInfo.startTime
            }
        }
    }
}