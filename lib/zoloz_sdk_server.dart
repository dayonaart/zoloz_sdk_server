import 'package:flutter/services.dart';

class ZolozSdkServer {
  final MethodChannel _methodChannel = const MethodChannel("zoloz_sdk_server");
  Future<dynamic> startZoloz() async {
    return await _methodChannel.invokeMethod("startZoloz");
  }
}
