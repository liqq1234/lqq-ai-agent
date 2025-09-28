package com.lqq.lqqaiagent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqq.lqqaiagent.domain.AiChatMemory;
import com.lqq.lqqaiagent.mapper.AiChatMemoryMapper;
import com.lqq.lqqaiagent.service.AiChatMemoryService;
import org.springframework.stereotype.Service;

@Service
public class AiChatMemoryServiceImpl
        extends ServiceImpl<AiChatMemoryMapper, AiChatMemory>
        implements AiChatMemoryService {
}
