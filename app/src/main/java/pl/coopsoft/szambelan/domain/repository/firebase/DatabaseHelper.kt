package pl.coopsoft.szambelan.domain.repository.firebase

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.getValue
import pl.coopsoft.szambelan.domain.model.DataModel
import javax.inject.Inject

class DatabaseHelper @Inject constructor(private val database: DatabaseReference) {

    fun downloadData(done: (DataModel?) -> Unit) =
        database.child("data").get()
            .addOnSuccessListener {
                done(it.getValue<DataModel>())
            }
            .addOnFailureListener {
                done(null)
            }

    fun uploadData(data: DataModel, done: (Throwable?) -> Unit) =
        database.child("data").setValue(data)
            .addOnCompleteListener { task ->
                done(if (task.isSuccessful) null else RuntimeException("Cannot write database"))
            }

}