//package ca.uhn.fhir.jpa.starter.controller;
//
//import ca.uhn.fhir.jpa.starter.service.EmailUserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/iprd")
//@RequiredArgsConstructor
//public class EmailController {
//
//	@Autowired
//	EmailUserService emailUserService;
//
////	@RequestMapping(method = RequestMethod.POST, value = "/email")
////	public Map<String, Object> registerRabbit(@RequestBody EmailUserService.RequestDto requestDto) {
////		long timeTaken = emailUserService.measureTimeMillis(() -> emailUserService.registerUserRabbit(requestDto));
////		Map<String, Object> response = new HashMap<>();
////		response.put("message", "User registered successfully using RabbitMQ Method");
////		response.put("timeTaken in Ms", timeTaken);
////		return response;
////	}
//
//	@RequestMapping(method = RequestMethod.POST, value = "/sendFacilitySummaryEmail")
//	public Map<String, Object> sendFacilitySummaryEmail(
//		@RequestHeader(name = "Authorization") String token,
//		@RequestParam("env") String env,
//		@RequestParam("lga") String lga,
//		@RequestBody EmailUserService.EmailRequestDto emailRequest,
//		@RequestParam Map<String, String> allFilters
//	) {
//		long timeTaken = emailUserService.measureTimeMillis(() ->
//			emailUserService.sendFacilitySummaryEmail(emailRequest.getEmail(), token, env, lga, allFilters)
//		);
//		Map<String, Object> response = new HashMap<>();
//		response.put("message", "Facility summary email request processed successfully");
//		response.put("timeTaken in Ms", timeTaken);
//		return response;
//	}
//}