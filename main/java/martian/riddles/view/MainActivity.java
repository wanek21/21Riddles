package martian.riddles.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.florent37.viewtooltip.ViewTooltip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;

import martian.riddles.BuildConfig;
import martian.riddles.R;
import martian.riddles.controllers.GetContextClass;
import martian.riddles.controllers.Progress;
import martian.riddles.controllers.RequestController;
import martian.riddles.controllers.StatisticsController;
import martian.riddles.controllers.StoredData;
import martian.riddles.model.Leaders;
import martian.riddles.model.Player;
import martian.riddles.model.Prize;
import martian.riddles.model.ResponseFromServer;
import martian.riddles.util.PreferencesToFromObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yanzhikai.textpath.AsyncTextPathView;
import yanzhikai.textpath.calculator.AroundCalculator;

import static martian.riddles.controllers.StoredData.DATA_COUNT_LAUNCH_APP;
import static martian.riddles.controllers.StoredData.DATA_DONE_GAME_ANIM_COMPLETE;


public class MainActivity extends AppCompatActivity {

    private Button btnNext;
    private AsyncTextPathView tvPrize;
    private ImageView btnHelp;
    private TextView tvName;
    private ImageView imgBackLevel;
    private TextView tvLevel;

    private UpdateDataThread updateDataThread;
    private CheckForceUpdateTask checkForceUpdateTask;
    private AnimationController animController;
    private ObjectAnimator animBtnHelp;

    private String locale;
    private ArrayList<String> playersNames = new ArrayList<>();
    private ArrayList<Integer> playersLevels = new ArrayList<>();
    private ArrayList<Integer> playersCount = new ArrayList<>();

    private final String DATA_LEADERS = "leaders_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(StoredData.getDataString(Player.DATA_NAME_PLAYER,Player.getInstance().getName()).equals("")) {
            startActivity(new Intent(this,LogupActivity.class));
            finish();
        }
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StoredData.saveData(DATA_COUNT_LAUNCH_APP,StoredData.getDataInt(DATA_COUNT_LAUNCH_APP,0)+1); // увеличиваем кол-во звапусков игры на один
        locale = Locale.getDefault().getLanguage();


        tvName = findViewById(R.id.tvName);
        imgBackLevel = findViewById(R.id.imgBackLevel);
        tvLevel = findViewById(R.id.tvLevel);
        btnNext = findViewById(R.id.btnNext);
        btnHelp = findViewById(R.id.btnHelp);

        tvPrize = findViewById(R.id.tvPrize);
        tvPrize.setText(
                String.valueOf(
                StoredData.getDataString(StoredData.DATA_PRIZE,
                        getResources().getString(R.string.prize))));
        tvPrize.setDuration(6000);
        tvPrize.setOnClickListener(onClickListener);
        tvPrize.setCalculator(new AroundCalculator());

        btnNext.setOnClickListener(onClickListener);
        btnHelp.setOnClickListener(onClickListener);

        animController = new AnimationController(); // контроллер для остальных анимаций в данной активити

        if(StoredData.getDataInt(DATA_COUNT_LAUNCH_APP,0) == 2) { // доп. анимации и подсказки, если запуск первый
            ViewTooltip
                    .on(this, btnHelp)
                    .autoHide(true, 5000)
                    .corner(30)
                    .position(ViewTooltip.Position.BOTTOM)
                    .withShadow(false)
                    .text(getResources().getString(R.string.read_rules))
                    .show();
        }

    }
    @Override
    protected void onResume() {
        super.onResume();

        animController.setTextForMainButton();
        if(Progress.getInstance().getLevel() == 22 &&
                !StoredData.getDataBool(DATA_DONE_GAME_ANIM_COMPLETE)) {
            animController.initLevel(true);
            StoredData.saveData(DATA_DONE_GAME_ANIM_COMPLETE,true);
        }

        // запускаем потоки для обновления данных и проверки принудительных обновлений
        updateDataThread = new UpdateDataThread();
        updateDataThread.start();
        checkForceUpdateTask = new CheckForceUpdateTask();
        checkForceUpdateTask.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateDataThread.toStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(updateDataThread != null) updateDataThread.toStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null) {
            if (data.hasExtra("differ_level")) {
                animController.increaseLevel(data.getIntExtra("differ_level", 0));
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnNext: {
                    Intent intent;
                    if(Progress.getInstance().getLevel() < 22) {
                        intent = new Intent(MainActivity.this,QuestionActivity.class);
                        intent.putExtra("past_level",Player.getInstance().getLevel());
                    } else if(Progress.getInstance().getLevel() == 22) {
                        intent = new Intent(MainActivity.this,DoneActivity.class);
                    } else intent = null;
                    startActivityForResult(intent, 1);
                    break;
                }
                case R.id.btnHelp: {
                    startActivity(new Intent(MainActivity.this,InfoActivity.class));
                    animController.clickRules();
                    break;
                }
                case R.id.tvPrize: {
                    tvPrize.startAnimation(0,1);
                    break;
                }
            }
        }
    };

    View.OnClickListener playerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() != R.id.tvNameLeader1) {
                ViewTooltip
                        .on(MainActivity.this, v)
                        .autoHide(true, 5000)
                        .corner(30)
                        .position(ViewTooltip.Position.BOTTOM)
                        .withShadow(false)
                        .text(getResources().getString(R.string.info_player))
                        .show();
            } else {
                ViewTooltip
                        .on(MainActivity.this, v)
                        .autoHide(true, 5000)
                        .corner(30)
                        .position(ViewTooltip.Position.BOTTOM)
                        .withShadow(false)
                        .text(playersLevels.get(0) == 22 ? getResources().getString(R.string.info_first_player_complete) : getResources().getString(R.string.info_first_player))
                        .show();
            }
        }
    };

    View.OnClickListener levelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() != R.id.tvLevel1) {
                ViewTooltip
                        .on(MainActivity.this, v)
                        .autoHide(true, 5000)
                        .corner(30)
                        .position(ViewTooltip.Position.BOTTOM)
                        .withShadow(false)
                        .text(getResources().getString(R.string.current_level_info))
                        .show();
            } else if(playersLevels.get(0) != 22) {
                ViewTooltip
                        .on(MainActivity.this, v)
                        .autoHide(true, 5000)
                        .corner(30)
                        .position(ViewTooltip.Position.BOTTOM)
                        .withShadow(false)
                        .text(getResources().getString(R.string.current_level_info))
                        .show();
            }
        }
    };


    // внутренние контроллеры и потоки -----------------------------------------------------------------------------
    private class AnimationController {

        // view`шки лидеров
        private ArrayList<TextView> namesLeaders = new ArrayList<>();
        private ArrayList<TextView> countLeaders = new ArrayList<>();
        private ArrayList<TextView> levelsLeaders = new ArrayList<>();
        private ArrayList<ImageView> lines = new ArrayList<>();

        // блоки одного уровня
        private ArrayList<ImageView> levelBlocks = new ArrayList<>();

        // анимация для уровня
        ObjectAnimator blockShow;
        ObjectAnimator blockScaleX;
        ObjectAnimator blockScaleY;

        // анимация для лидеров
        ObjectAnimator nameHide;
        ObjectAnimator nameShow;
        ObjectAnimator anotherShow;
        ObjectAnimator anotherHide;
        ObjectAnimator levelHide;
        ObjectAnimator levelShow;
        ObjectAnimator lineShow;
        ObjectAnimator lineScale;

        private float alphaCurrentLevel = 0.5f;

        public AnimationController() {
            tvName.setText(Player.getInstance().getName());

            levelBlocks.add((ImageView) findViewById(R.id.imgLvl1));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl2));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl3));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl4));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl5));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl6));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl7));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl8));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl9));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl10));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl11));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl12));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl13));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl14));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl15));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl16));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl17));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl18));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl19));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl20));
            levelBlocks.add((ImageView) findViewById(R.id.imgLvl21));

            namesLeaders.add((TextView)findViewById(R.id.tvNameLeader1));
            namesLeaders.add((TextView)findViewById(R.id.tvNameLeader2));
            namesLeaders.add((TextView)findViewById(R.id.tvNameLeader3));
            namesLeaders.add((TextView)findViewById(R.id.tvNameLeader4));
            //namesLeaders.add((TextView)findViewById(R.id.tvNameLeader5));

            countLeaders.add((TextView)findViewById(R.id.tvAnotherPlayers));
            countLeaders.add((TextView)findViewById(R.id.tvAnotherPlayers2));
            countLeaders.add((TextView)findViewById(R.id.tvAnotherPlayers3));
            countLeaders.add((TextView)findViewById(R.id.tvAnotherPlayers4));

            levelsLeaders.add((TextView)findViewById(R.id.tvLevel1));
            levelsLeaders.add((TextView)findViewById(R.id.tvLevel2));
            levelsLeaders.add((TextView)findViewById(R.id.tvLevel3));
            levelsLeaders.add((TextView)findViewById(R.id.tvLevel4));
            //levelsLeaders.add((TextView)findViewById(R.id.tvLevel5));

            lines.add((ImageView)findViewById(R.id.underLine1));
            lines.add((ImageView)findViewById(R.id.underLine2));
            lines.add((ImageView)findViewById(R.id.underLine3));
            lines.add((ImageView)findViewById(R.id.underLine4));
            //lines.add((ImageView)findViewById(R.id.underLine5));

            initLevel(false);
            initLeaders();
        }

        public void setTextForMainButton() {
            if(Progress.getInstance().getLevel() == 1) btnNext.setText(MainActivity.this.getResources().getString(R.string.start_game));
            else if(Progress.getInstance().getLevel() < 22) btnNext.setText(MainActivity.this.getResources().getString(R.string.continue_game));
            else if(Progress.getInstance().getLevel() > 21) btnNext.setText(R.string.view_resluts_btn);
        }

        public void clickRules() { // анимация нажатия кнопки с правилами
            ObjectAnimator btnHide = ObjectAnimator.ofFloat(btnHelp, "alpha", 1.0f,0.8f);
            ObjectAnimator btnShow = ObjectAnimator.ofFloat(btnHelp, "alpha", 0.8f,1.0f);
            btnHide.setDuration(300);
            btnHide.start();
            btnShow.setStartDelay(300);
            btnShow.setDuration(300);
            btnShow.start();
            if(animBtnHelp != null) {
                animBtnHelp.cancel();
                animBtnHelp = ObjectAnimator.ofFloat(btnHelp, "rotationY", 0.0f);
                animBtnHelp.setRepeatCount(0);
                animBtnHelp.setDuration(500);
                animBtnHelp.start();
            }

        }
        public void initLevel(boolean isFirstTime) {
            boolean isComplete = false;
            int currentLevel = Player.getInstance().getLevel();
            if(currentLevel == 22) {
                currentLevel = 21;
                isComplete = true;
            }
            if(isFirstTime) { // если игрок только что прошел все уровни, то готовим особую анимацию
                tvLevel.setAlpha(0);
                for(int i = 0; i < 21; i++) {
                    blockShow = ObjectAnimator.ofFloat(levelBlocks.get(i),"alpha",1f,0f);
                    blockScaleX = ObjectAnimator.ofFloat(levelBlocks.get(i),"scaleX",1f,0f);
                    blockScaleY = ObjectAnimator.ofFloat(levelBlocks.get(i),"scaleY",1f,0f);

                    blockShow.setDuration(0);
                    blockShow.start();

                    blockScaleX.setDuration(0);
                    blockScaleX.start();
                    blockScaleY.setDuration(0);
                    blockScaleY.start();
                }
            }
            for(int i = 0, delay = 0; i < currentLevel; i++, delay += 100) {
                if(i != (currentLevel - 1)) {
                    blockShow = ObjectAnimator.ofFloat(levelBlocks.get(i),"alpha",0f,1f);
                } else if(!isComplete) blockShow = ObjectAnimator.ofFloat(levelBlocks.get(i),"alpha",0f,alphaCurrentLevel);
                else blockShow = ObjectAnimator.ofFloat(levelBlocks.get(i),"alpha",0f,1f);
                blockScaleX = ObjectAnimator.ofFloat(levelBlocks.get(i),"scaleX",0f,1f);
                blockScaleY = ObjectAnimator.ofFloat(levelBlocks.get(i),"scaleY",0f,1f);

                blockShow.setStartDelay(delay);
                blockShow.setDuration(250);
                blockShow.start();

                blockScaleX.setStartDelay(delay);
                blockScaleX.setDuration(250);
                blockScaleX.start();
                blockScaleY.setStartDelay(delay);
                blockScaleY.setDuration(250);
                blockScaleY.start();
            }
            if(Progress.getInstance().getLevel() < 22) {
                String level = Player.getInstance().getLevel() + " " + getResources().getString(R.string.level);
                tvLevel.setText(level);
            } else {
                tvLevel.setText(R.string.complete_level);
                tvLevel.setTextColor(getResources().getColor(R.color.rightAnswer));
                TransitionDrawable transitionDrawable = (TransitionDrawable) imgBackLevel.getDrawable();
                transitionDrawable.startTransition(0);
            }
            if(isFirstTime) {
                ObjectAnimator levelShow = ObjectAnimator.ofFloat(tvLevel,"alpha",0f,1f);
                levelShow.setStartDelay(2100);
                levelShow.setDuration(1000);
                levelShow.start();
                TransitionDrawable transitionDrawable = (TransitionDrawable) imgBackLevel.getDrawable();
                transitionDrawable.startTransition(1000);

                // делаем аниамцию оконтовки
            }
        }
        public void initLeaders() {
            String leaders = StoredData.getDataString(DATA_LEADERS,"0-0-...;0-0-...;0-0-...;0-0-...;"); //0-0-...;0-0-...;0-0-...;0-0-...;0-0-...;
            String[] oneLevelLeadersTemp = leaders.split(";");
            List<String> oneLevelLeaders = Arrays.asList("0-0-...","0-0-...","0-0-...","0-0-...");
            for(int i = 0; i < oneLevelLeadersTemp.length; i++) {
                oneLevelLeaders.set(i,oneLevelLeadersTemp[i]);
            }
            for(int i = 0; i < oneLevelLeaders.size(); i++) {
                playersNames.add(oneLevelLeaders.get(i).split("-")[2]);
                playersCount.add(Integer.valueOf(oneLevelLeaders.get(i).split("-")[1]));
                playersLevels.add(Integer.valueOf(oneLevelLeaders.get(i).split("-")[0]));
                levelsLeaders.get(i).setOnClickListener(levelClickListener);
                namesLeaders.get(i).setOnClickListener(playerClickListener);

                // установка имени первого игрока и кол-ва других игроков на этом уровне
                namesLeaders.get(i).setText(playersNames.get(i));
                setCountOthers(locale,i);

                // установка уровня
                if(playersLevels.get(i) > 0 && playersLevels.get(i) < 22) {
                    levelsLeaders.get(i).setText(playersLevels.get(i) + " " + getString(R.string.lvl));
                } else if (playersLevels.get(i) == 22) {
                    levelsLeaders.get(i).setText(getString(R.string.complete_game_level));
                    lines.get(i).setImageDrawable(getDrawable(R.drawable.winner_line));
                } else {
                    levelsLeaders.get(i).setText(playersLevels.get(i) + " " + getString(R.string.lvl));
                }
            }
            initLeadersAnimation();
        }
        private void setCountOthers(String locale, int index) {
            int countOthers = playersCount.get(index)-1;
            if(locale.equals("en")) {
                if(countOthers > 1) {
                    countLeaders.get(index).setText(getString(R.string.and) + " " + (countOthers) + " " + getString(R.string.people));
                } else if(countOthers == 1) { // когда "еще один игрок"
                    countLeaders.get(index).setText(getString(R.string.and) + " " + getString(R.string.one_person));
                } else {
                    countLeaders.get(index).setText("");
                }
            } else { // если язык русский или схож с русским
                String resultString = getString(R.string.and) + " " + countOthers + " " + getString(R.string.players5_10); // игроков
                if (countOthers == 1 || ((countOthers % 10 == 1) && (countOthers != 11))) { // игрок
                    resultString = getString(R.string.and) + " " + countOthers + " " + getString(R.string.one_person);
                } else if ((countOthers % 10 == 2 || countOthers % 10 == 3 || countOthers % 10 == 4) && (countOthers - countOthers % 10 != 10)) { // игрока
                    resultString = getString(R.string.and) + " " + countOthers + " " + getString(R.string.people);
                } else if (countOthers <= 0) {
                    resultString = "";
                }
                countLeaders.get(index).setText(resultString);
            }
        }
        private void initLeadersAnimation() {
            for(int i = 0, delay = 0; i < namesLeaders.size(); i++, delay += 300) {
                lineShow = ObjectAnimator.ofFloat(lines.get(i),"alpha",0f,1f);
                lineScale = ObjectAnimator.ofFloat(lines.get(i),"scaleX",0f,1f);
                nameShow = ObjectAnimator.ofFloat(namesLeaders.get(i),"alpha",0f,1f);
                anotherShow = ObjectAnimator.ofFloat(countLeaders.get(i),"alpha",0f,1f);
                levelShow = ObjectAnimator.ofFloat(levelsLeaders.get(i),"alpha",0f,1f);
                lines.get(i).setPivotX(0);
                lineScale.setDuration(1000);
                lineScale.setStartDelay(delay);
                lineShow.setStartDelay(delay);
                nameShow.setStartDelay(delay);
                nameShow.setDuration(1500);
                anotherShow.setStartDelay(delay);
                anotherShow.setDuration(1500);
                levelShow.setDuration(1500);
                levelShow.setStartDelay(delay+500);
                lineShow.start();
                lineScale.start();
                nameShow.start();
                anotherShow.start();
                levelShow.start();
            }
        }
        private void increaseLevel(int differLevel) {
            tvLevel.setText(Player.getInstance().getLevel() + " " + getString(R.string.level));
            if(differLevel > 0) {
                int currentLevel = Player.getInstance().getLevel();
                int pastLevel = currentLevel - differLevel;
                boolean isComplete = (currentLevel == 22);
                for(int i = pastLevel-1, delay = 0; i < currentLevel; i++, delay += 200) {
                    if(i != (currentLevel-1)) blockShow = ObjectAnimator.ofFloat(levelBlocks.get(i),"alpha",0f,1f);
                    else if(!isComplete) blockShow = ObjectAnimator.ofFloat(levelBlocks.get(i),"alpha",0f,alphaCurrentLevel);
                    else blockShow = ObjectAnimator.ofFloat(levelBlocks.get(i),"alpha",0f,1f);
                    blockScaleX = ObjectAnimator.ofFloat(levelBlocks.get(i),"scaleX",0f,1f);
                    blockScaleY = ObjectAnimator.ofFloat(levelBlocks.get(i),"scaleY",0f,1f);

                    blockShow.setStartDelay(delay);
                    blockShow.setDuration(400);
                    blockShow.start();

                    blockScaleX.setStartDelay(delay);
                    blockScaleX.setDuration(400);
                    blockScaleX.start();
                    blockScaleY.setStartDelay(delay);
                    blockScaleY.setDuration(400);
                    blockScaleY.start();
                }
            }
            if(Progress.getInstance().getLevel() < 22) {
                tvLevel.setText(Progress.getInstance().getLevel() + " " + getResources().getString(R.string.level));
            } else {
                tvLevel.setText(R.string.complete_level);
                tvLevel.setTextColor(getResources().getColor(R.color.rightAnswer));
            }

        }
        private void changeInfoLeader(int position) {
            // установка имени первого игрока и кол-ва других игроков на этом уровне
            namesLeaders.get(position).setText(playersNames.get(position));
            setCountOthers(locale,position);

            // установка уровня
            if(playersLevels.get(position) > 0 && playersLevels.get(position) < 22) {
                levelsLeaders.get(position).setText(playersLevels.get(position) + " " + getString(R.string.lvl));
            } else if (playersLevels.get(position) == 22) {
                levelsLeaders.get(position).setText(getString(R.string.complete_game_level));
                lines.get(position).setImageDrawable(getDrawable(R.drawable.winner_line));
            } else {
                levelsLeaders.get(position).setText(playersLevels.get(position) + " " + getString(R.string.lvl));
            }
        }
        public void animateCountOther(final int position) {
            anotherHide = ObjectAnimator.ofFloat(countLeaders.get(position),"alpha",1f,0f);
            anotherShow = ObjectAnimator.ofFloat(countLeaders.get(position),"alpha",0f,1f);
            anotherHide.setDuration(800);
            anotherShow.setDuration(800);
            anotherShow.setStartDelay(800);
            anotherHide.start();
            anotherShow.start();
            anotherHide.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    changeInfoLeader(position);
                }
            });
        }
        public void animateChangeLeaders(final int position) {
            nameHide = ObjectAnimator.ofFloat(namesLeaders.get(position),"alpha",1f,0f);
            nameShow = ObjectAnimator.ofFloat(namesLeaders.get(position),"alpha",0f,1f);

            levelHide = ObjectAnimator.ofFloat(levelsLeaders.get(position),"alpha",1f,0f);
            levelShow = ObjectAnimator.ofFloat(levelsLeaders.get(position),"alpha",0f,1f);
            nameHide.setDuration(800);

            nameShow.setDuration(800);

            levelHide.setDuration(800);
            levelShow.setDuration(800);
            nameShow.setStartDelay(800);

            levelShow.setStartDelay(800);
            nameHide.start();

            nameShow.start();

            levelHide.start();
            levelShow.start();
            nameHide.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    changeInfoLeader(position);
                }
            });
        }
    }

    private class CheckForceUpdateTask extends AsyncTask<Void,Void,ResponseFromServer> { // проверяет принудительные обновления

        int typeUpdate; // тип обновления
        private final int FORCE_UPDATE = 6;
        private final int SOFT_UPDATE = 5;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ResponseFromServer doInBackground(Void... voids) {
            try {
                ResponseFromServer responseFromServer = RequestController.Companion
                        .getInstance()
                        .getApiService(MainActivity.this)
                        .checkUpdate(BuildConfig.VERSION_CODE)
                        .execute().body();
                return responseFromServer;
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResponseFromServer response) {
            super.onPostExecute(response);

            if(response != null) {
                switch (response.getResultCode()) {
                    case FORCE_UPDATE: {
                        AssistentDialog updateDialog = new AssistentDialog(AssistentDialog.DIALOG_FORCE_UPDATE);
                        updateDialog.show(getSupportFragmentManager(),"UPDATE");
                        break;
                    }
                }
            }
        }
    }
    private class UpdateDataThread extends Thread { // поток, обновляющий основные данные на главной активити

        private boolean isStop = false;
        private boolean isFirstLaunch = true;
        private List<Leaders> newLeaders;
        private ArrayList<String> newPlayersNames = new ArrayList<>(4);
        private ArrayList<Integer> newPlayersLevels = new ArrayList<>(4);
        private ArrayList<Integer> newPlayersCount = new ArrayList<>(4);

        public UpdateDataThread() { }

        @Override
        public void run() {
            while (true) {
                if(!isStop) {
                    if(!isFirstLaunch) {
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            sleep(3500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        isFirstLaunch = false;
                    }
                    RequestController.Companion // получем приз
                            .getInstance()
                            .getApiService(GetContextClass.getContext())
                            .getPrize("chair",locale)
                            .enqueue(new Callback<Prize>() {
                                @Override
                                public void onResponse(Call<Prize> call, Response<Prize> response) {
                                    try {
                                        Prize prizeResponse = response.body();
                                        String prize; // переменная хранит приз в рублях или долларах в зависимости от языка на устройстве
                                        prize = prizeResponse.getPrize() + " " + getString(R.string.currency_locale);
                                        if(!prize.equals(StoredData.getDataString(StoredData.DATA_PRIZE,GetContextClass.getContext().getResources().getString(R.string.prize)))) {
                                            StoredData.saveData(StoredData.DATA_PRIZE,prize);
                                            tvPrize.setText(prize);
                                            tvPrize.startAnimation(0,1);
                                        }
                                    } catch (NullPointerException ex) {
                                    }
                                }

                                @Override
                                public void onFailure(Call<Prize> call, Throwable t) {

                                }
                            });

                    RequestController.Companion // получем список лидеров
                            .getInstance()
                            .getApiService(GetContextClass.getContext())
                            .getLeaders("please")
                            .enqueue(new Callback<List<Leaders>>() {
                                @Override
                                public void onResponse(Call<List<Leaders>> call, Response<List<Leaders>> response) {

                                    newLeaders = response.body();
                                    if(newLeaders != null) {
                                        List<Leaders> oldLeaders = PreferencesToFromObject.toLeadersList(StoredData.getDataString(DATA_LEADERS,"0-0-...;0-0-...;0-0-...;0-0-...;"));
                                        if(newLeaders.size() > 0 && newLeaders.get(0).isCompleteGame()) newLeaders.get(0).setRiddle(22);
                                        StoredData.saveData(DATA_LEADERS,PreferencesToFromObject.toLeadersString(newLeaders));
                                        for(int i = 0; i < newLeaders.size(); i++) {
                                            newPlayersNames.add(i, newLeaders.get(i).getNickname());
                                            newPlayersCount.add(i, newLeaders.get(i).getCountUsersOnThisRiddle());
                                            newPlayersLevels.add(i, newLeaders.get(i).getRiddle());

                                            // сравниваем лидеров, если на каком то уровне сменилась инфа, то обновялем строчку на главном экране
                                            if(!newPlayersNames.get(i).equals(playersNames.get(i)) ||
                                                    !newPlayersLevels.get(i).equals(playersLevels.get(i))) {
                                                playersNames.set(i,newPlayersNames.get(i));
                                                playersCount.set(i,newPlayersCount.get(i));
                                                playersLevels.set(i,newPlayersLevels.get(i));
                                                animController.animateChangeLeaders(i);
                                            }
                                            if(!newPlayersCount.get(i).equals(playersCount.get(i))) {
                                                playersCount.set(i,newPlayersCount.get(i));
                                                animController.animateCountOther(i);
                                            }
                                        }

                                        int oldLeadersSize = oldLeaders.size();
                                        for(int i = oldLeadersSize-1, j = 0; j < (oldLeadersSize - newLeaders.size()); j++,i--) {
                                            playersNames.set(i,"...");
                                            playersCount.set(i,0);
                                            playersLevels.set(i,0);
                                            animController.animateChangeLeaders(i);
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<Leaders>> call, Throwable t) {}
                            });
                } else return;
            }
        }
        public void toStop() { // остановить поток
            isStop = true;
        }
    }
}
