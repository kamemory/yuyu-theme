import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import com.iwagawake.yuyu.gradle.plugin.BaseDefinition
import com.iwagawake.yuyu.gradle.plugin.BaseThemeCollection
import com.iwagawake.yuyu.gradle.plugin.BaseThemeDefinition
import com.iwagawake.yuyu.gradle.plugin.ThemeTemplate
import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlNodePrinter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

data class JetbrainsThemeDefinition(
    val id: String,
    val dark: Boolean,
    val name: String,
    val author: String,
    val editorScheme: String? = null,
    val colors: Map<String, Any>,
    val ui: Map<String, Any>,
    val icons: Map<String, Any>
)

open class BuildThemes : DefaultTask() {
    private val gson = GsonBuilder()
        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .setPrettyPrinting()
        .create()

    init {
        group = "yuyu"
        description = "Build all YUYU theme files."
    }

    @TaskAction
    fun run() {
        val baseThemePath = Paths.get(project.rootDir.absolutePath, "baseTheme")

        val pluginXmlPath = Paths.get(this.getResourceDirectory().toString(), "META-INF", "plugin.xml")
        val pluginXml = parseXml(pluginXmlPath)
        val extensionsNode = this.cleanPluginXmlThemes(pluginXml)

        BaseThemeCollection.findBaseThemes(baseThemePath)
            .forEach {
                val (themePath, themeDef) = it
                val (themeDir, baseName) = themePath
                val themeJsonPath = this.createThemeJson(baseName, themeDir, themeDef)

                this.applyThemeProvider(extensionsNode, themeDef, themeJsonPath)
            }

        this.writeXmlToFile(pluginXmlPath, pluginXml)
    }

    private fun createThemeJson(baseName: String, themePath: Path, theme: BaseThemeDefinition): Path {
        val themeResourceDir = this.getThemeDirectory(theme)
        if (!Files.exists(themeResourceDir)) {
            Files.createDirectories(themeResourceDir)
        }
        val themeJsonPath = Paths.get(
            this.getThemeDirectory(theme).toString(),
            "$baseName.theme.json"
        )
        if (Files.exists(themeJsonPath)) {
            Files.delete(themeJsonPath)
        }

        val templateFile = Paths.get(this.getThemeTemplateDirectory().toString(), "theme.template.json")
        val themeTemplate = File(templateFile.toString()).inputStream().use {
            gson.fromJson(it.bufferedReader(), ThemeTemplate::class.java)
        }

        val baseDefFile = Paths.get(this.getBaseDefinitionDirectory().toString(), "base.definition.json")
        val baseDef = File(baseDefFile.toString()).inputStream().use {
            gson.fromJson(it.bufferedReader(), BaseDefinition::class.java)
        }

        val themeColors = this.createThemeColors(theme)
        val namedColors = this.mergeThemeColors(baseDef, theme, themeColors)

        val themeDefinition = JetbrainsThemeDefinition(
            id = theme.id,
            dark = theme.dark,
            name = theme.name,
            author = theme.author,
            editorScheme = this.createEditorScheme(baseName, themePath, theme, namedColors),
            colors = namedColors,
            ui = this.createUiDefinition(theme, themeTemplate),
            icons = this.createIconsDefinition(theme, themeTemplate, namedColors)
        )
        File(themeJsonPath.toString()).outputStream()
            .writer().use {
                gson.toJson(themeDefinition, it)
            }

        return themeJsonPath
    }

    private fun cleanPluginXmlThemes(pluginXml: Node): Node {
        val extension = pluginXml.children()
            .filterIsInstance<Node>()
            .firstOrNull { it.name().equals("extensions") && it.attribute("defaultExtensionNs")?.equals("com.intellij") == true }
            ?: throw IllegalArgumentException("Node <extensions> not found in plugin.xml")

        return extension
    }

    private fun applyThemeProvider(extensionsNode: Node, theme: BaseThemeDefinition, themePath: Path) {
        val themeProvider = extensionsNode.children()
            .filterIsInstance<Node>()
            .firstOrNull { it.name().equals("themeProvider") && it.attribute("id")?.equals(theme.id) == true }
            ?: return
        themeProvider.attributes().replace("path", this.extractResourcesPath(themePath))
    }

    private fun getThemeDirectory(baseThemeDefinition: BaseThemeDefinition): Path {
        return Paths.get(
            this.getResourceDirectory().toString(),
            "yuyu",
            "themes",
            baseThemeDefinition.group,
            baseThemeDefinition.character
        )
    }

    private fun createThemeColors(theme: BaseThemeDefinition): Map<String, Any> {
        val colorMap = createColorMap(theme.themeColor)
        val foreground = theme.foregroundColor ?: colorMap.color100
        val foregroundMap = createColorMap(foreground)
        val lightMap = createColorMap(colorMap.color800)

        return mapOf(
            "colorPrimary" to colorMap.color500,
            "colorPrimarySubtle" to colorMap.color900,
            "colorPrimaryLight" to lightMap.color800,
            "colorPrimaryDark" to colorMap.color400,
            "colorPrimaryHover" to colorMap.color600,
            "colorForeground" to foreground,
            "colorForegroundLight" to foregroundMap.color800,
        )
    }

    private fun mergeThemeColors(
        baseDef: BaseDefinition,
        theme: BaseThemeDefinition,
        themeColor: Map<String, Any>
    ): Map<String, Any> {
        val mutableThemeColors = themeColor.toMutableMap()

        baseDef.colors?.forEach {
            mutableThemeColors[it.key] = it.value
        }
        theme.colors?.forEach {
            mutableThemeColors[it.key] = it.value
        }

        return mutableThemeColors
    }

    private fun createUiDefinition(theme: BaseThemeDefinition, template: ThemeTemplate): Map<String, Any> {
        return template.ui
    }

    private fun createIconsDefinition(
        theme: BaseThemeDefinition,
        template: ThemeTemplate,
        colors: Map<String, Any>
    ): Map<String, Any> {
        return this.resolveNamedColor(template.icons, colors)
    }

    @Suppress("unchecked_cast")
    private fun resolveNamedColor(src: Map<String, Any>, colors: Map<String, Any>): Map<String, Any> {
        return src.map {
            it.key to when (it.value) {
                is String -> this.resolveColor(it.value as String, colors)
                is Map<*, *> -> this.resolveNamedColor(it.value as Map<String, Any>, colors)
                else -> it.value
            }
        }.toMap()
    }

    private fun resolveColor(text: String, colors: Map<String, Any>): String {
        return if (colors.containsKey(text)) {
            colors[text].toString()
        } else {
            text
        }
    }

    private fun createEditorScheme(
        baseName: String,
        themePath: Path,
        theme: BaseThemeDefinition,
        colors: Map<String, Any>
    ): String {
        // 出力するエディタスキーマXMLファイルパス
        val themeXmlPath = Paths.get(
            this.getThemeDirectory(theme).toString(),
            "${baseName}.xml"
        )

        // おおもとのテンプレートXMLファイル
        val templateFile = Paths.get(this.getThemeTemplateDirectory().toString(), "theme.template.xml")
        // を読み込み
        val parsedTemplateXml = parseXml(templateFile)

        // テーマ独自定義XML
        val themeDefXmlPath = Paths.get(themePath.toString(), theme.editorScheme)
        val themeDefXml = parseXml(themeDefXmlPath)

        //this.resolveEditorSchemeXml(parsedTemplateXml, theme, colors)
        val generatedXml = this.resolveEditorSchemeXml(parsedTemplateXml, themeDefXml, theme, colors)

        //this.writeXmlToFile(themeXmlPath, parsedTemplateXml)
        this.writeXmlToFile(themeXmlPath, generatedXml)

        return this.extractResourcesPath(themeXmlPath)
    }

    private fun getResourceDirectory(): Path {
        return Paths.get(
            project.rootDir.absolutePath,
            "src",
            "main",
            "resources"
        )
    }

    private fun getThemeTemplateDirectory(): Path {
        return Paths.get(
            project.rootDir.absolutePath,
            "buildSrc",
            "templates"
        )
    }

    private fun getBaseDefinitionDirectory(): Path {
        return Paths.get(
            project.rootDir.absolutePath,
            "buildSrc",
            "base"
        )
    }

    private fun resolveEditorSchemeXml(
        themeTemplateXml: Node,
        themeDefXml: Node,
        theme: BaseThemeDefinition,
        colors: Map<String, Any>
    ): Node {
        val themeDefAttributes = themeDefXml["attributes"] as NodeList
        val templateAttributes = themeTemplateXml["attributes"] as NodeList
        themeDefAttributes.zip(templateAttributes)
            .filter { it.first is Node && it.second is Node }
            .map { it.first as Node to it.second as Node }
            .flatMap {
                (it.first.value() as NodeList).map { childNode ->
                    childNode as Node to it.second.value() as NodeList
                }
            }
            .forEach {
                val (childNode, templateList) = it
                val childName = childNode.attribute("name")
                val templateNode = templateList.map { it as Node }
                    .indexOfFirst { n ->
                        n.attribute("name") == childName
                    }
                if (templateNode >= 0) {
                    templateList.removeAt(templateNode)
                }
                templateList.add(childNode)
            }

        this.applyEditorSchemeColors(themeTemplateXml, theme, colors)

        return themeTemplateXml
    }

    private fun applyEditorSchemeColors(
        node: Node,
        theme: BaseThemeDefinition,
        colors: Map<String, Any>
    ) {
        node.breadthFirst()
            .filterIsInstance<Node>()
            .forEach {
                when (it.name()) {
                    "scheme" -> it.attributes().replace("name", theme.name)
                    "option" -> {
                        val value = it.attribute("value") as? String
                        if (value?.contains("$") == true) {
                            this.applyColor(it, value, colors)
                        }
                    }
                }
            }
    }

    private fun applyColor(node: Node, value: String, colors: Map<String, Any>) {
        val start = value.indexOf("$")
        val end = value.indexOf("$", start + 1)
        if (end == -1) {
            return
        }

        val colorKey = value.substring(start + 1, end)
        val colorValue = (colors[colorKey] as? String)?.replace("#", "") ?: return
        val attributeValue = value.replace("$${colorKey}$", colorValue)

        node.attributes().replace("value", attributeValue)
    }

    private fun extractResourcesPath(path: Path): String {
        val fullPath = path.toString()
        val separator = File.separator
        val resourcePath = fullPath.substring(fullPath.indexOf("${separator}yuyu${separator}themes"))
        return resourcePath.replace(separator, "/")
    }

    private fun writeXmlToFile(file: Path, node: Node) {
        File(file.toString()).outputStream().use { stream ->
            PrintWriter(stream).use { writer ->
                val printer = XmlNodePrinter(writer)
                printer.isPreserveWhitespace = true
                printer.print(node)
            }
        }
    }
}