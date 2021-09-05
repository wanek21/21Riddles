package martian.riddles.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Point
import android.graphics.drawable.Animatable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.DisplayMetrics
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.lifecycle.ViewModelProvider
import com.android.billingclient.api.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import martian.riddles.R
import martian.riddles.domain.*
import martian.riddles.dto.Player
import martian.riddles.util.GetContextClass
import java.util.*
import java.util.concurrent.TimeUnit

// активити, где отображаются загадки
class RiddlesActivity : AppCompatActivity() {

    var rewardedAd: RewardedAd? = null
    private var tvQuestion: TextView? = null
    private var tvTopLvl: TextView? = null
    private var tvBottomLvl: TextView? = null
    private var etAnswer: EditText? = null
    private var btnNext: Button? = null
    private var btnCheckAnswer: Button? = null
    private var progressAdLoad: ProgressBar? = null
    private var answerStateImg: ImageView? = null
    private var imgGreenMark: ImageView? = null
    private var imgBackToMain: ImageView? = null
    private var mlMain: MotionLayout? = null
    private var mlLevel: MotionLayout? = null
    private var mlBottom: MotionLayout? = null
    private var imgShowBuy: ImageView? = null
    private var btnBuy: Button? = null
    //private val riddlesController = RiddlesController()
    //private var statisticsController: StatisticsController? = null
    private var handler: Handler? = null
    private var answerInputAnimation: Handler? = null
    private var animationController: AnimationController? = null
    private var attemptsController: AttemptsController? = null
    private var purchaseController: PurchaseController? = null
    var fullScreenContentCallback: FullScreenContentCallback? = null
    private val ALPHA_DOWN = 1
    private val ALPHA_UP = 2
    private val LOAD_AD = 3
    private val SET_RED_ANSWER = 4
    private val ALPHA_DOWN_BTNNEXT = 7
    private val SET_INVISIBLE_BTNNEXT = 8
    private val CHANGE_ANSWER = 9
    private val TRANSITION_RESET = 13
    private val SHOW_PROGRESS = 14
    private val HIDE_PROGRESS = 15
    private val CHANGE_HINT_ANSWER = 17
    private val SHOW_PARTING_WORD = 18
    private val SHOW_PURCHASE = 19
    private val HIDE_PURCHASE = 20
    private val FOCUS_FIXED = 21
    private val RIGHT_ANSWER = 22
    private val WRONG_ANSWER = 23
    private var adBlockId: String? = null
    private var adShowed = false // если реклама показалась, то можно показывать предложение о покупке

    private val viewModel: RiddlesActivityViewModel by lazy {
        ViewModelProvider(this).get(RiddlesActivityViewModel::class.java)
    }

    @SuppressLint("HandlerLeak")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)
        adBlockId = this.resources.getString(R.string.ad_block)
        answerInputAnimation = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    FOCUS_FIXED -> {
                        answerStateImg!!.setImageDrawable(getDrawable(R.drawable.norm_to_right))
                        animationController!!.INPUT_STATE = 0
                    }
                    RIGHT_ANSWER -> {
                        animationController?.editTextRightAnswer()
                        animationController?.animationBtnNext()
                        animationController?.markAnimate()
                        btnCheckAnswer?.isClickable = false
                    }
                    WRONG_ANSWER -> {
                        animationController!!.editTextWrongAnswer()
                        etAnswer!!.setText("")
                    }
                    SET_RED_ANSWER -> {
                        answerStateImg!!.setImageResource(R.drawable.bottom_img_wrong)
                        animationController!!.INPUT_STATE = 2
                    }
                    TRANSITION_RESET -> {
                        animationController!!.transitionInputReset()
                    }
                }
            }
        }
        handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    ALPHA_DOWN -> tvQuestion!!.animate().alpha(0f).duration = 1000
                    ALPHA_UP -> tvQuestion!!.animate().alpha(1f).duration = 1000
                    LOAD_AD -> {
                        loadAd()
                    }
                    CHANGE_ANSWER -> {
                        //tvQuestion!!.text = riddlesController.riddle
                    }
                    CHANGE_HINT_ANSWER -> {
                        if (!attemptsController!!.isEndlessAttempts) {
                            /*val countAttempts = attemptsController!!.countAttempts
                            if (countAttempts > 0 && countAttempts <= 3) {
                                etAnswer!!.hint =
                                    resources.getString(R.string.attempts) + " " + countAttempts
                                btnCheckAnswer!!.maxLines = 1
                                btnCheckAnswer!!.setText(R.string.check_answer)
                            } else if (countAttempts == 0) {
                                etAnswer!!.hint =
                                    resources.getString(R.string.attempts) + " " + countAttempts
                                btnCheckAnswer!!.maxLines = 2
                                btnCheckAnswer!!.setText(R.string.look_ad)
                            }*/
                        } else {
                            etAnswer!!.hint = ""
                        }
                    }
                    ALPHA_DOWN_BTNNEXT -> {
                        btnNext!!.animate().alpha(0f).duration = 400
                    }
                    SET_INVISIBLE_BTNNEXT -> {
                        btnNext!!.visibility = View.INVISIBLE
                    }
                    SHOW_PROGRESS -> {
                        animationController!!.showProgressBarAd()
                    }
                    HIDE_PROGRESS -> {
                        animationController!!.hideProgressBarAd()
                    }
                    SHOW_PURCHASE -> {
                        animationController!!.showPurchase()
                    }
                    HIDE_PURCHASE -> {
                        animationController!!.hidePurchase()
                    }
                    SHOW_PARTING_WORD -> {
                    }
                }
            }
        }
        tvQuestion = findViewById(R.id.tvQuestion)
        tvTopLvl = findViewById(R.id.tvTop)
        tvBottomLvl = findViewById(R.id.tvBottom)
        imgGreenMark = findViewById(R.id.imgGreenMark)
        answerStateImg = findViewById(R.id.imgAnswerAnimation)
        etAnswer = findViewById(R.id.etAnswer)
        btnNext = findViewById(R.id.btnNextQuestion)
        progressAdLoad = findViewById(R.id.progressLoadAd)
        btnCheckAnswer = findViewById(R.id.btnCheckAnswer)
        imgBackToMain = findViewById(R.id.imgBackToMain)
        mlMain = findViewById(R.id.mlMain)
        mlLevel = findViewById(R.id.mlLevel)
        mlBottom = findViewById(R.id.mlCheckAndNext)
        imgShowBuy = findViewById(R.id.imgShowBuy)
        btnBuy = findViewById(R.id.btnBuy)
        btnBuy?.setOnClickListener { purchaseController!!.buy() }
        btnNext?.setOnClickListener(onClickListener)
        btnCheckAnswer?.setOnClickListener(onClickListener)
        imgBackToMain?.setOnClickListener(onClickListener)
        etAnswer?.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                animationController!!.focusEditText()
            }
        }
        //statisticsController = StatisticsController(this)
        //purchaseController = PurchaseController(this)
        animationController = AnimationController()
        //attemptsController = AttemptsController(statisticsController)

        // если юзер разгадал все, но не проверил является ли он победителем
        /*if (!StoredData.getDataBool(StoredData.DATA_WINNER_IS_CHECKED) && Progress.getInstance().level < 22) {
            tvQuestion?.text = riddlesController.riddle
            tvTopLvl?.text = (Progress.getInstance().level + 1).toString()
            tvBottomLvl?.text = Progress.getInstance().level.toString()
        }*/
        setInputMode() // если экран маленький, то макет поднимается при фокусе клавиатуры
        fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                // Code to be invoked when the ad showed full screen content.
            }

            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadAd()
                // Code to be invoked when the ad dismissed full screen content.
            }
        }
        loadAd()
    }

    private fun loadAd() {
        RewardedAd.load(
            this,
            adBlockId!!,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    super.onAdLoaded(rewardedAd!!)
                    rewardedAd = ad
                    rewardedAd!!.fullScreenContentCallback = fullScreenContentCallback
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    //statisticsController!!.sendErrorAd(loadAdError.code)
                    when (loadAdError.code) {
                        0 -> Toast.makeText(
                            this@RiddlesActivity,
                            getString(R.string.ad_error_0),
                            Toast.LENGTH_LONG
                        ).show()
                        else -> Toast.makeText(
                            GetContextClass.getContext(),
                            R.string.error_download_ad,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }

    // показать рекламу, чтобы добавить попытку
    val attemptByAd: Unit
        get() {
            if (rewardedAd != null) {
                rewardedAd!!.show(
                    this
                ) {
                    Toast.makeText(
                        this@RiddlesActivity,
                        R.string.attempt_is_added,
                        Toast.LENGTH_SHORT
                    ).show()
                    attemptsController!!.upCountAttempts()
                    adShowed = true
                }
            } else {
                Toast.makeText(
                    this@RiddlesActivity,
                    getText(R.string.ad_not_ready_yet), Toast.LENGTH_SHORT
                ).show()
            }
        }
    private val widthScreen: Int
        private get() {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            return size.x
        }

    private fun setInputMode() {
        val widthScreen = convertPixelsToDp(widthScreen.toFloat())
            .toInt()
        if (widthScreen < 360) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    public override fun onResume() {
        super.onResume()
        //animationController?.setAttemptsOnScreen()
        viewModel.getCurrentRiddle()
        if (Player.getInstance().level > 9) {
            //LoadRiddle().execute()
        }
    }

    public override fun onPause() {
        super.onPause()
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        val pastLevel = intent.getIntExtra("past_level", 1)
        val intentMain = Intent()
        intentMain.putExtra("differ_level", Progress.getInstance().level - pastLevel)
        try {
            setResult(RESULT_OK, intentMain)
            finish()
        } catch (ex: NullPointerException) {
        }
        finish()
    }

    private fun changeQuestion() {
        // анимация вопроса
        Thread {
            handler!!.sendEmptyMessage(ALPHA_DOWN)
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            handler!!.sendEmptyMessage(CHANGE_ANSWER)
            answerInputAnimation!!.sendEmptyMessage(TRANSITION_RESET) // сбрасываем transition, чтобы запустить потом снова
            handler!!.sendEmptyMessage(ALPHA_UP)
        }.start()
        animationController!!.changeQuestion()
        animationController!!.changeLevelTop()
        btnCheckAnswer!!.isClickable = true
        etAnswer!!.setText("")
    }

    // внутренние контроллеры и потоки -----------------------------------------------------------------------------

    private inner class AnimationController {
        /*переменная хранит состояние поля ввода ответа
        0 - обычное
        1 - зеленое
        2 - красное
        */
        var INPUT_STATE = 0
        private var isFirstLaunch = true
        var currentStateProgressBarAd = 0
        private val FOCUSING_ANSWER_INPUT_TIME = 1000
        private var answerTransitions: TransitionDrawable? = null

        fun setAttemptsOnScreen() {
            /*val countAttempts = attemptsController!!.countAttempts
            if (attemptsController!!.isEndlessAttempts) {
                btnCheckAnswer!!.maxLines = 1
                btnCheckAnswer!!.setText(R.string.check_answer)
                imgShowBuy!!.isClickable = false
                imgShowBuy!!.alpha = 0f
                etAnswer!!.hint = ""
            } else if (countAttempts == 0) {
                btnCheckAnswer!!.maxLines = 2
                btnCheckAnswer!!.setText(R.string.look_ad)
                etAnswer!!.hint = resources.getString(R.string.attempts) + " " + countAttempts
            } else if (countAttempts <= 3) {
                btnCheckAnswer!!.maxLines = 1
                btnCheckAnswer!!.setText(R.string.check_answer)
                etAnswer!!.hint = resources.getString(R.string.attempts) + " " + countAttempts
            }*/
        }

        fun focusEditText() {
            val transitionDrawable = answerStateImg!!.drawable as TransitionDrawable
            transitionDrawable.startTransition(FOCUSING_ANSWER_INPUT_TIME)
            Thread { // поток для изменения цвета обводки ответа на неправильный
                try {
                    TimeUnit.MILLISECONDS.sleep(FOCUSING_ANSWER_INPUT_TIME.toLong())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                if (INPUT_STATE != 1) {
                    answerInputAnimation!!.sendEmptyMessage(FOCUS_FIXED)
                }
            }.start()
        }

        fun changeQuestion() {
            answerTransitions!!.reverseTransition(500)
            mlMain!!.transitionToStart()
        }

        fun markAnimate() {
            mlMain!!.transitionToEnd()
            val drawable = imgGreenMark!!.drawable
            if (drawable is Animatable) {
                (drawable as Animatable).start()
            }
        }

        fun showPurchase() {
            mlBottom!!.transitionToEnd()
        }

        fun hidePurchase() {
            mlBottom!!.transitionToStart()
        }

        fun changeLevelTop() {
            mlLevel!!.setTransitionListener(object : MotionLayout.TransitionListener {
                override fun onTransitionStarted(motionLayout: MotionLayout, i: Int, i1: Int) {}
                override fun onTransitionChange(
                    motionLayout: MotionLayout,
                    i: Int,
                    i1: Int,
                    v: Float
                ) {
                }

                override fun onTransitionCompleted(motionLayout: MotionLayout, i: Int) {
                    if (i == R.id.end) {
                        tvBottomLvl!!.text = Progress.getInstance().level.toString()
                        motionLayout.progress = 0f
                        motionLayout.setTransition(R.id.start, R.id.end)
                    }
                }

                override fun onTransitionTrigger(
                    motionLayout: MotionLayout,
                    i: Int,
                    b: Boolean,
                    v: Float
                ) {
                }
            })
            tvTopLvl!!.text = Progress.getInstance().level.toString()
            mlLevel!!.transitionToEnd()
        }

        fun animationBtnNext(appear: Boolean) { // анимация появлеия кнопки "дальше"
            val animatorBtnNextX: ObjectAnimator
            val animatorBtnNextY: ObjectAnimator
            if (appear) {
                btnNext!!.visibility = View.VISIBLE
                btnNext!!.isClickable = true
                btnNext!!.alpha = 1.0f
                animatorBtnNextX = ObjectAnimator.ofFloat(btnNext, View.SCALE_X, 1.0f, 1.1f, 1.0f)
                animatorBtnNextY = ObjectAnimator.ofFloat(btnNext, View.SCALE_Y, 1.0f, 1.1f, 1.0f)
                animatorBtnNextX.duration = 300
                animatorBtnNextY.duration = 300
                animatorBtnNextX.start()
            } else {
                btnNext!!.isClickable = false
                val btnNextAnimator = ObjectAnimator.ofFloat(btnNext, View.ALPHA, 1.0f, 0.0f)
                btnNextAnimator.duration = 400
                btnNextAnimator.start()
            }
        }

        fun animationBtnNext() { // анимация появления кнопки "дальше"
            val animatorBtnNextX: ObjectAnimator
            val animatorBtnNextY: ObjectAnimator
            if (Progress.getInstance().level <= 21) {
                btnNext!!.visibility = View.VISIBLE
                btnNext!!.isClickable = true
                btnNext!!.alpha = 1.0f
                animatorBtnNextX = ObjectAnimator.ofFloat(btnNext, View.SCALE_X, 1.0f, 1.1f, 1.0f)
                animatorBtnNextY = ObjectAnimator.ofFloat(btnNext, View.SCALE_Y, 1.0f, 1.1f, 1.0f)
                animatorBtnNextX.duration = 300
                animatorBtnNextY.duration = 300
                animatorBtnNextX.start()
            } else {
                btnNext!!.isClickable = false
                val btnNextAnimator = ObjectAnimator.ofFloat(btnNext, View.ALPHA, 1.0f, 0.0f)
                if (!isFirstLaunch) {
                    btnNextAnimator.duration = 400
                } else {
                    isFirstLaunch = false
                    btnNext!!.visibility = View.INVISIBLE
                    btnNextAnimator.duration = 0
                }
                btnNextAnimator.start()
            }
        }

        fun transitionInputReset() {
            answerTransitions!!.resetTransition()
        }

        fun editTextRightAnswer() {
            if (INPUT_STATE == 2) { // если сейчас красный ободок, то заменяем на другой
                answerStateImg!!.setImageDrawable(getDrawable(R.drawable.norm_to_right))
            }
            answerTransitions = answerStateImg!!.drawable as TransitionDrawable
            answerTransitions!!.isCrossFadeEnabled = false
            answerTransitions!!.startTransition(280)
            INPUT_STATE = 1
        }

        fun editTextWrongAnswer() {
            Thread {
                // поток для изменения цвета обводки ответа на неправильный
                answerInputAnimation!!.sendEmptyMessage(SET_RED_ANSWER)
                try {
                    TimeUnit.SECONDS.sleep(3)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                if (INPUT_STATE != 1) {
                    answerInputAnimation!!.sendEmptyMessage(FOCUS_FIXED) // возвращаемся к обычному состоянию
                }
            }.start()
        }

        fun showProgressBarAd() {
            progressAdLoad!!.visibility = View.VISIBLE
        }

        fun hideProgressBarAd() {
            progressAdLoad!!.visibility = View.INVISIBLE
        }

        init {
            animationBtnNext(false) // делаем кнопку "дальше" невидимой при старте
            mlBottom!!.setTransitionListener(object : MotionLayout.TransitionListener {
                override fun onTransitionStarted(motionLayout: MotionLayout, i: Int, i1: Int) {}
                override fun onTransitionChange(
                    motionLayout: MotionLayout,
                    i: Int,
                    i1: Int,
                    v: Float
                ) {
                }

                override fun onTransitionCompleted(motionLayout: MotionLayout, i: Int) {
                    if (i == R.id.end) {
                        imgShowBuy!!.animate().rotation(180f)
                    } else if (i == R.id.start) {
                        imgShowBuy!!.animate().rotation(0f)
                    }
                }

                override fun onTransitionTrigger(
                    motionLayout: MotionLayout,
                    i: Int,
                    b: Boolean,
                    v: Float
                ) {
                }
            })
        }
    }

    private val onClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.btnNextQuestion -> {
                if (Progress.getInstance().level < 22) {
                    animationController!!.animationBtnNext(false)
                    changeQuestion()
                }
            }
            R.id.btnCheckAnswer -> {
                /*if (attemptsController!!.isEndlessAttempts) {
                    *//*val checkAnswerTask = CheckAnswerTask()
                    checkAnswerTask.execute(etAnswer!!.text.toString())*//*
                } else if (attemptsController!!.countAttempts == 0) {
                    attemptByAd
                } else {
                    *//*val checkAnswerTask = CheckAnswerTask()
                    checkAnswerTask.execute(etAnswer!!.text.toString())*//*
                }*/
            }
            R.id.imgBackToMain -> {
                // при возвращении на главную активити отправляем разницу между уровнем, когда юзер был на главном экране, и уровнем на данный момент
                // это нужно для анимации изменения уровня на главной активити
                val pastLevel = intent.getIntExtra("past_level", 1)
                val intentMain = Intent()
                intentMain.putExtra("differ_level", Progress.getInstance().level - pastLevel)
                try {
                    setResult(RESULT_OK, intentMain)
                    finish()
                } catch (ex: NullPointerException) {
                }
            }
        }
    }

    /*private inner class LoadRiddle : AsyncTask<Void?, Void?, Boolean?>() {
        var loadError = false
        protected override fun doInBackground(vararg voids: Void): Boolean? {
            try {
                if (!UpdateDataController.getInstance()
                        .nextRiddleIsLoaded() && Player.getInstance().level < 21
                ) {
                    riddlesController.loadNextRiddle()
                }
                if (!UpdateDataController.getInstance()
                        .riddleIsLoaded() && Player.getInstance().level > 9
                ) {
                    riddlesController.loadRiddle()
                    return true
                }
            } catch (ex: NoInternetException) {
                loadError = true
            }
            return null
        }

        override fun onPostExecute(isCurrentRiddle: Boolean?) {
            super.onPostExecute(isCurrentRiddle)
            if (isCurrentRiddle != null) {
                if (!loadError) {
                    if (isCurrentRiddle) {
                        tvQuestion!!.text = riddlesController.riddle
                    }
                } else Toast.makeText(
                    this@RiddlesActivity,
                    R.string.load_riddle_error,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private inner class CheckAnswerTask : AsyncTask<String?, Void?, Boolean>() {
        // проверка ответа
        // используются, чтобы запретить кликать пока не нажмут кнопку "дальше"
        var isAnswerRight = false
        var isNoServerErrors = true
        override fun onPreExecute() {
            btnCheckAnswer!!.isClickable = false
        }

        protected override fun doInBackground(vararg answer: String): Boolean {
            val answerOfUser = answer[0]
            if (answerOfUser != "") {
                try {
                    if (riddlesController.checkAnswer(answerOfUser)) { // если ответ правильный
                        attemptsController!!.resetCountAttempts()
                        attemptsController!!.resetCountWrongAnswers()
                        handler!!.sendEmptyMessage(HIDE_PURCHASE)
                        if (Progress.getInstance().level <= 20) {
                            Progress.getInstance().levelUp() // повышвем уровень
                            statisticsController!!.sendNewLevel(Progress.getInstance().level) // отправляем статистику на сервер
                            statisticsController!!.setStartTimeLevel() // устанавливаем время начала прохождения нового уровня
                            answerInputAnimation!!.sendEmptyMessage(RIGHT_ANSWER)
                        } else if (Progress.getInstance().level == 21) { // если пройденнй уровень был последним
                            Progress.getInstance().done(true)
                            Progress.getInstance().levelUp()
                            answerInputAnimation!!.sendEmptyMessage(RIGHT_ANSWER)
                        }
                        isAnswerRight = true
                    } else { // если ответ неверный, уменьшаем попытки
                        isAnswerRight = false
                        answerInputAnimation!!.sendEmptyMessage(WRONG_ANSWER)

                        // показываем предложение о покупке
                        if (adShowed && !purchaseController!!.isPayComplete) {
                            if (purchaseController!!.countPurchaseOffer == 0) {
                                handler!!.sendEmptyMessage(SHOW_PURCHASE)
                                purchaseController!!.increaseCountPurchaseOffer()
                            } else if (purchaseController!!.countPurchaseOffer == 1) {
                                if (attemptsController!!.countWrongAnswers == 18) {
                                    handler!!.sendEmptyMessage(SHOW_PURCHASE)
                                    purchaseController!!.increaseCountPurchaseOffer()
                                }
                            }
                        }
                        val countAttempts = attemptsController!!.countAttempts
                        if (countAttempts > 0 && !attemptsController!!.isEndlessAttempts) {
                            attemptsController!!.decrementCountAtempts()
                        }
                        attemptsController!!.increaseCountWrongAnswers()
                    }
                } catch (ex: NoInternetException) {
                    val assistentDialog = AssistentDialog(AssistentDialog.DIALOG_ALERT_INTERNET)
                    assistentDialog.show(
                        this@RiddlesActivity.supportFragmentManager,
                        "ALERT_INTERNET"
                    )
                    isNoServerErrors = false
                } catch (ex: ErrorOnServerException) {
                    isNoServerErrors = false
                    val assistentDialog = AssistentDialog(AssistentDialog.DIALOG_SERVER_ERROR)
                    assistentDialog.show(
                        this@RiddlesActivity.supportFragmentManager,
                        "ALERT_SERVER"
                    )
                } catch (ex: IOException) {
                    isNoServerErrors = false
                    val assistentDialog = AssistentDialog(AssistentDialog.DIALOG_SERVER_ERROR)
                    assistentDialog.show(
                        this@RiddlesActivity.supportFragmentManager,
                        "ALERT_SERVER"
                    )
                }
                handler!!.sendEmptyMessage(CHANGE_HINT_ANSWER)
            }
            return false
        }

        override fun onPostExecute(isWinner: Boolean) {
            if (isAnswerRight) btnCheckAnswer!!.isClickable =
                false else btnCheckAnswer!!.isClickable = true
            if (!isNoServerErrors) btnCheckAnswer!!.isClickable = true
            if (Progress.getInstance().isDone) {
                finish()
                val intent = Intent(this@RiddlesActivity, DoneActivity::class.java)
                intent.putExtra("past_level", getIntent().getIntExtra("past_level", 1))
                startActivity(
                    Intent(
                        this@RiddlesActivity,
                        DoneActivity::class.java
                    )
                ) // замена текущей активити на фрагмент с концом игры
            }
        }
    }*/

    companion object {
        fun convertPixelsToDp(px: Float): Float {
            return px / (GetContextClass.getContext().resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }
    }
}