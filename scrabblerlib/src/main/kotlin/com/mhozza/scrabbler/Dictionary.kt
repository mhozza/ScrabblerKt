package com.mhozza.scrabbler

import java.io.*
import java.util.*

data class Dictionary(val dictionary: Map<String, Int>) {
    enum class Format {
        AUTO,
        TXT,
        CSV,
    }

    companion object {
        private const val WORD_COLUMN_NAME = "Word"
        private const val COUNT_COLUMN_NAME = "Count"
        private const val DELIMITER = ","

        fun load(fname: String): Dictionary {
            return load(FileInputStream(File(fname)))
        }

        fun load(inputStream: InputStream, format: Format = Format.AUTO): Dictionary {
            @Suppress("NAME_SHADOWING") var format = format
            BufferedReader(
                InputStreamReader(inputStream)
            ).use {
                var firstLine = true
                var header: Map<String, Int>? = null

                val dictionary = generateSequence {
                    var line = it.readLine()
                    if (firstLine && line != null) {
                        firstLine = false
                        if (format == Format.AUTO || format == Format.CSV) {
                            if (format == Format.AUTO) {
                                format = detectFormat(line)
                            }
                            if (format == Format.CSV) {
                                header = getHeader(line)
                                check(isValidHeader(header!!))
                                // Read next line since the first one is a header.
                                line = it.readLine()
                            }
                        }

                    }
                    if (line == null) {
                        null
                    } else {
                        if (header == null) {
                            val word = line.trim().toLowerCase(Locale.getDefault())
                            val count = 1
                            word to count
                        } else {
                            val lineParts = line.split(DELIMITER)
                            val word =
                                lineParts[header!![WORD_COLUMN_NAME]!!].trim().toLowerCase(Locale.getDefault())
                            val count = lineParts[header!![COUNT_COLUMN_NAME]!!].toInt()
                            word to count
                        }
                    }
                }.toMap()
                return Dictionary(dictionary)
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
