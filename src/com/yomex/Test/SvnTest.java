package com.yomex.Test;

import com.yomex.Bean.SvnBean;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import com.yomex.Util.SVNUtil;

import java.io.File;

public class SvnTest {
    public static void main(String[] args){


    }


    public void checkOut(){
        File file = new File("E://t.txt");
        SVNUtil.setupLibrary();
        SvnBean svnBean = SVNUtil.loadProp();
        SVNClientManager svnClientManager = SVNUtil.authSvn(svnBean);
        if (!SVNUtil.isWorkingCopy(file)){
            return;
        }
        long rs = SVNUtil.checkout(svnClientManager,svnBean.getUrl()+"/ProjectName", SVNRevision.HEAD,file, SVNDepth.INFINITY);
        if (rs!=0){
            System.out.println("success");
        }else {
            System.out.println("Error");
        }
    }
}
