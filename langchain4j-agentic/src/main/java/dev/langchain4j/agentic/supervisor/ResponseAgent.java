package dev.langchain4j.agentic.supervisor;

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 响应智能体
 */
public interface ResponseAgent {

    /**
     * 您是一名响应评估员，将获得用户请求的两个响应。
     * 您的角色是根据对用户请求的相关性对这两个响应进行评分。
     *
     * 对于两个响应，response1 和 response2，您将分别返回一个分数，分别为 score 1 和 score 2，
     * 分数范围在 0.0 到 1.0 之间，其中 0.0 表示响应与用户请求完全不相关，1.0 表示响应与用户请求完全相关。
     *
     * 只返回分数，不要有任何额外文字或解释。
     *
     * 用户请求是：'{{request}}'。
     * 第一个响应是：'{{response1}}'。
     * 第二个响应是：'{{response2}}'。
     *
     * @param request 用户请求
     * @param response1 响应1
     * @param response2 响应2
     * @return 响应分数
     */
    @UserMessage("""
           You are a response evaluator that is provided with two responses to a user request.
           Your role is to score the two responses based on their relevance for the user request.
           
           For each of the two responses, response1 and response2, you will return a score, respectively score 1 and score 2,
           between 0.0 and 1.0, where 0.0 means the response is completely irrelevant to the user request,
           and 1.0 means the response is perfectly relevant to the user request.
           
           Return only the score and nothing else, without any additional text or explanation.

           The user request is: '{{request}}'.
           The first response is: '{{response1}}'.
           The second response is: '{{response2}}'.
           """)
    ResponseScore scoreResponses(
            @V("request") String request,
            @V("response1") String response1,
            @V("response2") String response2);
}
