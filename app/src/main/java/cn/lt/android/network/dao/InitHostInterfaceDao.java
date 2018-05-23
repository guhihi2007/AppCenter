package cn.lt.android.network.dao;

import cn.lt.android.network.bean.HostBean;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Administrator on 2016/2/29.
 */
public interface InitHostInterfaceDao {
    // 测试 acenter_dev.json
    // 正式 acenter.json
    @GET("/acenter.json")
    Call<HostBean> baseHost();
}
