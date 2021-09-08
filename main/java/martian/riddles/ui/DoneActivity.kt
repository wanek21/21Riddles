package martian.riddles.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import martian.riddles.R
import martian.riddles.util.Status

// активити появляется после прохождения всех уровней
@AndroidEntryPoint
class DoneActivity : AppCompatActivity() {

    private var tvPlace: TextView? = null
    private var tvFinalPhrase: TextView? = null
    private var tvEmail: TextView? = null
    private var btnSendReview: Button? = null
    private var emailGroup: Group? = null

    private val viewModel: DoneActivityViewModel by lazy {
        ViewModelProvider(this).get(DoneActivityViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.done_activity)

        tvPlace = findViewById(R.id.tvPlace)
        btnSendReview = findViewById(R.id.btnSendReview)
        tvFinalPhrase = findViewById(R.id.tvCongrat)
        tvEmail = findViewById(R.id.tvEmailFinish)
        emailGroup = findViewById(R.id.emailGroup)

        viewModel.place.observe(this, {
            if(it.status == Status.SUCCESS) {
                if(it.data == 1) // если место первое
                    showInfoForWinner()
                tvPlace?.text = it.data.toString()
                tvFinalPhrase?.text = it.data?.let { place -> getPhrase(place) }
            }
        })
        viewModel.emailContact.observe(this, {
            if(it.status == Status.SUCCESS) {
                tvEmail?.text = it.data.toString()
            }
        })
        /*if (place != 0) {
            tvPlace?.text = place.toString()
            if (place == 1) showInfoForWinner()
        } else AsyncLoadPlace().execute()*/

        btnSendReview?.setOnTouchListener { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleXBy(1f).scaleX(0.9f).scaleYBy(1f).scaleY(0.9f).setDuration(30)
                        .start()
                    v.animate().alphaBy(1.0f).alpha(0.9f).setDuration(80).start()
                }
                MotionEvent.ACTION_UP -> {
                    v.animate().scaleXBy(0.9f).scaleX(1f).scaleYBy(0.9f).scaleY(1f).setDuration(80)
                        .start()
                    v.animate().alphaBy(0.9f).alpha(1.0f).setDuration(80).start()
                    val appPackageName = packageName
                    try {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=$appPackageName")
                            )
                        )
                    } catch (anfe: ActivityNotFoundException) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                            )
                        )
                    }
                }
            }
            true
        }
        tvEmail?.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("", tvEmail?.text.toString())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this@DoneActivity, R.string.email_copy, Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    // TODO возможно это не надо
    /*override fun onBackPressed() {
        val pastLevel = intent.getIntExtra("past_level", 22)
        val intentMain = Intent()
        intentMain.putExtra("differ_level", Progress.getInstance().level - pastLevel)
        try {
            setResult(RESULT_OK, intentMain)
            finish()
        } catch (ex: NullPointerException) {
        }
        finish()
    }*/

    override fun onResume() {
        super.onResume()
        viewModel.getPlace()
    }

    private fun getPhrase(place: Int): String {
        return when (place) {
            2 -> resources.getString(R.string.place2_congratulations)
            3 -> resources.getString(R.string.place3_congratulations)
            4 -> resources.getString(R.string.place4_congratulations)
            5 -> resources.getString(R.string.place5_congratulations)
            6 -> resources.getString(R.string.place6_congratulations)
            7 -> resources.getString(R.string.place7_congratulations)
            8 -> resources.getString(R.string.place8_congratulations)
            9 -> resources.getString(R.string.place9_congratulations)
            10 -> resources.getString(R.string.place10_congratulations)
            11 -> resources.getString(R.string.place11_congratulations)
            12 -> resources.getString(R.string.place12_congratulations)
            13 -> resources.getString(R.string.place13_congratulations)
            14 -> resources.getString(R.string.place14_congratulations)
            else -> resources.getString(R.string.place_congratulations_default)
        }
    }

    private fun showInfoForWinner() {
        emailGroup?.visibility = View.VISIBLE
        viewModel.getEmailContact()
    }
}