package catfish.plugins.pdfgenerator;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import catfish.base.Logger;
import catfish.base.StartupConfig;
import catfish.base.business.common.AttachmentType;
import catfish.base.business.common.InstalmentChannel;
import catfish.base.business.dao.ApplicationAttachmentDao;
import catfish.base.business.dao.AttachmentDao;
import catfish.base.business.dao.InstallmentApplicationDao;
import catfish.base.business.dao.UserAttachmentDao;
import catfish.base.business.object.ApplicationAttachmentObject;
import catfish.base.business.object.AttachmentObject;
import catfish.base.business.object.UserAttachmentObject;
import catfish.plugins.finance.RepaymentItem;

import com.google.common.io.CharStreams;
import com.itextpdf.text.pdf.codec.Base64;

public class AgreementService {
	public static String HTML_BASE = StartupConfig.get("catfish.agreement.partial-base") == null ? 
									"./agreementTemplate/base.html" : StartupConfig.get("catfish.agreement.partial-base");
	public static String HTML_REGISTER = StartupConfig.get("catfish.agreement.partial-register") == null ? 
									"./agreementTemplate/registerterms.html" : StartupConfig.get("catfish.agreement.partial-register");
	public static String HTML_SERVICE = StartupConfig.get("catfish.agreement.partial-service") == null ? 
									"./agreementTemplate/partial-service.html" : StartupConfig.get("catfish.agreement.partial-service");
	public static String HTML_TRIPARTITE_FOTIC = StartupConfig.get("catfish.agreement.partial-fotic") == null ?
									"./agreementTemplate/partial-fotic.html" : StartupConfig.get("catfish.agreement.partial-fotic");
	public static String HTML_TRIPARTITE_LTZ = StartupConfig.get("catfish.agreement.partial-ltz") == null ? 
									"./agreementTemplate/partial-ltz.html" : StartupConfig.get("catfish.agreement.partial-ltz");
	public static String HTML_TRIPARTITE_SELF = StartupConfig.get("catfish.agreement.partial-self") == null ? 
									"./agreementTemplate/partial-self.html" : StartupConfig.get("catfish.agreement.partial-self");
	public static String HTML_TRIPARTITE_PDL_SELF = StartupConfig.get("catfish.agreement.partial-pdl-self") == null ? 
            "./agreementTemplate/partial-pdl-self.html" : StartupConfig.get("catfish.agreement.partial-pdl-self");
	public static String HTML_TRIPARTITE_JMBOX = StartupConfig.get("catfish.agreement.partial-jmbox") == null ? 
									"./agreementTemplate/partial-jmbox.html" : StartupConfig.get("catfish.agreement.partial-jmbox");
	public static String HTML_TRIPARTITE_ZRB = StartupConfig.get("catfish.agreement.partial-zrb") == null ? 
									"./agreementTemplate/partial-zrb.html" : StartupConfig.get("catfish.agreement.partial-zrb");
	public static String HTML_TRIPARTITE_PDL = StartupConfig.get("catfish.agreement.partial-pdl") == null ?
									"./agreementTemplate/partial-pdl.html" : StartupConfig.get("catfish.agreement.partial-pdl");
	public static String HTML_MINREPAYMENT_PDL = StartupConfig.get("catfish.agreement.partial-minrepayment") == null ?
			"./agreementTemplate/partial-pdl-minrepayment-service.html" : StartupConfig.get("catfish.agreement.partial-minrepayment");
	private static String HTML_TEST_PRODUCT = StartupConfig.get("catfish.agreement.partial-test") == null ?
									"./agreementTemplate/partial-test.html" : StartupConfig.get("catfish.agreement.partial-test");
	
	public static final String CSS = StartupConfig.get("catfish.pdf.css") == null ? 
									"./agreementTemplate/mobile.css" : StartupConfig.get("catfish.pdf.css");
	private static final String IMG_PATH = StartupConfig.get("catfish.pdf.img") == null ? 
									"./agreementTemplate" : StartupConfig.get("catfish.pdf.img");
	private static final int BUFFER_SIZE = 1024;
	private static final long TICKS_AT_EPOCH = 621355968000000000L; 
	private static String stampimg = getImgBaset64FromFile(IMG_PATH+"/stamp.gif");
	public static final String FUNTAG_ZRB = StartupConfig.get("catfish.fundtag.zrb") == null ? 
									"ZhenRongBao" : StartupConfig.get("catfish.fundtag.zrb");
	public static final String FUNTAG_AXDL = StartupConfig.get("catfish.fundtag.axdl", "AXDL");
	public static final int PDFBOX_DPI = StartupConfig.getAsInt("catfish.agreement.dpi", 90);
	public static final String FUNDTAG_SELF =StartupConfig.get("catfish.fundtag.self", "SELF");
	//新增合投资金源
	public static final String FUNDTAG_CROWDFUNDING1 =StartupConfig.get("catfish.fundtag.crowdfunding1", "CROWDFUNDING1");
	public static final String FUNDTAG_CROWDFUNDING2 =StartupConfig.get("catfish.fundtag.crowdfunding2", "CROWDFUNDING2");
	public static final String FUNDTAG_CROWDFUNDING3 =StartupConfig.get("catfish.fundtag.crowdfunding3", "CROWDFUNDING3");
	//铜板街
	public static final String FUNDTAG_COPPERSTREET =StartupConfig.get("catfish.fundtag.copperstreet", "COPPERSTREET");
	
	public AgreementService() {
		Logger.get().info("AgreementService Start");
	}

	public String getHtmlDom(String appId, AgreementType type) {
		if((appId == null) && (type != AgreementType.REGISTER)) {
			Logger.get().warn("AppId is null and Agreement Type is not REGISTER");
		}

		// register agreement
		if(type == AgreementType.REGISTER) {
			return getRegisterAgreement(HTML_BASE);
		}
		// service and tripartite agreement
		boolean fillServicee = false;
		boolean fillTripartite = false;
		String htmlFile = null;
		
		switch(type) {
		case SERVICE:
			fillServicee = true;
			break;
		case PRETRIPARTITE:
			fillTripartite = true;
			break;
		case BOTH:
			fillServicee = true;
			fillTripartite = true;
		case TRIPARTITE:
			fillTripartite = true;
		default:
			break;
		}
		
		try (FileInputStream finStream = new FileInputStream(HTML_BASE)) {
			
			String baseStr = CharStreams.toString(new InputStreamReader(finStream, "UTF-8"));
			String result = "";
			
			ApplicationAgreementModel model = AgreementApi.getUserModel(appId);
        	if(model.getInstallmentChannel() == InstalmentChannel.PayDayLoanApp.getValue()) {
        		if(fillTripartite) {
    				result = baseStr.replace("%TripartiteAgreement%", getTripartiteAgreement(appId));
    				result = result.replace("%BaseTitle%", "服务协议");
    				result = result.replace("%ServiceAgreement%", "");
    				return result;
    			} else {
    				if(fillServicee) {
	    				result = baseStr.replace("%ServiceAgreement%", getServiceAgreement(appId));
	    				result = result.replace("%BaseTitle%", "服务协议");
	    			} else {
	    				result = baseStr.replace("%ServiceAgreement%", "");
	    			}
    				result = result.replace("%TripartiteAgreement%", "");
    				return result;
    			}
    		}
        	
			if(fillTripartite) {
				result = baseStr.replace("%TripartiteAgreement%", getTripartiteAgreement(appId));
				result = result.replace("%BaseTitle%", "三方协议");
			} else {
				result = baseStr.replace("%TripartiteAgreement%", "");
			}
				
			if(fillServicee) {
				result = result.replace("%ServiceAgreement%", getServiceAgreement(appId));
				result = result.replace("%BaseTitle%", "服务协议");
			} else {
				result = result.replace("%ServiceAgreement%", "");
			}
			result = result.replace("%BaseTitle%", "");
			
			return result;
		} catch(IOException e) {
			Logger.get().error(e);
		}
		
		return "获取协议失败";
	}
	
	private String getRegisterAgreement(String fileName) {
		
		try (FileInputStream fins = new FileInputStream(fileName)) {
			
			String baseStr = CharStreams.toString(new InputStreamReader(fins, "UTF-8"));
			return baseStr;
		} catch (FileNotFoundException e) {
			Logger.get().error(e);
		} catch (UnsupportedEncodingException e) {
			Logger.get().error(e);
		} catch (IOException e) {
			Logger.get().error(e);
		}
		return null;
		
	}
	
	public String getServiceAgreement(String appId) {
		String htmlFile;
		try {
        	ApplicationAgreementModel model = AgreementApi.getUserModel(appId);
        	if(model.getInstallmentChannel() == InstalmentChannel.PayDayLoanApp.getValue()) {
        		htmlFile = HTML_MINREPAYMENT_PDL;
        		return CharStreams.toString(new InputStreamReader(constructUserInfoStream(appId, model, htmlFile), "UTF-8"));
        	} else {
        		Logger.get().info("not paydayLoan app, no service agreement");
        	}
		} catch (UnsupportedEncodingException e) {
			Logger.get().error(e);
		} catch (IOException e) {
			Logger.get().error(e);
		}
		return "";
		
	}
	
	private String getTripartiteAgreement(String appId) {
		String htmlFile;
		try {
        	ApplicationAgreementModel model = AgreementApi.getAppAgreementModel(appId);
        	if(model.getMoneyTransferredOn()!=null){
        		Calendar cal = Calendar.getInstance();
        		cal.set(2015, 6, 15, 0, 0);
        		if(model.getMoneyTransferredOn().before(cal.getTime())) {
        			Logger.get().info("old agreement required");
        			return turnPDFtoJPG(model.getAppId(), AttachmentType.ApplicationAgreementPDF);
        		}
        	}
        	
        	if(model.getInstallmentChannel() == InstalmentChannel.PayDayLoanApp.getValue()) {
        		if(FUNDTAG_SELF.toLowerCase().equals(model.getFundTag().toLowerCase())) {
        			if(PdlServerService.isOldWithdrawal(model.getToday())) {
        				htmlFile = HTML_TRIPARTITE_PDL_SELF;
        			} else {
        				htmlFile = HTML_MINREPAYMENT_PDL;
        			}
        		}else
        		{
        			htmlFile = HTML_TRIPARTITE_PDL;
        		}
        	} else {
				if (AgreementApi.isTestProduct(appId)) {
					htmlFile = HTML_TEST_PRODUCT;
				} else if(FUNDTAG_SELF.toLowerCase().equals(model.getFundTag().toLowerCase())) {
        			htmlFile = HTML_TRIPARTITE_SELF;
        		}else if(("jmbox").equals(model.getFundTag().toLowerCase())) {
        			htmlFile = HTML_TRIPARTITE_JMBOX;
        		} else if(("lantouzi").equals(model.getFundTag().toLowerCase())) {
        			htmlFile = HTML_TRIPARTITE_LTZ;
        		} else if(("fotic1").equals(model.getFundTag().toLowerCase())){
        			htmlFile = HTML_TRIPARTITE_FOTIC;
        		} else if(FUNTAG_ZRB.toLowerCase().equals(model.getFundTag().toLowerCase())){
        			htmlFile = HTML_TRIPARTITE_ZRB;
				} else if(FUNTAG_AXDL.toLowerCase().equals(model.getFundTag().toLowerCase())){
					htmlFile = HTML_TRIPARTITE_LTZ;
        		}
				//增加合投资金源  使用自有资金源的三方模板
				else if(FUNDTAG_CROWDFUNDING1.toLowerCase().equals(model.getFundTag().toLowerCase()) ||
						FUNDTAG_CROWDFUNDING2.toLowerCase().equals(model.getFundTag().toLowerCase()) ||
						FUNDTAG_CROWDFUNDING3.toLowerCase().equals(model.getFundTag().toLowerCase())){
					htmlFile = HTML_TRIPARTITE_SELF;
				}
				//铜板街资金源和懒投资一样
				else if(FUNDTAG_COPPERSTREET.toUpperCase().equals(model.getFundTag().toUpperCase())){
					htmlFile = HTML_TRIPARTITE_LTZ;
				}
				else {
        			return null;
        		}
        	}
        	
			return CharStreams.toString(new InputStreamReader(constructHtmlStream(appId, model, htmlFile), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			Logger.get().error(e);
		} catch (IOException e) {
			Logger.get().error(e);
		}
		return "";
	}
	
	
	private InputStream constructHtmlStream(String appId, String htmlFile) {
        	ApplicationAgreementModel model = AgreementApi.getAppAgreementModel(appId);
        	if(model.getInstallmentChannel() == InstalmentChannel.PayDayLoanApp.getValue()) {
        		return constructHtmlStream(appId, model, htmlFile);
        	} else {

        		if(model.getFundTag().toLowerCase().equals("jmbox")) {
        			return constructHtmlStream(appId, model, htmlFile);
        		} else if(model.getFundTag().toLowerCase().equals("lantouzi")) {
        			return constructHtmlStream(appId, model, htmlFile);
        		} else {
        			return constructHtmlStream(appId, model, htmlFile);
        		}
        	}
	}
	
	private InputStream constructUserInfoStream(String appId, ApplicationAgreementModel model, String htmlFile) {
		try (FileInputStream fin = new FileInputStream(htmlFile)) {
        	
			String htmlStr = CharStreams.toString(new InputStreamReader(fin, "UTF-8"));
			
			htmlStr = htmlStr.replace("./stamp.gif", "data:image/gif;base64, "+stampimg);
			htmlStr = htmlStr.replace("%Name%", model.getName());
			htmlStr = htmlStr.replace("%IdNumber%", model.getIdNumber());
			htmlStr = htmlStr.replace("%Address%", checkoutStr(model.getAddress()));
			htmlStr = htmlStr.replace("%Mobile%", checkoutStr(model.getMobile()));
			SimpleDateFormat format = new SimpleDateFormat("yyyy年M月d日");
			htmlStr = htmlStr.replace("%BankName%", model.getBankName());
			htmlStr = htmlStr.replace("%BankAccount%", model.getBankAccount());
			htmlStr = htmlStr.replace("%BankAccountName%", model.getBankAccountName());
			htmlStr = htmlStr.replace("%Today%", format.format(model.getToday()));
			htmlStr = htmlStr.replace("%PayDay%", PdlServerService.getPdlOpenAccountPayDay(appId));
			htmlStr = htmlStr.replace("%InterestRate%", PdlServerService.getInterestRate());
			htmlStr = htmlStr.replace("%ServiceFeeRate%", PdlServerService.getServiceFeeRate());
			
			htmlStr = fillNullSymbol(htmlStr);

			return new ByteArrayInputStream(htmlStr.getBytes(Charset.forName("UTF-8")));
		} catch (Exception e) {
			Logger.get().error(e);
		}
		return null;
	}
	
	private InputStream constructHtmlStream(String appId, ApplicationAgreementModel model, String htmlFile) {
        try (FileInputStream fin = new FileInputStream(htmlFile)) {
        	String htmlStr = CharStreams.toString(new InputStreamReader(fin, "UTF-8"));
			// 头像信息
//			String idImgStr =  getImgBase64String(appId, AttachmentType.UserIdPhoto);
//			String headImgStr =  getImgBase64String(appId, AttachmentType.UserHeadPhoto);
//			if(idImgStr != null) {
//				htmlStr = htmlStr.replace("%IdPhoto%", "data:image/jpg;base64, "+idImgStr);
//			} else {
//				htmlStr = htmlStr.replace("%IdPhoto%", "");
//			}
//			if(headImgStr != null) {
//				htmlStr = htmlStr.replace("%HeadPhoto%", "data:image/jpg;base64, "+headImgStr);
//			} else {
//				htmlStr = htmlStr.replace("%HeadPhoto%", "");
//			}
			// 基本信息
			htmlStr = htmlStr.replace("./stamp.gif", "data:image/gif;base64, "+stampimg);
			htmlStr = htmlStr.replace("%Name%", model.getName());
			htmlStr = htmlStr.replace("%IdNumber%", model.getIdNumber());
			htmlStr = htmlStr.replace("%Address%", checkoutStr(model.getAddress()));
			htmlStr = htmlStr.replace("%Principal%", String.valueOf(model.getPrincipal().setScale(2, BigDecimal.ROUND_HALF_UP).toString()));
			htmlStr = htmlStr.replace("%PrincipalString%", model.getPrincipalString());
			htmlStr = htmlStr.replace("%MonthlyPay%", String.valueOf(model.getMonthlyPay().intValue()));
			htmlStr = htmlStr.replace("%Repayments%", String.valueOf(model.getRepayments()));
			htmlStr = htmlStr.replace("%InterestPerMonth%", model.getInterestPerMonth().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
			htmlStr = htmlStr.replace("%PrincipalInMonthlyPay%", model.getPrincipalInMonthlyPay().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
			htmlStr = htmlStr.replace("%InterestsInMontylyPay%", model.getInterestInMonthlyPay().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
			htmlStr = htmlStr.replace("%FeeInMonthlyPay%", model.getFeeInMonthlyPay().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
			htmlStr = htmlStr.replace("%FeeInMonthlyPayStr%", model.getFeeInMonthlyPayStr());
			htmlStr = htmlStr.replace("%PrincipalInterestPerMonth%", model.getPrincipalInterestPerMonth().setScale(2, BigDecimal.ROUND_HALF_UP).toString());

			BigDecimal Interest = model.getPrincipalInterestPerMonth().subtract(model.getPrincipal());
			htmlStr = htmlStr.replace("%Interest%", Interest.setScale(2, BigDecimal.ROUND_HALF_UP).toString());
			
			htmlStr = htmlStr.replace("%Mobile%", checkoutStr(model.getMobile()));
			htmlStr = htmlStr.replace("%PayDay%", PdlServerService.getWithDrawalPayday(model.getFirstPaybackDate()));
			htmlStr = htmlStr.replace("%InterestRate%", PdlServerService.getInterestRate());
			htmlStr = htmlStr.replace("%ServiceFeeRate%", PdlServerService.getServiceFeeRate());

			htmlStr = htmlStr.replace("%Fee%", model.getFee().toString());
			htmlStr = htmlStr.replace("%FeeStr%", model.getFeeStr());
			htmlStr = htmlStr.replace("%FeeInMonthlyPay%", model.getFeeInMonthlyPay().toString());
			htmlStr = htmlStr.replace("%TotalPaybackStr%", model.getTotalPaybackStr());
			htmlStr = htmlStr.replace("%FirstPayback%", model.getFirstPayback().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
			htmlStr = htmlStr.replace("%FirstPaybackStr%", model.getFirstPaybackStr());
			htmlStr = htmlStr.replace("%OtherPaybackStr%", checkoutStr(model.getOtherPaybackStr()));
			if(model.getOtherPayback() != null) {
				htmlStr = htmlStr.replace("%OtherPayback%", model.getOtherPayback().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
			}
			htmlStr = htmlStr.replace("%MonthlyPaybackDay%", String.valueOf(model.getMonthlyPackbackDay()));
			SimpleDateFormat format = new SimpleDateFormat("yyyy年M月d日");
			htmlStr = htmlStr.replace("%FirstPaybackDate%", format.format(model.getFirstPaybackDate()));
			htmlStr = htmlStr.replace("%MonthlyPaybackDay%", String.valueOf(model.getMonthlyPackbackDay()));
			htmlStr = htmlStr.replace("%BankName%", model.getBankName());
			htmlStr = htmlStr.replace("%BankAccount%", model.getBankAccount());
			htmlStr = htmlStr.replace("%BankAccountName%", model.getBankAccountName());
			htmlStr = htmlStr.replace("%Today%", format.format(model.getToday()));
			htmlStr = htmlStr.replace("%MerchantStoreAddress%", model.getMerchantAddress());
			htmlStr = htmlStr.replace("%EndTime%", format.format(model.getEndTime()));
			htmlStr = htmlStr.replace("%MerchantName%", model.getMerchantName());
//			htmlStr = htmlStr.replace("%ProductName%", model.getProductName());
			double feeRate = model.getFee().doubleValue()*100/model.getPrincipal().doubleValue();
			htmlStr = htmlStr.replace("%FeeRate%", new BigDecimal(feeRate).setScale(3, BigDecimal.ROUND_HALF_UP).toString());
			htmlStr = htmlStr.replace("%AppId%", model.getAppId().replace("-", ""));
			htmlStr = htmlStr.replace("%InstallmentStartedOn%", format.format(model.getInstallmentStartedOn()));
			if (model.getRepaymentSchedule() != null) {
				String rs = "";
				for(RepaymentItem item : model.getRepaymentSchedule()) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(model.getFirstPaybackDate());
					int repayNum = item.repaymentNumber-1;
					if(repayNum < 0) {
						repayNum = 0;
					}
					cal.add(Calendar.MONTH, repayNum);
					rs += String.format("<tr><td>%d</td><td>%s</td><td>本息服务费共【%s】元</td></tr>", item.repaymentNumber
							, format.format(cal.getTime()), item.principal.add(item.interest).add(item.fee)
									.add(item.accountFee).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
				}
				htmlStr = htmlStr.replace("%RepaymentSchedule%", rs);
				htmlStr = htmlStr.replace("%Penalty%", model.getPenalty().toString());
			} else {
				htmlStr = htmlStr.replace("%RepaymentSchedule%", "<tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr>");
			}
			
			htmlStr = fillNullSymbol(htmlStr);
			
			return new ByteArrayInputStream(htmlStr.getBytes(Charset.forName("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			Logger.get().error(e);
		} catch (IOException e) {
			Logger.get().error(e);
		}
		return null;
	}
	
	private String checkoutStr(String in) {
		if (in == null || in.isEmpty()) {
			return "";
		}
		return in;
	}
	
	private String fillNullSymbol(String htmlStr) {
//		htmlStr = htmlStr.replace("%IdPhoto%", "");
//		htmlStr = htmlStr.replace("%HeadPhoto%", "");
		// 基本信息
		htmlStr = htmlStr.replace("%Name%", "");
		htmlStr = htmlStr.replace("%IdNumber%", "");
		htmlStr = htmlStr.replace("%Mobile%", "");
		htmlStr = htmlStr.replace("%Address%", "");
		htmlStr = htmlStr.replace("%Principal%", "");
		htmlStr = htmlStr.replace("%PrincipalString%", "");
		htmlStr = htmlStr.replace("%MonthlyPay%", "");
		htmlStr = htmlStr.replace("%Repayments%", "");
		htmlStr = htmlStr.replace("%InterestPerMonth%", "");
		htmlStr = htmlStr.replace("%PrincipalInMonthlyPay%", "");
		htmlStr = htmlStr.replace("%InterestsInMontylyPay%", "");
		htmlStr = htmlStr.replace("%FeeInMonthlyPay%", "");
		htmlStr = htmlStr.replace("%PrincipalInterestPerMonth%", "");
		htmlStr = htmlStr.replace("%FeeInMonthlyPayStr%", "");
		htmlStr = htmlStr.replace("%Fee%", "");
		htmlStr = htmlStr.replace("%FeeStr%", "");
		htmlStr = htmlStr.replace("%FeeInMonthlyPay%", "");
		htmlStr = htmlStr.replace("%TotalPaybackStr%", "");
		htmlStr = htmlStr.replace("%FirstPaybackStr%", "");
		htmlStr = htmlStr.replace("%FirstPayback%", "");
		htmlStr = htmlStr.replace("%OtherPaybackStr%", "");
		htmlStr = htmlStr.replace("%OtherPayback%", "");
		htmlStr = htmlStr.replace("%MonthlyPaybackDay%", "");
		htmlStr = htmlStr.replace("%FirstPaybackDate%", "");
		htmlStr = htmlStr.replace("%MonthlyPaybackDay%", "");
		htmlStr = htmlStr.replace("%BankName%", "");
		htmlStr = htmlStr.replace("%BankAccount%", "");
		htmlStr = htmlStr.replace("%BankAccountName%", "");
		htmlStr = htmlStr.replace("%Today%", "");
		htmlStr = htmlStr.replace("%EndTime%", "");
		htmlStr = htmlStr.replace("%MerchantName%", "");
		htmlStr = htmlStr.replace("%ProductName%", "");
		htmlStr = htmlStr.replace("%AppId%", "");
		htmlStr = htmlStr.replace("%InstallmentStartedOn%", "");
		htmlStr = htmlStr.replace("%RepaymentSchedule%", "<tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td></tr>");
		htmlStr = htmlStr.replace("%Penalty", "5");
		
		return htmlStr;
	}
	
	private String getImgBase64String(String appId, AttachmentType type) {
		String userId = InstallmentApplicationDao.getUserIdByAppId(appId);
		try {
			UserAttachmentDao uaDao = new UserAttachmentDao(userId, type);
			UserAttachmentObject uao = uaDao.getSingle();
			AttachmentDao aDo = new AttachmentDao(uao.AttachmentId);
			AttachmentObject ao = aDo.getSingle();
			InputStream imgStream = OssApi.get(ao.FilePath);
			byte[] imgByte = stream2byte(imgStream);
			return Base64.encodeBytes(imgByte);
		} catch(Exception e) {
			Logger.get().error(e);
		}
		return null;
	}
	
	private static String getImgBaset64FromFile(String filepath) {
		File file = new File(filepath);
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			BufferedImage bufimg = ImageIO.read(file);
		    ImageIO.write(bufimg, "gif", baos);  
		    byte[] bytes = baos.toByteArray();  
		    return Base64.encodeBytes(bytes);
		} catch (IOException e) {
			Logger.get().error(e);
		}
		
		return "";
	}
	
	public String turnPDFtoJPG(String appId, AttachmentType type) {
		ApplicationAttachmentDao aaDao = new ApplicationAttachmentDao(appId, type);
		ApplicationAttachmentObject aao = aaDao.getSingle();
		AttachmentDao aDo = new AttachmentDao(aao.AttachmentId);
		AttachmentObject ao = aDo.getSingle();
		try(InputStream pdfStream = OssApi.get(ao.FilePath)) {
			String htmlStr = "";
			PDDocument doc = PDDocument.load(pdfStream);
			PDFRenderer pdfRenderer = new PDFRenderer(doc);
			int pageCount = doc.getNumberOfPages(); 
			Logger.get().info(pageCount);
		    int pages = doc.getNumberOfPages(); 
		    for(int i=0;i<pages;i++){
		    	BufferedImage img = pdfRenderer.renderImageWithDPI(i, PDFBOX_DPI, ImageType.RGB);
		    	Iterator iter = ImageIO.getImageWritersBySuffix("jpg"); 
		    	ImageWriter writer = (ImageWriter)iter.next(); 
		    	ByteArrayOutputStream out = new ByteArrayOutputStream();
		    	ImageOutputStream outImage = ImageIO.createImageOutputStream(out); 
		    	writer.setOutput(outImage); 
		    	writer.write(new IIOImage(img,null,null)); 
		    	byte[] bytes = out.toByteArray();  
		    	String imgStr = Base64.encodeBytes(bytes);
		    	htmlStr += "<img src=\"data:image/jpg;base64,"+imgStr+"\" /><br />";
		    	out.close();
		    }
		    doc.close(); 
		    return htmlStr;
		} catch(Exception e) {
			Logger.get().error(e);
		}
		return "";
	}
	
	private byte[] stream2byte(InputStream inStream) throws IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
        byte[] data = new byte[BUFFER_SIZE];  
        int count = -1;  
        while((count = inStream.read(data,0,BUFFER_SIZE)) != -1)  
            outStream.write(data, 0, count);  
          
        data = null;  
        return outStream.toByteArray();  
	}
	
	public String pdfGenerateAndSave(TransferAgreementInfoModel model){
		PDFGenerator generator = new PDFGenerator();
		
//		System.out.println("======================================" + model.agreement );
//		System.out.println("++++++++++++++++++++++++++++++++++++++" + model.appId);
//		System.out.println("--------------------------------------" + model.userId);
//		StringBuffer sb = new StringBuffer();
//		sb.append("<!DOCTYPE html><html><head><meta http-equiv='Content-Type' content='text/html; charset=UTF-8' /><meta name='viewport' content='width=device-width, initial-scale=1, user-scalable=no' />");
//		sb.append("<style>body {font-size: 14px;}tr td {padding-left: 0.8em;text-align: left;}tr th {padding: 0.8em;}#agreement-title {text-align:center;font-weight:bold;font-size:2em;padding-top: 1.2em;}");
//		sb.append("#firstpart-title {text-align:center;font-weight:bold;font-size:1.5em;}#secondpart-title {text-align:center;font-weight:bold;font-size:1.5em;}.license-table {border: 1;border-collapse: collapse;width: 100%;");
//		sb.append("text-align: center;}</style></head><body style='font-family:SimSun;'><div id='agreement-text'><div id='agreement-title'>借款合同</div><br /><div style='float: right; padding-right: 20%'>编号：B6854100C477E511A974AC853D9F545E </div>");
//		sb.append("<br /><br /><div id='firstpart-title'>第一部分 借款合同具体信息表</div><br /><br /><table class='license-table' border='1' align='center'><tr><td><strong>甲方(贷款人)</strong>：</td><td>【侯翔宇】</td>");
//		sb.append("<td>【身份证号码】<br />310104198706266813 </td></tr><tr><td><strong>乙方(借款人)</strong>：</td><td>【徐程】</td><td>【身份证号码】 <br />320682198803300479 </td></tr><tr><td colspan='3'>");
//		sb.append("身份证照及头像照<br /><br /><br /><img src='[%%]' style='height: 250px'/>&nbsp;<img src='' style='height: 250px'/></td></tr><tr><td colspan='3'><strong>丙方(服务商)</strong>：上海秦苍信息科技有限公司</td>");
//		sb.append("</tr><tr><td colspan='3'>&nbsp;&nbsp;<strong>甲乙丙三方根据中国法律法规，就本借款合同的如下具体条款以及基本条款等，经协商达成一致，均同意信守。借款人对具体条款及基本条款已充分注意并完全理解。</strong></td>");
//		sb.append("</tr><tr><td rowspan='2' style='text-align: center'><strong>基本要素</strong></td><td>借款本金：【1000.00】元</td><td>分期期数：【1】期</td></tr><tr><td>【前三期月】还款金额：【1103.00】元<br />");
//		sb.append("【剩余期月】还款金额：【】元</td><td>【首次】还款日：【2015年10月28日】<br />【每月】还款日：【28】日(18:00前，节假日不顺延)</td></tr><tr><td rowspan='10' style='text-align: center'><strong>贷款合同要素</strong></td>");
//		sb.append("<td colspan='2'><strong>贷款金额</strong>：[人民币](大写)[壹仟圆]，(小写)[1000.00] <br />(大小写不一致以大写为准，下同)</td></tr><tr><td colspan='2'><strong>贷款期限</strong>：自2015年11月25日起至2015年12月28日止。</td>");
//		sb.append("</tr><tr><td colspan='2'><strong>贷款利率</strong>：为[ 0.70 &nbsp;%](利率)</td></tr><tr><td colspan='2'><strong>贷款偿还方式</strong>：等额本息</td></tr><tr><td colspan='2'><strong>贷款用途</strong>：消费</td>");
//		sb.append("</tr><tr><td colspan='2'><strong>还款分期月数</strong>：1</td></tr><tr><td colspan='2'>【每月】<strong>还款本息额</strong>：【1007.00】元</td></tr><tr><td colspan='2'>");
//		sb.append("<strong>客户服务费</strong>：共【96】元，若本合同项下借款为分期还款的，乙方应根据本合同约定的方式在【前三期】还款的同时将客户服务费支付完毕，每期还款时应支付的客户服务费金额为人民币【32.00  】元；若本合同项下借款为一次性还款的，乙方在还款时应支付到期应还服务费。");
//		sb.append("</td></tr><tr><td colspan='2'><strong>逾期滞纳金</strong>：5.00 元/日（自逾期之日至清偿完毕之日，若为小金库产品，则为借款金额的0.7%（最少5元））</td></tr><tr><td colspan='2'><strong>提前还款手续费</strong>：200 元</td>");
//		sb.append("</tr><tr><td rowspan='3' style='text-align: center'><strong>借款人还款账户</strong></td><td colspan='2'>户名：徐程</td></tr><tr><td colspan='2'>证件类型及号码: 身份证 320682198803300479 </td>");
//		sb.append("</tr><tr><td>开户行：交通银行</td><td>账号：6222600210018531783</td></tr><tr><td colspan='3'><strong>附件</strong>：</td></tr><tr><td colspan='3'><p style='line-height:1.5em'><strong>特别约定</strong>：");
//		sb.append("<br /><br />1、借款人采取以下 <u>&nbsp;<strong>A</strong>&nbsp;</u> 种方式支付当期借款本金、利息、逾期滞纳金（如有）、提前还款手续费（如有）（下称“当期应付款项”）：<br />");
//		sb.append("A. 借款人应在每月还款日当日（不得迟于18：00，如还款日为法定假日或公休日，还款日不顺延）或之前将当期应付款项存入上述还款账户，并同意第三方支付机构每月从上述还款账户中准确划扣当期应付款项给甲方 。甲方的银行账户信息如下：<br />");
//		sb.append("户名：侯翔宇<br />开户行：浦东发展银行上海嘉兴大厦支行<br />账号：6217920107193596 <br />");
//		sb.append("B. 由服务商秦苍科技在每月还款日当日（不得迟于18：00，如还款日为法定假日或公休日，还款日不顺延）或之前将当期应付款项直接支付给甲方，借款人应将当期应付款项在每月还款日当日支付至服务商秦苍科技的以下银行账户：<br />");
//		sb.append("户名： 上海秦苍信息科技有限公司 <br />开户行：招商银行上海联洋支行 <br />账号：121913627610301 <br /><br />2、秦苍科技协助贷款人与借款人签署相关法律文件；<br />");
//		sb.append("3、秦苍科技协助贷款人办理本笔借款在存续期间的贷后管理及资产保全；<br />4、借款人、贷款人、服务商秦苍科技三方在此不可撤销地同意本次借款的所有操作流程（包括但不限于借款、还款等）均通过秦苍科技提供的手机应用、微信服务号或者网站服务平台来完成；<br />");
//		sb.append("5、客户服务费的支付采取以下第 <u>&nbsp;<strong>A</strong>&nbsp;</u> 种方式：<br />A. 乙方直接向丙方支付客户服务费；<br />B. 丙方授权甲方向乙方代为收取客户服务费，然后一并付给丙方。因此，乙方应将“客户服务费”和当期应付款项一起支付到甲方账户而无需另外向丙方支付。<br />");
//		sb.append("</p></td></tr><tr><td colspan='3'>贷款人(签署)： 侯翔宇   <br />借款人(签署)：徐程   <br />服务方(盖章)：<br />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src='./agreementTemplate/stamp.gif' />");
//		sb.append("</td></tr></table><div style='text-align: center'>☆ 借款人同意贷款人在借款合同上加盖骑缝章以确认具体条款和基本条款等。</div><br /><br /><div id='secondpart-title'>第二部分 借款合同基本条款</div>");
//		sb.append("<div><p style='line-height: 1.5em;'>本合同由以下各方于【2015年11月25日】在【广西北路230号百米香榭105】签署； <br /><strong>甲方(贷款人)</strong>：侯翔宇（身份证号）：310104198706266813 <br />");
//		sb.append("住所：上海市浦东新区峨山路91弄120号1幢102-D室 <br /><strong>乙方(借款人)</strong>：徐程 <br />身份证号：320682198803300479 <br />住所：  <br />	<br /><strong>丙方(客户服务供应商)</strong>：上海秦苍信息科技有限公司<br />");
//		sb.append("住所：上海市浦东新区峨山路91弄120号1幢102-D室<br />甲方、乙方和丙方在本合同下文分别称为“一方”，合称“各方”。<br /><br /><strong>鉴于</strong>：<br />");
//		sb.append("1.	甲方同意按本合同的约定向乙方提供贷款，乙方有借款需求，丙方拥有手机应用、微信服务号或者网站服务平台为乙方与甲方的借贷提供服务；<br />");
//		sb.append("2.	丙方为乙方提供借款信息咨询，并在乙方借款过程中协助其办理各项手续及为乙方提供推荐贷款人、促成交易，以及还款提醒管理等服务，并有权获得客户服务费； <br />");
//		sb.append("3.	甲、乙、丙三方在此不可撤销地同意本次交易的所有操作流程（包括但不限于借款、还款等）均可通过丙方提供的手机应用、微信服务号或者网站服务平台来完成； <br />");
//		sb.append("&nbsp;&nbsp;甲、乙、丙三方因上述借款事宜，经友好协商，乙方同意接受甲方和丙方的服务并与甲方和丙方达成如下一致意见： <br /><br /><br />");
//		sb.append("<strong>第一条 基本借贷条款与定义贷款</strong><br />1.1	借款本金：以本合同第一部分《借款合同具体信息表》（以下简称“信息表”）所列“借款本金”为准；<br />");
//		sb.append("1.2	借款用途：本合同项下的借款用途仅为信息表所列明的用途，乙方不得将所借款项用于借款用途以外的目的。若借款用途为购买消费类电子产品，乙方在收到甲方发放的贷款后，应立即自行使用刷卡或者现金的方式支付给商户，用以支付其购买商品的货款； <br />");
//		sb.append("1.3	还款到期日：第一期期款的到期日以信息表所列“首次还款日”为准；其后的每一期期款（如有）以信息表所列“每月还款日”为准，为每一日历月的同一天（前两者统称“还款到期日”）；<br />");
//		sb.append("1.4	还款方式：本借款应以【A】还款的方式偿还。全额偿还本借款（包括借款人履行本合同项下其他的支付义务，逾期滞纳金及提前还款手续费除外）所需的分期期数以及每期应支付的期款以信息表所列“分期期数”和“每月还款额”为准；<br />");
//		sb.append("		1.5	借款期限：本合同项下的借款期限自从签署本合同之日开始至最后一笔期款的到期日为止。若借款人在借款期限结束后，仍有拖欠本合同项下的任何款项的情形，其还款义务并不因此而解除，应继续偿还未付款项；<br />");
//		sb.append("		1.6	借款形式：甲方或甲方委托的第三方将借款存入乙方账户内，则甲方在本合同项下发放贷款的行为已完成。如乙方的相关交易账户信息泄露、银行卡丢失等任何乙方原因导致第三方使用甲方向乙方提供的借款的，甲方、丙方不承担任何法律责任，乙方仍需承担相应的还款义务；<br />");
//		sb.append("		1.7	乙方若变更账户信息，需将变更前后的账户信息书面通知甲方、丙方。因乙方预留的银行账户相关信息发生变更而未书面通知甲方、丙方的，或乙方指定银行账户的相关信息发生错误导致乙方未收到借款或者银行划扣不成功的，甲方、丙方不承担任何法律责任，乙方仍需承担相应的还款义务（包括可能产生的逾期还款责任）。<br />");
//		sb.append("		<strong>第二条 客户服务</strong><br />");
//		sb.append("		2.1 就本合同，丙方向乙方提供以下服务：（a）客户咨询服务；（b）个人信息以及联系/通讯信息更改管理；（c）客户文档保管服务；（d）客户文档调阅服务；（e）客户合同付清证明文件服务；（f）客户还款渠道维护服务；（g）客户还款信息查询服务；（h）客户还款提醒及到账通知服务；（i）错误还款后续跟进，以及（j）溢缴款处理；（k）逾期提醒及逾期滞纳金提醒；(l)提前还款处理；<br />");
//		sb.append("		2.2 就上述服务，丙方向乙方收取相关的费用（即“客户服务费”），若为分期还款的情形，该笔费用分别在借款期间的【前三期】还款日收取；若为一次性还款的情形，该笔费用包含在还款金额中，并在借款到期日一次性收取。<br />");
//		sb.append("		<strong>第三条 贷款、费用的偿还与支付</strong><br />");
//		sb.append("		3.1	借款人支付当期应付款项、客户服务费的方式以信息表所约定的方式为准；<br />");
//		sb.append("		3.2	借款人在此不可撤销的授权甲方及丙方从信息表上列明的借款人银行账户划扣当期应付款项、客户服务费等本合同项下应支付的款项，而无需进一步通知借款人或者得到其同意。银行代扣将不早于还款到期日；<br />");
//		sb.append("		3.3	如果任何本合同项下的银行代扣失败，无论何种原因所致，借款人在本合同项下的还款义务不得因此而减免，如有必要借款人应采取其它合理方式继续清偿债务；<br />");
//		sb.append("		3.4	还款日期：以甲方账户实际收到乙方支付款项的到账时间为准；<br />");
//		sb.append("		3.5	如无法采用第3.2、3.3条约定的银行代扣方式还款的，付款方应于每月还款日18:00之前将当期应付款项直接打入收款方账户。甲方、丙方的收款账户以信息表中所列的为准；<br />");
//		sb.append("		3.6	本合同第三条之任何规定不可被视为免除乙方对本合同项下任何款项的偿还义务，如滞纳金；<br />");
//		sb.append("		3.7	到达收款方账户的款项，按照如下顺序清偿各项债务（包括乙方签订了其他由丙方作为客户服务供应商的借款合同的情形）：先到期的债务先偿还；同时到期的债务中，滞纳金先于期款偿还；多笔借款的期款同时到期，先签署的借款合同先偿还；同时到期的多笔借款中，乙方申请了提前还款的借款合同先偿还；<br />");
//		sb.append("		3.8	乙方应妥善保管本合同项下已还款的付款证明。因乙方的还款引起争议的，由乙方对其还款情况承担举证责任。<br /><strong>第四条 逾期还款</strong><br />");
//		sb.append("		4.1	每月还款到期日的18:00时前，若乙方未足额支付当期应付款项或者客户服务费的，则视为逾期；<br />");
//		sb.append("		4.2	乙方逾期的，应当承担逾期滞纳金。逾期滞纳金按逾期天数计算，乙方每逾期付款一天，则应支付【5.00】元的逾期滞纳金直至清偿完毕之日（若为小金库产品，则为借款金额的0.7%（最少5元））；乙方若使用促销产品(【007，利率，服务费率】或【007+，利率，服务费率)】，逾期滞纳金按逾期天数计算，乙方每逾期付款一天，则应支付【10元】/天的逾期滞纳金直至清偿完毕之日；<br />");
//		sb.append("		4.3	若乙方支付的款项按照本合同第3.7条的偿还方式偿还后，还有尚未支付的任何一小笔款项（包括前几期应付款项、当期应付款项等一切应支付的费用），那么乙方仍属于逾期还款，继续计算逾期天数，按照逾期天数支付滞纳金；<br />");
//		sb.append("		4.4	乙方可以从丙方处调阅每一期的还款、尚未到期的各期还款、以及应支付的逾期滞纳金；<br />");
//		sb.append("		4.5	当乙方存在逾期款项时，甲方有权自己或授权丙方或丙方的合法受托人，以包括电话、手机短信、电子邮件或其他合法方式提醒并催告乙方履行还款义务。<br />");
//		sb.append("		<strong>第五条 提前还款</strong><br />");
//		sb.append("		5.1	<strong><i>甲方或丙方有权根据乙方参加的金融消费产品决定乙方是否享有提前还款的权利（具体以在丙方平台发布的相关通知或介绍为准）</i></strong>，如乙方有权提前偿还本合同项下的贷款并终止本合同的，乙方应根据本条的约定一次性支付全部剩余本金、尚未支付的利息、尚未支付的客户服务费、逾期滞纳金（如有）和提前还款手续费（上述各类款项统称为“提前还款应付款项”），但乙方不得提前偿还部分应付款项。如乙方参加的金融消费产品未设置提前还款安排的，乙方不得向甲方或丙方进行提前还款； <br />");
//		sb.append("		5.2	尚未支付的利息指：提前还款日往期所有未付利息的总额，但不包括提前还款日当期至最终到期日期间的应付利息；<br />");
//		sb.append("		5.3	尚未支付的客户服务费指：提前还款日往期所有未付客户服务费的总额，但不包括提前还款日当期至最终到期日期间的应付客户服务费；<br />");
//		sb.append("		5.4	无论乙方在任何时间（犹豫期后）提出提前还款，都应支付提前还款手续费计人民币【200】元。但本合同另有约定的除外；<br />");
//		sb.append("		5.5	如��方使用促销金融产品【（），007+，利率，服务费率】，乙方在第一期提前还款之日前（犹豫期后）发起提前还款，则应支付提前还款手续费计人民币【200】元；如乙方在其余任何时间发起提前还款，则甲方会在5个工作日内退还5.4所描述之200元提前还款手续费至乙方所留银行卡账户。则甲方会在5个工作日内退还5.4所描述之200元手续费至乙方所留银行卡账户；<br />");
//		sb.append("		5.6	乙方提前还款，应在提前还款月份的还款到期日的【五】日之前通过电话或者微信客服号联系丙方申请提前还款并授权其向借款人办理提前还款手续。丙方的客服人员将告知借款人具体的提前还款金额；<br />");
//		sb.append("		5.7	乙方提前还款，其还款方式与第三条约定的偿还期款方式相同，但提前还款时，银行代扣可能在还款到期日之前发生；<br />");
//		sb.append("		5.8	若乙方未一次性足额支付提前还款应付款项，任何提前还款都将自动取消；<br />");
//		sb.append("		5.9	<strong>犹豫期：甲方和丙方将根据实际情况决定是否给予乙方犹豫期，具体详见丙方平台相关介绍。如甲方和丙方给予乙方本次借款犹豫期的，借款人在其签署《借款合同》（本合同）当天23:59之前向丙方申请提前还款，并在当天23:59之前将贷款本金全额支付至指定还款账户，则甲方和乙方不收取利息、客户服务费和提前还款手续费。</strong><br />");
//		sb.append("		<strong>第六条 借款债权的转让</strong><br />");
//		sb.append("		6.1   乙方知晓并同意，若乙方在还款分期内存在还款逾期或其他违约行为，甲方自动将本合同项下全部或部分债权（包括但不限于剩余债权本金、利息、逾期滞纳金、服务费、实现债权的费用等）一并转让予丙方，甲方无须就债权转让再次通知乙方；<br />");
//		sb.append("		6.2	甲方根据本合同转让借款债权的，除下述内容变更外，本合同项下其他条款不受影响，且变更内容对乙方、丙方仍有约束力：<br />&nbsp;&nbsp;6.2.1借款债权全部转让的，本合同项下的提供借款的一方变更为借款债权受让人；<br />");
//		sb.append("		&nbsp;&nbsp;6.2.2借款债权部分转让的，本合同项下的提供借款的一方变更为甲方和债权受让人，且甲方和债权受让人借出的本金应相应调整。<br />");
//		sb.append("		<strong>第七条 陈述与保证</strong><br />7.1	乙方向甲方、丙方陈述并保证：乙方为借款目的提供的所有信息（包括商品信息）完整、真实、准确并不存在误导性。不存在任何可能影响乙方信用的情况，如涉及乙方的诉讼、仲裁、行政程序等，无论以任何方式，也无论是否正在进行或者有潜在可能性；本借款用途仅限于购买商品。因以上任何陈述不真实、不准确而导致的甲方或丙方的任何损失，均应由乙方足额赔偿；<br />");
//		sb.append("		7.2	乙方应积极配合甲方、丙方对乙方的信用、借款使用情况、借款偿还情况进行监督；<br />7.3	乙方充分理解，其所支付给丙方的客户服务费并非借款利息，而是其接受丙方服务而应支付的合理报酬；<br />");
//		sb.append("		7.4	<strong>乙方授权甲方、丙方为信用评估、数据处理、风险控制、逾期账款催收等任何目的从任何数据库（包括中国人民银行个人信用信息基础数据库以及社会保障机构网站）查询乙方的个人信息或者向上述系统报送其个人信息以及为广告营销等任何目的通过任何方式（包括但不限于通过邮寄、电子邮箱、固定电话、手机等）使用其个人信息联系乙方或者授权第三方联系乙方；</strong><br />");
//		sb.append("		7.5	如果乙方违约，乙方不可撤销地同意甲方、丙方（或者其授权的第三方）直接或者经由可能认识的乙方的第三方，就该违约事件通过当面拜访、电话、邮寄、网络等合法形式提醒乙方或者督促乙方对违约行为进行改正，并且同意甲方、丙方向该第三方披露此违约事件；<br />");
//		sb.append("		7.6	若本合同约定的贷款用途为消费，乙方承认，本合同产生的法律关系完全独立于乙方与商家之间的买卖合同关系。该买卖合同关系的无效或变更并不影响本合同的法律效力。故乙方不得以与商家之间的任何纠纷（包括涉及商品质量或售后服务）为由拒绝履行本合同项下的任何义务。如因商品质量问题或任何其他原因导致乙方向商户退货或换货，乙方已支付的客户服务费不作退还，且乙方还应按照本合同的约定向甲方承担还款义务；<br />");
//		sb.append("		7.7	信息表上的乙方信息发生任何变化，乙方个人资产或者财物状况发生重大变化、或者发生了可能影响乙方履行本合同项下义务的任何其他情况时，乙方均应在五日内通知甲方、丙方；<br />");
//		sb.append("		7.8	如乙方使用产品（【007，利率，服务费率】或产品【007+利率，服务费率），】，如乙方发生死亡或达到二级及以上伤残的，乙方或乙方之直系亲属需提供甲方及丙方认可的鉴定机构之书面报告（包含但不限于伤残鉴定报告、病历等），甲方及丙方同意免除乙方截至书面材料提供时所负甲方应还而未还的全部债务。若乙方使用其他产品不享受该服务；<br />");
//		sb.append("		7.9	甲、乙、丙各方确认并同意，甲方和丙方对本合同项下任何事项的任何证明应作为本合同项下有关事项的终局证明。<br />");
//		sb.append("		<strong>第八条 提前到期</strong><br />");
//		sb.append("		8.1	以下任何事件发生，甲方可要求乙方立即一次性偿还本合同项下的全部款项（具体金额按照本合同第五条的有关规定计算）：（一）乙方违约；（二）乙方在其与甲方、或者其他借款方签署的任何其他借款合同项下发生重大违约；（三）甲方和丙方强烈怀疑乙方自借款申请日起就借款、客户服务费从事过任何欺诈行为或乙方可能无能力根据本合同付款；以及（四）若按照甲方和丙方的合理判断，乙方发生可能对甲方或丙方的权利或利益造成负面影响的任何其他情形；<br />");
//		sb.append("		8.2	如果第8.1条的情形发生在甲方发放借款之前，甲方和丙方可以解除本合同，并且无需发放借款或者承担任何其他责任；<br />");
//		sb.append("		8.3	若本合同约定的还款为分期还款，乙方在某一笔期款的还款到期日60日之后仍未完全偿还该笔期款，则甲方、丙方有权将本合同提前终止，乙方应立即按照本合同的约定一次性支付全部剩余本金、尚未支付的利息、尚未支付的客户服务费、逾期滞纳金（如有）和提前还款手续费。若乙方尚有其他未还清的借款，且该笔借款合同的信息服务供应商为丙方，则其他借款合同的提前终止也将导致本合同的提前终止。<br />");
//		sb.append("		<strong>第九条 违约</strong><br />9.1	发生下列任何一项或几项情形的，视为乙方违约：<br />&nbsp;&nbsp;9.1.1乙方违反其在本合同项下的任何义务的、承诺或保证的；<br />");
//		sb.append("		&nbsp;&nbsp;9.1.2乙方擅自改变本合同约定的借款用途的；<br />&nbsp;&nbsp;9.1.3乙方提供虚假资料或故意隐瞒重要事实，影响甲方和丙方审核乙方信用和还款能力的；<br />");
//		sb.append("		&nbsp;&nbsp;9.1.4乙方的任何财产遭受没收、征用、查封、扣押、冻结等可能影响其履约能力的不利事件，且不能及时提供有效不救措施的；<br />");
//		sb.append("		&nbsp;&nbsp;9.1.5乙方的财务状况出现影响其履约能力的不利变化，且不能及时提供有效不救措施的。<br />");
//		sb.append("		9.2	若乙方违约，应赔偿甲方和/或丙方因此遭受的损失，包括由此产生的律师费、调查费、催收费等其他费用；并且，在乙方违约的情况下，乙方已经支付给甲方、丙方的任何服务费用均不予退还。<br />");
//		sb.append("		<srong>第十条 保密条款</srong><br />");
//		sb.append("		10.1 本合同签署后，除非另有约定或事先得到各方的���面同意，本合同各方均应承担以下保密义务：<br />");
//		sb.append("		&nbsp;&nbsp;10.1.1任何一方不得向非本合同方（丙方及其委托的第三方服务商/合作方除外）披露本合同以及本合同项下的事宜以及与此等事宜有关的任何文件、资料或信息（“保密信息”）；<br />");
//		sb.append("		&nbsp;&nbsp;10.1.2任何一方只能将保密信息和其内容用于本合同项下的目的，不得用于任何其他目的。本款的约定不适用于下列保密信息；<br />");
//		sb.append("		&nbsp;&nbsp;A．从披露方获得时，已是公开的；<br />&nbsp;&nbsp;B．从披露方获得前，接受方已经获知的；<br />&nbsp;&nbsp;C．从有正当权限并不受保密义务制约的第三方获得的；<br />");
//		sb.append("		&nbsp;&nbsp;D．非依靠披露方披露或提供的信息独自开放的。<br />10.2 因下列原因披露保密信息，不受前款的限制： <br />&nbsp;&nbsp;10.2.1向本合同各方聘请的会计师、律师、咨询公司披露；<br />");
//		sb.append("		&nbsp;&nbsp;10.2.2因遵循可适用的法律、法规的强制性规定而披露； <br />&nbsp;&nbsp;10.2.3依据其他应遵守的法规向审批机构和/权利机构进行的披露；<br />&nbsp;&nbsp;10.2.4甲方拟将其在本合同项下的债权进行转让的，丙方向债权转让所涉相关当事人（包括但不限于网络借款平台、债权受让方等）进行的披露。<br />");
//		sb.append("		10.3 本合同任何一方应采取所有其他必要、适当和可以采取的措施，以确保保密信息的保密性；<br />10.4 本合同各方应促使其向之披露保密信息的人严格遵守本条约定；<br />10.5 各方在本条项下的权利和义务应在本合同终止或到期后继续有效。<br />");
//		sb.append("		<strong>第十一条 通知</strong><br />");
//		sb.append("		11.1 本合同任何一方根据本合同约定做出的通知和/或文件均应以书面形式（包括但不限于电子邮件、手机短信、分期付款信息公告等方式）作出，各方根据本合同做出的所有通知均为合法有效的通知；<br />");
//		sb.append("		11.2 本合同各方之间的书面通知或文件往来，必须发送至各方的联系人。乙方的联系信息具体见乙方的注册信息；<br />");
//		sb.append("		11.3 若本合同任何一方更改其联系人或联系地址或电子邮件邮箱地址，应尽快按本条约定在相关信息变更之日起3个工作日内书面通知其他一方。变更通知自通知到达被通知方之日起生效，因一方怠于通知产生的所有后果由该方承担；<br />");
//		sb.append("		11.4 乙方同意按上述11.2、11.3条款约定提供的地址作为相关通知和法律文书的送达地址，甲方、丙方或有关人民法院按该地址送达相关通知或法律文书的，即视为有效送达。<br />");
//		sb.append("		<strong>第十二条 争议解决</strong><br />12.1 本合同适用中华人民共和国（仅为本合同之目的，不包括香港特别行政区、澳门特别行政区和台湾地区）法律；<br />");
//		sb.append("		12.2 凡因本合同引起的或与本合同有关的任何争议，应通过协商解决，若协商仍无法解决，任何一方可以向上海市浦东新区人民法院提起诉讼。败诉方应承担为解决本争议而产生的所有费用，包括但不限于诉讼费、律师费、公证费、交通费等；<br />");
//		sb.append("		12.3 若争议正在解决过程之中，合同各方应继续履行其在本合同项下的所有义务。<br />");
//		sb.append("		<strong>第十三条 其他约定</strong><br />13.1 本合同自甲乙丙三方签署时成立，自甲方将出借款项委托丙方支付到乙方指定收款账户时生效；<br />");
//		sb.append("		13.2 本合同的任何修改、补充均须以书面形式做出，补充条款与本合同具有同等的法律效力；<br />13.3 本合同的电子件、传真件、复印件和扫描件等有效复本的效力与本合同原件具有同等法律效力；<br />");
//		sb.append("		13.4 本合同部分条款因违反法律或行政法规的强制性规定而被视为无效时不影响本合同其他条款的效力；<br />13.5 本合同一式叁份，甲、乙、丙三方各执壹份；<br />");
//		sb.append("		13.6 乙方明确了解并同意，每期应支付的期款是以下（1）至（4）项之和，期款以分为最小单位：（1）每月借款本金和利息（每月偿还的利息为固定利息）以按月【等额本息】的方式偿还；（2）每月客户服务费；（3）逾期滞纳金（如有）；（4）提前还款手续费（如有）。乙方因签订本合同所应承担的印花税由甲方或丙方代扣代缴。印花税包含在第一期期款内。<br />");
//		sb.append("</p><br /><p>&nbsp;&nbsp;本合同附件<br /><br />	&nbsp;&nbsp;附件1：《还款事项提醒函》<br /><br /><br />	&nbsp;&nbsp;甲方：侯翔宇<br /><br />&nbsp;&nbsp;乙方(借款人)：徐程 <br />");
//		sb.append("&nbsp;&nbsp;320682198803300479 <br /><br />&nbsp;&nbsp;丙方：上海秦苍信息科技有限公司 <br /><br />&nbsp;&nbsp;<img src='./agreementTemplate/stamp.gif' /></p></div><br /><br />");
//		sb.append("<div>附件1：</div><div id='agreement-title'>还款事项提醒函</div><div style='float: right; padding-right: 20%'>编号：B6854100C477E511A974AC853D9F545E </div><div><p>先生/女士您好：<br />");
//		sb.append("&nbsp;&nbsp;为了您更加方便、快捷地进行还款，同时累积良好的信用记录，在此，请您特别关注您的账户状态、还款计划以及如下事项：<br /></p><div>1. 还款计划一览</div><br /><table class='license-table' border='1' align='center'>");
//		sb.append("<tr><th>期数</th><th>到期还款日</th><th>还款状态</th></tr><tr><td>1</td><td>2015年10月28日</td><td>本息服务费共【1103.00】元</td></tr></table><br /><p style='line-height:1.5em'>");
//		sb.append("			2. <strong>还款账户</strong>：在《借款合同》里列示的专用账号是您每次用于还款的账户，请您妥善保管好该银行卡。如发生此银行卡丢失、损坏的情况，请您第一时间联系我们');。我们将根据具体情况，协助您安排按时还款事宜，以避免不必要的逾期违约金的产生。如您需要变更还款账户，请向我们提出申请（还款日当天不受理），我们的工作人员将协助您，进行变更贷款合同中专用账号的事项办理。同时，为便于您明确每笔还款不与其他进出账混淆，我们建议（但不要求）您单独开立银行账户，专门用于此还款目的；<br />");
//		sb.append("			3. <strong>还款账户最低余额为15元</strong>：为了保证您的还款成功划扣，请确保您的还款账户余额在划扣当月还款额以后，还有至少15元的余额存在您的还款账户里，此金额有可能用于支付还款银行卡的“年费”和其他相关费用。关于此“年费”和其他费用收费标准，请致电您的还款银行卡所属银行咨询。同时为了保证扣款日能够成功划扣，请您保持还款账户中有充足的资金，避免因其他未知或遗忘的代扣等业务造成违约；<br />");
//		sb.append("			4. <strong>还款金额</strong>：您的【前三期月】还款额为人民币<u>&nbsp;（大写）壹仟壹佰零叁圆&nbsp;</u>(￥)，【剩余期数月】还款额为人民币<u>（大写）</u><strong>请您务必牢记此数字</strong>。（其中服务费<u>（大写）叁拾贰圆</u>(￥)）；<br />");
//		sb.append("			5. <strong>还款时间</strong>：请您在【28】日18点之前，将每月还款额<strong>足额</strong>存到您的还款银行卡里。逾期将产生逾期违约金，并会影响您的个人信用记录。<strong>请您务必牢记此还款日</strong>，此还款日期不因节假日顺延；<br />");
//		sb.append("			6. <strong>若您采用跨行转账方式汇入还款账户，请提前一周进行划转</strong>。我们将以成功划扣还款账户的还款金额时间为准，记录还款时间；<br />");
//		sb.append("			7. 如果您因特殊原因暂时无法按时足额还款时，请在第一时间（<strong>当月还款日期之前</strong>）与我们联系。（电话：400 655 1250）。我们将协助您安排预约还款并准确告知您还款金额（本息、逾期违约金、合同约定的其他费用等）。<strong>特别需要提请您注意的是：针对违约时长超过60日，根据《借款合同》，出借人、我方有权提前终止《借款合同》，您须在出借人、我方提出终止《借款合同》要求的三日内一次性支付余下的所有本金、利息、客户服务费和逾期滞纳金</strong>；");
//		sb.append("			8. <strong>联系方式变更</strong>：为了方便我们更好地服务于您，如果您需要变更还款账号，或者您（或是您共同借款人）的联系方式、工作单位、居住或工作地址等与借款有关的信息发生变化，请在第一时间拨打【400 655 1250】通知我们。注：在还款日当天不予变更还款账号；<br />");
//		sb.append("			9. <strong>提前还款（即一次性还款）</strong>：为了方便我们更好地服务于您，如果您决定一次性将全部款项结清，请您务必及时我们进行书面确认（还款日及节假日期间不办理）。我们将帮助您安排预约一次性还款的相关事宜。无论您在任何时间提出提前还款，都应支付提前还款手续费计人民币【200】元；<br />");
//		sb.append("			10.	提前还款电话：【400 655 1250】。<br />");
//		sb.append("			<br /><br /><br />上海秦苍信息科技有限公司 <br /><br />以上内容由上海秦苍信息科技有限公司工作人员进行了清楚讲解，对于上述内容我已没有疑问。 <br /></p>");
//		sb.append("		<div align='right' style='padding-right:20px;'>借款人签字：徐程</div><br /><div align='right' style='padding-right:20px;'>日期： 2015年11月25日</div>");
//		sb.append("	</div><div>附件2：</div><div id='agreement-title'>委托扣款授权书</div><div><p style='line-height:1.5em'>");
//		sb.append("		&nbsp;&nbsp;乙方本人同意授权上海秦苍信息科技有限公司（以下简称“秦苍科技”） 委托重庆易行通科技有限公司选定的第三方支付机构——重庆易极付科技有限公司（下称“易极付”）每月从本人提供的以上账户中自动扣划当月的商品费用。本人同意如未及时还款导致逾期将按照合同规定承担违约责任并支付相关费用。本人同意该账户同时可用于因提前还款等引起的资金往来。甲方本人同意以下条款：<br />");
//		sb.append("		① 本人自愿申请且授权易极付对本人的银行账户进行代扣还款操作；<br />② 因本人银行账户余额不足导致无法按期还款所产生的全部法律责任由本人自行承担；<br />");
//		sb.append("		③ 因本人提供信息不真实或不完整所造成的损失由本人自行承担；<br />④ 本人利用易极付公司服务从事违法活动或不正当交易产生的一切后果与责任，由本人独立承担；<br />");
//		sb.append("		⑤ 本人应妥善保管账户信息，因本人故意或过失致使本人的信息泄漏产生的任何损失，均由本人自行承担；<br />");
//		sb.append("		⑥ 易极付公司发现本人的注册资料虚假、异常交易或有疑义或有违法嫌疑时，有权不经通知先行中断或终止本人使用易极付公司部分或全部服务功能。<br />");
//		sb.append("		</p><br /><br /><p style='text-aligan: right; padding-right: 20px;'>授权人（正楷，必填）：<u>&nbsp; 徐程 &nbsp;</u><br />身份证号码（必填）：<u>&nbsp; 320682198803300479 &nbsp;</u><br />");
//		sb.append("		授权日期（必填）： 2015年11月25日 <br /></p></div></div></body></html>");
		
		// 头像信息
		//由于pdfHtml不能将用户头像照片以及身份证照片信息传递过来，因此在后台对其重新插入
//		String idImgStr =  getImgBase64String(appId, AttachmentType.UserIdPhoto);
//		String headImgStr =  getImgBase64String(appId, AttachmentType.UserHeadPhoto);
//		if(idImgStr != null) {
//			pdfHtml = pdfHtml.replace("%IdPhoto%", "data:image/jpg;base64, "+idImgStr);
//		} else {
//			pdfHtml = pdfHtml.replace("%IdPhoto%", "");
//		}
//		if(headImgStr != null) {
//			pdfHtml = pdfHtml.replace("%HeadPhoto%", "data:image/jpg;base64, "+headImgStr);
//		} else {
//			pdfHtml = pdfHtml.replace("%HeadPhoto%", "");
//		}
		
		return generator.pdfGenerateAndSaveForNewVersion(model);
//		return generator.pdfGenerateAndSaveForNewVersion(sb.toString());
//		return null;
	}
}
