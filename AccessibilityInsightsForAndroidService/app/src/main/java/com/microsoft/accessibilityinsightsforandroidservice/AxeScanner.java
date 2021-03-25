// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.common.collect.ImmutableSet;

import com.deque.axe.android.Axe;
import com.deque.axe.android.AxeContext;
import com.deque.axe.android.AxeResult;
import com.google.android.apps.common.testing.accessibility.framework.*;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchyAndroid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AxeScanner {
  private final AxeRunnerFactory axeRunnerFactory;
  private final AxeContextFactory axeContextFactory;
  private final Context context;

  public AxeScanner(AxeRunnerFactory axeRunnerFactory, AxeContextFactory axeContextFactory, Context context) {
    this.axeRunnerFactory = axeRunnerFactory;
    this.axeContextFactory = axeContextFactory;
    this.context = context;
  }

  public AxeResult scanWithAxe(AccessibilityNodeInfo rootNode, Bitmap screenshot)
      throws ViewChangedException {
    final Axe axe = axeRunnerFactory.createAxeRunner();
    final AxeContext axeContext = axeContextFactory.createAxeContext(rootNode, screenshot);

    // temporary place to view Accessibility Test Framework for Android errors
    List<AccessibilityHierarchyCheckResult> otherErrors = this.RunAccessibilityChecks(rootNode);

    return axe.run(axeContext);
  }

  private List<AccessibilityHierarchyCheckResult> RunAccessibilityChecks(AccessibilityNodeInfo rootNode) {
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
