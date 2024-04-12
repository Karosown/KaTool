package cn.katool.util.llm.dao;

import com.fasterxml.jackson.annotation.JsonFormat;

public class LlmSingleSessionEntity {

    // llm的回复
    @JsonFormat
    String system;

    // 用户的提问
    String user;
}
