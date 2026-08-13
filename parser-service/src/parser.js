import Parser from 'web-tree-sitter';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

let javaLanguage = null;

export async function initParser() {
    await Parser.init();

    if (!javaLanguage) {
        const wasmPath = path.resolve(
            __dirname,
            '../node_modules/tree-sitter-wasms/out/tree-sitter-java.wasm'
        );
        javaLanguage = await Parser.Language.load(wasmPath);
    }

    const parser = new Parser();
    parser.setLanguage(javaLanguage);
    return parser;
}

export function parseSource(parser, sourceCode) {
    return parser.parse(sourceCode);
}
