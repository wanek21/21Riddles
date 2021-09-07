package martian.riddles.ui

import android.animation.ObjectAnimator
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import martian.riddles.R
import martian.riddles.ui.AssistentDialog
import martian.riddles.util.Status
import martian.riddles.util.UpdateType
import java.util.*

@AndroidEntryPoint
class InfoActivity : AppCompatActivity() {

    private var btnTelegram: Button? = null
    private var tvInfoUpdate: TextView? = null
    private var btnUpdate: Button? = null
    private var btnReview: Button? = null

    private val viewModel: InfoActivityViewModel by lazy {
        ViewModelProvider(this).get(InfoActivityViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)
        btnTelegram = findViewById(R.id.btnTelegram)
        tvInfoUpdate = findViewById(R.id.tvInfoUpdate)
        btnUpdate = findViewById(R.id.btnUpdateApp)
        btnReview = findViewById(R.id.btnReview)

        viewModel.appVersionStatus.observe(this, {
            if(it.status == Status.SUCCESS) {
                when(it.data) {
                    UpdateType.CURRENT -> { // версия актуальна
                        tvInfoUpdate?.text = getString(R.string.current_version)
                        btnUpdate?.isClickable = false
                        val tvInfo = ObjectAnimator.ofFloat(tvInfoUpdate, View.ALPHA, 0f, 1f)
                        tvInfo.duration = 800
                        tvInfo.start()
                    }
                    UpdateType.SOFT_UPDATE, UpdateType.FORCE_UPDATE -> { // версия не актуальна
                        tvInfoUpdate?.setText(R.string.older_version)
                        tvInfoUpdate?.setTextColor(resources.getColor(R.color.warning))
                        btnUpdate?.alpha = 1f
                        btnUpdate?.isClickable = true
                        val tvInfo = ObjectAnimator.ofFloat(tvInfoUpdate, View.ALPHA, 0f, 1f)
                        tvInfo.duration = 800
                        tvInfo.start()
                    }
                }
            } else Toast.makeText(this, getString(R.string.download_info_error), Toast.LENGTH_LONG).show()
        })

        btnReview?.setOnClickListener {
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
        btnUpdate?.setOnClickListener {
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
        if (btnTelegram != null) { // если кнопка телеграма есть в этой локализации
            btnTelegram?.setOnClickListener {
                try {
                    viewModel.joinGroup()
                    var uri = "tg://resolve?domain=twenty_one_riddles"
                    if (Locale.getDefault().language == "ru") uri =
                        "tg://resolve?domain=twenty_one_riddles_ru"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    startActivity(intent)
                } catch (ex: ActivityNotFoundException) {
                    val assistentDialog = AssistentDialog(AssistentDialog.DIALOG_NO_TELEGRAM)
                    assistentDialog.show(supportFragmentManager, null)
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("", "twenty_one_riddles")
                    clipboard.setPrimaryClip(clip)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkAppVersion()
    }
}