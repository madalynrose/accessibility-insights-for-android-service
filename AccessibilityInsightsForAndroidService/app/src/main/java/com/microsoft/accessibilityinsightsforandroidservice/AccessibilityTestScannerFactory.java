package com.microsoft.accessibilityinsightsforandroidservice;

import android.content.Context;

public class AccessibilityTestScannerFactory {
    public static AccessibilityTestScanner createAccessibilityTestScanner(
            Context context) {

        return new AccessibilityTestScanner(context);
    }
}
