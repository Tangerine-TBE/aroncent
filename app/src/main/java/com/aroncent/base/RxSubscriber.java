package com.aroncent.base;

import android.content.Context;
import android.os.Handler;

import com.aroncent.R;
import com.kongzue.dialogx.dialogs.WaitDialog;

import io.reactivex.rxjava3.core.Observer;


/**
 * des:订阅封装
 * Created by haiyuan
 * on 2017.7.16
 */

/********************使用例子********************/
/*_apiService.Login(mobile, verifyCode)
        .//省略
        .subscribe(new RxSubscriber<User ic_user>(mContext,false) {
@Override
public void _onNext(User ic_user) {
        // 处理user
        }

@Override
public void _onError(String msg) {
        ToastUtil.showShort(mActivity, msg);
        });*/
public abstract class RxSubscriber<T> implements Observer<T> {

    private Context mContext;
    private String msg;
    private boolean showDialog = true;

    /**
     * 是否显示浮动dialog
     */
    public void showDialog() {
        this.showDialog = true;
    }

    public void hideDialog() {
        this.showDialog = false;
    }

    public RxSubscriber(Context context, String msg, boolean showDialog) {
        this.mContext = context;
        this.msg = msg;
        this.showDialog = showDialog;
        onStart();
    }

    public RxSubscriber(Context context) {
        this(context, context.getString(R.string.please_wait), true);
    }

    public RxSubscriber(Context context, boolean showDialog) {
        this(context, context.getString(R.string.please_wait), showDialog);
    }

    @Override
    public void onComplete() {
        if (showDialog) {
            new Handler().postDelayed(new Runnable() {//延迟加载框的消失
                @Override
                public void run() {
                    WaitDialog.dismiss();
                }
            }, 500);
        }
    }

    public void onStart() {
        if (showDialog) {
            try {
                WaitDialog.show(msg);
            } catch (Exception e) {
                e.printStackTrace();
                WaitDialog.dismiss();
            }
        }
    }


    @Override
    public void onNext(T t) {
        WaitDialog.dismiss();
        _onNext(t);
    }

    @Override
    public void onError(Throwable e) {
        if (showDialog)
            WaitDialog.dismiss();
//        ExtensionsKt.showToast(mContext, ExceptionHandle.Companion.handleException(mContext,e));
        _onError(e.toString());
        e.printStackTrace();
    }

    protected abstract void _onNext(T t);

    protected abstract void _onError(String message);

}
