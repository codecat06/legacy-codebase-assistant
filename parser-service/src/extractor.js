function getModifiersNode(declNode) {
    return declNode.children.find((c) => c.type === 'modifiers') || null;
}

function extractAnnotations(declNode) {
    const modifiers = getModifiersNode(declNode);
    if (!modifiers) return [];
    return modifiers.children
        .filter((c) => c.type === 'annotation' || c.type === 'marker_annotation')
        .map((c) => c.text);
}

function extractPackage(rootNode) {
    const pkgNode = rootNode.children.find((c) => c.type === 'package_declaration');
    if (!pkgNode) return null;
    return pkgNode.text.replace(/^package\s+/, '').replace(/;$/, '').trim();
}

function extractImports(rootNode) {
    const imports = [];
    for (const child of rootNode.children) {
        if (child.type === 'import_declaration') {
            const text = child.text
                .replace(/^import\s+/, '')
                .replace(/^static\s+/, '')
                .replace(/;$/, '')
                .trim();
            imports.push(text);
        }
    }
    return imports;
}

function extractParams(paramsNode) {
    if (!paramsNode) return [];
    const params = [];
    for (const child of paramsNode.children) {
        if (child.type === 'formal_parameter') {
            const typeNode = child.childForFieldName('type');
            const nameNode = child.childForFieldName('name');
            params.push({
                type: typeNode ? typeNode.text : 'Object',
                name: nameNode ? nameNode.text : '?',
            });
        }
    }
    return params;
}

function extractCalls(bodyNode) {
    const calls = [];
    if (!bodyNode) return calls;

    function walk(node) {
        if (node.type === 'method_invocation') {
            const objectNode = node.childForFieldName('object');
            const nameNode = node.childForFieldName('name');
            const target = objectNode ? `${objectNode.text}.${nameNode.text}` : nameNode.text;
            calls.push({ target, line: node.startPosition.row + 1 });
        }
        for (const child of node.children) walk(child);
    }

    walk(bodyNode);
    return calls;
}

function extractMethods(classBodyNode) {
    const methods = [];
    for (const member of classBodyNode.children) {
        if (member.type === 'method_declaration' || member.type === 'constructor_declaration') {
            const nameNode = member.childForFieldName('name');
            const typeNode = member.childForFieldName('type');
            const paramsNode = member.childForFieldName('parameters');
            const bodyNode = member.childForFieldName('body');

            methods.push({
                name: nameNode ? nameNode.text : '<init>',
                returnType: typeNode ? typeNode.text : 'void',
                annotations: extractAnnotations(member),
                params: extractParams(paramsNode),
                calls: extractCalls(bodyNode),
            });
        }
    }
    return methods;
}

function extractFields(classBodyNode) {
    const fields = [];
    for (const member of classBodyNode.children) {
        if (member.type === 'field_declaration') {
            const typeNode = member.childForFieldName('type');
            const declarator = member.children.find((c) => c.type === 'variable_declarator');
            const nameNode = declarator ? declarator.childForFieldName('name') : null;
            fields.push({
                name: nameNode ? nameNode.text : '?',
                type: typeNode ? typeNode.text : 'Object',
                annotations: extractAnnotations(member),
            });
        }
    }
    return fields;
}

function extractClasses(rootNode) {
    const classes = [];
    for (const child of rootNode.children) {
        if (child.type === 'class_declaration' || child.type === 'interface_declaration') {
            const nameNode = child.childForFieldName('name');
            const bodyNode = child.childForFieldName('body');

            classes.push({
                name: nameNode ? nameNode.text : '?',
                annotations: extractAnnotations(child),
                fields: bodyNode ? extractFields(bodyNode) : [],
                methods: bodyNode ? extractMethods(bodyNode) : [],
            });
        }
    }
    return classes;
}

export function extract(tree, filePath) {
    const root = tree.rootNode;
    return {
        file: filePath,
        package: extractPackage(root),
        imports: extractImports(root),
        classes: extractClasses(root),
    };
}