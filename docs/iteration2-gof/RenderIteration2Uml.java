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

public class RenderIteration2Uml {
    private static final Color BACKGROUND = new Color(250, 252, 255);
    private static final Color LINE = new Color(45, 55, 72);
    private static final Color TITLE = new Color(24, 55, 91);
    private static final Color FRONTEND = new Color(232, 241, 255);
    private static final Color CONTROL = new Color(226, 239, 218);
    private static final Color SERVICE = new Color(255, 247, 219);
    private static final Color DATA = new Color(238, 234, 255);
    private static final Color ERROR = new Color(255, 232, 232);
    private static final Color EXTENSION = new Color(235, 248, 250);
    private static Font font;

    public static void main(String[] args) throws Exception {
        font = loadFont();
        Path output = Path.of("docs/iteration2-gof/generated");
        Files.createDirectories(output);
        renderPatternOverview(output.resolve("pattern-overview.png").toFile());
        renderClassDiagram(output.resolve("transaction-class-diagram.png").toFile());
        renderTemplateFlow(output.resolve("transaction-template-flow.png").toFile());
        renderTransferSequence(output.resolve("transfer-sequence.png").toFile());
        renderAdapterFlow(output.resolve("adapter-contract-flow.png").toFile());
        renderFutureMap(output.resolve("future-extension-map.png").toFile());
    }

    private static Font loadFont() {
        String[] candidates = {
                "C:/Windows/Fonts/msyh.ttc",
                "C:/Windows/Fonts/simhei.ttf",
                "C:/Windows/Fonts/simsun.ttc",
                "/mnt/c/Windows/Fonts/msyh.ttc",
                "/mnt/c/Windows/Fonts/simhei.ttf",
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

    private static void box(Graphics2D g, int x, int y, int w, int h, String title, String[] lines, Color fill) {
        g.setColor(fill);
        g.fillRoundRect(x, y, w, h, 18, 18);
        g.setColor(LINE);
        g.drawRoundRect(x, y, w, h, 18, 18);
        g.setFont(font.deriveFont(Font.BOLD, 22f));
        drawCentered(g, title, x, y + 34, w);
        g.drawLine(x, y + 48, x + w, y + 48);
        g.setFont(font.deriveFont(Font.PLAIN, 18f));
        int cy = y + 76;
        for (String line : lines) {
            for (String wrapped : wrap(g, line, w - 28)) {
                g.drawString(wrapped, x + 14, cy);
                cy += 25;
            }
        }
    }

    private static void classBox(Graphics2D g, int x, int y, int w, String name, String[] attrs, String[] methods, Color fill) {
        int h = 58 + attrs.length * 24 + methods.length * 24 + 24;
        g.setColor(fill);
        g.fillRoundRect(x, y, w, h, 12, 12);
        g.setColor(LINE);
        g.drawRoundRect(x, y, w, h, 12, 12);
        g.setFont(font.deriveFont(Font.BOLD, 20f));
        drawCentered(g, name, x, y + 32, w);
        int cy = y + 50;
        g.drawLine(x, cy, x + w, cy);
        g.setFont(font.deriveFont(Font.PLAIN, 15f));
        cy += 24;
        for (String attr : attrs) {
            g.drawString(attr, x + 12, cy);
            cy += 24;
        }
        g.drawLine(x, cy - 10, x + w, cy - 10);
        for (String method : methods) {
            g.drawString(method, x + 12, cy + 8);
            cy += 24;
        }
    }

    private static void flowNode(Graphics2D g, int x, int y, int w, int h, String text, Color fill) {
        g.setColor(fill);
        g.fillRoundRect(x, y, w, h, 16, 16);
        g.setColor(LINE);
        g.drawRoundRect(x, y, w, h, 16, 16);
        g.setFont(font.deriveFont(Font.BOLD, 19f));
        List<String> lines = wrap(g, text, w - 28);
        int cy = y + (h - lines.size() * 24) / 2 + 20;
        for (String line : lines) {
            drawCentered(g, line, x, cy, w);
            cy += 26;
        }
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
            g.setFont(font.deriveFont(Font.PLAIN, 16f));
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
        String[] manualLines = text.split("\\n", -1);
        for (String manualLine : manualLines) {
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

    private static void renderPatternOverview(File file) throws Exception {
        BufferedImage image = new BufferedImage(1800, 900, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "细化迭代2 GoF模式应用总览", image.getWidth());
        box(g, 60, 130, 250, 180, "Vue 前端", new String[]{
                "取款/存款/转账/修改密码页面",
                "统一通过 atm.js 调用接口",
                "保存 sessionId 与最新余额"
        }, FRONTEND);
        box(g, 370, 130, 250, 180, "Controller Facade", new String[]{
                "对外暴露稳定 REST API",
                "统一返回 Result<T>",
                "隔离前端与业务细节"
        }, CONTROL);
        box(g, 680, 130, 300, 180, "TransactionService Facade", new String[]{
                "隐藏会话校验、金额规则",
                "隐藏余额更新与流水写入",
                "封装 withdraw/deposit/transfer"
        }, SERVICE);
        box(g, 1050, 130, 290, 180, "Spring AOP Proxy", new String[]{
                "@Transactional 形成事务边界",
                "异常时整体回滚",
                "服务对象由容器代理调用"
        }, DATA);
        box(g, 1410, 130, 300, 180, "MyBatis Mapper Proxy", new String[]{
                "Mapper 接口由运行时代理实现",
                "AccountMapper/TransactionMapper",
                "隐藏 SQL 执行和结果映射"
        }, DATA);
        arrow(g, 310, 220, 370, 220, "HTTP");
        arrow(g, 620, 220, 680, 220, "调用");
        arrow(g, 980, 220, 1050, 220, "事务代理");
        arrow(g, 1340, 220, 1410, 220, "数据访问");

        box(g, 160, 470, 330, 210, "Adapter 兼容层", new String[]{
                "SessionValidator 以 sessionId 为主契约",
                "token 仅作为旧字段别名",
                "/accounts/* 为主路径，/account/* 兼容"
        }, EXTENSION);
        box(g, 590, 470, 330, 210, "Template Method 思想", new String[]{
                "请求校验 -> 会话解析 -> 业务规则",
                "创建 PENDING 流水 -> 更新账户",
                "标记 SUCCESS -> 返回响应"
        }, EXTENSION);
        box(g, 1020, 470, 330, 210, "Factory/Creator 思想", new String[]{
                "createTransaction 统一构造流水",
                "TransactionUtils 生成 transactionId",
                "避免交易编号与状态初始化散落"
        }, EXTENSION);
        box(g, 1450, 470, 300, 210, "第三次迭代拓展", new String[]{
                "Strategy: 凭条/手续费/风控规则",
                "Command: ATM 操作排队审计",
                "State: 会话与设备状态"
        }, EXTENSION);
        arrow(g, 755, 310, 755, 470, "公共流程");
        dashedArrow(g, 325, 470, 680, 310, "统一口径");
        dashedArrow(g, 1185, 470, 830, 310, "对象创建");
        dashedArrow(g, 1600, 470, 1195, 310, "演进");
        save(image, file);
    }

    private static void renderClassDiagram(File file) throws Exception {
        BufferedImage image = new BufferedImage(1900, 1180, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "第二次迭代交易模块设计类图", image.getWidth());

        classBox(g, 70, 110, 290, "TransactionController", new String[]{}, new String[]{
                "+withdraw(request)",
                "+deposit(request)",
                "+transfer(request)",
                "+getTransaction(id)"
        }, CONTROL);
        classBox(g, 460, 110, 300, "<<interface>> TransactionService", new String[]{}, new String[]{
                "+withdraw(request)",
                "+deposit(request)",
                "+transfer(request)",
                "+getTransactionById(id)"
        }, SERVICE);
        classBox(g, 880, 80, 390, "TransactionServiceImpl", new String[]{
                "-SessionValidator",
                "-AccountMapper",
                "-TransactionMapper"
        }, new String[]{
                "+withdraw(request)",
                "+deposit(request)",
                "+transfer(request)",
                "-loadAccountContext(sessionId)",
                "-createTransaction(...)",
                "-markSuccess(...)"
        }, SERVICE);
        classBox(g, 1440, 80, 320, "SessionValidator", new String[]{"-TokenManager"}, new String[]{
                "+validateAndGetCardNo(sessionId, token)"
        }, EXTENSION);
        classBox(g, 70, 500, 300, "Result<T> / ApiException", new String[]{
                "code, message, data, timestamp",
                "HttpStatus + message"
        }, new String[]{
                "+success(data)",
                "+error(code,message)"
        }, ERROR);
        classBox(g, 460, 500, 300, "AccountMapper", new String[]{}, new String[]{
                "+selectById(id)",
                "+selectByAccountNo(no)",
                "+subtractBalance(id,amount)",
                "+addBalance(id,amount)"
        }, DATA);
        classBox(g, 850, 500, 330, "TransactionMapper", new String[]{}, new String[]{
                "+insert(transaction)",
                "+updateStatus(...)",
                "+selectByTransactionId(id)",
                "+sumTodayAmount(cardNo,type)"
        }, DATA);
        classBox(g, 1280, 500, 330, "Transaction", new String[]{
                "transactionId",
                "transactionType",
                "amount",
                "balanceBefore/balanceAfter",
                "transactionStatus",
                "targetAccountNo"
        }, new String[]{}, DATA);
        classBox(g, 320, 870, 330, "Withdraw/Deposit/Transfer DTO", new String[]{
                "sessionId",
                "amount",
                "printReceipt",
                "targetAccountNo(transfer)"
        }, new String[]{}, FRONTEND);
        classBox(g, 800, 870, 330, "GoF 应用点", new String[]{
                "Facade: Service 对外隐藏内部细节",
                "Proxy: Spring/MyBatis 运行时代理",
                "Adapter: token -> sessionId 兼容",
                "Template Method 思想: 交易公共骨架"
        }, new String[]{}, EXTENSION);

        arrow(g, 360, 210, 460, 210, "依赖");
        dashedArrow(g, 760, 210, 880, 210, "实现");
        arrow(g, 1270, 190, 1440, 190, "会话");
        arrow(g, 1060, 430, 620, 500, "账户读写");
        arrow(g, 1040, 430, 1015, 500, "流水读写");
        arrow(g, 1160, 430, 1280, 570, "创建/转换");
        arrow(g, 210, 310, 210, 500, "统一响应");
        arrow(g, 1040, 820, 965, 870, "设计依据");
        arrow(g, 485, 870, 250, 310, "请求/响应");
        save(image, file);
    }

    private static void renderTemplateFlow(File file) throws Exception {
        BufferedImage image = new BufferedImage(1600, 1350, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "交易处理模板流程与异常分支", image.getWidth());

        flowNode(g, 650, 90, 300, 70, "开始：接收交易请求", FRONTEND);
        flowNode(g, 650, 210, 300, 90, "校验请求体与金额\nrequireRequest()\nrequirePositiveAmount()", CONTROL);
        flowNode(g, 650, 350, 300, 90, "SessionValidator\n解析 sessionId -> cardNo", EXTENSION);
        flowNode(g, 650, 490, 300, 90, "加载 AccountContext\nBankCard + Account", DATA);
        flowNode(g, 650, 640, 300, 90, "按交易类型执行规则", SERVICE);
        flowNode(g, 120, 810, 340, 150, "取款规则\n100整数倍\n单次5000\n单日20000\n余额充足", EXTENSION);
        flowNode(g, 630, 810, 340, 150, "存款规则\n金额大于0\n单次50000\n账户存在", EXTENSION);
        flowNode(g, 1140, 810, 340, 150, "转账规则\n目标账户存在\n不能自转账\n单次10000\n单日50000\n余额充足", EXTENSION);
        flowNode(g, 650, 1050, 300, 90, "创建 PENDING 流水\nTransactionUtils 生成编号", DATA);
        flowNode(g, 650, 1190, 300, 90, "更新余额并标记 SUCCESS\n返回 transactionId 与余额", CONTROL);
        flowNode(g, 1160, 1120, 330, 120, "异常分支\n抛出 ApiException\nGlobalExceptionHandler\n转为 Result 错误响应", ERROR);

        arrow(g, 800, 160, 800, 210, "");
        arrow(g, 800, 300, 800, 350, "");
        arrow(g, 800, 440, 800, 490, "");
        arrow(g, 800, 580, 800, 640, "");
        arrow(g, 710, 730, 300, 810, "withdraw");
        arrow(g, 800, 730, 800, 810, "deposit");
        arrow(g, 890, 730, 1310, 810, "transfer");
        arrow(g, 290, 960, 650, 1095, "通过");
        arrow(g, 800, 960, 800, 1050, "通过");
        arrow(g, 1310, 960, 950, 1095, "通过");
        arrow(g, 800, 1140, 800, 1190, "");
        dashedArrow(g, 950, 255, 1160, 1160, "校验失败");
        dashedArrow(g, 1480, 885, 1340, 1120, "业务失败");
        save(image, file);
    }

    private static void renderTransferSequence(File file) throws Exception {
        BufferedImage image = new BufferedImage(2100, 1350, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "转账业务顺序图：事务代理与双账户一致性", image.getWidth());
        String[] names = {"客户", "TransferView", "atm.js", "Controller", "Spring Proxy", "ServiceImpl", "Session\nValidator", "Account\nMapper", "Transaction\nMapper", "DB"};
        int[] xs = {100, 300, 500, 710, 930, 1160, 1390, 1600, 1810, 1990};
        g.setFont(font.deriveFont(Font.BOLD, 16f));
        for (int i = 0; i < xs.length; i++) {
            box(g, xs[i] - 80, 90, 160, 58, names[i], new String[]{}, i < 3 ? FRONTEND : i < 6 ? SERVICE : DATA);
            g.setColor(new Color(160, 170, 190));
            g.drawLine(xs[i], 148, xs[i], 1260);
        }
        String[][] messages = {
                {"0", "1", "输入目标账户和金额"},
                {"1", "2", "transfer(payload)"},
                {"2", "3", "POST /transactions/transfer"},
                {"3", "4", "调用 service.transfer"},
                {"4", "5", "开启事务"},
                {"5", "6", "validateAndGetCardNo"},
                {"6", "5", "返回 cardNo"},
                {"5", "7", "查询源账户/目标账户"},
                {"7", "9", "select account"},
                {"9", "7", "账户数据"},
                {"7", "5", "source + target"},
                {"5", "8", "sumTodayAmount"},
                {"8", "9", "查询当日累计"},
                {"9", "8", "累计金额"},
                {"8", "5", "返回限额依据"},
                {"5", "8", "insert 转出PENDING"},
                {"5", "7", "subtract source"},
                {"5", "7", "add target"},
                {"5", "8", "insert 转入SUCCESS"},
                {"5", "8", "update 转出SUCCESS"},
                {"5", "4", "TransferResponse"},
                {"4", "3", "提交事务"},
                {"3", "2", "Result.success"},
                {"2", "1", "展示成功与最新余额"}
        };
        int y = 185;
        g.setFont(font.deriveFont(Font.PLAIN, 15f));
        for (String[] msg : messages) {
            int from = Integer.parseInt(msg[0]);
            int to = Integer.parseInt(msg[1]);
            arrow(g, xs[from], y, xs[to], y, msg[2]);
            y += 42;
        }
        flowNode(g, 1350, 1130, 560, 110, "任一余额更新或流水写入失败时抛出 ApiException，Spring 事务代理回滚本次转账，避免只扣款不入账。", ERROR);
        save(image, file);
    }

    private static void renderAdapterFlow(File file) throws Exception {
        BufferedImage image = new BufferedImage(1600, 830, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "公开契约统一与 Adapter 兼容流程", image.getWidth());
        box(g, 90, 180, 280, 170, "旧调用口径", new String[]{
                "token 参数",
                "/api/atm/account/*",
                "仅兼容保留"
        }, ERROR);
        box(g, 90, 500, 280, 170, "新公开契约", new String[]{
                "sessionId 参数",
                "/api/atm/accounts/*",
                "前端/OpenAPI/HTTP示例统一"
        }, FRONTEND);
        box(g, 520, 320, 300, 200, "Controller 兼容入口", new String[]{
                "同时接收新老路径或参数",
                "优先读取 sessionId",
                "旧 token 不再作为文档主字段"
        }, CONTROL);
        box(g, 950, 320, 300, 200, "SessionValidator Adapter", new String[]{
                "resolvedSessionId = sessionId ?: token",
                "统一调用 TokenManager",
                "返回 cardNo 给业务服务"
        }, EXTENSION);
        box(g, 1320, 320, 220, 200, "业务服务", new String[]{
                "AccountService",
                "AuthService",
                "TransactionService",
                "不感知旧字段"
        }, SERVICE);
        arrow(g, 370, 265, 520, 390, "兼容");
        arrow(g, 370, 585, 520, 450, "主契约");
        arrow(g, 820, 420, 950, 420, "适配");
        arrow(g, 1250, 420, 1320, 420, "cardNo/DTO");
        save(image, file);
    }

    private static void renderFutureMap(File file) throws Exception {
        BufferedImage image = new BufferedImage(1650, 900, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "基于细化迭代2的第三次迭代拓展点", image.getWidth());
        flowNode(g, 650, 95, 350, 100, "细化迭代2核心交易闭环\n取款 / 存款 / 转账 / 修改密码 / 交易详情", SERVICE);
        flowNode(g, 90, 330, 300, 160, "Strategy\n手续费、凭条模板、风控限额、打印策略按场景切换", EXTENSION);
        flowNode(g, 470, 330, 300, 160, "Command\n把取款、存款、转账、退卡封装为可审计操作", EXTENSION);
        flowNode(g, 850, 330, 300, 160, "State\n待机、认证中、业务选择、交易中、退卡状态迁移", EXTENSION);
        flowNode(g, 1230, 330, 300, 160, "Observer\n交易成功事件触发凭条、流水列表和设备提示刷新", EXTENSION);
        flowNode(g, 310, 650, 1030, 120, "细化迭代3：交易流水列表、凭条展示、设备状态、状态图/组件图/部署图与最终文档汇总", CONTROL);
        arrow(g, 720, 195, 240, 330, "");
        arrow(g, 780, 195, 620, 330, "");
        arrow(g, 870, 195, 1000, 330, "");
        arrow(g, 930, 195, 1380, 330, "");
        arrow(g, 240, 490, 620, 650, "");
        arrow(g, 620, 490, 735, 650, "");
        arrow(g, 1000, 490, 930, 650, "");
        arrow(g, 1380, 490, 1030, 650, "");
        save(image, file);
    }
}
