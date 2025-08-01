/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

/**
 * {@link Conditional @Conditional} that only matches when a bean of the specified class
 * is already contained in the {@link BeanFactory} and a single candidate can be
 * determined.
 * <p>
 * The condition will also match if multiple matching bean instances are already contained
 * in the {@link BeanFactory} but a primary candidate has been defined; essentially, the
 * condition match if auto-wiring a bean with the defined type will succeed.
 * <p>
 * The condition can only match the bean definitions that have been processed by the
 * application context so far and, as such, it is strongly recommended to use this
 * condition on auto-configuration classes only. If a candidate bean may be created by
 * another auto-configuration, make sure that the one using this condition runs after.
 *
 * @author Stephane Nicoll
 * @since 1.3.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnBeanCondition.class)
public @interface ConditionalOnSingleCandidate {

	/**
	 * The class type of bean that should be checked. The condition matches if a bean of
	 * the class specified is contained in the {@link BeanFactory} and a primary candidate
	 * exists in case of multiple instances. Beans that are not autowire candidates, that
	 * are not default candidates, or that are fallback candidates are ignored.
	 * <p>
	 * This attribute may <strong>not</strong> be used in conjunction with
	 * {@link #type()}, but it may be used instead of {@link #type()}.
	 * @return the class type of the bean to check
	 * @see Bean#autowireCandidate()
	 * @see BeanDefinition#isAutowireCandidate
	 * @see Bean#defaultCandidate()
	 * @see AbstractBeanDefinition#isDefaultCandidate
	 */
	Class<?> value() default Object.class;

	/**
	 * The class type name of bean that should be checked. The condition matches if a bean
	 * of the class specified is contained in the {@link BeanFactory} and a primary
	 * candidate exists in case of multiple instances. Beans that are not autowire
	 * candidates, that are not default candidates, or that are fallback candidates are
	 * ignored.
	 * <p>
	 * This attribute may <strong>not</strong> be used in conjunction with
	 * {@link #value()}, but it may be used instead of {@link #value()}.
	 * @return the class type name of the bean to check
	 * @see Bean#autowireCandidate()
	 * @see BeanDefinition#isAutowireCandidate
	 * @see Bean#defaultCandidate()
	 * @see AbstractBeanDefinition#isDefaultCandidate
	 */
	String type() default "";

	/**
	 * Strategy to decide if the application context hierarchy (parent contexts) should be
	 * considered.
	 * @return the search strategy
	 */
	SearchStrategy search() default SearchStrategy.ALL;

}
