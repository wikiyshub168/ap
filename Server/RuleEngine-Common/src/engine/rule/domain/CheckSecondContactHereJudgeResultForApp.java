package engine.rule.domain;

import com.huateng.toprules.core.annotation.ModelDomainInstance;


@ModelDomainInstance(label = "第二联系人电话审核V3_联系人是否在分期现场", type = "number", adapter = "engine.rule.domain.adapter.EnumDomainAdapter", params = "X_CheckSecondContactHereJudgeResult,Catfish.Platform.ManualJobV3.Handlers")
public class CheckSecondContactHereJudgeResultForApp{
}

