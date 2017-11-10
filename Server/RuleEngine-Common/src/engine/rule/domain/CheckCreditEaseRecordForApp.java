package engine.rule.domain;

import com.huateng.toprules.core.annotation.ModelDomainInstance;


@ModelDomainInstance(label = "宜信审核V3_宜信记录", type = "number", adapter = "engine.rule.domain.adapter.EnumDomainAdapter", params = "X_CheckCreditEaseRecord,Catfish.Platform.ManualJobV3.Handlers")
public class CheckCreditEaseRecordForApp{
}

