package com.mhozza.scrabbler

import java.io.File

fun loadDictionary(fname: String): List<String> {
    return File(fname).readLines().map { it.trim().toLowerCase() }
}
