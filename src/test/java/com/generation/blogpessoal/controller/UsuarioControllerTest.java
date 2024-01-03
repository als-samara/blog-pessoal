package com.generation.blogpessoal.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.repository.UsuarioRepository;
import com.generation.blogpessoal.service.UsuarioService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsuarioControllerTest {

	// Cliente usado para escrever testes criando um modelo de comunicação com as APIs HTTP.
	// Fornece os mesmos Métodos, cabeçalhos e outras construções do protocolo HTTP.
	@Autowired
	private TestRestTemplate testRestTemplate;
	
	// persistir os objetos no Banco de dados de testes com a senha criptografada
	@Autowired
	private UsuarioService usuarioService;
	
	// limpar o Banco de dados de testes
	@Autowired
	private UsuarioRepository usuarioRepository;

	// apaga todos os dados da tabela e cria o usuário root@root.com para testar os
	// Métodos protegidos por autenticação
	@BeforeAll
	void start() {

		usuarioRepository.deleteAll();
		usuarioService.cadastrarUsuario(new Usuario(0L, "Root", "root@root.com", "rootroot", "-"));
	}

	@Test // indica que o método executará um teste
	@DisplayName("Cadastrar Um Usuário") // msg exibida no lugar do nome do método
	public void deveCriarUmUsuario() {
		
		// HttpEntity com objeto Usuário
		// Transforma os Atributos num objeto da Classe Usuario, que será enviado no corpo da requisição (Request Body).
		HttpEntity<Usuario> corpoRequisicao = new HttpEntity<Usuario>(
				new Usuario(0L, "Paulo Antunes", "paulo_antunes@email.com.br", "13465278", "-"));
		
		// Objeto do tipo ResponseEntity com Usuario, que recebe a resposta da requisição
		// A requisição é feita através do método .exchange (da classe TestRestTenplate)
		ResponseEntity<Usuario> corpoResposta = testRestTemplate.exchange("/usuarios/cadastrar", HttpMethod.POST,
				corpoRequisicao, Usuario.class);
		
		// busca o status da resposta e checa com o asserequals se é igual a resposta esperada (201)
		assertEquals(HttpStatus.CREATED, corpoResposta.getStatusCode());

	}

	
	@Test
	@DisplayName("Não deve permitir duplicação do Usuário")
	public void naoDeveDuplicarUsuario() {
		
		// É enviado um usuario para persistencia no DB
		usuarioService.cadastrarUsuario(new Usuario(0L, "Maria da Silva", "maria_silva@email.com.br", "13465278", "-"));
		
		// No corpo da requisição, está sendo enviado um usuário igual ao enviado acima, para tentar cadastrar em duplicidade
		HttpEntity<Usuario> corpoRequisicao = new HttpEntity<Usuario>(
				new Usuario(0L, "Maria da Silva", "maria_silva@email.com.br", "13465278", "-"));
		
		// Objeto que encapsula a resposta, requisitada através do exchange, que precisa de 4 parâmetros: a URI, o método HTTP, O Objeto HttpEntity (requisição), O conteúdo esperado no Corpo da Resposta (objeto da classe usuario)
		ResponseEntity<Usuario> corpoResposta = testRestTemplate.exchange("/usuarios/cadastrar", HttpMethod.POST,
				corpoRequisicao, Usuario.class);
		
		// Verifica se o status da requisição é igual ao esperado (bad request)
		assertEquals(HttpStatus.BAD_REQUEST, corpoResposta.getStatusCode());
	}
	

	@Test
	@DisplayName("Atualizar um Usuário")
	public void deveAtualizarUmUsuario() {
		
		// Optional para armazenar o objeto persistido no DB
		Optional<Usuario> usuarioCadastrado = usuarioService.cadastrarUsuario(
				new Usuario(0L, "Juliana Andrews", "juliana_andrews@email.com.br", "juliana123", "-"));
		
		// objeto que será usado para atualizar o usuário criado acima
		Usuario usuarioUpdate = new Usuario(usuarioCadastrado.get().getId(), "Juliana Andrews Ramos",
				"juliana_ramos@email.com.br", "juliana123", "-");
		
		// A requisição recebe o usuarioUpdate, que tem seus atributos transformados em um objeto Usuario a ser enviado no corpo da requisição
		HttpEntity<Usuario> corpoRequisicao = new HttpEntity<Usuario>(usuarioUpdate);
		
		// A requisição é enviada com os 4 atributos necessários e a resposta armzenada na variável corpoResposta
		// .withBasicAuth é usado para autenticar o usuário root criado no teste start()
		ResponseEntity<Usuario> corpoResposta = testRestTemplate.withBasicAuth("root@root.com", "rootroot")
				.exchange("/usuarios/atualizar", HttpMethod.PUT, corpoRequisicao, Usuario.class);
		
		// Espera um HttpStatus ok(200) no corpoResposta.
		assertEquals(HttpStatus.OK, corpoResposta.getStatusCode());

	}

	@Test
	@DisplayName("Listar todos os Usuários")
	public void deveMostrarTodosUsuarios() {
		
		// persiste 2 Usuarios no DB, através do Método de cadastrar da UsuarioService
		usuarioService.cadastrarUsuario(
				new Usuario(0L, "Sabrina Sanches", "sabrina_sanches@email.com.br", "sabrina123", "-"));

		usuarioService.cadastrarUsuario(
				new Usuario(0L, "Ricardo Marques", "ricardo_marques@email.com.br", "ricardo123", "-"));
		
		//  como o Blog Pessoal está com o Spring Security habilitado com autenticação do tipo Http Basic, o Objeto testRestTemplate dos endpoints que exigem autenticação, deverá efetuar o login com um usuário e uma senha válida para realizar os testes
		// .withBasicAuth autentica o usuário root criado no test start()
		// o método exchange recebe o endpoint, o tipo de método(get), corpo de requisição nulo (por ser um método get) e o tipo de dado que queremos retornar
		ResponseEntity<String> resposta = testRestTemplate.withBasicAuth("root@root.com", "rootroot")
				.exchange("/usuarios/all", HttpMethod.GET, null, String.class);
		
		// Espera um HttpStatus ok(200) na resposta.
		assertEquals(HttpStatus.OK, resposta.getStatusCode());

	}
	
	@Test
	@DisplayName("Listar Usuário por Id")
	public void deveMostrarUsuarioPeloIdFornecido() {
		
		Usuario mimi = new Usuario(0L, "Mimi Almeida", "mimi@email.com.br", "mimi123", "-");
		usuarioService.cadastrarUsuario(mimi);
		
		ResponseEntity<Usuario> resposta = testRestTemplate.withBasicAuth("root@root.com", "rootroot")
				.exchange("/usuarios/{id}", HttpMethod.GET, null, Usuario.class, mimi.getId());
		
		// Espera um HttpStatus ok(200) na resposta.
		assertEquals(HttpStatus.OK, resposta.getStatusCode());

	}

}
