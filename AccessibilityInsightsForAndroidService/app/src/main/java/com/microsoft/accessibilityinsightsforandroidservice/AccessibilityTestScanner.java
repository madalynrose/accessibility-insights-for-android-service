package com.microsoft.accessibilityinsightsforandroidservice;

import android.content.Context;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResultUtils;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchyAndroid;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.List;

public class AccessibilityTestScanner {
    private final Context context;

    public AccessibilityTestScanner(Context context) {
        this.context = context;
    }

    public List<AccessibilityHierarchyCheckResult> scanWithAccessibilityTestFramework(AccessibilityNodeInfo rootNode) {
        ImmutableSet<AccessibilityHierarchyCheck> checks =
                AccessibilityCheckPreset.getAccessibilityHierarchyChecksForPreset(
                        AccessibilityCheckPreset.LATEST);
        AccessibilityHierarchyAndroid hierarchy = AccessibilityHierarchyAndroid.newBuilder(rootNode,this.context).build();
        List<AccessibilityHierarchyCheckResult> results = new ArrayList<>();

        for (AccessibilityHierarchyCheck check : checks) {
            results.addAll(check.runCheckOnHierarchy(hierarchy));
        }

        List<AccessibilityHierarchyCheckResult> errors =
                AccessibilityCheckResultUtils.getResultsForType(
                        results, AccessibilityCheckResult.AccessibilityCheckResultType.ERROR);

        return errors;
    }
}
