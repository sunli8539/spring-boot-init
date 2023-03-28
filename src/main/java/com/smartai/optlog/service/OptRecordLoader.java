package com.smartai.optlog.service;

import com.smartai.optlog.vo.ResourceOperateRecordVO;

public interface OptRecordLoader {
    void handlerMsg(ResourceOperateRecordVO var1);

    default void receiveMsg(ResourceOperateRecordVO record) {
        try {
            this.handlerMsg(record);
        } catch (Exception var3) {
        }
    }
}
