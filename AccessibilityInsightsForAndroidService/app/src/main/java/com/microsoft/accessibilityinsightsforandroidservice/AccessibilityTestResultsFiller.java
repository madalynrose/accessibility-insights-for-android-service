package com.microsoft.accessibilityinsightsforandroidservice;

import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.Nullable;

import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck;
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult;
import com.google.android.apps.common.testing.accessibility.framework.Parameters;
import com.google.android.apps.common.testing.accessibility.framework.ViewHierarchyElementUtils;
import com.google.android.apps.common.testing.accessibility.framework.suggestions.FixSuggestion;
import com.google.android.apps.common.testing.accessibility.framework.suggestions.FixSuggestionPreset;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy;
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchyAndroid;
import com.google.android.apps.common.testing.accessibility.framework.uielement.DeviceStateAndroid;
import com.google.android.apps.common.testing.accessibility.framework.uielement.DisplayInfoAndroid;
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElementAndroid;
import com.google.android.apps.common.testing.accessibility.framework.uielement.WindowHierarchyElementAndroid;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

public class AccessibilityTestResultsFiller implements RequestFulfiller {
    private final RootNodeFinder rootNodeFinder;
    private final EventHelper eventHelper;
    private final ResponseWriter responseWriter;
    private final AccessibilityTestScanner accessibilityTestScanner;
    private final AxeViewsFactory axeViewsFactory =
            new AxeViewsFactory(
                    new NodeViewBuilderFactory(),
                    new AccessibilityNodeInfoQueueBuilder(new AccessibilityNodeInfoSorterFactory()));

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
        List<AccessibilityHierarchyCheckResult> results = accessibilityTestScanner.scanWithAccessibilityTestFramework(rootNode);
        if (results == null) {
            throw new ScanException("Scanner returned no data");
        }

        Gson gson = createAccessiblityTestAndroidGson();
        return gson.toJson(results);
    }

    String getSuggestion(AccessibilityHierarchyCheckResult checkResult){
            ImmutableList<FixSuggestion> suggestions =  FixSuggestionPreset.provideFixSuggestions(checkResult, checkResult.getElement().getWindow().getAccessibilityHierarchy(), null);
            StringBuilder suggestionText = new StringBuilder();
            for(FixSuggestion suggestion: suggestions)
                suggestionText.append(suggestion.getDescription(Locale.getDefault()));
            return suggestionText.toString();
        }


    public Gson createAccessiblityTestAndroidGson() {

        GsonBuilder customGson = new GsonBuilder();
        JsonSerializer<DisplayInfoAndroid> displayInfoAndroidJsonSerializer = (src, typeOfSrc, context) -> {
            JsonObject jsonResult = new JsonObject();
            Gson gson = customGson.create();
            jsonResult.addProperty("realMetrics", gson.toJson(src.getRealMetrics()));
            jsonResult.addProperty("metricsWithoutDecoration", gson.toJson(src.getMetricsWithoutDecoration().toString()));
            return jsonResult;
        };
        customGson.registerTypeAdapter(DisplayInfoAndroid.class, displayInfoAndroidJsonSerializer);
        JsonSerializer<DeviceStateAndroid> deviceStateAndroidJsonSerializer = (src, typeOfSrc, context) -> {
            JsonObject jsonResult = new JsonObject();
            jsonResult.addProperty("defaultDisplayInfo", customGson.create().toJson(src.getDefaultDisplayInfo()));

            return jsonResult;
        };
        customGson.registerTypeAdapter(DeviceStateAndroid.class, deviceStateAndroidJsonSerializer);

        JsonSerializer<AccessibilityHierarchyAndroid> accessibilityHierarchyAndroidJsonSerializer = (src, typeOfSrc, context) -> {
            JsonObject jsonResult = new JsonObject();
            jsonResult.addProperty("activeWindow", String.valueOf(src.getActiveWindow())); //we could translate this to an AxeView which is JsonSerializable already and has the same attributes

            return jsonResult;
        };
        customGson.registerTypeAdapter(AccessibilityHierarchyAndroid.class, accessibilityHierarchyAndroidJsonSerializer);
        JsonSerializer<WindowHierarchyElementAndroid> windowHierarchyElementAndroidJsonSerializer = (src, typeOfSrc, context) -> {
            JsonObject jsonResult = new JsonObject();
            Gson gson = customGson.create();
            jsonResult.addProperty("accessibilityHierarchy", gson.toJson(src.getAccessibilityHierarchy()));
            return jsonResult;
        };
        customGson.registerTypeAdapter(WindowHierarchyElementAndroid.class, windowHierarchyElementAndroidJsonSerializer);
        JsonSerializer<ViewHierarchyElementAndroid> viewHierarchyElementAndroidJsonSerializer = (src, typeOfSrc, context) -> {
            JsonObject jsonResult = new JsonObject();
            Gson gson = customGson.create();
            jsonResult.addProperty("speakableText", ViewHierarchyElementUtils.getSpeakableTextForElement(src).toString());
            jsonResult.addProperty("shouldFocusView", ViewHierarchyElementUtils.shouldFocusView(src));
            jsonResult.addProperty("isPotentiallyObscured", ViewHierarchyElementUtils.isPotentiallyObscured(src));
            jsonResult.addProperty("windowElement", gson.toJson(src.getWindow()));


            return jsonResult;
        };
        customGson.registerTypeAdapter(ViewHierarchyElementAndroid.class, viewHierarchyElementAndroidJsonSerializer);
        customGson.setPrettyPrinting();
        customGson.registerTypeAdapterFactory(new AccessibilityHierarchyCheckAdapterFactory());

        class AccessibilityHierarchyCheckResultAdapterFactory implements TypeAdapterFactory {
            @SuppressWarnings("unchecked")
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                if (!AccessibilityHierarchyCheckResult.class.isAssignableFrom(type.getRawType()))
                    return null;

                return (TypeAdapter<T>) new AccessibilityHierarchyCheckResultAdapter();
            }

            class AccessibilityHierarchyCheckResultAdapter extends TypeAdapter<AccessibilityHierarchyCheckResult> {
                @Override
                public void write(JsonWriter out, AccessibilityHierarchyCheckResult value) throws IOException {
                    Gson gson = customGson.create();
                    String suggestion = getSuggestion(value);
                    out.beginObject();
                    out.name("checkClass").value(String.valueOf(value.getSourceCheckClass()));
                    out.name("type").value(String.valueOf(value.getType()));
                    out.name("message").value(String.valueOf(value.getMessage(Locale.getDefault())));
                    out.name("resultId").value(value.getResultId());
                    out.name("element").value(gson.toJson(value.getElement()));
                    if(!suggestion.isEmpty()){
                        out.name("suggestion").value(suggestion);
                    }
                    out.endObject();
                }

                @Override
                public AccessibilityHierarchyCheckResult read(JsonReader in) throws IOException {
                    return null;
                }
            }

        }
        customGson.registerTypeAdapterFactory(new AccessibilityHierarchyCheckResultAdapterFactory());
        return customGson.create();
    }

    class AccessibilityHierarchyCheckAdapterFactory implements TypeAdapterFactory {
        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (!AccessibilityHierarchyCheck.class.isAssignableFrom(type.getRawType())) return null;

            return (TypeAdapter<T>) new AccessibilityHierarchyCheckAdapter();
        }

        class AccessibilityHierarchyCheckAdapter extends TypeAdapter<AccessibilityHierarchyCheck> {
            @Override
            public void write(JsonWriter out, AccessibilityHierarchyCheck value) throws IOException {
                out.beginObject();
                out.name("helpUrl").value(value.getHelpUrl());
                out.name("category").value(String.valueOf(value.getCategory()));
                out.name("titleMessage").value(value.getTitleMessage(Locale.getDefault()));
                out.endObject();
            }

            @Override
            public AccessibilityHierarchyCheck read(JsonReader in) throws IOException {
                return null;
            }
        }
    }


}