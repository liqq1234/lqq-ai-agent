package com.lqq.lqqaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lqq.lqqaiagent.model.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 消息 Mapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    /**
     * 查询对话的所有消息
     */
    @Select("SELECT * FROM message " +
            "WHERE conversation_id = #{conversationId} AND is_deleted = 0 " +
            "ORDER BY create_time ASC")
    List<Message> selectByConversationId(@Param("conversationId") Long conversationId);

    /**
     * 统计对话的消息数量
     */
    @Select("SELECT COUNT(*) FROM message " +
            "WHERE conversation_id = #{conversationId} AND is_deleted = 0")
    int countByConversationId(@Param("conversationId") Long conversationId);
}
