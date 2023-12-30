package com.generation.blogpessoal.configuration;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

@Configuration
public class SwaggerConfig {
	
	// um método com essa anotação é gerenciado pelo Spring e seu objeto retornado
	// pode ser injetado em qualquer ponto da aplicação.
	// Um Bean é um objeto que é instanciado, montado e gerenciado pelo Spring.
	@Bean
    OpenAPI springBlogPessoalOpenAPI() {
        
		// Cria um Objeto da Classe OpenAPI, que gera a documentação 
		// no Swagger utilizando a especificação OpenAPI.
		return new OpenAPI()
            .info(new Info()
                .title("Projeto Blog Pessoal")
                .description("Projeto Blog Pessoal - Generation Brasil")
                .version("v0.0.1")
                .license(new License()
                    .name("Generation Brasil")
                    .url("https://brazil.generation.org/"))
                .contact(new Contact()
                    .name("Samara Almeida")
                    .url("https://www.linkedin.com/in/samara-almeida-als/")
                    .email("samaraalmeida379@gmail.com")))
            .externalDocs(new ExternalDocumentation()
                .description("Github")
                .url("https://github.com/als-samara/"));
    }


	@Bean
	OpenApiCustomizer customerGlobalHeaderOpenApiCustomiser() {

		// Cria um Objeto da Classe OpenAPI, que gera a documentação no Swagger utilizando a especificação OpenAPI.
		return openApi -> {
			// Itera sobre todos os caminhos (paths) da API, acessa suas operações e adiciona respostas padrão para cada operação.
			openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {

				ApiResponses apiResponses = operation.getResponses();

				apiResponses.addApiResponse("200", createApiResponse("Sucesso!"));
				apiResponses.addApiResponse("201", createApiResponse("Objeto Persistido!"));
				apiResponses.addApiResponse("204", createApiResponse("Objeto Excluído!"));
				apiResponses.addApiResponse("400", createApiResponse("Erro na Requisição!"));
				apiResponses.addApiResponse("401", createApiResponse("Acesso Não Autorizado!"));
				apiResponses.addApiResponse("403", createApiResponse("Acesso Proibido!"));
				apiResponses.addApiResponse("404", createApiResponse("Objeto Não Encontrado!"));
				apiResponses.addApiResponse("500", createApiResponse("Erro na Aplicação!"));

			}));
		};
	}
	
	// O método adiciona uma descrição (Mensagem), em cada Resposta HTTP.
	private ApiResponse createApiResponse(String message) {

		return new ApiResponse().description(message);

	}
}
