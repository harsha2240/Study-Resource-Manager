package com.example.studyresourcemanager.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The Compose search screen filters by exact subject match, so these rules decide whether
 * live API results are visible at all when a subject chip is selected.
 */
class SubjectMapperTest {

    @Test
    fun classifiesCommonCourseTopics() {
        assertEquals("Data Structures & Algorithms", SubjectMapper.classify("Introduction to Algorithms"))
        assertEquals("Database Systems", SubjectMapper.classify("Database normalization"))
        assertEquals("Machine Learning", SubjectMapper.classify("Deep Learning"))
        assertEquals("Computer Networks", SubjectMapper.classify("Computer Networks"))
        assertEquals("Software Engineering", SubjectMapper.classify("Clean Code"))
        assertEquals("Operating Systems", SubjectMapper.classify("Operating system"))
    }

    @Test
    fun prefersMachineLearningOverNetworkingForNeuralNetworks() {
        assertEquals("Machine Learning", SubjectMapper.classify("Neural network architectures", null))
    }

    @Test
    fun fallsBackToGeneralWhenNothingMatches() {
        assertEquals(SubjectMapper.GENERAL, SubjectMapper.classify("World poetry anthology"))
        assertEquals(SubjectMapper.GENERAL, SubjectMapper.classify())
        assertEquals(SubjectMapper.GENERAL, SubjectMapper.classify(null, "  "))
    }

    @Test
    fun keepsCanonicalSubjectListInSyncWithTheCatalogue() {
        assertEquals(
            listOf(
                "Data Structures & Algorithms",
                "Database Systems",
                "Machine Learning",
                "Computer Networks",
                "Software Engineering",
                "Operating Systems"
            ),
            SubjectMapper.canonicalSubjects
        )
    }

    @Test
    fun providesSearchTermsForSubjectChips() {
        assertEquals("database systems sql", SubjectMapper.searchTerm("Database Systems"))
        // Unknown subjects are passed through untouched instead of being dropped.
        assertEquals("Astronomy", SubjectMapper.searchTerm("Astronomy"))
    }
}
