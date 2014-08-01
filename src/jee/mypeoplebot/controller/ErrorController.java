package jee.mypeoplebot.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ErrorController {

//	@Autowired LogService logService;
	
	@RequestMapping("/error/500")
	public ModelAndView error500( Throwable e, HttpServletRequest req, HttpServletResponse res ) {

		//
		String title = e.getMessage();
		StringBuffer message = new StringBuffer();
		StackTraceElement [] temp = e.getStackTrace();
		if( temp != null ) {
			for( int i=0; i<temp.length; i++ ) {
				
				StackTraceElement t = temp[i];
//				if( t.getClassName().indexOf( "toz" ) != -1 ) {
					message.append( t.toString() + "\n" );
//				}
			}
		}

//		logService.insertLogException( "500", title, message.toString() );
		
		ModelAndView mav = new ModelAndView( "error/500" );
		return mav;
	}

	@RequestMapping("/error/404")
	public ModelAndView error404( Throwable e, HttpServletRequest req, HttpServletResponse res ) {
		
		//
		String title = e.getMessage();
		StringBuffer message = new StringBuffer();
		StackTraceElement [] temp = e.getStackTrace();
		if( temp != null ) {
			for( int i=0; i<temp.length; i++ ) {
				
				StackTraceElement t = temp[i];
//				if( t.getClassName().indexOf( "toz" ) != -1 ) {
					message.append( t.toString() + "\n" );
//				}
			}
		}

//		logService.insertLogException( "404", title, message.toString() );

		ModelAndView mav = new ModelAndView( "error/404" );
		return mav;
	}

	@RequestMapping("/error/403")
	public ModelAndView error403( Throwable e, HttpServletRequest req, HttpServletResponse res ) {

		//
		String title = e.getMessage();
		StringBuffer message = new StringBuffer();
		StackTraceElement [] temp = e.getStackTrace();
		if( temp != null ) {
			for( int i=0; i<temp.length; i++ ) {
				
				StackTraceElement t = temp[i];
//				if( t.getClassName().indexOf( "toz" ) != -1 ) {
					message.append( t.toString() + "\n" );
//				}
			}
		}

//		logService.insertLogException( "403", title, message.toString() );

		ModelAndView mav = new ModelAndView( "error/403" );
		return mav;
	}
}
