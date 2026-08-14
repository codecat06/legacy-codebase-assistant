package com.example.codeintel.service;

import com.example.codeintel.dto.MethodCallRef;
import com.example.codeintel.dto.ParsedClass;
import com.example.codeintel.dto.ParsedField;
import com.example.codeintel.dto.ParsedFile;
import com.example.codeintel.dto.ParsedMethod;
import com.example.codeintel.dto.ParsedParam;
import com.example.codeintel.entity.CodeEdge;
import com.example.codeintel.entity.CodeNode;
import com.example.codeintel.entity.EdgeType;
import com.example.codeintel.entity.GitRepo;
import com.example.codeintel.entity.NodeType;
import com.example.codeintel.repository.CodeEdgeRepository;
import com.example.codeintel.repository.CodeNodeRepository;
import com.example.codeintel.repository.GitRepoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraphBuilderService {

    private final CodeNodeRepository nodeDao;
    private final CodeEdgeRepository edgeDao;
    private final GitRepoRepository repositoryDao;

    public Map<String, List<UUID>> build(UUID repositoryId, List<ParsedFile> files) {
        GitRepo repo = repositoryDao.findById(repositoryId).orElseThrow();

        Map<String, List<ClassEntry>> classesBySimpleName = new HashMap<>();
        Map<String, ClassEntry> classesByFqn = new HashMap<>();
        Map<String, List<UUID>> methodNodeIdsByFqn = new HashMap<>();

        // Pass 1: her class/method için bir CodeNode yarat, çözümleme için bir index
        // kur
        for (ParsedFile file : files) {
            for (ParsedClass cls : file.classes()) {
                String classFqn = buildFqn(file.packageName(), cls.name());

                CodeNode classNode = new CodeNode();
                classNode.setRepo(repo);
                classNode.setNodeType(NodeType.CLASS);
                classNode.setFqn(classFqn);
                classNode.setFilePath(file.file());
                classNode = nodeDao.save(classNode);

                Map<String, String> fieldTypes = new HashMap<>();
                for (ParsedField field : cls.fields()) {
                    fieldTypes.put(field.name(), field.type());
                }

                Map<String, List<UUID>> methodNodeIds = new HashMap<>();
                for (ParsedMethod method : cls.methods()) {
                    CodeNode methodNode = new CodeNode();
                    methodNode.setRepo(repo);
                    methodNode.setNodeType(NodeType.METHOD);
                    methodNode.setFqn(classFqn + "." + method.name());
                    methodNode.setFilePath(file.file());
                    methodNode.setSourceText(method.sourceText());

                    methodNode = nodeDao.save(methodNode);

                    methodNodeIdsByFqn
                            .computeIfAbsent(classFqn + "." + method.name(), k -> new ArrayList<>())
                            .add(methodNode.getId());

                    methodNodeIds
                            .computeIfAbsent(method.name(), k -> new ArrayList<>())
                            .add(methodNode.getId());
                }

                ClassEntry entry = new ClassEntry(classFqn, fieldTypes, methodNodeIds);
                classesBySimpleName.computeIfAbsent(cls.name(), k -> new ArrayList<>()).add(entry);
                classesByFqn.put(classFqn, entry);
            }
        }

        log.info("Created nodes for {} class(es)", classesByFqn.size());

        // Pass 2: method çağrılarını CALLS kenarlarına çevir (best-effort çözümleme)
        int created = 0;
        int skipped = 0;

        for (ParsedFile file : files) {
            for (ParsedClass cls : file.classes()) {
                ClassEntry self = classesByFqn.get(buildFqn(file.packageName(), cls.name()));
                if (self == null)
                    continue;

                for (ParsedMethod method : cls.methods()) {
                    List<UUID> callerIds = self.methods().get(method.name());
                    if (callerIds == null || callerIds.isEmpty())
                        continue;
                    UUID callerId = callerIds.get(0);

                    Map<String, String> paramTypes = new HashMap<>();
                    for (ParsedParam p : method.params()) {
                        paramTypes.put(p.name(), p.type());
                    }

                    for (MethodCallRef call : method.calls()) {
                        UUID targetId = resolveCallTarget(call.target(), self, paramTypes, classesBySimpleName);
                        if (targetId == null) {
                            skipped++;
                            continue;
                        }

                        CodeEdge edge = new CodeEdge();
                        edge.setSource(nodeDao.getReferenceById(callerId));
                        edge.setTarget(nodeDao.getReferenceById(targetId));
                        edge.setEdgeType(EdgeType.CALLS);
                        edgeDao.save(edge);
                        created++;
                    }
                }
            }
        }

        log.info("Graph build finished for repo {}: {} edge(s) created, {} call(s) unresolved",
                repositoryId, created, skipped);

        return methodNodeIdsByFqn;
    }

    private UUID resolveCallTarget(
            String target,
            ClassEntry self,
            Map<String, String> paramTypes,
            Map<String, List<ClassEntry>> classesBySimpleName) {

        int dot = target.lastIndexOf('.');
        String objectName = dot == -1 ? "this" : target.substring(0, dot);
        String methodName = dot == -1 ? target : target.substring(dot + 1);

        ClassEntry targetClass;
        if (objectName.equals("this") || objectName.equals("super")) {
            targetClass = self;
        } else {
            String typeName = paramTypes.getOrDefault(objectName, self.fields().get(objectName));
            if (typeName == null) {
                return null; // yerel değişken, static çağrı ya da bizim izlemediğimiz bir tip
            }
            List<ClassEntry> candidates = classesBySimpleName.get(typeName);
            if (candidates == null || candidates.isEmpty()) {
                return null; // bu repoya ait olmayan bir tip (ör. bir kütüphane class'ı)
            }
            targetClass = candidates.get(0); // aynı isimde birden fazla class varsa ilkini alıyoruz
        }

        List<UUID> methodIds = targetClass.methods().get(methodName);
        return (methodIds == null || methodIds.isEmpty()) ? null : methodIds.get(0);
    }

    private String buildFqn(String packageName, String className) {
        return (packageName == null || packageName.isBlank()) ? className : packageName + "." + className;
    }

    private record ClassEntry(String fqn, Map<String, String> fields, Map<String, List<UUID>> methods) {
    }
}
