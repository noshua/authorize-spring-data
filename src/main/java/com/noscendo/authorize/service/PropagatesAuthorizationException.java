package com.noscendo.authorize.service;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractView;

@ControllerAdvice
public class PropagatesAuthorizationException {

	@ExceptionHandler(HttpMessageNotWritableException.class)
	View handleWrite(HttpMessageNotWritableException ex) {
		if (ex.getRootCause() instanceof AuthorizationDeniedException denied) {
			return new ExceptionPropagatingView(denied);
		}
		return null;
	}

	private static class ExceptionPropagatingView extends AbstractView {
		private final Exception toPropagate;

		ExceptionPropagatingView(Exception toPropagate) {
			this.toPropagate = toPropagate;
		}

		@Override
		protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
			throw this.toPropagate;
		}
	}
}
