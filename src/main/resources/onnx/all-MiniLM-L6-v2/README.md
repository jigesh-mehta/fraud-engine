# all-MiniLM-L6-v2 (ONNX)

Local embedding model for `TransformersEmbeddingModel` (384 dimensions).

| File | Description |
|------|-------------|
| `tokenizer.json` | Hugging Face tokenizer (committed) |
| `model.onnx` | ONNX weights (~90 MB) |

If `model.onnx` is missing, run from the project root:

```powershell
.\scripts\download-onnx-model.ps1
```

Do not commit `model.onnx` unless you use Git LFS; it is listed in `.gitignore`.
