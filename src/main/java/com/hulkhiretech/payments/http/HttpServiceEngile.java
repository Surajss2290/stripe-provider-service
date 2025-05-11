package com.hulkhiretech.payments.http;





import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.exception.StripeProviderException;

import lombok.extern.slf4j.Slf4j;

//This class Acts as RestClient as centeralized Communication like API GateWay to communicate to Stripe PSP

//HttpService Engine is Generic in nature

@Component
@Slf4j
public class HttpServiceEngile {
	
	
	private RestClient restclient;
	
	public HttpServiceEngile(RestClient.Builder restClientBuilder) {
		this.restclient= restClientBuilder.build();
	}
	
	
	
	
	
	
	//This methode is responsible to call to Stripe PSP called from ServiceImpl class with necessary data
	public ResponseEntity<String> makehttpCall(HttpRequest httpRequest) {
		log.info("Invoke http call "+ restclient);
		
		try { 
			ResponseEntity<String> response=restclient.method(httpRequest.getMethod())
					.uri(httpRequest.getUrl())
					
					.headers(headers-> headers.addAll(httpRequest.getHttpHeaders()))
					
					
					.body(httpRequest.getRequestBody())
					
					.retrieve()
					
					.toEntity(String.class);
					
					log.info("Response"+ response);
					
					return response;
			
		}catch (HttpClientErrorException | HttpServerErrorException e) {
			log.error("Httpclient error exception",e);
			
			if (e.getStatusCode().equals(HttpStatus.GATEWAY_TIMEOUT)
					|| e.getStatusCode().equals(HttpStatus.SERVICE_UNAVAILABLE)){
				log.error("received error from 5XX statuscode "+e.getStatusCode() ,e);
				throw new StripeProviderException(ErrorCodeEnum.UNABLE_TO_CONNECT_TO_STRIPE_PSP.getErrorCode(),
						ErrorCodeEnum.UNABLE_TO_CONNECT_TO_STRIPE_PSP.getErrorMessage(),
						HttpStatus.valueOf(e.getStatusCode().value()));
				
			}
			
			//valid error response handling from Stripe PSP
			//NOT passing currerency or other fileds belongs to this code
			
			log.info("Got getErrorResponseBodyAsString "+e.getResponseBodyAsString());
			return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
			
		} catch (Exception e) { 
			log.error("Error in http call",e);
			throw new StripeProviderException(ErrorCodeEnum.UNABLE_TO_CONNECT_TO_STRIPE_PSP.getErrorCode(),
											ErrorCodeEnum.UNABLE_TO_CONNECT_TO_STRIPE_PSP.getErrorMessage(),
											HttpStatus.INTERNAL_SERVER_ERROR);
			
			
			//500 INTERNAL_SERVER_ERROR
		}
		
	}
	
	

}
