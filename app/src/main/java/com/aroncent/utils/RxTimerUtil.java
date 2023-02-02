package com.aroncent.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;


/**
 * Instruction:Rxjava2.x实现定时器
 * <p>
 * Author:pei
 * Date: 2017/6/29
 * Description:
 */

public class RxTimerUtil {

    private static Disposable mDisposable;

    /** milliseconds毫秒后执行next操作
     *
     * @param milliseconds
     * @param next
     */
    public static void timer(long milliseconds,final IRxNext next) {
        Observable.timer(milliseconds, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable disposable) {
                        mDisposable=disposable;
                    }

                    @Override
                    public void onNext(@NonNull Long number) {
                        if(next!=null){
                            next.doNext();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        //取消订阅
                        cancel();
                    }

                    @Override
                    public void onComplete() {
                        //取消订阅
                        cancel();
                    }
                });
    }


    /** 每隔milliseconds毫秒后执行next操作
     *
     * @param milliseconds
     * @param next
     */
    public static void interval(long milliseconds,final IRxNext next){
        Observable.interval(milliseconds, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable disposable) {
                        mDisposable=disposable;
                    }

                    @Override
                    public void onNext(@NonNull Long number) {
                        if(next!=null){
                            next.doNext();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    /**
     * 取消订阅
     */
    public static void cancel(){
        if(mDisposable!=null&&!mDisposable.isDisposed()){
            mDisposable.dispose();
            Log.e("mDisposable","====定时器取消======");
        }
    }
    /**
     * 取消订阅
     */
    public static void cancel(String tag){
        if(mDisposable!=null&&!mDisposable.isDisposed()){
            mDisposable.dispose();
            Log.e("mDisposable","====定时器取消======"+tag);
        }
    }

    public interface IRxNext{
        void doNext();
    }
}
