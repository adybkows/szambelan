package pl.coopsoft.szambelan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val prevEmptyActions = MutableLiveData<String>()
    val prevMainMeter = MutableLiveData<String>()
    val prevGardenMeter = MutableLiveData<String>()
    val currentMainMeter = MutableLiveData<String>()
    val currentGardenMeter = MutableLiveData<String>()
    val waterUsage = MutableLiveData<CharSequence>()

    init {
        val context = getApplication<Application>()
        prevMainMeter.value =
            Utils.toString(Persistence.getDouble(context, Persistence.PREF_OLD_MAIN))
        prevGardenMeter.value =
            Utils.toString(Persistence.getDouble(context, Persistence.PREF_OLD_GARDEN))
        currentMainMeter.value =
            Utils.toString(Persistence.getDouble(context, Persistence.PREF_CURRENT_MAIN))
        currentGardenMeter.value =
            Utils.toString(Persistence.getDouble(context, Persistence.PREF_CURRENT_GARDEN))
    }
}