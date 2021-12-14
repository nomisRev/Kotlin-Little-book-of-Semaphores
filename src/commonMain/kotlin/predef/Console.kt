package predef

interface Console {
  suspend fun putStr(msg: String): Unit

  companion object {
    val Default = object : Console {
      override suspend fun putStr(msg: String) = println(msg)
    }
  }
}
