package engine.rule.domain;

import com.huateng.toprules.core.annotation.ModelDomainInstance;


@ModelDomainInstance(label = "第一联系人电话审核V3_联系人提示申请人是否有风险", type = "number", adapter = "engine.rule.domain.adapter.EnumDomainAdapter", params = "X_CheckFirstContactRiskPromptResult,Catfish.Platform.ManualJobV3.Handlers")
public class CheckFirstContactRiskPromptResultForApp{
}

