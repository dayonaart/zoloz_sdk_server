package id.plugin.zoloz_sdk_server

import android.content.Context
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.zoloz.api.sdk.client.OpenApiClient
import ru.skornei.restserver.annotations.*
import ru.skornei.restserver.annotations.methods.POST
import ru.skornei.restserver.server.BaseRestServer
import ru.skornei.restserver.server.dictionary.ContentType
import ru.skornei.restserver.server.protocol.RequestInfo
import ru.skornei.restserver.server.protocol.ResponseInfo


@RestController("/check")
class ZolozCheckController {
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
        zolozCheckEntity: ZolozCheckEntity?
    ): JSONObject {
        try {
            val apiReq = JSONObject()
            apiReq["bizId"] = zolozCheckEntity?.bizId
            apiReq["transactionId"] = zolozCheckEntity?.transactionId
            apiReq["isReturnImage"] = "Y"
            // apiReq.put("extraImageControlList", "Y");
            // apiReq.put("returnFiveCategorySpoofResult", "Y");
            // apiReq.put("extraImageControlList", "Y");
            // apiReq.put("returnFiveCategorySpoofResult", "Y");
            val apiRespStr = openApiClient().callOpenApi(
                "v1.zoloz.facecapture.checkresult",
                JSON.toJSONString(apiReq)
            )

            val apiResp = JSON.parseObject(apiRespStr)
            return JSONObject(apiResp)
        } catch (e: Exception) {
            val error = JSONObject()
            error["error"] = e.toString()
            return error
        }
    }

}

@RestServer(
    port = ZolozCheckServer.PORT,
    converter = JsonConverter::class,
    controllers = [ZolozCheckController::class]
)
object ZolozCheckServer : BaseRestServer() {
    const val PORT = 7777
}

class ZolozCheckEntity {
    var bizId: String? = null
        private set
    var transactionId: String? = null
        private set

    constructor()
    constructor(bizId: String?, transactionId: String?) {
        this.bizId = bizId
        this.transactionId = transactionId
    }
}