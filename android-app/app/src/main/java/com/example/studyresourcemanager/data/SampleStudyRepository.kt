package com.example.studyresourcemanager.data

/** Temporary catalogue for building and reviewing the screens before backend integration. */
object SampleStudyRepository {
    val resources = listOf(
        book("algorithms", "Introduction to Algorithms", "Thomas H. Cormen", "Data Structures & Algorithms", "9780262046305",
            "Covers algorithm analysis, sorting, data structures, graph algorithms, and dynamic programming."),
        book("clean-code", "Clean Code", "Robert C. Martin", "Software Engineering", "9780132350884",
            "Uses code examples to explain naming, functions, error handling, and practices for keeping software readable."),
        book("database-systems", "Database System Concepts", "Abraham Silberschatz", "Database Systems", "9780078022159",
            "Introduces relational databases, SQL, schema design, transactions, and how database systems process queries."),
        book("deep-learning", "Deep Learning", "Ian Goodfellow", "Machine Learning", "9780262035613",
            "Builds from mathematical and machine learning foundations to neural networks and practical deep learning methods."),
        book("computer-networks", "Computer Networking: A Top-Down Approach", "James Kurose", "Computer Networks", "9780136681557",
            "Starts with network applications and works down through transport, routing, and link-layer protocols."),
        book("operating-systems", "Operating System Concepts", "Abraham Silberschatz", "Operating Systems", "9781119456339",
            "Explains processes, scheduling, concurrency, virtual memory, and file systems.")
    )

    private fun book(
        id: String,
        title: String,
        author: String,
        subject: String,
        isbn: String,
        description: String
    ) = StudyResource(id, title, author, subject, "Open Library", "BOOK", description, "https://openlibrary.org/isbn/$isbn")

    val subjects = listOf(
        "Data Structures & Algorithms",
        "Database Systems",
        "Machine Learning",
        "Computer Networks",
        "Software Engineering",
        "Operating Systems"
    )
}
