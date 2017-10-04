package com.example.nicolas.base11;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.io.FileUtils;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.ISVNReporter;
import org.tmatesoft.svn.core.io.ISVNReporterBaton;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.io.diff.SVNDeltaProcessor;
import org.tmatesoft.svn.core.io.diff.SVNDiffWindow;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Links to button clicks
        Button buttonLoad = (Button) findViewById(R.id.buttonLoad);

        // Capture button clicks
        buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("Application", "Load");

                //SVNURL url = SVNURL.parseURIEncoded("http://192.168.1.1/svn/TraceOp/");

                SVNURL url = null;
                try {
                    url = SVNURL.parseURIEncoded("https://github.com/jinigmichou/Test.git");
                    /*
         * Credentials to use for authentication.
         */
                    String userName = "jinigmichou";
                    String s_userPassword = "pass1988a";
                    char[] userPassword = s_userPassword.toCharArray();
                    //String userName = "http";
                    //char[] userPassword = {};


        /*
         * Prepare filesystem directory (export destination).
         */
                    // Get the directory for the user's public pictures directory.


                    File sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                    //System.out.println(sdCard);

                    File exportDirCheckout = new File (sdCard.getAbsolutePath());

                    exportDirCheckout.mkdirs();

                   /* if (exportDir.exists()) {
                        try {
                            FileUtils.deleteDirectory(new File("TestExport"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (exportDir.exists()) {
                        try {
                            FileUtils.deleteDirectory(new File("exportDirCheckout"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }*/
            /*SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, "Path ''{0}'' already exists", exportDir);
            throw new SVNException(err);}*/

        /*
         * Create an instance of SVNRepository class. This class is the main entry point
         * for all "low-level" Subversion operations supported by Subversion protocol.
         *
         * These operations includes browsing, update and commit operations. See
         * SVNRepository methods javadoc for more details.
         */
                    SVNRepository repository = SVNRepositoryFactory.create(url);

        /*
         * User's authentication information (name/password) is provided via  an
         * ISVNAuthenticationManager  instance.  SVNWCUtil  creates  a   default
         * authentication manager given user's name and password.
         *
         * Default authentication manager first attempts to use provided user name
         * and password and then falls back to the credentials stored in the
         * default Subversion credentials storage that is located in Subversion
         * configuration area. If you'd like to use provided user name and password
         * only you may use BasicAuthenticationManager class instead of default
         * authentication manager:
         *
         *  authManager = new BasicAuthenticationsManager(userName, userPassword);
         *
         * You may also skip this point - anonymous access will be used.
         */
                    ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userName, userPassword);
                    repository.setAuthenticationManager(authManager);

        /*
         * Get type of the node located at URL we used to create SVNRepository.
         *
         * "" (empty string) is path relative to that URL,
         * -1 is value that may be used to specify HEAD (latest) revision.
         */
                    SVNNodeKind nodeKind = repository.checkPath("", -1);
                    if (nodeKind == SVNNodeKind.NONE) {
                        SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.UNKNOWN, "No entry at URL ''{0}''", url);
                        throw new SVNException(err);
                    } else if (nodeKind == SVNNodeKind.FILE) {
                        SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.UNKNOWN, "Entry at URL ''{0}'' is a file while directory was expected", url);
                        throw new SVNException(err);
                    }

        /*
         * Get latest repository revision. We will export repository contents at this very revision.
         */
                    long latestRevision = repository.getLatestRevision();

        /*
         * Create reporterBaton. This class is responsible for reporting 'wc state' to the server.
         *
         * In this example it will always report that working copy is empty to receive update
         * instructions that are sufficient to create complete directories hierarchy and get full
         * files contents.
         */
                    ISVNReporterBaton reporterBaton = new ExportReporterBaton(latestRevision);

        /*
         * Create editor. This class will process update instructions received from the server and
         * will create directories and files accordingly.
         *
         * As we've reported 'emtpy working copy', server will only send 'addDir/addFile' instructions
         * and will never ask our editor implementation to modify a file or directory properties.
         */
                    ISVNEditor exportEditor = new ExportEditor(exportDirCheckout);

        /*
         * Now ask SVNKit to perform generic 'update' operation using our reporter and editor.
         *
         * We are passing:
         *
         * - revision from which we would like to export
         * - null as "target" name, to perform export from the URL SVNRepository was created for,
         *   not from some child directory.
         * - reporterBaton
         * - exportEditor.
         */

                    repository.update(latestRevision, null, true, reporterBaton, exportEditor);

                    System.out.println("Exported revision: " + latestRevision);


                    SVNClientManager ourClientManager = SVNClientManager.newInstance(null,
                            repository.getAuthenticationManager());
                    SVNUpdateClient updateClient = ourClientManager.getUpdateClient();
                    updateClient.setIgnoreExternals(false);

                    System.out.println("before doing checkout");

//        updateClient.doCheckout(url, exportDirCheckout, SVNRevision.HEAD, SVNRevision.HEAD,true);
                    updateClient.doCheckout(url, exportDirCheckout, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY , true);

                    System.out.println("Checkout Done");

                } catch (SVNException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private static class ExportReporterBaton implements ISVNReporterBaton {

        private long exportRevision;

        public ExportReporterBaton(long revision){
            exportRevision = revision;
        }

        public void report(ISVNReporter reporter) throws SVNException {
            try {

                reporter.setPath("", null, exportRevision, SVNDepth.INFINITY, true);

                reporter.finishReport();
            } catch (SVNException svne) {
                reporter.abortReport();
                System.out.println("Report failed.");
            }
        }
    }

    private static class ExportEditor implements ISVNEditor {

        private File myRootDirectory;
        private SVNDeltaProcessor myDeltaProcessor;

        public ExportEditor(File root) {
            myRootDirectory = root;
            myDeltaProcessor = new SVNDeltaProcessor();
        }

        public void targetRevision(long revision) throws SVNException {
        }

        public void openRoot(long revision) throws SVNException {
        }

        public void addDir(String path, String copyFromPath, long copyFromRevision) throws SVNException {
            File newDir = new File(myRootDirectory, path);
            if (!newDir.exists()) {
                if (!newDir.mkdirs()) {
                    SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, "error: failed to add the directory ''{0}''.", newDir);
                    throw new SVNException(err);
                }
            }
            System.out.println("dir added: " + path);
        }

        public void openDir(String path, long revision) throws SVNException {
        }

        public void changeDirProperty(String name, SVNPropertyValue property) throws SVNException {
        }

        public void addFile(String path, String copyFromPath, long copyFromRevision) throws SVNException {
            File file = new File(myRootDirectory, path);
            if (file.exists()) {
                SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, "error: exported file ''{0}'' already exists!", file);
                throw new SVNException(err);
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, "error: cannot create new  file ''{0}''", file);
                throw new SVNException(err);
            }
        }

        public void openFile(String path, long revision) throws SVNException {
        }

        public void changeFileProperty(String path, String name, SVNPropertyValue property) throws SVNException {
        }

        public void applyTextDelta(String path, String baseChecksum) throws SVNException {
            myDeltaProcessor.applyTextDelta((File) null, new File(myRootDirectory, path), false);
        }

        public OutputStream textDeltaChunk(String path, SVNDiffWindow diffWindow)   throws SVNException {
            return myDeltaProcessor.textDeltaChunk(diffWindow);
        }

        public void textDeltaEnd(String path) throws SVNException {
            myDeltaProcessor.textDeltaEnd();
        }

        public void closeFile(String path, String textChecksum) throws SVNException {
            System.out.println("file added: " + path);
        }

        public void closeDir() throws SVNException {
        }

        public void deleteEntry(String path, long revision) throws SVNException {
        }

        public void absentDir(String path) throws SVNException {
        }

        public void absentFile(String path) throws SVNException {
        }

        public SVNCommitInfo closeEdit() throws SVNException {
            return null;
        }

        public void abortEdit() throws SVNException {
        }

    }

    public static void setupLibrary() {
        /*
         * For using over http:// and https://
         */
        DAVRepositoryFactory.setup();
        /*
         * For using over svn:// and svn+xxx://
         */
        SVNRepositoryFactoryImpl.setup();

        /*
         * For using over file:///
         */
        FSRepositoryFactory.setup();
    }
}
