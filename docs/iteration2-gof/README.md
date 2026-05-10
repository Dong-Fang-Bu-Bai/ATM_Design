# 第二次迭代 GoF 与 UML 图源

本目录保存实验报告第三章“细化迭代 2”使用的图源和生成图像。

- `*.mmd`：Mermaid 图源，便于后续继续维护。
- `RenderIteration2Uml.java`：无第三方依赖的本地 PNG 渲染器，用 Java AWT 生成可插入 Word 的图片。
- `generated/`：运行渲染器后生成的 PNG 图片。
- `update_iteration2_report.py`：将第三章内容和图片整合进报告 DOCX 的脚本。

生成命令：

```powershell
javac docs/iteration2-gof/RenderIteration2Uml.java
java -Djava.awt.headless=true -cp docs/iteration2-gof RenderIteration2Uml
```
