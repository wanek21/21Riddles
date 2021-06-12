package martian.riddles.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.ALPHA
import android.view.View.OnFocusChangeListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import martian.riddles.R
import martian.riddles.domain.StatisticsController
import martian.riddles.util.Resource
import martian.riddles.util.Status

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private var tvInfo: TextView? = null
    private var imgAnimation: ImageView? = null
    private var etLogin: EditText? = null
    private var btnSignUp: Button? = null
    private var tvError: TextView? = null
    private var errorAnimatorShow = ObjectAnimator.ofFloat(tvError, ALPHA, 1.0f, 0.0f)
    private var errorAnimatorHide = ObjectAnimator.ofFloat(tvError, ALPHA, 0.0f, 1.0f)

    private val viewModel: SignUpViewModel by lazy {
        ViewModelProvider(this).get(SignUpViewModel::class.java)
    }

    private val registerStatusObserver = Observer<Resource<*>> {
        when (it.status) {
            Status.SUCCESS -> {
                btnSignUp?.isClickable = true
                Log.d("my","go to main activity")
                /*val mainActivityIntent = Intent(this, MainActivity::class.java)
                startActivity(mainActivityIntent)
                finish()*/
                Toast.makeText(this,"Okay",Toast.LENGTH_LONG).show()
            }
            Status.LOADING -> {
                btnSignUp?.isClickable = false
            }
            Status.ERROR -> {
                btnSignUp?.isClickable = true
                showError(it.message!!, 4000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logup_activity)
        tvInfo = findViewById(R.id.tvTopWord)
        imgAnimation = findViewById(R.id.imgLogupAnimation)
        etLogin = findViewById(R.id.etLogin)
        btnSignUp = findViewById(R.id.btnLogup)
        tvError = findViewById(R.id.tvInvalidName)
        btnSignUp?.setOnClickListener {
            if (etLogin?.text.toString() != "") {
                viewModel.signUp(etLogin?.text.toString())
                /*val logupTask = LogupTask()
                logupTask.execute(etLogin?.getText().toString())*/
            }
        }
        etLogin?.onFocusChangeListener = onFocusChangeListener

        viewModel.registerStatus.observe(this, registerStatusObserver)
    }

    private fun showError(errorString: String, duration: Int) {
        if (errorAnimatorHide.isStarted) {
            errorAnimatorShow.cancel()
            errorAnimatorHide.cancel()
            tvError!!.text = errorString
            errorAnimatorHide = ObjectAnimator.ofFloat(tvError, ALPHA, 1.0f, 0.0f)
            errorAnimatorShow = ObjectAnimator.ofFloat(tvError, ALPHA, 0.0f, 1.0f)
            errorAnimatorHide.duration = 800
            errorAnimatorHide.startDelay = duration.toLong()
            errorAnimatorShow.duration = 800
            errorAnimatorShow.start()
            errorAnimatorHide.start()
            return
        } else {
            tvError!!.text = errorString
            errorAnimatorHide = ObjectAnimator.ofFloat(tvError, ALPHA, 1.0f, 0.0f)
            errorAnimatorShow = ObjectAnimator.ofFloat(tvError, ALPHA, 0.0f, 1.0f)
            errorAnimatorHide.duration = 800
            errorAnimatorHide.startDelay = duration.toLong()
            errorAnimatorShow.duration = 800
            errorAnimatorShow.start()
            errorAnimatorHide.start()
            return
        }
    }

    private var onFocusChangeListener: OnFocusChangeListener = object : OnFocusChangeListener {
        private val wasShown = false
        override fun onFocusChange(v: View, hasFocus: Boolean) {
            if (hasFocus) {
                if (!wasShown) { // если warning еще не был показан
                    showError(getString(R.string.logup_warning), 7000)
                }
                val transitionDrawable = imgAnimation!!.drawable as TransitionDrawable
                transitionDrawable.startTransition(1200)
            }
        }
    }
}