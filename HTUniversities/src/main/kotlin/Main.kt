import java.util.Scanner
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.request.*
import io.ktor.utils.io.printStack
import kotlinx.coroutines.runBlocking
import redis.clients.jedis.JedisPooled
import redis.clients.jedis.search.RediSearchUtil

@Serializable
data class University(
    val name: String,
    val country: String,
    val web_pages: List<String>
)

suspend fun fetchUniversities(country: String): List<University> {
    val client = HttpClient(engineFactory = io.ktor.client.engine.cio.CIO) {
        install(ContentNegotiation){
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout){
            connectTimeoutMillis = 1500
        }
    }

    val url = "http://universities.hipolabs.com/search?country=${country.replace(" ", "%20")}"

    return try{
        client.use{
            it.get(url).body()
        }
    } catch (e: Exception) {
        e.printStack()
        emptyList()
    }
}

fun saveToDB (univer: List<University>, redis: JedisPooled){
    univer.forEach {
        redis.set(it.name, it.web_pages[0])
    }
}

fun searchInDB (redis: JedisPooled, nameOfCity: String): MutableMap<String, String>{
    var key: String
    var web: String
    val resaltList = redis.keys("$nameOfCity*")
    val resalt: MutableMap<String, String> = mutableMapOf()
    var size = resaltList.size
    if (size!=0) {
        size--
        for (u in 0 .. size) {
            key = resaltList.elementAt(u)
            web = redis.get(key)
            resalt[key] = web
        }
    }
    return resalt
}

fun main() = runBlocking{
    val scanner = Scanner(System.`in`)
    val redis = JedisPooled("Localhost", 6379)

    print("Введите страну: ")
    val country = scanner.nextLine()
    //val country = "Poland"

    println("----------------")

    val universities = fetchUniversities(country)
    println("Получено ${universities.size} университетов")
    saveToDB(universities, redis)
    println("----------------")

    print("\nВведите часть названия для поиска: ")
    val stringOfSearch = scanner.nextLine()
    //val stringOfSearch = "Warsaw"

    val result = searchInDB(redis, stringOfSearch)

    result.forEach {
        println(it)
    }

   redis.close()
}
