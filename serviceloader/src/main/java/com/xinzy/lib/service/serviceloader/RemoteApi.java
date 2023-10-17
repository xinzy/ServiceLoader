package com.xinzy.lib.service.serviceloader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RemoteApi {

    private ConnectionMonitor connectionMonitor;

    private int targetVersion;
    private IApiDescriptor targetService;
    private IBinder targetRemote;
    private TypeInfo targetTypeInfo;


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                String desc = service.getInterfaceDescriptor();
                if (TextUtils.isEmpty(desc)) {
                    throw new RemoteException("Binder interface descriptor is empty. ");
                } else if (!desc.equals(IApiDescriptor.class.getName())) {
                    throw new RuntimeException("Failed to init a remote api, get wrong type: " + desc);
                } else {
                    targetService = IApiDescriptor.Stub.asInterface(service);
                    targetVersion = targetService.getVersion();
                    targetRemote = targetService.getInterface();
                    targetTypeInfo = targetService.getTypeInfo();

                    if (connectionMonitor != null) {
                        connectionMonitor.onConnected(RemoteApi.this);
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (connectionMonitor != null) {
                connectionMonitor.onDisconnected(RemoteApi.this);
            }
        }
    };


    private RemoteApi(Context context, Intent service, ConnectionMonitor connectionMonitor) {
        this.connectionMonitor = connectionMonitor;
        context.bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public static RemoteApi create(Context context, Intent service, ConnectionMonitor connectionMonitor) {
        return new RemoteApi(context, service, connectionMonitor);
    }

    public <T> T asInterface(Class<T> clazz) {
        BinderInvocationHandler handler = new BinderInvocationHandler(targetRemote, targetTypeInfo);
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{ clazz }, handler);
    }

    public static Object readFromParcel(Parcel p, Class<?> type) {
        if (type.isPrimitive()) {
            if (type == Boolean.TYPE) {
                return p.readInt() != 0;
            } else if (type == Byte.TYPE) {
                return p.readByte();
            } else if (type == Integer.TYPE) {
                return p.readInt();
            } else if (type == Long.TYPE) {
                return p.readLong();
            } else if (type == Float.TYPE) {
                return p.readFloat();
            } else if (type == Double.TYPE) {
                return p.readDouble();
            } else if (type == Void.TYPE) {
                return null;
            } else {
                throw new IllegalArgumentException("Unknown primitive type: " + type.getSimpleName() + "!");
            }
        } else if (type == String.class) {
            return p.readString();
        } else if (type == Bundle.class) {
            return 0 != p.readInt() ? Bundle.CREATOR.createFromParcel(p) : null;
        } else if (type == List.class) {
            return p.readArrayList(RemoteApi.class.getClassLoader());
        } else if (type == Map.class) {
            return p.readHashMap(RemoteApi.class.getClassLoader());
        } else {
            throw new IllegalArgumentException("Unknown type: " + type.getSimpleName() + "!");
        }
    }

    public static void writeToParcel(Parcel p, TypeInfo info, Class<?> type, Object value) {
        if (type.isPrimitive()) {
            if (type == Boolean.TYPE) {
                p.writeInt((Boolean)value ? 1 : 0);
            } else if (type == Byte.TYPE) {
                p.writeByte((Byte)value);
            } else if (type == Integer.TYPE) {
                p.writeInt((Integer)value);
            } else if (type == Long.TYPE) {
                p.writeLong((Long)value);
            } else if (type == Float.TYPE) {
                p.writeFloat((Float)value);
            } else if (type == Double.TYPE) {
                p.writeDouble((Double)value);
            } else if (type != Void.TYPE) {
                throw new IllegalArgumentException("Unknown primitive type: " + type.getSimpleName() + "!");
            }
        } else if (type.isArray()) {
            if (type == boolean[].class) {
                p.writeBooleanArray((boolean[])value);
            } else if (type == byte[].class) {
                p.writeByteArray((byte[])value);
            } else if (type == int[].class) {
                p.writeIntArray((int[])value);
            } else if (type == long[].class) {
                p.writeLongArray((long[])value);
            } else if (type == float[].class) {
                p.writeFloatArray((float[])value);
            } else if (type == double[].class) {
                p.writeDoubleArray((double[])value);
            } else if (type == char[].class) {
                p.writeCharArray((char[])value);
            } else if (type == String[].class) {
                p.writeStringArray((String[])value);
            } else {
                if (type != Bundle[].class) {
                    throw new IllegalArgumentException("Unknown array type: " + type.getSimpleName() + "!");
                }

                p.writeTypedArray((Bundle[])value, 0);
            }
        } else if (type == String.class) {
            p.writeString((String)value);
        } else if (type == Bundle.class) {
            Bundle data = (Bundle)value;
            if (data != null) {
                p.writeInt(1);
                data.writeToParcel(p, 0);
            } else {
                p.writeInt(0);
            }
        } else if (type == List.class) {
            p.writeList((List)value);
        } else if (type == Map.class) {
            p.writeMap((Map)value);
        } else if (type.isInterface()) {
            SuperStub stub = new SuperStub(info, value);
            p.writeStrongBinder(stub);
        }
    }

    private static class BinderInvocationHandler implements InvocationHandler {
        private final IBinder remote;
        private final TypeInfo typeInfo;

        public BinderInvocationHandler(IBinder remote, TypeInfo typeInfo) {
            this.remote = remote;
            this.typeInfo = typeInfo;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            Class<?>[] paramTypes = method.getParameterTypes();
            Class<?> returnType = method.getReturnType();

            switch (name) {
                case "toString":
                    return this.toString();

                case "hashCode":
                    return this.hashCode();

                default:
                    if (remote != null && typeInfo != null && remote.isBinderAlive()) {
                        if (!typeInfo.transactionCodeMap.containsKey(name)) {
                            if (Debug.isDebug) {
                                throw new IllegalArgumentException("Remote doesn't have method: " + name);
                            }
                        }

                        int code = typeInfo.transactionCodeMap.get(name);
                        TypeInfo.MethodInfo methodInfo = typeInfo.methods.get(name);

                        if (paramTypes.length != methodInfo.methodParams.length) {
                            if (Debug.isDebug) {
                                throw new IllegalArgumentException("Parameters number doesn't match the remote");
                            }
                        }

                        Parcel data = Parcel.obtain();
                        Parcel reply = Parcel.obtain();

                        try {
                            data.writeInterfaceToken(typeInfo.binderDescriptor);

                            for (int i = 0; i < paramTypes.length; i++) {
                                RemoteApi.writeToParcel(data, methodInfo.methodParams[i], paramTypes[i], args[i]);
                            }
                            remote.transact(code, data, reply, 0);

                            reply.readException();
                            return RemoteApi.readFromParcel(reply, returnType);
                        } catch (DeadObjectException e) {
                            return emptyResult(returnType);
                        } catch (RuntimeException e) {
                            throw getException(method, e);
                        } finally {
                            reply.recycle();
                            data.recycle();
                        }
                    } else {
                        return emptyResult(returnType);
                    }
            }
        }

        private Exception getException(Method method, Exception exception) throws RemoteException {
            StackTraceElement[] stackTrace = exception.getStackTrace();
            StackTraceElement[] trimStackTrace = (StackTraceElement[]) Arrays.copyOfRange(stackTrace, 4, stackTrace.length);
            trimStackTrace[0] = new StackTraceElement(remote.getInterfaceDescriptor(), method.getName(), null, -1);
            exception.setStackTrace(trimStackTrace);
            return exception;
        }

        private Object emptyResult(Class<?> returnType) {
            if (returnType.isPrimitive()) {
                if (returnType == Boolean.TYPE) {
                    return false;
                }

                if (returnType == Byte.TYPE) {
                    return 0;
                }

                if (returnType == Integer.TYPE) {
                    return 0;
                }

                if (returnType == Long.TYPE) {
                    return 0L;
                }

                if (returnType == Float.TYPE) {
                    return 0.0F;
                }

                if (returnType == Double.TYPE) {
                    return 0.0;
                }

                if (returnType != Void.TYPE) {
                    throw new IllegalArgumentException("Unknown primitive type: " + returnType.getSimpleName() + "!");
                }
            }
            return null;
        }
    }

    static class SuperStub extends Binder implements IInterface {
        private final TypeInfo typeInfo;
        private final Object stub;

        public SuperStub(TypeInfo typeInfo, Object stub) {
            this.typeInfo = typeInfo;
            this.stub = stub;
            this.attachInterface(this, this.typeInfo.binderDescriptor);
        }

        @Override
        public IBinder asBinder() {
            return this;
        }

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == IBinder.LAST_CALL_TRANSACTION) {
                reply.writeString(getInterfaceDescriptor());
            } else {
                Iterator<Map.Entry<String, Integer>> iterator = typeInfo.transactionCodeMap.entrySet().iterator();

                Map.Entry<String, Integer> entity;
                do {
                    if (!iterator.hasNext()) {
                        return super.onTransact(code, data, reply, flags);
                    }
                    entity = iterator.next();
                } while (code != entity.getValue());

                String name = entity.getKey();

                data.enforceInterface(getInterfaceDescriptor());
                TypeInfo.MethodInfo methodInfo = typeInfo.methods.get(name);
                TypeInfo returnInfo = methodInfo.methodReturn;
                Object[] values = new Object[methodInfo.methodParams.length];
                Class<?>[] paramTypes = new Class[methodInfo.methodParams.length];

                for (int i = 0; i < methodInfo.methodParams.length; i++) {
                    TypeInfo info = methodInfo.methodParams[i];
                    if (info.binderDescriptor != null) {
                        throw new IllegalArgumentException("Don't support use aidl in callback");
                    }

                    Class<?> clazz = info.type;
                    paramTypes[i] = clazz;
                    Object value = RemoteApi.readFromParcel(data, clazz);
                    values[i] = value;
                }


                Object result;
                try {
                    Method method = stub.getClass().getDeclaredMethod(name, paramTypes);
                    result = method.invoke(stub, values);
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    if (e.getTargetException() instanceof RuntimeException) {
                        throw (RuntimeException) e.getTargetException();
                    }
                    throw new IllegalStateException(e.getTargetException());
                }

                reply.writeNoException();
                RemoteApi.writeToParcel(reply, returnInfo, returnInfo.type, result);
            }
            return true;
        }
    }

    public interface ConnectionMonitor {
        void onConnected(RemoteApi api);

        void onDisconnected(RemoteApi api);
    }
}
