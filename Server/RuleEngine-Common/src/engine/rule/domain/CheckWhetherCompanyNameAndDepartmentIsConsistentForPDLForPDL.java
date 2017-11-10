package engine.rule.domain;

import com.huateng.toprules.core.annotation.ModelDomainInstance;


@ModelDomainInstance(label = "PDL-工作证检查_公司名/部门信息比对", type = "number", adapter = "engine.rule.domain.adapter.EnumDomainAdapter", params = "X_CheckWhetherCompanyNameAndDepartmentIsConsistentForPDL,Catfish.Platform.ManualJobV4.PDLHandlers")
public class CheckWhetherCompanyNameAndDepartmentIsConsistentForPDLForPDL{
}

