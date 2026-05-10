from __future__ import annotations

import html
import re
import shutil
import struct
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile


ROOT = Path(__file__).resolve().parents[2]
SOURCE_DOCX = Path(
    "/mnt/c/Users/Alex/WPSDrive/472540441/WPS云盘/大二下实验/UML实验设计/软件工程1班第9小组UML实验报告.docx"
)
OUTPUT_DOCX = ROOT / "docs/iteration1-uml/generated/软件工程1班第9小组UML实验报告_第二章完成.docx"
GENERATED_DIR = ROOT / "docs/iteration1-uml/generated"


DIAGRAMS = [
    ("domain-model.png", "图 2-1 ATM 系统第一次迭代领域模型"),
    ("ssd-login.png", "图 2-2 登录认证系统顺序图"),
    ("ssd-balance.png", "图 2-3 查询余额系统顺序图"),
    ("ssd-withdraw.png", "图 2-4 取款交易骨架系统顺序图"),
    ("ssd-transfer.png", "图 2-5 转账交易骨架系统顺序图"),
    ("package-diagram.png", "图 2-6 第一次迭代 UML 包图"),
    ("interaction-login-balance.png", "图 2-7 登录与余额查询 UML 交互图"),
    ("interaction-transaction-skeleton.png", "图 2-8 交易模块骨架 UML 交互图"),
    ("class-diagram.png", "图 2-9 第一次迭代后端核心类图"),
]


def esc(text: str) -> str:
    return html.escape(text, quote=False)


def run_text(text: str, *, bold: bool = False, size: int = 21) -> str:
    bold_xml = "<w:b/>" if bold else ""
    return (
        "<w:r>"
        "<w:rPr>"
        '<w:rFonts w:ascii="宋体" w:hAnsi="宋体" w:eastAsia="宋体"/>'
        f"{bold_xml}<w:sz w:val=\"{size}\"/><w:szCs w:val=\"{size}\"/>"
        "</w:rPr>"
        f'<w:t xml:space="preserve">{esc(text)}</w:t>'
        "</w:r>"
    )


def paragraph(text: str = "", *, bold: bool = False, size: int = 21, align: str | None = None) -> str:
    ppr = ""
    if align:
        ppr = f'<w:pPr><w:jc w:val="{align}"/></w:pPr>'
    return f"<w:p>{ppr}{run_text(text, bold=bold, size=size)}</w:p>"


def heading(text: str, level: int) -> str:
    size = 32 if level == 1 else 26 if level == 2 else 23
    align = "center" if level == 1 else None
    return paragraph(text, bold=True, size=size, align=align)


def page_break() -> str:
    return '<w:p><w:r><w:br w:type="page"/></w:r></w:p>'


def table(rows: list[list[str]]) -> str:
    cell_width = 9000 // max(len(row) for row in rows)
    row_xml = []
    for row_index, row in enumerate(rows):
        cells = []
        for cell in row:
            fill = '<w:shd w:fill="D9EAF7"/>' if row_index == 0 else ""
            cells.append(
                "<w:tc>"
                f'<w:tcPr><w:tcW w:w="{cell_width}" w:type="dxa"/>{fill}</w:tcPr>'
                f"{paragraph(cell, bold=row_index == 0, size=20)}"
                "</w:tc>"
            )
        row_xml.append("<w:tr>" + "".join(cells) + "</w:tr>")
    borders = (
        "<w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/>"
        "<w:tblBorders>"
        '<w:top w:val="single" w:sz="4" w:space="0" w:color="666666"/>'
        '<w:left w:val="single" w:sz="4" w:space="0" w:color="666666"/>'
        '<w:bottom w:val="single" w:sz="4" w:space="0" w:color="666666"/>'
        '<w:right w:val="single" w:sz="4" w:space="0" w:color="666666"/>'
        '<w:insideH w:val="single" w:sz="4" w:space="0" w:color="999999"/>'
        '<w:insideV w:val="single" w:sz="4" w:space="0" w:color="999999"/>'
        "</w:tblBorders></w:tblPr>"
    )
    return "<w:tbl>" + borders + "".join(row_xml) + "</w:tbl>"


def png_size(path: Path) -> tuple[int, int]:
    data = path.read_bytes()
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError(f"{path} is not a PNG file")
    return struct.unpack(">II", data[16:24])


def image_paragraph(rel_id: str, image_path: Path, doc_pr_id: int) -> str:
    width_px, height_px = png_size(image_path)
    max_width_emu = int(6.2 * 914400)
    cx = max_width_emu
    cy = int(max_width_emu * height_px / width_px)
    return f"""
<w:p>
  <w:pPr><w:jc w:val="center"/></w:pPr>
  <w:r>
    <w:drawing>
      <wp:inline distT="0" distB="0" distL="0" distR="0"
          xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
          xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture">
        <wp:extent cx="{cx}" cy="{cy}"/>
        <wp:effectExtent l="0" t="0" r="0" b="0"/>
        <wp:docPr id="{doc_pr_id}" name="iteration1-uml-{doc_pr_id}"/>
        <wp:cNvGraphicFramePr>
          <a:graphicFrameLocks noChangeAspect="1"/>
        </wp:cNvGraphicFramePr>
        <a:graphic>
          <a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
            <pic:pic>
              <pic:nvPicPr>
                <pic:cNvPr id="{doc_pr_id}" name="{image_path.name}"/>
                <pic:cNvPicPr/>
              </pic:nvPicPr>
              <pic:blipFill>
                <a:blip r:embed="{rel_id}"/>
                <a:stretch><a:fillRect/></a:stretch>
              </pic:blipFill>
              <pic:spPr>
                <a:xfrm>
                  <a:off x="0" y="0"/>
                  <a:ext cx="{cx}" cy="{cy}"/>
                </a:xfrm>
                <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>
              </pic:spPr>
            </pic:pic>
          </a:graphicData>
        </a:graphic>
      </wp:inline>
    </w:drawing>
  </w:r>
</w:p>
"""


def diagram_block(rel_id: str, file_name: str, caption: str, doc_pr_id: int) -> str:
    path = GENERATED_DIR / file_name
    return image_paragraph(rel_id, path, doc_pr_id) + paragraph(caption, size=20, align="center")


def chapter_xml(rel_ids: dict[str, str]) -> str:
    parts: list[str] = [page_break()]
    parts.append(heading("第二章 细化迭代1", 1))
    parts.append(paragraph(
        "根据总体迭代计划，第二章对应细化迭代 1，要求提交 Domain Models、System Sequence Diagrams、Operation Contracts、UML Package Diagrams、UML Interaction Diagrams 和 UML Class Diagrams。本章基于项目当前实际代码展开，不把第二次迭代才实现的完整取款、存款、转账业务提前写成已完成能力，而是如实区分“已实现的登录、账户查询、前端主流程”和“已接入但仍为骨架的交易模块”。"
    ))
    parts.append(paragraph(
        "本次迭代后的工程基线如下：前端位于 frontend 目录，已包含 WelcomeView、LoginView、MenuView、BalanceView、PlaceholderView，以及 router、Pinia session store 和 api/http 封装；后端主模块为 atm-server-auth，包含 AuthController、AccountController、AuthServiceImpl、AccountServiceImpl、TokenManager、Mapper、实体与 DTO；交易处理模块位于 transaction 目录，已经通过 Maven 编译配置接入主后端，并提供 TransactionController、TransactionService、TransactionServiceImpl、交易 DTO 与 Transaction 实体骨架。"
    ))

    parts.append(heading("2.1 本次迭代目标与实现范围", 2))
    parts.append(paragraph(
        "细化迭代 1 的目标不是一次性完成全部 ATM 交易功能，而是在初始阶段用例与系统边界的基础上，把关键业务对象、系统操作、对象协作和代码包结构进一步明确，并用可运行代码验证主要接口边界。当前版本已经可以通过 dev 内存数据库完成登录、会话建立、账户信息查询和余额查询；主菜单已预留取款、存款、转账、修改密码和凭条入口；交易接口已挂载到 /api/atm/transactions 路径下，但服务实现按第一次迭代计划返回 501，表示完整业务逻辑将在后续迭代实现。"
    ))
    parts.append(table([
        ["迭代计划要求", "本章对应产物", "项目实际依据"],
        ["Domain Models", "领域模型图与对象说明", "Customer、BankCard、Account、Transaction、Session 等实体/概念"],
        ["System Sequence Diagrams", "登录、余额查询、取款骨架、转账骨架系统顺序图", "前端 views、api 封装、Controller、Service 调用路径"],
        ["Operation Contracts", "登录、查询余额、取款、转账系统操作契约", "AuthController、AccountController、TransactionController"],
        ["UML Package Diagrams", "第一次迭代包图", "frontend、atm-server-auth、transaction 三部分目录结构"],
        ["UML Interaction Diagrams", "登录/余额查询协作图、交易骨架协作图", "实际 Controller-Service-Mapper-DTO 协作关系"],
        ["UML Class Diagrams", "后端核心类图", "认证、账户、交易相关 Java 类和接口"],
    ]))

    parts.append(heading("2.2 领域模型", 2))
    parts.append(paragraph(
        "领域模型用于描述 ATM 系统中较稳定的业务概念及其关系，而不是直接照搬数据库表或界面控件。结合现有代码，Customer 表示客户身份信息，BankCard 表示客户持有的银行卡，Account 表示账户余额与账户类型，Session 表示登录后由 TokenManager 维护的会话，Transaction 表示取款、存款、转账等交易记录，Receipt 表示后续可打印或查询的凭条，ATMDevice 表示读卡、吐钞、打印等设备能力。"
    ))
    parts.append(diagram_block(rel_ids["domain-model.png"], "domain-model.png", DIAGRAMS[0][1], 201))
    parts.append(paragraph(
        "从模型关系看，客户可以持有一张或多张银行卡并关联账户；银行卡登录成功后产生会话；账户是交易发生的核心主体；交易可进一步关联凭条和设备动作。当前 dev 数据库已经包含 customer、account、bank_card 表，并补充了 transaction_record 表结构，为后续第二次迭代实现真实交易流水提供了落点。"
    ))

    parts.append(heading("2.3 系统顺序图", 2))
    parts.append(paragraph(
        "系统顺序图从系统边界角度描述参与者与系统之间的事件顺序。本项目采用前后端分离实现，因此图中将客户、前端视图、前端 API 封装、后端 Controller 和 Service 作为关键交互节点。登录认证和余额查询是当前可运行能力，取款与转账是当前已挂载但返回 501 的交易骨架能力。"
    ))
    for index in range(1, 5):
        file_name, caption = DIAGRAMS[index]
        parts.append(diagram_block(rel_ids[file_name], file_name, caption, 201 + index))
    parts.append(paragraph(
        "登录与余额查询顺序图反映了当前系统的可运行闭环：用户通过 LoginView 或 BalanceView 发起操作，经 atm.js 与 http.js 发送请求，由后端 Controller 调用 Service 和 Mapper 完成业务处理。取款与转账顺序图则特意保留 TransactionServiceImpl 返回 501 的事实，说明交易模块在第一次迭代中的交付重点是接口草案、实体/DTO 和服务骨架，而非真实账务处理。"
    ))

    parts.append(heading("2.4 操作契约", 2))
    parts.append(paragraph(
        "操作契约描述系统操作执行前后的状态变化、输入输出和异常约束。为了与实际代码一致，本节既给出业务意图，也标注当前迭代实现状态，避免把后续迭代能力误写成本次已完成内容。"
    ))
    parts.append(table([
        ["系统操作", "login(cardNo, password)"],
        ["关联用例", "登录认证"],
        ["前置条件", "客户位于登录界面；银行卡号和密码已输入；后端认证服务可用。"],
        ["后置条件", "密码正确时生成 sessionId，前端保存会话并进入主菜单；密码错误时返回 401，不建立有效会话。"],
        ["当前代码依据", "LoginView -> atm.js -> AuthController.login -> AuthServiceImpl.login -> BankCardMapper/TokenManager。"],
    ]))
    parts.append(table([
        ["系统操作", "getBalance(sessionId)"],
        ["关联用例", "查询余额"],
        ["前置条件", "客户已登录；sessionId 有效。"],
        ["后置条件", "系统根据 sessionId 解析 cardNo，查询账户余额并返回 BalanceResponse；会话无效时返回 401。"],
        ["当前代码依据", "BalanceView -> AccountController.getBalance -> TokenManager -> AccountServiceImpl.getBalance。"],
    ]))
    parts.append(table([
        ["系统操作", "withdraw(sessionId, amount, printReceipt)"],
        ["关联用例", "取款"],
        ["业务意图", "校验会话、金额、余额和设备现金后完成扣款、吐钞和交易流水记录。"],
        ["第一次迭代状态", "接口和 DTO 已接入，TransactionServiceImpl 当前返回 ApiException(501)，不修改账户余额。"],
        ["当前代码依据", "TransactionController.withdraw -> TransactionServiceImpl.withdraw；返回 Result(code=501)。"],
    ]))
    parts.append(table([
        ["系统操作", "transfer(sessionId, targetAccountNo, amount, printReceipt)"],
        ["关联用例", "转账"],
        ["业务意图", "校验会话、目标账户和余额后，在同一事务中完成扣款、入账和流水记录。"],
        ["第一次迭代状态", "接口和 DTO 已接入，TransactionServiceImpl 当前返回 ApiException(501)，不执行真实转账。"],
        ["当前代码依据", "TransactionController.transfer -> TransactionServiceImpl.transfer；返回 Result(code=501)。"],
    ]))

    parts.append(heading("2.5 UML 包图", 2))
    parts.append(paragraph(
        "第一次迭代的包结构体现了前后端分离和小组分工边界。frontend 负责界面、路由、状态管理和 HTTP 调用；atm-server-auth 是主 Spring Boot 后端，负责认证、账户和公共响应结构；transaction 作为独立目录保存交易处理模块骨架，并通过 Maven 编译配置接入主后端。"
    ))
    parts.append(diagram_block(rel_ids["package-diagram.png"], "package-diagram.png", DIAGRAMS[5][1], 206))

    parts.append(heading("2.6 UML 交互图", 2))
    parts.append(paragraph(
        "交互图进一步从对象协作角度说明当前代码如何完成业务请求。登录与余额查询协作已经形成 Controller-Service-Mapper-Entity 的闭环；交易模块协作图则说明当前已经完成 Controller、Service、DTO、统一异常处理之间的连接，为第二次迭代补充真实业务规则、事务控制和数据库写入留出稳定接口。"
    ))
    parts.append(diagram_block(rel_ids["interaction-login-balance.png"], "interaction-login-balance.png", DIAGRAMS[6][1], 207))
    parts.append(diagram_block(rel_ids["interaction-transaction-skeleton.png"], "interaction-transaction-skeleton.png", DIAGRAMS[7][1], 208))

    parts.append(heading("2.7 UML 类图", 2))
    parts.append(paragraph(
        "类图选取当前后端最核心的类和接口，重点展示认证、账户与交易模块之间的职责边界。Controller 负责 HTTP 请求入口，ServiceImpl 承担业务规则或骨架占位，Mapper 负责数据访问，TokenManager 维护内存会话，Result 和 DTO 负责接口数据结构。"
    ))
    parts.append(diagram_block(rel_ids["class-diagram.png"], "class-diagram.png", DIAGRAMS[8][1], 209))
    parts.append(paragraph(
        "从类图可以看出，认证与账户模块已经具备可执行服务实现，而交易模块当前保持接口和 DTO 先行的骨架形态。这一状态与第一次迭代目标一致：先稳定系统边界和对象协作，再在第二次迭代中补充取款、存款、转账的完整业务规则、事务控制和交易流水持久化。"
    ))

    parts.append(heading("2.8 本次迭代实现与验收说明", 2))
    parts.append(paragraph(
        "本次迭代的可运行部分包括：启动 dev 后端时使用 H2 内存数据库初始化演示客户、账户和银行卡；前端通过 Vite 运行并访问登录、菜单、余额查询和占位功能页；后端认证接口、账户信息接口、余额接口和交易骨架接口均能被 Spring MVC 挂载。交易模块返回 501 并非运行错误，而是第一次迭代骨架交付的边界说明。"
    ))
    parts.append(paragraph(
        "当前自动化验证命令为：JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 ./mvnw test。测试结果为 Tests run: 6, Failures: 0, Errors: 0, Skipped: 0，说明认证、账户和交易骨架 Controller 测试均通过，交易模块也已经随主后端完成编译和请求映射。"
    ))

    parts.append(heading("2.9 本章小结", 2))
    parts.append(paragraph(
        "本章按照总体迭代计划完成了细化迭代 1 所需的主要 UML 制品：领域模型、系统顺序图、操作契约、包图、交互图和类图。模型内容均基于当前项目实际代码和目录结构展开，并明确标注了交易模块在第一次迭代中的骨架状态。下一章将在本章模型基础上继续推进细化迭代 2，重点实现取款、存款、转账、交易详情等真实业务能力，并补充事务控制和异常处理。"
    ))
    return "".join(parts)


def update_relationships(rels_xml: str, image_targets: dict[str, str]) -> tuple[str, dict[str, str]]:
    existing_ids = [int(match) for match in re.findall(r'Id="rId(\d+)"', rels_xml)]
    next_id = max(existing_ids, default=0) + 1
    rel_ids = {}
    additions = []
    for file_name in image_targets:
        rel_id = f"rId{next_id}"
        next_id += 1
        rel_ids[file_name] = rel_id
        additions.append(
            f'<Relationship Id="{rel_id}" '
            'Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" '
            f'Target="{image_targets[file_name]}"/>'
        )
    rels_xml = rels_xml.replace("</Relationships>", "".join(additions) + "</Relationships>")
    return rels_xml, rel_ids


def main() -> None:
    OUTPUT_DOCX.parent.mkdir(parents=True, exist_ok=True)
    image_targets = {
        file_name: f"media/iteration1_{Path(file_name).stem}.png" for file_name, _ in DIAGRAMS
    }
    with ZipFile(SOURCE_DOCX, "r") as zin:
        document_xml = zin.read("word/document.xml").decode("utf-8")
        rels_xml = zin.read("word/_rels/document.xml.rels").decode("utf-8")
        rels_xml, rel_ids = update_relationships(rels_xml, image_targets)
        chapter = chapter_xml(rel_ids)
        insert_at = document_xml.rfind("<w:sectPr")
        if insert_at == -1:
            insert_at = document_xml.rfind("</w:body>")
        document_xml = document_xml[:insert_at] + chapter + document_xml[insert_at:]

        temp_docx = OUTPUT_DOCX.with_suffix(".tmp.docx")
        with ZipFile(temp_docx, "w", ZIP_DEFLATED) as zout:
            for item in zin.infolist():
                if item.filename == "word/document.xml":
                    zout.writestr(item, document_xml.encode("utf-8"))
                elif item.filename == "word/_rels/document.xml.rels":
                    zout.writestr(item, rels_xml.encode("utf-8"))
                else:
                    zout.writestr(item, zin.read(item.filename))
            for file_name, target in image_targets.items():
                zout.write(GENERATED_DIR / file_name, f"word/{target}")
        shutil.move(temp_docx, OUTPUT_DOCX)
    print(OUTPUT_DOCX)


if __name__ == "__main__":
    main()
