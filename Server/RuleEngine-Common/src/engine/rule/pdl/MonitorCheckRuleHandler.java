package engine.rule.pdl;

import java.util.Map;

import catfish.base.DescriptionParser;
import catfish.base.Logger;
import catfish.base.business.common.InstalmentChannel;
import catfish.base.business.common.JobStatus;
import catfish.base.business.common.RuleEngineDecisionResult;
import catfish.base.business.dao.FraudRuleResultDao;
import catfish.base.business.util.DatabaseApiUtil;
import catfish.base.business.util.EnumUtils;
import catfish.base.queue.QueueMessager;
import engine.rule.execute.RuleExecutor;
import engine.rule.execute.RuleManager;
import engine.rule.model.creator.CompositeModelCreator;
import engine.rule.model.creator.pdl.FraudCheckModelCreator;
import engine.rule.model.creator.pdl.InvestigationModelCreator;
import engine.rule.model.creator.pdl.OutModelCreator;
import engine.rule.model.inout.pdl.DecisionInOutForm;
import engine.rule.model.inout.pdl.StoreInOutForm;

public class MonitorCheckRuleHandler extends ApplicationRuleHandler<QueueMessager>{

    @Override
    public QueueMessager handle(QueueMessager message) {
        String appId = message.getAppId();
        
        /***************just for testing********************/
        if(isForTesting)
        {
            //InstallmentApplicationAction.Approve(message.getAppId());
            message.setJobStatus(JobStatus.Approved);
            return message;
        }
        /***************************************************/
                
        if (!DatabaseApiUtil.isNormalOrTestUser(appId)) {
        	Logger.get().info(String.format("The user of AppId: %s is not a normal or test user.", appId));
            message.setJobStatus(JobStatus.Approved);
            return message;
        }
        boolean isContinuable = false;
        String resultStr = JobStatus.Rejected;
        
        try {
            RuleExecutor executor = RuleManager.GetRuleExecutor(InstalmentChannel.PayDayLoanApp, message.getJobName());
            
            FraudCheckModelCreator fraudCheckModelCreator = new FraudCheckModelCreator(appId);
            InvestigationModelCreator investigationModelCreator = new InvestigationModelCreator(appId);

            CompositeModelCreator compositeModeCreator = new CompositeModelCreator();
            compositeModeCreator.addCreator(fraudCheckModelCreator);
            compositeModeCreator.addCreator(investigationModelCreator);
            
            Map<String, Object> out = executor.ExecuteRuleAndGetResponse(
                    compositeModeCreator, new OutModelCreator(appId));
            DecisionInOutForm result = (DecisionInOutForm) out.get("inout_Decision");
            Integer decisionResult = result.getDecisionResult();
            StoreInOutForm storeForm = (StoreInOutForm) out.get("inout_Store");

            if(decisionResult == RuleEngineDecisionResult.RecheckingRequired.getValue())
            	Logger.get().error(String.format("Unexpected result returned from toprules of AppId: %s, please check the rules again.", appId));
            
            isContinuable = (decisionResult == RuleEngineDecisionResult.Approved.getValue());
            
            this.recordReasonIfRejected(appId, result);
            ProcessRuleResult(appId, message.getJobName(), isContinuable, 0, result, storeForm);
            FraudRuleResultDao.updateRuleResult(appId, result.getFraudCheckScore().intValue());
            RuleEngineDecisionResult decisionResultEnum = EnumUtils.parse(RuleEngineDecisionResult.class, decisionResult);
            resultStr = DescriptionParser.getDescription(decisionResultEnum);
            message.setJobStatus(resultStr);
            return message;
        } catch (Exception e) {
        	Logger.get().warn("Execute rule error of AppId: " + appId, e);
        }
        return null;
    }

}
