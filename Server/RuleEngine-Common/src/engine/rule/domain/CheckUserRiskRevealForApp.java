package engine.rule.domain;

import com.huateng.toprules.core.annotation.ModelDomainInstance;


@ModelDomainInstance(label = "客户电话审核V3_本人风险提示", type = "number", adapter = "engine.rule.domain.adapter.EnumDomainAdapter", params = "X_CheckUserRiskReveal,Catfish.Platform.ManualJobV3.Handlers")
public class CheckUserRiskRevealForApp{
}

