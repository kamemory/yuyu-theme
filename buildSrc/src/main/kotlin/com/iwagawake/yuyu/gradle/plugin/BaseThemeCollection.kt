package com.iwagawake.yuyu.gradle.plugin

import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Stream

object BaseThemeCollection {
    private val gson = GsonBuilder()
        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .setPrettyPrinting()
        .create()

    fun findBaseThemes(baseThemeDirectory: Path): Stream<Pair<Pair<Path, String>, BaseThemeDefinition>> {
        val baseThemeDefinitionPath = Paths.get(baseThemeDirectory.toString(), "definitions")
        return Files.walk(baseThemeDefinitionPath)
            .filter { !Files.isDirectory(it) }
            .filter { it.fileName.toString().endsWith(".base.definition.json") }
            .map { it to Files.newInputStream(it) }
            .map { (jsonPath, stream) ->
                val themeBaseName = jsonPath.fileName.toString().replace(".base.definition.json", "")
                val def = gson.fromJson(InputStreamReader(stream), BaseThemeDefinition::class.java)
                jsonPath.parent to themeBaseName to def
            }
    }
}