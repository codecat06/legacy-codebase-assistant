package com.example.codeintel.service;

import org.springframework.stereotype.Service;

import com.example.codeintel.dto.AskResponse;
import com.example.codeintel.entity.CodeNode;

import lombok.RequiredArgsConstructor;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String SYSTEM_PROMPT = """
            Sen bir legacy codebase rehberisin. Sadece sana verilen kod parçalarına dayanarak cevap ver.
            Cevabında hangi method/dosyalardan yararlandığını (fqn ile) belirt.
            Verilen context dışında bir şey biliyormuş gibi davranma; context soruyu cevaplamaya
            yetmiyorsa bunu açıkça söyle, uydurma.
            """;

    private final RetrievalService retrievalService;
    private final ChatClient chatClient;

    public AskResponse ask(String question, int topK, int hops) {
        List<CodeNode> context = retrievalService.retrieve(question, topK, hops);

        String userPrompt = buildPrompt(question, context);
        String answer = chatClient.chat(SYSTEM_PROMPT, userPrompt);

        List<String> sources = context.stream().map(CodeNode::getFqn).toList();

        return new AskResponse(answer, sources);

    }

    private String buildPrompt(String question, List<CodeNode> context) {
        StringBuilder sb = new StringBuilder();
        sb.append("Soru: ").append(question).append("\n\n");
        sb.append("İlgili kod parçaları:\n\n");

        for (CodeNode node : context) {
            sb.append("### ").append(node.getFqn()).append(" (").append(node.getFilePath()).append(")\n");
            sb.append("```java\n");
            sb.append(node.getSourceText() != null ? node.getSourceText() : "// kaynak kod bulunamadı");
            sb.append("\n```\n\n");
        }

        return sb.toString();
    }
}
