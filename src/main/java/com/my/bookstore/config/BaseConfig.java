package com.my.bookstore.config;

import com.my.bookstore.dto.book.BookItemDTO;
import com.my.bookstore.dto.book.BookItemResponseDTO;
import com.my.bookstore.dto.employee.EmployeeResponseDTO;
import com.my.bookstore.dto.order.OrderResponseDTO;
import com.my.bookstore.model.BookItem;
import com.my.bookstore.model.EmployeeProfile;
import com.my.bookstore.model.Order;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class BaseConfig implements WebMvcConfigurer {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        modelMapper.typeMap(Order.class, OrderResponseDTO.class)
                .addMapping(src -> src.getClient().getUser().getId(), OrderResponseDTO::setClientId);

        modelMapper.typeMap(BookItem.class, BookItemDTO.class)
                .addMapping(src -> src.getBook().getId(), BookItemDTO::setBookId);

        modelMapper.typeMap(EmployeeProfile.class, EmployeeResponseDTO.class).addMappings(m -> {
            m.map(src -> src.getUser().getEmail(), EmployeeResponseDTO::setEmail);
            m.map(src -> src.getUser().getId(), EmployeeResponseDTO::setId);
        });

        modelMapper.typeMap(BookItem.class, BookItemResponseDTO.class)
                .addMapping(src -> src.getBook().getId(), BookItemResponseDTO::setBookId)
                .addMapping(src -> src.getBook().getName(), BookItemResponseDTO::setBookName);

        return modelMapper;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        // Change to "classpath:i18n/messages" if you moved them to an i18n folder
        messageSource.setBasename("classpath:i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    @Override
    @Bean
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource());
        return bean;
    }

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("lang");
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setCookieMaxAge(Duration.of(1, ChronoUnit.DAYS));
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
