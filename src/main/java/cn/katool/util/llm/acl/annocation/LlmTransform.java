package cn.katool.util.llm.acl.annocation;

import javax.validation.constraints.NotNull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Scanner;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LlmTransform {

    String introduct() default "";

    // 这个字段在json中是否是必须的
    boolean isMust() default true;

    int language() default 0;
    // 如果需要自定义语言类型，请将language改为-1
    String languageName() default "";

}
