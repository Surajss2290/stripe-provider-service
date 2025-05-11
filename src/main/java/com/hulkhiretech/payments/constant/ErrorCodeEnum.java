package com.hulkhiretech.payments.constant;

import lombok.Getter;

@Getter
public enum ErrorCodeEnum {
		
	UNABLE_TO_CONNECT_TO_STRIPE_PSP("3000","Unable to connect to StripePSP"),
	STRIPE_PSP_ERROR("3001","StripePSP Error"),
	GENERIC_ERROR("3003","Unable process the request , please try again later");
	
	private String errorCode;
	private String errorMessage;
	
	ErrorCodeEnum(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

}
