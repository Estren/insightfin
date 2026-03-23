import 'package:flutter/material.dart';
import 'package:orizon/app.dart';
import 'package:orizon/config/env.dart';
import 'package:orizon/config/injection_container.dart' as di;

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  Env.init(Flavor.production);
  await di.init();
  runApp(const App());
}
