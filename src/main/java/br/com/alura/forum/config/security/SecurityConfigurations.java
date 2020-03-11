package br.com.alura.forum.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.alura.forum.repository.UsuarioRepository;
//Para utilizar o módulo do Spring Security, devemos adicioná-lo como dependência do projeto no arquivo pom.xml
//Para habilitar e configurar o controle de autenticação e autorização do projeto, devemos criar uma classe e anotá-la com @Configuration e @EnableWebSecurity
//Para utilizar JWT na API, devemos adicionar a dependência da biblioteca jjwt no arquivo pom.xml do projeto
//Para adicionar o Spring Boot Actuator no projeto, devemos adicioná-lo como uma dependência no arquivo pom.xml
//Para utilizar o Spring Boot Admin, devemos criar um projeto Spring Boot e adicionar nele os módulos spring-boot-starter-web e spring-boot-admin-server
@EnableWebSecurity
@Configuration
public class SecurityConfigurations extends WebSecurityConfigurerAdapter {

	@Autowired
	private AutenticacaoService autenticacaoService;
	
	@Autowired
	private TokenService tokenService;
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Override
	@Bean
	//Para disparar manualmente o processo de autenticação no Spring Security, devemos utilizar a classe AuthenticationManager
	//Para poder injetar o AuthenticationManager no controller, devemos criar um método anotado com @Bean, na classe SecurityConfigurations, que retorna uma chamada ao método super.authenticationManager()
	protected AuthenticationManager authenticationManager() throws Exception {
		return super.authenticationManager();
	}
	
	//Configuracoes de autenticacao
	//Devemos indicar ao Spring Security qual o algoritmo de hashing de senha que utilizaremos na API, chamando o método passwordEncoder(), dentro do método configure(AuthenticationManagerBuilder auth)
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(autenticacaoService).passwordEncoder(new BCryptPasswordEncoder());
	}
	
	//Configuracoes de autorizacao
	//Para liberar acesso a algum endpoint da nossa API, devemos chamar o método http.authorizeRequests().antMatchers().permitAll() dentro do método configure(HttpSecurity http), que está na classe SecurityConfigurations
	//Para o Spring Security gerar automaticamente um formulário de login, devemos chamar o método and().formLogin(), dentro do método configure(HttpSecurity http)
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
		.antMatchers(HttpMethod.GET, "/topicos").permitAll()
		.antMatchers(HttpMethod.GET, "/topicos/*").permitAll()
		.antMatchers(HttpMethod.POST, "/auth").permitAll()
				//Para que o Actuator exponha mais informações sobre a API, devemos adicionar as propriedades management.endpoint.health.show-details=always e management.endpoints.web.exposure.include=* no arquivo application.properties
		.antMatchers(HttpMethod.GET, "/actuator/**").permitAll() //Para liberar acesso ao Actuator no Spring Security
		.anyRequest().authenticated() //indica ao Spring Security para bloquear todos os endpoints que não foram liberados anteriormente com o método permitAll()
		.and().csrf().disable()
		.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)//Para configurar a autenticação stateless no Spring Security
				// criar um filtro, que vai conter a lógica de recuperar o token do cabeçalho Authorization, validá-lo e autenticar o cliente
		.and().addFilterBefore(new AutenticacaoViaTokenFilter(tokenService, usuarioRepository), UsernamePasswordAuthenticationFilter.class); //Para habilitar o filtro no Spring Security, devemos chamar o método
	}
	
	
	//Configuracoes de recursos estaticos(js, css, imagens, etc.)
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/**.html", "/v2/api-docs", "/webjars/**", "/configuration/**", "/swagger-resources/**"); //Para liberar acesso ao Swagger no Spring Security
	}
	
}







