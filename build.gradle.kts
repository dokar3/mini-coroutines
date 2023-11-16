subprojects {
    tasks.register("ktLineCounter") {
        var lineCount = 0
        var nonEmptyLineCount = 0
        for (ktFile in File(project.projectDir, "src/main").walk()) {
            if (ktFile.isDirectory || !ktFile.name.endsWith(".kt")) {
                continue
            }
            ktFile.forEachLine {
                lineCount++
                if (it.isNotBlank()) {
                    nonEmptyLineCount++
                }
            }
        }
        println(
            """
                ======= .kt Line Counter ======
                          Lines: $lineCount
                Non-empty lines: $nonEmptyLineCount
            """.trimIndent()
        )
    }
}