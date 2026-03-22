import 'package:flutter/material.dart';

class AppSpacing {
  AppSpacing._();

  // Base unit: 4px
  static const double xs = 4;
  static const double sm = 8;
  static const double md = 16;
  static const double lg = 24;
  static const double xl = 32;
  static const double xxl = 48;

  // Border Radius
  static const radiusSm = BorderRadius.all(Radius.circular(8));
  static const radiusMd = BorderRadius.all(Radius.circular(12));
  static const radiusLg = BorderRadius.all(Radius.circular(16));
  static const radiusFull = BorderRadius.all(Radius.circular(9999));

  // Common paddings
  static const paddingCard = EdgeInsets.all(md);
  static const paddingPage = EdgeInsets.symmetric(horizontal: xl, vertical: lg);
  static const paddingSection = EdgeInsets.symmetric(vertical: lg);
}
