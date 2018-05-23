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

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import java.io.IOException;

import cn.lt.android.network.netdata.analyze.AnalyzeJson;
import cn.lt.android.util.StreamUtil;
import okhttp3.ResponseBody;
import retrofit2.Converter;

final class LTDataResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final TypeAdapter<T> adapter;

    LTDataResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
//    JsonReader jsonReader = gson.newJsonReader(value.charStream());
        String data = StreamUtil.readerToString(value.charStream());
//        Log.i("LTDataConverter", "data:" + data);
        try {
            return (T) AnalyzeJson.analyzeJson(data);//adapter.read(jsonReader);
        } finally {
            value.close();
        }
    }

}
