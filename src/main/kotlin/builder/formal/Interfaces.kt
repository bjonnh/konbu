package builder.formal

typealias Key = String
typealias Value = String

interface PersistentInformation
interface Information
interface MonadState<T> : Condition

interface Store<I : Information, K : Key, V : Value> {
    var info: I
    fun get(key: K): V
    fun put(key: K, value: V)
}

interface Hashable {
    val key: String
    val value: String
}

interface Builder<K : Key, V : Value> {
    fun fetch(key: K): V
}

interface Condition

interface Task<C : Condition, K : Key, V : Value> {
    val input: Set<K>
    val output: K
    val f: (task: Task<C, K, V>) -> Unit
}

interface Tasks<C : Condition, K : Key, V : Value>

interface Build<C : Condition, I : Information, K : Key, V : Value> {
    fun build(tasks: Tasks<C, K, V>, key: K, store: Store<I, K, V>): Store<I, K, V>
}

interface Rebuilder<C : Condition, IR : PersistentInformation, K : Key, V : Value> {
    fun rebuild(k: K, v: V, task: Task<C, K, V>): Task<MonadState<IR>, K, V>
}

interface Scheduler<C : Condition, I : Information, IR : PersistentInformation, K : Key, V : Value> {
    fun schedule(rebuilder: Rebuilder<C, IR, K, V>): Build<C, I, K, V>
}