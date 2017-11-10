package engine.rule.domain;

import com.huateng.toprules.core.annotation.ModelDomainInstance;


@ModelDomainInstance(label = "第三联系人电话审核V3_申请人关系审核", type = "number", adapter = "engine.rule.domain.adapter.EnumDomainAdapter", params = "X_CheckThirdContactIdentificationResult,Catfish.Platform.ManualJobV3.Handlers")
public class CheckThirdContactIdentificationResultForApp{
}

