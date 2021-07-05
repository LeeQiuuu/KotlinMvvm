/**
 * Copyright 2016 bingoogolapple
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jimi.app.mvvm.net.error;

/**
 * 作者:马少杰 邮件:mashaojie@jimi360.cn
 * 创建时间: 2018/10/16 17:08
 * 描述:自定义网络异常
 */
public class ApiException extends RuntimeException {
    /**
     * code为-1的时候，需要自己根据业务逻辑定的参数解析message
     */
    private int mCode;

    public ApiException(String msg, int code) {
        super(msg);
        mCode = code;
    }

    public int getCode() {
        return mCode;
    }

    @Override
    public String toString() {
        return "ApiException{" +
                "code = '" + mCode + '\'' +
                ",msg = '" + getMessage() + '\'' +
                "}";
    }
}