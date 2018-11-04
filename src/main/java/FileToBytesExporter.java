import error.FileError;
import error.InvalidPathException;
import error.MultipleFilesFoundError;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * <p>Files to bytes exporter provides a <b>custom, fast</b> class for generating <i>binary
 * files</i> from plain (or not) text files.
 * <p>
 * Binary files are present at <b>every computer</b> and the have some interesting advantages facing
 * regular text files:
 * <ul>
 * <li>
 * <b>Memory efficient</b>: while <i>plain text files</i> has 128 possible values (pure
 * unexpanded ASCII), binary files have <b>up to 256 possible values</b>, so it is twice compact (as
 * for representing some chars in ASCII, more than one value is needed).
 * </li>
 * <li>
 * <b>Compact</b>: storing a <i>binary file</i> will necessarily be more packed than a
 * <i>plain text file</i>.
 * </li>
 * <li>
 * <b>No syntax definition</b>: as some human-readable files such as <i>XML, JAVA, etc
 * .</i> need a <b>specific syntax</b> for saving data correctly, they increase its size and
 * complexity with that rules. As opposed to that, <b>binary files</b> just saves the data, with no
 * modifications or rules, which is faster and memory friendly.
 * </li>
 * <li>
 * <b>Directly to memory</b>: as there is <b>no need of processing the data</b>, reading
 * a binary file is as simple as it goes directly to the memory for using it.
 * </li>
 * <li>
 * <b>Secure</b>: as <b>binary file</b> is not human-readable file, it makes impossible
 * to others to inspect and know the <i>file contents</i> without a proper editor. Also they need to
 * know <b>which type of data structure</b> is saved for obtaining the desired instance.
 * </li>
 * </ul><p>
 *
 * This class allows you to easily generate binary files from regular text files with some simple
 * methods and attributes. In addition, it supports <b>glob syntax</b>, so it is even easier to find
 * and convert lots of files with just one line.
 */
public class FileToBytesExporter implements Cloneable, Serializable {
    private String mFilename;
    private ArrayList<String> mPath;
    private String mReadData;
    private String mFileSeparator;
    private final Object lock = new Lock();

    /**
     * Public default constructor - sets {@link #mFilename filename} to null and {@link #mPath path}
     * to empty {@code String array}.
     *
     * @see String
     */
    public FileToBytesExporter() {
        this(null);
    }

    /**
     * Constructor that uses only the {@link #mFilename filename} as argument ({@link #mPath path }
     * is an empty array).
     *
     * @param filename source file.
     *
     * @see String
     */
    public FileToBytesExporter(String filename) {
        this(filename, new String[]{});
    }

    /**
     * Constructor that uses both {@link #mFilename filename} and {@link #mPath path} as arguments.
     *
     * @param filename source file.
     * @param paths    list of paths where search for the source file.
     *
     * @see String
     */
    public FileToBytesExporter(String filename, String... paths) {
        this(filename, new ArrayList<>(Arrays.asList(paths)), null, null);
    }

    /**
     * Private constructor for cloning of generating a new instance - only visible for this class
     *
     * @param filename      source file
     * @param paths         list of paths where search for the source file
     * @param readData      current read data
     * @param fileSeparator current used file separator
     */
    private FileToBytesExporter(String filename, ArrayList<String> paths, String readData,
                                String fileSeparator) {
        mFilename = filename;
        mPath = paths;
        mReadData = readData;
        mFileSeparator = fileSeparator;
    }

    /**
     * Updates the filename once the {@link FileToBytesExporter class} is created
     *
     * @param filename new filename
     */
    public void setFilename(String filename) {
        mFilename = filename;
    }

    /**
     * Updates the paths once the {@link FileToBytesExporter class} is created
     *
     * @param paths new paths
     */
    public void setPaths(String... paths) {
        mPath = new ArrayList<>(Arrays.asList(paths));
    }

    /**
     * Includes a new path inside the stored paths - if {@link #mPath path} is not created, it
     * generates a new instance by using {@link #setPaths(String...)} method.
     *
     * @param newPath new path to include
     */
    public void addPath(String newPath) {
        if (mPath == null)
            setPaths(newPath);
        else
            mPath.add(newPath);
    }

    /**
     * Reads the source file and saves its contents inside a {@link String} object.
     *
     * @throws MultipleFilesFoundError if multiple files were found and {@code mustOpenAllFiles} is
     *                                 {@code false}.
     * @throws InvalidPathException    when one of the provided paths does not exists or any other
     *                                 error happened (like having not enough permissions).
     * @throws FileNotFoundException   when the filename is not found at any of the directories.
     */
    public void readSource() throws IOException {
        readSource(false);
    }

    /**
     * Reads all the sources file that were found at the current paths and inside all the provided
     * paths if {@code mustOpenAllFiles} is {@code true}, appending every file at the end with a
     * double blank line.
     *
     * @param mustOpenAllFiles whether if more than one source file was found it must be also read
     *                         and appended at the end of the current file.
     *
     * @throws MultipleFilesFoundError if multiple files were found and {@code mustOpenAllFiles} is
     *                                 {@code false}.
     * @throws InvalidPathException    when one of the provided paths does not exists or any other
     *                                 error happened (like having not enough permissions).
     * @throws FileNotFoundException   when the filename is not found at any of the directories.
     */
    public void readSource(boolean mustOpenAllFiles) throws IOException {
        readSource(mustOpenAllFiles, "\n\n");
    }

    /**
     * Reads all the sources file that were found at the current paths and inside all the provided
     * paths if {@code mustOpenAllFiles} is {@code true}, appending every file at the end the value
     * passed at {@code fileSeparator}.
     *
     * @param mustOpenAllFiles whether if more than one source file was found it must be also read
     *                         and appended at the end of the current file. In addition, if is set
     *                         to {@code true}, it will navigate through all the child directories
     *                         for file matches with the same name - pattern specified at {@link
     *                         #FileToBytesExporter(String)} or {@link #setFilename(String)}.
     * @param fileSeparator    the {@code String} that will be added at the end of the just read
     *                         file if any other file was found.
     *
     * @throws MultipleFilesFoundError             if multiple files were found and {@code
     *                                             mustOpenAllFiles} is {@code false}.
     * @throws error.InvalidFileSeparatorException when multiple files were found and {@code
     *                                             fileSeparator} is null.
     * @throws InvalidPathException                when one of the provided paths does not exists or
     *                                             any other error happened (like having not enough
     *                                             permissions).
     * @throws FileNotFoundException               when the filename is not found at any of the
     *                                             directories.
     * @throws IOException                         when looking for a file using glob and any error
     *                                             occurred
     * @see Glob#match(File, String, boolean)
     */
    public void readSource(boolean mustOpenAllFiles, final String fileSeparator)
            throws IOException {
        boolean isAnyPathProvided = mPath.size() > 0;
        File srcDir = Paths.get(".").toFile();
        ArrayList<File> filesInSrcDir = Glob.match(srcDir, mFilename, mustOpenAllFiles);
        int foundFilesInSrc = filesInSrcDir != null ? filesInSrcDir.size() : 0;
        if (foundFilesInSrc == 0 && !isAnyPathProvided)
            throw new FileNotFoundException(String.format("The file (or glob) \"%s\" was not " +
                    "found at any provided dir", mFilename));
        final ArrayList<File> allFoundFiles = foundFilesInSrc == 0 ?
                new ArrayList<>() :
                new ArrayList<>(filesInSrcDir);
        if (allFoundFiles.size() > 1 && !mustOpenAllFiles)
            throw new MultipleFilesFoundError("Multiple files found at the source directory");
        if (isAnyPathProvided) {
            for (String path : mPath) {
                File currentPath = new File(path);
                if (!currentPath.exists())
                    throw new InvalidPathException(String.format("Path \"%s\" does not exist",
                            path));
                ArrayList<File> filesFound = Glob.match(currentPath, mFilename, mustOpenAllFiles);
                int foundFiles = filesFound != null ? filesFound.size() : 0;
                if (foundFiles > 0 && !mustOpenAllFiles)
                    throw new MultipleFilesFoundError(String.format("Multiple files with the same" +
                            " name found at path: \"%s\" - run \"readSource(true)\" for avoiding " +
                            "this error", path));
                else if (foundFiles > 0)
                    allFoundFiles.addAll(filesFound);
            }
        }
        allFoundFiles.trimToSize();
        if (allFoundFiles.size() == 0)
            throw new FileNotFoundException(String.format("No file with name \"%s\" was found at " +
                    "any of the provided directories", mFilename));
        final StringBuilder results = new StringBuilder(allFoundFiles.size());
        final AtomicInteger currentAddedString = new AtomicInteger(0);
        IntStream.range(0, allFoundFiles.size()).parallel().forEachOrdered(i -> {
            File currentFile = allFoundFiles.get(i);
            try {
                BufferedReader reader = new BufferedReader(new FileReader(currentFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    while (i != currentAddedString.get()) {
                        synchronized (lock) { // for adding files at the found order
                            lock.wait();
                        }
                    }
                    results.append(line).append("\n");
                }
            } catch (IOException ignored) {
                System.err.println("Exception on thread: #" + i +
                        "\nMessage: " + ignored.getMessage());
                // This will never happen as we have ensured that all files exists
            } catch (InterruptedException e) {
                System.err.println("Interruption while executing! - Thread #" + i);
            }
            if (i < (allFoundFiles.size() - 1))
                results.append(fileSeparator);
            currentAddedString.incrementAndGet();
            synchronized (lock) {
                lock.notifyAll();
            }
        });
        mReadData = results.toString();
        mFileSeparator = allFoundFiles.size() > 1 ? fileSeparator : null;
    }

    /**
     * Obtains all the read data obtained after executing {@link #readSource()} methods
     *
     * @return {@code String} with the data
     */
    public String getReadData() {
        return mReadData;
    }

    /**
     * Obtains the file separator used when writing/reading the files - can be null if only one file
     * was read/written
     *
     * @return {@code String} with the file separator
     */
    public String getFileSeparator() {
        return mFileSeparator;
    }

    /**
     * Writes the read object to the specified destination given at {@code destination}. If it does
     * not exists, {@code FileToBytesExporter} will create all the necessary directories in order to
     * work as expected.
     *
     * @param destination relative or complete path to the output file - cannot be only dir.
     *
     * @throws IOException when there is an error by creating necessary directories or by writing
     *                     the file.
     */
    public void writeObject(File destination) throws IOException {
        if (!destination.exists()) {
            if (destination.isDirectory()) {
                if (!destination.mkdirs()) {
                    throw new IOException(String.format("Impossible to create the required " +
                                    "directories and file for the specified location: \"%s\"",
                            destination.toString()));
                }
            } else {
                if (!destination.createNewFile()) {
                    throw new IOException(String.format("Impossible to create the required " +
                                    "directories and file for the specified location: \"%s\"",
                            destination.toString()));
                }
            }
        }
        if (destination.isDirectory())
            throw new IOException(String.format("Destination file \"%s\" is a directory, not a " +
                    "file.", destination.toString()));
        try (ObjectOutputStream outputStream =
                     new ObjectOutputStream(new FileOutputStream(destination))) {
            String hash = getHash(mReadData);
            String[] output = new String[]{mFileSeparator, hash, mReadData};
            outputStream.writeObject(output);
        }
    }

    /**
     * Reads the data contained at {@code source} obtaining its {@link #mFileSeparator file
     * separator} (used if more than one file was read) and the {@link #mReadData file data}.
     *
     * Both file separator and data can be obtained by using {@link #getFileSeparator()} and {@link
     * #getReadData()}
     *
     * @param source relative or complete path to the file - cannot be only a directory
     *
     * @throws IOException        if the {@link FileNotFoundException file was not found}, there was
     *                            an error recovering the data or it is a {@link IOException
     *                            directory}.
     * @throws ClassCastException if the retrieved data is not a {@code String[]}
     * @throws FileError          if the obtained hash from file is not the same as the generated
     *                            one from the data extracted from the file.
     */
    public void readObject(File source) throws IOException, ClassCastException {
        if (!source.exists())
            throw new FileNotFoundException(String.format("File \"%s\" does not exists.",
                    source.toString()));
        if (source.isDirectory())
            throw new IOException(String.format("Source file \"%s\" is a directory, not a file.",
                    source.toString()));
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(source))) {
            String[] input = (String[]) inputStream.readObject();
            mFileSeparator = input[0];
            String obtainedHash = input[1];
            mReadData = input[2];
            String generatedHash = getHash(mReadData);
            if (!obtainedHash.equals(generatedHash))
                throw new FileError(String.format("Hashes are not equal! - file probably has been" +
                        " modified.\n\tObtained hash from file: %s\n\tGenerated hash from data: " +
                        "%s", obtainedHash, generatedHash));
        } catch (ClassNotFoundException e) {
            throw new ClassCastException(String.format("The read class is not a String[]. Have " +
                    "you altered the file \"%s\"?", source.toString()));
        }
    }

    /**
     * Obtains the hash of the specified {@code source} by using {@link
     * MessageDigest#digest(byte[])} method, generating a {@link MessageDigest#getInstance(String)
     * SHA-256 hash}. If it fails because {@link NoSuchAlgorithmException no SHA-256 hashing
     * algorithm} was found, it just returns the {@link String#hashCode()} casted to {@code
     * String}.<p>
     *
     * You can {@code @Override} this method if you inherit from {@code FileToBytesExporter} if you
     * need any other {@link StandardCharsets} charset.
     *
     * @param source original {@code String} where obtaining bytes from.
     *
     * @return {@code String} with the SHA-256 hash of {@code source}.
     */
    protected String getHash(String source) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] sourceBytes = source.getBytes(StandardCharsets.UTF_8);
            byte[] byteSourceStringHash = digest.digest(sourceBytes);
            return new String(byteSourceStringHash);
        } catch (NoSuchAlgorithmException ignored) {
            return String.valueOf(source.hashCode());
        }
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an equivalence relation on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     * {@code x}, {@code x.equals(x)} should return {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     * {@code x} and {@code y}, {@code x.equals(y)} should return {@code true} if and only if {@code
     * y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     * {@code x}, {@code y}, and {@code z}, if {@code x.equals(y)} returns {@code true} and {@code
     * y.equals(z)} returns {@code true}, then {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     * {@code x} and {@code y}, multiple invocations of {@code x.equals(y)} consistently return
     * {@code true} or consistently return {@code false}, provided no information used in {@code
     * equals} comparisons on the objects is modified.
     * <li>For any non-null reference value {@code x},
     * {@code x.equals(null)} should return {@code false}.
     * </ul>
     * <p>
     * The {@code equals} method for class {@code Object} implements the most discriminating
     * possible equivalence relation on objects; that is, for any non-null reference values {@code
     * x} and {@code y}, this method returns {@code true} if and only if {@code x} and {@code y}
     * refer to the same object ({@code x == y} has the value {@code true}).
     * <p>
     * Note that it is generally necessary to override the {@code hashCode} method whenever this
     * method is overridden, so as to maintain the general contract for the {@code hashCode} method,
     * which states that equal objects must have equal hash codes.
     *
     * @param o the reference object with which to compare.
     *
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     *
     * @see #hashCode()
     * @see java.util.HashMap
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileToBytesExporter exporter = (FileToBytesExporter) o;
        return Objects.equals(mFilename, exporter.mFilename) &&
                Objects.equals(mPath, exporter.mPath) &&
                Objects.equals(mReadData, exporter.mReadData) &&
                Objects.equals(mFileSeparator, exporter.mFileSeparator) &&
                Objects.equals(lock, exporter.lock);
    }

    /**
     * Returns a hash code value for the object. This method is supported for the benefit of hash
     * tables such as those provided by {@link java.util.HashMap}.
     * <p>
     * The general contract of {@code hashCode} is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     * an execution of a Java application, the {@code hashCode} method must consistently return the
     * same integer, provided no information used in {@code equals} comparisons on the object is
     * modified. This integer need not remain consistent from one execution of an application to
     * another execution of the same application.
     * <li>If two objects are equal according to the {@code equals(Object)}
     * method, then calling the {@code hashCode} method on each of the two objects must produce the
     * same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     * according to the {@link java.lang.Object#equals(java.lang.Object)} method, then calling the
     * {@code hashCode} method on each of the two objects must produce distinct integer results.
     * However, the programmer should be aware that producing distinct integer results for unequal
     * objects may improve the performance of hash tables.
     * </ul>
     * <p>
     * As much as is reasonably practical, the hashCode method defined by class {@code Object} does
     * return distinct integers for distinct objects. (The hashCode may or may not be implemented as
     * some function of an object's memory address at some point in time.)
     *
     * @return a hash code value for this object.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     * @see java.lang.System#identityHashCode
     */
    @Override
    public int hashCode() {
        return Objects.hash(mFilename, mPath, mReadData, mFileSeparator, lock);
    }

    /**
     * Creates and returns a copy of this object.  The precise meaning of "copy" may depend on the
     * class of the object. The general intent is that, for any object {@code x}, the expression:
     * <blockquote>
     * <pre>
     * x.clone() != x</pre></blockquote>
     * will be true, and that the expression:
     * <blockquote>
     * <pre>
     * x.clone().getClass() == x.getClass()</pre></blockquote>
     * will be {@code true}, but these are not absolute requirements. While it is typically the case
     * that:
     * <blockquote>
     * <pre>
     * x.clone().equals(x)</pre></blockquote>
     * will be {@code true}, this is not an absolute requirement.
     * <p>
     * By convention, the returned object should be obtained by calling {@code super.clone}.  If a
     * class and all of its superclasses (except {@code Object}) obey this convention, it will be
     * the case that {@code x.clone().getClass() == x.getClass()}.
     * <p>
     * By convention, the object returned by this method should be independent of this object (which
     * is being cloned).  To achieve this independence, it may be necessary to modify one or more
     * fields of the object returned by {@code super.clone} before returning it.  Typically, this
     * means copying any mutable objects that comprise the internal "deep structure" of the object
     * being cloned and replacing the references to these objects with references to the copies.  If
     * a class contains only primitive fields or references to immutable objects, then it is usually
     * the case that no fields in the object returned by {@code super.clone} need to be modified.
     * <p>
     * The method {@code clone} for class {@code Object} performs a specific cloning operation.
     * First, if the class of this object does not implement the interface {@code Cloneable}, then a
     * {@code CloneNotSupportedException} is thrown. Note that all arrays are considered to
     * implement the interface {@code Cloneable} and that the return type of the {@code clone}
     * method of an array type {@code T[]} is {@code T[]} where T is any reference or primitive
     * type. Otherwise, this method creates a new instance of the class of this object and
     * initializes all its fields with exactly the contents of the corresponding fields of this
     * object, as if by assignment; the contents of the fields are not themselves cloned. Thus, this
     * method performs a "shallow copy" of this object, not a "deep copy" operation.
     * <p>
     * The class {@code Object} does not itself implement the interface {@code Cloneable}, so
     * calling the {@code clone} method on an object whose class is {@code Object} will result in
     * throwing an exception at run time.
     *
     * @return a clone of this instance.
     *
     * @see Cloneable
     */
    @Override
    protected Object clone() {
        return new FileToBytesExporter(mFilename, mPath, mReadData, mFileSeparator);
    }


    /**
     * Returns a string representation of the object. In general, the {@code toString} method
     * returns a string that "textually represents" this object. The result should be a concise but
     * informative representation that is easy for a person to read. It is recommended that all
     * subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object} returns a string consisting of the name
     * of the class of which the object is an instance, the at-sign character `{@code @}', and the
     * unsigned hexadecimal representation of the hash code of the object. In other words, this
     * method returns a string equal to the value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Private custom class for synchronizing monitor locks.
     */
    private static final class Lock {
    }

    /**
     * Class for searching and obtaining files that corresponds to a custom glob.
     */
    private static final class Glob {
        /**
         * <p>
         * Matches all files corresponding the specified glob (
         * <a href="https://en.wikipedia.org/wiki/Glob_(programming)">see Glob</a>
         * for more information).<br /><br />
         *
         * Glob is only working at the current working directory, that must be included always,
         * cannot be {@code null} and it must {@link File#exists() exists}. If not, a {@link
         * InvalidPathException} is thrown.<br /><br />
         *
         * The {@code String} glob must contain a filename or a glob sequence (see the link
         * referenced before for more information). It cannot be {@code null} or an empty String
         * ({@code ""}). If not, an {@link IllegalArgumentException} is thrown.<br /><br />
         * </p>
         *
         * @param workingDirectory current working directory as {@link File} - it must exists and
         *                         cannot be {@code null}.
         * @param glob             glob that matches a filename or a
         *                         <a href="https://en.wikipedia.org/wiki/Glob_(programming)">glob
         *                         sequence</a>. It cannot be {@code null} or an empty String {@code
         *                         ""}.
         *
         * @return {@code ArrayList} with the found files, {@code null} if no files were found.
         *
         * @throws IOException              if there was an error while visiting some file for
         *                                  obtaining its information, with the filename and a
         *                                  complete cause.
         * @throws InvalidPathException     when the {@code workingDirectory} does not exists or it
         *                                  is {@code null}.
         * @throws IllegalArgumentException when the {@code glob} is {@code null} or an empty String
         *                                  {@code ""}.
         */
        static ArrayList<File> match(final File workingDirectory, final String glob,
                                     boolean mustInspectAllDirs)
                throws IOException {
            if (workingDirectory == null || !workingDirectory.exists())
                throw new InvalidPathException("The provided working directory is not valid!");
            if (glob == null || glob.equals(""))
                throw new IllegalArgumentException("The provided glob is not valid!");
            final PathMatcher pathMatcher =
                    FileSystems.getDefault().getPathMatcher("glob:" + glob);
            final ArrayList<File> matches = new ArrayList<>(100);
            int maxDepth = mustInspectAllDirs ? Integer.MAX_VALUE : 1;
            EnumSet<FileVisitOption> options = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
            Visitor<Path> fileVisitor = new Visitor<>(pathMatcher, matches);
            Files.walkFileTree(workingDirectory.toPath(), options, maxDepth, fileVisitor);
            matches.trimToSize();
            return matches.size() > 0 ? matches : null;
        }

        private static final class Visitor<E extends Path> extends SimpleFileVisitor<E> {
            private PathMatcher mPathMatcher;
            private ArrayList<File> mMatches;

            /**
             * Do not let anyone use this constructor
             */
            private Visitor() {
                super();
            }

            /**
             * Generates an instance for the visitor.
             *
             * @param pathMatcher defined matcher for finding files - cannot be {@code null}.
             * @param matches     {@code ArrayList} of {@code Files} which will contain all the
             *                    results - cannot be {@code null}.
             *
             * @throws NullPointerException if {@code pathMatcher} or {@code matches} are null.
             */
            public Visitor(PathMatcher pathMatcher, ArrayList<File> matches) {
                if (pathMatcher == null || matches == null)
                    throw new NullPointerException("Visitor params cannot be null");
                mPathMatcher = pathMatcher;
                mMatches = matches;
            }

            /**
             * Invoked for a file in a directory.
             *
             * <p> Unless overridden, this method returns {@link
             * FileVisitResult#CONTINUE CONTINUE}.
             *
             * @param file  current file that is being visited
             * @param attrs file attributes which determines if it is a dir, or a file, creation
             *              date, etc.
             */
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (mPathMatcher.matches(file.getFileName()) && !attrs.isDirectory())
                    mMatches.add(file.toFile());
                return FileVisitResult.CONTINUE;
            }

            /**
             * Invoked for a file that could not be visited.
             *
             * <p> Unless overridden, this method re-throws the I/O
             * exception that prevented the file from being visited.
             *
             * @param file file that could not be visited
             * @param exc  exception thrown with the cause of not being able to visit that file
             *
             * @throws FileError with the file information and thrown exception
             */
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                throw new FileError(String.format("There was an error visiting file: \"%s\"",
                        file.toString()), exc.getCause());
            }
        }
    }
}
