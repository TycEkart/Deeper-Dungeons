import com.example.shared.MonsterDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.files.File
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

val jsonClient = HttpClient(Js) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        })
    }
}

suspend fun fetchMonster(id: Int): MonsterDto {
    return jsonClient.get("http://localhost:8090/monsters/$id").body()
}

suspend fun fetchAllMonsters(): List<MonsterDto> {
    return jsonClient.get("http://localhost:8090/monsters").body()
}

suspend fun saveMonster(monster: MonsterDto): MonsterDto {
    return jsonClient.put("http://localhost:8090/monsters") {
        contentType(ContentType.Application.Json)
        setBody(monster)
    }.body()
}

suspend fun uploadMonsterImage(id: Int, file: File): MonsterDto {
    // Fallback to FileReader since arrayBuffer() is missing in this stdlib version
    val arrayBuffer = readFileAsArrayBuffer(file)
    val byteArray = Int8Array(arrayBuffer).unsafeCast<ByteArray>()

    return jsonClient.submitFormWithBinaryData(
        url = "http://localhost:8090/monsters/$id/image",
        formData = formData {
            append(FormPart("file", byteArray, Headers.build {
                append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
            }))
        }
    ).body()
}

suspend fun readFileAsArrayBuffer(file: File): ArrayBuffer = suspendCoroutine { continuation ->
    val reader = FileReader()
    reader.onload = { 
        continuation.resume(reader.result as ArrayBuffer)
    }
    reader.onerror = { 
        continuation.resumeWithException(Exception("Error reading file"))
    }
    reader.readAsArrayBuffer(file)
}