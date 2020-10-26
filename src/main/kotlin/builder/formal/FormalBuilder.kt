// TODO: Add task feedback
// TODO: Add way to skip if not changed

package builder.formal

import builder.formal.builders.BusyBuilder
import builder.formal.stores.SimpleStore

// Implementations

data class NullInformation(val name: String? = null) : Information

data class NamedTask(
    val name: String, override val input: Set<Key>, override val output: Key,
    override val f: (task: Task<Condition, Key, Value>) -> Unit
) : Task<Condition, Key, Value>

fun main() {
    val store = SimpleStore<Key, Value>()

    store.put("input1", "SomeInput")

    val task1 = NamedTask(
        "task1",
        setOf("input1"),
        "output1"
    ) { task ->
        store.put("output1", "someValue")
    }

    val task2 = NamedTask(
        "task2",
        setOf("output1"),
        "output2"
    ) { task ->
        store.put("output2", "someOtherValue")
    }

    // Add that one to add a cycle

    val task3 = NamedTask(
        "task3",
        setOf("output2"),
        "input1"
    ) { task ->
        store.put("input1", "foo")
    }

    val task4 = NamedTask(
        "task4",
        setOf("input1", "output2", "output1"),
        "output4"
    ) { task ->
        store.put("output4", "Bar")
    }

    val busyBuilder = BusyBuilder(
        setOf(task1, task2, task4, task4), store
    )

    busyBuilder.build("output4")
}