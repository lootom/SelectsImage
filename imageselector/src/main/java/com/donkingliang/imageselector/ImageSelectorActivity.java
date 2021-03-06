package com.donkingliang.imageselector;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.os.EnvironmentCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.donkingliang.imageselector.MyManager.FinishActivityManager;
import com.donkingliang.imageselector.adapter.FolderAdapter;
import com.donkingliang.imageselector.adapter.ImageAdapter;
import com.donkingliang.imageselector.entry.Folder;
import com.donkingliang.imageselector.entry.Image;
import com.donkingliang.imageselector.imaging.IMGEditActivity;
import com.donkingliang.imageselector.model.ImageModel;
import com.donkingliang.imageselector.utils.DateUtils;
import com.donkingliang.imageselector.utils.ImageSelector;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;


public class ImageSelectorActivity extends AppCompatActivity {
    public final static int REQ_IMAGE_EDIT = 1;    //????????????
    public final static int REQ_IMAGE_CHOOSE = 2;  //????????????

    private TextView tvTime;
    private TextView tvFolderName;
    private TextView tvConfirm;
    private TextView tvPreview;
    private FrameLayout btnConfirm;
    private FrameLayout btnPreview;
    private RecyclerView rvImage;
    private RecyclerView rvFolder;
    private View masking;

    private ImageAdapter mAdapter;
    private GridLayoutManager mLayoutManager;

    private ArrayList<Folder> mFolders;
    private Folder mFolder;
    private boolean applyLoadImage = false;
    private static final int PERMISSION_WRITE_EXTERNAL_REQUEST_CODE = 0x00000011;
    private static final int PERMISSION_CAMERA_REQUEST_CODE = 0x00000012;

    private static final int CAMERA_REQUEST_CODE = 0x00000010;

    private boolean isOpenFolder;
    private boolean isShowTime;
    private boolean isInitFolder;
    private boolean isSingle;
    private boolean isViewImage = true;
    private boolean isFull = false; //??????????????????
    private int mMaxCount;

    private boolean useCamera = true;
    private boolean isTagging = true;
    private String mPhotoPath;

    private Handler mHideHandler = new Handler();
    private Runnable mHide = new Runnable() {
        @Override
        public void run() {
            hideTime();
        }
    };

    // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    // ?????????????????????????????????????????????????????????????????????????????????
    private ArrayList<String> mSelectedImages;
    private FrameLayout diy_lable;
    private CheckBox selectOriginalImage; // ??????????????????
    private TextView diyLableText;
    private boolean onlyImage;

    /**
     * ?????????????????????
     *
     * @param activity
     * @param requestCode
     * @param isSingle       ????????????
     * @param onlyImage      ????????????????????????????????????
     * @param isViewImage    ??????????????????????????????
     * @param useCamera      ????????????????????????
     * @param maxSelectCount ??????????????????????????????????????????0?????????????????????isSingle???false???????????????
     * @param isTagging      ??????????????????
     * @param selected       ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *                       ??????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     */
    public static void openActivity(Activity activity, int requestCode,
                                    boolean isSingle,boolean onlyImage, boolean isViewImage, boolean useCamera,
                                    int maxSelectCount,boolean isTagging, ArrayList<String> selected) {
        Intent intent = new Intent(activity, ImageSelectorActivity.class);
        intent.putExtras(dataPackages(isSingle,onlyImage, isViewImage, useCamera, maxSelectCount,isTagging, selected));
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * ?????????????????????
     *
     * @param fragment
     * @param requestCode
     * @param isSingle       ????????????
     * @param onlyImage      ????????????????????????????????????
     * @param isViewImage    ??????????????????????????????
     * @param useCamera      ????????????????????????
     * @param maxSelectCount ??????????????????????????????????????????0?????????????????????isSingle???false???????????????
     * @param isTagging      ??????????????????
     * @param selected       ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *                       ??????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    public static void openActivity(Fragment fragment, int requestCode,
                                    boolean isSingle,boolean onlyImage, boolean isViewImage, boolean useCamera,
                                    int maxSelectCount,boolean isTagging, ArrayList<String> selected) {
        Intent intent = new Intent(fragment.getContext(), ImageSelectorActivity.class);
        intent.putExtras(dataPackages(isSingle,onlyImage,isViewImage, useCamera, maxSelectCount, isTagging,selected));
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * ?????????????????????
     *
     * @param fragment
     * @param requestCode
     * @param isSingle       ????????????
     * @param onlyImage      ????????????????????????????????????
     * @param isViewImage    ??????????????????????????????
     * @param useCamera      ????????????????????????
     * @param maxSelectCount ??????????????????????????????????????????0?????????????????????isSingle???false???????????????
     * @param isTagging      ??????????????????
     * @param selected       ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *                       ??????????????????????????????????????????????????????????????????????????????????????????????????????
     */

    public static void openActivity(android.app.Fragment fragment, int requestCode,
                                    boolean isSingle,boolean onlyImage, boolean isViewImage, boolean useCamera,
                                    int maxSelectCount,boolean isTagging, ArrayList<String> selected) {
        Intent intent = new Intent(fragment.getActivity(), ImageSelectorActivity.class);
        intent.putExtras(dataPackages(isSingle, onlyImage,isViewImage, useCamera, maxSelectCount, isTagging,selected));
        fragment.startActivityForResult(intent, requestCode);
    }

    public static Bundle dataPackages(boolean isSingle,boolean onlyImage, boolean isViewImage, boolean useCamera,
                                      int maxSelectCount,boolean isTagging, ArrayList<String> selected) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ImageSelector.IS_SINGLE, isSingle);
        bundle.putBoolean(ImageSelector.IS_VIEW_IMAGE, isViewImage);
        bundle.putBoolean(ImageSelector.USE_CAMERA, useCamera);
        bundle.putBoolean(ImageSelector.IS_TAGGING, isTagging);
        bundle.putBoolean(ImageSelector.ONLY_IMAGE, onlyImage);
        bundle.putInt(ImageSelector.MAX_SELECT_COUNT, maxSelectCount);
        bundle.putStringArrayList(ImageSelector.SELECTED, selected);
        return bundle;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select_ass);
        FinishActivityManager.getManager().addActivity(this); //????????????   ???????????????????????????


        Intent intent = getIntent();
        mMaxCount = intent.getIntExtra(ImageSelector.MAX_SELECT_COUNT, 0);
        isSingle = intent.getBooleanExtra(ImageSelector.IS_SINGLE, false);
        isViewImage = intent.getBooleanExtra(ImageSelector.IS_VIEW_IMAGE, true);
        useCamera = intent.getBooleanExtra(ImageSelector.USE_CAMERA, true);
        isTagging = intent.getBooleanExtra(ImageSelector.IS_TAGGING, true);
        onlyImage = intent.getBooleanExtra(ImageSelector.ONLY_IMAGE, false);
        mSelectedImages = intent.getStringArrayListExtra(ImageSelector.SELECTED);

        setStatusBarColor();
        initView();
        initListener();
        initImageList();
        checkPermissionAndLoadImages();
        hideFolderList();
        setSelectImageCount(0);
    }

    /**
     * ?????????????????????
     */
    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#373c3d"));
        }
    }

    private void initView() {
        rvImage = (RecyclerView) findViewById(R.id.rv_image);
        rvFolder = (RecyclerView) findViewById(R.id.rv_folder);
        tvConfirm = (TextView) findViewById(R.id.tv_confirm);
        tvPreview = (TextView) findViewById(R.id.tv_preview);
        btnConfirm = (FrameLayout) findViewById(R.id.btn_confirm);
        btnPreview = (FrameLayout) findViewById(R.id.btn_preview);
        diy_lable = (FrameLayout) findViewById(R.id.diy_lable);
        selectOriginalImage = (CheckBox) findViewById(R.id.selectOriginalImage);
        tvFolderName = (TextView) findViewById(R.id.tv_folder_name);
        diyLableText = (TextView) findViewById(R.id.diy_lable_text);
        tvTime = (TextView) findViewById(R.id.tv_time);
        masking = findViewById(R.id.masking);

        //???????????????
        if (!isTagging){
            diy_lable.setVisibility(View.GONE);
            selectOriginalImage.setVisibility(View.GONE);
        }

    }

    private void initListener() {
        selectOriginalImage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

//                if (isChecked) {
////                    Toast.makeText(ImageSelectorActivity.this, "?????????", Toast.LENGTH_SHORT).show();
//                } else {
////                    Toast.makeText(ImageSelectorActivity.this, "????????????", Toast.LENGTH_SHORT).show();
//                }
                isFull = isChecked;
            }
        });

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Image> images = new ArrayList<>();
                images.addAll(mAdapter.getSelectImages());
                toPreviewActivity(images, 0);
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm();
            }
        });

        diy_lable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lable();
            }
        });
        findViewById(R.id.btn_folder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInitFolder) {
                    if (isOpenFolder) {
                        closeFolder();
                    } else {
                        openFolder();
                    }
                }
            }
        });
        masking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFolder();
            }
        });

        rvImage.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                changeTime();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                changeTime();
            }
        });
    }

    // ?????????????????????

    private void lable() {

        ArrayList<Image> images = new ArrayList<>();
        images.addAll(mAdapter.getSelectImages());
//        toPreviewActivity(images, 0);
        if (mAdapter.getSelectImages().size() > 0) {
            Image image = mAdapter.getSelectImages().get(0); //?????????????????????
            String path = image.getPath();
            File imgfile = new File(path);
            Uri uri1 = Uri.fromFile(imgfile);
//            Toast.makeText(this, "????????????:" + imgfile.length(), Toast.LENGTH_SHORT).show();
            File mImageFile = new File(getCacheDir(), UUID.randomUUID().toString() + ".jpg");
            Intent intent = new Intent(this, IMGEditActivity.class);
            intent.putExtra(IMGEditActivity.EXTRA_IMAGE_URI, uri1); // ???????????????Url
            intent.putExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH, mImageFile.getAbsolutePath()); // ??????????????????
            startActivity(intent);

//          startActivityForResult(intent, REQ_IMAGE_EDIT);
        } else {
            Toast.makeText(this, "???????????????", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ?????????????????????
     */
    private void initImageList() {
        // ??????????????????
        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLayoutManager = new GridLayoutManager(this, 3);
        } else {
            mLayoutManager = new GridLayoutManager(this, 5);
        }

        rvImage.setLayoutManager(mLayoutManager);
        mAdapter = new ImageAdapter(this, mMaxCount, isSingle, isViewImage);
        rvImage.setAdapter(mAdapter);
        ((SimpleItemAnimator) rvImage.getItemAnimator()).setSupportsChangeAnimations(false);
        if (mFolders != null && !mFolders.isEmpty()) {
            setFolder(mFolders.get(0));
        }
        mAdapter.setOnImageSelectListener(new ImageAdapter.OnImageSelectListener() {
            @Override
            public void OnImageSelect(Image image, boolean isSelect, int selectCount) {
                setSelectImageCount(selectCount);
            }
        });
        mAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(Image image, int position){
                toPreviewActivity(mAdapter.getData(), position);
            }

            @Override
            public void OnCameraClick() {
                checkPermissionAndCamera();
            }
        });
    }

    /**
     * ??????????????????????????????
     */
    private void initFolderList() {
        if (mFolders != null && !mFolders.isEmpty()) {
            isInitFolder = true;
            rvFolder.setLayoutManager(new LinearLayoutManager(ImageSelectorActivity.this));
            FolderAdapter adapter = new FolderAdapter(ImageSelectorActivity.this, mFolders);
            adapter.setOnFolderSelectListener(new FolderAdapter.OnFolderSelectListener() {
                @Override
                public void OnFolderSelect(Folder folder) {
                    setFolder(folder);
                    closeFolder();
                }
            });
            rvFolder.setAdapter(adapter);
        }
    }

    /**
     * ???????????????????????????????????????????????????
     */
    private void hideFolderList() {
        rvFolder.post(new Runnable() {
            @Override
            public void run() {
                rvFolder.setTranslationY(rvFolder.getHeight());
                rvFolder.setVisibility(View.GONE);
            }
        });
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param folder
     */
    private void setFolder(Folder folder) {
        if (folder != null && mAdapter != null && !folder.equals(mFolder)) {
            mFolder = folder;
            tvFolderName.setText(folder.getName());
            rvImage.scrollToPosition(0);
            mAdapter.refresh(folder.getImages(), folder.isUseCamera());
        }
    }

    private void setSelectImageCount(int count){
        if (count == 0) {
            btnConfirm.setEnabled(false);
            btnPreview.setEnabled(false);
            diy_lable.setEnabled(false);
            tvConfirm.setText("??????(" + count + "/" + mMaxCount + ")");
            tvPreview.setText("??????");
            selectOriginalImage.setText("??????");
            if (isSingle) {
                tvConfirm.setText("??????(" + count + "/" + 1 + ")");
            }
        } else {
            if (count==1){
                diy_lable.setEnabled(true);
            }else{
                diy_lable.setEnabled(false);
            }
            btnConfirm.setEnabled(true);
            btnPreview.setEnabled(true);
            tvPreview.setText("??????(" + count + ")");
            if (isSingle) {
                tvConfirm.setText("??????(" + count + "/" + 1 + ")");
            } else if (mMaxCount > 0) {
                tvConfirm.setText("??????(" + count + "/" + mMaxCount + ")");
            } else {
                tvConfirm.setText("??????(" + count + ")");
            }
            if (mAdapter!=null&&mAdapter.getSelectImages().size()>0){
                long tolSize = 0;
                ArrayList<Image> selectImages = mAdapter.getSelectImages();
                for (int i = 0; i <selectImages.size() ; i++) {
                    String path = selectImages.get(i).getPath();
                    //??????????????????
                    int dotIndex = path.lastIndexOf(".");
                    String end = path.substring(dotIndex, path.length()).toLowerCase();
                    // ??????????????? ????????????????????????
                     if(end.equals(".avi") || end.equals(".mov") || end.equals(".FLV") || end.equals(".3GP") || end.equals(".mp4") || end.equals(".rmvb") || end.equals(".rm")) {
                         diy_lable.setEnabled(false);
                     }
                    File file = new File(path);
                    tolSize +=  file.length();
                }
                selectOriginalImage.setText("?????? ("+FormetFileSize(tolSize)+")");
            }
        }
    }

    /**
     * ?????????????????????
     */
    private void openFolder() {
        if (!isOpenFolder) {
            masking.setVisibility(View.VISIBLE);
            ObjectAnimator animator = ObjectAnimator.ofFloat(rvFolder, "translationY",
                    rvFolder.getHeight(), 0).setDuration(300);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    rvFolder.setVisibility(View.VISIBLE);
                }
            });
            animator.start();
            isOpenFolder = true;
        }
    }

    /**
     * ?????????????????????
     */
    private void closeFolder() {
        if (isOpenFolder) {
            masking.setVisibility(View.GONE);
            ObjectAnimator animator = ObjectAnimator.ofFloat(rvFolder, "translationY",
                    0, rvFolder.getHeight()).setDuration(300);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    rvFolder.setVisibility(View.GONE);
                }
            });
            animator.start();
            isOpenFolder = false;
        }
    }

    /**
     * ???????????????
     */
    private void hideTime() {
        if (isShowTime) {
            ObjectAnimator.ofFloat(tvTime, "alpha", 1, 0).setDuration(300).start();
            isShowTime = false;
        }
    }

    /**
     * ???????????????
     */
    private void showTime() {
        if (!isShowTime) {
            ObjectAnimator.ofFloat(tvTime, "alpha", 0, 1).setDuration(300).start();
            isShowTime = true;
        }
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????????????????
     */
    private void changeTime() {
        int firstVisibleItem = getFirstVisibleItem();
        Image image = mAdapter.getFirstVisibleImage(firstVisibleItem);
        if (image != null) {
            String time = DateUtils.getImageTime(image.getTime() * 1000);
            tvTime.setText(time);
            showTime();
            mHideHandler.removeCallbacks(mHide);
            mHideHandler.postDelayed(mHide, 1000);

        }
    }

    private int getFirstVisibleItem() {
        return mLayoutManager.findFirstVisibleItemPosition();
    }

    private void confirm() {
        if (mAdapter == null) {
            return;
        }
        //???????????????????????????Image????????????????????????String?????????????????????????????????
        ArrayList<Image> selectImages = mAdapter.getSelectImages();
        ArrayList<String> images = new ArrayList<>();
        for (Image image : selectImages) {
            images.add(image.getPath());
        }
        //???????????????????????????????????????Intent???????????????Activity???
        setResult(images, false,isFull);
        finish();
    }

    private void setResult(ArrayList<String> images, boolean isCameraImage,boolean isFull) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(ImageSelector.SELECT_RESULT, images);
        intent.putExtra(ImageSelector.IS_CAMERA_IMAGE, isCameraImage);
        intent.putExtra(ImageSelector.IS_FULL, isFull);
        setResult(RESULT_OK, intent);
    }

    private void toPreviewActivity(ArrayList<Image> images, int position) {
        if (images != null && !images.isEmpty()) {
            PreviewActivity.openActivity(this, images,
                    mAdapter.getSelectImages(), isSingle, mMaxCount, position);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (applyLoadImage) {
            applyLoadImage = false;
            checkPermissionAndLoadImages();
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImageSelector.RESULT_CODE) {
            if (data != null && data.getBooleanExtra(ImageSelector.IS_CONFIRM, false)) {
                //?????????????????????????????????????????????????????????????????????????????????????????????
                confirm();
            } else {
                //?????????????????????????????????
                mAdapter.notifyDataSetChanged();
                setSelectImageCount(mAdapter.getSelectImages().size());
            }
        } else if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(mPhotoPath))));
                ArrayList<String> images = new ArrayList<>();
                images.add(mPhotoPath);
                setResult(images, true,isFull);
                finish();
            }
        } else if (requestCode == REQ_IMAGE_EDIT) {
            //???????????????

        } else if (requestCode == REQ_IMAGE_CHOOSE) {

        }


    }

    /**
     * ?????????????????????
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mLayoutManager != null && mAdapter != null) {
            //???????????????
            if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                mLayoutManager.setSpanCount(3);
            }
            //???????????????
            else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mLayoutManager.setSpanCount(5);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * ?????????????????????SD??????????????????
     */
    private void checkPermissionAndLoadImages() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            Toast.makeText(this, "????????????", Toast.LENGTH_LONG).show();
            return;
        }
        int hasWriteExternalPermission = ContextCompat.checkSelfPermission(getApplication(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteExternalPermission == PackageManager.PERMISSION_GRANTED) {
            //???????????????????????????
            loadImageForSDCard();
        } else {
            //??????????????????????????????
            ActivityCompat.requestPermissions(ImageSelectorActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_REQUEST_CODE);
        }
    }

    /**hm,mnjhjjnjjbnvbbv m,k,mmmmmmmmmmm
     * ????????????????????????
     */
    private void checkPermissionAndCamera() {
        int hasCameraPermission = ContextCompat.checkSelfPermission(getApplication(),
                Manifest.permission.CAMERA);
        if (hasCameraPermission == PackageManager.PERMISSION_GRANTED) {
            //????????????????????????
            openCamera();
        } else {
            //??????????????????????????????
            ActivityCompat.requestPermissions(ImageSelectorActivity.this,
                    new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST_CODE);
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_WRITE_EXTERNAL_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //??????????????????????????????
                loadImageForSDCard();
            } else {
                //?????????????????????????????????
                showExceptionDialog(true);
            }
        } else if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //???????????????????????????????????????
                openCamera();
            } else {
                //?????????????????????????????????
                showExceptionDialog(false);
            }
        }
    }

    /**
     * ???????????????????????????????????????????????????dialog.
     */
    private void showExceptionDialog(final boolean applyLoad) {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("??????")
                .setMessage("????????????????????????????????????????????????????????????????????????>????????????>??????????????????????????????")
                .setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                }).setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                startAppSettings();
                if (applyLoad) {
                    applyLoadImage = true;
                }
            }
        }).show();
    }

    /**
     * ???SDCard???????????????
     */
    private void loadImageForSDCard() {
        ImageModel.loadImageForSDCard(this, onlyImage,new ImageModel.DataCallback() {
            @Override
            public void onSuccess(ArrayList<Folder> folders) {
                mFolders = folders;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mFolders != null && !mFolders.isEmpty()) {
                            initFolderList();
                            mFolders.get(0).setUseCamera(useCamera);
                            setFolder(mFolders.get(0));
                            if (mSelectedImages != null && mAdapter != null) {
                                mAdapter.setSelectedImages(mSelectedImages);
                                mSelectedImages = null;
                                setSelectImageCount(mAdapter.getSelectImages().size());
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * ??????????????????
     */
    private void openCamera() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (captureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (photoFile != null) {
                mPhotoPath = photoFile.getAbsolutePath();
                //??????FileProvider????????????content?????????Uri
                Uri photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
//                Uri photoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                startActivityForResult(captureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = String.format("JPEG_%s.jpg", timeStamp);
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        File tempFile = new File(storageDir, imageFileName);
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }
        return tempFile;
    }

    /**
     * ?????????????????????
     */
    private void startAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && isOpenFolder) {
            closeFolder();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // ??????????????????
    public  String FormetFileSize(long fileS) {// ??????????????????
        DecimalFormat df = new DecimalFormat("#.00");//0 ?????????????????????????????? 0 ?????????# ???????????????????????????????????????????????????
        String fileSizeString = "";
        if (fileS == 0) {
            fileSizeString = "";
        } else if (fileS < 1024 && fileS > 0) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

}
