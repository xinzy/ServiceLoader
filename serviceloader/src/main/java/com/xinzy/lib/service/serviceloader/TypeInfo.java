package com.xinzy.lib.service.serviceloader;

import android.os.Bundle;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TypeInfo implements Parcelable {
    private static final String TRANSACTION_PREFIX = "TRANSACTION_";
    private static final String DESCRIPTOR = "DESCRIPTOR";

    Class<?> type;
    String binderDescriptor;
    final Map<String, Integer> transactionCodeMap = new HashMap<>();
    final Map<String, MethodInfo> methods = new HashMap<>();

    public TypeInfo(Class<?> clazz) {
        if (!clazz.isPrimitive()
                && clazz != String.class
                && clazz != Bundle.class
                && clazz != boolean[].class
                && clazz != byte[].class
                && clazz != int[].class
                && clazz != long[].class
                && clazz != float[].class
                && clazz != double[].class
                && clazz != char[].class
                && clazz != String[].class
                && clazz != Bundle[].class
                && clazz != List.class
                && clazz != Map.class) {

            if (!IInterface.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Don't support other object type in the parameter: " + clazz);
            }

            this.type = IInterface.class;

            try {
                Class<?> stubClass = Class.forName(clazz.getName() + "$Stub");
                Debug.d("TypeInfo", "stubClass=" + stubClass);

                Field[] stubClassDeclaredFields = stubClass.getDeclaredFields();
                filterField(stubClassDeclaredFields);

                Field[] parentFields = stubClass.getInterfaces()[0].getDeclaredFields();
                filterField(parentFields);

                Method[] methods = clazz.getDeclaredMethods();
                int length = methods.length;

                for(int index = 0; index < length; ++index) {
                    Method method = methods[index];
                    Debug.d("TypeInfo", "method=" + method);
                    String name = method.getName();
                    if (!this.transactionCodeMap.containsKey(name)) {
                        throw new IllegalStateException("Unknown method: " + name);
                    }

                    MethodInfo methodInfo = new MethodInfo(method);
                    this.methods.put(name, methodInfo);
                }
            } catch (ClassNotFoundException | IllegalAccessException var11) {
                throw new RuntimeException(var11);
            }
        } else {
            this.type = clazz;
            this.binderDescriptor = null;
        }
    }

    private void filterField(Field[] fields) throws IllegalAccessException {

        for(int index = 0; index < fields.length; ++index) {
            Field stubField = fields[index];
            String name = stubField.getName();
            Debug.d("TypeInfo", "stubField=" + stubField);

            if (name.startsWith(TRANSACTION_PREFIX)) {
                name = name.substring(TRANSACTION_PREFIX.length());
                stubField.setAccessible(true);
                int code = stubField.getInt(null);
                this.transactionCodeMap.put(name, code);
            } else if (name.equals(DESCRIPTOR)) {
                stubField.setAccessible(true);
                this.binderDescriptor = (String) stubField.get(null);
            }
        }
    }

    public static final Parcelable.Creator<TypeInfo> CREATOR = new Parcelable.Creator<TypeInfo>() {
        public TypeInfo createFromParcel(Parcel in) {
            return new TypeInfo(in);
        }

        public TypeInfo[] newArray(int size) {
            return new TypeInfo[size];
        }
    };

    public TypeInfo(Parcel in) {
        this.readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.type);
        dest.writeString(this.binderDescriptor);
        dest.writeMap(this.transactionCodeMap);
        dest.writeMap(this.methods);
    }

    private void readFromParcel(Parcel source) {
        this.type = (Class)source.readSerializable();
        this.binderDescriptor = source.readString();
        source.readMap(this.transactionCodeMap, TypeInfo.class.getClassLoader());
        source.readMap(this.methods, TypeInfo.class.getClassLoader());
    }

    static final class MethodInfo implements Parcelable {
        String methodName;
        TypeInfo[] methodParams;
        TypeInfo methodReturn;

        public MethodInfo(Method method) {
            methodName = method.getName();
            methodReturn = new TypeInfo(method.getReturnType());
            Class<?>[] paramTypes = method.getParameterTypes();
            methodParams = new TypeInfo[paramTypes.length];

            for(int i = 0; i < paramTypes.length; ++i) {
                Class<?> paramType = paramTypes[i];
                methodParams[i] = new TypeInfo(paramType);
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.methodName);
            dest.writeTypedArray(this.methodParams, flags);
            dest.writeParcelable(this.methodReturn, flags);
        }

        public void readFromParcel(Parcel source) {
            this.methodName = source.readString();
            this.methodParams = source.createTypedArray(TypeInfo.CREATOR);
            this.methodReturn = source.readParcelable(TypeInfo.class.getClassLoader());
        }

        protected MethodInfo(Parcel in) {
            this.methodName = in.readString();
            this.methodParams = in.createTypedArray(TypeInfo.CREATOR);
            this.methodReturn = in.readParcelable(TypeInfo.class.getClassLoader());
        }

        public static final Creator<MethodInfo> CREATOR = new Creator<MethodInfo>() {
            @Override
            public MethodInfo createFromParcel(Parcel source) {
                return new MethodInfo(source);
            }

            @Override
            public MethodInfo[] newArray(int size) {
                return new MethodInfo[size];
            }
        };
    }
}
