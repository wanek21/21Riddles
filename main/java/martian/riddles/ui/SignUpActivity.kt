package martian.riddles.ui

import android.animation.ObjectAnimator
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.View
import android.view.View.ALPHA
import android.view.View.OnFocusChangeListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import martian.riddles.R
import martian.riddles.util.Resource
import martian.riddles.util.Status
import martian.riddles.util.log

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    private var tvInfo: TextView? = null
    private var imgAnimation: ImageView? = null
    private var etLogin: EditText? = null
    private var btnSignUp: Button? = null
    private var tvError: TextView? = null
    private var errorAnimatorShow = ObjectAnimator.ofFloat(tvError, ALPHA, 1.0f, 0.0f)
    private var errorAnimatorHide = ObjectAnimator.ofFloat(tvError, ALPHA, 0.0f, 1.0f)

    private val errorShowingTime = 4000 // в миллисекундах

    private val viewModel: SignUpViewModel by lazy {
        ViewModelProvider(this).get(SignUpViewModel::class.java)
    }

    private val registerStatusObserver = Observer<Resource<*>> {
        when (it.status) {
            Status.SUCCESS -> {
                btnSignUp?.isClickable = true
                log("go to main activity")
                /*val mainActivityIntent = Intent(this, MainActivity::class.java)
                startActivity(mainActivityIntent)
                finish()*/
            }
            Status.LOADING -> {
                btnSignUp?.isClickable = false
            }
            Status.ERROR -> {
                btnSignUp?.isClickable = true
                showError(it.message!!, errorShowingTime)
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
            }
        }
        etLogin?.onFocusChangeListener = onFocusChangeListener

        viewModel.registerStatus.observe(this, registerStatusObserver)
    }

    // высвечивает ошибки на определенное время
    private fun showError(errorStringId: Int, duration: Int) {
        if (errorAnimatorHide.isStarted) { // если прошлая ошибка еще не исчезла, то отменяем прошлую анимацию
            errorAnimatorShow.cancel()
            errorAnimatorHide.cancel()
        }
        tvError?.text = getString(errorStringId)
        errorAnimatorHide = ObjectAnimator.ofFloat(tvError, ALPHA, 1.0f, 0.0f)
        errorAnimatorShow = ObjectAnimator.ofFloat(tvError, ALPHA, 0.0f, 1.0f)
        errorAnimatorHide.duration = 800
        errorAnimatorHide.startDelay = duration.toLong()
        errorAnimatorShow.duration = 800
        errorAnimatorShow.start()
        errorAnimatorHide.start()
    }

    private var onFocusChangeListener: OnFocusChangeListener = object : OnFocusChangeListener {
        private val wasShown = false
        override fun onFocusChange(v: View, hasFocus: Boolean) {
            if (hasFocus) {
                if (!wasShown) { // если warning еще не был показан
                    showError(R.string.logup_warning, 7000)
                }
                val transitionDrawable = imgAnimation!!.drawable as TransitionDrawable
                transitionDrawable.startTransition(1200)
            }
        }
    }
}