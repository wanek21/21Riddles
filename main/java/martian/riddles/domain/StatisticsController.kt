package martian.riddles.domain

import android.content.Context
import martian.riddles.data.repositories.UsersRepository

//import com.google.firebase.analytics.FirebaseAnalytics;

class StatisticsController     //mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    (  // private FirebaseAnalytics mFirebaseAnalytics;
    private val context: Context,
    private val usersRepository: UsersRepository
) {
    fun setStartTimeLevel() { // установить время начала прохождения уровня
        /*SimpleDateFormat format = new SimpleDateFormat("hh dd MM yyyy");
        Date nowDate = new Date();
        String nowDateString = format.format(nowDate);
        StoredData.saveData(StoredData.DATA_START_TIME, nowDateString);*/
    }

    fun sendAttempt(endlessAttempts: Boolean) {
        /*Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Level: " + user); // TODO remove Player.getInstance()
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, endlessAttempts ? 0 : 1);
        bundle.putString(FirebaseAnalytics.Param.VIRTUAL_CURRENCY_NAME, "attempt");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SPEND_VIRTUAL_CURRENCY, bundle);*/
    }

    fun sendPurchase(countWrongAttempts: Int) {
        /*Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Count wrong attempts");
        bundle.putInt(FirebaseAnalytics.Param.VALUE, countWrongAttempts);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.ECOMMERCE_PURCHASE, bundle);*/
    }

    fun sendErrorAd(errorCode: Int) {
        /*Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Error showing ad");
        bundle.putInt(FirebaseAnalytics.Param.VALUE, errorCode);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.CAMPAIGN_DETAILS, bundle);*/
    }

    fun signUp() {
        /*Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SIGN_UP_METHOD,"Sign up");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Param.SIGN_UP_METHOD,bundle);*/
    }

    fun joinGroup() { // не совсем вступление в группу, а просто переход в нее
        /*Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.GROUP_ID, "Telegram");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.JOIN_GROUP, bundle);*/
    }

    fun earnAttempt(count: Int) {
        /*Bundle bundle = new Bundle();
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, count);
        bundle.putString(FirebaseAnalytics.Param.VIRTUAL_CURRENCY_NAME, "Attempts");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.EARN_VIRTUAL_CURRENCY, bundle);*/
    }

    fun sendNewLevel(level: Int) {
        // отправляем данные в Firebase
        /*Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CHARACTER, "Some riddle");
        bundle.putLong(FirebaseAnalytics.Param.LEVEL, level);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle);*/
    }
}