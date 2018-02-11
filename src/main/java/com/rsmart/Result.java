package com.rsmart;

import java.util.Map;

public class Result {
	public String reason;
	public Object detail;
	
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public Object getDetail() {
		return detail;
	}
	public void setDetail(Object detail) {
		this.detail = detail;
	}
	
	public static Result newReasonOnlyResult(String reason){
		Result result = new Result();
		result.setReason(reason);
		return result;
	}
	
	public static Result newTimeoutResult(){
		Result result = new Result();
		result.setReason("timeout");
		return result;
	}
	
	public static Result newResult(String reason, Object detail){
		Result result = new Result();
		result.setReason(reason);
		result.setDetail(detail);
		return result;
	}
	
	public static Result newOpResult(Map<String, String> mp){
		Result result = new Result();
		result.setReason("Failure");
		result.setDetail(mp);
		return result;
	}
	
	@Override
	public String toString() {
		return "Result [reason=" + reason + ", detail=" + detail + "]";
	}
	
	
}
