import com.example.shared.MonsterDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

val jsonClient = HttpClient(Js) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        })
    }
}

suspend fun fetchMonster(id: Int? = null): MonsterDto {
    val url = if (id != null) "http://localhost:8090/monsters/$id" else "http://localhost:8090/monsters"
    return jsonClient.get(url).body()
}

suspend fun saveMonster(monster: MonsterDto): MonsterDto {
    return jsonClient.put("http://localhost:8090/monsters") {
        contentType(ContentType.Application.Json)
        setBody(monster)
    }.body()
}