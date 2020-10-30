package builder.handlers

import builder.BuildParameters
import builder.Target
import builder.TargetType
import cache.FileCacheManager
import controllers.robot.RobotController
import mu.KotlinLogging
import timeBlock
import java.io.File


class BuildHandler(private val buildParameters: BuildParameters, private val robotController: RobotController) {
    private val root = buildParameters.root
    private val cacheManager = FileCacheManager(File(root, "cache"))

    private fun outputFile(target: String, format: String) = File(File(File(root), "output"), "${buildParameters.name}-$target.$format")

    fun build(target: Target) {
        logger.timeBlock("  Building ${target.name}") {
            val out = when (target.targetType) {
                TargetType.FULL -> {
                    val merged = robotController.handler()
                        .merge(File(File(root), buildParameters.mainSource), listOf())
                    if (target.reasoning) {
                        merged.reason().relax().reduce()
                    } else {
                        merged
                    }
                }
                TargetType.BASE -> {
                    val merged = robotController.handler()
                        .mergeAndRemoveImports(File(File(root), buildParameters.mainSource), listOf())
                    if (target.reasoning) {
                        throw Exception("We are not using reasoning on base")
                    }
                    merged
                }
            }
            val annotated = out.annotateOntology(
                iri = buildParameters.uribase,
                version = "${buildParameters.uribase}/releases/${buildParameters.version}/${buildParameters.name}",
                versionName = buildParameters.version,
                outFile = outputFile(target.name, "owl")
            )

            // Conversions
            buildParameters.formats.forEach {
                when (it) {
                    "json" -> {
                        logger.timeBlock("Converting to json format") {
                            val tempFile = createTempFile()
                            annotated.convert(format = "json", tempFile)
                            outputFile(target.name, "json").writeText(
                                tempFile.readLines().filter { line -> !line.startsWith("owl-axioms") }
                                    .joinToString("\n")
                            )
                            tempFile.delete()
                        }
                    }
                    "obo" -> {
                        logger.timeBlock("Converting to obo format") {
                            val tempFile = createTempFile()
                            annotated.convert(format = "obo", tempFile)
                            outputFile(target.name, "obo").writeText(
                                tempFile.readLines().filter { line -> !line.startsWith("owl-axioms") }
                                    .joinToString("\n")
                            )
                            tempFile.delete()
                        }
                    }
                    else -> throw Exception("Unhandled format for converting ontology: $it")
                }

            }

        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}