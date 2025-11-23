package com.lqq.lqqaiagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lqq.lqqaiagent.model.entity.Conversation;
import com.lqq.lqqaiagent.model.vo.ConversationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 对话 Mapper
 */
@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {

    /**
     * 查询用户的对话列表（批量查询消息数量和最后一条消息）
     *
     * - 使用单条 SQL 通过 LEFT JOIN + 聚合函数一次性查出：
     *   - 对话基础信息
     *   - 消息数量 messageCount
     *   - 最后一条消息内容 lastMessage
     */
    @Select("SELECT "+
            "  c.id, "+
            "  c.title, "+
            "  c.create_time, "+
            "  c.update_time, "+
            "  COUNT(m.id) AS message_count, "+
            "  (SELECT m2.content FROM message m2 " +
            "   WHERE m2.conversation_id = c.id AND m2.is_deleted = 0 " +
            "   ORDER BY m2.create_time DESC LIMIT 1) AS last_message " +
            "FROM conversation c " +
            "LEFT JOIN message m ON m.conversation_id = c.id AND m.is_deleted = 0 " +
            "WHERE c.user_id = #{userId} AND c.is_deleted = 0 " +
            "GROUP BY c.id, c.title, c.create_time, c.update_time " +
            "ORDER BY c.update_time DESC")
    List<ConversationVO> listUserConversations(@Param("userId") Long userId);

    /**
     * 更新对话标题
     */
    @Update("UPDATE conversation SET title = #{title} WHERE id = #{conversationId}")
    int updateTitle(@Param("conversationId") Long conversationId, @Param("title") String title);
}
