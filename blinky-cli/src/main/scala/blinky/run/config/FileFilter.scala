package blinky.run.config

import ammonite.ops.RelPath

sealed trait FileFilter

object FileFilter {

  case class SingleFileOrFolder(fileOrFolder: RelPath) extends FileFilter

  case class FileName(fileName: String) extends FileFilter

}
