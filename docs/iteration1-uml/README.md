# 第一次迭代 UML 图源

本目录保存实验报告第二章“细化迭代 1”使用的 UML 图源和生成图像。

- `*.mmd`：Mermaid 图源，便于后续用 Mermaid、Markdown 或在线工具维护。
- `RenderIteration1Uml.java`：无第三方依赖的本地 PNG 渲染器，用 Java AWT 生成可插入 Word 的图片。
- `generated/`：运行渲染器后生成的 PNG 图片。

生成命令：

```bash
javac docs/iteration1-uml/RenderIteration1Uml.java
java -Djava.awt.headless=true -cp docs/iteration1-uml RenderIteration1Uml
```

