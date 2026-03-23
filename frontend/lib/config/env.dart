enum Flavor { development, production }

class Env {
  static late Flavor _flavor;

  static Flavor get flavor => _flavor;

  static void init(Flavor flavor) {
    _flavor = flavor;
  }

  static String get apiBaseUrl {
    switch (_flavor) {
      case Flavor.development:
        return 'http://localhost:8080/api'; // Android emulator -> host localhost
      case Flavor.production:
        return 'https://api.orizon.app/api';
    }
  }

  static bool get isDevelopment => _flavor == Flavor.development;
  static bool get isProduction => _flavor == Flavor.production;
}
