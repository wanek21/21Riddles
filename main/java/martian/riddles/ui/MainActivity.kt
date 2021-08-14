package martian.riddles.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.florent37.viewtooltip.ViewTooltip
import dagger.hilt.android.AndroidEntryPoint
import martian.riddles.R
import martian.riddles.data.local.StoredData
import martian.riddles.dto.Leaders
import martian.riddles.dto.Player
import martian.riddles.ui.*
import martian.riddles.util.Resource
import martian.riddles.util.Status
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var btnNext: Button? = null
    private var tvPrize: TextView? = null
    private var btnHelp: ImageView? = null
    private var tvName: TextView? = null
    private var imgBackLevel: ImageView? = null
    private var tvLevel: TextView? = null

    //private UpdateDataThread updateDataThread;
    //private CheckForceUpdateTask checkForceUpdateTask;
    private var animBtnHelp: ObjectAnimator? = null
    private var locale: String? = null

    /*private val playersNames = ArrayList<String>()
    private val playersLevels = ArrayList<Int>()
    private val playersCount = ArrayList<Int>()*/
    private val DATA_LEADERS = "leaders_list"


    private var leadersController: LeadersController? = null
    private var levelController: LevelController? = null

    private val viewModel: MainActivityViewModel by lazy {
        ViewModelProvider(this).get(MainActivityViewModel::class.java)
    }

    private val leadersObserver = Observer<Resource<ArrayList<Leaders>>> {
        when (it.status) {
            Status.SUCCESS -> {
                if (leadersController == null) {
                    leadersController = LeadersController(it.data)
                }
                leadersController!!.updateLeaders(it.data)
            }
        }
    }
    private val prizeObserver = Observer<Resource<String>> {
        when (it.status) {
            Status.SUCCESS -> {
                val prize = it.data + " " + getString(R.string.currency_locale);
                Log.d("my", "mainactivity prize: $prize")
                tvPrize?.text = prize
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)

        // если юзер еще не зарегался, перенаправляем на другую активити
        if (!viewModel.isLogged()) {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        setContentView(R.layout.activity_main)
        viewModel.upCountLaunchApp() // увеличиваем кол-во звапусков игры на один
        locale = Locale.getDefault().language
        tvName = findViewById(R.id.tvName)
        tvName?.text = viewModel.getNickname()
        imgBackLevel = findViewById(R.id.imgBackLevel)
        tvLevel = findViewById(R.id.tvLevel)
        btnNext = findViewById(R.id.btnNext)
        btnHelp = findViewById(R.id.btnHelp)
        tvPrize = findViewById(R.id.tvPrize)

        btnNext?.setOnClickListener(buttonsClickListener)
        btnHelp?.setOnClickListener(buttonsClickListener)


        levelController = LevelController()

        if (viewModel.getCountCountLaunch() == 2) { // доп. анимации и подсказки, если запуск первый
            ViewTooltip
                .on(this, btnHelp)
                .autoHide(true, 5000)
                .corner(30)
                .position(ViewTooltip.Position.BOTTOM)
                .withShadow(false)
                .text(resources.getString(R.string.read_rules))
                .show()
        }

        viewModel.leaders.observe(this, leadersObserver)
        viewModel.prize.observe(this, prizeObserver)
    }

    override fun onResume() {
        super.onResume()
        setTextForMainButton()
        if (viewModel.getLevel() == 22 &&
            !StoredData.getDataBool(StoredData.DATA_DONE_GAME_ANIM_COMPLETE)
        ) {
            levelController?.initLevel(true)
            StoredData.saveData(StoredData.DATA_DONE_GAME_ANIM_COMPLETE, true)
        }

        // запускаем потоки для обновления данных и проверки принудительных обновлений
        /*updateDataThread = new UpdateDataThread();
        updateDataThread.start();
        checkForceUpdateTask = new CheckForceUpdateTask();
        checkForceUpdateTask.execute();*/
    }

    override fun onPause() {
        super.onPause()
        //updateDataThread.toStop();
    }

    override fun onDestroy() {
        super.onDestroy()
        //if(updateDataThread != null) updateDataThread.toStop();
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            if (data.hasExtra("differ_level")) {
                levelController?.increaseLevel(data.getIntExtra("differ_level", 0))
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private var buttonsClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.btnNext -> {
                val intent: Intent?
                if (viewModel.getLevel() < 22) {
                    intent = Intent(this@MainActivity, RiddlesActivity::class.java)
                    intent.putExtra("past_level", Player.getInstance().level)
                } else if (viewModel.getLevel() == 22) {
                    intent = Intent(this@MainActivity, DoneActivity::class.java)
                } else intent = null
                startActivityForResult(intent, 1)
            }
            R.id.btnHelp -> {
                startActivity(Intent(this@MainActivity, InfoActivity::class.java))
                clickRules()
            }
        }
    }


    private fun setTextForMainButton() {
        when (viewModel.getLevel()) {
            1 -> {
                btnNext!!.text = this@MainActivity.resources.getString(R.string.start_game)
            }
            in 2..21 -> {
                btnNext!!.text = this@MainActivity.resources.getString(R.string.continue_game)
            }
            22 -> {
                btnNext!!.setText(R.string.view_resluts_btn)
            }
        }
    }

    fun clickRules() { // анимация нажатия кнопки с правилами
        val btnHide = ObjectAnimator.ofFloat(btnHelp, View.ALPHA, 1.0f, 0.8f)
        val btnShow = ObjectAnimator.ofFloat(btnHelp, View.ALPHA, 0.8f, 1.0f)
        btnHide.duration = 300
        btnHide.start()
        btnShow.startDelay = 300
        btnShow.duration = 300
        btnShow.start()
        if (animBtnHelp != null) {
            animBtnHelp!!.cancel()
            animBtnHelp = ObjectAnimator.ofFloat(btnHelp, View.ROTATION_Y, 0.0f)
            animBtnHelp?.repeatCount = 0
            animBtnHelp?.duration = 500
            animBtnHelp?.start()
        }
    }

    //---------------------------------------------------------------------------------------------------------------

    // для управления уровнем
    private inner class LevelController {

        // "блоки" одного уровня
        private val levelBlocks = ArrayList<ImageView>(21)

        // анимация для уровня
        var blockShow: ObjectAnimator? = null
        var blockScaleX: ObjectAnimator? = null
        var blockScaleY: ObjectAnimator? = null

        // прозрачность блока для текущего уровня
        private val currentLevelAlpha = 0.5f

        init {
            levelBlocks.add(findViewById<View>(R.id.imgLvl1) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl2) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl3) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl4) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl5) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl6) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl7) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl8) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl9) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl10) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl11) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl12) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl13) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl14) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl15) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl16) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl17) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl18) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl19) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl20) as ImageView)
            levelBlocks.add(findViewById<View>(R.id.imgLvl21) as ImageView)
            initLevel(false)
        }

        fun initLevel(isFirstTime: Boolean) {
            var isComplete = false
            var currentLevel = Player.getInstance().level
            if (currentLevel == 22) {
                currentLevel = 21
                isComplete = true
            }
            if (isFirstTime) { // если игрок только что прошел все уровни, то готовим особую анимацию
                tvLevel!!.alpha = 0f
                for (i in 0..20) {
                    blockShow = ObjectAnimator.ofFloat(levelBlocks[i], "alpha", 1f, 0f)
                    blockScaleX = ObjectAnimator.ofFloat(levelBlocks[i], "scaleX", 1f, 0f)
                    blockScaleY = ObjectAnimator.ofFloat(levelBlocks[i], "scaleY", 1f, 0f)
                    blockShow?.setDuration(0)
                    blockShow?.start()
                    blockScaleX?.setDuration(0)
                    blockScaleX?.start()
                    blockScaleY?.setDuration(0)
                    blockScaleY?.start()
                }
            }
            var i = 0
            var delay = 0
            while (i < currentLevel) {
                blockShow = if (i != currentLevel - 1) {
                    ObjectAnimator.ofFloat(levelBlocks[i], "alpha", 0f, 1f)
                } else if (!isComplete) ObjectAnimator.ofFloat(
                    levelBlocks[i],
                    "alpha",
                    0f,
                    currentLevelAlpha
                ) else ObjectAnimator.ofFloat(
                    levelBlocks[i], "alpha", 0f, 1f
                )
                blockScaleX = ObjectAnimator.ofFloat(levelBlocks[i], "scaleX", 0f, 1f)
                blockScaleY = ObjectAnimator.ofFloat(levelBlocks[i], "scaleY", 0f, 1f)
                blockShow?.setStartDelay(delay.toLong())
                blockShow?.setDuration(250)
                blockShow?.start()
                blockScaleX?.setStartDelay(delay.toLong())
                blockScaleX?.setDuration(250)
                blockScaleX?.start()
                blockScaleY?.setStartDelay(delay.toLong())
                blockScaleY?.setDuration(250)
                blockScaleY?.start()
                i++
                delay += 100
            }
            if (viewModel.getLevel() < 22) {
                val level =
                    Player.getInstance().level.toString() + " " + resources.getString(R.string.level)
                tvLevel!!.text = level
            } else {
                tvLevel!!.setText(R.string.complete_level)
                tvLevel!!.setTextColor(resources.getColor(R.color.rightAnswer))
                val transitionDrawable = imgBackLevel!!.drawable as TransitionDrawable
                transitionDrawable.startTransition(0)
            }
            if (isFirstTime) {
                val levelShow = ObjectAnimator.ofFloat(tvLevel, View.ALPHA, 0f, 1f)
                levelShow.startDelay = 2100
                levelShow.duration = 1000
                levelShow.start()
                val transitionDrawable = imgBackLevel!!.drawable as TransitionDrawable
                transitionDrawable.startTransition(1000)

                // делаем аниамцию оконтовки
            }
        }

        fun increaseLevel(differLevel: Int) {
            tvLevel!!.text = viewModel.getLevel().toString() + " " + getString(R.string.level)
            if (differLevel > 0) {
                val currentLevel = Player.getInstance().level
                val pastLevel = currentLevel - differLevel
                val isComplete = currentLevel == 22
                var i = pastLevel - 1
                var delay = 0
                while (i < currentLevel) {
                    blockShow = if (i != currentLevel - 1) ObjectAnimator.ofFloat(
                        levelBlocks[i],
                        "alpha",
                        0f,
                        1f
                    ) else if (!isComplete) ObjectAnimator.ofFloat(
                        levelBlocks[i], "alpha", 0f, currentLevelAlpha
                    ) else ObjectAnimator.ofFloat(
                        levelBlocks[i], "alpha", 0f, 1f
                    )
                    blockScaleX = ObjectAnimator.ofFloat(levelBlocks[i], "scaleX", 0f, 1f)
                    blockScaleY = ObjectAnimator.ofFloat(levelBlocks[i], "scaleY", 0f, 1f)
                    blockShow?.startDelay = delay.toLong()
                    blockShow?.duration = 400
                    blockShow?.start()
                    blockScaleX?.startDelay = delay.toLong()
                    blockScaleX?.duration = 400
                    blockScaleX?.start()
                    blockScaleY?.startDelay = delay.toLong()
                    blockScaleY?.duration = 400
                    blockScaleY?.start()
                    i++
                    delay += 200
                }
            }
            if (viewModel.getLevel() < 22) {
                tvLevel?.text =
                    viewModel.getLevel().toString() + " " + resources.getString(R.string.level)
            } else {
                tvLevel?.setText(R.string.complete_level)
                tvLevel?.setTextColor(resources.getColor(R.color.rightAnswer))
            }
        }
    }

    // для управления таблицой лидеров
    private inner class LeadersController() {

        private val countRows = 4; // кол-во строк в таблице лидеров
        // data
        private var leadersData = ArrayList<Leaders>()

        // view's
        private val leadersNicknames = ArrayList<TextView>() // никнейм лидера на уровне
        private val leadersLevels = ArrayList<TextView>() // уровень, на котором находятся лидеры
        private val countLeaders = ArrayList<TextView>() // кол-во игроков на этом уровне
        private val lines = ArrayList<ImageView>() // декоративные линии

        // объекты анимации
        var nameHide: ObjectAnimator? = null
        var nameShow: ObjectAnimator? = null
        var anotherShow: ObjectAnimator? = null
        var anotherHide: ObjectAnimator? = null
        var levelHide: ObjectAnimator? = null
        var levelShow: ObjectAnimator? = null
        var lineShow: ObjectAnimator? = null
        var lineScale: ObjectAnimator? = null

        constructor(ld: ArrayList<Leaders>?) : this() {
            if (ld != null) {
                leadersData = ld
            }

            // просто устанвливаем значения в таблицу лидеров
            for (i in 0 until leadersData.size) {
                setNicknameValue(i)
                setCountOthers(locale, i)
                setLevel(i)
            }

            // анимация таблицы при заходе в приложение
            initAnimation()
        }

        // обработчик нажатий никнеймов
        var nameClickListener = View.OnClickListener { v ->
            if (v.id != R.id.tvNameLeader1) {
                ViewTooltip
                    .on(this@MainActivity, v)
                    .autoHide(true, 5000)
                    .corner(30)
                    .position(ViewTooltip.Position.BOTTOM)
                    .withShadow(false)
                    .text(resources.getString(R.string.info_player))
                    .show()
            } else {
                ViewTooltip
                    .on(this@MainActivity, v)
                    .autoHide(true, 5000)
                    .corner(30)
                    .position(ViewTooltip.Position.BOTTOM)
                    .withShadow(false)
                    .text(
                        if (leadersData[0].riddle == 22) resources.getString(R.string.info_first_player_complete)
                        else resources.getString(R.string.info_first_player)
                    )
                    .show()
            }
        }
        // обработчик нажатий уровней
        var levelClickListener = View.OnClickListener { v ->
            if (v.id != R.id.tvLevel1) {
                ViewTooltip
                    .on(this@MainActivity, v)
                    .autoHide(true, 5000)
                    .corner(30)
                    .position(ViewTooltip.Position.BOTTOM)
                    .withShadow(false)
                    .text(resources.getString(R.string.current_level_info))
                    .show()
            } else if (leadersData[0].riddle != 22) {
                ViewTooltip
                    .on(this@MainActivity, v)
                    .autoHide(true, 5000)
                    .corner(30)
                    .position(ViewTooltip.Position.BOTTOM)
                    .withShadow(false)
                    .text(resources.getString(R.string.current_level_info))
                    .show()
            }
        }

        init {
            leadersNicknames.add(findViewById<View>(R.id.tvNameLeader1) as TextView)
            leadersNicknames.add(findViewById<View>(R.id.tvNameLeader2) as TextView)
            leadersNicknames.add(findViewById<View>(R.id.tvNameLeader3) as TextView)
            leadersNicknames.add(findViewById<View>(R.id.tvNameLeader4) as TextView)
            countLeaders.add(findViewById<View>(R.id.tvAnotherPlayers) as TextView)
            countLeaders.add(findViewById<View>(R.id.tvAnotherPlayers2) as TextView)
            countLeaders.add(findViewById<View>(R.id.tvAnotherPlayers3) as TextView)
            countLeaders.add(findViewById<View>(R.id.tvAnotherPlayers4) as TextView)
            leadersLevels.add(findViewById<View>(R.id.tvLevel1) as TextView)
            leadersLevels.add(findViewById<View>(R.id.tvLevel2) as TextView)
            leadersLevels.add(findViewById<View>(R.id.tvLevel3) as TextView)
            leadersLevels.add(findViewById<View>(R.id.tvLevel4) as TextView)
            lines.add(findViewById<View>(R.id.underLine1) as ImageView)
            lines.add(findViewById<View>(R.id.underLine2) as ImageView)
            lines.add(findViewById<View>(R.id.underLine3) as ImageView)
            lines.add(findViewById<View>(R.id.underLine4) as ImageView)

            // установка обработчиков нажатий
            for(i in 0 until countRows) {
                leadersNicknames[i].setOnClickListener(nameClickListener)
                leadersLevels[i].setOnClickListener(levelClickListener)
            }
        }

        // анимация таблицы лидеров при открытии активити
        fun initAnimation() {
            var i = 0
            var delay = 0
            while (i < leadersNicknames.size) {
                lineShow = ObjectAnimator.ofFloat(lines[i], "alpha", 0f, 1f)
                lineScale = ObjectAnimator.ofFloat(lines[i], "scaleX", 0f, 1f)
                nameShow = ObjectAnimator.ofFloat(leadersNicknames[i], "alpha", 0f, 1f)
                anotherShow = ObjectAnimator.ofFloat(countLeaders[i], "alpha", 0f, 1f)
                levelShow = ObjectAnimator.ofFloat(leadersLevels[i], "alpha", 0f, 1f)
                lines[i].pivotX = 0f
                lineScale?.duration = 1000
                lineScale?.startDelay = delay.toLong()
                lineShow?.startDelay = delay.toLong()
                nameShow?.startDelay = delay.toLong()
                nameShow?.duration = 1500
                anotherShow?.startDelay = delay.toLong()
                anotherShow?.duration = 1500
                levelShow?.duration = 1500
                levelShow?.startDelay = (delay + 500).toLong()
                lineShow?.start()
                lineScale?.start()
                nameShow?.start()
                anotherShow?.start()
                levelShow?.start()
                i++
                delay += 300
            }
        }

        fun updateLeaders(newLeaders: ArrayList<Leaders>?) {
            if (newLeaders != null) {

                Log.d("my", "updating leaders")
                // проходимся по текущим данным, если что-то не совпадает с новыми данными, обновляем
                for ((i, row) in leadersData.withIndex()) {
                    if (row.nickname != newLeaders[i].nickname) {
                        leadersData[i] = newLeaders[i]
                        animateNickname(i)
                    }
                    if (row.riddle != newLeaders[i].riddle) {
                        leadersData[i] = newLeaders[i]
                        animateLevel(i)
                    }
                    if (row.countUsersOnThisRiddle != newLeaders[i].countUsersOnThisRiddle) {
                        leadersData[i] = newLeaders[i]
                        animateCountOther(i)
                    }
                }
            }
        }

        // анимация изменения никнейма
        private fun animateNickname(position: Int) {
            nameHide = ObjectAnimator.ofFloat(leadersNicknames[position], "alpha", 1f, 0f)
            nameShow = ObjectAnimator.ofFloat(leadersNicknames[position], "alpha", 0f, 1f)

            nameHide?.duration = 800
            nameShow?.duration = 800

            nameShow?.startDelay = 800

            nameHide?.start()
            nameShow?.start()

            nameHide?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    setNicknameValue(position)
                }
            })
        }

        // само изменение никнейма
        private fun setNicknameValue(position: Int) {
            leadersNicknames[position].text = leadersData[position].nickname
        }


        // анимация изменения уровня
        private fun animateLevel(position: Int) {
            levelHide = ObjectAnimator.ofFloat(leadersLevels[position], "alpha", 1f, 0f)
            levelShow = ObjectAnimator.ofFloat(leadersLevels[position], "alpha", 0f, 1f)
            levelHide?.duration = 800
            levelShow?.duration = 800
            levelShow?.startDelay = 800
            levelHide?.start()
            levelShow?.start()

            levelHide?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    setLevel(position)
                }
            })
        }

        // само изменение уровня
        private fun setLevel(position: Int) {
            if (leadersData[position].riddle in 1..21) { // на
                leadersLevels[position].text =
                    leadersData[position].riddle.toString() + " " + getString(R.string.lvl)
            } else if (leadersData[position].riddle == 22) { // прошел игру
                leadersLevels[position].text = getString(R.string.complete_game_level)
                lines[position].setImageDrawable(getDrawable(R.drawable.winner_line))
            } else {
                leadersLevels[position].text =
                    leadersData[position].riddle.toString() + " " + getString(R.string.lvl)
            }
            //TODO("возможно последний else можно будет удалить, надо чекнуть потом")
        }

        // анимация изменения кол-ва игроков на уровне
        private fun animateCountOther(position: Int) {
            anotherHide = ObjectAnimator.ofFloat(countLeaders[position], "alpha", 1f, 0f)
            anotherShow = ObjectAnimator.ofFloat(countLeaders[position], "alpha", 0f, 1f)
            anotherHide?.duration = 800
            anotherShow?.duration = 800
            anotherShow?.startDelay = 800
            anotherHide?.start()
            anotherShow?.start()
            anotherHide?.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    setCountOthers(locale, position)
                }
            })
        }

        // само изменение кол-ва игроков на уровне
        private fun setCountOthers(locale: String?, position: Int) {
            val countOthers = leadersData[position].countUsersOnThisRiddle - 1
            if (locale == "en") {
                if (countOthers > 1) {
                    countLeaders[position].text =
                        getString(R.string.and) + " " + countOthers + " " + getString(R.string.people)
                } else if (countOthers == 1) { // когда "еще один игрок"
                    countLeaders[position].text =
                        getString(R.string.and) + " " + getString(R.string.one_person)
                } else {
                    countLeaders[position].text = ""
                }
            } else { // если язык русский или схож с русским
                var resultString =
                    getString(R.string.and) + " " + countOthers + " " + getString(R.string.players5_10) // игроков
                if (countOthers == 1 || countOthers % 10 == 1 && countOthers != 11) { // игрок
                    resultString =
                        getString(R.string.and) + " " + countOthers + " " + getString(R.string.one_person)
                } else if ((countOthers % 10 == 2 || countOthers % 10 == 3 || countOthers % 10 == 4) && countOthers - countOthers % 10 != 10) { // игрока
                    resultString =
                        getString(R.string.and) + " " + countOthers + " " + getString(R.string.people)
                } else if (countOthers <= 0) {
                    resultString = ""
                }
                countLeaders[position].text = resultString
            }
        }
    }
/*private class CheckForceUpdateTask extends AsyncTask<Void,Void,ResponseFromServer> { // проверяет принудительные обновления

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
    */
}