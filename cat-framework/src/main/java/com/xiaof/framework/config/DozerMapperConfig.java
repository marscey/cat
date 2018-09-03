/*
 * Copyright (C) 2017 eKing Technology, Inc. All Rights Reserved.
 */

package com.xiaof.framework.config;

import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DozerMapperConfig {

    @Bean
    public Mapper mapper() {
        return DozerBeanMapperSingletonWrapper.getInstance();
    }
}
