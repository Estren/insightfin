import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:orizon/core/theme/app_colors.dart';
import 'package:orizon/core/theme/app_spacing.dart';
import 'package:orizon/core/theme/app_typography.dart';

class AppTheme {
  AppTheme._();

  static ThemeData get light {
    final base = ThemeData(
      useMaterial3: true,
      brightness: Brightness.light,
    );
    return base.copyWith(
      textTheme: GoogleFonts.interTextTheme(base.textTheme),
      colorScheme: const ColorScheme.light(
        primary: AppColors.primary600,
        onPrimary: Colors.white,
        primaryContainer: AppColors.primary100,
        onPrimaryContainer: AppColors.primary900,
        secondary: AppColors.accent500,
        onSecondary: Colors.white,
        secondaryContainer: AppColors.accent100,
        onSecondaryContainer: AppColors.accent700,
        surface: Colors.white,
        onSurface: AppColors.neutral900,
        surfaceVariant: AppColors.neutral100,
        error: AppColors.expense,
        onError: Colors.white,
        outline: AppColors.neutral300,
        outlineVariant: AppColors.neutral200,
      ),
      scaffoldBackgroundColor: AppColors.neutral50,
      appBarTheme: AppBarTheme(
        backgroundColor: Colors.white,
        foregroundColor: AppColors.neutral900,
        elevation: 0,
        scrolledUnderElevation: 1,
        centerTitle: true,
        titleTextStyle: GoogleFonts.inter(
          fontSize: 18,
          fontWeight: FontWeight.w600,
          color: AppColors.neutral900,
          height: 1.33,
        ),
      ),
      cardTheme: CardTheme(
        color: Colors.white,
        elevation: 0,
        shape: RoundedRectangleBorder(borderRadius: AppSpacing.radiusMd),
        margin: EdgeInsets.zero,
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.primary600,
          foregroundColor: Colors.white,
          elevation: 0,
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
          shape: RoundedRectangleBorder(borderRadius: AppSpacing.radiusSm),
          textStyle: AppTypography.label,
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: AppColors.primary600,
          side: const BorderSide(color: AppColors.primary600),
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
          shape: RoundedRectangleBorder(borderRadius: AppSpacing.radiusSm),
          textStyle: AppTypography.label,
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          foregroundColor: AppColors.primary600,
          textStyle: AppTypography.label,
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AppColors.neutral50,
        contentPadding:
            const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        border: OutlineInputBorder(
          borderRadius: AppSpacing.radiusSm,
          borderSide: const BorderSide(color: AppColors.neutral300),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: AppSpacing.radiusSm,
          borderSide: const BorderSide(color: AppColors.neutral300),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: AppSpacing.radiusSm,
          borderSide: const BorderSide(color: AppColors.primary600, width: 2),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: AppSpacing.radiusSm,
          borderSide: const BorderSide(color: AppColors.expense),
        ),
        labelStyle: AppTypography.body.copyWith(color: AppColors.neutral500),
        hintStyle: AppTypography.body.copyWith(color: AppColors.neutral400),
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: Colors.white,
        indicatorColor: AppColors.primary100,
        labelTextStyle: MaterialStateProperty.resolveWith((states) {
          if (states.contains(MaterialState.selected)) {
            return AppTypography.bodySmall
                .copyWith(color: AppColors.primary700, fontWeight: FontWeight.w600);
          }
          return AppTypography.bodySmall.copyWith(color: AppColors.neutral500);
        }),
        iconTheme: MaterialStateProperty.resolveWith((states) {
          if (states.contains(MaterialState.selected)) {
            return const IconThemeData(color: AppColors.primary700, size: 24);
          }
          return const IconThemeData(color: AppColors.neutral500, size: 24);
        }),
      ),
      snackBarTheme: SnackBarThemeData(
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: AppSpacing.radiusSm),
        backgroundColor: AppColors.neutral800,
        contentTextStyle: AppTypography.body.copyWith(color: Colors.white),
      ),
      dividerTheme: const DividerThemeData(
        color: AppColors.neutral200,
        thickness: 1,
        space: 1,
      ),
      chipTheme: ChipThemeData(
        backgroundColor: AppColors.neutral100,
        labelStyle: AppTypography.bodySmall,
        shape: RoundedRectangleBorder(borderRadius: AppSpacing.radiusFull),
        side: BorderSide.none,
      ),
    );
  }

  static ThemeData get dark {
    final base = ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
    );
    return base.copyWith(
      textTheme: GoogleFonts.interTextTheme(base.textTheme),
      colorScheme: const ColorScheme.dark(
        primary: AppColors.primary400,
        onPrimary: AppColors.primary900,
        primaryContainer: AppColors.primary800,
        onPrimaryContainer: AppColors.primary100,
        secondary: AppColors.accent400,
        onSecondary: AppColors.accent700,
        secondaryContainer: AppColors.accent700,
        onSecondaryContainer: AppColors.accent100,
        surface: AppColors.neutral800,
        onSurface: AppColors.neutral100,
        surfaceVariant: AppColors.neutral700,
        error: Color(0xFFF87171),
        onError: AppColors.neutral900,
        outline: AppColors.neutral600,
        outlineVariant: AppColors.neutral700,
      ),
      scaffoldBackgroundColor: AppColors.neutral900,
      appBarTheme: AppBarTheme(
        backgroundColor: AppColors.neutral800,
        foregroundColor: AppColors.neutral100,
        elevation: 0,
        scrolledUnderElevation: 1,
        centerTitle: true,
        titleTextStyle: GoogleFonts.inter(
          fontSize: 18,
          fontWeight: FontWeight.w600,
          color: AppColors.neutral100,
          height: 1.33,
        ),
      ),
      cardTheme: CardTheme(
        color: AppColors.neutral800,
        elevation: 0,
        shape: RoundedRectangleBorder(borderRadius: AppSpacing.radiusMd),
        margin: EdgeInsets.zero,
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.primary500,
          foregroundColor: Colors.white,
          elevation: 0,
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
          shape: RoundedRectangleBorder(borderRadius: AppSpacing.radiusSm),
          textStyle: AppTypography.label,
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: AppColors.primary400,
          side: const BorderSide(color: AppColors.primary400),
          padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
          shape: RoundedRectangleBorder(borderRadius: AppSpacing.radiusSm),
          textStyle: AppTypography.label,
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          foregroundColor: AppColors.primary400,
          textStyle: AppTypography.label,
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AppColors.neutral900,
        contentPadding:
            const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
        border: OutlineInputBorder(
          borderRadius: AppSpacing.radiusSm,
          borderSide: const BorderSide(color: AppColors.neutral600),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: AppSpacing.radiusSm,
          borderSide: const BorderSide(color: AppColors.neutral600),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: AppSpacing.radiusSm,
          borderSide: const BorderSide(color: AppColors.primary400, width: 2),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: AppSpacing.radiusSm,
          borderSide: const BorderSide(color: Color(0xFFF87171)),
        ),
        labelStyle: AppTypography.body.copyWith(color: AppColors.neutral400),
        hintStyle: AppTypography.body.copyWith(color: AppColors.neutral500),
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: AppColors.neutral800,
        indicatorColor: AppColors.primary800,
        labelTextStyle: MaterialStateProperty.resolveWith((states) {
          if (states.contains(MaterialState.selected)) {
            return AppTypography.bodySmall
                .copyWith(color: AppColors.primary300, fontWeight: FontWeight.w600);
          }
          return AppTypography.bodySmall.copyWith(color: AppColors.neutral400);
        }),
        iconTheme: MaterialStateProperty.resolveWith((states) {
          if (states.contains(MaterialState.selected)) {
            return const IconThemeData(color: AppColors.primary300, size: 24);
          }
          return const IconThemeData(color: AppColors.neutral400, size: 24);
        }),
      ),
      snackBarTheme: SnackBarThemeData(
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: AppSpacing.radiusSm),
        backgroundColor: AppColors.neutral700,
        contentTextStyle: AppTypography.body.copyWith(color: Colors.white),
      ),
      dividerTheme: const DividerThemeData(
        color: AppColors.neutral700,
        thickness: 1,
        space: 1,
      ),
      chipTheme: ChipThemeData(
        backgroundColor: AppColors.neutral700,
        labelStyle: AppTypography.bodySmall,
        shape: RoundedRectangleBorder(borderRadius: AppSpacing.radiusFull),
        side: BorderSide.none,
      ),
    );
  }
}
