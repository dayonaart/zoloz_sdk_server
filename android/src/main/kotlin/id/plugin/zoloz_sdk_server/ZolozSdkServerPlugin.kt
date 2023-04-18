package id.plugin.zoloz_sdk_server

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import androidx.annotation.NonNull
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.ap.zoloz.hummer.api.*
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import ru.skornei.restserver.RestServerManager


/** ZolozSdkServerPlugin */
class ZolozSdkServerPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    private lateinit var channel: MethodChannel
    private lateinit var context: Context
    private val REQUEST_EXTERNAL_STORAGE = 1
    private var mHandler: Handler? = null
    private lateinit var activity: Activity
    private var zolozInitServer = ZolozInitServer
    private var zolozCheckServer = ZolozCheckServer
    private var time = System.currentTimeMillis()
    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "zoloz_sdk_server")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
        mHandler = Handler()

    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        if (call.method == "startZoloz") {
            startZoloz(result)
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun runOnIoThread(runnable: Runnable) {
        val thread = Thread(runnable)
        thread.start()
    }

    private fun startZoloz(@NonNull result: Result) {
        runOnIoThread {
            val initRequest: String? = initRequest()
            val initResponse = JSON.parseObject(initRequest, InitResponse::class.java)
            val zlzFacade = ZLZFacade.getInstance()
            val request = ZLZRequest()
            request.zlzConfig = initResponse.clientCfg
            request.bizConfig[ZLZConstants.CONTEXT] = activity
            request.bizConfig[ZLZConstants.PUBLIC_KEY] = initResponse.rsaPubKey
            request.bizConfig[ZLZConstants.LOCALE] = "en-US"
            mHandler!!.postAtFrontOfQueue {
                zlzFacade.start(request, object : IZLZCallback {
                    override fun onCompleted(response: ZLZResponse) {
                        runOnIoThread {
                            val initCheck = initCheck("$time", initResponse.transactionId)
                            result.success(initCheck)
                        }
                    }

                    override fun onInterrupted(response: ZLZResponse) {
                        val callback = JSONObject()
                        callback["ret_code"] = response.retCode
                        callback["ext_info"] = response.extInfo
                        result.success(callback)
                    }
                })
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun initRequest(): String? {
        val request: IRequest = LocalRequest()
        val jsonObject = JSONObject()

        jsonObject["bizId"] = "$time"
        jsonObject["userId"] = "$time"
        val requestData: String = jsonObject.toString()
        return request.request("http://127.0.0.1:6666/init", requestData)
    }

    private fun initCheck(bizId: String, transactionId: String): String? {
        val request: IRequest = LocalRequest()
        val jsonObject = JSONObject()
        jsonObject["bizId"] = bizId
        jsonObject["transactionId"] = transactionId
        val requestData: String = jsonObject.toString()
        return request.request("http://127.0.0.1:7777/check", requestData)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        RestServerManager.initialize(binding.activity.application)
        zolozInitServer.start()
        zolozCheckServer.start()

    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    }

    override fun onDetachedFromActivity() {
    }
}
