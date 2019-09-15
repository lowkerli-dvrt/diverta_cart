package io.github.sonicarg.diverta_cart

import java.io.InputStream
import java.net.URL
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors.toList

/**
 *  Singleton object in charge of retrieving in-JAR files/resources to be used in the app
 */
object ResourceLoader {
    fun glob(startPath: String = ".", pattern: String = "*"): List<Any> {
        val matcher = FileSystems.getDefault().getPathMatcher(pattern)
        val path = Paths.get(startPath)
        return Files.walk(path)
            //Filter out anything that doesn't match the glob
            .filter { it : Path? -> it?.let { matcher.matches(it.fileName) } ?: false }
            //Collect to a list
            .collect(toList())
    }

    /**
     * Retrieves a binary content from inside the JAR
     * @param resource: Path to the resource being loaded, relative to the root of JAR file
     * @return A URL pointing to the requested resource
     */
    fun getResource(resource: String): URL? =
        Thread.currentThread().contextClassLoader.getResource(resource)
            ?: resource::class.java.getResource(resource)

    /**
     * Retrieves a binary content from inside the JAR, but inside a Stream
     * @param resource: Path to the resource being loaded, relative to the root of JAR file
     * @return A Stream of the requested resource
     */
    fun getResourceAsStream(resource: String): InputStream? =
        Thread.currentThread().contextClassLoader.getResourceAsStream(resource)
            ?: resource::class.java.getResourceAsStream(resource)
}
