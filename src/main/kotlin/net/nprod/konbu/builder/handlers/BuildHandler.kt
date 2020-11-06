package net.nprod.konbu.builder.handlers

import net.nprod.konbu.builder.BuildParameters
import net.nprod.konbu.builder.Target
import net.nprod.konbu.builder.TargetType
import net.nprod.konbu.builder.formal.tasks.NullValue
import net.nprod.konbu.controllers.OntoTask
import net.nprod.konbu.controllers.robot.RobotController
import net.nprod.konbu.controllers.robot.RobotOutputFile
import java.io.File


class BuildHandler(private val buildParameters: BuildParameters, private val robotController: RobotController) {
    private val root = buildParameters.root
    private val mainSource = File(File(root), buildParameters.mainSource)

    fun outputFile(target: String, format: String): File =
        File(File(File(root), "output"), "${buildParameters.name}-$target.$format")

    fun getTasks(target: Target, importFiles: List<File>, moduleFiles: List<File>): List<OntoTask> {
        val mainOwl = outputFile(target.name, "owl")

        val tasks = mutableListOf(OntoTask(
            "Building ${target.name} owl",
            listOf(mainSource) + importFiles + moduleFiles,
            mainOwl
        ) {
            makeOwl(target, mainOwl, moduleFiles)
            NullValue()
        })


        buildParameters.formats.forEach { format ->
            if (format != "json" && format != "obo") throw RuntimeException("Unhandled format $format")
            tasks.add(
                OntoTask(
                    "Building ${target.name} $format",
                    listOf(mainOwl),
                    outputFile(target.name, format)
                ) {
                    makeFormat(mainOwl, target, format)
                    NullValue()
                }
            )
        }
        return tasks
    }

    // TODO: We need to allow for a two step process and maybe go back to the optimized way when needed?
    //       We could keep the owl version cached
    private fun makeFormat(mainOwl: RobotOutputFile, target: Target, format: String) {
        val tempFile = createTempFile()
        val owl = robotController.handler().convertFromFile(mainOwl, format, tempFile)
        outputFile(target.name, format).writeText(
            tempFile.readLines().filter { line -> !line.startsWith("owl-axioms") }
                .joinToString("\n")
        )
        tempFile.delete()
    }

    private fun makeOwl(target: Target, mainOwl: RobotOutputFile, moduleFiles: List<File>) {
        val out = when (target.targetType) {
            TargetType.FULL -> {
                val merged = robotController.handler()
                    .merge(mainSource, moduleFiles)
                if (target.reasoning) {
                    merged.reason().relax().reduce()
                } else {
                    merged
                }
            }
            TargetType.BASE -> {
                val merged = robotController.handler()
                    .mergeAndRemoveImports(mainSource, listOf())
                if (target.reasoning) {
                    throw Exception("We are not using reasoning on base")
                }
                merged
            }
        }

        out.annotateOntology(
            iri = buildParameters.uribase,
            version = "${buildParameters.uribase}/releases/${buildParameters.version}/${buildParameters.name}",
            versionName = buildParameters.version,
            outFile = mainOwl
        )
    }
}