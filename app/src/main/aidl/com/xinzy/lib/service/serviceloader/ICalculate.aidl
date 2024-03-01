// ICalculate.aidl
package com.xinzy.lib.service.serviceloader;
import com.xinzy.lib.service.serviceloader.ICallback;
import com.xinzy.lib.service.serviceloader.entity.Param;

interface ICalculate {

    int add(int first, int second);

    int sub(int first, int second);

    int multi(in Param param);

    void setCallback(ICallback callback);
}