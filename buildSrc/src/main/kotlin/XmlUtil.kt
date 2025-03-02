import groovy.util.Node
import groovy.xml.XmlParser
import java.nio.file.Files
import java.nio.file.Path

fun parseXml(path: Path): Node {
    return Files.newBufferedReader(path).use {
        val parser = XmlParser(false, true, true)
        parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        parser.parse(it)
    }
}