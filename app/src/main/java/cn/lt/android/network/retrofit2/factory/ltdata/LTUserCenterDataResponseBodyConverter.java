/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.lt.android.network.retrofit2.factory.ltdata;


import java.io.IOException;

import cn.lt.android.network.netdata.analyze.AnalyzeJson;
import cn.lt.android.util.LogUtils;
import cn.lt.android.util.StreamUtil;
import okhttp3.ResponseBody;
import retrofit2.Converter;

final class LTUserCenterDataResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    @Override
    public T convert(ResponseBody value) throws IOException {
//    JsonReader jsonReader = gson.newJsonReader(value.charStream());
        String data = StreamUtil.readerToString(value.charStream());
        LogUtils.i("LTDataConverter", "data:" + data);
//        LogUtils.i("getGenericSuperclass", "type:" + adapter.getClass().getGenericSuperclass());
        try {
            return (T) AnalyzeJson.parseUserCenterData(data);//adapter.read(jsonReader);
//        } catch (JSONException e) {
//            return null;
        } finally {
            value.close();
        }
    }
}
