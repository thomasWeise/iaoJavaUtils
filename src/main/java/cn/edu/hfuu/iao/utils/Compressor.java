package cn.edu.hfuu.iao.utils;

import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;

/** Use a pre-installed tar.xz command to compress files and folders. */
public class Compressor {

  /**
   * Compress all the files in the given {@code folderToCompress} to the
   * {@code destination} folder or path
   *
   * @param pathToCompress
   *          the folder or file to compress
   * @param destination
   *          the destination tile or path, or {@code null} to generate a
   *          file name
   */
  public static final void compress(final Path pathToCompress,
      final Path destination) {

    final Path sourcePath = IOUtils.canonicalize(pathToCompress);
    final Path sourceFolder = IOUtils.canonicalize(sourcePath.getParent());
    final String sourceName = sourcePath.getFileName().toString();

    final Path destFolder, archivePath;
    final String archiveName;

    // create the paths and names for the destination files
    setup: {
      if (destination == null) {
        destFolder = sourceFolder;
      } else {
        final Path dest = IOUtils.canonicalize(destination);
        if (Files.isDirectory(dest)) {
          destFolder = dest;
        } else {
          destFolder = IOUtils.canonicalize(dest.getParent());
          archivePath = dest;
          archiveName = archivePath.getFileName().toString();
          break setup;
        }
      }

      archiveName = IOUtils.removeFileExtension(sourceName) + ".tar.xz"; //$NON-NLS-1$
      archivePath = IOUtils.canonicalize(destFolder.resolve(archiveName));
    }

    try {
      final ProcessBuilder pb = new ProcessBuilder();

      ConsoleIO.stdout(//
          "Trying to build archive '"//$NON-NLS-1$
              + archiveName + "' as file '" + //$NON-NLS-1$
              archivePath + "' from object '" + //$NON-NLS-1$
              sourceName + "' identified by path '" //$NON-NLS-1$
              + sourcePath + "' into folder '" + //$NON-NLS-1$
              destFolder + '\'');

      pb.command(//
          "tar", //$NON-NLS-1$
          "-cJf", //$NON-NLS-1$
          archivePath.toFile().toString(), //
          sourceName);

      pb.environment().put("XZ_OPT", //$NON-NLS-1$
          "-9e"); //$NON-NLS-1$
      pb.directory(sourceFolder.toFile());
      pb.redirectErrorStream(true);
      pb.redirectOutput(Redirect.INHERIT);

      final int result = pb.start().waitFor();

      ConsoleIO.stdout(//
          "Finished building archive '"//$NON-NLS-1$
              + archiveName + "' as file '" + //$NON-NLS-1$
              archivePath + "' from object '" + //$NON-NLS-1$
              sourceName + "' identified by path '" //$NON-NLS-1$
              + sourcePath + "' into folder '" + //$NON-NLS-1$
              destFolder + "', return code " + result);//$NON-NLS-1$
    } catch (final Throwable error) {
      ConsoleIO.stderr(//
          "Failed to build archive archive '"//$NON-NLS-1$
              + archiveName + "' as file '" + //$NON-NLS-1$
              archivePath + "' from object '" + //$NON-NLS-1$
              sourceName + "' identified by path '" //$NON-NLS-1$
              + sourcePath + "' into folder '" + //$NON-NLS-1$
              destFolder + //
              "', maybe you do not have the tar command installed.", //$NON-NLS-1$
          error);
    }
  }
}
