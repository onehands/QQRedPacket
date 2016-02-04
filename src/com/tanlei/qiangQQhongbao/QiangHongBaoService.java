package com.tanlei.qiangQQhongbao;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;

public class QiangHongBaoService extends AccessibilityService {

    static final String TAG = "QiangHongBao";

    /** 微信的包名 */
    static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    /** 红包消息的关键字 */
    static final String HONGBAO_TEXT_KEY = "[QQ红包]";
    static List<String> fetchedIdentifiers = new ArrayList<String>();

    static boolean flag = true;

    Handler handler = new Handler();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        Log.d(TAG, "事件---->" + event);

        // 通知栏事件
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            List<CharSequence> texts = event.getText();
            if (!texts.isEmpty()) {
                for (CharSequence t : texts) {
                    String text = String.valueOf(t);
                    if (text.contains(HONGBAO_TEXT_KEY)) {
                        openNotify(event);
                        break;
                    }
                }
            }
        } else if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED || eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            openHongBao(event);
        }
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "中断抢红包服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Toast.makeText(this, "连接抢红包服务", Toast.LENGTH_SHORT).show();
    }

    private void sendNotifyEvent() {
        AccessibilityManager manager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        if (!manager.isEnabled()) {
            return;
        }
        AccessibilityEvent event = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
        event.setPackageName(WECHAT_PACKAGENAME);
        event.setClassName(Notification.class.getName());
        CharSequence tickerText = HONGBAO_TEXT_KEY;
        event.getText().add(tickerText);
        manager.sendAccessibilityEvent(event);
    }

    /** 打开通知栏消息 */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openNotify(AccessibilityEvent event) {
        if (event.getParcelableData() == null || !(event.getParcelableData() instanceof Notification)) {
            return;
        }
        // 以下是精华，将微信的通知栏消息打开
        Notification notification = (Notification) event.getParcelableData();
        PendingIntent pendingIntent = notification.contentIntent;
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openHongBao(AccessibilityEvent event) {
        checkKey2();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkKey1() {
        // android.os.Debug.waitForDebugger();
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            return;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByText("拆红包");
        for (AccessibilityNodeInfo n : list) {
            n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            performGlobalAction(GLOBAL_ACTION_BACK);
        }
        performGlobalAction(GLOBAL_ACTION_BACK);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkKey2() {
        if (!flag) {
            return;
        }
        synchronized (this) {
            if (flag) {
                flag = false;
            }
        }
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();

        if (nodeInfo == null) {
            Log.w(TAG, "rootWindow为空");
            flag = true;
            return;
        }
        int i1 = 1;
        int j1 = 1;
        int k1 = 1;
        List<AccessibilityNodeInfo> listHBB2 = nodeInfo.findAccessibilityNodeInfosByText("搜索");
        AccessibilityNodeInfo parent = listHBB2.get(1).getParent().getParent();
        List<AccessibilityNodeInfo> listOpen = nodeInfo.findAccessibilityNodeInfosByText("点击拆开");
        if (!listOpen.isEmpty()) {
            for (int i = listOpen.size() - 1; i >= 0; i++) {
                AccessibilityNodeInfo n2 = listOpen.get(i);
                n2.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                performGlobalAction(GLOBAL_ACTION_BACK);
                return;
            }
        }

        List<AccessibilityNodeInfo> list4 = nodeInfo.findAccessibilityNodeInfosByText("[QQ红包]");
        if (list4 != null && list4.size() > 0) {
            for (AccessibilityNodeInfo accessibilityNodeInfo : list4) {
                accessibilityNodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }

        List<AccessibilityNodeInfo> list3 = nodeInfo.findAccessibilityNodeInfosByText("红包记录");
        if (list3 != null && list3.size() > 0) {
            performGlobalAction(GLOBAL_ACTION_BACK);
            flag = true;
            return;
        }

        flag = true;
    }

    /**
     * 添加方法注释.
     * 
     * @param parent
     * @return .
     */
    private String getHongbaoHash(AccessibilityNodeInfo node) {

        /* 获取红包上的文本 */
        String content;
        try {
            node.hashCode();
            // AccessibilityNodeInfo i = node.getParent().getChild(0);
            content = node.getViewIdResourceName().toString();
        } catch (NullPointerException npr) {
            return null;
        }

        return content + "@" + getNodeId(node);

    }

    /**
     * 获取节点对象唯一的id，通过正则表达式匹配 AccessibilityNodeInfo@后的十六进制数字
     *
     * @param node
     *            AccessibilityNodeInfo对象
     * @return id字符串
     */
    private String getNodeId(AccessibilityNodeInfo node) {
        /* 用正则表达式匹配节点Object */
        Pattern objHashPattern = Pattern.compile("(?<=@)[0-9|a-z]+(?=;)");
        Matcher objHashMatcher = objHashPattern.matcher(node.toString());

        // AccessibilityNodeInfo必然有且只有一次匹配，因此不再作判断
        objHashMatcher.find();

        return objHashMatcher.group(0);
    }

}
