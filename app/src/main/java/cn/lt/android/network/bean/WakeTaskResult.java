package cn.lt.android.network.bean;

import cn.lt.android.network.netdata.bean.BaseBean;

/**
 */

public class WakeTaskResult<T> extends BaseBean{
        public long pull_cycle;
        public T tasks;
        public String is_valid;
}
