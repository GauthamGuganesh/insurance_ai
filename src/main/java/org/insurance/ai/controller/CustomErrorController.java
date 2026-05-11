package org.insurance.ai.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

	private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

	@RequestMapping("/error")
	public ModelAndView handleError(HttpServletRequest request) {
		Object status = request.getAttribute("jakarta.servlet.error.status_code");
		Object message = request.getAttribute("jakarta.servlet.error.message");
		Object exception = request.getAttribute("jakarta.servlet.error.exception");

		logger.error("Error occurred - Status: {}, Message: {}, Exception: {}",
				status, message, exception);

		ModelAndView modelAndView = new ModelAndView();

		if (status != null) {
			int statusCode = Integer.parseInt(status.toString());

			if (statusCode == HttpStatus.NOT_FOUND.value()) {
				modelAndView.setViewName("forward:/error.html");
				modelAndView.addObject("code", "404");
				modelAndView.addObject("message", "Page Not Found");
				modelAndView.addObject("details",
						"The page you are looking for might have been removed, had its name changed, or is temporarily unavailable.");
			} else if (statusCode >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
				modelAndView.setViewName("forward:/5xx.html");
				modelAndView.addObject("code", String.valueOf(statusCode));
				modelAndView.addObject("message", "Server Error");
				modelAndView.addObject("details",
						"Something went wrong on our end. Our team has been notified and is working to fix this issue.");
			} else {
				modelAndView.setViewName("forward:/error.html");
				modelAndView.addObject("code", String.valueOf(statusCode));
				modelAndView.addObject("message", "Error");
				modelAndView.addObject("details",
						"An unexpected error occurred. Please try again or contact support.");
			}
		} else {
			modelAndView.setViewName("forward:/error.html");
			modelAndView.addObject("code", "Unknown");
			modelAndView.addObject("message", "Error");
			modelAndView.addObject("details",
					"An unknown error occurred. Please try again or contact support.");
		}

		return modelAndView;
	}
}