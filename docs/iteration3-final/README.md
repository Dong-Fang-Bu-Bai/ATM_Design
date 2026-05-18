# 第三次迭代 UML 与最终报告图源

本目录保存实验报告第四章“细化迭代 3”使用的图源和生成图像。

- `*.mmd`：Mermaid 图源，便于后续维护和对照 UML 语义。
- `RenderIteration3Uml.java`：无第三方依赖的本地 PNG 渲染器，用 Java AWT 生成可插入 Word 的图片。
- `generated/`：运行渲染器后生成的 PNG 图片。
- `update_iteration3_report.py`：将第四章内容和图片整合进报告 DOCX 的脚本。

生成命令：

```powershell
javac docs/iteration3-final/RenderIteration3Uml.java
java -Djava.awt.headless=true -cp docs/iteration3-final RenderIteration3Uml
py docs/iteration3-final/update_iteration3_report.py
```
