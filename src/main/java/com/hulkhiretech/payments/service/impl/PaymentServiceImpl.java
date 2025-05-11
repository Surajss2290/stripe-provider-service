package com.hulkhiretech.payments.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.google.gson.Gson;
import com.hulkhiretech.payments.constant.Constants;
import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.dto.CreatePaymentDTO;
import com.hulkhiretech.payments.dto.PaymentDTO;
import com.hulkhiretech.payments.dto.stripe.StripeError;
import com.hulkhiretech.payments.dto.stripe.StripeErrorWrapper;
import com.hulkhiretech.payments.exception.StripeProviderException;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngile;
import com.hulkhiretech.payments.pojo.LineItem;
import com.hulkhiretech.payments.service.interfaces.PaymentService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j

public class PaymentServiceImpl implements PaymentService{





	@Value("${stripe.apikey}")
	private  String api_key;

	@Value("${stripe.create-session.url}")
	String CreateSessionUrl;

	@Value("${stripe.get-session.url}")
	String getSessionUrl;
	
	@Value("${stripe.expire-session.url}")
	String expireSessionUrl;



	private HttpServiceEngile httpServiceEngine;

	private Gson gson;

	public PaymentServiceImpl(HttpServiceEngile httpServiceEngine,Gson gson) {

		this.httpServiceEngine=httpServiceEngine;
		this.gson=gson;
	}

	@Override
	public PaymentDTO createPayment(CreatePaymentDTO createPaymentDTO) {
		log.info("Invoked"+createPaymentDTO);

		/*
		//TESTING -Error Handling in Spring Boot
		if(createPaymentDTO.getSuccessUrl()==null) { //TODO: temp
			log.error("Throwing exception");
			throw new StripeProviderException("30001","This is test error message");
		}
		 */
		
	

		HttpHeaders httpHeaders=new HttpHeaders();
		//HARD-CODED
		httpHeaders.setBasicAuth(api_key,Constants.EMPTY_STRING);
		//httpHeaders.add("Content-Type", APPLICATION_X_WWW_FORM_URLENCODED);



		httpHeaders.add(httpHeaders.CONTENT_TYPE, Constants.APPLICATION_X_WWW_FORM_URLENCODED);



		MultiValueMap<String, String> requestBody=new LinkedMultiValueMap<>();

		requestBody.add(Constants.MODE, Constants.MODE_PAYMENT);

		requestBody.add(Constants.SUCCESS_URL, createPaymentDTO.getSuccessUrl());
		requestBody.add(Constants.CANCEL_URL, createPaymentDTO.getCancleUrl());

		for(int i=0;i<createPaymentDTO.getLineItems().size();i++) {

			LineItem lineItem = createPaymentDTO.getLineItems().get(i);

			/* HARD-CODED
				    requestBody.add("line_items[" + i + "][quantity]",String.valueOf(lineItem.getQuantity()));
				    requestBody.add("line_items[" + i + "][price_data][product_data][name]",lineItem.getProductName());
				    requestBody.add("line_items[" + i + "][price_data][currency]", lineItem.getCurrency());
				    requestBody.add("line_items[" + i + "][price_data][unit_amount]",String.valueOf(lineItem.getUnitAmount()));
			 */

			requestBody.add(String.format(Constants.LINE_ITEM_QUANTITY, i), String.valueOf(lineItem.getQuantity()));
			requestBody.add(String.format(Constants.LINE_ITEM_PRODUCT_NAME, i), lineItem.getProductName());
			requestBody.add(String.format(Constants.LINE_ITEM_CURRENCY, i), lineItem.getCurrency());
			requestBody.add(String.format(Constants.LINE_ITEM_UNIT_AMOUNT, i), String.valueOf(lineItem.getUnitAmount()));
		}


		//Ref Purpose
		//						requestBody.add("line_items[0][quantity]", "2");
		//						requestBody.add("line_items[0][price_data][product_data][name]", "MacBOOK");
		//						
		//						requestBody.add("line_items[0][price_data][currency]", "EUR");
		//						requestBody.add("line_items[0][price_data][unit_amount]", "1000");


		HttpRequest httpRequest=HttpRequest.builder()
				.method(HttpMethod.POST)
				.url(CreateSessionUrl)
				.httpHeaders(httpHeaders)
				.requestBody(requestBody)
				.build();


		//Stripe PSP createSession API call to HttpServiceEngine class methode
		ResponseEntity<String> response=httpServiceEngine.makehttpCall(httpRequest);
		log.info("Response from http serviceengine "+response);


		//When we Create session If you want necessary information instead of Entire JSON
		//It can covert entire json body to necessary information and send info to PaymentDTO #Caller
		PaymentDTO paymentDTO = processResponse(response);

	
		log.info("Returning PaymentDTO Json Body into paymentDTO "+paymentDTO);



		return paymentDTO;
	}




	//For Failure cases lOGIC 
	private PaymentDTO processResponse(ResponseEntity<String> response) {

		//handle the success
		if (response.getStatusCode().is2xxSuccessful()) {
			log.info("Success response received from Stripe" + response);
			//convert to success java object structure .If we get valid url, then retrun success object
			

			//When we Create session If you want necessary information instead of Entire JSON
			//It can covert entire json body to necessary information and send info to PaymentDTO #Caller
			PaymentDTO paymentDTO=gson.fromJson(response.getBody(),PaymentDTO.class);

			
			if (paymentDTO!=null && paymentDTO.getUrl()!=null) { // we got 2XX with valid URL
				log.info("Got Success response received from Stripe returning" + response);
				return paymentDTO;

			}
			log.error("Got Success response received from Stripe but no URL found" );
		}
		//handle the failure
		
		//Convert the error response to java object structure
		StripeErrorWrapper errorObj = gson.fromJson(response.getBody(),StripeErrorWrapper.class);
		log.error("Error response received from Stripe" + errorObj);
		
		if (errorObj!=null && errorObj.getError()!=null) {
			log.error("Error response received from Stripe" + errorObj.getError());
			
			//For every error from stripe we will return Stripe PSP ERROR CODe
			//and message will dynamic based what strips is returning
			//Pass the http status which we got from stripe
			throw new StripeProviderException(
					ErrorCodeEnum.STRIPE_PSP_ERROR.getErrorCode(),
					prepareErrorMessage(errorObj.getError()),
					HttpStatus.valueOf(response.getStatusCode().value()));
		}
		
		//This block will execute when we get a 2XX response but no URL found
		log.error("Raising generic error");
		 throw new StripeProviderException(
				ErrorCodeEnum.GENERIC_ERROR.getErrorCode(),
				ErrorCodeEnum.GENERIC_ERROR.getErrorCode(),
				HttpStatus.valueOf(response.getStatusCode().value()));



	}

//This method will be used to prepare the error message concate the error code and message
	private String prepareErrorMessage(StripeError error) {
		return error.getType()
		 + ":"+ error.getMessage() 
		 + ":"+ error.getParam()
		 + ":"+ error.getCode();
		
	}

	@Override
	public PaymentDTO getPayment(String id) {
		log.info("Invoked getpayemt"+id);

		HttpHeaders httpHeaders=new HttpHeaders();
		//HARD-CODED
		httpHeaders.setBasicAuth(api_key,Constants.EMPTY_STRING);
		//httpHeaders.add("Content-Type", APPLICATION_X_WWW_FORM_URLENCODED);
		httpHeaders.add(httpHeaders.CONTENT_TYPE, Constants.APPLICATION_X_WWW_FORM_URLENCODED);


		HttpRequest httpRequest=HttpRequest.builder()
				.method(HttpMethod.GET)
				.url(getSessionUrl.replace(Constants.STRIPE_API_DYNAMIC_ID,id))
				.httpHeaders(httpHeaders)
				.requestBody(Constants.EMPTY_STRING)
				.build();

		log.info("passing httprequest to get payment "+httpRequest);

		//Stripe PSP createSession API call to HttpServiceEngine class methode
		ResponseEntity<String> response=httpServiceEngine.makehttpCall(httpRequest);
		log.info("Response from http serviceengine "+response);

		//When we Create session If you want necessary information instead of Entire JSON
		//It can covert entire json body to necessary information and send info to PaymentDTO #Caller
		PaymentDTO paymentDTO=gson.fromJson(response.getBody(),PaymentDTO.class);
		log.info("Converted Json Body into paymentDTO "+paymentDTO);



		return paymentDTO;

	}



	@Override
	public PaymentDTO expirePayment(String id) {

		log.info("Invoked expirePayment"+id);


		HttpHeaders httpHeaders=new HttpHeaders();
		//HARD-CODED
		httpHeaders.setBasicAuth(api_key,Constants.EMPTY_STRING);
		//httpHeaders.add("Content-Type", APPLICATION_X_WWW_FORM_URLENCODED);
		httpHeaders.add(httpHeaders.CONTENT_TYPE, Constants.APPLICATION_X_WWW_FORM_URLENCODED);

		HttpRequest httpRequest=HttpRequest.builder()
				.method(HttpMethod.POST)
				.url(expireSessionUrl.replace(Constants.STRIPE_API_DYNAMIC_ID,id))
				.httpHeaders(httpHeaders)
				.requestBody(Constants.EMPTY_STRING)
				.build();

		//Stripe PSP createSession API call to HttpServiceEngine class methode
		ResponseEntity<String> response=httpServiceEngine.makehttpCall(httpRequest);
		log.info("Response from http serviceengine "+response);

		//When we Create session If you want necessary information instead of Entire JSON
		//It can covert entire json body to necessary information and send info to PaymentDTO #Caller
		PaymentDTO paymentDTO=gson.fromJson(response.getBody(),PaymentDTO.class);
		log.info("Converted Json Body into paymentDTO "+paymentDTO);

		return paymentDTO;
	}

}
