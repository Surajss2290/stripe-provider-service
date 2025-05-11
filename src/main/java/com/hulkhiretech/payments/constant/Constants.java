package com.hulkhiretech.payments.constant;

public class Constants {
	
	private Constants() {
		
	}
	
	public static final String STRIPE_API_DYNAMIC_ID = "{id}";
	
	public static final String EMPTY_STRING = "";
	public static final String LINE_ITEM_QUANTITY = "line_items[%d][quantity]";
	public static final String LINE_ITEM_PRODUCT_NAME = "line_items[%d][price_data][product_data][name]";
	public static final String LINE_ITEM_CURRENCY = "line_items[%d][price_data][currency]";
	public static final String LINE_ITEM_UNIT_AMOUNT = "line_items[%d][price_data][unit_amount]";
	

	public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

	public static final String CANCEL_URL = "cancel_url";

	public static final String SUCCESS_URL = "success_url";

	public static final String MODE_PAYMENT = "payment";

	public static final String MODE = "mode";
	



}
