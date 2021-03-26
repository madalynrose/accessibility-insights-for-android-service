package com.microsoft.accessibilityinsightsforandroidservice;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

public class AccessibilityTestResultsFiller implements RequestFulfiller {
    private final RootNodeFinder rootNodeFinder;
    private final EventHelper eventHelper;
    private final ResponseWriter responseWriter;
    private final AccessibilityTestScanner accessibilityTestScanner;

    public AccessibilityTestResultsFiller(
            ResponseWriter responseWriter,
            RootNodeFinder rootNodeFinder,
            EventHelper eventHelper,
            AccessibilityTestScanner accessibilityTestScanner) {
        this.responseWriter = responseWriter;
        this.rootNodeFinder = rootNodeFinder;
        this.eventHelper = eventHelper;
        this.accessibilityTestScanner = accessibilityTestScanner;
    }

    public void fulfillRequest(RunnableFunction onRequestFulfilled) {
        try {
            AccessibilityNodeInfo source = eventHelper.claimLastSource();
            AccessibilityNodeInfo rootNode = rootNodeFinder.getRootNodeFromSource(source);

            String content = getScanContent(rootNode);
            responseWriter.writeSuccessfulResponse(content);

            if (rootNode != null && rootNode != source) {
                rootNode.recycle();
            }
            if (source != null && !eventHelper.restoreLastSource(source)) {
                source.recycle();
            }
        } catch (Exception e) {
            responseWriter.writeErrorResponse(e);
        }
        onRequestFulfilled.run();
    }

    @Override
    public boolean isBlockingRequest() {
        return true;
    }

    private String getScanContent(AccessibilityNodeInfo rootNode)
            throws ScanException, ViewChangedException {
        if (rootNode == null) {
            throw new ScanException("Unable to locate root node to scan");
        }
        List<AccessibilityHierarchyCheckResult> result = accessibilityTestScanner.scanWithAccessibilityTestFramework(rootNode);
        if (result == null) {
            throw new ScanException("Scanner returned no data");
        }

        GsonBuilder gsonBuilder = new GsonBuilder().setFieldNamingStrategy(new FieldNamingStrategy() {
            int x = 0;
            @Override
            public String translateName(Field f) {
                return x++ + f.getName();
            }
        });

        Gson gson = gsonBuilder.create();
        String res = gson.toJson(result);
        return res;
    }
}
