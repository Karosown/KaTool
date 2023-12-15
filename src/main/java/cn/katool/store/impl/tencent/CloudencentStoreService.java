package cn.katool.store.impl.tencent;

import cn.katool.Exception.KaToolException;
import cn.katool.store.interfaces.ICloudTencentStoreService;
import com.qiniu.common.QiniuException;

import java.io.File;
import java.io.InputStream;

public class CloudencentStoreService implements ICloudTencentStoreService {
    @Override
    public String getOriginName(String URL) {
        return null;
    }

    @Override
    public boolean isExist(String dir, String fileName) throws KaToolException {
        return false;
    }

    @Override
    public boolean isExist(String dir, String fileName_fast, String fileName_second) throws KaToolException {
        return false;
    }

    @Override
    public String uploadFile(File file, String dir, String fileName_fast, String fileName_second, boolean isCompulsion) throws Exception {
        return null;
    }

    @Override
    public String uploadFile(File file, String dir, String fileName, boolean isCompulsion) throws Exception {
        return null;
    }

    @Override
    public String getUrlByName(String fileName) {
        return null;
    }

    @Override
    public String uploadFile(InputStream inputStream, String dir, String fileName_fast, String fileName_second, boolean isCompulsion) throws Exception {
        return null;
    }

    @Override
    public String uploadFile(InputStream inputStream, String dir, String fileName, boolean isCompulsion) throws Exception {
        return null;
    }

    @Override
    public String delete(String dir, String fileName) throws QiniuException {
        return null;
    }

    @Override
    public String delete(String dir, String fileName_fast, String fileName_second) throws QiniuException {
        return null;
    }
}
