// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.microsoft.accessibilityinsightsforandroidservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import android.view.accessibility.AccessibilityEvent;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FocusVisualizer.class})
public class FocusVisualizerTest {
  FocusVisualizer testSubject;

  @Mock FocusVisualizerStyles focusVisualizerStylesMock;
  @Mock FocusVisualizationCanvas focusVisualizationCanvasMock;
  @Mock AccessibilityEvent accessibilityEventMock;
  @Mock FocusElementHighlight focusElementHighlightMock;
  @Mock FocusElementLine focusElementLineMock;

  @Before
  public void prepare() throws Exception {

    whenNew(FocusElementHighlight.class).withAnyArguments().thenReturn(focusElementHighlightMock);
    whenNew(FocusElementLine.class).withAnyArguments().thenReturn(focusElementLineMock);

    testSubject = new FocusVisualizer(focusVisualizerStylesMock, focusVisualizationCanvasMock);
  }

  @Test
  public void returnsNotNull() {
    Assert.assertNotNull(testSubject);
  }

  @Test
  public void handleAccessibilityFocusEventCreatesElementOnFirstCall() {
    testSubject.HandleAccessibilityFocusEvent(accessibilityEventMock);
    ArrayList<FocusElementHighlight> resultingHighlightList =
        Whitebox.getInternalState(testSubject, "focusElementHighlights");
    Assert.assertEquals(resultingHighlightList.size(), 1);
  }

  @Test
  public void handleAccessibilityFocusEventCreatesLineOnFirstCall() {
    testSubject.HandleAccessibilityFocusEvent(accessibilityEventMock);
    ArrayList<FocusElementLine> resultingLineList =
        Whitebox.getInternalState(testSubject, "focusElementLines");
    Assert.assertEquals(resultingLineList.size(), 1);
  }

  @Test
  public void secondAccessibilityEventSetsPreviousElementNonCurrent() throws Exception {
    FocusVisualizer testSubjectSpy = spy(testSubject);
    testSubjectSpy.HandleAccessibilityFocusEvent(accessibilityEventMock);
    testSubjectSpy.HandleAccessibilityFocusEvent(accessibilityEventMock);

    verifyPrivate(testSubjectSpy, times(1))
        .invoke("setPreviousElementHighlightNonCurrent", any(FocusElementHighlight.class));
  }

  @Test
  public void secondAccessibilityEventSetsPreviousLineNonCurrent() throws Exception {
    FocusVisualizer testSubjectSpy = spy(testSubject);
    testSubjectSpy.HandleAccessibilityFocusEvent(accessibilityEventMock);
    testSubjectSpy.HandleAccessibilityFocusEvent(accessibilityEventMock);

    verifyPrivate(testSubjectSpy, times(1))
        .invoke("setPreviousLineNonCurrent", any(FocusElementLine.class));
  }

  @Test
  public void tabStopCountIncrementsAsExpected() {
    testSubject.HandleAccessibilityFocusEvent(accessibilityEventMock);
    testSubject.HandleAccessibilityFocusEvent(accessibilityEventMock);
    testSubject.HandleAccessibilityFocusEvent(accessibilityEventMock);

    int resultingTabStopCount = Whitebox.getInternalState(testSubject, "tabStopCount");

    Assert.assertEquals(resultingTabStopCount, 3);
  }

  @Test
  public void resetVisualizationsDoesTheJob() {
    testSubject.HandleAccessibilityFocusEvent(accessibilityEventMock);
    testSubject.HandleAccessibilityFocusEvent(accessibilityEventMock);

    testSubject.resetVisualizations();
    ArrayList<FocusElementLine> resultingLineList =
        Whitebox.getInternalState(testSubject, "focusElementLines");
    ArrayList<FocusElementHighlight> resultingHighlightList =
        Whitebox.getInternalState(testSubject, "focusElementHighlights");
    int resultingTabStopCount = Whitebox.getInternalState(testSubject, "tabStopCount");

    Assert.assertEquals(resultingHighlightList.size(), 0);
    Assert.assertEquals(resultingLineList.size(), 0);
    Assert.assertEquals(resultingTabStopCount, 0);
  }
}