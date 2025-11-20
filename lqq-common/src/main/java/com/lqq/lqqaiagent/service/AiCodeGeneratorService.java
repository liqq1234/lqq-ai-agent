package com.lqq.lqqaiagent.service;

import com.lqq.lqqaiagent.model.dto.HtmlCodeResult;
import com.lqq.lqqaiagent.model.dto.MultiFileCodeResult;

public interface AiCodeGeneratorService {

    HtmlCodeResult generateHtmlCode(String userMessage);

    MultiFileCodeResult generateMultiFileCode(String userMessage);
}