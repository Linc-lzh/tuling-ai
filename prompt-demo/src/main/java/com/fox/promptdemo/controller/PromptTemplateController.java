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

package com.fox.promptdemo.controller;

import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplate;
import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplateFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/example/ai")
public class PromptTemplateController {

	private final ChatClient chatClient;

	private final ConfigurablePromptTemplateFactory configurablePromptTemplateFactory;
	
	
	@Value("classpath:/prompts/joke-prompt.st")
	private Resource jokeResource;

	public PromptTemplateController(
			ChatClient.Builder builder,
			ConfigurablePromptTemplateFactory configurablePromptTemplateFactory
	) {

		this.chatClient = builder.build();
		this.configurablePromptTemplateFactory = configurablePromptTemplateFactory;
	}
	
	@GetMapping("/prompt")
	public AssistantMessage completion(
			@RequestParam(value = "adjective", defaultValue = "有趣") String adjective,
			@RequestParam(value = "topic", defaultValue = "奶牛") String topic
	) {

		PromptTemplate promptTemplate = new PromptTemplate(jokeResource);
		Prompt prompt = promptTemplate.create(Map.of("adjective", adjective, "topic", topic));

		return chatClient.prompt(prompt)
				.call()
				.chatResponse()
				.getResult()
				.getOutput();
	}
	
	/**
	 * nacos template config [{"name:"test-template","template:"please list the most famous books by this {author}."}]
	 */
	
	@GetMapping("/prompt-template")
	public AssistantMessage generate(
			@RequestParam(value = "author", defaultValue = "鲁迅") String author
	) {

		ConfigurablePromptTemplate template = configurablePromptTemplateFactory.getTemplate("test-template");

		if (template == null) {
			template = configurablePromptTemplateFactory.create("test-template",
					"请列出 {author} 最著名的三本书。");
		}

		Prompt prompt;
		if (StringUtils.hasText(author)) {
			prompt = template.create(Map.of("author", author));
		} else {
			prompt = template.create();
		}

		return chatClient.prompt(prompt)
				.call()
				.chatResponse()
				.getResult()
				.getOutput();
	}

}
