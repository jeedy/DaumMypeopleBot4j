package jee.mypeoplebot.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {
	
	@RequestMapping("/index")
	public ModelAndView index( HttpServletRequest req, HttpServletResponse res){
		
		ModelAndView mav = new ModelAndView("index");
		return mav;
	}
	
}
