package gov.pnnl.velo.filesys.callback;
public class FileAttributes {
        public static final long READ_ONLY = 1L;
        public static final long HIDDEN = 2L;
        public static final long SYSTEM = 4L;
        public static final long DIRECTORY = 16L;
        public static final long ARCHIVE = 32L;
        public static final long DEVICE = 64L;
        public static final long NORMAL = 128L;
        public static final long TEMPORARY = 256L;
        public static final long SPARSE_FILE = 512L;
        public static final long REPARSE_POlong = 1024L;
        public static final long COMPRESSED = 2048L;
        public static final long OFFLINE = 4096L;//still was calling read for images
        public static final long NOT_CONTENT_INDEXED = 8192L;
        public static final long ENCRYPTED = 16384L;
    }
