package com.microsoft.accessibilityinsightsforandroidservice;

import android.content.Context;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultBaseUtils;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityEventCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.checks.TouchTargetSizeCheck;
import com.google.android.apps.common.testing.accessibility.framework.suggestions.FixSuggestionPreset;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchyAndroid;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElementAndroid;
import com.google.android.apps.common.testing.accessibility.framework.utils.contrast.BitmapImage;
import com.google.common.collect.ImmutableSet;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccessibilityTestScanner {
    private final Context context;

    public AccessibilityTestScanner(Context context) {
        this.context = context;
    }

    public List<AccessibilityHierarchyCheckResult> scanWithAccessibilityTestFramework(AccessibilityNodeInfo rootNode, BitmapImage screenshot) {
        Parameters parameters = new Parameters();
        parameters.setSaveViewImages(true);
        parameters.putCustomTouchTargetSize(44);
        parameters.putScreenCapture(screenshot);
        ImmutableSet<AccessibilityHierarchyCheck> checks =
                AccessibilityCheckPreset.getAccessibilityHierarchyChecksForPreset(
                        AccessibilityCheckPreset.LATEST);
        AccessibilityHierarchyAndroid hierarchy = AccessibilityHierarchyAndroid.newBuilder(rootNode,this.context).build();
        ViewHierarchyElementAndroid element = hierarchy.getActiveWindow().getRootView();
        List<AccessibilityHierarchyCheckResult> results = new ArrayList<>();

        for (AccessibilityHierarchyCheck check : checks) {
            results.addAll(check.runCheckOnHierarchy(hierarchy, element, parameters));
        }

        AccessibilityCheckResult.AccessibilityCheckResultType relevantTypes[] = {AccessibilityCheckResult.AccessibilityCheckResultType.ERROR,AccessibilityCheckResult.AccessibilityCheckResultType.INFO, AccessibilityCheckResult.AccessibilityCheckResultType.WARNING,AccessibilityCheckResult.AccessibilityCheckResultType.RESOLVED,
                AccessibilityCheckResult.AccessibilityCheckResultType.NOT_RUN};

        return AccessibilityCheckResultUtils.getResultsForTypes(results, new HashSet<>(Arrays.asList(relevantTypes)));
    }
}
