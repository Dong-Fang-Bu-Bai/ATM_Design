import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class RenderIteration3Uml {
    private static final Color BACKGROUND = new Color(250, 252, 255);
    private static final Color LINE = new Color(45, 55, 72);
    private static final Color TITLE = new Color(24, 55, 91);
    private static final Color FRONTEND = new Color(232, 241, 255);
    private static final Color SERVICE = new Color(255, 247, 219);
    private static final Color DATA = new Color(238, 234, 255);
    private static final Color DEVICE = new Color(235, 248, 250);
    private static final Color ERROR = new Color(255, 232, 232);
    private static Font font;

    public static void main(String[] args) throws Exception {
        font = loadFont();
        Path output = Path.of("docs/iteration3-final/generated");
        Files.createDirectories(output);
        renderStateMachine(output.resolve("atm-session-state.png").toFile());
        renderReceiptSequence(output.resolve("receipt-history-sequence.png").toFile());
        renderDeviceCashFlow(output.resolve("device-cash-flow.png").toFile());
        renderComponentDeployment(output.resolve("final-component-deployment.png").toFile());
        renderDemoFlow(output.resolve("final-demo-flow.png").toFile());
        renderRefinementMap(output.resolve("model-refinement-map.png").toFile());
    }

    private static Font loadFont() {
        String[] candidates = {
                "C:/Windows/Fonts/msyh.ttc",
                "C:/Windows/Fonts/simhei.ttf",
                "C:/Windows/Fonts/simsun.ttc",
                "/mnt/c/Windows/Fonts/msyh.ttc",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
        };
        for (String candidate : candidates) {
            try {
                File file = new File(candidate);
                if (file.exists()) {
                    return Font.createFont(Font.TRUETYPE_FONT, file).deriveFont(Font.PLAIN, 20f);
                }
            } catch (Exception ignored) {
            }
        }
        return new Font("SansSerif", Font.PLAIN, 20);
    }

    private static Graphics2D canvas(BufferedImage image) {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(BACKGROUND);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.setColor(LINE);
        g.setStroke(new BasicStroke(2f));
        g.setFont(font);
        return g;
    }

    private static void title(Graphics2D g, String text, int width) {
        g.setFont(font.deriveFont(Font.BOLD, 34f));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(TITLE);
        g.drawString(text, (width - fm.stringWidth(text)) / 2, 54);
        g.setColor(LINE);
        g.setFont(font);
    }

    private static void box(Graphics2D g, int x, int y, int w, int h, String text, Color fill) {
        g.setColor(fill);
        g.fillRoundRect(x, y, w, h, 18, 18);
        g.setColor(LINE);
        g.drawRoundRect(x, y, w, h, 18, 18);
        g.setFont(font.deriveFont(Font.BOLD, 19f));
        List<String> lines = wrap(g, text, w - 28);
        int cy = y + (h - lines.size() * 24) / 2 + 20;
        for (String line : lines) {
            drawCentered(g, line, x, cy, w);
            cy += 26;
        }
    }

    private static void panel(Graphics2D g, int x, int y, int w, int h, String name, Color fill) {
        g.setColor(fill);
        g.fillRoundRect(x, y, w, h, 22, 22);
        g.setColor(new Color(120, 130, 150));
        g.drawRoundRect(x, y, w, h, 22, 22);
        g.setFont(font.deriveFont(Font.BOLD, 24f));
        g.setColor(TITLE);
        g.drawString(name, x + 24, y + 38);
        g.setColor(LINE);
    }

    private static void arrow(Graphics2D g, int x1, int y1, int x2, int y2, String label) {
        g.setColor(LINE);
        g.setStroke(new BasicStroke(2f));
        g.draw(new Line2D.Double(x1, y1, x2, y2));
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int len = 15;
        int ax1 = (int) (x2 - len * Math.cos(angle - Math.PI / 7));
        int ay1 = (int) (y2 - len * Math.sin(angle - Math.PI / 7));
        int ax2 = (int) (x2 - len * Math.cos(angle + Math.PI / 7));
        int ay2 = (int) (y2 - len * Math.sin(angle + Math.PI / 7));
        g.drawLine(x2, y2, ax1, ay1);
        g.drawLine(x2, y2, ax2, ay2);
        if (label != null && !label.isEmpty()) {
            g.setFont(font.deriveFont(Font.PLAIN, 15f));
            FontMetrics fm = g.getFontMetrics();
            int tx = (x1 + x2) / 2 - fm.stringWidth(label) / 2;
            int ty = (y1 + y2) / 2 - 8;
            g.setColor(new Color(255, 255, 255, 230));
            g.fillRoundRect(tx - 6, ty - 18, fm.stringWidth(label) + 12, 24, 8, 8);
            g.setColor(new Color(74, 85, 104));
            g.drawString(label, tx, ty);
            g.setColor(LINE);
        }
    }

    private static void dashedArrow(Graphics2D g, int x1, int y1, int x2, int y2, String label) {
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{8f, 8f}, 0f));
        arrow(g, x1, y1, x2, y2, label);
        g.setStroke(new BasicStroke(2f));
    }

    private static void drawCentered(Graphics2D g, String text, int x, int y, int w) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, x + (w - fm.stringWidth(text)) / 2, y);
    }

    private static List<String> wrap(Graphics2D g, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        for (String manualLine : text.split("\\n", -1)) {
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < manualLine.length(); i++) {
                char ch = manualLine.charAt(i);
                String next = line.toString() + ch;
                if (g.getFontMetrics().stringWidth(next) > maxWidth && line.length() > 0) {
                    lines.add(line.toString());
                    line.setLength(0);
                }
                line.append(ch);
            }
            if (line.length() > 0) {
                lines.add(line.toString());
            }
        }
        return lines;
    }

    private static void save(BufferedImage image, File file) throws Exception {
        ImageIO.write(image, "png", file);
    }

    private static void renderStateMachine(File file) throws Exception {
        BufferedImage image = new BufferedImage(1700, 1120, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "细化迭代3 ATM会话与业务状态机", image.getWidth());
        box(g, 720, 100, 260, 70, "待机", DEVICE);
        box(g, 720, 230, 260, 70, "认证中\n校验卡号与密码", FRONTEND);
        box(g, 720, 360, 260, 70, "业务选择\n持有 sessionId", SERVICE);
        box(g, 210, 520, 260, 90, "设备检查\n状态与现金能力", DEVICE);
        box(g, 720, 520, 260, 90, "交易处理中\n取款/存款/转账", SERVICE);
        box(g, 1230, 520, 260, 90, "异常提示\n返回可重试状态", ERROR);
        box(g, 520, 720, 260, 80, "凭条展示\n按会话限制访问", FRONTEND);
        box(g, 920, 720, 260, 80, "流水查询\n当前账户分页", FRONTEND);
        box(g, 720, 900, 260, 70, "退卡中 / 会话失效", ERROR);
        arrow(g, 850, 170, 850, 230, "插卡");
        arrow(g, 850, 300, 850, 360, "登录成功");
        arrow(g, 720, 395, 470, 565, "设备状态/取款前");
        arrow(g, 980, 395, 720, 565, "选择交易");
        arrow(g, 470, 565, 720, 565, "设备可用");
        arrow(g, 980, 565, 1230, 565, "业务失败");
        dashedArrow(g, 470, 565, 1230, 565, "设备不可用");
        arrow(g, 850, 610, 650, 720, "查看凭条");
        arrow(g, 780, 760, 920, 760, "查流水");
        arrow(g, 920, 760, 780, 760, "选凭条");
        arrow(g, 650, 800, 850, 900, "返回菜单/退卡");
        arrow(g, 1050, 800, 850, 900, "返回菜单/退卡");
        arrow(g, 1360, 610, 980, 395, "返回重试");
        arrow(g, 850, 970, 850, 1030, "回到待机");
        box(g, 700, 1030, 300, 60, "结束后重新进入待机", DEVICE);
        save(image, file);
    }

    private static void renderReceiptSequence(File file) throws Exception {
        BufferedImage image = new BufferedImage(2000, 1230, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "交易流水与凭条查询顺序图", image.getWidth());
        String[] names = {"客户", "Vue显示模块", "atm.js", "Controller", "Service", "SessionValidator", "Mapper", "数据库"};
        int[] xs = {110, 350, 600, 850, 1110, 1390, 1640, 1870};
        for (int i = 0; i < xs.length; i++) {
            box(g, xs[i] - 95, 95, 190, 58, names[i], i < 3 ? FRONTEND : i < 5 ? SERVICE : DATA);
            g.setColor(new Color(160, 170, 190));
            g.drawLine(xs[i], 153, xs[i], 1120);
        }
        String[][] msgs = {
                {"0", "1", "查看交易流水"},
                {"1", "2", "getTransactionHistory(sessionId,page,size)"},
                {"2", "3", "GET /transactions/history"},
                {"3", "4", "getTransactionHistory"},
                {"4", "5", "校验 sessionId"},
                {"5", "4", "返回 cardNo"},
                {"4", "6", "按当前账户分页查询"},
                {"6", "7", "selectByAccountIdPaged / count"},
                {"7", "6", "records,total"},
                {"6", "4", "流水列表"},
                {"4", "1", "分页结果"},
                {"0", "1", "选择单笔凭条"},
                {"1", "2", "getReceipt(transactionId,sessionId)"},
                {"2", "3", "GET /receipts/{transactionId}"},
                {"3", "4", "getReceipt"},
                {"4", "5", "再次校验当前会话"},
                {"4", "6", "selectByTransactionId"},
                {"6", "7", "查询单笔交易"},
                {"7", "6", "transaction"},
                {"4", "1", "返回凭条数据"}
        };
        int y = 195;
        for (String[] msg : msgs) {
            int from = Integer.parseInt(msg[0]);
            int to = Integer.parseInt(msg[1]);
            arrow(g, xs[from], y, xs[to], y, msg[2]);
            y += 43;
        }
        box(g, 1040, 1020, 760, 110, "访问控制亮点：流水和凭条都先由 sessionId 定位当前账户，凭条编号存在但不属于当前账户时仍返回“凭条不存在”，避免跨账户读取交易信息。", ERROR);
        save(image, file);
    }

    private static void renderDeviceCashFlow(File file) throws Exception {
        BufferedImage image = new BufferedImage(1500, 1320, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "取款设备现金联动流程", image.getWidth());
        int x = 575;
        int[][] nodes = {{x, 95}, {x, 220}, {x, 360}, {x, 500}, {x, 640}, {x, 780}, {x, 920}, {x, 1060}};
        String[] texts = {
                "取款请求\nsessionId + amount",
                "校验会话、金额、100整数倍与限额",
                "查询当前账户余额",
                "读取 ATM001 设备状态与现金",
                "创建 PENDING 交易流水",
                "扣减账户余额",
                "扣减设备可用现金",
                "标记 SUCCESS\n返回 transactionId 与余额"
        };
        for (int i = 0; i < nodes.length; i++) {
            box(g, nodes[i][0], nodes[i][1], 350, 80, texts[i], i < 4 ? SERVICE : DATA);
            if (i > 0) {
                arrow(g, x + 175, nodes[i - 1][1] + 80, x + 175, nodes[i][1], "");
            }
        }
        box(g, 80, 330, 320, 110, "异常分支\n金额非法、余额不足、设备停用或现金不足时抛出 ApiException", ERROR);
        dashedArrow(g, x, 260, 400, 385, "校验失败");
        dashedArrow(g, x, 540, 400, 385, "设备失败");
        box(g, 1040, 500, 330, 140, "设备模型\natm_device 保存 atmCode、location、status、cashAvailable；取款成功才扣减现金，存款和转账不改变设备现金。", DEVICE);
        arrow(g, 925, 540, 1040, 570, "读写设备");
        save(image, file);
    }

    private static void renderComponentDeployment(File file) throws Exception {
        BufferedImage image = new BufferedImage(1800, 980, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "第三次迭代组件与部署视图", image.getWidth());
        panel(g, 60, 120, 470, 720, "用户侧 / ATM模拟终端", FRONTEND);
        panel(g, 650, 120, 510, 720, "Spring Boot 后端", SERVICE);
        panel(g, 1280, 120, 460, 720, "数据层 / MySQL", DATA);
        box(g, 140, 220, 300, 80, "浏览器 / Vite 前端", FRONTEND);
        box(g, 140, 370, 300, 90, "Vue页面\n菜单、交易、流水、凭条、设备", FRONTEND);
        box(g, 140, 540, 300, 80, "atm.js 接口封装", FRONTEND);
        box(g, 740, 200, 330, 75, "AuthController / AccountController", SERVICE);
        box(g, 740, 315, 330, 75, "TransactionController", SERVICE);
        box(g, 740, 430, 330, 75, "ReceiptController", SERVICE);
        box(g, 740, 545, 330, 75, "DeviceController", DEVICE);
        box(g, 740, 675, 330, 75, "GlobalExceptionHandler", ERROR);
        box(g, 1350, 205, 320, 70, "customer / bank_card", DATA);
        box(g, 1350, 325, 320, 70, "account", DATA);
        box(g, 1350, 445, 320, 70, "transaction_record", DATA);
        box(g, 1350, 565, 320, 70, "atm_device", DATA);
        arrow(g, 290, 300, 290, 370, "");
        arrow(g, 290, 460, 290, 540, "");
        arrow(g, 440, 580, 740, 235, "HTTP");
        arrow(g, 440, 580, 740, 350, "HTTP");
        arrow(g, 440, 580, 740, 465, "HTTP");
        arrow(g, 440, 580, 740, 580, "HTTP");
        arrow(g, 1070, 235, 1350, 240, "Mapper");
        arrow(g, 1070, 350, 1350, 360, "账户读写");
        arrow(g, 1070, 465, 1350, 480, "凭条/流水");
        arrow(g, 1070, 580, 1350, 600, "设备状态");
        dashedArrow(g, 905, 675, 905, 620, "统一错误");
        save(image, file);
    }

    private static void renderDemoFlow(File file) throws Exception {
        BufferedImage image = new BufferedImage(1550, 1000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "第三次迭代最终演示流程", image.getWidth());
        String[] texts = {
                "启动后端与前端\n真实联调模式",
                "登录演示账号\n生成 sessionId",
                "主菜单查看设备状态摘要",
                "设备状态页\n刷新状态并检查吐钞能力",
                "办理取款/存款/转账",
                "交易成功后查看凭条",
                "进入交易流水分页列表",
                "从流水再次查看单笔凭条",
                "可选：修改密码\n当前 sessionId 失效",
                "退卡退出"
        };
        int[][] pos = {{80, 130}, {470, 130}, {860, 130}, {860, 310}, {470, 310}, {80, 310}, {80, 510}, {470, 510}, {860, 510}, {470, 720}};
        for (int i = 0; i < texts.length; i++) {
            box(g, pos[i][0], pos[i][1], 300, 100, texts[i], i == 8 ? ERROR : i == 3 ? DEVICE : FRONTEND);
        }
        for (int i = 1; i < texts.length; i++) {
            arrow(g, pos[i - 1][0] + 300, pos[i - 1][1] + 50, pos[i][0], pos[i][1] + 50, i == 8 ? "可选" : "");
        }
        box(g, 1180, 350, 290, 180, "演示覆盖\n设备状态、吐钞检查、核心交易、凭条、流水、会话失效和退卡，能够对应第三次迭代验收点。", SERVICE);
        save(image, file);
    }

    private static void renderRefinementMap(File file) throws Exception {
        BufferedImage image = new BufferedImage(1700, 940, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "从前三章到细化迭代3的模型完善关系", image.getWidth());
        box(g, 80, 170, 300, 110, "初始阶段\n用例、系统边界、关键规约", FRONTEND);
        box(g, 480, 170, 300, 110, "细化迭代1\n领域模型、SSD、操作契约", FRONTEND);
        box(g, 880, 170, 300, 110, "细化迭代2\nGoF模式与核心交易闭环", SERVICE);
        box(g, 1280, 170, 300, 110, "细化迭代3\n状态机、模型完善、文档汇总", DEVICE);
        arrow(g, 380, 225, 480, 225, "细化");
        arrow(g, 780, 225, 880, 225, "实现");
        arrow(g, 1180, 225, 1280, 225, "收束");
        box(g, 220, 470, 300, 130, "状态机补充\n待机、认证、业务选择、交易、凭条、退卡", DEVICE);
        box(g, 560, 470, 300, 130, "组件视图完善\n前端、后端、数据层职责对应", SERVICE);
        box(g, 900, 470, 300, 130, "设备模型落地\nATM001、状态、现金能力", DEVICE);
        box(g, 1240, 470, 300, 130, "安全边界收口\nsessionId 限制流水与凭条", ERROR);
        arrow(g, 1430, 280, 370, 470, "");
        arrow(g, 1430, 280, 710, 470, "");
        arrow(g, 1430, 280, 1050, 470, "");
        arrow(g, 1430, 280, 1390, 470, "");
        box(g, 515, 740, 670, 90, "最终交付：UML终稿、接口文档、测试证据、演示流程和报告第四章统一对应真实代码实现", DATA);
        arrow(g, 370, 600, 700, 740, "");
        arrow(g, 710, 600, 800, 740, "");
        arrow(g, 1050, 600, 900, 740, "");
        arrow(g, 1390, 600, 1000, 740, "");
        save(image, file);
    }
}
