package com.app.controller;

import com.app.parser.McaGovIn;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
@RequestMapping("/")
public class ParserController {
	@RequestMapping(method = RequestMethod.GET)
	public String printWelcome(ModelMap model) {
		model.addAttribute("message", "Hello world!");
        System.out.println("Hello!");
        List<String> allCIN = McaGovIn.getAllCIN("https://www.zaubacorp.com/company-list/p-1-company.html");
        McaGovIn.getCompanies(allCIN);
        return "hello";
	}
}