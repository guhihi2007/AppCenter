package cn.lt.android.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import cn.lt.android.util.LogUtils;
import cn.lt.appstore.R;

/**
 * Created by wenchao on 2016/3/16.
 * 公用对话框
 */
public class CustomDialog extends Dialog implements CollapsingView.CollapseListener {
    private static final String TAG = "CustomDialog";

    private Builder mBuilder;

    private Listener mListener;

    private int mWhich = Integer.MIN_VALUE;

    private final Runnable dismissRunnable = new Runnable() {
        @Override
        public void run() {
            dismiss();
        }
    };


    private CustomDialog(Context context, Builder builder) {
        super(context, builder.style);
        mBuilder = builder;
        mListener = builder.listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!canCreateSheet()) {
            throw new IllegalStateException("Unable to create BottomSheet, missing params");
        }

        Window  window   = getWindow();
        int     width    = 0;
        boolean isTablet = width > 0;

        if (window != null) {
            window.setLayout(width <= 0 ? ViewGroup.LayoutParams.MATCH_PARENT : width, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
        } else {
            LogUtils.e(TAG, "Window came back as null, unable to set defaults");
        }


        if (mBuilder.view != null) {
            initViewLayout();
        } else if (!TextUtils.isEmpty(mBuilder.message)) {
            initMessageLayout();
        }
        if (mListener != null) mListener.onShown();

        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(mBuilder.onDismissListener != null) {
                    mBuilder.onDismissListener.onDismiss(dialog);
                }
            }
        });
    }

    @Override
    public void onCollapse() {
        // Post a runnable for dismissing to avoid "Attempting to destroy the window while drawing!" error
        if (getWindow() != null && getWindow().getDecorView() != null) {
            getWindow().getDecorView().post(dismissRunnable);
        } else {
            dismiss();
        }
    }
    private boolean canCreateSheet() {
        return mBuilder != null
                || mBuilder.view != null
                || !TextUtils.isEmpty(mBuilder.message);
    }

    /**
     * Initializes the layout for custom view
     *
     */
    private void initViewLayout() {
        CollapsingView collapsingView = new CollapsingView(getContext());
        collapsingView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        collapsingView.setCollapseListener(this);
        collapsingView.addView(mBuilder.view);
        setContentView(collapsingView);
    }

    private void initMessageLayout() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_custom, null);
        ((CollapsingView) view).setCollapseListener(this);


        TextView message = (TextView) view.findViewById(R.id.message);
        message.setText(mBuilder.message);

        if (!TextUtils.isEmpty(mBuilder.positiveBtn)) {
            Button positive = (Button) view.findViewById(R.id.positive);
            positive.setText(mBuilder.positiveBtn);
            positive.setVisibility(View.VISIBLE);
            positive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mBuilder.positiveListener!=null){
                        mBuilder.positiveListener.onClick(view);
                    }
                    mWhich = Dialog.BUTTON_POSITIVE;
                    dismiss();
                }
            });

        }

        if (!TextUtils.isEmpty(mBuilder.negativeBtn)) {
            Button negative = (Button) view.findViewById(R.id.negative);
            negative.setText(mBuilder.negativeBtn);
            negative.setVisibility(View.VISIBLE);
            negative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mBuilder.negativeListener!=null){
                        mBuilder.negativeListener.onClick(view);
                    }
                    mWhich = Dialog.BUTTON_NEGATIVE;
                    dismiss();
                }
            });

        }
        setContentView(view);
    }

    @Override
    public void dismiss() {
        if (mListener != null) mListener.onDismiss(mWhich);
        super.dismiss();
    }

    public interface Listener {
        void onShown();

        void onDismiss(int which);
    }

    /**
     * Builder factory used for creating
     */
    public static class Builder {
        @StyleRes
        int style = R.style.BottomDialogStyle;


        String title = null;

        boolean cancelable = true;


        Context context;

        Resources resources;

        Listener listener;


        @Nullable
        View view;


        String message;

        String neutralBtn;

        String negativeBtn;

        String positiveBtn;

        View.OnClickListener negativeListener,positiveListener;

        OnDismissListener onDismissListener;


        /**
         * Constructor for creating a
         *
         * @param context App context
         */
        public Builder(Context context) {
            this(context, R.style.BottomDialogStyle);
        }

        /**
         * Constructor for creating a
         *
         * @param context App context
         * @param style   The style the  will use
         */
        public Builder(Context context, @StyleRes int style) {
            this.context = context;
            this.style = style;
            this.resources = context.getResources();
        }

        /**
         * Sets the title of the
         *
         * @param title String for the title
         * @return
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Sets the title of the
         *
         * @param title String resource for the title
         * @return
         */
        public Builder setTitle(@StringRes int title) {
            return setTitle(resources.getString(title));
        }


        /**
         * Sets whether the  is cancelable with the {@link KeyEvent#KEYCODE_BACK BACK} key.
         *
         * @param cancelable
         * @return
         */
        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        /**
         * Sets the  to receive callbacks
         *
         * @param listener
         * @return
         */
        public Builder setListener(Listener listener) {
            this.listener = listener;
            return this;
        }


        /**
         * Sets the style of the
         *
         * @param style
         * @return
         */
        public Builder setStyle(@StyleRes int style) {
            this.style = style;
            return this;
        }


        /**
         * Sets the view the  will show. If called, any attempt to add menu items or show a simgple message will be ignored
         *
         * @param view The view to display
         * @return
         */
        public Builder setView(View view) {
            this.view = view;
            return this;
        }

        /**
         * Sets the view the  will show. If called, any attempt to add menu items or show a simgple message will be ignored
         *
         * @param view The view resource to display
         * @return
         */
        public Builder setView(@LayoutRes int view) {
            return setView(LayoutInflater.from(context).inflate(view, null));
        }

        /**
         * Sets the message to be used for the .
         * This parameter will be ignored if a {@link View} is supplied to {@link #setView(View)}
         *
         * @param message Message to use
         * @return
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * Sets the message to be used for the .
         * This parameter will be ignored if a {@link View} is supplied to {@link #setView(View)}
         *
         * @param message Message resource
         * @return
         */
        public Builder setMessage(@StringRes int message) {
            return setMessage(resources.getString(message));
        }

        /**
         * Sets the text of the positive button for a message
         * This parameter will be ignored if a {@link View} is supplied to {@link #setView(View)}
         *
         * @param positiveButton String of the positive button
         * @return
         */
        public Builder setPositiveButton(String positiveButton) {
            this.positiveBtn = positiveButton;
            return this;
        }

        /**
         * Sets the text of the positive button for a message
         * This parameter will be ignored if a {@link View} is supplied to {@link #setView(View)}
         *
         * @param positiveButton String resource of the positive button
         * @return
         */
        public Builder setPositiveButton(@StringRes int positiveButton) {
            return setPositiveButton(resources.getString(positiveButton));
        }

        public Builder setPositiveListener(View.OnClickListener onClickListener){
            positiveListener = onClickListener;
            return this;
        }

        /**
         * Sets the text of the negative button for a message
         * This parameter will be ignored if a {@link View} is supplied to {@link #setView(View)}
         *
         * @param negativeButton String of the negative button
         * @return
         */
        public Builder setNegativeButton(String negativeButton) {
            this.negativeBtn = negativeButton;
            return this;
        }

        /**
         * Sets the text of the negative button for a message
         * This parameter will be ignored if a {@link View} is supplied to {@link #setView(View)}
         *
         * @param negativeButton String resource of the negative button
         * @return
         */
        public Builder setNegativeButton(@StringRes int negativeButton) {
            return setNegativeButton(resources.getString(negativeButton));
        }


        public Builder setNegativeListener(View.OnClickListener onClickListener){
            negativeListener = onClickListener;
            return this;
        }
        /**
         * Sets the text of the neutral button for a message
         * This parameter will be ignored if a {@link View} is supplied to {@link #setView(View)}
         *
         * @param neutralButton String neutral of the negative button
         * @return
         */
        public Builder setNeutralButton(String neutralButton) {
            this.neutralBtn = neutralButton;
            return this;
        }

        /**
         * Sets the text of the neutral button for a message
         * This parameter will be ignored if a {@link View} is supplied to {@link #setView(View)}
         *
         * @param neutralButton String resource neutral of the negative button
         * @return
         */
        public Builder setNeutralButton(@StringRes int neutralButton) {
            return setNeutralButton(resources.getString(neutralButton));
        }


        /**
         * Creates the  but does not show it.
         *
         * @return
         */
        public CustomDialog create() {
            return new CustomDialog(context, this);
        }

        /**
         * Creates the  and shows it.
         */
        public void show() {
            create().show();
        }

        public Builder setOnDismissListener(OnDismissListener onDismissListener) {
            this.onDismissListener = onDismissListener;
            return this;
        }

    }
}

