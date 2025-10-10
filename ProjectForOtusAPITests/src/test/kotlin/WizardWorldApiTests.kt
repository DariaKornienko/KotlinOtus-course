package api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.qameta.allure.Owner
import io.qameta.allure.Severity
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import org.junit.jupiter.api.*
import io.qameta.allure.SeverityLevel

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WizardWorldApiTests {

    private lateinit var client: HttpClient
    private val baseUrl = "https://wizard-world-api.herokuapp.com"

    @BeforeAll
    fun setup() {
        client = HttpClient(CIO) {
            expectSuccess = false
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
    }

    @AfterAll
    fun tearDown() = runBlocking {
        client.close()
    }

    @SeverityOwner
    @DisplayName("Тест на список зелий")
    fun `ELIXIRS - GET list returns 200 and non-empty array`() = runBlocking {
        val res: HttpResponse = client.get("$baseUrl/Elixirs")
        Assertions.assertEquals(HttpStatusCode.OK, res.status)
        val payload: JsonElement = res.body()
        Assertions.assertTrue(payload is JsonArray, "Ожидался JsonArray")
        val arr = payload.jsonArray
        Assertions.assertTrue(arr.isNotEmpty(), "Список не должен быть пустым")
    }

    @SeverityOwner
    @DisplayName("Тест с ошибочным id зелья")
    fun `ELIXIRS - GET by invalid id returns 400`() = runBlocking {
        val res: HttpResponse = client.get("$baseUrl/Elixirs/not-a-real-id")
        Assertions.assertEquals(HttpStatusCode.BadRequest, res.status)
    }

    @SeverityOwner
    @DisplayName("Тест на получение факультета из списка")
    fun `HOUSES - GET by id returns 200 and has name`() = runBlocking {
        val list: JsonArray = client.get("$baseUrl/Houses").body()
        Assumptions.assumeTrue(list.isNotEmpty())
        val id = list.first().jsonObject["id"]!!.jsonPrimitive.content
        val res: HttpResponse = client.get("$baseUrl/Houses/$id")
        Assertions.assertEquals(HttpStatusCode.OK, res.status)
        val house: JsonObject = res.body()
        Assertions.assertEquals(id, house["id"]!!.jsonPrimitive.content)
        Assertions.assertTrue(house.containsKey("name"))
    }

    @SeverityOwner
    @DisplayName("Тест с ошибочным id факультета")
    fun `HOUSES - GET by invalid id returns 400`() = runBlocking {
        val res: HttpResponse = client.get("$baseUrl/Houses/this-id-does-not-exist")
        Assertions.assertEquals(HttpStatusCode.BadRequest, res.status)
    }

    @SeverityOwner
    @DisplayName("Тест на список заклинаний")
    fun `SPELLS - should return exactly 2 spells of type DarkArts`() = runBlocking {
        val res: HttpResponse = client.get("$baseUrl/Spells") {
            url {
                parameters.append("Type", "DarkArts")
            }
        }

        Assertions.assertEquals(HttpStatusCode.OK, res.status)
        val payload: JsonElement = res.body()
        Assertions.assertTrue(payload is JsonArray, "Ожидался JsonArray")
        val spells = payload.jsonArray
        Assertions.assertEquals(2, spells.size)
    }

    @SeverityOwner
    @DisplayName("Тест с ошибочным id заклинания")
    fun `SPELLS - GET by invalid id returns 404`() = runBlocking {
        val res: HttpResponse = client.get("$baseUrl/Spells/3fa85f64-5717-4562-b3fc-2c963f66afa6")
        Assertions.assertEquals(HttpStatusCode.NotFound, res.status)
    }
}

@Test
@Severity(SeverityLevel.NORMAL)
@Owner("da.kornienko")
annotation class SeverityOwner