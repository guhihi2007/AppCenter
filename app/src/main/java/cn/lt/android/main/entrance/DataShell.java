package cn.lt.android.main.entrance;

import java.util.ArrayList;
import java.util.List;

import cn.lt.android.main.entrance.data.ItemData;
import cn.lt.android.main.entrance.data.PresentType;
import cn.lt.android.network.netdata.bean.BaseBean;
import cn.lt.android.network.netdata.bean.BaseBeanList;

/***
 * Created by dxx on 2016/3/7.
 */
@SuppressWarnings("ALL")
public class DataShell {


    public static <T extends BaseBean> List<ItemData<T>> wrapData(List<T> list, int startIndex) {
        List<ItemData<T>> temps = null;
        BigPositionGeter bigPositionGeter = new BigPositionGeter();
        try {
            if (list != null) {
                temps = new ArrayList<>();
                int count = list.size();
                for (int i = 0; i < count; i++) {
                    T module = list.get(i);
                    PresentType type = PresentType.valueOf(module.getLtType());

                    if (module instanceof List) {
                        //                        temps.addAll(splitBaseBeanList((List<BaseBean>)
                        // module, startIndex + i +
                        //                                1));
                        temps.add(wrapBaseBeanList((List<T>) module, module.p1));
                    } else {
                        temps.add(wrapBaseBean((T) module, module.getLtType(), module.p1, -1));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temps;
    }

    /**
     * 处理单个的T对象。
     *
     * @param module 单个待处理数据；
     * @param pos    在网络返回数据中的实际位置（上级位置）；
     * @param subPos 在上一级UIModuleGroup中的位置（下级位置）；如果无下级位置则传-1；
     * @return 处理后的对象；
     */
    public static <T extends BaseBean> ItemData<T> wrapBaseBean(T module, String presentType,
                                                                 int pos, int subPos) {
        ItemData<T> item = null;
        try {
            item = new ItemData<>(module);
            item.setmType(PresentType.valueOf(presentType));
            item.setPos(pos);
            item.setSubPos(subPos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }

    /**
     * 按照要求重新组合module中的元素返回一个新的list集合；
     *
     * @param module List对象；
     * @param pos    在网络返回数据中的实际位置；
     * @return 新的list集合；
     */
    private static <T extends BaseBean> List<ItemData<T>> splitBaseBeanList(List<T> module, int
            pos) {
        List<ItemData<T>> temps = null;
        BigPositionGeter bigPositionGeter = new BigPositionGeter();
        try {
            if (module != null && module.size() > 0) {
                String presentType = ((BaseBeanList) module).getLtType();
                temps = new ArrayList<>();
                int count = module.size();

                for (int i = 0; i < count; i++) {
                    T bean = (T) module.get(i);
                    ItemData<T> item = wrapBaseBean(bean, presentType, pos, i + 1);
                    if (i == 0) {
                        item.setIsFirst(true);
                    }
                    if (i == count - 1) {
                        item.setIsLast(true);
                    }
                    temps.add(item);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temps;
    }

    /**
     * 将一个List对象作为一个整体处理，
     *
     * @param module 单个待处理数据
     * @param pos    在网络返回数据中的实际位置；
     * @return Itemdata
     */
    private static <T extends BaseBean> ItemData<T> wrapBaseBeanList(List<T> module, int pos) {
        ItemData<T> datas = null;
        try {
            if (module != null && module.size() > 0) {
                int count = module.size();
                String presentType = ((BaseBeanList) module).getLtType();
                List<ItemData<T>> group = new BaseBeanList<>(presentType);
                for (int i = 0; i < count; i++) {
                    T o = module.get(i);
                    ItemData<T> tempItem = wrapBaseBean(o, presentType, o.p1, o.p2);
                    if (i == 0) {
                        tempItem.setIsFirst(true);
                    } else if (i == count - 1) {
                        tempItem.setIsLast(true);
                    }
                    group.add(tempItem);
                }
                datas = wrapBaseBean((T) group, presentType, pos, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datas;
    }

}
