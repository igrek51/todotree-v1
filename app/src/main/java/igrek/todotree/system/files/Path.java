package igrek.todotree.system.files;

public class Path {
    String pathstr;

    public Path(String pathstr) {
        this.pathstr = pathstr;
    }

    @Override
    public String toString() {
        return pathstr;
    }

    public static String cutSlashFromBeginning(String pathstr) {
        if (pathstr.length() > 0 && pathstr.charAt(0) == '/') {
            pathstr = pathstr.substring(1);
        }
        return pathstr;
    }

    public static String cutSlashFromEnd(String pathstr) {
        if (pathstr.length() > 0 && pathstr.charAt(pathstr.length() - 1) == '/') {
            pathstr = pathstr.substring(0, pathstr.length() - 1);
        }
        return pathstr;
    }

    public Path append(String pathstr) {
        String newPathstr = cutSlashFromEnd(this.pathstr) + "/" + cutSlashFromBeginning(pathstr);
        return new Path(newPathstr);
    }
}
