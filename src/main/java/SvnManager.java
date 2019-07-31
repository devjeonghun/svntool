import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 업무 자동화 SVN TOOL
 * <p>
 * author: jhuncho
 * date: 2019-07-31
 */
public class SvnManager {
    public static final String BRANCHES = "/branches";
    public static final String TRUNK = "/trunk";

    private SVNRepository repository;

    // 각 프로토콜 초기화
    static {
        SVNRepositoryFactoryImpl.setup();
        //DAVRepositoryFactory.setup();
        //FSRepositoryFactory.setup();
    }

    // SVN 연결 생성자
    public SvnManager(String url, String username, String password) throws SVNException {
        repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
        repository.setAuthenticationManager(authManager);
    }

    public List<SVNDirEntry> getList(String category) throws SVNException {
        return getDirectories(category, Calendar.getInstance());
    }

    public List<SVNDirEntry> getList(String category, Calendar olderThan) throws SVNException {
        return getDirectories(category, olderThan);
    }

    public void getLatestFileRev(String filePath) throws SVNException {
        SVNDirEntry entry = repository.info(filePath, -1);
        System.out.println(String.format("Latest Rev of file \t %s \t %d ", filePath, entry.getRevision()));
    }

    public void getLogEntry() throws SVNException {
        SVNDirEntry info = repository.info("", -1);
        Collection<SVNLogEntry> log = repository.log(new String[]{"/branches"}, null, info.getRevision() - 500, -1, true, true);
        List<SVNLogEntry> list = log.stream().sorted((o1, o2) ->(o2.getRevision() < o1.getRevision()) ? -1 : ((o2.getRevision() == o1.getRevision()) ? 0 : 1)).collect(Collectors.toCollection(ArrayList::new));
        list.forEach(p -> {
            if (p.getAuthor().equalsIgnoreCase("jhuncho")) {
                System.out.println(p.getRevision()+p.getMessage()+p.getDate());
                Set changedPathsSet = p.getChangedPaths().keySet();
                Iterator changedPaths = changedPathsSet.iterator();
                while (changedPaths.hasNext()) {
                    SVNLogEntryPath entryPath = (SVNLogEntryPath) p.getChangedPaths().get(changedPaths.next());
                    if (entryPath.getCopyPath() != null) {
                        System.out.println(" " + entryPath.getType() + " " + entryPath.getPath() + "(from " + entryPath.getCopyPath() + " revision " + entryPath.getCopyRevision() + ")");
                    } else {
                        System.out.println(" " + entryPath.getType() + " " + entryPath.getPath());
                    }
                }
            }
        });
    }

    private List<SVNDirEntry> getDirectories(String path, Calendar olderThan) throws SVNException {
        List<SVNDirEntry> branches = new ArrayList<>();
        List<SVNDirEntry> entries = (List<SVNDirEntry>) repository.getDir(path, -1, null, (Collection) null);
        for (SVNDirEntry entry : entries) {
            if (olderThan.getTime().after(entry.getDate()))
                branches.add(entry);
        }
        return branches;
    }

}
