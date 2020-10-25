package builder

typealias Hash = Int

typealias Key = String

interface Store {
    fun get(key: Key): Hashable
    fun put(key: Key, value: Hashable)
}

interface Hashable {
    val key: String
    fun getHash(store: Store): Hash
}

interface Input : Hashable

// Implementations

open class FakeHash(override val key: String) : Hashable {
    override fun getHash(store: Store): Hash {
        return key.hashCode()
    }
}

class TestStringInput(override val key: String) : FakeHash(key) {
    override fun toString(): String = "Input<$key>"
}

class TestStringOutput(override val key: String) : FakeHash(key) {
    override fun toString(): String = "Output<$key>"
}

data class Task(
    val input: Set<Key>,
    val output: Key,
    val f: (task: Task, store: Store) -> Hashable
)

/**
 * A store with no memory
 */
class FakeStore : Store {
    override fun get(key: Key): Hashable = FakeHash(key)

    override fun put(key: Key, value: Hashable) {
        println("Updated the store for $key with $value")
        // noop
    }
}

interface Builder {
    fun fetch(key: Key): Hashable?
}

data class TaskDesc(
    val input: MutableSet<Key>,
    val outputs: MutableSet<Key>
)

class BusyBuilder(val tasks: Set<Task>, val store: Store) : Builder {
    override fun fetch(key: Key): Hashable {
        val tasksMatching = tasks.filter { it.output == key }
        return if (tasksMatching.isEmpty()) {
            // nothing to do, no task explain how to make it
            store.get(key)
        } else {
            require(tasksMatching.size == 1) { "We have two tasks making the same output!" }
            val task = tasksMatching.first()
            println("Building $key")


            task.input.forEach { fetch(it) }
            task.f(task, store).also { value ->
                println("The task made $value")
                require(value.key == key) { "The task didn't build what it said it would build" }
                store.put(key, value)
            }
        }
    }

    fun getAllInputsAndOutputs(task: Task, taskDesc: TaskDesc) {
        // We check all the inputs of the given task and we verify if they are in the already known outputs to
        // detect cycles. If not, we iterate into those tasks and check their inputs.
        task.input.forEach { key ->
            // Check if this input was produced already by one of the outputs
            if (key in taskDesc.outputs) throw Exception("We have a loop")
            // Find the task making the current input
            val allOutputs = tasks.filter { it.output == key }
            taskDesc.outputs.addAll(allOutputs.map { it.output }.toSet() - taskDesc.input)
            allOutputs.map { getAllInputsAndOutputs(it, taskDesc) }
        }
    }

    fun build(key: Key) {
        // Validate we have no cycles
        println("Checking for loops")
        tasks.forEach {
            val taskDesc = TaskDesc(it.input.toMutableSet(), mutableSetOf(it.output))
            getAllInputsAndOutputs(it, taskDesc)
        }
        fetch(key)
    }
}

fun main() {
    val store = FakeStore()

    val task1 = Task(
        setOf("input1"),
        "output1"
    ) { task, store ->
        TestStringOutput("output1")
    }

    val task2 = Task(
        setOf("output1"),
        "output2"
    ) { task, store ->
        TestStringOutput("output2")
    }

    // Add that one to add a cycle

    val task3 = Task(
        setOf("output2"),
        "input1"
    ) { task, store ->
        TestStringOutput("input1")
    }

    val task4 = Task(
        setOf("input1", "output2", "output1"),
        "output4"
    ) { task, store ->
        TestStringOutput("output4")
    }

    val busyBuilder = BusyBuilder(setOf(task1, task2, task4, task4), store)

    busyBuilder.build("output4")
}