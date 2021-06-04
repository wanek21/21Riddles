package martian.riddles.ui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import martian.riddles.R;
import martian.riddles.controllers.AttemptsController;
import martian.riddles.controllers.GetContextClass;
import martian.riddles.controllers.Progress;
import martian.riddles.controllers.RiddlesController;
import martian.riddles.controllers.StatisticsController;
import martian.riddles.controllers.StoredData;
import martian.riddles.controllers.UpdateDataController;
import martian.riddles.exceptions.ErrorOnServerException;
import martian.riddles.exceptions.NoInternetException;
import martian.riddles.dto.Player;


public class QuestionActivity extends AppCompatActivity { // активити, где отображаются загадки


    public RewardedAd rewardedAd;

    private TextView tvQuestion;
    private TextView tvTopLvl;
    private TextView tvBottomLvl;
    private EditText etAnswer;
    private Button btnNext;
    private Button btnCheckAnswer;
    private ProgressBar progressAdLoad;
    private ImageView answerStateImg;
    private ImageView imgGreenMark;
    private ImageView imgBackToMain;
    private MotionLayout mlMain;
    private MotionLayout mlLevel;
    private MotionLayout mlBottom;
    private ImageView imgShowBuy;
    private Button btnBuy;

    private RiddlesController riddlesController = new RiddlesController();
    private StatisticsController statisticsController;
    private Handler handler;
    private Handler answerInputAnimation;
    private AnimationController animationController;
    private AttemptsController attemptsController;
    private PurchaseController purchaseController;
    FullScreenContentCallback fullScreenContentCallback = null;

    private final int ALPHA_DOWN = 1;
    private final int ALPHA_UP = 2;
    private final int LOAD_AD = 3;
    private final int SET_RED_ANSWER = 4;
    private final int ALPHA_DOWN_BTNNEXT = 7;
    private final int SET_INVISIBLE_BTNNEXT = 8;
    private final int CHANGE_ANSWER = 9;
    private final int TRANSITION_RESET = 13;
    private final int SHOW_PROGRESS = 14;
    private final int HIDE_PROGRESS = 15;
    private final int CHANGE_HINT_ANSWER = 17;
    private final int SHOW_PARTING_WORD = 18;
    private final int SHOW_PURCHASE = 19;
    private final int HIDE_PURCHASE = 20;
    private final int FOCUS_FIXED = 21;
    private final int RIGHT_ANSWER = 22;
    private final int WRONG_ANSWER = 23;


    private String adBlockId;
    private boolean adShowed = false; // если реклама показалась, то можно показывать предложение о покупке


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        adBlockId = this.getResources().getString(R.string.ad_block);

        answerInputAnimation = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case FOCUS_FIXED: {
                        answerStateImg.setImageDrawable(getDrawable(R.drawable.norm_to_right));
                        animationController.INPUT_STATE = 0;
                        break;
                    }
                    case RIGHT_ANSWER: {
                        animationController.editTextRightAnswer();
                        animationController.animationBtnNext();
                        animationController.markAnimate();
                        btnCheckAnswer.setClickable(false);
                        break;

                    }
                    case WRONG_ANSWER: {
                        animationController.editTextWrongAnswer();
                        etAnswer.setText("");
                        break;
                    }
                    case SET_RED_ANSWER: {
                        answerStateImg.setImageResource(R.drawable.bottom_img_wrong);
                        animationController.INPUT_STATE = 2;
                        break;
                    }
                    case TRANSITION_RESET: {
                        animationController.transitionInputReset();
                        break;
                    }
                }
            }
        };

        handler = new Handler() {

            /*ObjectAnimator animPartingWordShow = ObjectAnimator.ofFloat(tvPartingWord, "alpha", 0.0f, 1.0f);
            ObjectAnimator animPartingWordHide = ObjectAnimator.ofFloat(tvPartingWord, "alpha", 1.0f, 0.0f);*/

            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case ALPHA_DOWN:
                        tvQuestion.animate().alpha(0).setDuration(1000);
                        break;
                    case ALPHA_UP:
                        tvQuestion.animate().alpha(1).setDuration(1000);
                        break;
                    case LOAD_AD: {
                        loadAd();
                        break;
                    }
                    case CHANGE_ANSWER: {
                        tvQuestion.setText(riddlesController.getRiddle());
                        break;
                    }
                    case CHANGE_HINT_ANSWER: {
                        if (!attemptsController.isEndlessAttempts()) {
                            int countAttempts = attemptsController.getCountAttempts();
                            if (countAttempts > 0 && countAttempts <= 3) {
                                etAnswer.setHint(getResources().getString(R.string.attempts) + " " + countAttempts);
                                btnCheckAnswer.setMaxLines(1);
                                btnCheckAnswer.setText(R.string.check_answer);
                            } else if (countAttempts == 0) {
                                etAnswer.setHint(getResources().getString(R.string.attempts) + " " + countAttempts);
                                btnCheckAnswer.setMaxLines(2);
                                btnCheckAnswer.setText(R.string.look_ad);
                            }
                        } else {
                            etAnswer.setHint("");
                        }
                        break;
                    }
                    case ALPHA_DOWN_BTNNEXT: {
                        btnNext.animate().alpha(0).setDuration(400);
                        break;
                    }
                    case SET_INVISIBLE_BTNNEXT: {
                        btnNext.setVisibility(View.INVISIBLE);
                        break;
                    }
                    case SHOW_PROGRESS: {
                        animationController.showProgressBarAd();
                        break;
                    }
                    case HIDE_PROGRESS: {
                        animationController.hideProgressBarAd();
                        break;
                    }
                    case SHOW_PURCHASE: {
                        animationController.showPurchase();
                        break;
                    }
                    case HIDE_PURCHASE: {
                        animationController.hidePurchase();
                        break;
                    }
                    case SHOW_PARTING_WORD: {
                        /*String partingWord = partingWords.getRandomWord();
                        if (!partingWord.equals("")) {
                            if (!animPartingWordHide.isStarted()) {
                                tvPartingWord.setText(partingWord);
                                animPartingWordShow = ObjectAnimator.ofFloat(tvPartingWord, "alpha", 0.0f, 1.0f);
                                animPartingWordHide = ObjectAnimator.ofFloat(tvPartingWord, "alpha", 1.0f, 0.0f);
                                animPartingWordShow.setDuration(1000);
                                animPartingWordHide.setDuration(1000);
                                animPartingWordHide.setStartDelay(9000);
                                animPartingWordHide.start();
                                animPartingWordShow.start();
                            }
                        }
                        break;*/
                    }
                }
            }
        };

        tvQuestion = findViewById(R.id.tvQuestion);
        tvTopLvl = findViewById(R.id.tvTop);
        tvBottomLvl = findViewById(R.id.tvBottom);
        imgGreenMark = findViewById(R.id.imgGreenMark);
        answerStateImg = findViewById(R.id.imgAnswerAnimation);
        etAnswer = findViewById(R.id.etAnswer);
        btnNext = findViewById(R.id.btnNextQuestion);
        progressAdLoad = findViewById(R.id.progressLoadAd);
        btnCheckAnswer = findViewById(R.id.btnCheckAnswer);
        imgBackToMain = findViewById(R.id.imgBackToMain);
        mlMain = findViewById(R.id.mlMain);
        mlLevel = findViewById(R.id.mlLevel);
        mlBottom = findViewById(R.id.mlCheckAndNext);
        imgShowBuy = findViewById(R.id.imgShowBuy);
        btnBuy = findViewById(R.id.btnBuy);
        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseController.buy();
            }
        });

        btnNext.setOnClickListener(onClickListener);
        btnCheckAnswer.setOnClickListener(onClickListener);
        imgBackToMain.setOnClickListener(onClickListener);
        etAnswer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    animationController.focusEditText();
                }
            }
        });

        statisticsController = new StatisticsController(this);
        purchaseController = new PurchaseController(this);
        animationController = new AnimationController();
        attemptsController = new AttemptsController(statisticsController);

        // если юзер разгадал все, но не проверил является ли он победителем
        if (!StoredData.getDataBool(StoredData.DATA_WINNER_IS_CHECKED) && (Progress.getInstance().getLevel() < 22)) {
            tvQuestion.setText(riddlesController.getRiddle());
            tvTopLvl.setText(String.valueOf(Progress.getInstance().getLevel() + 1));
            tvBottomLvl.setText(String.valueOf(Progress.getInstance().getLevel()));
        }
        setInputMode(); // если экран маленький, то макет поднимается при фокусе клавиатуры

        fullScreenContentCallback =
                new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        // Code to be invoked when the ad showed full screen content.
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        rewardedAd = null;
                        loadAd();
                        // Code to be invoked when the ad dismissed full screen content.
                    }
                };
        loadAd();
    }

    private void loadAd() {
        RewardedAd.load(this, adBlockId, new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                super.onAdLoaded(rewardedAd);
                rewardedAd = ad;
                rewardedAd.setFullScreenContentCallback(fullScreenContentCallback);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                statisticsController.sendErrorAd(loadAdError.getCode());
                switch (loadAdError.getCode()) {
                    case 0:
                        Toast.makeText(QuestionActivity.this, getString(R.string.ad_error_0), Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Toast.makeText(GetContextClass.getContext(), R.string.error_download_ad, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getAttemptByAd() { // показать рекламу, чтобы добавить попытку
        if (rewardedAd != null) {
            rewardedAd.show(
                    this,
                    new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            Toast.makeText(QuestionActivity.this, R.string.attempt_is_added, Toast.LENGTH_SHORT).show();
                            attemptsController.incrementCountAtempts();
                            adShowed = true;
                        }
                    });
        } else {
            Toast.makeText(
                    QuestionActivity.this,
                    getText(R.string.ad_not_ready_yet), Toast.LENGTH_SHORT).show();
        }
    }

    private int getWidthScreen() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static float convertPixelsToDp(float px) {
        return px / ((float) GetContextClass.getContext().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    private void setInputMode() {
        int widthScreen = (int) convertPixelsToDp(getWidthScreen());
        if (widthScreen < 360) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        animationController.setAttemptsOnScreen();
        if (Player.getInstance().getLevel() > 9) {
            new LoadRiddle().execute();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        int pastLevel = getIntent().getIntExtra("past_level", 1);
        Intent intentMain = new Intent();
        intentMain.putExtra("differ_level", Progress.getInstance().getLevel() - pastLevel);
        try {
            setResult(Activity.RESULT_OK, intentMain);
            finish();
        } catch (NullPointerException ex) {
        }
        finish();
    }

    private void changeQuestion() {
        // анимация вопроса
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(ALPHA_DOWN);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(CHANGE_ANSWER);
                answerInputAnimation.sendEmptyMessage(TRANSITION_RESET); // сбрасываем transition, чтобы запустить потом снова
                handler.sendEmptyMessage(ALPHA_UP);
            }
        }).start();

        animationController.changeQuestion();
        animationController.changeLevelTop();
        btnCheckAnswer.setClickable(true);
        etAnswer.setText("");
    }


    // внутренние контроллеры и потоки -----------------------------------------------------------------------------


    private class PurchaseController {

        BillingClient billingClient;
        private Map<String, SkuDetails> mSkuDetailsMap = new HashMap<>();
        private Context context;
        private int countPurchaseOffer;
        private boolean isPayComplete = false;

        private String mSkuId = "endless_attempts";
        public final static String DATA_SHOW_PURCHASE = "show_purchase";

        public PurchaseController(Context context) {
            this.context = context;
            countPurchaseOffer = StoredData.getDataInt(DATA_SHOW_PURCHASE, 0);
            billingClient = BillingClient.newBuilder(context)
                    .enablePendingPurchases()
                    .setListener((billingResult, list) -> {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                            //сюда мы попадем когда будет осуществлена покупка
                            if (list.get(0).getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                handlePurchase(list.get(0));
                            }

                        }
                    }).build();
            billingClient.startConnection(new BillingClientStateListener() {

                @Override
                public void onBillingSetupFinished(BillingResult billingResult) {
                    try {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            querySkuDetails(); //запрос о товарах
                            List<Purchase> purchasesList = queryPurchases(); //запрос о покупках

                            //если товар уже куплен, предоставить его пользователю
                            for (int i = 0; i < purchasesList.size(); i++) {
                                String purchaseId = purchasesList.get(i).getSku();
                                if (TextUtils.equals(mSkuId, purchaseId)) {
                                    payComplete();
                                }
                            }
                        }
                    } catch (NullPointerException ex) {
                    }
                }

                private List<Purchase> queryPurchases() {
                    Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
                    return purchasesResult.getPurchasesList();
                }

                @Override
                public void onBillingServiceDisconnected() {
                    //сюда мы попадем если что-то пойдет не так
                }
            });
        }

        private void handlePurchase(Purchase purchase) {
            // Acknowledge the purchase if it hasn't already been acknowledged.
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        payComplete();
                        statisticsController.sendPurchase(attemptsController.getCountWrongAnswers());
                        handler.sendEmptyMessage(HIDE_PURCHASE);
                    } else {
                        Toast.makeText(QuestionActivity.this, "Error with code " + billingResult.getResponseCode(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        public int getCountPurchaseOffer() {
            return countPurchaseOffer;
        }

        public void increaseCountPurchaseOffer() {
            StoredData.saveData(DATA_SHOW_PURCHASE, ++countPurchaseOffer);
        }

        public void buy() {
            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(mSkuDetailsMap.get(mSkuId))
                    .build();
            billingClient.launchBillingFlow((Activity) context, billingFlowParams);
        }

        private void payComplete() {
            attemptsController.setEndlessAttempts(true);
            animationController.setAttemptsOnScreen();
            isPayComplete = true;
        }

        public boolean isPayComplete() {
            return isPayComplete;
        }

        private void querySkuDetails() {
            SkuDetailsParams.Builder skuDetailsParamsBuilder = SkuDetailsParams.newBuilder();
            List<String> skuList = new ArrayList<>();
            skuList.add(mSkuId);
            skuDetailsParamsBuilder.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
            billingClient.querySkuDetailsAsync(skuDetailsParamsBuilder.build(), new SkuDetailsResponseListener() {

                @Override
                public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> list) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                        for (SkuDetails skuDetails : list) {
                            mSkuDetailsMap.put(skuDetails.getSku(), skuDetails);
                        }
                    }
                }
            });
        }
    }

    private class AnimationController {

        /*переменная хранит состояние поля ввода ответа
        0 - обычное
        1 - зеленое
        2 - красное
        */
        public int INPUT_STATE = 0;
        private boolean isFirstLaunch = true;
        private int stateProgressBarAd;
        private final int FOCUSING_ANSWER_INPUT_TIME = 1000;

        private TransitionDrawable answerTransitions;

        public AnimationController() {
            animationBtnNext(false); // делаем кнопку "дальше" невидимой при старте
            mlBottom.setTransitionListener(new MotionLayout.TransitionListener() {
                @Override
                public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {
                }

                @Override
                public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {

                }

                @Override
                public void onTransitionCompleted(MotionLayout motionLayout, int i) {
                    if (i == R.id.end) {
                        imgShowBuy.animate().rotation(180);
                    } else if (i == R.id.start) {
                        imgShowBuy.animate().rotation(0);
                    }
                }

                @Override
                public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {

                }
            });
        }

        public void setAttemptsOnScreen() {
            int countAttempts = attemptsController.getCountAttempts();
            if (attemptsController.isEndlessAttempts()) {
                btnCheckAnswer.setMaxLines(1);
                btnCheckAnswer.setText(R.string.check_answer);
                imgShowBuy.setClickable(false);
                imgShowBuy.setAlpha(0f);
                etAnswer.setHint("");
            } else if (countAttempts == 0) {
                btnCheckAnswer.setMaxLines(2);
                btnCheckAnswer.setText(R.string.look_ad);
                etAnswer.setHint(getResources().getString(R.string.attempts) + " " + countAttempts);
            } else if (countAttempts <= 3) {
                btnCheckAnswer.setMaxLines(1);
                btnCheckAnswer.setText(R.string.check_answer);
                etAnswer.setHint(getResources().getString(R.string.attempts) + " " + countAttempts);
            }

        }

        public void focusEditText() {
            TransitionDrawable transitionDrawable = (TransitionDrawable) answerStateImg.getDrawable();
            transitionDrawable.startTransition(FOCUSING_ANSWER_INPUT_TIME);
            new Thread(new Runnable() {
                @Override
                public void run() { // поток для изменения цвета обводки ответа на неправильный
                    try {
                        TimeUnit.MILLISECONDS.sleep(FOCUSING_ANSWER_INPUT_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (INPUT_STATE != 1) {
                        answerInputAnimation.sendEmptyMessage(FOCUS_FIXED);
                    }
                }
            }).start();
        }

        private void changeQuestion() {
            answerTransitions.reverseTransition(500);
            mlMain.transitionToStart();
        }

        private void markAnimate() {
            mlMain.transitionToEnd();
            Drawable drawable = imgGreenMark.getDrawable();
            if (drawable instanceof Animatable) {
                ((Animatable) drawable).start();
            }
        }

        private void showPurchase() {
            mlBottom.transitionToEnd();
        }

        private void hidePurchase() {
            mlBottom.transitionToStart();
        }

        private void changeLevelTop() {
            mlLevel.setTransitionListener(new MotionLayout.TransitionListener() {
                @Override
                public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {
                }

                @Override
                public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {
                }

                @Override
                public void onTransitionCompleted(MotionLayout motionLayout, int i) {
                    if (i == R.id.end) {
                        tvBottomLvl.setText(String.valueOf(Progress.getInstance().getLevel()));
                        motionLayout.setProgress(0f);
                        motionLayout.setTransition(R.id.start, R.id.end);
                    }
                }

                @Override
                public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {
                }

            });
            tvTopLvl.setText(String.valueOf(Progress.getInstance().getLevel()));
            mlLevel.transitionToEnd();
        }

        private void animationBtnNext(boolean appear) { // анимация появлеия кнопки "дальше"
            ObjectAnimator animatorBtnNextX;
            ObjectAnimator animatorBtnNextY;
            if (appear) {
                btnNext.setVisibility(View.VISIBLE);
                btnNext.setClickable(true);
                btnNext.setAlpha(1.0f);
                animatorBtnNextX = ObjectAnimator.ofFloat(btnNext, "scaleX", 1.0f, 1.1f, 1.0f);
                animatorBtnNextY = ObjectAnimator.ofFloat(btnNext, "scaleY", 1.0f, 1.1f, 1.0f);
                animatorBtnNextX.setDuration(300);
                animatorBtnNextY.setDuration(300);
                animatorBtnNextX.start();
            } else {
                btnNext.setClickable(false);
                ObjectAnimator btnNextAnimator = ObjectAnimator.ofFloat(btnNext, "alpha", 1.0f, 0.0f);
                btnNextAnimator.setDuration(400);
                btnNextAnimator.start();
            }
        }

        private void animationBtnNext() { // анимация появления кнопки "дальше"
            ObjectAnimator animatorBtnNextX;
            ObjectAnimator animatorBtnNextY;
            if (Progress.getInstance().getLevel() <= 21) {
                btnNext.setVisibility(View.VISIBLE);
                btnNext.setClickable(true);
                btnNext.setAlpha(1.0f);
                animatorBtnNextX = ObjectAnimator.ofFloat(btnNext, "scaleX", 1.0f, 1.1f, 1.0f);
                animatorBtnNextY = ObjectAnimator.ofFloat(btnNext, "scaleY", 1.0f, 1.1f, 1.0f);
                animatorBtnNextX.setDuration(300);
                animatorBtnNextY.setDuration(300);
                animatorBtnNextX.start();
            } else {
                btnNext.setClickable(false);
                ObjectAnimator btnNextAnimator = ObjectAnimator.ofFloat(btnNext, "alpha", 1.0f, 0.0f);
                if (!isFirstLaunch) {
                    btnNextAnimator.setDuration(400);
                } else {
                    isFirstLaunch = false;
                    btnNext.setVisibility(View.INVISIBLE);
                    btnNextAnimator.setDuration(0);
                }
                btnNextAnimator.start();
            }
        }

        private void transitionInputReset() {
            answerTransitions.resetTransition();
        }

        private void editTextRightAnswer() {
            if (INPUT_STATE == 2) { // если сейчас красный ободок, то заменяем на другой
                answerStateImg.setImageDrawable(getDrawable(R.drawable.norm_to_right));
            }
            answerTransitions = (TransitionDrawable) answerStateImg.getDrawable();
            answerTransitions.setCrossFadeEnabled(false);
            answerTransitions.startTransition(280);
            INPUT_STATE = 1;
        }

        private void editTextWrongAnswer() {
            new Thread(new Runnable() {
                @Override
                public void run() { // поток для изменения цвета обводки ответа на неправильный
                    answerInputAnimation.sendEmptyMessage(SET_RED_ANSWER);
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (INPUT_STATE != 1) {
                        answerInputAnimation.sendEmptyMessage(FOCUS_FIXED); // возвращаемся к обычному состоянию
                    }
                }
            }).start();
        }

        public int getCurrentStateProgressBarAd() {
            return stateProgressBarAd;
        }

        public void setCurrentStateProgressBarAd(int currentState) {
            this.stateProgressBarAd = currentState;
        }

        public void showProgressBarAd() {
            progressAdLoad.setVisibility(View.VISIBLE);
        }

        public void hideProgressBarAd() {
            progressAdLoad.setVisibility(View.INVISIBLE);
        }

    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnNextQuestion: {
                    if (!(Progress.getInstance().getLevel() >= 22)) {
                        animationController.animationBtnNext(false);
                        changeQuestion();
                    }
                    break;
                }
                case R.id.btnCheckAnswer: {
                    if (attemptsController.isEndlessAttempts()) {
                        CheckAnswerTask checkAnswerTask = new CheckAnswerTask();
                        checkAnswerTask.execute(etAnswer.getText().toString());
                    } else if (attemptsController.getCountAttempts() == 0) {
                        getAttemptByAd();
                    } else {
                        CheckAnswerTask checkAnswerTask = new CheckAnswerTask();
                        checkAnswerTask.execute(etAnswer.getText().toString());
                    }
                    break;
                }
                case R.id.imgBackToMain:
                    // при возвращении на главную активити отправляем разницу между уровнем, когда юзер был на главном экране, и уровнем на данный момент
                    // это нужно для анимации изменения уровня на главной активити
                    int pastLevel = getIntent().getIntExtra("past_level", 1);
                    Intent intentMain = new Intent();
                    intentMain.putExtra("differ_level", Progress.getInstance().getLevel() - pastLevel);
                    try {
                        setResult(Activity.RESULT_OK, intentMain);
                        finish();
                    } catch (NullPointerException ex) {
                    }
                    break;
            }
        }
    };

    private class LoadRiddle extends AsyncTask<Void, Void, Boolean> {

        boolean loadError = false;

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                if (!UpdateDataController.getInstance().nextRiddleIsLoaded() && Player.getInstance().getLevel() < 21) {
                    riddlesController.loadNextRiddle();
                }
                if (!UpdateDataController.getInstance().riddleIsLoaded() && Player.getInstance().getLevel() > 9) {
                    riddlesController.loadRiddle();
                    return true;
                }
            } catch (NoInternetException ex) {
                loadError = true;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean isCurrentRiddle) {
            super.onPostExecute(isCurrentRiddle);
            if (isCurrentRiddle != null) {
                if (!loadError) {
                    if (isCurrentRiddle) {
                        tvQuestion.setText(riddlesController.getRiddle());
                    }
                } else
                    Toast.makeText(QuestionActivity.this, R.string.load_riddle_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    private class CheckAnswerTask extends AsyncTask<String, Void, Boolean> { // проверка ответа

        // используются, чтобы запретить кликать пока не нажмут кнопку "дальше"
        boolean isAnswerRight = false;
        boolean isNoServerErrors = true;

        @Override
        protected void onPreExecute() {
            btnCheckAnswer.setClickable(false);
        }

        @Override
        protected Boolean doInBackground(String... answer) {
            String answerOfUser = answer[0];
            if (!(answerOfUser.equals(""))) {
                try {
                    if (riddlesController.checkAnswer(answerOfUser)) { // если ответ правильный
                        attemptsController.resetCountAttempts();
                        attemptsController.resetCountWrongAnswers();
                        handler.sendEmptyMessage(HIDE_PURCHASE);
                        if (Progress.getInstance().getLevel() <= 20) {
                            Progress.getInstance().levelUp(); // повышвем уровень
                            statisticsController.sendNewLevel(Progress.getInstance().getLevel()); // отправляем статистику на сервер
                            statisticsController.setStartTimeLevel(); // устанавливаем время начала прохождения нового уровня
                            answerInputAnimation.sendEmptyMessage(RIGHT_ANSWER);
                        } else if (Progress.getInstance().getLevel() == 21) { // если пройденнй уровень был последним
                            Progress.getInstance().done(true);
                            Progress.getInstance().levelUp();
                            answerInputAnimation.sendEmptyMessage(RIGHT_ANSWER);
                        }
                        isAnswerRight = true;
                    } else { // если ответ неверный, уменьшаем попытки
                        isAnswerRight = false;
                        answerInputAnimation.sendEmptyMessage(WRONG_ANSWER);

                        // показываем предложение о покупке
                        if (adShowed && !purchaseController.isPayComplete()) {
                            if (purchaseController.getCountPurchaseOffer() == 0) {
                                handler.sendEmptyMessage(SHOW_PURCHASE);
                                purchaseController.increaseCountPurchaseOffer();
                            } else if (purchaseController.getCountPurchaseOffer() == 1) {
                                if (attemptsController.getCountWrongAnswers() == 18) {
                                    handler.sendEmptyMessage(SHOW_PURCHASE);
                                    purchaseController.increaseCountPurchaseOffer();
                                }
                            }
                        }

                        int countAttempts = attemptsController.getCountAttempts();
                        if (countAttempts > 0 && !attemptsController.isEndlessAttempts()) {
                            attemptsController.decrementCountAtempts();
                        }
                        attemptsController.increaseCountWrongAnswers();
                    }
                } catch (NoInternetException ex) {
                    AssistentDialog assistentDialog = new AssistentDialog(AssistentDialog.DIALOG_ALERT_INTERNET);
                    assistentDialog.show(QuestionActivity.this.getSupportFragmentManager(), "ALERT_INTERNET");
                    isNoServerErrors = false;
                } catch (ErrorOnServerException | IOException ex) {
                    isNoServerErrors = false;
                    AssistentDialog assistentDialog = new AssistentDialog(AssistentDialog.DIALOG_SERVER_ERROR);
                    assistentDialog.show(QuestionActivity.this.getSupportFragmentManager(), "ALERT_SERVER");
                }
                handler.sendEmptyMessage(CHANGE_HINT_ANSWER);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean isWinner) {


            if(isAnswerRight) btnCheckAnswer.setClickable(false);
            else btnCheckAnswer.setClickable(true);
            if(!isNoServerErrors) btnCheckAnswer.setClickable(true);

            if (Progress.getInstance().isDone()) {
                finish();
                Intent intent = new Intent(QuestionActivity.this, DoneActivity.class);
                intent.putExtra("past_level", getIntent().getIntExtra("past_level", 1));
                startActivity(new Intent(QuestionActivity.this, DoneActivity.class)); // замена текущей активити на фрагмент с концом игры
            }
        }

    }
}
