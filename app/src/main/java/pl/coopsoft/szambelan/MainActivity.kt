package pl.coopsoft.szambelan

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import pl.coopsoft.szambelan.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), TextWatcher {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.prevMainEditText.addTextChangedListener(this)
        binding.prevGardenEditText.addTextChangedListener(this)
        binding.currentMainEditText.addTextChangedListener(this)
        binding.currentGardenEditText.addTextChangedListener(this)

        binding.emptyTankButton.setOnClickListener { emptyTankClicked() }
    }

    override fun onDestroy() {
        viewModel.saveEditValues()
        super.onDestroy()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
        viewModel.refreshCalculation()
    }

    private fun emptyTankClicked() {
        AlertDialog.Builder(this)
            .setTitle(R.string.empty_tank)
            .setMessage(R.string.empty_tank_question)
            .setCancelable(true)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                dialog.dismiss()
                viewModel.emptyTank()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setOnCancelListener { dialog ->
                dialog.dismiss()
            }
            .show()
    }
}