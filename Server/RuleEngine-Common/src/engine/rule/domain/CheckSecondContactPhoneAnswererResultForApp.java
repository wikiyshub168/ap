package engine.rule.domain;

import com.huateng.toprules.core.annotation.ModelDomainInstance;


@ModelDomainInstance(label = "第二联系人电话审核V3_通话人接听情况", type = "number", adapter = "engine.rule.domain.adapter.EnumDomainAdapter", params = "X_CheckSecondContactPhoneAnswererResult,Catfish.Platform.ManualJobV3.Handlers")
public class CheckSecondContactPhoneAnswererResultForApp{
}

