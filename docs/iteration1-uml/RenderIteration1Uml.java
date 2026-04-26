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
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;

public class RenderIteration1Uml {
    private static final Color BACKGROUND = new Color(250, 252, 255);
    private static final Color LINE = new Color(45, 55, 72);
    private static final Color HEADER = new Color(214, 229, 255);
    private static final Color ENTITY = new Color(236, 247, 241);
    private static final Color SERVICE = new Color(255, 247, 220);
    private static final Color FRONTEND = new Color(232, 240, 255);
    private static final Color ERROR = new Color(255, 235, 235);
    private static Font font;

    public static void main(String[] args) throws Exception {
        font = loadFont();
        Path output = Path.of("docs/iteration1-uml/generated");
        Files.createDirectories(output);
        renderDomain(output.resolve("domain-model.png").toFile());
        renderSsdLogin(output.resolve("ssd-login.png").toFile());
        renderSsdBalance(output.resolve("ssd-balance.png").toFile());
        renderSsdWithdraw(output.resolve("ssd-withdraw.png").toFile());
        renderSsdTransfer(output.resolve("ssd-transfer.png").toFile());
        renderPackage(output.resolve("package-diagram.png").toFile());
        renderInteractionLoginBalance(output.resolve("interaction-login-balance.png").toFile());
        renderInteractionTransaction(output.resolve("interaction-transaction-skeleton.png").toFile());
        renderClassDiagram(output.resolve("class-diagram.png").toFile());
    }

    private static Font loadFont() {
        String[] candidates = {
                "/mnt/c/Windows/Fonts/simhei.ttf",
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
        g.setFont(font.deriveFont(Font.BOLD, 30f));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(new Color(29, 53, 87));
        g.drawString(text, (width - fm.stringWidth(text)) / 2, 48);
        g.setFont(font);
        g.setColor(LINE);
    }

    private static void box(Graphics2D g, int x, int y, int w, int h, String title, String[] lines, Color fill) {
        g.setColor(fill);
        g.fillRoundRect(x, y, w, h, 18, 18);
        g.setColor(LINE);
        g.drawRoundRect(x, y, w, h, 18, 18);
        g.setFont(font.deriveFont(Font.BOLD, 20f));
        drawCentered(g, title, x, y + 30, w);
        g.drawLine(x, y + 42, x + w, y + 42);
        g.setFont(font.deriveFont(Font.PLAIN, 17f));
        int cy = y + 68;
        for (String line : lines) {
            for (String wrapped : wrap(g, line, w - 22)) {
                g.drawString(wrapped, x + 12, cy);
                cy += 23;
            }
        }
    }

    private static void packageBox(Graphics2D g, int x, int y, int w, int h, String title, String[] items, Color fill) {
        g.setColor(fill);
        g.fillRoundRect(x, y, w, h, 16, 16);
        g.setColor(LINE);
        g.drawRoundRect(x, y, w, h, 16, 16);
        g.setFont(font.deriveFont(Font.BOLD, 22f));
        g.drawString(title, x + 18, y + 32);
        g.setFont(font.deriveFont(Font.PLAIN, 18f));
        int cy = y + 65;
        for (String item : items) {
            g.drawString("• " + item, x + 20, cy);
            cy += 30;
        }
    }

    private static void arrow(Graphics2D g, int x1, int y1, int x2, int y2, String label) {
        g.setColor(LINE);
        g.draw(new Line2D.Double(x1, y1, x2, y2));
        double angle = Math.atan2(y2 - y1, x2 - x1);
        int len = 13;
        int ax1 = (int) (x2 - len * Math.cos(angle - Math.PI / 7));
        int ay1 = (int) (y2 - len * Math.sin(angle - Math.PI / 7));
        int ax2 = (int) (x2 - len * Math.cos(angle + Math.PI / 7));
        int ay2 = (int) (y2 - len * Math.sin(angle + Math.PI / 7));
        g.drawLine(x2, y2, ax1, ay1);
        g.drawLine(x2, y2, ax2, ay2);
        if (label != null && !label.isEmpty()) {
            g.setFont(font.deriveFont(Font.PLAIN, 16f));
            g.setColor(new Color(65, 70, 85));
            g.drawString(label, (x1 + x2) / 2 - g.getFontMetrics().stringWidth(label) / 2, (y1 + y2) / 2 - 6);
        }
    }

    private static void sequence(Graphics2D g, int[] xs, String[] participants, String[][] messages, int height) {
        g.setFont(font.deriveFont(Font.BOLD, 17f));
        for (int i = 0; i < xs.length; i++) {
            box(g, xs[i] - 75, 80, 150, 50, participants[i], new String[]{}, HEADER);
            g.setColor(new Color(160, 170, 190));
            g.drawLine(xs[i], 130, xs[i], height - 30);
        }
        g.setFont(font.deriveFont(Font.PLAIN, 16f));
        int y = 165;
        for (String[] msg : messages) {
            int from = Integer.parseInt(msg[0]);
            int to = Integer.parseInt(msg[1]);
            arrow(g, xs[from], y, xs[to], y, msg[2]);
            y += 54;
        }
    }

    private static void classBox(Graphics2D g, int x, int y, int w, String name, String[] attrs, String[] methods, Color fill) {
        int h = 54 + attrs.length * 22 + methods.length * 22 + 20;
        g.setColor(fill);
        g.fillRoundRect(x, y, w, h, 12, 12);
        g.setColor(LINE);
        g.drawRoundRect(x, y, w, h, 12, 12);
        g.setFont(font.deriveFont(Font.BOLD, 19f));
        drawCentered(g, name, x, y + 29, w);
        int cy = y + 48;
        g.drawLine(x, cy, x + w, cy);
        g.setFont(font.deriveFont(Font.PLAIN, 15f));
        cy += 22;
        for (String attr : attrs) {
            g.drawString(attr, x + 12, cy);
            cy += 22;
        }
        g.drawLine(x, cy - 8, x + w, cy - 8);
        for (String method : methods) {
            g.drawString(method, x + 12, cy + 8);
            cy += 22;
        }
    }

    private static void drawCentered(Graphics2D g, String text, int x, int y, int w) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, x + (w - fm.stringWidth(text)) / 2, y);
    }

    private static List<String> wrap(Graphics2D g, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
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
        return lines;
    }

    private static void save(BufferedImage image, File file) throws Exception {
        ImageIO.write(image, "png", file);
    }

    private static void renderDomain(File file) throws Exception {
        BufferedImage image = new BufferedImage(1320, 860, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "ATM系统第一次迭代领域模型", image.getWidth());
        box(g, 70, 110, 230, 150, "Customer 客户", new String[]{"id", "customerName", "idCard", "phone"}, ENTITY);
        box(g, 390, 110, 230, 150, "BankCard 银行卡", new String[]{"cardNo", "password", "accountId"}, ENTITY);
        box(g, 710, 110, 230, 150, "Account 账户", new String[]{"accountNo", "balance", "accountType"}, ENTITY);
        box(g, 1010, 110, 230, 150, "Session 会话", new String[]{"sessionId", "cardNo", "valid"}, SERVICE);
        box(g, 220, 410, 260, 190, "Transaction 交易", new String[]{"transactionId", "transactionType", "amount", "transactionStatus"}, ENTITY);
        box(g, 590, 430, 230, 150, "Receipt 凭条", new String[]{"transactionId", "printTime"}, SERVICE);
        box(g, 930, 410, 260, 190, "ATMDevice 设备", new String[]{"screen", "cardReader", "cashDispenser", "printer"}, SERVICE);
        arrow(g, 300, 185, 390, 185, "1..* 持有");
        arrow(g, 620, 185, 710, 185, "关联账户");
        arrow(g, 940, 185, 1010, 185, "建立");
        arrow(g, 825, 260, 480, 410, "0..* 发生交易");
        arrow(g, 480, 505, 590, 505, "可打印");
        arrow(g, 930, 505, 480, 505, "设备参与");
        save(image, file);
    }

    private static void renderSsdLogin(File file) throws Exception {
        BufferedImage image = new BufferedImage(1360, 760, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "登录认证系统顺序图", image.getWidth());
        sequence(g, new int[]{110, 300, 500, 710, 930, 1130}, new String[]{"客户", "LoginView", "atm.js", "AuthController", "AuthService", "TokenManager/DB"}, new String[][]{
                {"0", "1", "输入卡号和密码"},
                {"1", "2", "login(cardNo,password)"},
                {"2", "3", "POST /auth/login"},
                {"3", "4", "login(LoginRequest)"},
                {"4", "5", "查询银行卡并生成 sessionId"},
                {"5", "4", "BankCard + sessionId"},
                {"4", "3", "LoginResponse"},
                {"3", "2", "Result<LoginResponse>"},
                {"2", "1", "保存 sessionId"},
                {"1", "0", "进入主菜单"}
        }, image.getHeight());
        save(image, file);
    }

    private static void renderSsdBalance(File file) throws Exception {
        BufferedImage image = new BufferedImage(1360, 720, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "查询余额系统顺序图", image.getWidth());
        sequence(g, new int[]{110, 300, 500, 710, 930, 1130}, new String[]{"客户", "BalanceView", "atm.js", "AccountController", "TokenManager", "AccountService/DB"}, new String[][]{
                {"0", "1", "选择查询余额"},
                {"1", "2", "getBalance(sessionId)"},
                {"2", "3", "GET /accounts/balance"},
                {"3", "4", "校验 sessionId"},
                {"4", "3", "cardNo"},
                {"3", "5", "getBalance(cardNo)"},
                {"5", "3", "BalanceResponse"},
                {"3", "2", "Result<BalanceResponse>"},
                {"2", "1", "balance"},
                {"1", "0", "展示余额"}
        }, image.getHeight());
        save(image, file);
    }

    private static void renderSsdWithdraw(File file) throws Exception {
        BufferedImage image = new BufferedImage(1320, 650, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "取款交易骨架系统顺序图", image.getWidth());
        sequence(g, new int[]{120, 340, 560, 790, 1030}, new String[]{"客户", "取款页面", "API", "TransactionController", "TransactionServiceImpl"}, new String[][]{
                {"0", "1", "选择取款并输入金额"},
                {"1", "2", "withdraw(sessionId,amount)"},
                {"2", "3", "POST /transactions/withdraw"},
                {"3", "4", "withdraw(WithdrawRequest)"},
                {"4", "3", "ApiException 501"},
                {"3", "2", "Result(code=501)"},
                {"2", "1", "骨架提示"},
                {"1", "0", "提示第二次迭代实现"}
        }, image.getHeight());
        save(image, file);
    }

    private static void renderSsdTransfer(File file) throws Exception {
        BufferedImage image = new BufferedImage(1320, 650, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "转账交易骨架系统顺序图", image.getWidth());
        sequence(g, new int[]{120, 340, 560, 790, 1030}, new String[]{"客户", "转账页面", "API", "TransactionController", "TransactionServiceImpl"}, new String[][]{
                {"0", "1", "输入目标账户和金额"},
                {"1", "2", "transfer(sessionId,target,amount)"},
                {"2", "3", "POST /transactions/transfer"},
                {"3", "4", "transfer(TransferRequest)"},
                {"4", "3", "ApiException 501"},
                {"3", "2", "Result(code=501)"},
                {"2", "1", "骨架提示"},
                {"1", "0", "提示第二次迭代实现"}
        }, image.getHeight());
        save(image, file);
    }

    private static void renderPackage(File file) throws Exception {
        BufferedImage image = new BufferedImage(1280, 820, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "第一次迭代 UML 包图", image.getWidth());
        packageBox(g, 70, 110, 300, 240, "frontend", new String[]{"views", "api", "router", "stores", "styles"}, FRONTEND);
        packageBox(g, 500, 100, 330, 270, "atm-server-auth", new String[]{"controller", "service", "mapper", "entity", "dto/common", "util"}, SERVICE);
        packageBox(g, 930, 120, 290, 230, "transaction", new String[]{"controller", "service", "dto", "entity"}, ENTITY);
        packageBox(g, 500, 500, 330, 180, "resources/dev", new String[]{"schema.sql", "data.sql", "H2 dev profile"}, new Color(240, 240, 245));
        arrow(g, 370, 230, 500, 230, "HTTP /api/atm");
        arrow(g, 830, 230, 930, 230, "Maven 编译引入");
        arrow(g, 665, 370, 665, 500, "数据初始化");
        save(image, file);
    }

    private static void renderInteractionLoginBalance(File file) throws Exception {
        BufferedImage image = new BufferedImage(1320, 760, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "登录与余额查询 UML 交互图", image.getWidth());
        box(g, 70, 130, 230, 110, "LoginView", new String[]{"提交登录表单"}, FRONTEND);
        box(g, 70, 400, 230, 110, "BalanceView", new String[]{"读取 sessionId", "展示余额"}, FRONTEND);
        box(g, 380, 250, 210, 120, "atm.js/http.js", new String[]{"封装 axios 请求"}, FRONTEND);
        box(g, 680, 130, 250, 110, "AuthController", new String[]{"login", "logout"}, SERVICE);
        box(g, 680, 400, 250, 110, "AccountController", new String[]{"profile", "balance"}, SERVICE);
        box(g, 1030, 130, 220, 110, "AuthServiceImpl", new String[]{"密码校验"}, ENTITY);
        box(g, 1030, 400, 220, 110, "AccountServiceImpl", new String[]{"账户查询"}, ENTITY);
        box(g, 1030, 270, 220, 80, "TokenManager", new String[]{"sessionId/cardNo"}, SERVICE);
        arrow(g, 300, 185, 380, 290, "login");
        arrow(g, 300, 455, 380, 330, "getBalance");
        arrow(g, 590, 290, 680, 185, "/auth/login");
        arrow(g, 590, 330, 680, 455, "/accounts/balance");
        arrow(g, 930, 185, 1030, 185, "调用");
        arrow(g, 930, 455, 1030, 455, "调用");
        arrow(g, 930, 430, 1030, 315, "校验会话");
        save(image, file);
    }

    private static void renderInteractionTransaction(File file) throws Exception {
        BufferedImage image = new BufferedImage(1260, 680, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "交易模块骨架 UML 交互图", image.getWidth());
        box(g, 70, 130, 260, 120, "PlaceholderView", new String[]{"取款/存款/转账入口", "第一次迭代占位"}, FRONTEND);
        box(g, 440, 130, 270, 140, "TransactionController", new String[]{"withdraw", "deposit", "transfer", "getTransaction"}, SERVICE);
        box(g, 830, 130, 280, 140, "TransactionServiceImpl", new String[]{"统一抛出 ApiException", "HTTP 501"}, ERROR);
        box(g, 440, 390, 270, 120, "交易 DTO", new String[]{"WithdrawRequest", "DepositRequest", "TransferRequest"}, ENTITY);
        box(g, 830, 390, 280, 120, "GlobalExceptionHandler", new String[]{"Result.error(501,msg)"}, SERVICE);
        arrow(g, 330, 190, 440, 190, "后续 API 调用");
        arrow(g, 710, 190, 830, 190, "service method");
        arrow(g, 965, 270, 965, 390, "异常处理");
        arrow(g, 830, 450, 710, 450, "结构化响应");
        arrow(g, 575, 270, 575, 390, "请求对象");
        save(image, file);
    }

    private static void renderClassDiagram(File file) throws Exception {
        BufferedImage image = new BufferedImage(1400, 1050, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas(image);
        title(g, "第一次迭代后端核心类图", image.getWidth());
        classBox(g, 60, 110, 300, "AuthController", new String[]{}, new String[]{"+login(LoginRequest)", "+logout(LogoutRequest)"}, SERVICE);
        classBox(g, 60, 390, 300, "AccountController", new String[]{}, new String[]{"+getAccountInfo(...)", "+getBalance(...)"}, SERVICE);
        classBox(g, 60, 690, 300, "TransactionController", new String[]{}, new String[]{"+withdraw(...)", "+deposit(...)", "+transfer(...)"}, SERVICE);
        classBox(g, 500, 110, 300, "AuthServiceImpl", new String[]{"-bankCardMapper", "-tokenManager"}, new String[]{"+login(...)", "+logout(...)"}, ENTITY);
        classBox(g, 500, 390, 300, "AccountServiceImpl", new String[]{"-accountMapper", "-bankCardMapper"}, new String[]{"+getAccountInfo(...)", "+getBalance(...)"}, ENTITY);
        classBox(g, 500, 690, 300, "TransactionServiceImpl", new String[]{}, new String[]{"+withdraw(...)", "+deposit(...)", "+transfer(...)"}, ERROR);
        classBox(g, 970, 110, 300, "TokenManager", new String[]{"-Map tokenCardMap"}, new String[]{"+generateToken()", "+isValidToken()"}, SERVICE);
        classBox(g, 970, 390, 300, "Entity/Mapper", new String[]{"Customer", "Account", "BankCard"}, new String[]{"Mapper 查询账户数据"}, ENTITY);
        classBox(g, 970, 690, 300, "Transaction/DTO", new String[]{"Transaction", "WithdrawRequest", "TransferRequest"}, new String[]{"Result<T> 返回结构"}, ENTITY);
        arrow(g, 360, 190, 500, 190, "uses");
        arrow(g, 800, 190, 970, 190, "token");
        arrow(g, 360, 470, 500, 470, "uses");
        arrow(g, 800, 470, 970, 470, "query");
        arrow(g, 360, 770, 500, 770, "uses");
        arrow(g, 800, 770, 970, 770, "DTO/entity");
        save(image, file);
    }
}
