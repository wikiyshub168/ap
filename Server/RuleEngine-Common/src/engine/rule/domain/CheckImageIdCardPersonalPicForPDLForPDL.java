package engine.rule.domain;

import com.huateng.toprules.core.annotation.ModelDomainInstance;


@ModelDomainInstance(label = "PDL-身份证照审核V3_身份证件是否清晰可辨识", type = "number", adapter = "engine.rule.domain.adapter.EnumDomainAdapter", params = "X_CheckImageIdCardPersonalPicForPDL,Catfish.Platform.ManualJobV4.PDLHandlers")
public class CheckImageIdCardPersonalPicForPDLForPDL{
}

