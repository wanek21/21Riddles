package martian.riddles.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Point
import android.graphics.drawable.Animatable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
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
import dagger.hilt.android.AndroidEntryPoint
import martian.riddles.R
import martian.riddles.domain.*
import martian.riddles.util.Status
import java.util.*
import java.util.concurrent.TimeUnit

// активити, где отображаются загадки
@AndroidEntryPoint
class RiddlesActivity : AppCompatActivity() {

    private var tvRiddle: TextView? = null
    private var tvTopLvl: TextView? = null // следующий уровень, будет "вылазить" сверху при анимации
    private var tvBottomLvl: TextView? = null // текущий уровень
    private var etAnswer: EditText? = null
    private var btnNextRiddle: Button? = null
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
    var rewardedAd: RewardedAd? = null

    private var riddleHandlerAnimation: Handler? = null
    private var answerInputAnimation: Handler? = null
    private var purchaseController: PurchaseController? = null
    private var animationController: AnimationController? = null
    var fullScreenContentCallback: FullScreenContentCallback? = null

    private val RIDDLE_ALPHA_DOWN = 1
    private val RIDDLE_ALPHA_UP = 2
    private val CHANGE_ANSWER = 9
    private val ANSWER_TRANSITION_RESET = 13

    private var adBlockId: String? = null
    private var adShowed = false // если реклама показалась, то можно показывать предложение о покупке
    val SHOW_PURCHASE_ATTEMPTS = 18 // кол-во неудачных попыток ответа, после которых показывается предложение о покупке

    private val viewModel: RiddlesActivityViewModel by lazy {
        ViewModelProvider(this).get(RiddlesActivityViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

        adBlockId = this.resources.getString(R.string.ad_block)
        answerInputAnimation = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    ANSWER_TRANSITION_RESET -> {
                        animationController?.transitionInputReset()
                    }
                }
            }
        }
        riddleHandlerAnimation = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    RIDDLE_ALPHA_DOWN -> tvRiddle?.animate()?.alpha(0f)?.duration = 1000
                    RIDDLE_ALPHA_UP -> tvRiddle?.animate()?.alpha(1f)?.duration = 1000
                    CHANGE_ANSWER -> {
                        viewModel.getCurrentRiddle()
                    }
                }
            }
        }

        tvRiddle = findViewById(R.id.tvQuestion)
        tvTopLvl = findViewById(R.id.tvTop)
        tvBottomLvl = findViewById(R.id.tvBottom)
        imgGreenMark = findViewById(R.id.imgGreenMark)
        answerStateImg = findViewById(R.id.imgAnswerAnimation)
        etAnswer = findViewById(R.id.etAnswer)
        btnNextRiddle = findViewById(R.id.btnNextQuestion)
        progressAdLoad = findViewById(R.id.progressLoadAd)
        btnCheckAnswer = findViewById(R.id.btnCheckAnswer)
        imgBackToMain = findViewById(R.id.imgBackToMain)
        mlMain = findViewById(R.id.mlMain)
        mlLevel = findViewById(R.id.mlLevel)
        mlBottom = findViewById(R.id.mlCheckAndNext)
        imgShowBuy = findViewById(R.id.imgShowBuy)
        btnBuy = findViewById(R.id.btnBuy)
        btnBuy?.setOnClickListener { purchaseController?.buy() }
        btnNextRiddle?.setOnClickListener(onClickListener)
        btnCheckAnswer?.setOnClickListener(onClickListener)
        imgBackToMain?.setOnClickListener(onClickListener)

        etAnswer?.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                animationController?.focusEditText()
            }
        }
        viewModel.riddle.observe(this, {
            when(it.status) {
                Status.SUCCESS -> {
                    tvRiddle?.text = it.data
                }
                Status.ERROR -> {
                    tvRiddle?.text = getString(it.message ?: R.string.load_riddle_error)
                }
            }
        })
        viewModel.answerStatus.observe(this, {
            when(it.status) {
                Status.SUCCESS -> {
                    if(it.data == true) { // если ответ правильный
                        animationController?.editTextRightAnswer()
                        animationController?.animationBtnNext()
                        animationController?.markAnimate()
                        btnCheckAnswer?.isClickable = false

                        // замена текущей активити на фрагмент с концом игры
                        if(viewModel.getMyLevel() >= 22) {
                            val intent = Intent(this@RiddlesActivity, DoneActivity::class.java)
                            intent.putExtra("past_level", getIntent().getIntExtra("past_level", 1))
                            startActivity(
                                Intent(
                                    this@RiddlesActivity,
                                    DoneActivity::class.java
                                )
                            )
                            finish()
                        }
                    } else { // если ответ неправильный
                        animationController?.editTextWrongAnswer()
                        etAnswer?.setText("")

                        // показываем предложение о покупке, если потрачено достаточное кол-во попыток
                        if (adShowed && !purchaseController!!.isPayComplete) {
                            if (purchaseController?.countPurchaseOffer == 0) {
                                animationController?.showPurchase()
                                purchaseController?.increaseCountPurchaseOffer()
                            } else if (purchaseController!!.countPurchaseOffer == 1) {
                                if (viewModel.getCountWrongAnswers() == SHOW_PURCHASE_ATTEMPTS) {
                                    animationController?.showPurchase()
                                    purchaseController?.increaseCountPurchaseOffer()
                                }
                            }
                        }
                    }

                    animationController?.changeHintAnswer()
                }
                Status.LOADING -> {
                    btnCheckAnswer?.isClickable = false
                }
                Status.ERROR -> {
                    Toast.makeText(
                        this,
                        getString(it.message ?: R.string.error_during_checking_answer),
                        Toast.LENGTH_SHORT
                    ).show()
                    btnCheckAnswer?.isClickable = true
                }
            }
        })
        tvTopLvl?.text = (viewModel.getMyLevel()+1).toString()
        tvBottomLvl?.text = viewModel.getMyLevel().toString()
        animationController = AnimationController()

        setInputMode() // если экран маленький, то макет поднимается при появлении клавиатуры
        fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {}

            // Code to be invoked when the ad dismissed full screen content.
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadAd()
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
                    rewardedAd = ad
                    rewardedAd?.fullScreenContentCallback = fullScreenContentCallback
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    //statisticsController!!.sendErrorAd(loadAdError.code)
                    when (loadAdError.code) {
                        0 -> Toast.makeText(
                            this@RiddlesActivity,
                            getString(R.string.ad_error_0),
                            Toast.LENGTH_LONG
                        ).show()
                        else -> Toast.makeText(
                            this@RiddlesActivity,
                            R.string.error_download_ad,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }

    // показать рекламу, чтобы добавить попытку
    private fun getAttemptByAd() {
        if (rewardedAd != null) {
            rewardedAd?.show(
                this
            ) {
                Toast.makeText(
                    this@RiddlesActivity,
                    R.string.attempt_is_added,
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.upCountAttempts()
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
        get() {
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
        animationController?.setAttemptsOnScreen()
        viewModel.getCurrentRiddle()
    }

    public override fun onPause() {
        super.onPause()
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        toMainScreen()
    }

    // при возвращении на главную активити отправляем разницу между уровнем, когда юзер был на главном экране и уровнем на данный момент
    // это нужно для анимации изменения уровня на главной активити
    private fun toMainScreen() {
        val pastLevel = intent.getIntExtra("past_level", 1)
        val intentMain = Intent()
        intentMain.putExtra("differ_level", viewModel.getMyLevel() - pastLevel)
        try {
            setResult(RESULT_OK, intentMain)
            finish()
        } catch (ex: NullPointerException) {
        }
        finish()
    }

    private fun changeRiddle() {
        // анимация смены загадки
        Thread {
            riddleHandlerAnimation?.sendEmptyMessage(RIDDLE_ALPHA_DOWN)
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            riddleHandlerAnimation?.sendEmptyMessage(CHANGE_ANSWER)
            answerInputAnimation?.sendEmptyMessage(ANSWER_TRANSITION_RESET) // сбрасываем transition, чтобы запустить потом снова
            riddleHandlerAnimation?.sendEmptyMessage(RIDDLE_ALPHA_UP)
        }.start()
        animationController?.changeRiddle()
        animationController?.changeLevelTop()
        btnCheckAnswer?.isClickable = true
        etAnswer?.setText("")
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
            val countAttempts = viewModel.getCountAttempts()
            if (viewModel.isEndlessAttempts()) {
                btnCheckAnswer?.maxLines = 1
                btnCheckAnswer?.setText(R.string.check_answer)
                imgShowBuy?.isClickable = false
                imgShowBuy?.alpha = 0f
                etAnswer?.hint = ""
            } else if (countAttempts == 0) {
                btnCheckAnswer?.maxLines = 2
                btnCheckAnswer?.setText(R.string.look_ad)
                etAnswer?.hint = resources.getString(R.string.attempts) + " " + countAttempts
            } else if (countAttempts <= 3) {
                btnCheckAnswer?.maxLines = 1
                btnCheckAnswer?.setText(R.string.check_answer)
                etAnswer?.hint = resources.getString(R.string.attempts) + " " + countAttempts
            }
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
                    setAnswerInputToNormal()
                }
            }.start()
        }

        fun setAnswerInputToNormal() {
            answerStateImg?.setImageDrawable(getDrawable(R.drawable.norm_to_right))
            animationController?.INPUT_STATE = 0
        }
        fun changeRiddle() {
            answerTransitions?.reverseTransition(500)
            mlMain?.transitionToStart()
        }

        fun changeHintAnswer() {
            if (!viewModel.isEndlessAttempts()) {
                val countAttempts = viewModel.getCountAttempts()
                if (countAttempts in 1..3) {
                    etAnswer?.hint =
                        resources.getString(R.string.attempts) + " " + countAttempts
                    btnCheckAnswer?.maxLines = 1
                    btnCheckAnswer?.setText(R.string.check_answer)
                } else if (countAttempts == 0) {
                    etAnswer?.hint =
                        resources.getString(R.string.attempts) + " " + countAttempts
                    btnCheckAnswer?.maxLines = 2
                    btnCheckAnswer?.setText(R.string.look_ad)
                }
            } else {
                etAnswer?.hint = ""
            }
        }

        fun markAnimate() {
            mlMain?.transitionToEnd()
            val drawable = imgGreenMark!!.drawable
            if (drawable is Animatable) {
                (drawable as Animatable).start()
            }
        }

        fun showPurchase() {
            mlBottom?.transitionToEnd()
        }

        fun hidePurchase() {
            mlBottom?.transitionToStart()
        }

        fun changeLevelTop() {
            mlLevel?.setTransitionListener(object : MotionLayout.TransitionListener {
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
                        tvBottomLvl?.text = viewModel.getMyLevel().toString()
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
            tvTopLvl?.text = viewModel.getMyLevel().toString()
            mlLevel?.transitionToEnd()
        }

        fun animationBtnNext(appear: Boolean) { // анимация появлеия кнопки "дальше"
            val animatorBtnNextX: ObjectAnimator
            val animatorBtnNextY: ObjectAnimator
            if (appear) {
                btnNextRiddle?.visibility = View.VISIBLE
                btnNextRiddle?.isClickable = true
                btnNextRiddle?.alpha = 1.0f
                animatorBtnNextX = ObjectAnimator.ofFloat(btnNextRiddle, View.SCALE_X, 1.0f, 1.1f, 1.0f)
                animatorBtnNextY = ObjectAnimator.ofFloat(btnNextRiddle, View.SCALE_Y, 1.0f, 1.1f, 1.0f)
                animatorBtnNextX.duration = 300
                animatorBtnNextY.duration = 300
                animatorBtnNextX.start()
            } else {
                btnNextRiddle?.isClickable = false
                val btnNextAnimator = ObjectAnimator.ofFloat(btnNextRiddle, View.ALPHA, 1.0f, 0.0f)
                btnNextAnimator.duration = 400
                btnNextAnimator.start()
            }
        }

        fun animationBtnNext() { // анимация появления кнопки "дальше"
            val animatorBtnNextX: ObjectAnimator
            val animatorBtnNextY: ObjectAnimator
            if (viewModel.getMyLevel() <= 21) {
                btnNextRiddle?.visibility = View.VISIBLE
                btnNextRiddle?.isClickable = true
                btnNextRiddle?.alpha = 1.0f
                animatorBtnNextX = ObjectAnimator.ofFloat(btnNextRiddle, View.SCALE_X, 1.0f, 1.1f, 1.0f)
                animatorBtnNextY = ObjectAnimator.ofFloat(btnNextRiddle, View.SCALE_Y, 1.0f, 1.1f, 1.0f)
                animatorBtnNextX.duration = 300
                animatorBtnNextY.duration = 300
                animatorBtnNextX.start()
            } else {
                btnNextRiddle?.isClickable = false
                val btnNextAnimator = ObjectAnimator.ofFloat(btnNextRiddle, View.ALPHA, 1.0f, 0.0f)
                if (!isFirstLaunch) {
                    btnNextAnimator.duration = 400
                } else {
                    isFirstLaunch = false
                    btnNextRiddle?.visibility = View.INVISIBLE
                    btnNextAnimator.duration = 0
                }
                btnNextAnimator.start()
            }
        }

        fun transitionInputReset() {
            answerTransitions?.resetTransition()
        }

        fun editTextRightAnswer() {
            if (INPUT_STATE == 2) { // если сейчас красный ободок, то заменяем на другой
                answerStateImg?.setImageDrawable(getDrawable(R.drawable.norm_to_right))
            }
            answerTransitions = answerStateImg!!.drawable as TransitionDrawable
            answerTransitions?.isCrossFadeEnabled = false
            answerTransitions?.startTransition(280)
            INPUT_STATE = 1
        }

        fun setRedAnswer() {
            answerStateImg?.setImageResource(R.drawable.bottom_img_wrong)
            animationController?.INPUT_STATE = 2
        }

        fun editTextWrongAnswer() {
            Thread {
                // поток для изменения цвета обводки ответа на неправильный
                setRedAnswer()
                try {
                    TimeUnit.SECONDS.sleep(3)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                if (INPUT_STATE != 1) {
                    setAnswerInputToNormal() // возвращаемся к обычному состоянию
                }
            }.start()
        }

        fun showProgressBarAd() {
            progressAdLoad?.visibility = View.VISIBLE
        }

        fun hideProgressBarAd() {
            progressAdLoad?.visibility = View.INVISIBLE
        }

        init {
            animationBtnNext(false) // делаем кнопку "дальше" невидимой при старте
            mlBottom?.setTransitionListener(object : MotionLayout.TransitionListener {
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
                        imgShowBuy?.animate()?.rotation(180f)
                    } else if (i == R.id.start) {
                        imgShowBuy?.animate()?.rotation(0f)
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

        /*companion object {
            const val defaultInputState = 0
            const val rightInputState = 1 // правильный ответ
            const val wrongInputState = 2 // неправильный ответ
        }*/
    }

    private val onClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.btnNextQuestion -> {
                if (viewModel.getMyLevel() < 22) {
                    animationController?.animationBtnNext(false)
                    changeRiddle()
                }
            }
            R.id.btnCheckAnswer -> {
                if (viewModel.isEndlessAttempts()) {
                    viewModel.checkAnswer(etAnswer?.text.toString())
                } else if (viewModel.getCountAttempts() == 0) {
                    getAttemptByAd()
                } else {
                    viewModel.checkAnswer(etAnswer?.text.toString())
                }
            }
            R.id.imgBackToMain -> {
                toMainScreen()
            }
        }
    }

    /* контроллер покупки находится в активити, потому что ему нужен доступ к этой активити и контексту
    *  а передача ссылки на активити через view model может привести к утечке памяти */
    inner class PurchaseController {

        var billingClient: BillingClient
        private val mSkuDetailsMap: MutableMap<String, SkuDetails> = HashMap()
        var countPurchaseOffer: Int
            private set
        var isPayComplete = false
            private set
        private val mSkuId = "endless_attempts"

        private fun handlePurchase(purchase: Purchase) {
            // Acknowledge the purchase if it hasn't already been acknowledged.
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult: BillingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        payComplete()
                        //statisticsController.sendPurchase(attemptsController.countWrongAnswers)
                        animationController?.hidePurchase()
                    } else {
                        Toast.makeText(
                            this@RiddlesActivity,
                            "Error with code " + billingResult.responseCode,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        fun increaseCountPurchaseOffer() {
            viewModel.changeCountPurchaseOffer(++countPurchaseOffer)
        }

        fun buy() {
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(mSkuDetailsMap[mSkuId]!!)
                .build()
            billingClient.launchBillingFlow(this@RiddlesActivity, billingFlowParams)
        }

        private fun payComplete() {
            animationController?.setAttemptsOnScreen()
            viewModel.setEndlessAttempts()
            isPayComplete = true
        }

        private fun querySkuDetails() {
            val skuDetailsParamsBuilder = SkuDetailsParams.newBuilder()
            val skuList: MutableList<String> = ArrayList()
            skuList.add(mSkuId)
            skuDetailsParamsBuilder.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
            billingClient.querySkuDetailsAsync(skuDetailsParamsBuilder.build()) { billingResult, list ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                    for (skuDetails in list) {
                        mSkuDetailsMap[skuDetails.sku] = skuDetails
                    }
                }
            }
        }

        init {
            countPurchaseOffer = viewModel.getCountPurchaseOffer()
            billingClient = BillingClient.newBuilder(this@RiddlesActivity)
                .enablePendingPurchases()
                .setListener { billingResult: BillingResult, list: List<Purchase>? ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                        //сюда мы попадем когда будет осуществлена покупка
                        if (list[0].purchaseState == Purchase.PurchaseState.PURCHASED) {
                            handlePurchase(list[0])
                        }
                    }
                }.build()
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    try {
                        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            querySkuDetails() //запрос о товарах
                            val purchasesList = queryPurchases() //запрос о покупках

                            //если товар уже куплен, предоставить его пользователю
                            for (i in purchasesList!!.indices) {
                                val purchaseId = purchasesList[i].sku
                                if (TextUtils.equals(mSkuId, purchaseId)) {
                                    payComplete()
                                }
                            }
                        }
                    } catch (ex: NullPointerException) {
                    }
                }

                private fun queryPurchases(): List<Purchase>? {
                    val purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                    return purchasesResult.purchasesList
                }

                override fun onBillingServiceDisconnected() {
                    //сюда мы попадем если что-то пойдет не так
                }
            })
        }
    }

    private fun convertPixelsToDp(px: Float): Float {
        return px / (this.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}