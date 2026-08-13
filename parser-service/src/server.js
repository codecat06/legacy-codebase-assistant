import express from 'express';
import { initParser, parseSource } from './parser.js';
import { extract } from './extractor.js';

const app = express();
app.use(express.json({ limit: '5mb' }));

const parser = await initParser();

app.post('/parse', (req, res) => {
    const { filePath, content } = req.body;
    if (!content) {
        return res.status(400).json({ error: 'content is required' });
    }
    try {
        const tree = parseSource(parser, content);
        const result = extract(tree, filePath ?? 'unknown.java');
        res.json(result);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

app.get('/health', (req, res) => res.json({ status: 'ok' }));

const PORT = process.env.PORT || 3001;
app.listen(PORT, () => {
    console.log(`Parser sidecar listening on :${PORT}`);
});