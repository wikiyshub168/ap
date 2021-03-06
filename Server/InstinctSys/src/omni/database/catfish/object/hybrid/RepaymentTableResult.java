/**
 * Copyright (C), 上海秦苍信息科技有限公司
 */
package omni.database.catfish.object.hybrid;

import java.util.List;
import java.util.Map;

/**
 * 〈账务接口查询二次贷相关情况〉
 *
 * @author hwei
 * @version RepaymentTableResult.java, V1.0 2016年12月30日 下午3:43:24
 */
public class RepaymentTableResult {
 private PaymentDetail principal;
    
    private PaymentDetail interest;  
     
    private Map<String, PaymentDetail> extendFees; //服务费
     
    private List<PaymentDetail> penalties; //罚金列表
     
    private Integer instalmentNum; //当前条目的期数

    public PaymentDetail getPrincipal() {
        return principal;
    }

    public void setPrincipal(PaymentDetail principal) {
        this.principal = principal;
    }

    public PaymentDetail getInterest() {
        return interest;
    }

    public void setInterest(PaymentDetail interest) {
        this.interest = interest;
    }

    public Map<String, PaymentDetail> getExtendFees() {
        return extendFees;
    }

    public void setExtendFees(Map<String, PaymentDetail> extendFees) {
        this.extendFees = extendFees;
    }

    public List<PaymentDetail> getPenalties() {
        return penalties;
    }

    public void setPenalties(List<PaymentDetail> penalties) {
        this.penalties = penalties;
    }

    public Integer getInstalmentNum() {
        return instalmentNum;
    }

    public void setInstalmentNum(Integer instalmentNum) {
        this.instalmentNum = instalmentNum;
    }
    
    
}
