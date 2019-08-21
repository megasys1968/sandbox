package quo.vadis.megasys

import org.apache.commons.lang3.StringUtils
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.ResourceProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.reactive.config.ResourceHandlerRegistry
import org.springframework.web.reactive.config.ViewResolverRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer
import reactor.core.publisher.Mono

@SpringBootApplication
@Controller
class BackendApplication(appContext: ApplicationContext, resourceProperties: ResourceProperties) {
  val welcomePage = resourceProperties.staticLocations
    .map {
      appContext.getResource(StringUtils.appendIfMissing(it, "/") + "index.html")
    }
    .firstOrNull {
      it.exists()
    }

  @Bean
  fun springSecurityFilterChain(http: ServerHttpSecurity, jwtDecoder: ReactiveJwtDecoder): SecurityWebFilterChain {
    http
      .csrf().disable()
      .authorizeExchange()
      .pathMatchers("/", "/*.*").permitAll()
      .anyExchange().authenticated()
      .and()
      .oauth2ResourceServer()
      .jwt().jwtDecoder(jwtDecoder)
    return http.build();
  }

  @GetMapping("/api/{name}")
  fun hello(@PathVariable name: String): Mono<ResponseEntity<String>> {
    return Mono.just(ResponseEntity.ok("Hello, $name!"))
  }

  @GetMapping("/")
  fun welcome(): Mono<ResponseEntity<Resource>> {
    return Mono.just(ResponseEntity.ok().contentType(MediaType.TEXT_HTML)
      .body(welcomePage))
  }

}

fun main(args: Array<String>) {
  runApplication<BackendApplication>(*args)
}
