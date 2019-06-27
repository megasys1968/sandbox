package quo.vadis.megasys

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GraphqlWebfluxApplicationTests {
  @Autowired
  lateinit var context: ApplicationContext

	@Test
	fun query() {
    val client = WebTestClient.bindToApplicationContext(context).build()
    client
      .post()
      .uri("/api/v1")
      .contentType(MediaType.APPLICATION_JSON)
      .body(BodyInserters.fromObject("""
          {
            "query": "{ users { id name } }"
          }
        """.trimIndent()))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("$.errors").doesNotExist()
      .returnResult().toString().let {
        println(it)
      }
	}

  @Test
  fun mutation() {
    val client = WebTestClient.bindToApplicationContext(context).build()
    client
      .post()
      .uri("/api/v1")
      .contentType(MediaType.parseMediaType("application/graphql"))
      .body(BodyInserters.fromObject("""
          mutation {
            createUser(name: "ベス")
          }
        """.trimIndent()))
      .exchange()
      .expectStatus().isOk
      .expectBody()
        .jsonPath("$.errors").doesNotExist()
      .returnResult().toString().let {
        println(it)
      }

    query()

  }




}
