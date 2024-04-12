package cn.katool.util.llm.server;

import cn.katool.util.llm.dao.LlmSingleSessionEntity;

import java.util.List;

public interface LargeLanguageModelAskInterface {

    void useJson(boolean status);

    boolean isUseJson();

    String ask(String ask);

    String askByHistory(String ask, List<LlmSingleSessionEntity> entityList);
}
