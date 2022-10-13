package pl.coopsoft.szambelan.database

import com.google.firebase.FirebaseApp
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import pl.coopsoft.szambelan.models.DataModel
import javax.inject.Inject

class DatabaseHelper @Inject constructor() {

    private companion object {
        private val DATABASE_URL =
            "https://${FirebaseApp.getInstance().options.projectId}-default-rtdb.europe-west1.firebasedatabase.app"
    }

    fun downloadData(done: (DataModel?) -> Unit) {
        val database = Firebase.database(DATABASE_URL).reference
        database.child("data").get()
            .addOnSuccessListener {
                done(it.getValue<DataModel>())
            }
            .addOnFailureListener {
                done(null)
            }
    }

    fun uploadData(data: DataModel, done: (Throwable?) -> Unit) {
        val database = Firebase.database(DATABASE_URL).reference
        database.child("data").setValue(data).addOnCompleteListener { task ->
            done(if (task.isSuccessful) null else RuntimeException("Cannot write database"))
        }
    }

}