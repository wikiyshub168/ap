package engine.rule.domain;

import com.huateng.toprules.core.annotation.ModelDomainInstance;


@ModelDomainInstance(label = "第二联系人电话审核V3_联系人声音与常识推断", type = "number", adapter = "engine.rule.domain.adapter.EnumDomainAdapter", params = "X_CheckSecondContactSoundJudgeResult,Catfish.Platform.ManualJobV3.Handlers")
public class CheckSecondContactSoundJudgeResultForApp{
}

