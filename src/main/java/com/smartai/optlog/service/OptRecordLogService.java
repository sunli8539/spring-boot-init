package com.smartai.optlog.service;

import com.alibaba.fastjson.JSON;
import com.smartai.common.util.SpringBeanUtils;
import com.smartai.optlog.entity.ResourceOperateRecord;
import com.smartai.optlog.mapper.ResourceOperateRecordMapper;
import com.smartai.optlog.vo.ResourceOperateRecordVO;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class OptRecordLogService implements OptRecordLoader {

    @Override
    public void handlerMsg(ResourceOperateRecordVO resourceOperateRecordVO) {
        if (Objects.isNull(resourceOperateRecordVO)) {
            return;
        }
        try {
            ResourceOperateRecord record = new ResourceOperateRecord();
            BeanUtils.copyProperties(resourceOperateRecordVO, record);
            ResourceOperateRecordMapper recordMapper = SpringBeanUtils.getBean(ResourceOperateRecordMapper.class);
            recordMapper.insert(record);
        } catch (Exception e) {
            log.error("OptRecordLogService.handlerMsg error :{}, param :{}", e,
                JSON.toJSONString(resourceOperateRecordVO));
        }
    }
}
