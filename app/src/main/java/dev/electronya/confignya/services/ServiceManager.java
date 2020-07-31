package dev.electronya.confignya.services;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.util.ArrayList;

public class ServiceManager {
    Context mContext;
    NsdManager mNsdManager;
    NsdManager.DiscoveryListener mDiscoveryListener;
    NsdManager.ResolveListener mResolveListener;
    ArrayList<NsdServiceInfo> mServices;
    final String TAG = "ServiceManager";

    public static final String PIRBLASTER_SVC_TYPE = "_pirblaster._tcp";

    public ServiceManager(Context context) {
        mContext = context;
        mNsdManager = (NsdManager)context.getSystemService(Context.NSD_SERVICE);
    }

    private void initDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onDiscoveryStarted(String serviceType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onServiceFound(NsdServiceInfo serviceInfo) {
                // A service was found! Do something with it.
                Log.d(TAG, "Service discovery success" + serviceInfo);
                mNsdManager.resolveService(serviceInfo, mResolveListener);
            }

            @Override
            public void onServiceLost(NsdServiceInfo serviceInfo) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost: " + serviceInfo);

                for (NsdServiceInfo service : mServices) {
                    if (service.equals(serviceInfo)) {
                        mServices.remove(service);
                    }
                }
            }
        };
    }

    private void initResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "resolving service failed: Error code: " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                mServices.add(serviceInfo);
            }
        };
    }

    public void init() {
        initResolveListener();
    }

    public void discoverServices(String serviceType) {
        stopDiscovery();
        initDiscoveryListener();
        mNsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void stopDiscovery() {
        if (mDiscoveryListener != null) {
            try {
                mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            } finally {

            }
            mDiscoveryListener = null;
        }
    }

    public ArrayList<NsdServiceInfo> getServices() {
        return mServices;
    }
}
