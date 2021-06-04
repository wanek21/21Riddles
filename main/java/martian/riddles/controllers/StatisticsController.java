package martian.riddles.controllers;


import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import martian.riddles.model.Player;


public class StatisticsController {

    private FirebaseAnalytics mFirebaseAnalytics;
    private Context context;

    public StatisticsController(Context context) {
        this.context = context;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    private int getLongOfLevel() { // получить время прохождения уровня
        SimpleDateFormat format = new SimpleDateFormat("hh dd MM yyyy");
        Date nowDate = new Date();
        String nowDateString = format.format(nowDate);
        String startTime = StoredData.getDataString(StoredData.DATA_START_TIME, "1");
        try {
            Date oldDate = format.parse(startTime);
            Date newDate = format.parse(nowDateString);
            int diffInDays = (int) ((newDate.getTime() - oldDate.getTime())
                    / (1000 * 60 * 60));
            return diffInDays;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void setStartTimeLevel() { // установить время начала прохождения уровня
        SimpleDateFormat format = new SimpleDateFormat("hh dd MM yyyy");
        Date nowDate = new Date();
        String nowDateString = format.format(nowDate);
        StoredData.saveData(StoredData.DATA_START_TIME, nowDateString);
    }

    // отправка статистики на сервер
    public void sendAttempt(boolean endlessAttempts) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Level: " + Player.getInstance().getLevel());
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, endlessAttempts ? 0 : 1);
        bundle.putString(FirebaseAnalytics.Param.VIRTUAL_CURRENCY_NAME, "attempt");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SPEND_VIRTUAL_CURRENCY, bundle);
    }

    public void sendPurchase(int countWrongAttempts) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Count wrong attempts");
        bundle.putInt(FirebaseAnalytics.Param.VALUE, countWrongAttempts);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.ECOMMERCE_PURCHASE, bundle);
    }

    public void sendErrorAd(int errorCode) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Error showing ad");
        bundle.putInt(FirebaseAnalytics.Param.VALUE, errorCode);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.CAMPAIGN_DETAILS, bundle);
    }

    public void signUp() {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SIGN_UP_METHOD,"Sign up");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Param.SIGN_UP_METHOD,bundle);
    }

    public void joinGroup() { // не совсем вступление в группу, а просто переход в нее
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.GROUP_ID, "Telegram");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.JOIN_GROUP, bundle);
    }

    public void earnAttempt(int count) {
        Bundle bundle = new Bundle();
        bundle.putDouble(FirebaseAnalytics.Param.VALUE, count);
        bundle.putString(FirebaseAnalytics.Param.VIRTUAL_CURRENCY_NAME, "Attempts");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.EARN_VIRTUAL_CURRENCY, bundle);
    }

    public void sendNewLevel(int level) {
        // отправляем данные в Firebase
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.CHARACTER, "Some riddle");
        bundle.putLong(FirebaseAnalytics.Param.LEVEL, level);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle);
    }
}
