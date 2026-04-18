package com.therapycompanion.`import`

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class CsvImporterTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun csvUri(content: String): Uri {
        val file = File.createTempFile("import_test", ".csv", context.cacheDir)
        file.writeText(content)
        return Uri.fromFile(file)
    }

    @Test
    fun novelBodySystemValuePassesValidation() {
        val csv = """
            name,body_system,instructions,duration,frequency,days,priority
            Balance Board,Vestibular System,Stand on one leg with eyes closed for 30 seconds,5,Daily,Daily,2
        """.trimIndent()

        val result = CsvImporter.parse(context, csvUri(csv))

        assertTrue("Expected success but got: $result", result is ImportResult.Success)
        val exercises = (result as ImportResult.Success).exercises
        assertEquals(1, exercises.size)
        assertEquals("Vestibular System", exercises.first().bodySystem)
    }

    @Test
    fun multiWordBodySystemPassesValidation() {
        val csv = """
            name,body_system,instructions,duration,frequency,days,priority
            Hip Flexor Stretch,Lower Extremity Hip Flexors,Lunge stretch holding 30 seconds each side,5,Daily,Mon,1
        """.trimIndent()

        val result = CsvImporter.parse(context, csvUri(csv))

        assertTrue("Expected success but got: $result", result is ImportResult.Success)
    }

    @Test
    fun emptyBodySystemFailsValidation() {
        val csv = """
            name,body_system,instructions,duration,frequency,days,priority
            Balance Board,,Stand on one leg,5,Daily,Daily,2
        """.trimIndent()

        val result = CsvImporter.parse(context, csvUri(csv))

        assertTrue("Expected validation errors but got: $result", result is ImportResult.ValidationErrors)
        val errors = (result as ImportResult.ValidationErrors).errors
        assertTrue(errors.any { it.column == "body_system" })
    }

    @Test
    fun bodySystemOver100CharsFailsValidation() {
        val longValue = "A".repeat(101)
        val csv = """
            name,body_system,instructions,duration,frequency,days,priority
            Test Exercise,$longValue,Do the thing,5,Daily,Daily,2
        """.trimIndent()

        val result = CsvImporter.parse(context, csvUri(csv))

        assertTrue("Expected validation errors but got: $result", result is ImportResult.ValidationErrors)
    }
}
