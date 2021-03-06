package jma.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import catfish.base.Logger;
import catfish.base.StartupConfig;
import catfish.base.business.util.AppDerivativeVariableManager;
import catfish.base.business.util.RawData;
import catfish.base.business.util.RawDataStorageManager;
import catfish.cowfish.application.model.ApplicationModel;
import grasscarp.account.model.Account;
import grasscarp.user.model.User;
import jma.AppDerivativeVariablesBuilder;
import jma.NonBlockingJobHandler;
import jma.RetryRequiredException;
import jma.models.BuzTypeMobileNetWorkAndThreeResponseModel;
import jma.models.DataSourceResponseBase;
import jma.models.DerivativeVariableNames;
import jma.models.RawDataVariableNames;
import jma.resource.CLApplicationResourceFactory;
import jma.resource.UserResourceFactory;
import jma.util.DSPApiUtils;
/**
 * 手机在网时长接入LTV
 * @author yeyb
 * @date 2017年8月10日
 */
public class CheckUserOnBuzTypeMobileNetWorkForCLHandler extends NonBlockingJobHandler{
	
	static final String url = StartupConfig.get("dsp.api.buzType.queryMobileNetWork");
	@Override
	public void execute(String appId) throws RetryRequiredException {
		
		if(!isOpenSwitch("BuzTypeSwitch")){
			return;
		}
		Logger.get().info("CheckUserOnBuzTypeMobileNetWorkForCLHandler execute appId:" + appId);
		Map<String, Object> param = getUserBaseInfoModel(appId);
		if(param==null){
			Logger.get().info("CheckUserOnBuzTypeMobileNetWorkForCLHandler not execute,because the dsp param is null, appId:" + appId);
			return;
		}
		try {
			//调用手机在网时长dsp
            DataSourceResponseBase<BuzTypeMobileNetWorkAndThreeResponseModel> res=DSPApiUtils.invokeDspApi(appId, url, param, new TypeToken<DataSourceResponseBase<BuzTypeMobileNetWorkAndThreeResponseModel>>() {
            }.getType());
            if(res.getCode() != 200){
                Logger.get().info(String.format("request doesnot success,retry. url=%s, result=%s", url, new Gson().toJson(res)));
                throw new RetryRequiredException();
            }
            List<BuzTypeMobileNetWorkAndThreeResponseModel> data = res.getData();
            if(data == null || data.size() == 0){
                Logger.get().info("response data is null.");
                return ;
            }
            BuzTypeMobileNetWorkAndThreeResponseModel model=data.get(0);
            RawDataStorageManager.addRawDatas(new RawData(appId,RawDataVariableNames.BUZTYPE_MOBILENETWORK,new Gson().toJson(model)));
          //写mongo
            AppDerivativeVariableManager.addVariables(new AppDerivativeVariablesBuilder(appId)
            		.isNotNullAdd(DerivativeVariableNames.MOBILENETWORK_RESULT,Integer.parseInt(model.getResult()))
            		.isNotNullAdd(DerivativeVariableNames.MOBILENETWORK_MESSAGE,model.getMessage())
            		.build());

		} catch (Exception e) {
            Logger.get().warn(String.format("exception occurred!appId=%s, url=%s, param=%s", appId, url, param.toString()), e);
            throw new RetryRequiredException();
        }
	}

	public Map<String, Object> getUserBaseInfoModel(String appId) {
        ApplicationModel clApp = CLApplicationResourceFactory.getApplication(appId);
        if(clApp==null){
        	Logger.get().warn(String.format("***get null ApplicationModel from cowfishHost,appId=%s",appId));
        	return null;
        }
        User user = UserResourceFactory.getUser(clApp.userId);
        Account userAccount = UserResourceFactory.getUserAccount(clApp.userId);
        Map<String, Object> param = new HashMap<String, Object>();
        if(user!=null){
        	param.put("name", user.getIdName());
        	param.put("idNo", user.getIdNumber());
        }
        if(userAccount!=null){
        	param.put("mobile", userAccount.getMobile());
        }
		return param;
	}

}
