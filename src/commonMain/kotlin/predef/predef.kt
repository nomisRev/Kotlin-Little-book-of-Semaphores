package predef

suspend fun <A> A.debug(): A = also(::println)
