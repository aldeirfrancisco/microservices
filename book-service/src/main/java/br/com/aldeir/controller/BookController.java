package br.com.aldeir.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.aldeir.model.Book;
import br.com.aldeir.proxy.CambioProxy;
import br.com.aldeir.repository.BookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Book endPoint")
@RestController
@RequestMapping("book-service")
public class BookController {
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private BookRepository repository;
	
	@Autowired
	private CambioProxy proxy;
	
	//http://localhost:8100/book-service/1/BRL
	
	@Operation(summary = "Find a specific book by your ID")
	@GetMapping("/{id}/{currency}")
	private Book findBook( @PathVariable("id") Long id,
			               @PathVariable("currency") String currency) {
		HashMap<String, String> params = new HashMap();
		
		var book = repository.findById(id).get();
		if(book == null ) throw new RuntimeException("Book not found");
		
		params.put("amount", book.getPrice().toString());
		params.put("from", "USD");
		params.put("to", currency);
		var port =	environment.getProperty("local.server.port");
		
		
		var cambio = proxy.getCambio(book.getPrice(), "USD", currency);
		 
		 book.setEnvironment("Book prot "+port + 
				  " cambio port " + cambio.getEnvironment()  );
		 book.setPrice(cambio.getConvertedValue());
		 
		return book;
	}
/*	@GetMapping("/{id}/{currency}")
	private Book findBook( @PathVariable("id") Long id,
			@PathVariable("currency") String currency) {
		HashMap<String, String> params = new HashMap();
		
		var book = repository.findById(id).get();
		if(book == null ) throw new RuntimeException("Book not found");
		
		params.put("amount", book.getPrice().toString());
		params.put("from", "USD");
		params.put("to", currency);
		var port =	environment.getProperty("local.server.port");
		
		var response = new RestTemplate()
				.getForEntity("http://localhost:8000/cambio-service/{amount}/{from}/{to}", Cambio.class, params);
		
		var cambio = response.getBody();
		
		book.setEnvironment(port);
		book.setPrice(cambio.getConvertedValue());
		
		return book;
	}*/
}
