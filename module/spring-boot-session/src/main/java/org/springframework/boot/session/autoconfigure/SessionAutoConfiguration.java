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

package org.springframework.boot.session.autoconfigure;

import java.time.Duration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.web.server.Cookie;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.session.Session;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Spring Session.
 *
 * @author Andy Wilkinson
 * @author Tommy Ludwig
 * @author Eddú Meléndez
 * @author Stephane Nicoll
 * @author Vedran Pavic
 * @author Weix Sun
 * @since 4.0.0
 */
@AutoConfiguration
@ConditionalOnClass(Session.class)
@ConditionalOnWebApplication
@EnableConfigurationProperties({ ServerProperties.class, SessionProperties.class })
public class SessionAutoConfiguration {

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnWebApplication(type = Type.SERVLET)
	@Import(SessionRepositoryFilterConfiguration.class)
	static class ServletSessionConfiguration {

		@Bean
		@Conditional(DefaultCookieSerializerCondition.class)
		DefaultCookieSerializer cookieSerializer(ServerProperties serverProperties,
				ObjectProvider<DefaultCookieSerializerCustomizer> cookieSerializerCustomizers) {
			Cookie cookie = serverProperties.getServlet().getSession().getCookie();
			DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
			PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
			map.from(cookie::getName).to(cookieSerializer::setCookieName);
			map.from(cookie::getDomain).to(cookieSerializer::setDomainName);
			map.from(cookie::getPath).to(cookieSerializer::setCookiePath);
			map.from(cookie::getHttpOnly).to(cookieSerializer::setUseHttpOnlyCookie);
			map.from(cookie::getSecure).to(cookieSerializer::setUseSecureCookie);
			map.from(cookie::getMaxAge).asInt(Duration::getSeconds).to(cookieSerializer::setCookieMaxAge);
			map.from(cookie::getSameSite).as(SameSite::attributeValue).to(cookieSerializer::setSameSite);
			map.from(cookie::getPartitioned).to(cookieSerializer::setPartitioned);
			cookieSerializerCustomizers.orderedStream().forEach((customizer) -> customizer.customize(cookieSerializer));
			return cookieSerializer;
		}

		@Configuration(proxyBeanMethods = false)
		@ConditionalOnClass(RememberMeServices.class)
		static class RememberMeServicesConfiguration {

			@Bean
			DefaultCookieSerializerCustomizer rememberMeServicesCookieSerializerCustomizer() {
				return (cookieSerializer) -> cookieSerializer
					.setRememberMeRequestAttribute(SpringSessionRememberMeServices.REMEMBER_ME_LOGIN_ATTR);
			}

		}

	}

	/**
	 * Condition to trigger the creation of a {@link DefaultCookieSerializer}. This kicks
	 * in if either no {@link HttpSessionIdResolver} and {@link CookieSerializer} beans
	 * are registered, or if {@link CookieHttpSessionIdResolver} is registered but
	 * {@link CookieSerializer} is not.
	 */
	static class DefaultCookieSerializerCondition extends AnyNestedCondition {

		DefaultCookieSerializerCondition() {
			super(ConfigurationPhase.REGISTER_BEAN);
		}

		@ConditionalOnMissingBean({ HttpSessionIdResolver.class, CookieSerializer.class })
		static class NoComponentsAvailable {

		}

		@ConditionalOnBean(CookieHttpSessionIdResolver.class)
		@ConditionalOnMissingBean(CookieSerializer.class)
		static class CookieHttpSessionIdResolverAvailable {

		}

	}

}
