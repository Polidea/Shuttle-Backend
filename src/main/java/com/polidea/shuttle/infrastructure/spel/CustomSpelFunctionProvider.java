package com.polidea.shuttle.infrastructure.spel;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static java.lang.String.format;

public class CustomSpelFunctionProvider implements BeanFactoryPostProcessor {

    private final Class<?>[] functionHolders;

    public CustomSpelFunctionProvider(Class<?>... functionHolders) {
        this.functionHolders = functionHolders;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.setBeanExpressionResolver(new CustomFunctionsBeanExpressionResolver());
    }

    private class CustomFunctionsBeanExpressionResolver extends StandardBeanExpressionResolver {

        @Override
        protected void customizeEvaluationContext(StandardEvaluationContext evalContext) {

            Arrays.stream(functionHolders)
                  .forEach(customSpelFunctionsHolder ->
                               registerFunctionsInEvaluationContext(evalContext, customSpelFunctionsHolder));
        }

        private void registerFunctionsInEvaluationContext(StandardEvaluationContext evalContext, Class<?> functionHolder) {
            Arrays.stream(functionHolder.getDeclaredMethods())
                  .forEach(function -> {
                      checkIfFunctionRegistrationIsPossible(function);
                      evalContext.registerFunction(function.getName(), function);
                  });
        }

        private void checkIfFunctionRegistrationIsPossible(Method function) {
            int modifiers = function.getModifiers();

            if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
                throw new RuntimeException(
                    format(
                        "Custom SPEL function named \"%s()\" from class %s MUST be public static!",
                        function.getName(),
                        function.getClass()
                    )
                );
            }
        }
    }
}
