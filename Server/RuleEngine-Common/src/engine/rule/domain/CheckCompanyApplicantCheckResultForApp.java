package engine.rule.domain;

import com.huateng.toprules.core.annotation.ModelDomainInstance;


@ModelDomainInstance(label = "公司电话审核V3_申请人核对", type = "number", adapter = "engine.rule.domain.adapter.EnumDomainAdapter", params = "X_CheckCompanyApplicantCheckResult,Catfish.Platform.ManualJobV3.Handlers")
public class CheckCompanyApplicantCheckResultForApp{
}

