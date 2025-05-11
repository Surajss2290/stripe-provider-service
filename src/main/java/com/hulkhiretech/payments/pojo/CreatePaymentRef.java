package com.hulkhiretech.payments.pojo;

import java.util.List;

import lombok.Data;

@Data
public class CreatePaymentRef {
	
	private String successUrl;
	
	private String cancleUrl;

	private List<LineItem> lineItems;
}
