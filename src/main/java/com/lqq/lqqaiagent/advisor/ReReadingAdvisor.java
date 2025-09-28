package com.lqq.lqqaiagent.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.lang.NonNull;

import java.util.Map;

public class ReReadingAdvisor implements BaseAdvisor {

	private static final String DEFAULT_RE2_ADVISE_TEMPLATE = """
      {re2_input_query}
      Read the question again: {re2_input_query}
      """;

	private final String re2AdviseTemplate;

	private int order = 0;

	public ReReadingAdvisor() {
		this(DEFAULT_RE2_ADVISE_TEMPLATE);
	}

	public ReReadingAdvisor(String re2AdviseTemplate) {
		this.re2AdviseTemplate = re2AdviseTemplate;
	}

	@Override
	@NonNull
	public ChatClientRequest before(@NonNull ChatClientRequest chatClientRequest,
									@NonNull AdvisorChain advisorChain) {
		String augmentedUserText = PromptTemplate.builder()
				.template(this.re2AdviseTemplate)
				.variables(Map.of("re2_input_query", chatClientRequest.prompt().getUserMessage().getText()))
				.build()
				.render();

		return chatClientRequest.mutate()
				.prompt(chatClientRequest.prompt().augmentUserMessage(augmentedUserText))
				.build();
	}

	@Override
	@NonNull
	public ChatClientResponse after(@NonNull ChatClientResponse chatClientResponse,
									@NonNull AdvisorChain advisorChain) {
		return chatClientResponse;
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	public ReReadingAdvisor withOrder(int order) {
		this.order = order;
		return this;
	}
}
