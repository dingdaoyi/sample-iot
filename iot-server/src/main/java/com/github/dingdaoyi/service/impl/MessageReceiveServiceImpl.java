package com.github.dingdaoyi.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.dingdaoyi.model.PageResult;
import com.github.dingdaoyi.model.query.MessageRecivePageQuery;
import com.github.dingdaoyi.model.query.ProductPageQuery;
import com.github.dingdaoyi.utils.PageHelper;
import net.dreamlu.mica.core.utils.$;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.dingdaoyi.mapper.MessageReceiveMapper;
import com.github.dingdaoyi.entity.MessageReceive;
import com.github.dingdaoyi.service.MessageReceiveService;
/**
 * @author dingyunwei
 */
@Service
public class MessageReceiveServiceImpl extends ServiceImpl<MessageReceiveMapper, MessageReceive> implements MessageReceiveService{

    @Override
    public PageResult<MessageReceive> pageByQuery(MessageRecivePageQuery pageQuery) {
        return PageHelper.result(page(PageHelper.page(pageQuery),
                Wrappers
                        .<MessageReceive>lambdaQuery()
                        .eq($.isNotBlank(pageQuery.getReceiver()), MessageReceive::getReceiver, pageQuery.getReceiver())
                        .eq($.isNotNull(pageQuery.getNotifyType()), MessageReceive::getNotifyType, pageQuery.getNotifyType())
                        .like($.isNotBlank(pageQuery.getName()), MessageReceive::getName, pageQuery.getName())));
    }

    @Override
    public Boolean deleteById(Integer id) {
        //判断关联关系
        return super.removeById(id);
    }
}