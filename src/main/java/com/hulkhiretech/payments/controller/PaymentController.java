package com.hulkhiretech.payments.controller;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hulkhiretech.payments.dto.CreatePaymentDTO;
import com.hulkhiretech.payments.dto.PaymentDTO;
import com.hulkhiretech.payments.pojo.CreatePaymentRef;
import com.hulkhiretech.payments.pojo.PaymentRes;
import com.hulkhiretech.payments.service.interfaces.PaymentService;

import lombok.extern.slf4j.Slf4j;
 

@RestController
@RequestMapping("/v1/payments")
@Slf4j
public class PaymentController {
	
	private PaymentService paymentService;
	
	
	private ModelMapper modelMapper;
	
	public PaymentController(PaymentService paymentService,ModelMapper modelMapper) {
		this.paymentService=paymentService;
		this.modelMapper=modelMapper;
	}

	
	
	
	@PostMapping     //THIS IS USED FOR CREATE SESSION
	public ResponseEntity<PaymentRes> createPayment(@RequestBody CreatePaymentRef createPaymentRef) {
		log.info("Invoke Create Payment "+createPaymentRef);
		
		//Its can covert the Pojo class to DTO 
		CreatePaymentDTO paymentDTO=modelMapper.map(createPaymentRef, CreatePaymentDTO.class);
		log.info("Converted POJO to DTO");
	
		//Here we can Call HTTP methode
		
		PaymentDTO response=paymentService.createPayment(paymentDTO);
		
		//RESPONSE
		//It can convert DTO class to Pojo class beacuse we cant send DTO directly as response to end user
		PaymentRes paymentRes=modelMapper.map(response, PaymentRes.class);
		log.info("Coverted  Response DTO to POJO "+paymentRes);
		
		
		log.info("Returning PaymentRes "+paymentRes);
	    return new ResponseEntity<>(paymentRes,HttpStatus.CREATED);
		
	
		
	}
	        @GetMapping("/{id}")
	       public ResponseEntity<PaymentRes> getPayment(@PathVariable("id") String id) {
	        	
	        	//to receive and convert it to dto and pass DTO to servive -NOT APPLICABLE
	        	//call the service 
	        	PaymentDTO payment = paymentService.getPayment(id);
	    
	        	
	        	//handle the response
	        	PaymentRes paymentRes=modelMapper.map(payment, PaymentRes.class);
	    		log.info("Coverted  Response DTO to POJO "+paymentRes);
	    		
	    		  return new ResponseEntity<>(paymentRes,HttpStatus.OK);
	       }
	        
	        @PostMapping("/{id}/expire")
	        public ResponseEntity<PaymentRes>  expirePayment(@PathVariable("id") String id) {

	        	//to receive and convert it to dto and pass DTO to servive -NOT APPLICABLE
	        	//call the service 

	        	PaymentDTO expirePayment = paymentService.expirePayment(id);
	        	log.info("Expire Payment "+expirePayment);

	        	//handle the response

	        	//handle the response
	        	PaymentRes paymentRes=modelMapper.map(expirePayment, PaymentRes.class);
	        	log.info("Coverted  Response DTO to POJO "+paymentRes);
	        	return new ResponseEntity<>(paymentRes,HttpStatus.OK);

	        }

	       

}
