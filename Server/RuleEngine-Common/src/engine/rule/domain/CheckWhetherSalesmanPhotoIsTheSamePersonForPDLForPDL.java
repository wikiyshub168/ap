package engine.rule.domain;

import com.huateng.toprules.core.annotation.ModelDomainInstance;


@ModelDomainInstance(label = "PDL-合影检查_销售照片判断（是否为同一个人）", type = "number", adapter = "engine.rule.domain.adapter.EnumDomainAdapter", params = "X_CheckWhetherSalesmanPhotoIsTheSamePersonForPDL,Catfish.Platform.ManualJobV4.PDLHandlers")
public class CheckWhetherSalesmanPhotoIsTheSamePersonForPDLForPDL{
}

