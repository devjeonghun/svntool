import org.junit.BeforeClass;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNDirEntry;

import java.util.List;

public class SvnTester {
    private static SvnManager svnManager;

    @BeforeClass
    public static void createSvnMangager() throws Exception {
        svnManager = new SvnManager("svn://10.32.31.21/repos", "", "");
    }

    @Test
    public void getListTrunk() throws Exception {
        List<SVNDirEntry> list = svnManager.getList(SvnManager.TRUNK+"/LJDomain/homepage");
        //list.forEach(a -> System.out.println(a.getRepositoryRoot()));
        //svnManager.getLatestFileRev(SvnManager.TRUNK+"/LJDomain/homepage");
        svnManager.getLogEntry();

    }
}
