package com.jimi.app.mvvm.modules.fence

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import com.gyf.immersionbar.ImmersionBar
import com.jimi.app.common.C
import com.jimi.app.common.GlobalData
import com.jimi.app.R
import com.jimi.app.common.*
import com.jimi.app.databinding.DeviceFenceAddActivityBinding
import com.jimi.app.entitys.GEOBean
import com.jimi.app.entitys.SignDevice
import com.jimi.app.modules.device.DelayAlarmSettingDialog
import com.jimi.app.modules.device.DelayAlarmSettingDialog.OnDelayAlarmSettingListener
import com.jimi.app.modules.device.RelevanceUserActivity
import com.jimi.app.modules.device.adapter.SearchResultAdapter
import com.jimi.app.mvvm.base.BaseActivity
import com.jimi.app.mvvm.utils.DateUtil
import com.jimi.app.mvvm.utils.DateUtil.YYYY_MM_DD_HH_MM_SS
import com.jimi.app.protocol.ServiceApi
import com.jimi.app.utils.*
import com.jimi.app.views.CustomDialog
import com.jimi.app.views.MapControlView
import com.jimi.app.views.NavigationView
import com.jimi.app.views.pulltorefresh.PullToRefreshBase
import com.jimi.map.*
import com.jimi.map.Map
import com.jimi.map.listener.OnCameraChangeListener
import com.jimi.map.listener.OnMapLoadedCallback
import com.jimi.map.listener.OnMapReadyCallback
import com.jimi.map.listener.OnSearchResultListener
import de.greenrobot.event.EventBus
import kotlinx.android.synthetic.main.common_search_bar.*
import kotlinx.android.synthetic.main.common_seekbar_zoom_in_out.*
import kotlinx.android.synthetic.main.device_fence_add_activity.*
import kotlinx.android.synthetic.main.device_fence_add_foot.*
import kotlinx.android.synthetic.main.device_map_foot_search_bar_layout.*
import kotlinx.android.synthetic.main.layout_search.*
import java.lang.ref.WeakReference
import java.util.*

/**
 * 新建或编辑围栏
 */
class FenceAddOrEditActivity() : BaseActivity<FenceApplyViewModel, DeviceFenceAddActivityBinding>(), OnMapReadyCallback, OnMapLoadedCallback, OnDelayAlarmSettingListener {
    private var locationFlag = false

    /**
     * 围栏的最小半径
     */
    private val MIN = 200

    /**
     * 围栏最大半径
     */
    private val MAX = 10000

    /**
     * 点击加减号时的单位增量
     */
    private val UNIT = 50
    private lateinit var mMap: Map
    private var mMarkerStart: MyMarker? = null
    private var mDeviceDescriptor: MyBitmapDescriptor? = null
    private var mAnimationIn: Animation? = null
    private lateinit var mAnimationout: Animation
    private var mAdapter: SearchResultAdapter? = null

    /**
     * 保存围栏相关的参数
     */
    private var mLatLng //位置
            : MyLatLng? = null
    private var mDeviceLatLng //设备位置
            : MyLatLng? = null
    var mFenceName = "" //围栏名称
    var mFenceID = "" //围栏id
    var mAllImei = "" //围栏id
    var mImeis = ArrayList<String?>()
    private var mImei: String? = null
    private val mLat = MyLatLng(0.0, 0.0)
    private var mRadius = 0
    private var mScale = 16 //默认16比较好看
    private var isEditFence = false
    private var delayAlarmSettingDialog: DelayAlarmSettingDialog? = null
    private var delayIn = 0
    private var delayOut = 0
    private var buryingSource = 0
    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            if (s.toString() != "") {
                tv_search!!.visibility = View.VISIBLE
            } else {
                tv_search!!.visibility = View.GONE
            }
        }
    }
    private var mHandler: MyHandler? = null

    internal class MyHandler(activity: FenceAddOrEditActivity) : Handler() {
        //注意下面的“PopupActivity”类是MyHandler类所在的外部类，即所在的activity
        var mActivity: WeakReference<FenceAddOrEditActivity> = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            val theActivity = mActivity.get()
            if (msg.what == 1) { //位置搜索无结果时，间隔十秒关闭加载框
                theActivity!!.closeProgressDialog()

                theActivity.showToast(LanguageUtil.getInstance().getString(LanguageHelper.FENCE_SEARCH_NO_RESULT))
                return
            }
            removeMessages(0)
            theActivity!!.zoom(MapUtil.metersToEquatorPixels(theActivity.mMap, theActivity.mLatLng, theActivity.fence_view!!.mRadiusText.toFloat()).toFloat())
        }

    }

    private var mCancleImeis = ArrayList<String>()
    private var mGeobean: GEOBean? = null
    private var userIds = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLocationPermission() //检查定位权限
        buryingSource = intent.getIntExtra("source", 0)
        mHandler = MyHandler(this)
        mMap = Map() //初始化地图
        mMap.getMap(this, map, savedInstanceState)
        mMap.setOnMapReadyCallback(this)
        control_view!!.setLocationCallback(MapControlView.LocationCallback {
            if (GlobalData.getLatLng() != null && null != mDeviceLatLng) {
                if (locationFlag) {
                    moveAnimateCamera(mDeviceLatLng)
                    locationFlag = false
                } else {
                    moveAnimateCamera(GlobalData.getLatLng())
                    locationFlag = true
                }
            } else if (mDeviceLatLng != null && mDeviceLatLng!!.mLatLng.longitude != 0.0 && mDeviceLatLng!!.mLatLng.latitude != 0.0) {
                moveAnimateCamera(mDeviceLatLng)
            } else if (GlobalData.getLatLng() != null) {
                moveAnimateCamera(GlobalData.getLatLng())
            }
        })
    }

    override fun initView() {
        device_fence_my_location!!.text = LanguageUtil.getInstance().getString(LanguageHelper.COMMON_MY_LOCATION)
        device_fence_save!!.text = LanguageUtil.getInstance().getString(LanguageHelper.COMMON_SAVE_TEXT)
        device_fence_alarm_out!!.text = LanguageUtil.getInstance().getString(LanguageHelper.COMMON_OUT_TEXT)
        device_fence_alarm_in!!.text = LanguageUtil.getInstance().getString(LanguageHelper.COMMON_IN_TEXT)
        device_application_list_item_history!!.text = LanguageUtil.getInstance().getString(LanguageHelper.COMMON_ALARM_IN_OUT_TEXT)
        common_search_text!!.hint = LanguageUtil.getInstance().getString(LanguageHelper.DEVICE_SEARCH_ADDR_WORDS)
        common_search_button!!.text = LanguageUtil.getInstance().getString(LanguageHelper.DEVICE_SEARCH_WORD)
        device_fence_edit_name!!.text = LanguageUtil.getInstance().getString(LanguageHelper.FENCE_NAME_EDIT)
        initSeekBar()
        /** 动画  */
        mAnimationIn = AnimationUtils.loadAnimation(this, R.anim.misc_in)
        mAnimationout = AnimationUtils.loadAnimation(this, R.anim.misc_out)
        mAnimationout.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                // TODO Auto-generated method stub
            }

            override fun onAnimationRepeat(animation: Animation) {
                // TODO Auto-generated method stub
            }

            override fun onAnimationEnd(animation: Animation) {
                // TODO Auto-generated method stubs
                search_result!!.visibility = View.GONE
            }
        })
        setStatusBarView(status_bar_view)
        seekbar_layout!!.setPadding(0, ImmersionBar.getStatusBarHeight(this), 0, 0)
    }

    override fun initClick() {
        common_search_text!!.addTextChangedListener(watcher)
        if (common_search_text!!.text.toString().trim { it <= ' ' } != "") {
            tv_search!!.visibility = View.VISIBLE
        } else {
            tv_search!!.visibility = View.GONE
        }
        common_search_text!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                common_search_text!!.setText("")
            }
        })
        common_search_text!!.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View, hasFocus: Boolean) {
                if (hasFocus) {
                    if (TextUtils.isEmpty(mFenceID)) {
                        BuryingPointUtils.onEvent(this@FenceAddOrEditActivity, "c_app_tqzx_wd_tjwlym_sssr")
                    } else {
                        BuryingPointUtils.onEvent(this@FenceAddOrEditActivity, "c_app_tqzx_wd_bjwlym_sssr")
                    }
                }
            }
        }
        device_fence_alarm_in!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (TextUtils.isEmpty(mFenceID)) {
                    BuryingPointUtils.onEvent(this@FenceAddOrEditActivity, "c_app_tqzx_wd_tjwlym_gjjgx")
                } else {
                    BuryingPointUtils.onEvent(this@FenceAddOrEditActivity, "c_app_tqzx_wd_bjwlym_gjjgx")
                }
            }
        })
        device_fence_alarm_out!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (TextUtils.isEmpty(mFenceID)) {
                    BuryingPointUtils.onEvent(this@FenceAddOrEditActivity, "c_app_tqzx_wd_tjwlym_gjcgx")
                } else {
                    BuryingPointUtils.onEvent(this@FenceAddOrEditActivity, "c_app_tqzx_wd_bjwlym_gjcgx")
                }
            }
        })
        search_result_background!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                closeDailog()
            }
        })
        common_search_back.setOnClickListener() {
            if (TextUtils.isEmpty(mFenceID)) {
                BuryingPointUtils.onEvent(this, "c_app_tqzx_wd_tjwlym_fhan")
            } else {
                BuryingPointUtils.onEvent(this, "c_app_tqzx_wd_bjwlym_fhan")
            }
            finish()
        }
        device_fence_edit_name.setOnClickListener() {
            if (TextUtils.isEmpty(mFenceID)) {
                BuryingPointUtils.onEvent(this, "c_app_tqzx_wd_tjwlym_bjmcan")
            } else {
                BuryingPointUtils.onEvent(this, "c_app_tqzx_wd_bjwlym_bjmcan")
            }
            changeFenceName()
        }
        device_fence_add_device.setOnClickListener() {
            if (!TextUtils.isEmpty(mFenceID) && (mGeobean != null) && mGeobean!!.getFenceType()) {
                val vBundle = Bundle()
                vBundle.putString("fenceId", mFenceID)
                intent = Intent()
                intent.setClass(this@FenceAddOrEditActivity, RelevanceUserActivity::class.java)
                intent.putExtras(vBundle)
                startActivityForResult(intent, RelevanceUserActivity.REQUEST_RELEVANCE_CODE)
            } else {
                //跳转到添加设备页面
                BuryingPointUtils.onFenceEvent(this, buryingSource, 1)
                val vBundle = Bundle()
                vBundle.putStringArrayList(C.key.ACTION_IMEIS, mImeis)
                vBundle.putBoolean("isQueryNoExpired", true)
                vBundle.putInt("source", buryingSource)
                intent = Intent()
                intent.setClass(this@FenceAddOrEditActivity, RelevanceUserActivity::class.java)
                intent.putExtras(vBundle)
                startActivityForResult(intent, REQUESTCODE)
            }
        }
            device_fence_save.setOnClickListener() {
                if (TextUtils.isEmpty(mFenceID)) {
                    BuryingPointUtils.onEvent(this, "c_app_tqzx_wd_tjwlym_bcan")
                } else {
                    BuryingPointUtils.onEvent(this, "c_app_tqzx_wd_bjwlym_bcan")
                }
                if ((mLatLng == null) || (fenceRadius() < MIN) || (fenceRadius() > MAX)) {
                    /*R.string.no_position_info*/
                    showToast(LanguageUtil.getInstance().getString(LanguageHelper.NO_POSITION_INFO))
                }else {
                    saveFence() //完成，保存围栏
                }
            }
            tv_search.setOnClickListener() { searchKeyword() }
            delay_alarm_setting.setOnClickListener() {
                delayIn = if (device_fence_alarm_in!!.isChecked) delayIn else 0
                delayOut = if (device_fence_alarm_out!!.isChecked) delayOut else 0
                delayAlarmSettingDialog!!.setFenceStatus(device_fence_alarm_in!!.isChecked, device_fence_alarm_out!!.isChecked)
                delayAlarmSettingDialog!!.setFenceDelay(delayIn, delayOut)
                if (!delayAlarmSettingDialog!!.isShowing) {
                    delayAlarmSettingDialog!!.show()
                }
            }
        }

        override fun initData() {
            intentDatas()
        }

        override fun initVM() {
            vm.saveGeoTagFaile.observe(this, androidx.lifecycle.Observer {
                if (it.code == 5019) {
                    showToast(LanguageUtil.getInstance().getString(LanguageHelper.FENCE_ALARM_SET_FAIL), 5000)
                    this.setResult(RESULTCODE)
                    finish()
                } else if (it.code == 5022) {
                    showToast("围栏关联用户不能超过20个")
                } else if (it.code == 5023) {
                    showToast("围栏最多关联设备数量1000个")
                } else {
                    showToast(RetCode.getCodeMsg(this, it.code))
                }
            })
            vm.saveGeoTagSuccess.observe(this, androidx.lifecycle.Observer {
                this.setResult(RESULTCODE)
                finish()
            })
        }

        private fun initSeekBar() {
            fence_minus!!.text = String.format(LanguageUtil.getInstance().getString(LanguageHelper.FENCE_MIN_METER), MIN)
            fence_plus!!.text = String.format(LanguageUtil.getInstance().getString(LanguageHelper.FENCE_MIN_METER), MAX)
            fence_plus!!.setOnClickListener {
                var progress = fenceRadius() + UNIT
                if (progress > MAX) progress = MAX
                setFenceRadius(progress)
            }
            fence_minus!!.setOnClickListener {
                var progress = fenceRadius() - UNIT
                if (progress < MIN) progress = MIN
                setFenceRadius(progress)
            }
            fence_progress!!.max = MAX - MIN
            fence_progress!!.progress = 0
            fence_progress!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (mMap!!.projection != null) {
                        if (mLatLng != null) {
                            zoom(updateFence(progress + MIN))
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })
        }

        /**
         * 获取上一个页面传过来的数据
         */
        private fun intentDatas() {
            val fenceName = DateUtil.getCurrentDate(YYYY_MM_DD_HH_MM_SS)
            mFenceName = fenceName
            device_fence_name!!.text = mFenceName
            device_fence_alarm_in!!.isChecked = true
            device_fence_alarm_out!!.isChecked = true
            val vBundle = intent.extras
            mImei = vBundle!!.getString(C.key.ACTION_IMEI)
            mLat.mLatLng = vBundle.getParcelable(C.key.ACTION_LATLNG)
            if (mLat.mLatLng != null) {
                mImeis.add(mImei)
            }
            mGeobean = vBundle.getSerializable(C.key.ACTION_GEOBEAN) as GEOBean?
            delayAlarmSettingDialog = DelayAlarmSettingDialog(this)
            delayAlarmSettingDialog!!.setOnDelayAlarmSettingListener(this)
            delayAlarmSettingDialog!!.setUserFence(false)
            if (mGeobean != null) {
                if (mGeobean!!.alarmParam != null) {
                    device_fence_alarm_in!!.isChecked = mGeobean!!.alarmParam.isFenceIn
                    device_fence_alarm_out!!.isChecked = mGeobean!!.alarmParam.isFenceOut
                    delayIn = if (TextUtils.isEmpty(mGeobean!!.alarmParam.delayIn)) 0 else mGeobean!!.alarmParam.delayIn.toInt()
                    delayOut = if (TextUtils.isEmpty(mGeobean!!.alarmParam.delayOut)) 0 else mGeobean!!.alarmParam.delayOut.toInt()
                    delayAlarmSettingDialog!!.setFenceStatus(device_fence_alarm_in!!.isChecked, device_fence_alarm_out!!.isChecked)
                    delayAlarmSettingDialog!!.setFenceDelay(delayIn, delayOut)
                }
                isEditFence = true
                mRadius = if (TextUtils.isEmpty(mGeobean!!.radius)) 0 else mGeobean!!.radius.toFloat().toInt()
                mRadius = if (mRadius >= 200) mRadius else 200
                device_fence_name!!.text = mGeobean!!.fenName
                mFenceName = mGeobean!!.fenName
                if (!TextUtils.isEmpty(mGeobean!!.scale) && Integer.valueOf(mGeobean!!.scale) > 12) {
                    mScale = Integer.valueOf(mGeobean!!.scale)
                }
                mFenceID = mGeobean!!.fenceId
                val sb = StringBuilder()
                for (mSign: SignDevice in mGeobean!!.configureList) {
                    if (!TextUtils.isEmpty(mFenceID) && (mGeobean != null) && mGeobean!!.getFenceType()) {
                        sb.append(mSign.userId).append(",")
                    } else {
                        mImeis.add(mSign.imei)
                    }
                }
                if (!TextUtils.isEmpty(sb)) userIds = sb.substring(0, sb.length - 1)
                mDeviceLatLng = MyLatLng(mGeobean!!.points[0].lat, mGeobean!!.points[0].lng)
            }
            if ((mLat.mLatLng == null) && (mGeobean == null) && !mImei!!.isEmpty()) {
                mImeis.add(mImei)
            }
            if (!TextUtils.isEmpty(mFenceID) && (mGeobean != null) && mGeobean!!.getFenceType()) {
                val user_add = "[" + getString(R.string.user_add) + "]"
                device_fence_add_device!!.text = user_add
            } else {
                device_fence_add_device!!.text = LanguageUtil.getInstance().getString(LanguageHelper.COMMON_DEVICE_ADD)
            }
        }

        override fun initNavigationBar(navigationView: NavigationView) {
            common_search_text!!.setOnEditorActionListener(listener)
            common_search_button!!.setOnClickListener {
                if (common_search_text!!.text.toString().isEmpty()) {
                    val shake = AnimationUtils.loadAnimation(applicationContext, R.anim.common_shake)
                    common_search_text!!.startAnimation(shake)
                } else {
                    searchKeyword()
                }
            }
        }

        /**
         * 添加汽车标识图标
         *
         * @param lng
         */
        private fun initOverlay(lng: MyLatLng?) {
            if (lng == null) return
            if (mMarkerStart != null) {
                mMarkerStart!!.remove()
            }
            mLatLng = lng
            val ooA = MyMarkerOptions().position(lng).icon(mDeviceDescriptor)
            mMarkerStart = mMap!!.addMarker(ooA)
            val u = MyCameraUpdate()
            u.newLatLng(lng)
            mMap!!.animateCamera(u)
        }

        override fun onMapReady() {
            /**
             * 隐藏缩放控件
             */
            if (!mMap!!.isNull) {
                control_view!!.setMap(mMap)
                mMap!!.hideZoomControls() //不显示缩放控件
                mMap!!.setOnMapLoadedCallback(this)
            }
            if (TextUtils.isEmpty(mFenceID)) {
                control_view!!.setBuryingSource(3)
            } else {
                control_view!!.setBuryingSource(4)
            }
            if (GlobalData.getLatLng() != null) {
                mMap!!.locationString = LanguageUtil.getInstance().getString(LanguageHelper.MAP_MARK_MY_LOCATION)
                mMap!!.myLocation()
                mMap!!.setIsShowPhone(true)
            }
            mDeviceDescriptor = MyBitmapDescriptor(R.drawable.device_home_mark_new_online)
            if (isEditFence) {
                initOverlay(mDeviceLatLng)
            } else {
                //编辑时不需要设为MIN
                if (fence_progress != null) {
                    setFenceRadius(MIN)
                }
                if (mLat.mLatLng != null) {
                    mDeviceLatLng = MyLatLng(mLat.mLatLng.latitude, mLat.mLatLng.longitude)
                    if (mLat.mLatLng.latitude == 0.0) {
                        mDeviceLatLng = GlobalData.getLatLng()
                        mLatLng = mDeviceLatLng
                    } else {
                        initOverlay(mDeviceLatLng)
                    }
                } else {
                    mDeviceLatLng = GlobalData.getLatLng()
                    mLatLng = mDeviceLatLng
                    //                initOverlay(mDeviceLatLng);
                }
            }
            mMap!!.setOnCameraChangeListener(object : OnCameraChangeListener {
                override fun onCameraChange(pMyCameraPosition: MyCameraPosition) {
                    if (0f == fence_view!!.mCx) {
                        return
                    }
                    mLatLng = mMap!!.target
                    if (mMap!!.projection.mProjection != null) {
                        if (mLatLng != null) {
                            updateFence(fenceRadius())
                        }
                    }
                }
            })
        }

        override fun onMapLoaded() {
            if (mMap!!.projection != null) {
                if (mDeviceLatLng != null) {
                    val u = MyCameraUpdate()
                    u.newLatLngZoom(mDeviceLatLng, mScale.toFloat())
                    mMap!!.moveCamera(u)
                    if (mRadius != 0) {
                        fence_progress!!.progress = mRadius - MIN
                        zoom(updateFence(mRadius))
                    } else {
                        zoom(updateFence(fenceRadius()))
                    }
                } else if (GlobalData.getLatLng() != null) {
                    val u = MyCameraUpdate()
                    u.newLatLngZoom(GlobalData.getLatLng(), mScale.toFloat())
                    mMap!!.moveCamera(u)
                    zoom(updateFence(fenceRadius()))
                }
            }
        }

        /**
         * 设置围栏大小
         *
         * @param value
         */
        private fun setFenceRadius(value: Int) {
            fence_progress!!.progress = value - MIN
        }

        /**
         * 获取围栏大小
         *
         * @return
         */
        private fun fenceRadius(): Int {
            return fence_progress!!.progress + MIN
        }

        /**
         * 调整缩放级别
         *
         * @param vRadius
         */
        private fun zoom(vRadius: Float) {
            if (vRadius == 0f) {
                return
            }
            if (vRadius > GlobalData.mScreenWidth / 4f) {
                val zoom = MyCameraUpdate()
                mMap!!.moveCamera(zoom.zoomOut())
                val msg: Message = Message()
                msg.what = 0
                mHandler!!.sendMessageDelayed(msg, 100)
            } else if (vRadius < GlobalData.mScreenWidth / 8f) {
                val zoom = MyCameraUpdate()
                mMap!!.moveCamera(zoom.zoomIn())
                val msg: Message = Message()
                msg.what = 0
                mHandler!!.sendMessageDelayed(msg, 100)
            }
        }

        /**
         * 绘制围栏视图 devicePointMapBg
         */
        private fun updateFence(radius: Int): Float {
            if (mMap == null || mLatLng == null) {
                return 0f
            }
            val meters = MapUtil.metersToEquatorPixels(mMap, mLatLng, radius.toFloat())
            if ((fence_view!!.mCx > 0) && (fence_view!!.mRadiusText == radius) && (fence_view!!.mRadius ==
                            meters)) {
                return 0f
            }
            val vPoint = mMap!!.projection.toScreenLocation(mMap!!.target)
            if (fence_view!!.mCx == 0f && fence_view!!.mCy == 0f) {
                fence_view!!.mCx = vPoint.x.toFloat()
                fence_view!!.mCy = vPoint.y.toFloat()
                fence_view!!.setXY()
            }
            fence_view!!.mRadiusText = radius
            fence_view!!.mRadius = meters
            fence_view!!.postInvalidate()
            return fence_view!!.mRadius.toFloat()
        }

        /**
         * 搜索关键字
         */
        private fun searchKeyword() {
            showProgressDialog(LanguageUtil.getInstance().getString(LanguageHelper.CHECK_FENCE_SEARCH_ING))
            mHandler!!.sendEmptyMessageDelayed(1, 10000)
            mMap!!.searchSugget(common_search_text!!.text.toString(), object : OnSearchResultListener {
                override fun onSearchResult(pLocations: List<LocationResult>) {
                    if (pLocations.isEmpty()) {
//                    showToast(R.string.fence_search_no_result);
                        return
                    }
                    closeProgressDialog()
                    mHandler!!.removeMessages(1)
                    device_fence_poi!!.text = ""
                    val address = GlobalData.getLatLng().address
                    device_fence_my_addr!!.text = address
                    device_fence_foot_my_locate!!.setOnClickListener {
                        mLatLng = GlobalData.getLatLng()
                        moveAnimateCamera(mLatLng)
                        setFenceRadius(MIN)
                        closeDailog()
                    }
                    Functions.closeIMM(this@FenceAddOrEditActivity, common_search_button)
                    if (mAdapter == null) {
                        mAdapter = SearchResultAdapter(this@FenceAddOrEditActivity,
                                ImageHelper(this@FenceAddOrEditActivity))
                        search_result_background!!.visibility = View.VISIBLE
                        list!!.mode = PullToRefreshBase.Mode.DISABLED
                        list!!.setAdapter(mAdapter)
                        list!!.setOnItemClickListener { parent, view, position, id ->
                            mLatLng = (parent.getItemAtPosition(position) as LocationResult).latLng
                            moveAnimateCamera(mLatLng)
                            setFenceRadius(MIN)
                            closeDailog()
                        }
                    }
                    try {
                        mAdapter!!.setData(pLocations)
                        mAdapter!!.notifyDataSetChanged()
                        showDailog()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })
        }

        /**
         * 将某点移动到屏幕中心
         *
         * @param pLatlng
         */
        private fun moveAnimateCamera(pLatlng: MyLatLng?) {
            val u = MyCameraUpdate()
            u.newLatLng(pLatlng)
            mMap!!.animateCamera(u)
        }

        /**
         * 显示搜索关键字结果列表
         */
        private fun showDailog() {
            search_result!!.visibility = View.VISIBLE // 显示布局
            common_search_button!!.startAnimation(mAnimationIn)
        }

        /**
         * 关闭搜索关键字结果列表
         */
        private fun closeDailog() {
            search_result!!.startAnimation(mAnimationout) // 开始动画
        }

        public override fun onPause() {
            mMap!!.onPause()
            super.onPause()
        }

        public override fun onResume() {
            mMap!!.onResume()
            super.onResume()
        }

        public override fun onDestroy() {
            mHandler!!.removeMessages(0)
            EventBus.getDefault().unregister(this)
            mMap.onDestroy()
            super.onDestroy()
        }


        /**
         * 保存围栏
         */
        private fun saveFence() {
            if (GlobalData.getUser() == null) {
                return
            }
            if (mFenceName.isEmpty()) {
                showToast(LanguageUtil.getInstance().getString(LanguageHelper.NO_FENCE_NAME))
                return
            }

            showProgressDialog(LanguageUtil.getInstance().getString(LanguageHelper.SUBMIT_FENCE))
            vm.saveGeo(arrayOf(ServiceApi.geoSave,
                    SharedPre.mSharedPre.userID,
                    if ((!TextUtils.isEmpty(mFenceID) && (mGeobean != null) && mGeobean!!.getFenceType())) "" else getDeviceImei(mImeis),
                    mFenceName,
                    Constant.MAP_TYPE,
                    geoLatlng, fenceRadius().toString() + "",
                    alarmSet, mMap!!.zoom.toString(),
                    mFenceID,
                    appendList(mCancleImeis), delayIn.toString(), delayOut.toString(), userIds))
        }

        private fun appendList(pImeis: ArrayList<String>?): String {
            /** 添加非空判断  */
            if (pImeis == null || pImeis.size == 0) return ""
            var pImei = ""
            val buffer = StringBuffer()
            for (vs: String in pImeis) {
                buffer.append("$vs,")
            }
            if (buffer.length != 0) {
                pImei = buffer.substring(0, buffer.length - 1)
            }
            return pImei
        }

        /**
         * 获取围栏中心坐标
         */
        private val geoLatlng: String
        private get () = "(" + mLatLng!!.latitude + "," + mLatLng!!.longitude + ")"

        /**
         * 拼接所有的设备号
         */
        private fun getDeviceImei(pImeis: List<String?>): String {
            var sb = StringBuilder()
            for (i in pImeis.indices) {
                if (i > 0) {
                    sb = sb.append("," + pImeis[i])
                    continue
                }
                sb = sb.append(mImeis[i])
            }
            return if (!mAllImei.isEmpty()) {
                "-1"
            } else sb.toString()
        }

        /**
         * 获取围栏告警设置
         */
        private val alarmSet: String
        private get () {
            val sb = StringBuilder()
            sb.append(if (device_fence_alarm_in!!.isChecked) "in," else "")
            sb.append(if (device_fence_alarm_out!!.isChecked) "out" else "")
            return sb.toString()
        }

        override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
            if ((keyCode == KeyEvent.KEYCODE_BACK
                            && (search_result!!.visibility == View.VISIBLE))) {
                closeDailog()
                return false
            }
            return super.onKeyDown(keyCode, event)
        }

        /**
         * 限制输入的字符个数
         *
         * @param pNewName
         */
        private fun limitedNumberofWords(pNewName: String): Boolean {
            if (pNewName.trim().isEmpty()) {
                /*R.string.fence_name_not_null*/
                showToast("fence_name_not_null")
                return true
            } else if (pNewName.trim { it <= ' ' }.length > 200) {
                /*R.string.limit_fence_name_words*/
                showToast(LanguageUtil.getInstance().getString(LanguageHelper.LIMIT_FENCE_NAME_WORDS))
                return true
            }
            return false
        }

        /**
         * 修改围栏名称对话框
         */
        private fun changeFenceName() {
            val customBuilder = CustomDialog.Builder(this)
            customBuilder
                    .setTitle(LanguageUtil.getInstance().getString(LanguageHelper.FENCE_NAME))
                    .setMessage(mFenceName)
                    .setMessageHint(LanguageUtil.getInstance().getString(LanguageHelper.DEVICE_ENTER_FENCE_NAME))
                    .setNegativeButton(LanguageUtil.getInstance().getString(LanguageHelper.COMMON_TEXT_OK),
                            object : DialogInterface.OnClickListener {
                                override fun onClick(dialog: DialogInterface,
                                                     which: Int) {
                                    val vEdit = customBuilder.layout.findViewById<View>(R.id.message) as EditText
                                    if (TextUtils.isEmpty(mFenceID)) {
                                        BuryingPointUtils.onEvent(this@FenceAddOrEditActivity, "c_app_tqzx_wd_tjwlym_bjmcqran")
                                    } else {
                                        BuryingPointUtils.onEvent(this@FenceAddOrEditActivity, "c_app_tqzx_wd_bjwlym_bjmcqran")
                                    }
                                    if (!CheckForm.getInstance().isEmpty(vEdit, LanguageUtil.getInstance().getString(LanguageHelper.FENCE_NAME_NOT_NULL))) {
                                        mFenceName = vEdit.text.toString().trim { it <= ' ' }
                                        if (limitedNumberofWords(mFenceName)) return
                                        updateFence(fenceRadius())
                                        device_fence_name!!.text = mFenceName
                                        dialog.dismiss()
                                    }
                                }
                            })
                    .setPositiveButton(LanguageUtil.getInstance().getString(LanguageHelper.COMMON_CANCEL_TEXT)
                    ) { dialog, which ->
                        if (TextUtils.isEmpty(mFenceID)) {
                            BuryingPointUtils.onEvent(this@FenceAddOrEditActivity, "c_app_tqzx_wd_tjwlym_bjmcqxan")
                        } else {
                            BuryingPointUtils.onEvent(this@FenceAddOrEditActivity, "c_app_tqzx_wd_bjwlym_bjmcqxan")
                        }
                        dialog.dismiss()
                    }
                    .setContentClean { dialogInterface, i ->
                        val vEdit = customBuilder.layout.findViewById<View>(R.id.message) as EditText
                        vEdit.setText("")
                    }
                    .setMsgButtonClickListener { dialog, which ->
                        if (TextUtils.isEmpty(mFenceID)) {
                            BuryingPointUtils.onEvent(this@FenceAddOrEditActivity, "c_app_tqzx_wd_tjwlym_bjmcsr")
                        } else {
                            BuryingPointUtils.onEvent(this@FenceAddOrEditActivity, "c_app_tqzx_wd_bjwlym_bjmcsr")
                        }
                    }
            customBuilder.create().show()
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == REQUESTCODE && resultCode == RESULTCODE) {
                val vImeis = data!!.getStringArrayListExtra(C.key.ACTION_IMEIS)
                mCancleImeis = data.getStringArrayListExtra(C.key.ACTION_CANCLE_IMEIS)
                mAllImei = data.getStringExtra(C.key.ACTION_IMEI)
                mImeis.clear()
                if (vImeis != null && vImeis.size > 0) {
                    mImeis.addAll(vImeis)
                }
            } else if (requestCode == RelevanceUserActivity.REQUEST_RELEVANCE_CODE &&
                    resultCode == RelevanceUserActivity.RESULT_RELEVANCE_CODE) {
                userIds = data!!.getStringExtra(C.key.ACTION_USERID)
            }
        }

        private val mOnKeyListener: View.OnKeyListener = object : View.OnKeyListener {
            override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    Functions.closeIMM(this@FenceAddOrEditActivity, common_search_text)
                }
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    /*隐藏软键盘*/
                    Functions.closeIMM(this@FenceAddOrEditActivity, common_search_text)
                    if (TextUtils.isEmpty(common_search_text!!.text.toString().trim { it <= ' ' })) {
                        val shake = AnimationUtils.loadAnimation(applicationContext, R.anim.common_shake)
                        common_search_text!!.startAnimation(shake)
                    } else {
                        searchKeyword()
                    }
                    return true
                }
                return false
            }
        }
        private val listener: TextView.OnEditorActionListener = object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.keyCode
                                == KeyEvent.KEYCODE_ENTER)) {
                    if (TextUtils.isEmpty(common_search_text!!.text.toString().trim { it <= ' ' })) {
                        val shake = AnimationUtils.loadAnimation(applicationContext, R.anim.common_shake)
                        common_search_text!!.startAnimation(shake)
                    } else {
                        searchKeyword()
                    }
                    return true
                }
                return false
            }
        }

        /**
         * 检查是否有定位权限
         */
        private fun checkLocationPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                }
            }
        }

        /**
         * 权限是否获取回调
         *
         * @param requestCode
         * @param permissions
         * @param grantResults
         */
        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == 1) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }
        }

        override fun onDelayAlarmSettiongClick(delayIn: Int, delayOut: Int, inStatus: Boolean, outStatus: Boolean) {
            device_fence_alarm_in!!.isChecked = inStatus
            device_fence_alarm_out!!.isChecked = outStatus
            this.delayIn = delayIn
            this.delayOut = delayOut
        }

        companion object {
            const val RESULTCODE = 99
            const val REQUESTCODE = 101
        }

    }