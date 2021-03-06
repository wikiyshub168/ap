package engine.rule.test.validator.pdl;

import java.util.Map;
import java.util.Set;

import catfish.base.Logger;
import catfish.base.business.common.RuleEngineDecisionResult;
import engine.rule.model.BaseForm;
import engine.rule.model.inout.pdl.DecisionInOutForm;
import engine.rule.test.validator.ResultValidator;

public class DecisionResultValidator implements ResultValidator{

	protected String expectedDecision;
	protected String expectedRejReason;
	
	protected void initDefaultValue()
	{
	    if(expectedDecision == null || expectedDecision.equalsIgnoreCase(NULL))
	    {
	    	expectedDecision = RuleEngineDecisionResult.Approved.getValue().toString();
	    }
	    if(expectedRejReason == null || !expectedRejReason.equalsIgnoreCase(NULL))
	    {
	    	expectedRejReason = "";
	    }
	}
	@Override
	public boolean validate(BaseForm form, Map<String, String> columnValueMappings) {

    	DecisionInOutForm decisionInOutForm = (DecisionInOutForm)form;
    	
		expectedDecision = columnValueMappings.get(DECISION);
		expectedRejReason = columnValueMappings.get(REJREASONSET);
		Logger.get().info("expectedDecision is " + expectedDecision);
		Logger.get().info("expectedRejReason is " + expectedRejReason);
		initDefaultValue();
		
		String realDecision = decisionInOutForm.getDecisionResult().toString();
		
		Logger.get().info("realDecision is " +  realDecision);
		
		if(!realDecision.equals(expectedDecision))
		{
			return false;
		}
		
		//if result is approved, then ignore other status
		if(decisionInOutForm.getDecisionResult() == RuleEngineDecisionResult.Approved.getValue() && realDecision.equals(expectedDecision)){
			return true;
		}
		
		Set<String> realRejReason = decisionInOutForm.getDecisionRejectReasonSet();
		for (String rr : realRejReason){
		    Logger.get().info("realRejReason is " +  rr);
		}
		
		if(decisionInOutForm.getDecisionResult() == RuleEngineDecisionResult.Canceled.getValue() && realDecision.equals(expectedDecision))
		{
			if(realRejReason.size() != 0 && !realRejReason.contains(expectedRejReason))
			{
				return false;
			}
			if(!expectedRejReason.equals(""))
			{
				return false;
			}
			return true;
		}
		
		return true;
	}

}
