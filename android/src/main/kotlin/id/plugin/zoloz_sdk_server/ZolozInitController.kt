package id.plugin.zoloz_sdk_server

import android.content.Context
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.ap.zoloz.hummer.api.ZLZFacade
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.zoloz.api.sdk.client.OpenApiClient
import ru.skornei.restserver.annotations.*
import ru.skornei.restserver.annotations.methods.POST
import ru.skornei.restserver.server.BaseRestServer
import ru.skornei.restserver.server.converter.BaseConverter
import ru.skornei.restserver.server.dictionary.ContentType
import ru.skornei.restserver.server.protocol.RequestInfo
import ru.skornei.restserver.server.protocol.ResponseInfo
import java.io.IOException


@RestController("/init")
class ZolozInitController {
    private fun openApiClient(): OpenApiClient {
        val oc = OpenApiClient()
        oc.hostUrl = ZOLOZ_URL
        oc.clientId = CLIENT_ID
        oc.merchantPrivateKey = MERCHANT_PRIVATE
        oc.openApiPublicKey = ZOLOZ_PUB
        oc.isSigned = true
        return oc
    }

    @POST
    @Produces(ContentType.APPLICATION_JSON)
    @Accept(ContentType.APPLICATION_JSON)
    fun realIdInit(
        context: Context?,
        request: RequestInfo?,
        response: ResponseInfo?,
        zolozInitEntity: ZolozInitEntity?
    ): JSONObject {
       try {
           val payload = JSONObject()
           val metaInfo: String = ZLZFacade.getMetaInfo(context)
           payload["operationMode"] = "CLOSED"
           payload["serviceLevel"] = "FACECAPTURE0003"
           payload["bizId"] = zolozInitEntity?.bizId
           payload["userId"] = zolozInitEntity?.userId
           payload["metaInfo"] = metaInfo
           payload["docType"] = "00000001003"
           openApiClient().isSigned = true
           val apiRespStr = openApiClient().callOpenApi(
               "v1.zoloz.facecapture.initialize",
               JSON.toJSONString(payload)
           )
           val apiResp = JSON.parseObject(apiRespStr)
           return JSONObject(apiResp)
       }catch (e:Exception){
           val error = JSONObject()
           error["error"] = e.toString()
           return error
       }
    }

}

@RestServer(
    port = ZolozInitServer.PORT,
    converter = JsonConverter::class,
    controllers = [ZolozInitController::class]
)
object ZolozInitServer : BaseRestServer() {
    const val PORT = 6666
}
class ZolozInitEntity {
    var bizId: String? = null
        private set
    var userId: String? = null
        private set
    constructor()
    constructor(bizId: String?, userId: String?) {
        this.bizId = bizId
        this.userId = userId
    }
}
class JsonConverter : BaseConverter {
    private val mapper = ObjectMapper()
    override fun writeValueAsBytes(value: Any): ByteArray {
        return try {
            mapper.writeValueAsBytes(value)
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }

    override fun <T> writeValue(src: ByteArray, valueType: Class<T>): T {
        return try {
            mapper.readValue(src, valueType)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}


var ZOLOZ_URL = "https://sg-sandbox-api.zoloz.com"

var CLIENT_ID = "2188485307665580"
var ZOLOZ_PUB =
    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvg78EIG7CKTV+FLotCu/4hJgbhPukVU1Ui8uk3tlDArt8zD8q4SH+lBovaOchxeyamSQ0HK3NgvgTIcfDhFcONETTzNr1F9aI1jiikvJC6Tx5W4va7N9UDB8+r5O362kRrttAB73pyebgAiD932Vn1hE9e31BT8Jq0+x1AEeKAl0lSKwf9AmnqnClSI/87kHjEJ2fVSLCGR93ss09lvjwaby+1bJKRZHToy5Rdto/fMVg4vn/vl4CxvqrIjELjAN1pqNM/0WXoJzopogobKSUxxGRoEN1DQgf4by30KvGxHjS71qAJvZ02N5F3ybGRug3v77MrmsQybhUBB9q4OY9QIDAQAB"
var MERCHANT_PRIVATE =
    "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC5rSLMqyi5wyW1V0jcfGAIdbGiSO72BgwYhaCASMAf4Pt1AiVGFsrQg7llw935hrIEGgrpN2hwa7tWpT2Ua2KQKbH9oScbo2B8hFDA9Yg7chp5UkMHpXnQ6iO34aeNo60y1+3Utu7V8e1M6RApmJLjU6iIIlkOyVEx8qzRrOpw0HsSWTvhsONN5yYLI+cOWqOJfhiJctWS45g0MouW9Zs33/6/GzyU/ZagQHT9LaRGNg2QG+2D1XpfT6jqWgjLnjs4ojmPRn/iBzQhj3VMp7Arm7kVBxgpP7FcgH8wRe47mHWLEEc812n9431+h948aNawde+2Fms03PLoyJRelAJXAgMBAAECggEARhKGKJGLrBduurI3KCa0I10vbZxyulxr4jFD8kYCY8WHgJAPuxTM88ZSiIpT0mC3+/5MEdm8S3kM2VVBGSsxplBmAN75oF+XAUb6JuhCwpR8Uz7tVwxnHaXPVw0NK9ISgjalZfqnxgwoTzdBx0DyZmJjHwU/cOoe/2ejOvoz++C9Bf9ZLrSp5Q8rkidlo2IpTkD9I5IvbcFkboXPDSzcL526xCwaHB4+it3CErvrsJYhxUBc9NzUb+24w8bvZCf6XLipnOoxJY16yE72+ZK1ZDQ2E+roTX7RbKl6ABu/A3hoSo9BcqEwLljBTkYBrLO1PK5IwIVl0SoJOOULfYfVEQKBgQDsjBgCnVp5aLA/G13wN8n1zEYCMoiOuXKgTeDP73bEXgoE+uTsp7rq+vF9nj0DYSW2lhwTdNIlA7TulG/BS83/KlryqOegN0pY5Ch54nCQo3ABbD6olbM1QCSGYWBEbtNNYMC6mn8YTsv2Gsf7U+BQc1SdWQh5uhdt47rfhCpU7wKBgQDI8hU4v7UQK1wDO00yO1m4cplWP5JBd7vnhAHfZRZYxsAUjf9Tw7r6dseXl5lROK1n0sJWG4oVhKcsInne4qhECgYrJ4omEEpwInZhed5eHLnT1QgcMC6rFkDjVcm8evnNXuLGgVdD6YV51ypNA8hGJOozWvOmPvpdkG5jC/+5GQKBgAs18IVfM8SBQ6kaAO+7lSTDE6ZTAsnQ3C/gwQDZ1oUj99GYmnTk1iUA06UidL3OQt1Oa/I4HSWH8XFFM66ziwXG2hyaCLA9Lpbb6VctdixEsrA+kxGmVqH1ckW0I73aisUmCrxNSiy6v0vgpQ2yQfVOIfp7F729JhMdMeQZ/W+dAoGAH5yl5gWSsHaBxShhHpPjq3ar1LuvgIkbkHJd8QzwFQs/UHx+PGlAUwK4p4p73iEydnDbjbxLXtM9kV3jGkNCAWIqUkOoIhDSBQ4G3ZLUfq26Ni7/VZ/m15dqodjvIa2e+sYAmwXh+pcUHnTQ/Mipiw9noKigR/kEXg/IreBHXJECgYAygQcvPMLw3wIea497SlGf7DYSlLZyqH4YTqN94Y9yGbwdxoQS7BeueywuXUS2sHP7pAhdAgjXb+b51rHbMON5McxQdoWkkhY6aTFSe5dW0mxnja8LJYdmUUyqpl+N0U/Y+mxxYAd0s/v3a2NRUvOvB8ShOO53+nH58W3KyTKHZw=="
