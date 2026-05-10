from __future__ import annotations

import html
import re
import shutil
import struct
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile


ROOT = Path(__file__).resolve().parents[2]
SOURCE_DOCX = Path(r"D:\代码\细化迭代2第9组 马启凡 叶炳良 周子栋 庄子杰.docx")
OUTPUT_DOCX = ROOT / "tmp" / "iteration2_report_updated.docx"
GENERATED_DIR = ROOT / "docs" / "iteration2-gof" / "generated"


DIAGRAMS = [
    ("pattern-overview.png", "图 3-1 细化迭代 2 GoF 模式应用总览"),
    ("transaction-class-diagram.png", "图 3-2 第二次迭代交易模块设计类图"),
    ("transaction-template-flow.png", "图 3-3 交易处理模板流程与异常分支"),
    ("transfer-sequence.png", "图 3-4 转账业务顺序图：事务代理与双账户一致性"),
    ("adapter-contract-flow.png", "图 3-5 公开契约统一与 Adapter 兼容流程"),
    ("future-extension-map.png", "图 3-6 基于细化迭代 2 的第三次迭代拓展点"),
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
    size = 32 if level == 1 else 28 if level == 2 else 24
    align = "center" if level == 1 else None
    return paragraph(text, bold=True, size=size, align=align)


def page_break() -> str:
    return '<w:p><w:r><w:br w:type="page"/></w:r></w:p>'


def table(rows: list[list[str]]) -> str:
    max_cols = max(len(row) for row in rows)
    cell_width = 9000 // max_cols
    row_xml = []
    for row_index, row in enumerate(rows):
        cells = []
        for cell in row:
            fill = '<w:shd w:fill="D9EAF7"/>' if row_index == 0 else ""
            cells.append(
                "<w:tc>"
                f'<w:tcPr><w:tcW w:w="{cell_width}" w:type="dxa"/>{fill}</w:tcPr>'
                f"{paragraph(cell, bold=row_index == 0, size=19)}"
                "</w:tc>"
            )
        row_xml.append("<w:tr>" + "".join(cells) + "</w:tr>")
    borders = (
        '<w:tblPr><w:tblW w:w="0" w:type="auto"/>'
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


def callout(text: str) -> str:
    return table([[text]])


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
        <wp:docPr id="{doc_pr_id}" name="iteration2-gof-{doc_pr_id}"/>
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
    return image_paragraph(rel_id, GENERATED_DIR / file_name, doc_pr_id) + paragraph(caption, size=20, align="center")


def chapter_xml(rel_ids: dict[str, str]) -> str:
    parts: list[str] = [page_break()]
    parts.append(heading("第三章 细化迭代2", 1))
    parts.append(paragraph(
        "根据总体迭代计划，第三章对应“细化迭代 2”，提交内容被明确标注为 Applying GoF Design Patterns，提交时间为第 10 周周五晚。"
        "因此，本章不再重复第二章已经完成的领域模型、系统顺序图和包图等基础制品，而是在第一次迭代形成的代码基线之上，重点说明核心交易能力如何通过可维护的对象协作和设计模式思想落地。"
        "结合第二次迭代记录，本轮实现范围集中在取款、存款、转账、修改密码、账户完整信息、交易前校验和交易详情查询，并保持前端调用、后端 DTO、OpenAPI 和接口测试示例使用同一套公开契约。"
    ))
    parts.append(callout(
        "本章写法延续前两章“先说明阶段目标，再说明模型与代码依据，最后说明边界、验收和后续演进”的风格。不同之处在于，第三章的中心不是枚举 UML 图，而是围绕 GoF 设计模式应用解释第二次迭代为什么这样组织交易模块、如何保证账务一致性、如何兼容旧接口并支撑后续凭条、流水和设备状态扩展。"
    ))

    parts.append(heading("3.1 本章定位与迭代依据", 2))
    parts.append(paragraph(
        "总体迭代计划中，第二章“细化迭代 1”要求提交 Domain Models、System Sequence Diagrams、Operation Contracts、UML Package Diagrams、UML Interaction Diagrams 和 UML Class Diagrams；而第三章“细化迭代 2”只列出 Applying GoF Design Patterns。"
        "这说明本阶段的核心任务不是重新绘制所有上一轮模型，而是在已有模型基础上进入设计决策层：识别哪些对象应该承担稳定职责，哪些变化点需要被封装，哪些跨模块调用需要通过统一接口协调，哪些兼容问题应由适配层处理。"
    ))
    parts.append(paragraph(
        "从项目实现角度看，第二次迭代已经把第一次迭代中的交易接口骨架推进为真实业务闭环。前端新增取款、存款、转账和修改密码页面；后端补充完整账户信息、交易前校验、修改密码、取款、存款、转账和交易详情接口；交易服务通过事务控制完成账户余额更新与 transaction_record 交易流水写入；公开字段统一为 sessionId、transactionId 和 targetAccountNo。"
        "这些内容共同构成本章分析 GoF 模式应用的事实基础。"
    ))
    parts.append(table([
        ["总体计划要求", "第三章响应方式", "项目实际依据"],
        ["Applying GoF Design Patterns", "分析 Facade、Proxy、Adapter、Template Method 思想、Factory/Creator 思想及容器单例管理", "TransactionServiceImpl、SessionValidator、Spring @Transactional、MyBatis Mapper、Result、TransactionUtils"],
        ["承接细化迭代 1", "沿用第二章领域对象、系统操作、包结构和类图基线，不改变既有模块边界", "frontend、atm-server-auth、transaction 三目录继续保持分工结构"],
        ["服务第二次迭代实现", "把取款、存款、转账、修改密码和交易详情写成可验证业务闭环", "第二次迭代记录、api-test.http、openapi-atm.yaml、交易服务集成测试"],
        ["为细化迭代 3 留出扩展", "明确流水列表、凭条、设备状态、状态图和最终文档仍属后续阶段", "三次迭代任务安排和第二次迭代记录中的未纳入范围"],
    ]))

    parts.append(heading("3.2 第二次迭代功能闭环", 2))
    parts.append(paragraph(
        "第二次迭代的直接目标是补全 ATM 系统的核心交易能力，使系统从“登录认证 - 主菜单 - 查询余额”的最小闭环，升级为“登录认证 - 账户校验 - 交易执行 - 余额更新 - 流水查询”的主要业务闭环。"
        "其中，取款、存款和转账会改变账户状态，因此比第一轮查询类功能更强调事务一致性、异常恢复、业务规则集中管理和接口契约统一。"
    ))
    parts.append(table([
        ["模块", "第二次迭代完成内容", "设计关注点"],
        ["前端显示模块", "新增 WithdrawView、DepositView、TransferView、ChangePasswordView；主菜单入口改为真实业务页面；atm.js 统一封装交易和账户接口", "边界对象只负责输入、提示、会话状态和接口调用，不承担账务规则"],
        ["认证与账户模块", "新增修改密码、完整账户信息、交易前校验；SessionValidator 统一校验 sessionId；账户接口主路径统一为 /api/atm/accounts/*", "把身份解析和账户上下文获取作为交易前置能力，为交易服务提供稳定输入"],
        ["交易处理模块", "实现 withdraw、deposit、transfer、getTransactionById；写入 transaction_record；按单次/单日限额、余额、自转账、目标账户存在性进行校验", "通过服务门面和事务代理隐藏复杂规则，保证接口简单且账务状态一致"],
        ["文档与测试材料", "OpenAPI、README、FUNCTIONS、api-test.http、第二次迭代记录同步更新；集成测试覆盖余额更新、异常拒绝和交易详情", "让模型、接口、代码和验收材料保持同一事实口径"],
    ]))
    parts.append(paragraph(
        "本轮特别需要说明的是，第二次迭代并未把交易流水列表、凭条展示、打印机状态、ATM 设备吐钞能力和完整设备异常处理提前写成已实现功能。"
        "这些内容虽然在第一章用例中已经出现，但按照三次迭代任务安排，它们应保留到细化迭代 3 或最终集成阶段。这样的边界划分既避免文档过度承诺，也符合 UP 迭代式逐步细化的思想。"
    ))

    parts.append(heading("3.3 GoF 模式应用总览", 2))
    parts.append(paragraph(
        "GoF 设计模式的价值不在于为每个类强行贴标签，而在于面对变化点时提供稳定的职责分配方式。"
        "在 ATM 第二次迭代中，主要变化点集中在交易类型差异、会话字段统一、数据库访问代理、事务边界、异常响应格式和后续凭条/设备扩展等方面。"
        "图 3-1 从前端、控制层、服务层、事务代理、数据访问层和后续扩展点几个角度，概括了本轮可以识别和应用的模式。"
    ))
    parts.append(diagram_block(rel_ids["pattern-overview.png"], "pattern-overview.png", DIAGRAMS[0][1], 301))
    parts.append(table([
        ["模式或模式思想", "本项目中的落点", "解决的问题"],
        ["Facade（外观模式）", "TransactionController 和 TransactionService 对外暴露 withdraw、deposit、transfer、getTransactionById 等简单接口", "隐藏会话校验、金额规则、Mapper 调用、流水写入和统一响应细节，降低前端与业务实现耦合"],
        ["Proxy（代理模式）", "Spring @Transactional 通过 AOP 代理形成事务边界；MyBatis Mapper 接口由框架生成运行时代理", "把事务提交/回滚、SQL 执行和结果映射从业务代码中剥离出来"],
        ["Adapter（适配器模式）", "SessionValidator 将新 sessionId 和旧 token 兼容口径适配为业务服务需要的 cardNo", "在不扩大业务服务复杂度的前提下完成字段统一和旧调用兼容"],
        ["Template Method 思想", "取款、存款、转账共享“请求校验 - 会话解析 - 账户上下文 - 规则校验 - 流水 - 余额更新 - 响应”骨架", "让三类交易既保持公共处理流程，又允许差异化金额规则和目标账户规则"],
        ["Factory/Creator 思想", "createTransaction 和 TransactionUtils.generateTransactionId 统一构造交易流水和交易编号", "避免交易对象初始化逻辑散落在多个分支中，便于后续扩展凭条或流水列表"],
        ["容器管理单例", "Controller、Service、Mapper、Validator 均由 Spring 管理，默认以单例 Bean 复用", "避免手写 Singleton 带来的测试困难，同时保证核心服务对象生命周期稳定"],
    ]))
    parts.append(paragraph(
        "其中，Proxy、Adapter 和 Facade 在当前代码中已有比较直接的落点；Template Method 和 Factory/Creator 更准确地说是“模式思想”在服务实现中的体现，当前通过私有公共方法和统一构造入口完成。"
        "后续如果交易种类继续增加，可进一步把这些思想重构为更严格的抽象模板、策略接口或命令对象。"
    ))

    parts.append(heading("3.4 Facade 与 Template Method 思想：交易服务设计", 2))
    parts.append(paragraph(
        "第二次迭代后，交易模块不再只是 Controller 和 DTO 的占位接口，而是承担了真实业务规则。"
        "为了不让前端页面或 Controller 直接感知银行卡、账户、交易流水和异常处理细节，本轮将 TransactionService 作为交易业务的服务门面：前端只需要提交 sessionId、amount、printReceipt 和必要的 targetAccountNo；Controller 只负责接收请求并包装 Result；TransactionServiceImpl 则在内部完成会话校验、账户上下文加载、业务规则判断、交易对象创建、余额更新、流水写入和响应组装。"
    ))
    parts.append(diagram_block(rel_ids["transaction-class-diagram.png"], "transaction-class-diagram.png", DIAGRAMS[1][1], 302))
    parts.append(paragraph(
        "图 3-2 展示了第二次迭代交易模块的主要设计类。TransactionController 依赖 TransactionService 接口，而不是直接依赖具体实现；TransactionServiceImpl 作为业务实现类，组合 SessionValidator、AccountMapper、BankCardMapper 和 TransactionMapper。"
        "这一结构具有明显的 Facade 效果：对外接口保持简洁稳定，对内则可以容纳金额规则、日累计限额、余额变更、交易状态和异常分支等复杂逻辑。"
    ))
    parts.append(paragraph(
        "在实现层面，取款、存款和转账具有相同的交易骨架：先校验请求体和金额，再通过 sessionId 获取当前银行卡和账户，再执行交易类型相关规则，随后创建 PENDING 状态交易流水，更新账户余额，最后将交易标记为 SUCCESS 并返回 transactionId 与最新余额。"
        "差异点则集中在取款金额必须为 100 的整数倍、取款单次和单日限额、存款单次上限、转账目标账户存在性、自转账禁止、转账单次和单日限额等规则上。"
    ))
    parts.append(diagram_block(rel_ids["transaction-template-flow.png"], "transaction-template-flow.png", DIAGRAMS[2][1], 303))
    parts.append(paragraph(
        "图 3-3 将这一公共骨架抽象为模板流程。当前代码并未引入抽象基类来强制实现 Template Method，但已经通过 requireRequest、requirePositiveAmount、loadAccountContext、createTransaction、markSuccess 等私有公共方法把重复步骤集中起来。"
        "这种写法适合当前课程项目规模：既能体现模板方法思想，又不会为了模式而过度拆分。若第三次迭代增加手续费、凭条策略或更多交易类型，可以在此基础上进一步抽取 TransactionOperation 或 TransactionRule 接口。"
    ))

    parts.append(heading("3.5 Proxy 模式：事务一致性与 Mapper 代理", 2))
    parts.append(paragraph(
        "转账是本轮最能体现架构风险的场景，因为它同时修改源账户和目标账户，还要记录至少一条对外可查询的交易流水。"
        "如果没有明确的事务边界，系统可能出现“源账户已扣款、目标账户未入账”或“余额已变更、流水未记录”等不一致状态。"
        "当前实现通过 Spring 的 @Transactional 在服务方法外层形成事务代理，业务代码只表达交易步骤，事务提交和异常回滚由框架代理统一处理。"
    ))
    parts.append(diagram_block(rel_ids["transfer-sequence.png"], "transfer-sequence.png", DIAGRAMS[3][1], 304))
    parts.append(paragraph(
        "图 3-4 中的 Spring Transaction Proxy 是 Proxy 模式在本项目中的关键应用。Controller 调用的是被容器代理后的 TransactionService 对象；代理在进入 transfer 方法前开启事务，在方法正常返回后提交事务，在 ApiException 或运行时异常抛出时回滚事务。"
        "同时，AccountMapper 和 TransactionMapper 也是由 MyBatis 基于接口生成的运行时代理，它们把 SQL 执行、参数绑定和结果映射隐藏在接口调用之后，使 TransactionServiceImpl 能够以面向对象的方式表达余额扣减、余额增加、流水插入和状态更新。"
    ))
    parts.append(table([
        ["一致性场景", "实现处理", "验收依据"],
        ["成功取款", "先插入交易流水，再扣减账户余额，最后标记交易成功并返回 remainingBalance", "集成测试验证取款后余额为 4900.00，交易详情可按 transactionId 查询"],
        ["成功存款", "插入交易流水后增加账户余额，返回 updatedBalance", "集成测试验证存款后余额更新为 5100.00，交易类型和状态正确"],
        ["余额不足", "在余额校验阶段抛出 ApiException，不继续扣款或写成功流水", "集成测试验证余额不足时账户余额保持不被错误扣减"],
        ["目标账户不存在", "在查询目标账户阶段返回 404，不执行源账户扣款", "集成测试验证目标账户不存在时源账户余额保持 5000.00"],
        ["成功转账", "同一事务中完成源账户扣款、目标账户入账、转出和转入流水写入", "集成测试验证源账户减少、目标账户增加，交易详情记录 targetAccountNo"],
    ]))

    parts.append(heading("3.6 Adapter 模式：公开契约统一与兼容", 2))
    parts.append(paragraph(
        "第二次迭代除了实现交易功能，还完成了一次重要的公开契约统一：会话字段以 sessionId 为主，账户路径以 /api/atm/accounts/* 为主，交易流水号以 transactionId 为对外标识，转账目标账户字段统一为 targetAccountNo。"
        "这类统一如果直接扩散到所有 Controller 和 Service，很容易让业务层同时处理新老字段，从而造成后续文档、前端和测试示例持续漂移。"
    ))
    parts.append(diagram_block(rel_ids["adapter-contract-flow.png"], "adapter-contract-flow.png", DIAGRAMS[4][1], 305))
    parts.append(paragraph(
        "图 3-5 展示了当前的适配思路。对外文档和前端调用统一使用 sessionId，但后端仍在少数兼容入口保留 token 作为旧别名。"
        "SessionValidator 负责将 sessionId 或 token 解析为统一的 resolvedSessionId，再调用 TokenManager 获取 cardNo。"
        "这样一来，AccountService、AuthService 和 TransactionService 都只面对统一后的业务身份，不必知道请求方使用的是新字段还是旧字段。"
    ))
    parts.append(table([
        ["契约项", "第二次迭代主口径", "兼容与边界说明"],
        ["会话字段", "sessionId", "token 仅作为旧请求兼容别名，不再作为新文档主字段"],
        ["账户路径", "/api/atm/accounts/*", "/api/atm/account/* 仅作为后端兼容路径保留"],
        ["交易流水号", "transactionId", "对外返回和交易详情查询均使用该字段"],
        ["转账目标账户", "targetAccountNo", "targetBank 只保留在实体和数据库层用于后续跨行扩展"],
        ["统一响应结构", "code、message、data、timestamp", "前端 mock、OpenAPI、HTTP 示例与后端 Result 保持一致"],
    ]))

    parts.append(heading("3.7 操作契约与关键业务规则", 2))
    parts.append(paragraph(
        "在第一章和第二章中，操作契约已经用于说明系统操作的前置条件、后置条件和异常分支。"
        "第二次迭代后，交易类操作从“骨架接口返回 501”升级为真实状态变更操作，因此需要重新明确关键操作契约。"
        "本节不再重复所有字段定义，而是聚焦会导致账户状态变化或会影响安全边界的操作。"
    ))
    parts.append(table([
        ["系统操作", "前置条件", "后置条件", "异常/替代结果"],
        ["withdraw(sessionId, amount, printReceipt)", "sessionId 有效；金额大于 0；账户存在且余额足够", "生成 transactionId；扣减账户余额；交易状态为 SUCCESS；返回 remainingBalance", "金额非 100 整数倍、超单次或单日限额、余额不足时返回 400，不产生错误扣款"],
        ["deposit(sessionId, amount, printReceipt)", "sessionId 有效；金额大于 0；账户存在", "增加账户余额；写入交易流水；返回 updatedBalance", "超单次存款限额或账户不存在时拒绝交易"],
        ["transfer(sessionId, targetAccountNo, amount, printReceipt)", "sessionId 有效；目标账户存在；不能自转账；余额足够", "源账户扣款、目标账户入账；写入转出和转入流水；返回 remainingBalance", "目标账户不存在返回 404；余额不足、超限额或自转账返回 400；事务失败时整体回滚"],
        ["changePassword(sessionId, oldPassword, newPassword)", "sessionId 有效；旧密码正确；新密码符合规则", "更新银行卡密码；当前 sessionId 失效，需要重新登录", "旧密码错误、会话无效或密码格式不合规时返回失败"],
        ["getTransactionById(transactionId)", "transactionId 非空", "返回交易类型、金额、交易状态、交易前后余额、目标账户等详情", "交易编号不存在返回 404"],
    ]))
    parts.append(paragraph(
        "这些契约与第二次迭代记录中的公开字段保持一致：取款、存款请求由 sessionId、amount、printReceipt 组成；转账请求额外包含 targetAccountNo；交易详情以 path 参数 transactionId 查询。"
        "业务规则也与交易模块设计文档保持一致，只是当前实现进一步补充了事务注解、每日累计金额查询和目标账户校验的实际代码路径。"
    ))

    parts.append(heading("3.8 前后端联调与验证", 2))
    parts.append(paragraph(
        "第二次迭代的前端不再只提供占位入口，而是形成了可演示的核心交易页面。"
        "WithdrawView、DepositView、TransferView 和 ChangePasswordView 通过统一 API 封装调用后端接口，并在成功后更新页面状态和 Pinia 中的余额。"
        "mock 模式仍用于无后端环境下演示，但真实后端已经支持取款、存款、转账、修改密码和交易详情，因而本轮可同时覆盖前端演示和真实联调两个场景。"
    ))
    parts.append(table([
        ["验证项", "验证方式", "预期结果"],
        ["后端自动化测试", "在 atm-server-auth 目录执行 .\\mvnw.cmd test", "Tests run: 13, Failures: 0, Errors: 0, Skipped: 0，BUILD SUCCESS"],
        ["手工接口链路", "按 api-test.http 先登录获取 sessionId，再依次调用账户、取款、存款、转账、交易详情接口", "每个成功交易返回 transactionId，交易详情可查询，异常示例返回明确错误信息"],
        ["前端真实联调", "使用 npm run dev 或 npm run dev:real 连接 http://localhost:8080", "主菜单进入取款、存款、转账、修改密码页面后可调用真实后端"],
        ["前端 mock 演示", "使用 npm run dev:mock 打开 mock 页面", "无后端时仍可演示第二次迭代业务流程和错误提示"],
        ["契约一致性检查", "比对 openapi-atm.yaml、README、FUNCTIONS、api-test.http 和前端 atm.js", "字段统一为 sessionId、transactionId、targetAccountNo，账户路径统一为 /api/atm/accounts/*"],
    ]))
    parts.append(paragraph(
        "从验收角度看，本章关注的重点不是单纯“接口能访问”，而是“模型、代码、接口文档和验证材料是否说明同一件事”。"
        "例如，第一次迭代中交易接口返回 501 是符合骨架边界的；第二次迭代后，交易接口必须真正完成金额规则校验、账户余额变更和交易详情查询。"
        "这种分阶段验收方式能让报告内容与项目实际状态保持一致。"
    ))

    parts.append(heading("3.9 亮点、风险与拓展点", 2))
    parts.append(paragraph(
        "第二次迭代的亮点不只在于补齐了取款、存款和转账页面，更在于把交易功能放入相对稳定的设计结构中：前端只关心页面输入和结果展示；Controller 提供稳定接口；TransactionService 隐藏复杂交易流程；SessionValidator 统一身份契约；事务代理和 Mapper 代理承担基础设施职责。"
        "这种结构使后续迭代可以在不频繁改变公开接口的前提下继续增加凭条、流水列表、设备状态和异常恢复能力。"
    ))
    parts.append(diagram_block(rel_ids["future-extension-map.png"], "future-extension-map.png", DIAGRAMS[5][1], 306))
    parts.append(page_break())
    parts.append(table([
        ["类别", "内容", "说明"],
        ["亮点 1：交易一致性", "转账在同一事务中完成源账户扣款、目标账户入账和交易流水写入", "直接回应 ATM 系统中账务一致性风险，也是本轮最核心的后端设计价值"],
        ["亮点 2：契约统一", "sessionId、transactionId、targetAccountNo 和 /api/atm/accounts/* 形成统一公开口径", "减少前端、后端 DTO、OpenAPI 和测试示例之间的字段漂移"],
        ["亮点 3：模式意识", "以 Facade、Proxy、Adapter 和 Template Method 思想组织交易实现", "不是为了堆砌模式，而是用模式解释边界封装、事务代理、兼容适配和公共流程复用"],
        ["风险 1：规则分支继续增长", "取款、存款、转账规则目前仍集中在 TransactionServiceImpl 中", "后续如增加手续费、跨行转账、设备现金校验，建议抽取策略接口"],
        ["风险 2：设备和凭条尚未实现", "printReceipt 当前是请求字段和后续衔接点，未形成真实打印或凭条查询闭环", "应在第三次迭代补充凭条页面、凭条接口和打印失败处理"],
        ["拓展点：第三次迭代模式", "Strategy、Command、State、Observer 可分别服务于规则策略、ATM 操作封装、会话状态和交易事件扩展", "与总体计划中第四章状态机建模和最终文档汇总相衔接"],
    ]))

    parts.append(heading("3.10 本章小结", 2))
    parts.append(paragraph(
        "本章依据总体迭代计划中“细化迭代 2 - Applying GoF Design Patterns”的要求，在第二次迭代实现结果基础上说明了 ATM 系统核心交易能力的设计模式应用。"
        "从功能层面看，系统已经完成取款、存款、转账、修改密码、完整账户信息、交易前校验和交易详情查询，核心业务闭环已经从第一次迭代的骨架状态推进到可验证状态。"
        "从设计层面看，TransactionService 作为服务门面封装了复杂交易流程，Spring 和 MyBatis 代理隐藏了事务与数据访问细节，SessionValidator 作为适配层完成新老会话字段统一，交易服务内部则以模板方法思想复用公共流程并保留交易类型差异。"
    ))
    parts.append(paragraph(
        "同时，本章也明确了第二次迭代的边界：交易流水列表、凭条展示、真实打印、ATM 设备状态和更完整的状态机建模仍属于后续细化迭代 3 的主要任务。"
        "因此，下一章可以在本章形成的交易闭环和模式应用基础上继续展开 UML State Machine Diagrams and Modeling，并对前述模型进行 refine，最终形成完整的课程实验文档。"
    ))
    return "".join(parts)


def update_relationships(rels_xml: str, image_targets: dict[str, str]) -> tuple[str, dict[str, str]]:
    rels_xml = re.sub(
        r'<Relationship Id="rId\d+" Type="http://schemas\.openxmlformats\.org/officeDocument/2006/relationships/image" Target="media/iteration2_[^"]+"/>',
        "",
        rels_xml,
    )
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


def strip_existing_iteration2_chapter(document_xml: str) -> str:
    marker = '<w:t xml:space="preserve">第三章 细化迭代2</w:t>'
    marker_at = document_xml.find(marker)
    if marker_at == -1:
        return document_xml
    chapter_start = document_xml.rfind('<w:p><w:r><w:br w:type="page"/></w:r></w:p>', 0, marker_at)
    if chapter_start == -1:
        chapter_start = document_xml.rfind("<w:p", 0, marker_at)
    sect_at = document_xml.rfind("<w:sectPr")
    if sect_at == -1:
        sect_at = document_xml.rfind("</w:body>")
    if chapter_start == -1 or sect_at == -1 or chapter_start >= sect_at:
        return document_xml
    return document_xml[:chapter_start] + document_xml[sect_at:]


def main() -> None:
    OUTPUT_DOCX.parent.mkdir(parents=True, exist_ok=True)
    image_targets = {
        file_name: f"media/iteration2_{Path(file_name).stem}.png" for file_name, _ in DIAGRAMS
    }
    with ZipFile(SOURCE_DOCX, "r") as zin:
        document_xml = zin.read("word/document.xml").decode("utf-8")
        document_xml = strip_existing_iteration2_chapter(document_xml)
        rels_xml = zin.read("word/_rels/document.xml.rels").decode("utf-8")
        rels_xml, rel_ids = update_relationships(rels_xml, image_targets)
        chapter = chapter_xml(rel_ids)
        insert_at = document_xml.rfind("<w:sectPr")
        if insert_at == -1:
            insert_at = document_xml.rfind("</w:body>")
        document_xml = document_xml[:insert_at] + chapter + document_xml[insert_at:]

        temp_docx = OUTPUT_DOCX.with_suffix(".tmp.docx")
        image_package_paths = {f"word/{target}" for target in image_targets.values()}
        with ZipFile(temp_docx, "w", ZIP_DEFLATED) as zout:
            for item in zin.infolist():
                if item.filename in image_package_paths:
                    continue
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
