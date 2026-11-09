package com.example.studyresourcemanager.data

/** Temporary catalogue for building and reviewing the screens before backend integration. */
object SampleStudyRepository {
    val resources = listOf(
        StudyResource("algorithms", "Introduction to Algorithms", "Thomas H. Cormen", "Data Structures & Algorithms", "Open Library", "BOOK"),
        StudyResource("clean-code", "Clean Code", "Robert C. Martin", "Software Engineering", "Open Library", "BOOK"),
        StudyResource("database-systems", "Database System Concepts", "Abraham Silberschatz", "Database Systems", "Open Library", "BOOK"),
        StudyResource("deep-learning", "Deep Learning", "Ian Goodfellow", "Machine Learning", "Open Library", "BOOK"),
        StudyResource("computer-networks", "Computer Networking: A Top-Down Approach", "James Kurose", "Computer Networks", "Open Library", "BOOK"),
        StudyResource("operating-systems", "Operating System Concepts", "Abraham Silberschatz", "Operating Systems", "Open Library", "BOOK")
    )

    val subjects = listOf(
        "Data Structures & Algorithms",
        "Database Systems",
        "Machine Learning",
        "Computer Networks",
        "Software Engineering",
        "Operating Systems"
    )
}
