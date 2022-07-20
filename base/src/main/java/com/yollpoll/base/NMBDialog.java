package com.yollpoll.base;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.Window;

import androidx.databinding.DataBindingUtil;
import androidx.viewbinding.ViewBinding;

import com.yollpoll.framework.widgets.BaseDialog;

public abstract class NMBDialog<BIND extends ViewBinding, T extends Dialog> implements DialogInterface {
    private T mDialog;
    protected BIND binding;
    protected int layoutId;
    protected Context mContext;

    public BIND getBinding() {
        return binding;
    }

    public void setBinding(BIND binding) {
        this.binding = binding;
    }

    public NMBDialog(Context mContext) {
        this.mContext = mContext;
    }

    protected T getDialog() {
        return mDialog;
    }

    public void show() {
        if (mDialog == null) {
            //新建dialog
            mDialog = createDialog(mContext);
            //设置动画
            Window window = mDialog.getWindow();
            window.setBackgroundDrawable(mContext.getDrawable(R.drawable.shape_dialog));
//            window.setWindowAnimations(R.style.DialogWindowAnim);
            //设置回调
            mDialog.setOnDismissListener(dialog -> onDialogDismiss(mDialog));
            mDialog.setOnShowListener(dialog -> onDialogShow(mDialog));
            //show
            mDialog.show();
            //设置contentView
            layoutId = getLayoutId();
            binding = (BIND) DataBindingUtil.inflate(LayoutInflater.from(mContext), layoutId, null, false);
            mDialog.setContentView(binding.getRoot());
            //单独处理dialog中的事件
            onInit(mDialog, binding);
            return;
        }
        if (!mDialog.isShowing()) {
            mDialog.show();
        }

    }

    @Override
    public void cancel() {
        mDialog.cancel();
    }

    @Override
    public void dismiss() {
        mDialog.dismiss();
    }


    public abstract int getLayoutId();

    //创建
    public abstract T createDialog(Context context);

    //初始化
    public void onInit(T dialog, BIND binding) {
    }

    public void onDialogDismiss(T dialog) {
    }

    public void onDialogShow(T dialog) {
    }

}
