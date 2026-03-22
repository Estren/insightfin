import 'package:flutter/material.dart';

class AppColors {
  AppColors._();

  // Primary (Teal)
  static const primary50 = Color(0xFFF0FDFA);
  static const primary100 = Color(0xFFCCFBF1);
  static const primary200 = Color(0xFF99F6E4);
  static const primary300 = Color(0xFF5EEAD4);
  static const primary400 = Color(0xFF2DD4BF);
  static const primary500 = Color(0xFF14B8A6);
  static const primary600 = Color(0xFF0D9488);
  static const primary700 = Color(0xFF0F766E);
  static const primary800 = Color(0xFF115E59);
  static const primary900 = Color(0xFF134E4A);

  // Accent (Amber)
  static const accent50 = Color(0xFFFFFBEB);
  static const accent100 = Color(0xFFFEF3C7);
  static const accent200 = Color(0xFFFDE68A);
  static const accent300 = Color(0xFFFCD34D);
  static const accent400 = Color(0xFFFBBF24);
  static const accent500 = Color(0xFFF59E0B);
  static const accent600 = Color(0xFFD97706);
  static const accent700 = Color(0xFFB45309);

  // Semantic
  static const income = Color(0xFF10B981);
  static const expense = Color(0xFFEF4444);
  static const warning = Color(0xFFF59E0B);
  static const info = Color(0xFF3B82F6);

  // Neutral (Slate)
  static const neutral50 = Color(0xFFF8FAFC);
  static const neutral100 = Color(0xFFF1F5F9);
  static const neutral200 = Color(0xFFE2E8F0);
  static const neutral300 = Color(0xFFCBD5E1);
  static const neutral400 = Color(0xFF94A3B8);
  static const neutral500 = Color(0xFF64748B);
  static const neutral600 = Color(0xFF475569);
  static const neutral700 = Color(0xFF334155);
  static const neutral800 = Color(0xFF1E293B);
  static const neutral900 = Color(0xFF0F172A);

  // Gradients
  static const horizonGradient = LinearGradient(
    colors: [primary600, primary500, accent500],
    begin: Alignment.centerLeft,
    end: Alignment.centerRight,
  );

  static const sunriseGradient = LinearGradient(
    colors: [primary600, accent500],
    begin: Alignment.bottomCenter,
    end: Alignment.topCenter,
  );
}
