package com.hulkhiretech.payments.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hulkhiretech.payments.constant.ErrorCodeEnum;
import com.hulkhiretech.payments.pojo.ErrorRes;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	//StripeProviderException (when exception is thrown from StripeProvider this method will be called)
	  @ExceptionHandler(StripeProviderException.class)
	    public ResponseEntity<ErrorRes> handleStripeProviderException(StripeProviderException ex) {
		  log.error("StripeProviderException: ", ex);
		  
		  ErrorRes errorRes = new ErrorRes();
		  errorRes.setErrorCode(ex.getErrorCode());
		  errorRes.setErrorMessage(ex.getErrorMessage());
		  
		  
//	       LinkedHashMap<String, Object> errorResponse = new LinkedHashMap<String, Object>();
//	      
//	       errorResponse.put("errorCode", ex.getErrorCode());
//	       errorResponse.put("errorMessage", ex.getErrorMessage());
	      
	       log.info("Returning  Response: {}", errorRes);

	        return new ResponseEntity<>(errorRes,ex.getHttpStatus()); 
	    }

	  
	  
	  //Generic Exception  when exception is thrown from any other class 
	  //like General Exception 10/0 this method will be called
	  @ExceptionHandler(Exception.class)
	    public ResponseEntity<ErrorRes> handleGenericException(StripeProviderException ex) {
		  log.error("handleGenericException: ", ex);
		  
		  ErrorRes errorRes = new ErrorRes();
		  errorRes.setErrorCode(ErrorCodeEnum.GENERIC_ERROR.getErrorCode());
		  errorRes.setErrorMessage(ErrorCodeEnum.GENERIC_ERROR.getErrorMessage());
		  
		  
//	       LinkedHashMap<String, Object> errorResponse = new LinkedHashMap<String, Object>();
//	      
//	       errorResponse.put("errorCode", ex.getErrorCode());
//	       errorResponse.put("errorMessage", ex.getErrorMessage());
	      
	       log.info("Returning  Response: {}", errorRes);

	        return new ResponseEntity<>(errorRes,HttpStatus.INTERNAL_SERVER_ERROR); 
	    }
	}



