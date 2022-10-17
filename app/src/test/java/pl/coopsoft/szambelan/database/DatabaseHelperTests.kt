package pl.coopsoft.szambelan.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.common.truth.Truth.assertThat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.junit.runner.RunWith
import pl.coopsoft.szambelan.models.DataModel

@RunWith(AndroidJUnit4::class)
class DatabaseHelperTests {

    @Test
    fun testDownloadDataSuccess() {
        testDownloadData(true)
    }

    @Test
    fun testDownloadDataFailure() {
        testDownloadData(false)
    }

    private fun testDownloadData(testSuccess: Boolean) {
        val onSuccessListeners = mutableListOf<OnSuccessListener<DataSnapshot>>()
        val onFailureListeners = mutableListOf<OnFailureListener>()
        val done = mockk<(DataModel?) -> Unit> {
            justRun { this@mockk.invoke(any<DataModel>()) }
            justRun { this@mockk.invoke(null) }
        }
        val task = mockk<Task<DataSnapshot>> {
            every { addOnSuccessListener(capture(onSuccessListeners)) } returns this
            every { addOnFailureListener(capture(onFailureListeners)) } returns this
        }
        val databaseChild = mockk<DatabaseReference> {
            every { get() } returns task
        }
        val database = mockk<DatabaseReference> {
            every { child(any()) } returns databaseChild
        }
        val databaseHelper = DatabaseHelper(database)

        assertThat(databaseHelper.downloadData(done)).isSameInstanceAs(task)

        verify { databaseChild.get() }
        verify { task.addOnSuccessListener(any()) }
        verify { task.addOnFailureListener(any()) }
        if (testSuccess) {
            val dataSnapshot = mockk<DataSnapshot>()
            val data = mockk<DataModel>()
            every { dataSnapshot.getValue(any<GenericTypeIndicator<DataModel>>()) } returns data
            onSuccessListeners.first().onSuccess(dataSnapshot)
            verify { done(data) }
        } else {
            val exception = mockk<Exception>()
            onFailureListeners.first().onFailure(exception)
            verify { done(null) }
        }
    }

    @Test
    fun testUploadDataSuccess() {
        testUploadData(true)
    }

    @Test
    fun testUploadDataFailure() {
        testUploadData(false)
    }

    private fun testUploadData(testSuccess: Boolean) {
        val onCompleteListeners = mutableListOf<OnCompleteListener<Void>>()
        val done = mockk<(Throwable?) -> Unit> {
            justRun { this@mockk.invoke(any<Exception>()) }
            justRun { this@mockk.invoke(null) }
        }
        val task = mockk<Task<Void>> {
            every { addOnCompleteListener(capture(onCompleteListeners)) } returns this
            every { isSuccessful } returns testSuccess
        }
        val data = mockk<DataModel>()
        val databaseChild = mockk<DatabaseReference> {
            every { setValue(any()) } returns task
        }
        val database = mockk<DatabaseReference> {
            every { child(any()) } returns databaseChild
        }
        val databaseHelper = DatabaseHelper(database)

        assertThat(databaseHelper.uploadData(data, done)).isSameInstanceAs(task)

        onCompleteListeners.first().onComplete(task)
        if (testSuccess) {
            verify { done(null) }
        } else {
            verify { done(any<Exception>()) }
        }
    }
}