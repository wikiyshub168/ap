package engine.rule.domain;

import com.huateng.toprules.core.annotation.ModelDomainInstance;


@ModelDomainInstance(label = "PDL-客户电话审核_身份证住址确认", type = "number", adapter = "engine.rule.domain.adapter.EnumDomainAdapter", params = "X_CheckUserIDCardAddressForPDL,Catfish.Platform.ManualJobV4.PDLHandlers")
public class CheckUserIDCardAddressForPDLForPDL{
}

