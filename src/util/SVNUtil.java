package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import Bean.SvnBean;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;


public class SVNUtil {
    public static SvnBean svnBean;
    public static Properties prop;
    public static String propertiesFileUrl = ""; //svn配置文件位置


    /**
     * 加载svn.properties文件
     * */
    public static SvnBean loadProp() {
        prop = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(propertiesFileUrl);
            prop.load(in);
        } catch (Exception e) {
            // TODO: handle exception
//			System.out.println("svn文件未找到");
            e.printStackTrace();
        }finally {
            if (in!=null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    System.out.println(e.getMessage());
                }
            }
        }
        svnBean = new SvnBean(prop.getProperty("svn.url"),prop.getProperty("name"),prop.getProperty("pwd"));
        return svnBean;
    }


    /**
     * 通过不同的协议初始化版本库
     */
    public static void setupLibrary() {
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
        FSRepositoryFactory.setup();
    }

    /**
     * 验证登录svn
     */
    public static SVNClientManager authSvn(SvnBean svnBean) {
        // 初始化版本库
        setupLibrary();

        // 创建库连接
        SVNRepository repository = null;
        try {
            repository = SVNRepositoryFactory.create(SVNURL
                    .parseURIEncoded(svnBean.getUrl()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

        // 身份验证
        ISVNAuthenticationManager authManager = SVNWCUtil

                .createDefaultAuthenticationManager(svnBean.getName(), svnBean.getPwd());

        // 创建身份验证管理器
        repository.setAuthenticationManager(authManager);

        DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
        SVNClientManager clientManager = SVNClientManager.newInstance(options,
                authManager);
        return clientManager;
    }

    /**
     * 验证登录svn
     */
    public static SVNClientManager authSvn(String svnRoot, String username,
                                           String password) {
        // 初始化版本库
        setupLibrary();

        // 创建库连接
        SVNRepository repository = null;
        try {
            repository = SVNRepositoryFactory.create(SVNURL
                    .parseURIEncoded(svnRoot));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

        // 身份验证
        ISVNAuthenticationManager authManager = SVNWCUtil

                .createDefaultAuthenticationManager(username, password);

        // 创建身份验证管理器
        repository.setAuthenticationManager(authManager);

        DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
        SVNClientManager clientManager = SVNClientManager.newInstance(options,
                authManager);
        return clientManager;
    }

    /**
     * Make directory in svn repository
     * @param clientManager
     * @param url
     * 			eg: http://svn.ambow.com/wlpt/bsp/trunk
     * @param commitMessage
     * @return
     * @throws Exception
     */
    public static SVNCommitInfo makeDirectory(SVNClientManager clientManager,
                                              SVNURL url, String commitMessage) {
        try {
            return clientManager.getCommitClient().doMkDir(
                    new SVNURL[] { url }, commitMessage);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Imports an unversioned directory into a repository location denoted by a
     * 	destination URL
     * @param clientManager
     * @param localPath
     * 			a local unversioned directory or singal file that will be imported into a
     * 			repository;
     * @param dstURL
     * 			a repository location where the local unversioned directory/file will be
     * 			imported into
     * @param commitMessage
     * @param isRecursive 递归
     * @return
     */
    public static SVNCommitInfo importDirectory(SVNClientManager clientManager,
                                                File localPath, SVNURL dstURL, String commitMessage,
                                                boolean isRecursive) {
        try {
            return clientManager.getCommitClient().doImport(localPath, dstURL,
                    commitMessage, null, true, true,
                    SVNDepth.fromRecurse(isRecursive));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Puts directories and files under version control
     * @param clientManager
     * 			SVNClientManager
     * @param wcPath
     * 			work copy path
     */
    public static int addEntry(SVNClientManager clientManager, File wcPath) {
        try {
            clientManager.getWCClient().doAdd(new File[] { wcPath }, true,
                    false, false, SVNDepth.INFINITY, false, false,
                    true);
            return 1;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    /**
     * Collects status information on a single Working Copy item
     * @param clientManager
     * @param wcPath
     * 			local item's path
     * @param remote
     * 			true to check up the status of the item in the repository,
     *			that will tell if the local item is out-of-date (like '-u' option in the SVN client's
     *			'svn status' command), otherwise false
     * @return
     * @throws Exception
     */
    public static SVNStatus showStatus(SVNClientManager clientManager,
                                       File wcPath, boolean remote) {
        SVNStatus status = null;
        try {
            status = clientManager.getStatusClient().doStatus(wcPath, remote);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return status;
    }

    /**
     * Commit work copy's change to svn
     * @param clientManager
     * @param wcPath
     *			working copy paths which changes are to be committed
     * @param keepLocks
     *			whether to unlock or not files in the repository
     * @param commitMessage
     *			commit log message
     * @return
     * @throws Exception
     */
    public static SVNCommitInfo commit(SVNClientManager clientManager,
                                       File wcPath, boolean keepLocks, String commitMessage) {
        try {
            return clientManager.getCommitClient().doCommit(
                    new File[] { wcPath }, keepLocks, commitMessage, null,
                    null, false, false, SVNDepth.INFINITY);
        } catch (SVNException e) {
            try {
                cleanup(clientManager, wcPath);
                return clientManager.getCommitClient().doCommit(
                        new File[] { wcPath }, keepLocks, commitMessage, null,
                        null, false, false, SVNDepth.INFINITY);
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Updates a working copy (brings changes from the repository into the working copy).
     * @param clientManager
     * @param wcPath
     * 			working copy path
     * @param updateToRevision
     * 			revision to update to
     * @param depth
     * 			update的深度：目录、子目录、文件
     * @return
     * @throws Exception
     */
    public static long update(SVNClientManager clientManager, File wcPath,
                              SVNRevision updateToRevision, SVNDepth depth) {
        SVNUpdateClient updateClient = clientManager.getUpdateClient();

        /*
         * sets externals not to be ignored during the update
         */
        updateClient.setIgnoreExternals(false);
        /*
         * returns the number of the revision wcPath was updated to
         */
        try {
            return updateClient.doUpdate(wcPath, updateToRevision,depth, false, false);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    /**
     * recursively checks out a working copy from url into wcDir
     * @param clientManager
     * @param url
     * 			a repository location from where a Working Copy will be checked out
     * @param revision
     * 			the desired revision of the Working Copy to be checked out
     * @param destPath
     * 			the local path where the Working Copy will be placed
     * @param depth
     * 			checkout的深度，目录、子目录、文件
     * @return
     * @throws Exception
     */
    public static long checkout(SVNClientManager clientManager, String url,
                                SVNRevision revision, File destPath, SVNDepth depth) {

        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        /*
         * sets externals not to be ignored during the checkout
         */
        updateClient.setIgnoreExternals(false);
        /*
         * returns the number of the revision at which the working copy is
         */
        try {
            return updateClient.doCheckout(SVNURL.parseURIEncoded(url), destPath, revision, revision,depth, true);
//			return updateClient.doCheckout(SVNURL.parseURIEncoded(url), destPath, revision, revision, false, true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    /**
     * cleanup
     *
     * */
    public static long cleanup(SVNClientManager clientManager, File wcPath) {
        try {
            if (wcPath.isDirectory()) {
                clientManager.getWCClient().doCleanup(wcPath);
            }else if (wcPath.getParentFile()!=null) {
                clientManager.getWCClient().doCleanup(wcPath.getParentFile());
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    public static boolean revert(SVNClientManager clientManager, File wcPath) {
        try {
            clientManager.getWCClient().doRevert(wcPath, true);
            return true;
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e.getMessage());
        }
        return false;
    }

    public static boolean resolve(SVNClientManager clientManager, File wcPath) {
        try {
            clientManager.getWCClient().doResolve(wcPath, true);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }


    /**
     * 获取更新文件列表
     * */
    public static List<String> getChangedList(String url,Long oldRevision) {
        loadProp();
        SVNRepository repository = null;
        List<String> changedList = new ArrayList<String>();
        try {
            repository = SVNRepositoryFactory.create( SVNURL.parseURIEncoded( url ) );
            ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager( svnBean.getName(), svnBean.getPwd() );
            repository.setAuthenticationManager( authManager );
            Collection logEntries = null;
            logEntries = repository.log( new String[] { "" } , null , oldRevision, SVNRevision.HEAD.getNumber() , true , true );
            for ( Iterator entries = logEntries.iterator( ); entries.hasNext( ); ) {
                SVNLogEntry logEntry = ( SVNLogEntry ) entries.next( );
                if ( logEntry.getChangedPaths( ).size( ) > 0 ) {
                    Set changedPathsSet = logEntry.getChangedPaths( ).keySet( );
                    for ( Iterator changedPaths = changedPathsSet.iterator( ); changedPaths.hasNext(); ) {
                        SVNLogEntryPath entryPath = ( SVNLogEntryPath ) logEntry.getChangedPaths( ).get( changedPaths.next( ) );
                        if (entryPath.getType()=='M'||entryPath.getType()=='A') {
                            String[] split = entryPath.getPath().split("/工程目录/");
                            StringBuffer br = new StringBuffer("");
                            br.append("/");
                            for (int i = 1; i < split.length; i++) {
                                br.append(split[i] + "/");
                            }
                            br.deleteCharAt(br.length()-1);
                            changedList.add(br.toString());
                        }
                    }
                }
            }
        }catch (Exception e) {
            // TODO: handle exception
        }
        return changedList;
    }



    /**
     * 确定path是否是一个工作空间
     * @param path
     * @return
     */
    public static boolean isWorkingCopy(File path){
        if(!path.exists()){
            System.out.println("'" + path + "' not exist!");
            return false;
        }
        try {
            if(null == SVNWCUtil.getWorkingCopyRoot(path, false)){
                return false;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return true;
    }

    /**
     * 确定一个URL在SVN上是否存在
     * @param url
     * @return
     */
    public static boolean isURLExist(SVNURL url,String username,String password){
        try {
            SVNRepository svnRepository = SVNRepositoryFactory.create(url);
            ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
            svnRepository.setAuthenticationManager(authManager);
            SVNNodeKind nodeKind = svnRepository.checkPath("", -1);
            return nodeKind == SVNNodeKind.NONE ? false : true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void commitFile(String filePath) {
        File thisFile = new File(filePath);
        if (!isWorkingCopy(thisFile)) {
            return;
        }
        SVNClientManager clientManager = authSvn(svnBean);
        addEntry(clientManager, thisFile);
        commit(clientManager, thisFile, false, null);
    }

    public static void updateFile(String filePath) {
        File thisFile = new File(filePath);
        if (!isWorkingCopy(thisFile)) {
            return;
        }
        loadProp();
        setupLibrary();
        SVNClientManager clientManager = authSvn(svnBean);
        update(clientManager, thisFile, SVNRevision.HEAD, SVNDepth.INFINITY);
        if ("conflicted".equals(showStatus(clientManager, thisFile, false).getContentsStatus())) {
            resolve(clientManager, thisFile);
            revert(clientManager, thisFile);
        }
    }

    /**
     * 删除文件
     * */
    public static boolean delete(String filePath) {
        try {
            loadProp();
            setupLibrary();
            SVNClientManager clientManager = authSvn(svnBean.getUrl()+filePath, svnBean.getName(), svnBean.getPwd());
            SVNURL svnUrl = SVNURL.parseURIEncoded(svnBean.getUrl()+filePath);
            clientManager.getCommitClient().doDelete(new SVNURL[] {svnUrl}, null);
            return true;
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println(e.getMessage());
        }
        return false;
    }

}
