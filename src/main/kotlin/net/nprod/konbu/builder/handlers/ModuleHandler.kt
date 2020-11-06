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
        val inFile = File(modulesSource, module)
        return OntoTask(
            "Creating module $module",
            listOf(inFile) + dependencies,
            getOutFile(module)
        ) {
            robotController.handler().template(
                input = mainSource,
                template = inFile,
                prefixes = buildParameters.prefixes
            ).annotateOntology("${buildParameters.uribase}/modules/$module.owl", outFile = getOutFile(module))
            NullValue()
        }
    }

    /**
     * Get the output file of the task
     */
    fun getOutFile(module: OntoModule) = File(modulesOut, "${module.split(".")[0]}.owl")
}