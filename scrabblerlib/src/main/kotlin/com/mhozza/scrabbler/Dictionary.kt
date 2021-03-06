package com.mhozza.scrabbler

import java.io.*
import java.util.*
import java.util.zip.GZIPInputStream

data class Dictionary(val dictionary: Map<String, Int>, val hasCounts: Boolean) {
    enum class Format {
        AUTO,
        TXT,
        CSV,
    }

    companion object {
        private const val WORD_COLUMN_NAME = "Word"
        private const val COUNT_COLUMN_NAME = "Count"
        private const val DELIMITER = ","

        fun load(fname: String, compressed: Boolean? = null, removeAccents: Boolean = false): Dictionary {
            return load(
                FileInputStream(File(fname)),
                compressed = compressed ?: fname.endsWith(".gz"),
                removeAccents = removeAccents,
            )
        }

        fun load(
            inputStream: InputStream,
            format: Format = Format.AUTO,
            compressed: Boolean = false,
            removeAccents: Boolean = false,
        ): Dictionary {
            @Suppress("NAME_SHADOWING")
            val inputStream =
                if (compressed) {
                    GZIPInputStream(inputStream)
                } else {
                    inputStream
                }

            @Suppress("NAME_SHADOWING")
            var format = format

            BufferedReader(
                InputStreamReader(inputStream)
            ).use {
                var firstLine = true
                var header: Map<String, Int>? = null
                val dictionary = HashMap<String, Int>()

                for (line in generateSequence { it.readLine() }) {
                    if (firstLine) {
                        firstLine = false
                        if (format == Format.AUTO || format == Format.CSV) {
                            if (format == Format.AUTO) {
                                format = detectFormat(line)
                            }
                            if (format == Format.CSV) {
                                header = getHeader(line)
                                check(isValidHeader(header))
                                // Read next line since the first one is a header.
                                continue
                            }
                        }
                    }
                    if (header == null) {
                        var word = line.trim().toLowerCase(Locale.getDefault())
                        if (removeAccents) {
                            word = foldToASCII(word)
                        }
                        dictionary[word] = 1
                    } else {
                        val lineParts = line.split(DELIMITER)
                        var word =
                            lineParts[header[WORD_COLUMN_NAME]!!].trim().toLowerCase(Locale.getDefault())
                        if (removeAccents) {
                            word = foldToASCII(word)
                        }
                        val count = lineParts[header[COUNT_COLUMN_NAME]!!].toInt()
                        dictionary[word] = (dictionary[word] ?: 0) + count
                    }
                }
                return Dictionary(dictionary, format == Format.CSV)
            }
        }

        private fun getHeader(line: String): Map<String, Int> {
            return line.split(DELIMITER).mapIndexed { index, element -> element to index }.toMap()
        }

        private fun detectFormat(line: String): Format {
            val header = getHeader(line)
            return if (isValidHeader(header)) {
                Format.CSV
            } else {
                Format.TXT
            }
        }

        private fun isValidHeader(header: Map<String, Int>): Boolean {
            return header.contains(WORD_COLUMN_NAME) && header.contains(COUNT_COLUMN_NAME)
        }
    }


}
