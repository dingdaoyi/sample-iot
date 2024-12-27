package com.github.dingdaoyi.service;

import com.github.dingdaoyi.entity.MessageReceive;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.dingdaoyi.model.PageResult;
import com.github.dingdaoyi.model.query.MessageRecivePageQuery;
import com.github.dingdaoyi.model.query.ProductPageQuery;

/**
 * @author dingyunwei
 */
public interface MessageReceiveService extends IService<MessageReceive>{


    /**
     * 分页查询
     * @param pageQuery
     * @return
     */
    PageResult<MessageReceive> pageByQuery(MessageRecivePageQuery pageQuery);

    Boolean deleteById(Integer id);
}