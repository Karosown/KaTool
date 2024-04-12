package cn.katool.util.llm.acl.consts;

import java.util.HashMap;

public abstract class LanguageConsts {

    public static final Integer CHINESE = 0;
    public static final Integer ENGLISH = 1;
    public static final Integer OTHER =-1;

    public static final HashMap<Integer,String> languageMap = new HashMap<>();
    static {
        languageMap.put(0,"中文");
        languageMap.put(1,"英文");
    }
}
