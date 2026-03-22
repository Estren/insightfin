import 'package:flutter/material.dart';
import 'package:orizon/core/theme/app_colors.dart';

class LoadingIndicator extends StatelessWidget {
  final double size;
  final double strokeWidth;

  const LoadingIndicator({
    super.key,
    this.size = 36,
    this.strokeWidth = 3,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: SizedBox(
        width: size,
        height: size,
        child: CircularProgressIndicator(
          strokeWidth: strokeWidth,
          color: AppColors.primary500,
        ),
      ),
    );
  }
}
