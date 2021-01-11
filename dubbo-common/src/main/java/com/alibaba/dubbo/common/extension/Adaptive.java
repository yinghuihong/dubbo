/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.common.extension;

import com.alibaba.dubbo.common.URL;

import java.lang.annotation.*;

/**
 * Provide helpful information for {@link ExtensionLoader} to inject dependency extension instance.
 * <p>
 * 自适应：提供有效信息，帮忙注入依赖扩展实例
 * <p>
 * 两种用法，但最终的作用都是拥有一个interface的Adaptive实现类，假设有一个interface名为XxxFactory
 * 一、注解放在实现类上
 * 1、在编码期，就已有一个Adaptive实现类，文件名比如：AdaptiveXxxFactory.java
 * 2、@Adaptive的value一定为空
 * 3、AdaptiveXxxFactory.java 中可自定义实现，决定返回哪个真正实现类（不包括AdaptiveXxxFactory.java自身）
 * 二、注解放在函数上
 * 1、在运行期，将会由ExtensionLoader动态拼接代码，生成Adaptive实现类：XxxFactory$Adaptive.java
 * 2、@Adaptive的value可为空/非空，函数的参数列表中必须有Url参数
 * 3、XxxFactory$Adaptive.java 的函数实现（可参见 SimpleExt$Adaptive ），生成规则见下文
 * <p>
 * 对于注解放在函数上，动态生成代码时，考虑的规则：
 * 一、获取扩展实现名称（extensionName），依序如下：
 * 1、函数上的@Adaptive(String [])
 * 1.1、确保String[] parameterKeys 不为空
 * 当String[]不为空时，将String[]
 * 当String[]为空时，拆解interface的类名称，作为parameterKey值
 * 1.2、遍历 String[] parameterKeys
 * 依序从URL上查找对应的parameter，若存在parameter，则对应的parameterValue即为扩展实现名称
 * 若没有存在的parameter，或parameterValue为空，则进入2
 * 2、通过interface类上的@SPI(string)
 * string即为扩展实现名称（默认指定的扩展实现）
 * 二、通过扩展实现名称，找到并实例化扩展实现类
 *
 * @see ExtensionLoader
 * @see URL
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Adaptive {
    /**
     * Decide which target extension to be injected. The name of the target extension is decided by the parameter passed
     * in the URL, and the parameter names are given by this method.
     * <p>
     * If the specified parameters are not found from {@link URL}, then the default extension will be used for
     * dependency injection (specified in its interface's {@link SPI}).
     * <p>
     * For examples, given <code>String[] {"key1", "key2"}</code>:
     * <ol>
     * <li>find parameter 'key1' in URL, use its value as the extension's name</li>
     * <li>try 'key2' for extension's name if 'key1' is not found (or its value is empty) in URL</li>
     * <li>use default extension if 'key2' doesn't appear either</li>
     * <li>otherwise, throw {@link IllegalStateException}</li>
     * </ol>
     * If default extension's name is not give on interface's {@link SPI}, then a name is generated from interface's
     * class name with the rule: divide classname from capital char into several parts, and separate the parts with
     * dot '.', for example: for {@code com.alibaba.dubbo.xxx.YyyInvokerWrapper}, its default name is
     * <code>String[] {"yyy.invoker.wrapper"}</code>. This name will be used to search for parameter from URL.
     *
     * @return parameter key names in URL
     */
    String[] value() default {};

}