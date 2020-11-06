package net.nprod.konbu.builder.handlers

import net.nprod.konbu.builder.BuildParameters
import net.nprod.konbu.builder.OntoModule
import net.nprod.konbu.builder.formal.tasks.NullValue
import net.nprod.konbu.controllers.OntoTask
import net.nprod.konbu.controllers.robot.RobotController
import java.io.File

class ModuleHandler(
    private val buildParameters: BuildParameters,
    private val robotController: RobotController,
    private val build: File
) {
    private val root = buildParameters.root
    private val modulesSource = File(root, "modules")
    private val modulesOut = File(build, "modules")
    private val mainSource = File(File(root), buildParameters.mainSource)

    init {
        modulesOut.mkdirs()
    }

    fun clean() {
        modulesOut.deleteRecursively()
        modulesOut.mkdirs()
    }

    fun getTasks(module: OntoModule, dependencies: List<File>): OntoTask {
        val inFile = File(modulesSource, module.name)
        val requirements = module.require.map { getOutFile(OntoModule(it, listOf())) }
        val totalDependencies =
            listOf(inFile) + dependencies + requirements // Dependencies but also the requirements of that module
        println("Total dependencies= $totalDependencies")
        println("Out file: ${getOutFile(module)}")
        println("Main Source: ${mainSource}")
        return OntoTask(
            "Creating module ${module.name}",
            totalDependencies,
            getOutFile(module)
        ) {
            robotController.handler().merge(
                input = mainSource,
                extraInput = requirements,
            ).template(
                template = inFile,
                prefixes = buildParameters.prefixes
            ).annotateOntology("${buildParameters.uribase}/modules/${module.name}.owl", outFile = getOutFile(module))
            NullValue()
        }
    }

    /**
     * Get the output file of the task
     */
    fun getOutFile(module: OntoModule): File = File(modulesOut, "${module.name.split(".")[0]}.owl")
}