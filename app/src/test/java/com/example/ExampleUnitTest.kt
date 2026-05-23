package com.example

import org.junit.Assert.*
import org.junit.Test
import java.net.URL

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun fetchSpreadsheetDB() {
    println("--- FETCHING DB SHEET ---")
    val url = "https://docs.google.com/spreadsheets/d/1cZ5IAB8MZcI8sZAO1xs_QzwFpzzeB_gAmj4duRJKwrY/export?format=csv&gid=0"
    val csv = URL(url).readText()
    println(csv)
    println("--- END DB SHEET ---")
  }

  @Test
  fun fetchSpreadsheetToken() {
    println("--- FETCHING TOKEN SHEET ---")
    val url = "https://docs.google.com/spreadsheets/d/1cZ5IAB8MZcI8sZAO1xs_QzwFpzzeB_gAmj4duRJKwrY/export?format=csv&gid=566010147"
    val csv = URL(url).readText()
    println(csv)
    println("--- END TOKEN SHEET ---")
  }
}
