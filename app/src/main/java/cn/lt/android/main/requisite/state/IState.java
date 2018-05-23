package cn.lt.android.main.requisite.state;

import android.app.Dialog;
import android.view.View;
import android.widget.AdapterView;

import java.lang.ref.WeakReference;

/***
 * Created by dxx on 2016/3/10.
 */
public abstract class IState<T> implements View.OnClickListener, AdapterView.OnItemClickListener {

    protected WeakReference<Dialog> mDiaglogWeak;

    protected View mRootView;

    public abstract void setContentView(Dialog dialog);

    public abstract void fillData(T info);

}
